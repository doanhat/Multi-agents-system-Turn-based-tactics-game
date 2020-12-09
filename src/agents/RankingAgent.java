package agents;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.ParallelBehaviour;
import jade.core.behaviours.SequentialBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import tools.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

/**
 * Agent se charge du classement des joueurs
 */
public class RankingAgent extends Agent {
    private RankingList rankingList;
    private String agentMatchmakerName;
    private List<String> agentArenaNameList;
    private ObjectMapper objectMapper;

    @Override
    protected void setup() {
        System.out.println("Agent " + getLocalName() + " started.");
        this.rankingList = new RankingList();
        this.agentMatchmakerName = null;
        this.agentArenaNameList = new ArrayList<>();
        this.objectMapper = new ObjectMapper();
        //Enregistrement via le DF
        DFTool.registerAgent(this, Constants.RANKING_DF,getLocalName());

        addBehaviour(new RankingAgentBehaviour(this));

    }

    private class RankingAgentBehaviour extends SequentialBehaviour {
        public RankingAgentBehaviour(Agent a) {
            super(a);
            addSubBehaviour(new RegisterBehaviour(myAgent,ParallelBehaviour.WHEN_ALL));
            addSubBehaviour(new RankingBehaviour());
        }
    }

    private class RegisterBehaviour extends ParallelBehaviour {
        public RegisterBehaviour(Agent a, int endCondition) {
            super(a, endCondition);
            addSubBehaviour(new WaitMatchMakerRegistration());
            addSubBehaviour(new WaitArenaRegistration());

        }
    }

    private class WaitMatchMakerRegistration extends Behaviour {
        @Override
        public void action() {
            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.SUBSCRIBE);
            ACLMessage answer = getAgent().receive(mt);
            if(answer == null) block();
            else {
                String content = answer.getContent();
                RegisterModel model = RegisterModel.deserialize(content, RegisterModel.class);
                if (model.getName().equals(Constants.MATCHMAKER_DF)){
                    agentMatchmakerName = model.getName();
                }
            }

        }

        @Override
        public boolean done() {
            return agentMatchmakerName!=null;
        }
    }

    private class WaitArenaRegistration extends Behaviour {
        private int counter = Constants.NBR_ARENA;
        @Override
        public void action() {
            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.SUBSCRIBE);
            ACLMessage answer = getAgent().receive(mt);
            if(answer == null) block();
            else {
                String content = answer.getContent();
                RegisterModel model = Model.deserialize(content, RegisterModel.class);
                if (model.getName().contains(Constants.ARENA_DF)){
                    agentArenaNameList.add(model.getName());
                    counter--;
                }
            }
        }

        @Override
        public boolean done() {
            if (counter==0){
                System.out.println("Arena subcribed !");
            }
            return counter==0;
        }
    }

    private class RankingBehaviour extends CyclicBehaviour {
        @Override
        public void action() {
            ACLMessage message = getAgent().receive();
            if(message == null) block();
            else {
                switch(message.getPerformative()) {
                    //traiter la demande de renvoyer le classement d'un joueur
                    case ACLMessage.REQUEST:
                        HashMap<String,Player> rankingRequest = new HashMap<>();
                        try {
                            rankingRequest = objectMapper.readValue(message.getContent(),HashMap.class);
                        } catch (JsonProcessingException e) {
                            e.printStackTrace();
                        }

                        ACLMessage inform = new ACLMessage(ACLMessage.INFORM);

                        //Le message contient le nom précis de l'agent matchmaker enregistré dans le DF
                        inform.addReceiver(DFTool.findFirstAgent(getAgent(), Constants.MATCHMAKER_DF, Constants.MATCHMAKER_DF));

                        if (rankingRequest.containsKey(RankingList.RANKING)){
                            try {
                                Player p = rankingRequest.get(RankingList.RANKING);
                                int levelRanking = rankingList.getPlayerLevelRanking(p.getAgentName());
                                int winrateRanking = rankingList.getPlayerWinrateRanking(p.getAgentName());

                                HashMap<String, Integer> rankingMap = new HashMap<>();
                                rankingMap.put(RankingList.BYLEVEL,levelRanking);
                                rankingMap.put(RankingList.BYWINRATE,winrateRanking);

                                inform.setContent(objectMapper.writeValueAsString(rankingMap));
                            } catch (JsonProcessingException e) {
                                e.printStackTrace();
                            }
                        }

                        //L'ID de la conversation correspond à celui de l'entité manipulée (unique pour un tour de demande)
                        inform.setConversationId(UUID.randomUUID().toString());

                        //Envoi
                        getAgent().send(inform);
                        break;

                    //Recoir la liste des joueurs à mettre à jour après un match
                    case ACLMessage.INFORM:
                        List<Player> playerList = Model.deserializeToList(message.getContent(), Player.class);
                        rankingList.addOrUpdatePlayers(playerList);
                        break;
                }
            }
        }
    }
}
