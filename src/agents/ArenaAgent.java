package agents;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.SequentialBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.AchieveREInitiator;
import Messages.Messages;
import Messages.serialisation_des_statistiques_joueur;
import tools.*;

import java.util.ArrayList;
import java.util.List;

public class ArenaAgent extends Agent {
	MessageTemplate subscribe_template = MessageTemplate.MatchPerformative(ACLMessage.SUBSCRIBE);
	MessageTemplate inform_template = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
	public int nb_joueurs;
	public int nb_joueurs_A;
	public int nb_joueurs_B;
	public boolean[] joueursA;
	public boolean[] joueursB;
	public int reponses;
	List<Player> playerListInit = new ArrayList<Player>();
	List<Player> playerListFinal= new ArrayList<Player>();
	Characteristics[] playerListCharac;
	Characteristics[] playerListCharacInit;
	public String Actions[] = { "attaquer", "esquiver", "defendre", "lancer_un_sort", "utiliser_un_objet" };
	List<String> TurnA = new ArrayList<String>();
	List<String> TurnB= new ArrayList<String>();
	public List<Integer> priority = new ArrayList<Integer>();
//	public Characteristics[] init_car_joeurs;

	@Override
	public void setup() {
		System.out.println(getLocalName() + "--> Installed");
		// Enregistrement via le DF
		DFTool.registerAgent(this, Constants.ARENA_DF, getLocalName());
		addBehaviour(new SubscribeAgentMatchmaking());
		addBehaviour(new SubscribeAgentClassement());
		addBehaviour(new AttributionDeJoueurs());
	}

	public class SubscribeAgentMatchmaking extends OneShotBehaviour {
		public void action() {
			RegisterModel model = new RegisterModel(getLocalName());
			ACLMessage message = new ACLMessage(ACLMessage.SUBSCRIBE);
			message.addReceiver(DFTool.findFirstAgent(getAgent(), Constants.MATCHMAKER_DF, Constants.MATCHMAKER_DF));
			message.setContent(model.serialize());
			message.setProtocol(Constants.ARENA_DF);
			getAgent().send(message);
		}
	}

	public class SubscribeAgentClassement extends OneShotBehaviour {
		public void action() {
			RegisterModel model = new RegisterModel(getLocalName());
			ACLMessage message = new ACLMessage(ACLMessage.SUBSCRIBE);
			message.addReceiver(DFTool.findFirstAgent(getAgent(), Constants.RANKING_DF, Constants.RANKING_DF));
			message.setContent(model.serialize());
			message.setProtocol(Constants.ARENA_DF);
			getAgent().send(message);
		}
	}

	// 1 )L’Agent Arène reçoit la composition des équipes par l’Agent Matchmaker
	public class AttributionDeJoueurs extends CyclicBehaviour {
		public void action() {
			ACLMessage message = receive(subscribe_template);
			if (message != null) {
				/* commencer la bataille avec les agents */
				playerListInit = Model.deserializeToList(message.getContent(), Player.class);
				playerListFinal = Model.deserializeToList(message.getContent(), Player.class);
				int size = playerListInit.size(); // le nombre de joueurs qu'il reçoit du matchmaking
				nb_joueurs = size;
				nb_joueurs_A = size / 2;
				nb_joueurs_B = size / 2;
				joueursA = new boolean[nb_joueurs_A];
				joueursB = new boolean[nb_joueurs_B];
				playerListCharac = new Characteristics[nb_joueurs];
				String[] names_Joeurs = new String[size];// le nom de chaque de joueur qu'il reçoit du matchmaking
				// 2) L’Agent Arène envoie un message aux agents joueurs
				addBehaviour(new GarderCharacteristiques());

			} else
				block();
		}
	}

