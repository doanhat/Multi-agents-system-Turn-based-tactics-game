package main;

import java.io.File;

import jade.core.ProfileException;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import tools.Constants;

import static jade.core.Runtime.instance;

/**
 * Classe de lancement de la console JADE à partir d'un fichier de configuration,
 * via un conteneur d'agent principal.
 */
public class MainBoot {
	public static String MAIN_PROPERTIES_FILE = "properties/main.properties";

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		boot_gui();
	}

	public static void boot_gui() {
		// open main console gui
		// properties: main=true; gui = true;
		Runtime rt = instance();
		ProfileImpl p = null;
		try {
			p = new ProfileImpl(MAIN_PROPERTIES_FILE);
			rt.createMainContainer(p);
		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}
}
