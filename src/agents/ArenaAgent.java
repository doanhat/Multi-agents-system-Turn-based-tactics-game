package agents;

import com.fasterxml.jackson.core.JsonProcessingException;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.*;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.AchieveREInitiator;
import tools.*;

import java.util.*;

import static java.lang.System.out;

public class ArenaAgent extends Agent {
    private final int waitingInterval = 5000;
    private ArrayList<Player> arenaPlace;
    private HashMap<String,ArrayList<Player>> teams;
    private final Random random = new Random();
    private boolean inBattle;
    private String winnerTeam;

    @Override
    public void setup() {
        out.println(getLocalName() + "--> Installed");
        arenaPlace = new ArrayList<>();
        inBattle = false;
        winnerTeam = null;
        teams = new HashMap<>();
        // Enregistrement via le DF
        DFTool.registerAgent(this, Constants.ARENA_DF, getLocalName());

        addBehaviour(new ArenaAgentBehaviour(this));
    }

    @Override
    protected void takeDown() {
        out.println("---> " + getLocalName() + " : Good bye");

        try {
            DFService.deregister(this);
        } catch (FIPAException e) {
            e.printStackTrace();
        }
    }

    /*public class connexion_interface_graphique extends CyclicBehaviour{
    	public void action() {
    		ACLMessage message = receive(request_de_Fx_template);
    		if(message!=null) {
    			Arene_Fx info_arene = new Arene_Fx(joueursA,joueursB,playerListInit);
    			//1er 2eme parametre//une liste des joueurs A et B est envoyée en booléen où elle indique s'ils se battent encore ou non
    			//3eme parametre //la liste des joueurs dans l'arène, il suffit de la diviser en deux pour obtenir A et B
    			//System.out.print(info_arene.toJSON());
    			send(Messages.Subscribe(ACLMessage.INFORM_REF, "Nom_agent de connexion", info_arene.toJSON(), AID.ISLOCALNAME)); //changer le nom de l'agent d'interface
    		}
    		else {
    			block();
    		}
    	}
    }*/

    private class ArenaAgentBehaviour extends SequentialBehaviour {
        public ArenaAgentBehaviour(Agent a) {
            super(a);
            addSubBehaviour(new SubscribeMatchMakerBehaviour());
            addSubBehaviour(new ArenaBehaviour());
        }

        private class SubscribeMatchMakerBehaviour extends OneShotBehaviour {
            @Override
            public void action() {
                ACLMessage message = new ACLMessage(ACLMessage.SUBSCRIBE);
                AID receiver = DFTool.findFirstAgent(getAgent(), Constants.MATCHMAKER_DF, Constants.MATCHMAKER_DF);
                if (receiver != null) {
                    message.addReceiver(receiver);
                    message.setContent(getLocalName());
                    message.setProtocol(Constants.ARENA_DF);
                    send(message);
                }
            }
        }

        private class ArenaBehaviour extends ParallelBehaviour {
            public ArenaBehaviour() {
                addSubBehaviour(new MatchMakerRequestAddPlayerBehaviour());
                addSubBehaviour(new BeginBattleBehaviour(myAgent, waitingInterval));
            }

            private class MatchMakerRequestAddPlayerBehaviour extends Behaviour {
                private int counter = Constants.NBR_PLAYER_PER_TEAM*2;
                @Override
                public void action() {
                    MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
                    ACLMessage message = receive(mt);
                    if (message != null){
                        Player player = Model.deserialize(message.getContent(), Player.class);
                        arenaPlace.add(player);
                        counter--;
                        ACLMessage reply = message.createReply();
                        reply.setPerformative(ACLMessage.INFORM);
                        reply.setContent(getLocalName() + " a ajouté le joueur " + player.getAgentName());
                        send(reply);
                    }
                }

                @Override
                public boolean done() {
                    return counter==0;
                }
            }

            private class BeginBattleBehaviour extends TickerBehaviour {
                public BeginBattleBehaviour(Agent a, long period) {
                    super(a, period);
                }