	public class GarderCharacteristiques extends SequentialBehaviour {
		GarderCharacteristiques() {
			super();
			for (int i = 0; i < nb_joueurs; i++) {
				serialisation_des_statistiques_joueur my_seria = new serialisation_des_statistiques_joueur(i); // donner
																												// un
																												// identifiant
																												// à
																												// chaque
																												// joueur
				this.addSubBehaviour(new InteractionJoueurArene(myAgent, Messages.Subscribe(ACLMessage.REQUEST,
						playerListInit.get(i).getAgentName(), my_seria.toString(), AID.ISLOCALNAME)));
			}
		}
	}

	// 3) Les Agents Joueurs répondent à l’agent Arène en fournissant leurs
	// caractéristiques
	public class InteractionJoueurArene extends AchieveREInitiator {
		public InteractionJoueurArene(Agent a, ACLMessage msg) {
			super(a, msg);
		}

		protected void handleInform(ACLMessage inform) {
			/* sauvegarder les caractéristiques du joueur */
			// 4) L’Agent Arène récupère toutes les données et commence le combat tour par
			// tour
			serialisation_des_statistiques_joueur my_seria = serialisation_des_statistiques_joueur
					.read(inform.getContent());
			playerListCharac[my_seria.nb_joeur] = my_seria.car;
			reponses++;
			if (reponses == nb_joueurs) { /*
											 * si nous avons obtenu les caractéristiques de tous les joueurs et que nous
											 * pouvons commencer le combat
											 */
				playerListCharacInit = new Characteristics[nb_joueurs];
				playerListCharacInit = playerListCharac;
				priority = Priorites();
				addBehaviour(new DeveloppementDuCombat());
			}
		}
	}

	public class DeveloppementDuCombat extends SequentialBehaviour {
		DeveloppementDuCombat() {
			super();
			/*
			 * développement du combat 4a 4b 4c
			 */
			this.addSubBehaviour(new DeveloppementDuCombatTourATour(nb_joueurs_A, nb_joueurs_B));
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
			int j=0;
			int fin = nb_joueurs_A;
			if( nb_joeurs(joueursA,nb_joueurs_A) ==0) {
				j=nb_joueurs;
				fin = nb_joueurs_A*2;
			}
			
			for (int i=0; i < nb_joueurs; i++) {
				Characteristics mod = playerListCharacInit[i];
				if(i<fin && i>=j) {
					mod = modification_pour_le_gagnant(playerListCharacInit[i]);
				}
				playerListFinal.get(i).setCharacteristics(mod);
				serialisation_des_statistiques_joueur my_seria = new serialisation_des_statistiques_joueur(mod);
				send(Messages.Subscribe(ACLMessage.CONFIRM,playerListInit.get(i).getAgentName() ,my_seria.toString(), AID.ISLOCALNAME));
			}
			// chaque de joueur qu'il reçoit du matchmaking

			// 6)L’Agent Arène envoie les informations de victoire/défaite à l’Agent
			// Classement ainsi que de level up pour les Agents qui montent de niveau.
			String list_player = playerListFinal.toString();
			send(Messages.Subscribe(ACLMessage.INFORM, Constants.RANKING_DF, list_player, AID.ISLOCALNAME)); // des //																									// résultats
			// 7)L’Agent Arène envoie un message à l’Agent Matchmaker pour l’informer qu’il
			// est à nouveau libre
			send(Messages.Subscribe(ACLMessage.INFORM, "MatchmakerAgent", "Libre", AID.ISLOCALNAME));
		}
	}

	public class DeveloppementDuCombatTourATour extends SequentialBehaviour {
		DeveloppementDuCombatTourATour(int nb_equipe_a, int nb_equipe_b) {
			super();
			this.addSubBehaviour(new Demande_d_actions_aux_joueurs());
			if (nb_joeurs(joueursA,nb_equipe_a) != 0 && nb_joeurs(joueursB,nb_equipe_b) != 0) {
				this.addSubBehaviour(new DeveloppementDuCombatTourATour(nb_equipe_a, nb_equipe_b));
			}
		}
	}

	// tourner pour le joueur An ou Bn
	public class TourPlayerABn extends AchieveREInitiator {
		public TourPlayerABn(Agent a, ACLMessage msg) {
			super(a, msg);
		}

