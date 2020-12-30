package agents;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jade.core.AID;
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
    private ObjectMapper objectMapper;

    @Override
    protected void setup() {
        System.out.println(getLocalName() + "--> Installed");
        this.rankingList = new RankingList();
        this.objectMapper = new ObjectMapper();
        //Enregistrement via le DF
        DFTool.registerAgent(this, Constants.RANKING_DF, Constants.RANKING_DF);

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
            addSubBehaviour(new SubscribeMatchMakerBehaviour());
            addSubBehaviour(new WaitPlayerRegistration());
            addSubBehaviour(new RankingBehaviour());
        }

        private class SubscribeMatchMakerBehaviour extends OneShotBehaviour {
            @Override
            public void action() {
                ACLMessage message = new ACLMessage(ACLMessage.SUBSCRIBE);
                AID receiver = DFTool.findFirstAgent(getAgent(), Constants.MATCHMAKER_DF, Constants.MATCHMAKER_DF);
                if (receiver!=null){
                    message.addReceiver(receiver);
                    message.setContent(getLocalName());
                    message.setProtocol(Constants.RANKING_DF);
                    send(message);
                }
            }
        }

        private class RankingBehaviour extends ParallelBehaviour {
            public RankingBehaviour() {
                addSubBehaviour(new RankingPlayerBehaviour());
            }
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

    private class RankingPlayerBehaviour extends CyclicBehaviour {
        @Override
        public void action() {
            //traiter la demande de renvoyer le classement d'un joueur
            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
            ACLMessage message = receive(mt);
            if (message != null) {
                String content = message.getContent();
                PlayerOperation pOperation = Model.deserialize(content, PlayerOperation.class);
                assert pOperation != null;
                if (pOperation.getOperation().equals(Constants.RANKING_PLAYER)){
                    Player p = pOperation.getPlayer();
                    ACLMessage inform = message.createReply();
                    inform.setPerformative(ACLMessage.INFORM);
                    rankingList.addOrUpdatePlayer(p);
                    int levelRanking = rankingList.getPlayerLevelRanking(p.getAgentName());
                    int winrateRanking = rankingList.getPlayerWinrateRanking(p.getAgentName());

                    PlayerRanking rankingMap = new PlayerRanking();
                    rankingMap.setLevelR(levelRanking);
                    rankingMap.setWinrateR(winrateRanking);
                    inform.setContent(rankingMap.serialize());
                    send(inform);
                }
            } else {
                block();
            }
        }
    }

}