                @Override
                protected void onTick() {
                    if (arenaPlace.size() == Constants.NBR_PLAYER_PER_TEAM * 2 && !inBattle) {
                        inBattle = true;
                        arrangeTeams();
                        try {
                            beginBattle();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }

                private void arrangeTeams() {
                    out.println("Battre les joueurs : " + getPlayerNames(arenaPlace));
                    Collections.shuffle(arenaPlace);
                    //team.add(arenaPlace.subList(0, Constants.NBR_PLAYER_PER_TEAM));
                    //team.add(arenaPlace.subList(Constants.NBR_PLAYER_PER_TEAM,arenaPlace.size()));
                    teams.put("A",new ArrayList(arenaPlace.subList(0, Constants.NBR_PLAYER_PER_TEAM)));
                    teams.put("B",new ArrayList(arenaPlace.subList(Constants.NBR_PLAYER_PER_TEAM,arenaPlace.size())));
                    teams.put("A_battle",new ArrayList(arenaPlace.subList(0, Constants.NBR_PLAYER_PER_TEAM)));
                    teams.put("B_battle",new ArrayList(arenaPlace.subList(Constants.NBR_PLAYER_PER_TEAM,arenaPlace.size())));


                }

                private void beginBattle() throws JsonProcessingException {
                    HashMap<String, Integer> action = new HashMap<>();
                    action.put("attack",0);
                    out.println("Le bataille commence ");
                    if (random.nextBoolean()) {
                        out.println("Equipe 0 commence");
                        //fight("A",action);
                        fight("A_battle","B_battle");
                    } else {
                        out.println("Equipe 1 commence");
                        //fight("B",action);
                        fight("B_battle","A_battle");
                    }
                }

                private void fight(String team_battle_1st, String team_battle_2nd) {
                    ArrayList<Player> firstTeam = teams.get(team_battle_1st);
                    ArrayList<Player> secondTeam = teams.get(team_battle_2nd);
                    HashMap<String, Integer> action = new HashMap<>();
                    action.put("attack",0);
                    while (firstTeam.size()>0 && secondTeam.size()>0){
                        firstTeam.get(0).receiveAttack(action.get("attack"));
                        out.println("Santé de "+firstTeam.get(0).getAgentName()+" est "+firstTeam.get(0).getCharacteristics().getHealth());
                        if (firstTeam.get(0).getCharacteristics().getHealth()<=0){
                            out.println(firstTeam.get(0).getAgentName()+" est mort");
                            firstTeam.remove(0);
                        }
                        if (firstTeam.size()>0){
                            out.println(firstTeam.get(0).getAgentName()+" lance une attaque "+ firstTeam.get(0).getCharacteristics().getAttack());
                            action.put("attack",firstTeam.get(0).getCharacteristics().getAttack());
                        }
                        secondTeam.get(0).receiveAttack(action.get("attack"));
                        out.println("Santé de " +secondTeam.get(0).getAgentName()+" est "+secondTeam.get(0).getCharacteristics().getHealth());
                        if (secondTeam.get(0).getCharacteristics().getHealth()<=0){
                            out.println(secondTeam.get(0).getAgentName()+" est mort");
                            secondTeam.remove(0);
                        }
                        if (secondTeam.size()>0){
                            out.println(secondTeam.get(0).getAgentName()+" lance une attaque "+ secondTeam.get(0).getCharacteristics().getAttack());
                            action.put("attack",secondTeam.get(0).getCharacteristics().getAttack());
                        }
                    }
                    if (firstTeam.size()==0){
                        winnerTeam = team_battle_2nd.replace("_battle","");
                    } else if (secondTeam.size()==0){
                        winnerTeam = team_battle_1st.replace("_battle","");
                    }
                    out.println("L'équipe des joueurs: " +getPlayerNames(teams.get(winnerTeam))+" a gagné");
                    addBehaviour(new EndBattleBehaviour());
                }

                private String getPlayerNames(ArrayList<Player> players) {
                    String names = "";
                    for (Player p : players){
                        names += " - "+p.getAgentName();
                    }
                    names += " ";
                    return names;
                }

                private String getOpposite(String teamOrder) {
                    if (teamOrder.equals("A")){
                        return "B";
                    }
                    if (teamOrder.equals("B")){
                        return "A";
                    }
                    return null;
                }

                private class EndBattleBehaviour extends OneShotBehaviour {
                    @Override
                    public void action() {
                        updatePlayerCharacteristics();

                        out.println(" Le classement des joueurs :");
                        for (Player p : arenaPlace){
                            ACLMessage request = new ACLMessage(ACLMessage.REQUEST);
                            AID receiver = DFTool.findFirstAgent(getAgent(), Constants.RANKING_DF, Constants.RANKING_DF);
                            request.addReceiver(receiver);
                            request.setContent(p.serialize());
                            addBehaviour(new GetRankingInitiator(myAgent,request,p));
                        }
                        freeArenaPlace();
                    }

                    private void updatePlayerCharacteristics() {
                        for (Player p : teams.get(winnerTeam)) {
                            p.getCharacteristics().setHealth(20);
                            p.levelUp();
                            p.setNbrVictory(p.getNbrVictory()+1);
                            ACLMessage request = new ACLMessage(ACLMessage.INFORM);
                            AID receiver = DFTool.findFirstAgent(getAgent(), Constants.PLAYER_DF, p.getAgentName());
                            AID rankingAgent = DFTool.findFirstAgent(getAgent(), Constants.RANKING_DF, Constants.RANKING_DF);
                            request.addReceiver(receiver);
                            request.addReceiver(rankingAgent);
                            request.setContent(p.serialize());
                            send(request);
                        }
                        for (Player p : teams.get(getOpposite(winnerTeam))) {
                            p.getCharacteristics().setHealth(20);
                            p.setNbrDefeat(p.getNbrDefeat()+1);
                            ACLMessage request = new ACLMessage(ACLMessage.INFORM);
                            AID receiver = DFTool.findFirstAgent(getAgent(), Constants.PLAYER_DF, p.getAgentName());
                            AID rankingAgent = DFTool.findFirstAgent(getAgent(), Constants.RANKING_DF, Constants.RANKING_DF);
                            request.addReceiver(receiver);
                            request.addReceiver(rankingAgent);
                            request.setContent(p.serialize());
                            send(request);
                        }
                    }

                    private void freeArenaPlace() {
                        arenaPlace.clear();
                        teams.clear();
                        inBattle = false;
                        winnerTeam = null;
                    }

                    private class GetRankingInitiator extends AchieveREInitiator {
                        private final Player player;

                        public GetRankingInitiator(Agent a, ACLMessage msg, Player p) {
                            super(a, msg);
                            this.player = p;
                        }

                        @Override
                        protected void handleInform(ACLMessage inform) {
                            PlayerRanking playerRanking = new PlayerRanking();
                            playerRanking = Model.deserialize(inform.getContent(), PlayerRanking.class);
                            //out.println(player.serialize());
                            out.println("Classement " + player.getAgentName() + " - rapport victoire/défaite : " + playerRanking.getWinrateR() + " - niveau : " + playerRanking.getLevelR());
                        }
                    }
                }
            }
        }
    }
}