		protected void handleInform(ACLMessage inform) {
			// pour enregistrer la liste des tours du joueur
			if (TurnA.size() < nb_joueurs_A) {
				TurnA.add(inform.getContent());
			} else {
				TurnB.add(inform.getContent());
			}
		}
	}

	public class Demande_d_actions_aux_joueurs extends SequentialBehaviour {
		Demande_d_actions_aux_joueurs() {
			super();
			for (int i = 0; i < nb_joueurs_A; i++) {
				this.addSubBehaviour(new TourPlayerABn(myAgent, Messages.Subscribe(ACLMessage.INFORM,
						playerListInit.get(i).getAgentName(), "turn", AID.ISLOCALNAME)));
			}
			for (int i = 0; i < nb_joueurs_B; i++) {
				this.addSubBehaviour(new TourPlayerABn(myAgent,
						Messages.Subscribe(ACLMessage.INFORM, playerListInit.get(nb_joueurs_B+i).getAgentName(), "turn", AID.ISLOCALNAME)));
			}
			
			this.addSubBehaviour(new execution_de_tuour());
		}
	}

	public class execution_de_tuour extends OneShotBehaviour {
		public void action() {
			int nb_turn = nb_joueurs_B*2;
			int i = 0;
			while(i!=nb_turn) {
				if(priority.get(i)<nb_joueurs_A) { //est un joueur appartenant à A
					decision_affecter(priority.get(i),nb_joueurs_B+priority.get(i));
				}
				else { //est un joueur appartenant à B
					decision_affecter(priority.get(i),priority.get(i)-nb_joueurs_B);
				}
			}
			i++;
		}
	}
	
	//Modification du 09/12/2020
	public void decision_affecter(int de,int a) {
		char equipe = 'B';
		if(a>nb_joueurs_A)equipe = 'A';
		boolean viveoupas;
		if(a<nb_joueurs_A) {
			viveoupas =joueursA[a];
		}
		else {
			viveoupas = joueursB[a-nb_joueurs_B];
		}
		if(joueur_affecte(a,equipe) !=-1 && viveoupas) {
			a = joueur_affecte(a,equipe);
			int i;
			if(de<nb_joueurs_A) {
				i = index_action(TurnA.get(de));
			}
			else {
				i = index_action(TurnB.get(de-nb_joueurs_B));
			}
			switch(i) {
			case 0:				
				int t;
				if(a<nb_joueurs_A) {
					t = index_action(TurnA.get(a));
				}
				else {
					t = index_action(TurnB.get(a+nb_joueurs_B));
				}
				if(t==2) { //si le joueur attaqué se défend
					int damage = playerListCharac[a].getDefense() - playerListCharac[de].getAttack();
					if(damage<0)damage=0;
					playerListCharac[a].setHealth(playerListCharac[a].getHealth()-damage);
					if(playerListCharac[a].getHealth()<=0) {
						if(a<nb_joueurs_A) {
							joueursA[a]=false;
						}
						else {
							joueursB[a-nb_joueurs_B] = false;
						}
					}
				}
				else if(t==1) { //si le joueur attaqué se défend esquive
					int damage = playerListCharac[de].getAttack();
					int randomNum = 0 + (int)(Math.random() * 100);
					if(randomNum%playerListCharac[a].getDodge()==0)playerListCharac[a].setHealth(playerListCharac[a].getHealth()-damage);
					if(playerListCharac[a].getHealth()<=0 ) {
						if(a<nb_joueurs_A) {
							joueursA[a]=false;
						}
						else {
							joueursB[a-nb_joueurs_B] = false;
						}
					}
				}
				else {//si le joueur attaqué se attaque
					int damage = playerListCharac[de].getAttack();
					playerListCharac[a].setHealth(playerListCharac[a].getHealth()-damage);
					if(playerListCharac[a].getHealth()<=0) {
						if(a<nb_joueurs_A) {
							joueursA[a]=false;
						}
						else {
							joueursB[a-nb_joueurs_B] = false;
						}
					}
				}
				break;
			case 1:
				System.out.print("Je vais esquiver" );
				break;
			case 2:
				System.out.print("Je me protégeais" );
				break;
			case 3:
				int randomNum = 0 + (int)(Math.random() * 100);
				if(randomNum%2==0) {
					if(playerListCharac[a].getDefense()>0)
						playerListCharac[a].setDefense(playerListCharac[a].getDefense()-1);
				}
				else {
					playerListCharac[de].setAttack(playerListCharac[de].getAttack()+1);
				}
				break;
			case 4:
				int randomNum2 = 0 + (int)(Math.random() * 100);
				if(randomNum2%3==0) {
					if(playerListCharac[de].getHealth()!=playerListCharacInit[de].getHealth()) {
						playerListCharac[de].setHealth(playerListCharac[de].getHealth()+1);
					}
					break;	
				}
				if(randomNum2%3==1) {
					playerListCharac[de].setAttack(playerListCharac[de].getAttack()+1);
				}
				else {
					playerListCharac[de].setDefense(playerListCharac[de].getDefense()+1);
				}
				break;				
			}
		}
	}
	
