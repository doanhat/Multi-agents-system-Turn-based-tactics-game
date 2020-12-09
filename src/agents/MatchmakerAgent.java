package agents;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import agents.ArenaAgent;
import agents.PlayerAgent;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.ParallelBehaviour;
import jade.core.behaviours.SequentialBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.AchieveREInitiator;
import tools.Model;
import tools.Player;
import tools.PlayerWaiting;
import tools.RegisterModel;
import tools.Characteristics;
import tools.Constants;
import tools.DFTool;

public class MatchmakerAgent extends Agent{
	private static final long serialVersionUID = 1L;
	private Map<Integer, RegisterModel> Pagents;
	private Map<Integer, RegisterModel> Aagents;
	private ArrayList<PlayerWaiting> playerQueueList;
	private ArrayList<AID> playerReadyList;
	private ArrayList<AID> arenaList;
    private ObjectMapper objectMapper;
	
	protected void setup() {
		System.out.println(getLocalName()+ "--> Installed");
        this.playerQueueList = new ArrayList<PlayerWaiting>();
        this.playerReadyList = new ArrayList<AID>();
        this.arenaList = new ArrayList<AID>();
        //Enregistrement via le DF
        DFTool.registerAgent(this, Constants.MATCHMAKER_DF,Constants.MATCHMAKER_DF);

        //recherche des arènes et des joueurs
        addBehaviour(new WaitforSubscriptionBehaviour());
        
        //attribution des joueurs aux arènes
        addBehaviour(new MatchmakingBehaviour(this, 5000));
	}

		public class WaitforSubscriptionBehaviour extends ParallelBehaviour {
			private static final long serialVersionUID = 1L;
			
			public WaitforSubscriptionBehaviour() {
				super();
				addSubBehaviour(new WaitforPlayersBehaviour(myAgent));
				addSubBehaviour(new WaitforArenasBehaviour(myAgent));
			}
		}
		
		
		public class WaitforPlayersBehaviour extends CyclicBehaviour {
			Player p = null;
			
