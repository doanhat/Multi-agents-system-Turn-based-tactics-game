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
import java.util.Map;

public class MatchmakerAgent extends Agent {
    private static final long serialVersionUID = 1L;
    private ArrayList<Player> allPlayers;
    private ArrayList<Player> playerReadyList;
    private HashMap<String,ArrayList<Player>> arenaAgentMap;
    private String rankingAgent;
    private ObjectMapper objectMapper;

    @Override
    protected void setup() {
        System.out.println(getLocalName() + "--> Installed");
        this.allPlayers = new ArrayList<>();
        this.playerReadyList = new ArrayList<>();
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

    private class MatchMakerAgentBehaviour extends ParallelBehaviour {
        public MatchMakerAgentBehaviour() {
            addSubBehaviour(new WaitForSubscriptionBehaviour(myAgent,ParallelBehaviour.WHEN_ALL));
            addSubBehaviour(new MatchMakerBehaviour());
        }
        private class WaitForSubscriptionBehaviour extends ParallelBehaviour {
            private static final long serialVersionUID = 1L;

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

        private class MatchMakerBehaviour extends ParallelBehaviour {
            public MatchMakerBehaviour() {
                System.out.println("Matchmaker Behaviour");
                addSubBehaviour(new PlayerRequestToBattleBehaviour());
            }

            private class PlayerRequestToBattleBehaviour extends CyclicBehaviour {
                @Override
                public void action() {
                    MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
                    ACLMessage message = receive(mt);
                    if (message != null){
                        Player player = Model.deserialize(message.getContent(), Player.class);
                        ACLMessage reply = message.createReply();
                        addPlayerToArena(player,reply);
                    }
                }

                private void addPlayerToArena(Player player, ACLMessage reply) {
                    for (Map.Entry<String, ArrayList<Player>> entry : arenaAgentMap.entrySet()) {
                        String arenaAgentName = entry.getKey();
                        ArrayList<Player> arenaPlace = entry.getValue();
                        if (arenaPlace.size()<=Constants.NBR_PLAYER_PER_TEAM*2 && !arenaHasPlayer(arenaPlace,player)){
                            arenaPlace.add(player);
                            ACLMessage request = new ACLMessage(ACLMessage.REQUEST);
                            AID receiver = DFTool.findFirstAgent(getAgent(), Constants.ARENA_DF, arenaAgentName);
                            request.addReceiver(receiver);
                            request.setContent(player.serialize());
                            addBehaviour(new RequestAddPlayerToArenaInitiator(myAgent,request,reply));
                        }
                    }
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

                    public RequestAddPlayerToArenaInitiator(Agent myAgent, ACLMessage request, ACLMessage reply) {
                        super(myAgent,request);
                        this.reply = reply;
                    }

                    @Override
                    protected void handleInform(ACLMessage inform) {
                        reply.setContent("Joueur a été ajouté dans une arène");
                        reply.setPerformative(ACLMessage.AGREE);
                        send(reply);
                    }
                }
            }
        }
    }
}
