import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;

public class ArenaAgent extends Agent {

	public void Setup() {
		System.out.println(getLocalName() + "--> Installed");
		addBehaviour(new Subscribe_Agent_Matchmaking());
		addBehaviour(new Subscribe_Agent_Classement());
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

}