package agents;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.*;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.AchieveREInitiator;
import tools.*;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static java.lang.System.out;

public class PlayerAgent extends Agent {
    public long requestInterval;
    public boolean inBattle = false;
    public Player player;
    Random rnd = new Random();
    List<String> actions = Arrays.asList(Constants.ATTACK, Constants.DODGE, Constants.DEFENSE, Constants.CAST_SPELL, Constants.USE_OBJECT);

    @Override
    protected void setup() {
        System.out.println(getLocalName() + "--> Installed");
        Characteristics characteristics = new Characteristics(
                (rnd.nextInt(10) * 10 + 1),
                (rnd.nextInt(10) * 10 + 1),
                20,
                (rnd.nextInt(10) * 3 + 1),
                (rnd.nextInt(10) * 3 + 1),
                0,
                0);
        this.player = new Player(this.getLocalName(), 0, 0, characteristics);
        //Enregistrement via le DF
        DFTool.registerAgent(this, Constants.PLAYER_DF, getLocalName());
        this.requestInterval = (rnd.nextInt(6)+1)*1000;
        addBehaviour(new PlayerAgentBehaviour(this));
    }

    @Override
    protected void takeDown() {
        System.out.println("---> " + getLocalName() + " : Good bye");

        try {
            DFService.deregister(this);
        } catch (FIPAException e) {
            e.printStackTrace();
        }
    }

    private class PlayerAgentBehaviour extends SequentialBehaviour {
        public PlayerAgentBehaviour(Agent a) {
            super(a);
            addSubBehaviour(new SubscribeMatchMakerBehaviour());
            addSubBehaviour(new SubscribeRankingBehaviour());
            addSubBehaviour(new PlayerBehaviour());
        }

        private class SubscribeMatchMakerBehaviour extends OneShotBehaviour {
            @Override
            public void action() {
                ACLMessage message = new ACLMessage(ACLMessage.SUBSCRIBE);
                AID receiver = DFTool.findFirstAgent(getAgent(), Constants.MATCHMAKER_DF, Constants.MATCHMAKER_DF);
                if (receiver != null) {
                    message.addReceiver(receiver);
                    message.setContent(player.serialize());
                    message.setProtocol(Constants.PLAYER_DF);
                    send(message);
                }
            }
        }

        private class PlayerBehaviour extends OneShotBehaviour {
           /* public PlayerBehaviour() {
                addSubBehaviour(new RequestToBattleBehaviour(myAgent, requestInterval));
                //addSubBehaviour(new ActionPlayerBehaviour());
                addSubBehaviour(new PlayerOperationBehaviour());
            }*/

            @Override
            public void action() {
                addBehaviour(new RequestToBattleBehaviour(myAgent,requestInterval));
                addBehaviour(new PlayerOperationBehaviour());
                //addBehaviour(new ActionPlayerBehaviour());
            }

            private class PlayerOperationBehaviour extends CyclicBehaviour {
                @Override
                public void action() {
                    MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
                    ACLMessage message = receive(mt);
                    //System.out.println(getLocalName()+ "-"+ message);
                    if (message == null) block();
                    else {
                        String content = message.getContent();
                        //System.out.println(content);
                        PlayerOperation pOperation = Model.deserialize(content, PlayerOperation.class);
                        if (pOperation.getOperation().equals(Constants.UPDATE_PLAYER)){
                            Player p = pOperation.getPlayer();
                            //System.out.println(p.getAgentName());
                            player.setNbrDefeat(p.getNbrDefeat());
                            player.setNbrVictory(p.getNbrVictory());
                            player.setCharacteristics(p.getCharacteristics());
                            ACLMessage reply = message.createReply();
                            reply.setPerformative(ACLMessage.INFORM);
                            //reply.setContent(p.serialize());
                            send(reply);
                            addBehaviour(new GetRankingBehaviour());
                            inBattle = false;
                        }
                        if (pOperation.getOperation().equals(Constants.ACTION_PLAYER)){
                            ACLMessage reply = message.createReply();
                            reply.setPerformative(ACLMessage.INFORM);
                            PlayerAction pAction = new PlayerAction(player.getAgentName(), actions.get(rnd.nextInt(actions.size())));
                            reply.setContent(pAction.serialize());
                            //System.out.println(reply);
                            send(reply);
                        }
                    }
                }

