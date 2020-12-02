package tools;

import jade.core.AID;

public class Player extends Model{
    //private UUID id;
    private AID agentId; //unique
    private Integer nbrVictory;
    private Integer nbrDefeat;
    private Integer level;
    private Characteristics characteristics;
    public Player() {
    }

    public Player(AID agentId, Integer nbrVictory, Integer nbrDefeat, Integer level, Characteristics characteristics) {
        this.agentId = agentId;
        this.nbrVictory = nbrVictory;
        this.nbrDefeat = nbrDefeat;
        this.level = level;
        this.characteristics = characteristics;
    }


    public float getWinrate(){
        if (nbrVictory+nbrDefeat>0){
            return nbrVictory*100f/(nbrDefeat+nbrVictory);
        } else return 0;
    }

    public Integer getLevel(){
        return level;
    }
    /*public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }*/

    public AID getAgentId() {
        return agentId;
    }

    public void setAgentId(AID agentId) {
        this.agentId = agentId;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public Integer getNbrVictory() {
        return nbrVictory;
    }

    public void setNbrVictory(Integer nbrVictory) {
        this.nbrVictory = nbrVictory;
    }

    public Integer getNbrDefeat() {
        return nbrDefeat;
    }

    public void setNbrDefeat(Integer nbrDefeat) {
        this.nbrDefeat = nbrDefeat;
    }
    
    public Characteristics getCharacteristics() {
        return characteristics;
    }
    
    public void setCharacteristics(Characteristics characteristics) {
        this.characteristics = characteristics;
    }
    

    @Override
    public String toString() {
        return "Player{" +
                "agent ='" + agentId + '\'' +
                ", nombre de victoires =" + nbrVictory +
                ", nombre de défaites =" + nbrDefeat +
                ", niveau =" + level +
                '}';
    }
}
