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
import jade.proto.AchieveREInitiator;
import tools.*;

import java.util.*;
import java.util.stream.Collectors;

import static java.lang.System.out;
import static java.lang.System.setOut;

public class ArenaAgent extends Agent {
    private final int waitingInterval = 5000;
    private final Random random = new Random();
    MessageTemplate fxRequestTemplate = MessageTemplate.MatchPerformative(ACLMessage.REQUEST_WHEN);
    private ArrayList<Player> initialArenaPlayers;
    private ArrayList<Player> finalArenaPlayers;

    private HashMap<String, ArrayList<Player>> teams;
    private HashMap<String, String> actionPlayers;
    private HashMap<String, Integer> initialHealthPlayer;
    private ArrayList<Player> firstTeam;
    private ArrayList<Player> secondTeam;
    private boolean inBattle;
    private String winnerTeam;
    private List<Player> priorities;
    private String team_battle_1st;
    private String team_battle_2nd;
    private int counterPlayers = Constants.NBR_PLAYER_PER_TEAM*2;
    @Override
    public void setup() {
        out.println(getLocalName() + "--> Installed");
        initialArenaPlayers = new ArrayList<>();
        finalArenaPlayers = new ArrayList<>();

        firstTeam = new ArrayList<>();
        secondTeam = new ArrayList<>();
        actionPlayers = new HashMap<>();
        initialHealthPlayer = new HashMap<>();
        inBattle = false;
        winnerTeam = null;
        team_battle_1st = null;
        team_battle_2nd = null;
        teams = new HashMap<>();
        priorities = new ArrayList<>();
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


    private ArrayList<Player> getPlayerTeam(String playerName) {
        for (Player p : firstTeam) {
            if (p.getAgentName().equals(playerName)) {
                return firstTeam;
            }
        }
        for (Player p : secondTeam) {
            if (p.getAgentName().equals(playerName)) {
                return secondTeam;
            }
        }
        return null;
    }

    private ArrayList<Player> getPlayerOpponentTeam(String playerName) {
        for (Player p : firstTeam) {
            if (p.getAgentName().equals(playerName)) {
                return secondTeam;
            }
        }
        for (Player p : secondTeam) {
            if (p.getAgentName().equals(playerName)) {
                return firstTeam;
            }
        }
        return null;
    }

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

        private class ArenaBehaviour extends OneShotBehaviour {

            @Override
            public void action() {
                addBehaviour(new MatchMakerRequestAddPlayerBehaviour());
                addBehaviour(new BeginBattleBehaviour(myAgent, waitingInterval));

            }

            private class MatchMakerRequestAddPlayerBehaviour extends CyclicBehaviour {

                @Override
                public void action() {
                    MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
                    ACLMessage message = receive(mt);
                    if (message != null) {
                        if (initialArenaPlayers.size() < Constants.NBR_PLAYER_PER_TEAM * 2) {
                            Player player = Model.deserialize(message.getContent(), Player.class);
                            assert player != null;
                            initialArenaPlayers.add(player);
                            initialHealthPlayer.put(player.getAgentName(), player.getCharacteristics().getHealth());
                            counterPlayers--;
                            if (counterPlayers == 0) {
                                try {
                                    priorities = setPriorities();
                                } catch (JsonProcessingException e) {
                                    e.printStackTrace();
                                }
                            }
                            ACLMessage reply = message.createReply();
                            reply.setPerformative(ACLMessage.AGREE);
                            reply.setContent(getLocalName());
                            send(reply);
                        } else {
                            ACLMessage reply = message.createReply();
                            reply.setPerformative(ACLMessage.REFUSE);
                            reply.setContent(getLocalName());
                            send(reply);
                        }
                    }
                }

                private List<Player> setPriorities() throws JsonProcessingException {
                    List<Player> prio = Model.deserializeToList(new ObjectMapper().writeValueAsString(initialArenaPlayers), Player.class);
                    assert prio != null;
                    prio = prio.stream()
                            .sorted(Comparator.comparingInt(Player::obtainInitiative).reversed())
                            .collect(Collectors.toList());
                    return prio;
                }
            }

            private class BeginBattleBehaviour extends TickerBehaviour {
                public BeginBattleBehaviour(Agent a, long period) {
                    super(a, period);
                }

                @Override
                protected void onTick() {
                    if (initialArenaPlayers.size() == Constants.NBR_PLAYER_PER_TEAM * 2 && !inBattle) {
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
                    out.println(getLocalName()+" : "+getLocalName() + " joueurs : " + getPlayerNames(initialArenaPlayers));
                    Collections.shuffle(initialArenaPlayers);
                    teams.put("A", new ArrayList(initialArenaPlayers.subList(0, Constants.NBR_PLAYER_PER_TEAM)));
                    teams.put("B", new ArrayList(initialArenaPlayers.subList(Constants.NBR_PLAYER_PER_TEAM, initialArenaPlayers.size())));
                    teams.put("A_battle", new ArrayList(initialArenaPlayers.subList(0, Constants.NBR_PLAYER_PER_TEAM)));
                    teams.put("B_battle", new ArrayList(initialArenaPlayers.subList(Constants.NBR_PLAYER_PER_TEAM, initialArenaPlayers.size())));


                }

                private void beginBattle() {
                    if (random.nextBoolean()) {
                        out.println(getLocalName()+" : "+"Equipe A commence");
                        //fight("A",action);
                        team_battle_1st = "A_battle";
                        team_battle_2nd = "B_battle";
                        fight(team_battle_1st, team_battle_2nd);
                    } else {
                        out.println(getLocalName()+" : "+"Equipe B commence");
                        //fight("B",action);
                        team_battle_1st = "B_battle";
                        team_battle_2nd = "A_battle";
                        fight(team_battle_1st, team_battle_2nd);
                    }
                }

                private void fight(String team1st, String team2nd) {
                    firstTeam = teams.get(team1st);
                    secondTeam = teams.get(team2nd);
                    out.println(getLocalName()+" : "+"Le bataille commence ");
                    addBehaviour(new BattleExecutionBehaviour());
                }


                public String getPlayerNames(ArrayList<Player> players) {
                    String names = "";
                    for (Player p : players) {
                        names += " - " + p.getAgentName();
                    }
                    names += " ";
                    return names;
                }

                private String getOpposite(String teamOrder) {
                    if (teamOrder.equals("A")) {
                        return "B";
                    }
                    if (teamOrder.equals("B")) {
                        return "A";
                    }
                    return null;
                }

                private class EndBattleBehaviour extends SequentialBehaviour {
                    public EndBattleBehaviour(Agent a) throws JsonProcessingException {
                        super(a);
                        if (firstTeam.size() == 0 && team_battle_2nd!=null) {
                            winnerTeam = team_battle_2nd.replace("_battle", "");
                        } else if (secondTeam.size() == 0 && team_battle_1st!=null) {
                            winnerTeam = team_battle_1st.replace("_battle", "");
                        }
                        if (winnerTeam != null) {
                            out.println(getLocalName()+" : "+"L'équipe des joueurs: " + getPlayerNames(teams.get(winnerTeam)) + " a gagné");

                            updatePlayerCharacteristics();
                            finalArenaPlayers = (ArrayList<Player>) Model.deserializeToList(new ObjectMapper().writeValueAsString(initialArenaPlayers),Player.class);
                            addSubBehaviour(new FreePlaceBehaviour(myAgent));
                        } else {
                            addSubBehaviour(new BattleExecutionBehaviour());
                        }
                    }

                    private void updatePlayerCharacteristics() {
                        for (Player p : teams.get(winnerTeam)) {
                            p.getCharacteristics().setHealth(initialHealthPlayer.get(p.getAgentName()));
                            p.levelUp();
                            p.setNbrVictory(p.getNbrVictory() + 1);
                        }
                        for (Player p : teams.get(getOpposite(winnerTeam))) {
                            p.getCharacteristics().setHealth(initialHealthPlayer.get(p.getAgentName()));
                            p.setNbrDefeat(p.getNbrDefeat() + 1);
                        }
                    }

                    private void freeArenaPlace() {
                        initialArenaPlayers.clear();
                        teams.clear();
                        actionPlayers.clear();
                        initialHealthPlayer.clear();
                        firstTeam.clear();
                        secondTeam.clear();
                        priorities.clear();
                        team_battle_1st = null;
                        team_battle_2nd = null;
                        inBattle = false;
                        winnerTeam = null;
                        counterPlayers = Constants.NBR_PLAYER_PER_TEAM*2;
                    }

                    private class FreePlaceBehaviour extends SequentialBehaviour {
                        public FreePlaceBehaviour(Agent a) {
                            super(a);
                            out.println("Clean arena .....");
                            freeArenaPlace();
                            ACLMessage request1 = new ACLMessage(ACLMessage.REQUEST);
                            AID receiver1 = DFTool.findFirstAgent(getAgent(), Constants.MATCHMAKER_DF, Constants.MATCHMAKER_DF);
                            request1.addReceiver(receiver1);
                            AgentRequest freeArenaRequest = new AgentRequest(getLocalName(),Constants.FREE_ARENA);
                            request1.setContent(freeArenaRequest.serialize());
                            addSubBehaviour(new FreePlaceInitiator(myAgent,request1));
                            addSubBehaviour(new UpdateCharacteristicsBehaviour(myAgent));
                        }

                        private class FreePlaceInitiator extends AchieveREInitiator {
                            public FreePlaceInitiator(Agent myAgent, ACLMessage inform) {
                                super(myAgent,inform);
                            }

                            @Override
                            protected void handleInform(ACLMessage inform) {
                                out.println(inform.getContent());
                            }
                        }
                    }

                    private class UpdateCharacteristicsBehaviour extends SequentialBehaviour {
                        public UpdateCharacteristicsBehaviour(Agent a) {
                            super(a);
                            out.println(getLocalName()+" : "+" Le classement des joueurs:");
                            for (Player p : finalArenaPlayers){
                                ACLMessage request2 = new ACLMessage(ACLMessage.REQUEST);
                                //AID receiver2 = DFTool.findFirstAgent(getAgent(), Constants.PLAYER_DF, p.getAgentName());
                                AID receiver2 = DFTool.findFirstAgent(getAgent(), Constants.RANKING_DF, Constants.RANKING_DF);

                                request2.addReceiver(receiver2);
                                PlayerOperation uOperation = new PlayerOperation(Constants.UPDATE_PLAYER, p);
                                request2.setContent(uOperation.serialize());
                                addSubBehaviour(new UpdatePlayerInitiator(myAgent, request2,p));;
                            }
                        }

                    }

                    private class UpdatePlayerInitiator extends AchieveREInitiator {
                        private Player p;
                        public UpdatePlayerInitiator(Agent myAgent, ACLMessage inform, Player p) {
                            super(myAgent,inform);
                            this.p = p;
                        }

                        @Override
                        protected void handleInform(ACLMessage inform) {
                            PlayerRanking playerRanking;
                            playerRanking = Model.deserialize(inform.getContent(), PlayerRanking.class);
                            assert playerRanking != null;
                            out.println(getLocalName()+" : "+"Classement " + p.getAgentName() + " - rapport victoire/défaite : " + playerRanking.getWinrateR() + " - niveau : " + playerRanking.getLevelR());
                        }
                    }
                }

                private class GetPlayerActionBehaviour extends SequentialBehaviour {
                    public GetPlayerActionBehaviour(Agent a) {
                        super(a);
                        for (Player priority : priorities) {
                            ACLMessage request = new ACLMessage(ACLMessage.REQUEST);
                            AID receiver = DFTool.findFirstAgent(getAgent(), Constants.PLAYER_DF, priority.getAgentName());
                            request.addReceiver(receiver);
                            PlayerOperation aOperation = new PlayerOperation(Constants.ACTION_PLAYER, priority);
                            request.setContent(aOperation.serialize());
                            addSubBehaviour(new ActionPlayerInitiator(myAgent, request));
                        }
                        addSubBehaviour(new ExecuteTurn());

                    }

                    private class ActionPlayerInitiator extends AchieveREInitiator {
                        public ActionPlayerInitiator(Agent a, ACLMessage msg) {
                            super(a, msg);
                        }

                        @Override
                        protected void handleInform(ACLMessage inform) {
                            // pour enregistrer la liste des tours du joueur
                            PlayerAction pActions;
                            pActions = Model.deserialize(inform.getContent(), PlayerAction.class);
                            assert pActions != null;
                            actionPlayers.put(pActions.getPlayerName(), pActions.getAction());
                        }
                    }


                    private class ExecuteTurn extends OneShotBehaviour {
                        @Override
                        public void action() {
                            priorities.removeIf(p -> getPlayerTeam(p.getAgentName())==null);
                            for (Player p : priorities) {
                                ArrayList<Player> team = getPlayerTeam(p.getAgentName());
                                playerMakeAction(p, team);
                            }
                        }

                        private void playerMakeAction(Player p, ArrayList<Player> team) {
                            ArrayList<Player> opponentTeam = getPlayerOpponentTeam(p.getAgentName());
                            String action = actionPlayers.get(p.getAgentName());
                            if (opponentTeam!=null && !opponentTeam.isEmpty()){
                                switch (action) {
                                    case Constants.ATTACK:
                                        Player enemie = opponentTeam.get(0);
                                        String enemieAction = actionPlayers.get(enemie.getAgentName());
                                        out.println(getLocalName()+" : "+p.getAgentName() + " attaque " + enemie.getAgentName() + " !");
                                        switch (enemieAction) {
                                            case Constants.DEFENSE: { //si le joueur attaqué se défend
                                                int damage = enemie.getCharacteristics().getDefense() - p.getCharacteristics().getAttack();
                                                if (damage < 0) damage = 0;
                                                enemie.getCharacteristics().setHealth(enemie.getCharacteristics().getHealth() - damage);
                                                if (enemie.getCharacteristics().getHealth() <= 0) {
                                                    opponentTeam.removeIf(player -> player.getAgentName().equals(enemie.getAgentName()));
                                                }
                                                break;
                                            }
                                            case Constants.DODGE: { //si le joueur attaqué se défend esquive
                                                int damage = p.getCharacteristics().getAttack();
                                                int randomNum = (int) (Math.random() * 100);
                                                if (randomNum % enemie.getCharacteristics().getDodge() == 0)
                                                    enemie.getCharacteristics().setHealth(enemie.getCharacteristics().getHealth() - damage);
                                                if (enemie.getCharacteristics().getHealth() <= 0) {
                                                    opponentTeam.removeIf(player -> player.getAgentName().equals(enemie.getAgentName()));
                                                }
                                                break;
                                            }
                                            case Constants.ATTACK: {
                                                int damage = enemie.getCharacteristics().getAttack();
                                                enemie.getCharacteristics().setHealth(enemie.getCharacteristics().getHealth() - damage);
                                                if (enemie.getCharacteristics().getHealth() <= 0) {
                                                    opponentTeam.removeIf(player -> player.getAgentName().equals(enemie.getAgentName()));
                                                }
                                                break;
                                            }
                                        }
                                        break;
                                    case Constants.DODGE:
                                        out.println(getLocalName()+" : "+p.getAgentName() + " essaye d'esquiver !");
                                        break;
                                    case Constants.DEFENSE:
                                        out.println(getLocalName()+" : "+p.getAgentName() + " se défend !");
                                        break;
                                    case Constants.CAST_SPELL:

                                        int randomNum = (int) (Math.random() * 100);
                                        if (randomNum % 2 == 0) {
                                            assert opponentTeam != null;
                                            enemie = opponentTeam.get(0);
                                            if (enemie.getCharacteristics().getDefense() > 0) {
                                                enemie.getCharacteristics().setDefense(enemie.getCharacteristics().getDefense() - 1);
                                            }
                                            out.println(getLocalName()+" : "+p.getAgentName() + " lance un sort au " + enemie.getAgentName() + " !");
                                        } else {
                                            p.getCharacteristics().setAttack(p.getCharacteristics().getAttack() + 1);
                                        }
                                        break;
                                    case Constants.USE_OBJECT:
                                        int randomNum2 = (int) (Math.random() * 100);
                                        if (randomNum2 % 3 == 0) {
                                            p.getCharacteristics().setHealth(p.getCharacteristics().getHealth() + 1);
                                        }
                                        if (randomNum2 % 3 == 1) {
                                            p.getCharacteristics().setAttack(p.getCharacteristics().getAttack() + 1);
                                        } else {
                                            p.getCharacteristics().setDefense(p.getCharacteristics().getDefense() + 1);
                                        }
                                        out.println(getLocalName()+" : "+p.getAgentName() + " utilise un objet !");
                                        break;
                                }
                            }
                        }
                    }

                }

                private class BattleBehaviour extends SequentialBehaviour {
                    public BattleBehaviour(Agent a) throws JsonProcessingException {
                        super(a);
                        addSubBehaviour(new GetPlayerActionBehaviour(myAgent));
                        addSubBehaviour(new EndBattleBehaviour(myAgent));
                    }
                }

                private class BattleExecutionBehaviour extends OneShotBehaviour {
                    @Override
                    public void action() {
                        try {
                            addBehaviour(new BattleBehaviour(myAgent));
                        } catch (JsonProcessingException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }
}