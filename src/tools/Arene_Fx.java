package tools;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.databind.ObjectMapper;


public class Arene_Fx {
	public boolean[] joueursA;
    public boolean[] joueursB;
    public List<Player> playerList;
    
    public Arene_Fx(boolean[] A,boolean[] B,List<Player> playerListp) {
    	super();
    	this.joueursA = A;
        this.joueursB = B;
        this.playerList = playerListp;
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

	public static Arene_Fx read(String jsonString) {
		ObjectMapper mapper = new ObjectMapper();
		Arene_Fx fv = null;
		try {
			fv = mapper.readValue(jsonString, Arene_Fx.class);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return fv;
	}
}
