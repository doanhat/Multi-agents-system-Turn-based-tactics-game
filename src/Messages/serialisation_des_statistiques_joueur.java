package Messages;

import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;
import tools.Characteristics;

public class serialisation_des_statistiques_joueur {
	public Characteristics car;
	public int nb_joeur;
	
	public serialisation_des_statistiques_joueur() {
		super();
	}

	public serialisation_des_statistiques_joueur(Characteristics number) {
		super();
		this.car = number;
	}
	public serialisation_des_statistiques_joueur(int i) {
		super();
		this.nb_joeur=i;
	}
	public void caract(Characteristics c) {
		this.car = c;
	}
	
	public String toJSON() {
		ObjectMapper mapper = new ObjectMapper();
		String s = "";
		try {
			s = mapper.writeValueAsString(this);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return s;
	}

	public static serialisation_des_statistiques_joueur read(String jsonString) {
		ObjectMapper mapper = new ObjectMapper();
		serialisation_des_statistiques_joueur fv = null;
		try {
			fv = mapper.readValue(jsonString, serialisation_des_statistiques_joueur.class);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return fv;
	}
}