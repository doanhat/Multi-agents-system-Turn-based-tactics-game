package agents;

import Messages.*;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.WakerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import tools.*;

import java.util.Random;
import java.util.UUID;

public class PlayerAgent  extends Agent{
private static MessageTemplate reqtemplate = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
	private static MessageTemplate infotemplate = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
	private static MessageTemplate restemplate = MessageTemplate.MatchPerformative(ACLMessage.CONFIRM);
	
	public long TimeforBattle = 60000;
	public String Actions[] = {"attaquer", "esquiver", "defendre", "lancer_un_sort", "utiliser_un_objet"};
	public boolean bataille = false;
	public Player player;
	Random rnd = new Random();

	@Override
	protected void setup() {
		Characteristics characteristics = new Characteristics((int)(rnd.nextDouble() * 10+1),(int)(rnd.nextDouble() * 10+1),20,(int)(rnd.nextDouble() * 3+1),(int)(rnd.nextDouble() * 3+1),0,0);
		player = new Player(this.getLocalName(),0,0,characteristics);

		//Enregistrement via le DF
		DFTool.registerAgent(this, Constants.PLAYER_DF,getLocalName());

		addBehaviour(new WaitForBattle(this, TimeforBattle));
	}
	
	
	private class WaitForBattle extends WakerBehaviour {
		public WaitForBattle(Agent a, long period) {
			super(a,period);
		}
		@Override
		protected void onWake() {
	        if (!bataille) {
				ACLMessage message = new ACLMessage(ACLMessage.SUBSCRIBE);
				message.addReceiver(DFTool.findFirstAgent(getAgent(), Constants.MATCHMAKER_DF, Constants.MATCHMAKER_DF));
				message.setContent(player.serialize());
				message.setProtocol(Constants.PLAYER_DF);
				getAgent().send(message);
	        	//send(Messages.Subscribe(ACLMessage.SUBSCRIBE,"MatchmakerAgent",getLocalName(),AID.ISLOCALNAME));
	        	addBehaviour(new WaitForArene());
	        }
		}
	}
	

	private class WaitForArene extends Behaviour {
		public void action() {
			ACLMessage message = receive(reqtemplate);		
			if (message != null) {		
				serialisation_des_statistiques_joueur car = serialisation_des_statistiques_joueur.read(message.getContent());
				car.caract(player.getCharacteristics());
				ACLMessage reply = message.createReply();
				reply.setPerformative(ACLMessage.INFORM);
				reply.setContent(car.toJSON());
				send(reply);
				addBehaviour(new WaitFortour());
				bataille = true;
			} else
				block();
		}
		
		public boolean done() {
			return bataille;
		}
		
	    @Override
		public int onEnd() {
	        return super.onEnd();
	    }
	}
	
	
	private class WaitFortour extends Behaviour {
		
		public void action() {
			ACLMessage message = receive(infotemplate);
			ACLMessage message_fin = receive(restemplate);
			if (message != null) {
				ACLMessage reply = message.createReply();
				reply.setPerformative(ACLMessage.INFORM);
				reply.setContent(Actions[(int)(rnd.nextDouble() * 5)]);
				send(reply);
			}
			else if (message_fin != null) {
				serialisation_des_statistiques_joueur car = serialisation_des_statistiques_joueur.read(message_fin.getContent());
				player.setCharacteristics(car.car);
				addBehaviour(new WaitForBattle(myAgent, 60000));
				bataille = false;
			}
			
			else
				block();
		}
		
		public boolean done() {
			return !bataille;
		}
		
	    @Override
		public int onEnd() {
	        return super.onEnd();
	    }	
	}	
}
