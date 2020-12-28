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

import java.util.Random;

public class PlayerAgent extends Agent {
    public long requestInterval = 1000;
    public boolean inBattle = false;
    public Player player;
    Random rnd = new Random();

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

        addBehaviour(new PlayerAgentBehaviour(this));
    }

    @Override
    protected void takeDown() {
        System.out.println("---> " + getLocalName() + " : Good bye");

        try
        {
            DFService.deregister(this);
        }
        catch (FIPAException e)
        {
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
                if (receiver!=null){
                    message.addReceiver(receiver);
                    message.setContent(player.serialize());
                    message.setProtocol(Constants.PLAYER_DF);
                    send(message);
                }
            }
        }

        private class PlayerBehaviour extends ParallelBehaviour {
            public PlayerBehaviour() {
                addSubBehaviour(new RequestToBattleBehaviour(myAgent,requestInterval));
                addSubBehaviour(new UpdateStatsBehaviour());
            }

            private class UpdateStatsBehaviour extends CyclicBehaviour {
                @Override
                public void action() {
                    MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
                    ACLMessage answer = receive(mt);
                    if (answer == null) block();
                    else {
                        String content = answer.getContent();
                        Player p = Model.deserialize(content, Player.class);
                        player.setNbrDefeat(p.getNbrDefeat());
                        player.setNbrVictory(p.getNbrVictory());
                        player.setCharacteristics(p.getCharacteristics());
                    }
                }
            }

            private class RequestToBattleBehaviour extends TickerBehaviour {
                public RequestToBattleBehaviour(Agent a, long period) {
                    super(a, period);
                }

                @Override
                protected void onTick() {
                    if (!inBattle) {
                        ACLMessage request = new ACLMessage(ACLMessage.REQUEST);
                        AID receiver = DFTool.findFirstAgent(getAgent(), Constants.MATCHMAKER_DF, Constants.MATCHMAKER_DF);
                        request.addReceiver(receiver);
                        request.setContent(player.serialize());
                        addBehaviour(new RequestToBattleInitiator(myAgent,request));
                    }
                }
            }

            private class RequestToBattleInitiator extends AchieveREInitiator {
                public RequestToBattleInitiator(Agent myAgent, ACLMessage request) {
                    super(myAgent,request);
                }

                @Override
                protected void handleAgree(ACLMessage agree) {
                    inBattle = true;
                    System.out.println(agree.getContent());
                }
            }
        }

        private class SubscribeRankingBehaviour extends OneShotBehaviour {
            @Override
            public void action() {
                ACLMessage message = new ACLMessage(ACLMessage.SUBSCRIBE);
                AID receiver = DFTool.findFirstAgent(getAgent(), Constants.RANKING_DF, Constants.RANKING_DF);
                if (receiver!=null){
                    message.addReceiver(receiver);
                    message.setContent(player.serialize());
                    message.setProtocol(Constants.PLAYER_DF);
                    send(message);
                }
            }
        }
    }
}
