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
                if (pOperation.getOperation().equals(Constants.UPDATE_PLAYER)){
                    Player p = pOperation.getPlayer();
                    //System.out.println(p.getAgentName());
                    ACLMessage reply = message.createReply();
                    reply.setPerformative(ACLMessage.INFORM);
                    rankingList.addOrUpdatePlayer(p);
                    int levelRanking = rankingList.getPlayerLevelRanking(p.getAgentName());
                    int winrateRanking = rankingList.getPlayerWinrateRanking(p.getAgentName());

                    PlayerRanking rankingMap = new PlayerRanking();
                    rankingMap.setLevelR(levelRanking);
                    rankingMap.setWinrateR(winrateRanking);
                    reply.setContent(rankingMap.serialize());
                    send(reply);
                    
                    ACLMessage request2 = new ACLMessage(ACLMessage.REQUEST);
                    AID receiver2 = DFTool.findFirstAgent(getAgent(), Constants.PLAYER_DF, p.getAgentName());
                    request2.addReceiver(receiver2);
                    PlayerOperation uOperation = new PlayerOperation(Constants.UPDATE_PLAYER, p);
                    request2.setContent(uOperation.serialize());
                    addBehaviour(new UpdatePlayerInitiator(myAgent,request2));
                    AID receiver3 = DFTool.findFirstAgent(getAgent(), Constants.CONNECTION_DF, Constants.CONNECTION_DF);
                    ACLMessage inform3 = new ACLMessage(ACLMessage.INFORM);
                    inform3.setProtocol(Constants.RANKING_DF);
                    inform3.setContent(rankingList.serialize());
                    inform3.addReceiver(receiver3);
                    send(inform3);
                }
            } else {
                block();
            }
        }

        private class UpdatePlayerInitiator extends AchieveREInitiator {
            public UpdatePlayerInitiator(Agent a, ACLMessage msg) {
                super(a, msg);
            }

            @Override
            protected void handleInform(ACLMessage inform) {
                super.handleInform(inform);
            }
        }
    }

}