                private class GetRankingBehaviour extends OneShotBehaviour {
                    @Override
                    public void action() {
                        ACLMessage request = new ACLMessage(ACLMessage.REQUEST);
                        AID receiver = DFTool.findFirstAgent(getAgent(), Constants.RANKING_DF, Constants.RANKING_DF);
                        request.addReceiver(receiver);
                        PlayerOperation rOperation = new PlayerOperation(Constants.RANKING_PLAYER, player);
                        request.setContent(rOperation.serialize());
                        addBehaviour(new GetRankingInitiator(myAgent, request));
                    }

                    private class GetRankingInitiator extends AchieveREInitiator {
                        public GetRankingInitiator(Agent myAgent, ACLMessage request) {
                            super(myAgent,request);
                        }

                        @Override
                        protected void handleInform(ACLMessage inform) {
                            PlayerRanking playerRanking;
                            playerRanking = Model.deserialize(inform.getContent(), PlayerRanking.class);
                            assert playerRanking != null;
                            out.println(getLocalName()+" : "+"Classement " + player.getAgentName() + " - rapport victoire/défaite : " + playerRanking.getWinrateR() + " - niveau : " + playerRanking.getLevelR());
                        }
                    }
                }
            }

            private class RequestToBattleBehaviour extends TickerBehaviour {
                public RequestToBattleBehaviour(Agent a, long period) {
                    super(a, period);
                }

                @Override
                protected void onTick() {
                    //System.out.println(getLocalName()+" ontick ");
                    if (!inBattle) {
                        ACLMessage request = new ACLMessage(ACLMessage.REQUEST);
                        AID receiver = DFTool.findFirstAgent(getAgent(), Constants.MATCHMAKER_DF, Constants.MATCHMAKER_DF);
                        request.addReceiver(receiver);
                        request.setContent(player.serialize());
                        addBehaviour(new RequestToBattleInitiator(myAgent, request));
                    }
                }
            }

            private class RequestToBattleInitiator extends AchieveREInitiator {
                public RequestToBattleInitiator(Agent myAgent, ACLMessage request) {
                    super(myAgent, request);
                }

                @Override
                protected void handleAgree(ACLMessage agree) {
                    inBattle = true;
                    System.out.println(agree.getContent());
                }

                @Override
                protected void handleRefuse(ACLMessage refuse) {
                    inBattle = false;
                    System.out.println(refuse.getContent());
                }
            }

            private class ActionPlayerBehaviour extends CyclicBehaviour {
                @Override
                public void action() {
                    MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
                    ACLMessage message = receive(mt);
                    //System.out.println(message);
                    if (message != null) {
                        //System.out.println(message);
                        String content = message.getContent();
                        PlayerOperation pOperation = Model.deserialize(content, PlayerOperation.class);
                        if (pOperation.getOperation().equals(Constants.ACTION_PLAYER)){
                            ACLMessage reply = message.createReply();
                            reply.setPerformative(ACLMessage.INFORM);
                            PlayerAction pAction = new PlayerAction(player.getAgentName(), actions.get(rnd.nextInt(actions.size())));
                            reply.setContent(pAction.serialize());
                            //System.out.println(reply);
                            send(reply);
                        }
                    } else {
                        block();
                    }
                }
            }
        }

        private class SubscribeRankingBehaviour extends OneShotBehaviour {
            @Override
            public void action() {
                ACLMessage message = new ACLMessage(ACLMessage.SUBSCRIBE);
                AID receiver = DFTool.findFirstAgent(getAgent(), Constants.RANKING_DF, Constants.RANKING_DF);
                if (receiver != null) {
                    message.addReceiver(receiver);
                    message.setContent(player.serialize());
                    message.setProtocol(Constants.PLAYER_DF);
                    send(message);
                }
            }
        }
    }
}
