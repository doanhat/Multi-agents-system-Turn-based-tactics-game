package tools;

import java.util.HashMap;

public class Team extends Model{
    //private UUID id;

    private HashMap<String, Player> playerHashMap;

    public Team(HashMap<String, Player> playerHashMap) {
        this.playerHashMap = playerHashMap;
    }

    public Team() {
    }

    public HashMap<String, Player> getPlayerHashMap() {
        return playerHashMap;
    }

    public void setPlayerHashMap(HashMap<String, Player> playerHashMap) {
        this.playerHashMap = playerHashMap;
    }

    public void addOrUpdatePlayer(Player p){
        playerHashMap.put(p.getAgentName(),p);
    }
}
