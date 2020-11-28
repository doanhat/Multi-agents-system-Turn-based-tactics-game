import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.WakerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import java.util.Random;

public class PlayerAgent  extends Agent{
	private static MessageTemplate reqtemplate = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
	private static MessageTemplate infotemplate = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
	private static MessageTemplate restemplate = MessageTemplate.MatchPerformative(ACLMessage.CONFIRM);
	
	public boolean  bataille = false;
	public boolean repos = true;
	public boolean attente = false;
	public long TimeforBattle = 60000;
	public serialisation_des_statistiques_joueur myCaracteristiques;
	public String Actions[] = {"attaquer", "esquiver", "defendre", "lancer_un_sort", "utiliser_un_objet"};
	
	protected void setup() {
		Random rnd = new Random();
		Caracteristiques caracteristiques = new Caracteristiques((int)(rnd.nextDouble() * 10+1),(int)(rnd.nextDouble() * 10+1),20,(int)(rnd.nextDouble() * 3+1),(int)(rnd.nextDouble() * 3+1),0,0);
		myCaracteristiques = new serialisation_des_statistiques_joueur(caracteristiques);

		
		addBehaviour(new WaitforBattle(this, TimeforBattle));
		addBehaviour(new WaitforArene());
		addBehaviour(new Waitfortour());
		addBehaviour(new WaitforResult());
	}
	
	
	private class WaitforBattle extends WakerBehaviour {
		public WaitforBattle(Agent a, long period) {
			super(a,period);
		}
		protected void onWake() {
	        if (repos = true) {
	        	send(Messages.Subscribe(ACLMessage.SUBSCRIBE,"MatchmakerAgent",getLocalName(),AID.ISLOCALNAME));
	        	addBehaviour(new WaitforArene());
	        	repos = false;
	        	attente = true;
	        }
		}
	}
	

	private class WaitforArene extends CyclicBehaviour {
		public void action() {
			ACLMessage message = receive(reqtemplate);
			
			if (message != null) {
				attente = false;
				bataille = true;
				
				ACLMessage reply = message.createReply();
				reply.setPerformative(ACLMessage.INFORM);
				reply.setContent(myCaracteristiques.toJSON());
				addBehaviour(new AnswerforArene(myAgent, reply));
			} else
				block();
		}
	}
	
	
	private class Waitfortour extends CyclicBehaviour {
		
		public void action() {
			Random rnd = new Random();
			ACLMessage message = receive(infotemplate);
			if (message != null) {
				ACLMessage reply = message.createReply();
				reply.setPerformative(ACLMessage.INFORM);
				reply.setContent(Actions[(int)(rnd.nextDouble() * 5)]);
				addBehaviour(new AnswerforArene(myAgent, reply));
			} else
				block();
		}
	}

	private class AnswerforArene extends OneShotBehaviour{
		ACLMessage message;
		
		public AnswerforArene(Agent a, ACLMessage msg) {
			super(a);
			message = msg;
		}				
		public void action() {	
			send(message);
		}
		
	}
	
	private class WaitforResult extends CyclicBehaviour{
		public void action() {
			Random rnd = new Random();
			ACLMessage message = receive(restemplate);
			if (message != null) {
				
				serialisation_des_statistiques_joueur car = serialisation_des_statistiques_joueur.read(message.getContent());
				myCaracteristiques = car;

				repos = true;
				addBehaviour(new WaitforBattle(myAgent, 60000));
			}
		}
		
	}
	
}
