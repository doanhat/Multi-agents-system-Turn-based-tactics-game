import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;

public class ArenaAgent extends Agent {
    
    MessageTemplate subscribe_template = MessageTemplate.MatchPerformative(ACLMessage.SUBSCRIBE);

	public void Setup() {
		System.out.println(getLocalName() + "--> Installed");
		addBehaviour(new Subscribe_Agent_Matchmaking());
		addBehaviour(new Subscribe_Agent_Classement());
		addBehaviour(new Attribution_de_Joueurs());
	}

	public class Subscribe_Agent_Matchmaking extends OneShotBehaviour {
		public void action() {
			send(Messages.Subscribe(ACLMessage.INFORM,"MatchmakerAgent",getLocalName(),AID.ISLOCALNAME));
		}
	}
	
	public class Subscribe_Agent_Classement extends OneShotBehaviour {
		public void action() {
			send(Messages.Subscribe(ACLMessage.INFORM,"RankingAgent",getLocalName(),AID.ISLOCALNAME));
		}
	}
	
	public class Attribution_de_Joueurs extends CyclicBehaviour {
		public void action() {
			ACLMessage message = receive(subscribe_template);
			if (message != null) {
				/*commencer la bataille avec les agents*/
			} else
				block();
		}
	}

}