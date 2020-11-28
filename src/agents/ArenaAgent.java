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
	        int nb_equipe_a = 4; //nombre de combattants de l'équipe A - changer lorsque nous obtenons la sérialisation
			int nb_equipe_b = 4; //nombre de combattants de l'équipe B - changer lorsque nous obtenons la sérialisation
			while(nb_equipe_a!=0 && nb_equipe_b!=0) {
				/*développement du combat
				 * 4a
				 * 4b
				 * 4c
				 */
			}
			//5)Une fois le combat finit, l’Agent Arène attribue l’expérience gagnée aux Agents Joueurs, ce qui les informe aussi de la fin du combat
				/*Modifiction des attributes*/
			String[] names_Joeurs = new String[nb_joueurs];// le nom de chaque de joueur qu'il reçoit du matchmaking
			for(int i = 0; i<nb_joueurs;i++) { 
				String newSerialisation = "nouveaux résultats basés sur la modification";
				send(Messages.Subscribe(ACLMessage.CONFIRM,names_Joeurs[i], newSerialisation, AID.ISLOCALNAME));
			}
			//6)L’Agent Arène envoie les informations de victoire/défaite à l’Agent Classement ainsi que de level up pour les Agents qui montent de niveau.
			String serialisation = "donnes_joueurs_du_combat";
			send(Messages.Subscribe(ACLMessage.INFORM, "RankingAgent", serialisation, AID.ISLOCALNAME)); // changer le nom local en une sérialisation des résultats
			//7)L’Agent Arène envoie un message à l’Agent Matchmaker pour l’informer qu’il est à nouveau libre
			send(Messages.Subscribe(ACLMessage.INFORM, "MatchmakerAgent", "Libre", AID.ISLOCALNAME));		
		}
	}
}