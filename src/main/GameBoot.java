package main;

import java.io.File;

import jade.core.ProfileException;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;
import tools.Constants;

/**
 * Classe de lancement du conteneur d'agent secondaire pour la résolution 
 * de Sudoku à partir d'un fichier de configuration.
 */
public class GameBoot {
	public static String SECOND_PROPERTIES_FILE = "properties/second.properties";
	public static void main (String[] args) {
		startWithProfile();
	}
	public static void startWithProfile() {
		Runtime rt = Runtime.instance();
		ProfileImpl p = null;
		ContainerController cc;
		try {
			p = new ProfileImpl(SECOND_PROPERTIES_FILE);
			cc = rt.createAgentContainer(p);
			//create agents here
			cc.createNewAgent(Constants.MATCHMAKER_DF, "agents.MatchmakerAgent", null).start();
			for(int i = 0; i < Constants.NBR_ARENA; ++i) {
				cc.createNewAgent(Constants.ARENA_DF + i, "agents.ArenaAgent", null).start();
			}
			for(int i = 0; i < Constants.NBR_PLAYER; ++i) {
				cc.createNewAgent(Constants.PLAYER_DF + i, "agents.PlayerAgent", null).start();
			}
			cc.createNewAgent(Constants.RANKING_DF,"agents.RankingAgent",null).start();

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
