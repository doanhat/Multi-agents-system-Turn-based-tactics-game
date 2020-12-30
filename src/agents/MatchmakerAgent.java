package agents;

import com.fasterxml.jackson.databind.ObjectMapper;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.*;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.AchieveREInitiator;
import tools.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Agent MatchMaker : Organisateur des combats.
 */
public class MatchmakerAgent extends Agent {
    private static final long serialVersionUID = 1L;
    private ArrayList<Player> allPlayers;
    private HashMap<String,ArrayList<Player>> arenaAgentMap;
    private String rankingAgent;
    private ObjectMapper objectMapper;

    @Override
    protected void setup() {
        System.out.println(getLocalName() + "--> Installed");
        this.allPlayers = new ArrayList<>();
        this.arenaAgentMap = new HashMap<>();
        //Enregistrement via le DF
        DFTool.registerAgent(this, Constants.MATCHMAKER_DF, Constants.MATCHMAKER_DF);

        addBehaviour(new MatchMakerAgentBehaviour());
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

    private class MatchMakerAgentBehaviour extends SequentialBehaviour {
        public MatchMakerAgentBehaviour() {
            addSubBehaviour(new WaitForSubscriptionBehaviour(myAgent,ParallelBehaviour.WHEN_ALL));
            addSubBehaviour(new MatchMakerBehaviour());
        }
        private class WaitForSubscriptionBehaviour extends ParallelBehaviour {
            private static final long serialVersionUID = 1L;

            /**
             * Attendre les souscriptions des joueurs, des arènes et l'agent de classement
             *
             * @param agent     agent
             * @param condition condition
             */
            public WaitForSubscriptionBehaviour(Agent agent, int condition) {
                super(agent,condition);
                addSubBehaviour(new WaitForPlayersBehaviour());
                addSubBehaviour(new WaitForArenasBehaviour());
                addSubBehaviour(new WaitForRankingBehaviour());
            }

            private class WaitForPlayersBehaviour extends Behaviour {
                private int counter = Constants.NBR_PLAYER;

                @Override
                public void action() {
                    MessageTemplate mt = MessageTemplate.and(
                            MessageTemplate.MatchPerformative(ACLMessage.SUBSCRIBE),
                            MessageTemplate.MatchProtocol(Constants.PLAYER_DF));
                    ACLMessage answer = getAgent().receive(mt);
                    if (answer == null) block();
                    else {
                        String content = answer.getContent();
                        Player player = Model.deserialize(content, Player.class);
                        allPlayers.add(player);
                        counter--;
                    }

                }

                @Override
                public boolean done() {
                    return counter == 0;
                }
            }

            private class WaitForArenasBehaviour extends Behaviour {
                private int counter = Constants.NBR_ARENA;

                @Override
                public void action() {
                    MessageTemplate mt = MessageTemplate.and(
                            MessageTemplate.MatchPerformative(ACLMessage.SUBSCRIBE),
                            MessageTemplate.MatchProtocol(Constants.ARENA_DF));
                    ACLMessage answer = getAgent().receive(mt);
                    if (answer == null) block();
                    else {
                        String content = answer.getContent();
                        arenaAgentMap.put(content,new ArrayList<>());
                        counter--;
                    }

                }

                @Override
                public boolean done() {
                    return counter == 0;
                }
            }

            private class WaitForRankingBehaviour extends Behaviour {
                @Override
                public void action() {
                    MessageTemplate mt = MessageTemplate.and(
                            MessageTemplate.MatchPerformative(ACLMessage.SUBSCRIBE),
                            MessageTemplate.MatchProtocol(Constants.RANKING_DF));
                    ACLMessage answer = getAgent().receive(mt);
                    if (answer == null) block();
                    else {
                        String content = answer.getContent();
                        rankingAgent = content;
                    }

                }

                @Override
                public boolean done() {
                    return rankingAgent != null;
                }
            }
        }

        private class MatchMakerBehaviour extends OneShotBehaviour {
            /**
             * Instantiates a new Match maker behaviour.
             */

            @Override
            public void action() {
                System.out.println("Matchmaker Behaviour");
                addBehaviour(new RequestToMatchMakerBehaviour());
                addBehaviour(new ArenaFreePlaceBehaviour());

            }

            private class RequestToMatchMakerBehaviour extends CyclicBehaviour {
                @Override
                public void action() {

                    MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
                    ACLMessage message = receive(mt);
                    if (message != null){
                        if (message.getContent().contains(Constants.FREE_ARENA)){
                            AgentRequest agentRequest = Model.deserialize(message.getContent(),AgentRequest.class);
                            assert agentRequest != null;
                            String arenaName = agentRequest.getAgentName();
                            if (arenaAgentMap.containsKey(arenaName)){
                                arenaAgentMap.get(arenaName).clear();
                            }
                            ACLMessage reply = message.createReply();
                            reply.setContent(arenaName + " a été libré !");
                            reply.setPerformative(ACLMessage.INFORM);
                            send(reply);
                        } else {
                            List<String> arenaList = new ArrayList(arenaAgentMap.keySet());
                            int arenaPosition = 0;
                            while (arenaAgentMap.get(arenaList.get(arenaPosition)).size()==Constants.NBR_PLAYER_PER_TEAM*2 && arenaPosition<Constants.NBR_ARENA-1){
                                arenaPosition++;
                            }
                            String arenaAgentName = arenaList.get(arenaPosition);
                            Player player = Model.deserialize(message.getContent(), Player.class);
                            ACLMessage reply = message.createReply();
                            assert player != null;
                            addPlayerToArena(player,reply,arenaAgentName);
                        }

                    } else block();
                }

                private void addPlayerToArena(Player player, ACLMessage reply, String arenaAgentName) {
                    ACLMessage request = new ACLMessage(ACLMessage.REQUEST);
                    AID receiver = DFTool.findFirstAgent(getAgent(), Constants.ARENA_DF, arenaAgentName);
                    request.addReceiver(receiver);
                    request.setContent(player.serialize());
                    addBehaviour(new RequestAddPlayerToArenaInitiator(myAgent,request,reply,player));

                }

                private boolean arenaHasPlayer(ArrayList<Player> arenaPlace, Player player) {
                    for (Player p : arenaPlace) {
                        if (p.getAgentName().equals(player.getAgentName())){
                            return true;
                        }
                    }
                    return false;
                }

                private class RequestAddPlayerToArenaInitiator extends AchieveREInitiator {

                    private final ACLMessage reply;
                    private Player player;

                    /**
                     * Instantiates a new Request add player to arena initiator.
                     *  @param myAgent my agent
                     * @param request request
                     * @param reply   reply
                     * @param player
                     */
                    public RequestAddPlayerToArenaInitiator(Agent myAgent, ACLMessage request, ACLMessage reply, Player player) {
                        super(myAgent,request);
                        this.reply = reply;
                        this.player = player;
                    }

                    @Override
                    protected void handleAgree(ACLMessage inform) {
                        String arenaAgentName = inform.getContent();
                        if (!arenaHasPlayer(arenaAgentMap.get(arenaAgentName),player)) {
                            arenaAgentMap.get(arenaAgentName).add(player);
                        }
                        reply.setContent(player.getAgentName()+" a été ajouté dans l'"+inform.getContent());
                        reply.setPerformative(ACLMessage.AGREE);
                        send(reply);
                    }

                    @Override
                    protected void handleRefuse(ACLMessage refuse) {
                        reply.setContent(player.getAgentName()+" doit attendre, l'"+refuse.getContent()+" est plein !");
                        reply.setPerformative(ACLMessage.REFUSE);
                        send(reply);
                    }
                }
            }

            private class ArenaFreePlaceBehaviour extends CyclicBehaviour {
                @Override
                public void action() {
                    MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
                    ACLMessage message = receive(mt);
                    if (message != null){
                        String arenaName = message.getContent();
                        if (arenaAgentMap.containsKey(arenaName)){
                            arenaAgentMap.get(arenaName).clear();
                        }
                    } else block();
                }
            }
        }
    }
}
