package gui;

import static java.lang.System.out;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.ParallelBehaviour;
import jade.core.behaviours.SequentialBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.AchieveREInitiator;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import tools.Constants;
import tools.DFTool;
import tools.Model;
import tools.Player;
import tools.PlayerRanking;
import tools.RankingList;

public class ConnectionAgent extends Agent{
	
	public int rankingAgent;
	public List<Player> PlayerList;
	public ConnectionController controller;
	public StringProperty messageAnswer;
	public String TextofAction;
	public int n;
	public int m;
	public int z;
	public int i = 1;
	public HashMap<String,ArrayList<String>> oldarenaAgentMap;
	public HashMap<String,ArrayList<String>> newarenaAgentMap;
	public HashMap<String,String> arenaActionMap;
	public List<String> arena = new ArrayList();
	public ConnectionAgent() {
		super();
		messageAnswer = new SimpleStringProperty();
	}
	protected void setup() {
		super.setup();
		controller = (ConnectionController) getArguments()[0];
		// L'agent s'enregistre dans le contrôleur de l'application
		controller.setAgent(this);
	    DFTool.registerAgent(this, Constants.CONNECTION_DF, Constants.CONNECTION_DF);
		System.out.println(getLocalName() + " agent ---> installed");
		this.oldarenaAgentMap = new HashMap<>();
		this.newarenaAgentMap = new HashMap<>();
		this.arenaActionMap = new HashMap<>();
		this.arena = new ArrayList();
		this.TextofAction = "";
		addBehaviour(new ConnectionBehaviour());
		
	}
	public StringProperty messageAnswerProperty() {
		return messageAnswer;
	}
	 private class ConnectionBehaviour extends SequentialBehaviour {
	        public ConnectionBehaviour() {
	        	addBehaviour(new WaitForArenasBehaviour());
	    		addBehaviour(new GetinfoBehaviour());
	        }
	 }
	 private class GetinfoBehaviour extends ParallelBehaviour {
	        public GetinfoBehaviour () {
	        	addBehaviour(new WaitForRankingBehaviour());
	            addBehaviour(new WaitForArenasMessage());
	            addBehaviour(new WaitForArenasAction());
	            
	        }
	 }
	 private class WaitForRankingBehaviour extends Behaviour {
         @Override
         public void action() {
        	 //System.out.println("wait ranking info");
             MessageTemplate mt = MessageTemplate.MatchProtocol(Constants.RANKING_DF);
             ACLMessage answer = getAgent().receive(mt);
             if (answer == null) block();
             else {
            	 //System.out.println("get info");
            	 //PlayerRanking playerRanking;
            	 RankingList rl;
                 //playerRanking = Model.deserialize(answer.getContent(), PlayerRanking.class);
            	 rl = Model.deserialize(answer.getContent(), RankingList.class);
                 assert rl != null;
                 if(i==10) {
                
                	 PlayerList =  new ArrayList<Player>();
                     PlayerList.addAll(rl.getPlayerHashMap().values());
                 	 i = 1;
                 }
                 else
                	 {i++;}
                 n++;
                 }

         }

         @Override
         public boolean done() {
             return n == 10000;
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
                 //System.out.println(content);
                 oldarenaAgentMap.put(content,new ArrayList<>());
                 newarenaAgentMap.put(content,new ArrayList<>());
                 arena.add(content);
                 counter--;
             }

         }

         @Override
         public boolean done() {
             return counter == 0;
         }
     }
	 private class WaitForArenasMessage  extends Behaviour {

         @Override
         public void action() {
        	//System.out.println(arena.size());
             MessageTemplate mt = MessageTemplate.and(
                     MessageTemplate.MatchPerformative(ACLMessage.INFORM),
                     MessageTemplate.MatchProtocol(Constants.ARENA_DF));
             ACLMessage answer = getAgent().receive(mt);
             if (answer == null) block();
             else {
            	 String sender;
                 String content = answer.getContent();
                 if  (content != null) {
                 List<String> items1= Stream.of(content.split(","))
                	     .map(String::trim)
                	     .collect(Collectors.toList());
                 if(items1.get(0).contains(arena.get(0))) {
                	 sender = arena.get(0);
                	 items1.set(0, items1.get(0).replace( arena.get(0), ""));
                	 
                 }
                 else {
                	 sender = arena.get(1);
                	 items1.set(0, items1.get(0).replace( arena.get(1), ""));
                	 
                 }
                 items1.set(0, items1.get(0).replace( "[", ""));
                 items1.set(9, items1.get(9).replace( "]", ""));
                 //System.out.println(items1.get(0));
                 //System.out.println(items1.get(9));
                /* System.out.println(sender);
                 for(String s : items1) {
                     System.out.println(s);
                 }*/
                 if (oldarenaAgentMap.containsKey(sender)){
                 oldarenaAgentMap.replace(sender, newarenaAgentMap.get(sender));}
                 else
                 {
                	 oldarenaAgentMap.put(sender, newarenaAgentMap.get(sender));
                 }
                 
                 newarenaAgentMap.put(sender, (ArrayList<String>) items1);
                 
                 m++; 
             }
             }

         }

         @Override
         public boolean done() {
             return m == 10000;
         }
     }
	 private class WaitForArenasAction  extends Behaviour {

         @Override
         public void action() {
             MessageTemplate mz = MessageTemplate.and(
                     MessageTemplate.MatchPerformative(ACLMessage.PROPOSE),
                     MessageTemplate.MatchProtocol(Constants.ARENA_DF));
             ACLMessage answer = getAgent().receive(mz);
             if (answer == null) block();
             else {
            	 String sender2;
            	 List<String> items2= Stream.of(answer.getContent().split("\n"))
                	     .map(String::trim)
                	     .collect(Collectors.toList());
                 if(items2.get(0).contains(arena.get(0))) {
                	 sender2 = arena.get(0);
                	 
                 }
                 else {
                	 sender2 = arena.get(1);
                	 
                 }
                 TextofAction = answer.getContent().replace(sender2+"\n", "");
                 arenaActionMap.put(sender2,  TextofAction);
                 
                 //System.out.println(items1.get(9));
                 
                 
                 z++; 
             }

         }
         

         @Override
         public boolean done() {
             return z == 10000;
         }
     }
	
 }


