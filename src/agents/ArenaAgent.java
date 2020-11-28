import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;

public class ArenaAgent extends Agent {
    
    	MessageTemplate subscribe_template = MessageTemplate.MatchPerformative(ACLMessage.SUBSCRIBE);
	MessageTemplate inform_template = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
	public int nb_joueurs;
	public int reponses;

	public void Setup() {
		System.out.println(getLocalName() + "--> Installed");
		addBehaviour(new Subscribe_Agent_Matchmaking());
		addBehaviour(new Subscribe_Agent_Classement());
		addBehaviour(new Attribution_de_Joueurs());
	}

	public class Subscribe_Agent_Matchmaking extends OneShotBehaviour {
		public void action() {
			send(Messages.Subscribe(ACLMessage.INFORM, "MatchmakerAgent", getLocalName(), AID.ISLOCALNAME));
		}
	}

	public class Subscribe_Agent_Classement extends OneShotBehaviour {
		public void action() {
			send(Messages.Subscribe(ACLMessage.INFORM, "RankingAgent", getLocalName(), AID.ISLOCALNAME));
		}
	}

	//1 )L’Agent Arène reçoit la composition des équipes par l’Agent Matchmaker
	public class Attribution_de_Joueurs extends CyclicBehaviour {
		public void action() {
			ACLMessage message = receive(subscribe_template);
			if (message != null) {
				/*commencer la bataille avec les agents*/
				int size =  4; // le nombre de joueurs qu'il reçoit du matchmaking
				nb_joueurs = size;
				String[] names_Joeurs = new String[size];// le nom de chaque de joueur qu'il reçoit du matchmaking
				// 2) L’Agent Arène envoie un message aux agents joueurs
				for(int i = 0; i<size;i++) { 
					addBehaviour(new interaction_joueur_arene(myAgent,Messages.Subscribe(ACLMessage.REQUEST,names_Joeurs[i], getLocalName(), AID.ISLOCALNAME)));
				}
			} else
				block();
		}
	}
	//3) Les Agents Joueurs répondent à l’agent Arène en fournissant leurs caractéristiques
	public class interaction_joueur_arene extends AchieveREInitiator{
		public interaction_joueur_arene(Agent a, ACLMessage msg) {
			super(a, msg);
		}
		protected void handleInform(ACLMessage inform) {
			/*sauvegarder les caractéristiques du joueur*/
			//4) L’Agent Arène récupère toutes les données et commence le combat tour par tour
			reponses++;
			if(reponses==nb_joueurs) { /*si nous avons obtenu les caractéristiques de tous les joueurs et que nous pouvons commencer le combat*/
			    addBehaviour(new developpement_du_combat());	
			}
		}	
	}
	
	public class developpement_du_combat extends OneShotBehaviour{
		public void action() {
			
		}
	}
}