	public int index_action(String ac) {
		int i=0;
		if(ac.compareTo("attaquer")==0)i=0;
		else if(ac.compareTo("esquiver")==0)i=1;
		else if(ac.compareTo("defendre")==0)i=2;
		else if(ac.compareTo("lancer_un_sort")==0)i=3;
		else if(ac.compareTo("utiliser_un_objet")==0)i=4;
		return i;
	}
	//Modication du 09/12/2020

	public int joueur_affecte(int nb, char equipeAouB) {
		if (equipeAouB == 'A') {
			if (joueursB[nb])
				return nb;
			else {
				while (!joueursB[nb]) {
					nb++;
					if (nb >= nb_joueurs_A) {
						if (nb_joueurs_A == 1)
							return -1;
						nb = 0;
					}
				}
				return nb;
			}
		} else {
			if (joueursA[nb])
				return nb;
			else {
				while (!joueursA[nb]) {
					nb++;
					if (nb >= nb_joueurs_B) {
						if (nb_joueurs_B == 1)
							return -1;
						nb = 0;
					}
				}
				return nb;
			}
		}
	}
	
	public int nb_joeurs(boolean LB[],int nb) {
		int acum = 0;
		for(int i=0;i<nb;i++) {
			if(LB[i])acum++;
		}
		return acum;
	}
	
	public List<Integer> Priorites(){
		List<Integer>prio =  new ArrayList<Integer>();
		List<Integer>deja = new ArrayList<Integer>();
		for(int j = 0;j<nb_joueurs;j++) {
			int index = 0;
			int plus_prio = playerListInit.get(0).getCharacteristics().getInitiative();
			if(dejalist(deja,index)) {
				while(dejalist(deja,index)) {
					index++;
					plus_prio = playerListInit.get(index).getCharacteristics().getInitiative();
				}
			}
			for(int i = 1;i<nb_joueurs;i++) {
				if(plus_prio<playerListInit.get(i).getCharacteristics().getInitiative() && !dejalist(deja,i)) {
					plus_prio = playerListInit.get(i).getCharacteristics().getInitiative();
					index = i;
				}
			}
			prio.add(index);
			deja.add(index);
		}
		
		return prio;
	}
	
	public boolean dejalist(List<Integer> lt,int it) {
		for(int i = 0; i<lt.size();i++) {
			if(lt.get(i)==it)return true;
		}
		return false;
	}

	public Characteristics modification_pour_le_gagnant(Characteristics init) {
		Characteristics mod = init;
		mod.setExperience(init.getExperience()+1);
		if(mod.getExperience() % 5 ==0) {
			mod.setLevel(init.getLevel()+1);
			mod.setInitiative(init.getInitiative()+1);
			mod.setHealth(init.getHealth()+2);
			mod.setAttack(init.getAttack()+1);
			mod.setDefense(init.getDefense()+1);
			mod.setDodge(init.getDodge()+1);
		}		
		return mod;
	}
}