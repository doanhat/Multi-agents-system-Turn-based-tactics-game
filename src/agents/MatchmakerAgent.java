package agents;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import agents.ArenaAgent;
import agents.PlayerAgent;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.ParallelBehaviour;
import jade.core.behaviours.SequentialBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import tools.Model;
import tools.RegisterModel;
import tools.Constants;
import tools.DFTool;

public class MatchmakerAgent extends Agent{
	private static final long serialVersionUID = 1L;
	private Map<Integer, RegisterModel> Pagents;
	private Map<Integer, RegisterModel> Aagents;
	protected void setup() {
		System.out.println(getLocalName()+ "--> Installed");
		DFTool.registerAgent(this, Constants.MATCHMAKER_DF, Constants.MATCHMAKER_DF);
		addBehaviour(new WaitforSubscriptionBehaviour());
	}
	/*public class MatchmakerBehaviour extends CyclicBehaviour{
		private static final long serialVersionUID = 1L;
		public void action(){
			WaitforSubscriptionBehaviour a=new WaitforSubscriptionBehaviour();
			Behaviour a =new ???(WaitforSubscriptionBehaviour.WHEN_ALL));
		} 
		public void done(
		
		)
		}
		*/
		
		
		
		public class WaitforSubscriptionBehaviour extends ParallelBehaviour {
			private static final long serialVersionUID = 1L;
			
			public WaitforSubscriptionBehaviour() {
				super();
				Pagents = new HashMap<>();
				Aagents = new HashMap<>();
				addSubBehaviour(new WaitforPlayersBehaviour(myAgent));
				addSubBehaviour(new WaitforArenasBehaviour(myAgent));
			}
		}
		
		
		public class WaitforPlayersBehaviour extends Behaviour {
			private static final long serialVersionUID = 1L;
			private int counter = Constants.NBR_PLAYER;
			public WaitforPlayersBehaviour(Agent agent) {
				super(agent);
			}
			@Override
			
			public void action() {
				MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.SUBSCRIBE); 
				ACLMessage answer = myAgent.receive(mt);
				if(answer == null) block();
				else {
					RegisterModel model = Model.deserialize(answer.getContent(), RegisterModel.class);
					Pagents.put(--counter, model);
				}
			}

			@Override
			public boolean done() {
				return counter == 0;
			}
		}
		public class WaitforArenasBehaviour extends Behaviour {
			private static final long serialVersionUID = 1L;
			private int counter = Constants.NBR_ARENA;
			public WaitforArenasBehaviour(Agent agent) {
				super(agent);
			}
			@Override
			public void action() {
				MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.SUBSCRIBE); 
				ACLMessage answer = myAgent.receive(mt);
				if(answer == null) block();
				else {
					RegisterModel model = Model.deserialize(answer.getContent(), RegisterModel.class);
					Aagents.put(--counter, model);
				}
			}

			@Override
			public boolean done() {
				return counter == 0;
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

