package tools;

import jade.core.AID;

import java.util.*;
import java.util.stream.Collectors;

public class PlayerWaiting {

    //private ArrayList<AID,Integer,Integer> playerList;
	private Player p;
	private AID aid;
    private Integer Lrank;
    private Integer WRrank;

    public PlayerWaiting(Player player, AID aid) {
        this.p = player;
        this.aid = aid;
    }

    public PlayerWaiting() {
        this.p = new Player();
    }

    public void setPlayer(Player player) {
        this.p = player;
    }
    
    public Player getPlayer() {
        return p;
    }

    public void setAID(AID aid) {
        this.aid = aid;
    }

    public AID getAID() {
        return this.aid;
    }
    
    public void setLrank(Integer rank) {
        this.Lrank = rank;
    }
    
    public Integer getLrank() {
    	return this.Lrank;
    }
    
    public void setWRrank(Integer rank) {
        this.WRrank = rank;
    }
    
    public Integer getWRrank() {
    	return this.WRrank;
    }

}
