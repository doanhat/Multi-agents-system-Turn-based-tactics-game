package agents;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jade.core.Agent;
import jade.core.behaviours.*;
import jade.domain.DFService;
import jade.domain.FIPAException;
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
        System.out.println(getLocalName() + "--> Installed");
        this.rankingList = new RankingList();
        this.agentMatchmakerName = null;
        this.agentArenaNameList = new ArrayList<>();
        this.objectMapper = new ObjectMapper();
        //Enregistrement via le DF
        DFTool.registerAgent(this, Constants.RANKING_DF, getLocalName());

        addBehaviour(new RankingAgentBehaviour(this));

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

    private class RankingAgentBehaviour extends SequentialBehaviour {
        public RankingAgentBehaviour(Agent a) {
            super(a);
            addSubBehaviour(new RegisterBehaviour(myAgent, ParallelBehaviour.WHEN_ALL));
            addSubBehaviour(new RankingBehaviour());
        }
    }

    private class RegisterBehaviour extends ParallelBehaviour {
        public RegisterBehaviour(Agent a, int endCondition) {
            super(a, endCondition);
            addSubBehaviour(new WaitPlayerRegistration());
            addSubBehaviour(new WaitArenaRegistration());

        }
    }

    private class WaitPlayerRegistration extends Behaviour {
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
                rankingList.addOrUpdatePlayer(player);
                counter--;
            }

        }

        @Override
        public boolean done() {
            return counter == 0;
        }
    }

    private class WaitArenaRegistration extends Behaviour {
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
                RegisterModel model = Model.deserialize(content, RegisterModel.class);
                if (model.getName().contains(Constants.ARENA_DF)) {
                    agentArenaNameList.add(model.getName());
                    counter--;
                }
            }
        }

        @Override
        public boolean done() {
            return counter == 0;
        }
    }

    private class RankingPlayerBehaviour extends CyclicBehaviour {
        @Override
        public void action() {
            //traiter la demande de renvoyer le classement d'un joueur
            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
            ACLMessage message = receive(mt);
            if (message != null) {
                ACLMessage inform = message.createReply();
                inform.setPerformative(ACLMessage.INFORM);
                //Le message contient le nom précis de l'agent matchmaker enregistré dans le DF
                //inform.addReceiver(DFTool.findFirstAgent(getAgent(), Constants.MATCHMAKER_DF, Constants.MATCHMAKER_DF));

                Player p = Model.deserialize(message.getContent(), Player.class);
                int levelRanking = rankingList.getPlayerLevelRanking(p.getAgentName());
                int winrateRanking = rankingList.getPlayerWinrateRanking(p.getAgentName());

                PlayerRanking rankingMap = new PlayerRanking();
                rankingMap.setLevelR(levelRanking);
                rankingMap.setWinrateR(winrateRanking);
                //System.out.println("Classement " + p.toString() + " - rapport victoire/défaite : " + rankingMap.getWinrateR() + " - niveau : " + rankingMap.getLevelR());
                inform.setContent(rankingMap.serialize());
                //L'ID de la conversation correspond à celui de l'entité manipulée (unique pour un tour de demande)
                //inform.setConversationId(UUID.randomUUID().toString());
                send(inform);
            } else {
                block();
            }
        }
    }

    private class UpdatePlayerRankingBehaviour extends CyclicBehaviour {

        @Override
        public void action() {
            //Recoir la liste des joueurs à mettre à jour après un match
            MessageTemplate mt = MessageTemplate.and(
                    MessageTemplate.MatchPerformative(ACLMessage.INFORM),
                    MessageTemplate.MatchProtocol(Constants.ARENA_DF));
            ACLMessage message = getAgent().receive(mt);
            if (message == null) block();
            else {
                List<Player> playerList = Model.deserializeToList(message.getContent(), Player.class);
                rankingList.addOrUpdatePlayers(playerList);
            }
        }
    }

    private class RankingBehaviour extends OneShotBehaviour {
        @Override
        public void action() {
            System.out.println(agentArenaNameList);
            addBehaviour(new RankingPlayerBehaviour());
            addBehaviour(new UpdatePlayerRankingBehaviour());
        }
    }
}
