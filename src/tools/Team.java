package tools;

import jade.core.AID;

import java.util.HashMap;

public class Team extends Model{
    //private UUID id;

    private HashMap<AID,Player> playerHashMap;

    public Team(HashMap<AID, Player> playerHashMap) {
        this.playerHashMap = playerHashMap;
    }

    public Team() {
    }

    public HashMap<AID, Player> getPlayerHashMap() {
        return playerHashMap;
    }

    public void setPlayerHashMap(HashMap<AID, Player> playerHashMap) {
        this.playerHashMap = playerHashMap;
    }

    public void addOrUpdatePlayer(Player p){
        playerHashMap.put(p.getAgentId(),p);
    }
}