			public WaitforPlayersBehaviour(Agent agent) {
				super(agent);
			}
			public void action() {
				MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.SUBSCRIBE); 
				ACLMessage message = myAgent.receive(mt);
				if(message != null) {
	                Player p = Model.deserialize(message.getContent(), Player.class);
	                ACLMessage request = new ACLMessage(ACLMessage.REQUEST);
	                request.addReceiver(DFTool.findFirstAgent(getAgent(), Constants.RANKING_DF, Constants.RANKING_DF));
	                request.setContent(p.serialize());
					addBehaviour(new WaitforRanking(myAgent,request,p, message.getSender()));
				}
			}

		}
		
		public class WaitforRanking extends AchieveREInitiator{
			Player player;
			AID aid;
			Integer Lrank = 0;
			Integer WRrank = 0;
			
			public WaitforRanking(Agent a, ACLMessage msg, Player p, AID aid) {
				super(a, msg);
				this.player = p;
				this.aid = aid;
			}
			
			public void handleInform() {
                HashMap<String,Integer> answer = new HashMap<>();
                ACLMessage message = receive();
                try {
	                answer = objectMapper.readValue(message.getContent(),HashMap.class);
					Lrank = answer.get("Lrank");
					WRrank = answer.get("WRrank");
				} catch (IOException e) {
                    e.printStackTrace();
                }
                PlayerWaiting pw = new PlayerWaiting(player, aid);
                pw.setLrank(Lrank);
                pw.setWRrank(WRrank);
                playerQueueList.add(pw);
			}
			
		}
		
		public class WaitforArenasBehaviour extends CyclicBehaviour {
			public WaitforArenasBehaviour(Agent agent) {
				super(agent);
			}
			
			public void action() {
				MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.REQUEST); 
				ACLMessage answer = receive(mt);
				if(answer != null) {
					arenaList.add(answer.getSender());
				}
			}
		}
		
	    private void byRatio() {

	    }

	    private void byRanking() {
	    	
	    }
	    
	    private void byLevel() {
	    	int compteur=0;
	    	ArrayList<PlayerWaiting> match = new ArrayList<PlayerWaiting>();
	    	
	    	Iterator<PlayerWaiting> joueurActuel = playerQueueList.iterator(); 
	    	while (joueurActuel.hasNext() && compteur<10) {
	    		compteur=0;
	    		match.clear();
	    		Player jA = joueurActuel.next().getPlayer();
	        	Iterator<PlayerWaiting> joueur = playerQueueList.iterator(); 
	        	while (joueur.hasNext() && compteur<10) {
	        		PlayerWaiting pw = joueur.next();
	        		Player j = pw.getPlayer();
	        		if(j.getCharacteristics().getNiveau() >= jA.getCharacteristics().getNiveau()-1 && j.getCharacteristics().getNiveau() <= jA.getCharacteristics().getNiveau()+1) {
	        			compteur++;
	        			match.add(pw);
	        		}
	    		}
	            System.out.println("Joueur " + jA.getAgentName().toString() + " est niveau " + jA.getCharacteristics().getNiveau() + ".");
	            System.out.println("Nombre de joueurs matchable : "+ Integer.toString(compteur));
	    	}
	    	if(compteur>=10) {
	    		Iterator<PlayerWaiting> ite = match.iterator();
	    		for(int i = 0; i<10; i++) {
	    			PlayerWaiting j = ite.next();
	    			playerReadyList.add(j.getAID());
	    			playerQueueList.remove(j);
	    		}
				System.out.println("De nouveaux joueurs sont prêts.");
	    	}
	    	else {
	    		System.out.println("Pas assez de joueurs similaires");
	    	}
	    }
	    
	    
	    
	    private class MatchmakingBehaviour extends TickerBehaviour {

	        public MatchmakingBehaviour(Agent a, long period) {
				super(a, period);
				// TODO Auto-generated constructor stub
				
				//Pour tester les fonctions de matchmaking
				Player p = new Player("p1",1,0,new Characteristics());
				p.getCharacteristics().setNiveau(0);
				playerQueueList.add(new PlayerWaiting(p,new AID("t", true)));
				p = new Player("p2",1,0,new Characteristics());
				p.getCharacteristics().setNiveau(1);
				playerQueueList.add(new PlayerWaiting(p,new AID("t", true)));
				p = new Player("p3",1,0,new Characteristics());
				p.getCharacteristics().setNiveau(2);
				playerQueueList.add(new PlayerWaiting(p,new AID("t", true)));
				p = new Player("p4",1,0,new Characteristics());
				p.getCharacteristics().setNiveau(2);
				playerQueueList.add(new PlayerWaiting(p,new AID("t", true)));
				p = new Player("p5",1,0,new Characteristics());
				p.getCharacteristics().setNiveau(2);
				playerQueueList.add(new PlayerWaiting(p,new AID("t", true)));
				p = new Player("p6",1,0,new Characteristics());
				p.getCharacteristics().setNiveau(2);
				playerQueueList.add(new PlayerWaiting(p,new AID("t", true)));
				p = new Player("p7",1,0,new Characteristics());
				p.getCharacteristics().setNiveau(1);
				playerQueueList.add(new PlayerWaiting(p,new AID("t", true)));
				p = new Player("p8",1,0,new Characteristics());
				p.getCharacteristics().setNiveau(0);
				playerQueueList.add(new PlayerWaiting(p,new AID("t", true)));
				p = new Player("p9",1,0,new Characteristics());
				p.getCharacteristics().setNiveau(3);
				playerQueueList.add(new PlayerWaiting(p,new AID("t", true)));
				p = new Player("p10",1,0,new Characteristics());
				p.getCharacteristics().setNiveau(2);
				playerQueueList.add(new PlayerWaiting(p,new AID("t", true)));
				p = new Player("p11",1,0,new Characteristics());
				p.getCharacteristics().setNiveau(3);
				playerQueueList.add(new PlayerWaiting(p,new AID("t", true)));
				p = new Player("p12",1,0,new Characteristics());
				p.getCharacteristics().setNiveau(2);
				playerQueueList.add(new PlayerWaiting(p,new AID("t", true)));
			}

			public void onTick() {
				if(playerQueueList.size() >= /*Constants.NBR_PLAYER_PER_TEAM*/5*2) {
					byLevel();
				}
				if(!playerReadyList.isEmpty() && !arenaList.isEmpty()) {
					//envoyer AID des agents joueur à l'arène avec un OneShotBehaviour
					arenaList.remove(arenaList.get(0));
				}
	        }
	    }
		
		public class WaitForEndBehaviour extends TickerBehaviour {
			private static final long serialVersionUID = 1L;
			private boolean gameEnded;
			
			public WaitForEndBehaviour(Agent a, long period) {
				super(a, period);
				gameEnded = false;
			}
			@Override
			public void onTick() {
				MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
				ACLMessage notif = getAgent().receive(mt);
				if(notif != null){
					gameEnded = true;
					block();
					
				}
			}
			
		}
	}
