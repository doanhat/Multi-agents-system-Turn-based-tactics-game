package agents;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.AchieveREInitiator;
import Messages.Messages;
import tools.*;

public class ArenaAgent extends Agent {
	MessageTemplate subscribe_template = MessageTemplate.MatchPerformative(ACLMessage.SUBSCRIBE);
	MessageTemplate inform_template = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
	public int nb_joueurs;
	public int reponses;
//	public Characteristics[] init_car_joeurs;


	@Override
	public void setup() {
		System.out.println(getLocalName() + "--> Installed");
		//Enregistrement via le DF
		DFTool.registerAgent(this, Constants.ARENA_DF,getLocalName());
		addBehaviour(new SubscribeAgentMatchmaking());
		addBehaviour(new SubscribeAgentClassement());
		addBehaviour(new AttributionDeJoueurs());
	}

	public class SubscribeAgentMatchmaking extends OneShotBehaviour {
		public void action() {
			RegisterModel model =  new RegisterModel(getLocalName());
			ACLMessage message = new ACLMessage(ACLMessage.SUBSCRIBE);
			message.addReceiver(DFTool.findFirstAgent(getAgent(), Constants.MATCHMAKER_DF, Constants.MATCHMAKER_DF));
			message.setContent(model.serialize());
			getAgent().send(message);
		}
	}

	public class SubscribeAgentClassement extends OneShotBehaviour {
		public void action() {
			RegisterModel model =  new RegisterModel(getLocalName());
			ACLMessage message = new ACLMessage(ACLMessage.SUBSCRIBE);
			message.addReceiver(DFTool.findFirstAgent(getAgent(), Constants.RANKING_DF, Constants.RANKING_DF));
			message.setContent(model.serialize());
			getAgent().send(message);
		}
	}

	//1 )L’Agent Arène reçoit la composition des équipes par l’Agent Matchmaker
	public class AttributionDeJoueurs extends CyclicBehaviour {
		public void action() {
			ACLMessage message = receive(subscribe_template);
			if (message != null) {
				/*commencer la bataille avec les agents*/
				int size =  4; // le nombre de joueurs qu'il reçoit du matchmaking
				nb_joueurs = size;
				//init_car_joeurs = new Characteristics[size];
				String[] names_Joeurs = new String[size];// le nom de chaque de joueur qu'il reçoit du matchmaking
				// 2) L’Agent Arène envoie un message aux agents joueurs
				for(int i = 0; i<size;i++) { 
					//serialisation_des_statistiques_joueur my_seria = new serialisation_des_statistiques_joueur(i); //donner un identifiant à chaque joueur
					addBehaviour(new InteractionJoueurArene(myAgent,Messages.Subscribe(ACLMessage.REQUEST,names_Joeurs[i],names_Joeurs[i] , AID.ISLOCALNAME)));
				}
			} else
				block();
		}
	}
	//3) Les Agents Joueurs répondent à l’agent Arène en fournissant leurs caractéristiques
	public class InteractionJoueurArene extends AchieveREInitiator{
		public InteractionJoueurArene(Agent a, ACLMessage msg) {
			super(a, msg);
		}
		protected void handleInform(ACLMessage inform) {
			/*sauvegarder les caractéristiques du joueur*/
			//4) L’Agent Arène récupère toutes les données et commence le combat tour par tour
		//	serialisation_des_statistiques_joueur my_seria = serialisation_des_statistiques_joueur.read(inform.getContent());
			//init_car_joeurs[my_seria.nb_joeur] = my_seria.car;
			reponses++;
			if(reponses==nb_joueurs) { /*si nous avons obtenu les caractéristiques de tous les joueurs et que nous pouvons commencer le combat*/
				addBehaviour(new DeveloppementDuCombat());
			}
		}	
	}
	
	public class DeveloppementDuCombat extends SequentialBehaviour {
		DeveloppementDuCombat() {
			super();
			int nb_equipe_a = 4; // nombre de combattants de l'équipe A - changer lorsque nous obtenons la
			// sérialisation
			int nb_equipe_b = 4; // nombre de combattants de l'équipe B - changer lorsque nous obtenons la
			// sérialisation
			/*
			 * développement du combat 4a 4b 4c
			 */
			this.addSubBehaviour(new DeveloppementDuCombatTourATour(nb_equipe_a, nb_equipe_b));
			// 5)Une fois le combat finit, l’Agent Arène attribue l’expérience gagnée aux
			this.addSubBehaviour(new Fin_de_Combat());

		}

	}

	public class Fin_de_Combat extends OneShotBehaviour {
		public void action() {
			// Agents Joueurs, ce qui les informe aussi de la fin du combat
			/* Modifiction des attributes */
			String[] names_Joeurs = new String[nb_joueurs];
			// Characteristics[] car_joeurs = new Characteristics[nb_joueurs];// le nom de
			// chaque de joueur qu'il reçoit du matchmaking
			for (int i = 0; i < nb_joueurs; i++) {
				// serialisation_des_statistiques_joueur my_seria = new
				// serialisation_des_statistiques_joueur(car_joeurs[i]);
				send(Messages.Subscribe(ACLMessage.CONFIRM, names_Joeurs[i], " my_seria.toJSON()", AID.ISLOCALNAME));
			}
			// chaque de joueur qu'il reçoit du matchmaking

			// 6)L’Agent Arène envoie les informations de victoire/défaite à l’Agent
			// Classement ainsi que de level up pour les Agents qui montent de niveau.
			String serialisation = "donnes_joueurs_du_combat";
			send(Messages.Subscribe(ACLMessage.INFORM, "RankingAgent", serialisation, AID.ISLOCALNAME)); // des //
																											// résultats
			// 7)L’Agent Arène envoie un message à l’Agent Matchmaker pour l’informer qu’il
			// est à nouveau libre
			send(Messages.Subscribe(ACLMessage.INFORM, "MatchmakerAgent", "Libre", AID.ISLOCALNAME));
		}
	}

	public class DeveloppementDuCombatTourATour extends SequentialBehaviour {
		DeveloppementDuCombatTourATour(int nb_equipe_a, int nb_equipe_b) {
			super();
			this.addSubBehaviour(new TourPlayerAn(myAgent,
					Messages.Subscribe(ACLMessage.INFORM, "RankingAgent", "n", AID.ISLOCALNAME)));// change le message
			this.addSubBehaviour(new TourPlayerBn(myAgent,
					Messages.Subscribe(ACLMessage.INFORM, "RankingAgent", "n", AID.ISLOCALNAME)));// change le message
			if (nb_equipe_a != 0 && nb_equipe_b != 0) {
				this.addSubBehaviour(new DeveloppementDuCombatTourATour(nb_equipe_a, nb_equipe_b));
			}
		}
	}

	// tourner pour le joueur An
	public class TourPlayerAn extends AchieveREInitiator {
		public TourPlayerAn(Agent a, ACLMessage msg) {
			super(a, msg);
		}

		protected void handleInform(ACLMessage inform) {

		}
	}

	// tourner pour le joueur Bn
	public class TourPlayerBn extends AchieveREInitiator {
		public TourPlayerBn(Agent a, ACLMessage msg) {
			super(a, msg);
		}

		protected void handleInform(ACLMessage inform) {

		}
	}
}