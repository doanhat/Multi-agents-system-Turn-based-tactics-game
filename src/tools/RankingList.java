package tools;

import jade.core.AID;

import java.util.*;
import java.util.stream.Collectors;

public class RankingList extends Model{

    public static final String RANKING = "classement";
    public static final String BYLEVEL = "niveau";
    public static final String BYWINRATE = "rapport";

    private List<Player> playerList;
    private HashMap<AID,Player> playerHashMap;


    public RankingList(List<Player> playerList) {
        this.playerList = playerList;
        this.playerHashMap = (HashMap<AID, Player>) this.toMap(playerList);
    }

    public RankingList() {
        this.playerList = new ArrayList<>();
        this.playerHashMap = new HashMap<>();
    }

    public List<Player> getPlayerList() {
        return playerList;
    }

    public void setPlayerList(List<Player> playerList) {
        this.playerList = playerList;
    }

    public HashMap<AID, Player> getPlayerHashMap() {
        return playerHashMap;
    }

    public void setPlayerHashMap(HashMap<AID, Player> playerHashMap) {
        this.playerHashMap = playerHashMap;
    }

    public void addPlayer(Player player){
        this.playerList.add(player);
    }

    public static Map<AID, Player> toMap(List<Player> playerList){
        return playerList
                .stream()
                .collect(Collectors.toMap(Player::getAgentId,p ->p));
    }
    public HashMap<String, List<Player>> getLevelRanking(){
        List<Player> playerListTmp = playerList
                .stream()
                .sorted(Comparator.comparingInt(Player::getLevel).reversed())
                .collect(Collectors.toList());
        HashMap<String,List<Player>> playerListMap = new HashMap<>();
        playerListMap.put(RANKING,playerListTmp);
        return playerListMap;
    }

    public HashMap<String, List<Player>> getWinRateRanking(){
        List<Player> playerListTmp = playerList
                .stream()
                .sorted(Comparator.comparingDouble(Player::getWinrate).reversed())
                .collect(Collectors.toList());
        HashMap<String,List<Player>> playerListMap = new HashMap<>();
        playerListMap.put(RANKING,playerListTmp);
        return playerListMap;
    }


}
