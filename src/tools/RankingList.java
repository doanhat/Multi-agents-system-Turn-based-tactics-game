package tools;

import jade.core.AID;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Classe contient une liste des joueurs pour le classement.
 */
public class RankingList extends Model{

    public static final String RANKING = "classement";
    public static final String BYLEVEL = "niveau";
    public static final String BYWINRATE = "rapport";

    private List<Player> playerList;
    // On a un hashmap pour controller l'unicité d'un joueur dans la liste, hashmap au lieu de set car
    // hashmap est plus puissance que set.
    private HashMap<AID,Player> playerHashMap;


    public RankingList(List<Player> playerList) {
        this.playerList = playerList;
        //Convertir automatiquement List -> HashMap
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

    /**
     * Ajouter ou Mettre à jour un joueur dans la liste,
     *
     * @param player le joueur
     */
    public void addOrUpdatePlayer(Player player){
        this.playerHashMap.put(player.getAgentId(),player);
        this.playerList = toList(this.playerHashMap);
    }

    /**
     * Ajouter ou Mettre à jour des joueurs dans la liste,
     *
     * @param players les joueurs
     */
    public void addOrUpdatePlayers(List<Player> players){
        for (Player player : players) {
            this.playerHashMap.put(player.getAgentId(),player);
        }
        this.playerList = toList(this.playerHashMap);
    }

    /**
     * STATIC : convertir une liste des jouers en map
     *
     * @param playerList la liste
     * @return le map
     */
    public static Map<AID, Player> toMap(List<Player> playerList){
        return playerList
                .stream()
                .collect(Collectors.toMap(Player::getAgentId,p ->p));
    }

    /**
     * STATIC : convertir un hashmap des jouers en liste
     *
     * @param playerHashMap le hashmap
     * @return la liste
     */
    public static List<Player> toList(HashMap<AID,Player> playerHashMap) {
        return new ArrayList<>(playerHashMap.values());
    }

    /**
     * Obtenir le classement par niveau
     *
     * @return le hashmap en format {'classement':[<liste joueurs ordonnés>]}
     */
    public HashMap<String, List<Player>> getLevelRanking(){
        List<Player> playerListTmp = playerList
                .stream()
                .sorted(Comparator.comparingInt(Player::getLevel).reversed())
                .collect(Collectors.toList());
        HashMap<String,List<Player>> playerListMap = new HashMap<>();
        playerListMap.put(RANKING,playerListTmp);
        return playerListMap;
    }

    /**
     * Obtenir le classement par rapport victoire/défaite
     *
     * @return le hashmap en format {'classement':[<liste joueurs ordonnés>]}
     */
    public HashMap<String, List<Player>> getWinRateRanking(){
        List<Player> playerListTmp = playerList
                .stream()
                .sorted(Comparator.comparingDouble(Player::getWinrate).reversed())
                .collect(Collectors.toList());
        HashMap<String,List<Player>> playerListMap = new HashMap<>();
        playerListMap.put(RANKING,playerListTmp);
        return playerListMap;
    }

    /**
     * Obtenir le classment d'un joueur par niveau
     *
     * @param aidPlayer l'aid de joueur
     * @return le classment, -1 si non existant
     */
    public int getPlayerLevelRanking (AID aidPlayer){
        List<Player> playerList = this.getLevelRanking().get(RANKING);
        for (int i = 0;i < playerList.size();i++){
            if (playerList.get(i).getAgentId().equals(aidPlayer)){
                return i+1;
            }
        }
        return -1;
    }

    /**
     * Obtenir le classment d'un joueur par rapport victoire/défaite
     *
     * @param aidPlayer l'aid de joueur
     * @return le classment, -1 si non existant
     */
    public int getPlayerWinrateRanking (AID aidPlayer){
        List<Player> playerList = this.getWinRateRanking().get(RANKING);
        for (int i = 0;i < playerList.size();i++){
            if (playerList.get(i).getAgentId().equals(aidPlayer)){
                return i+1;
            }
        }
        return -1;
    }

}
