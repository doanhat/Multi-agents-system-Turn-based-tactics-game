package tools;

import jade.core.AID;

public class Player extends Model{
    //private UUID id;
    private AID agentId; //unique
    private int nbrVictory;
    private int nbrDefeat;
    private Characteristics characteristics;

    public Player() {
    }

    public Player(AID agentId, int nbrVictory, int nbrDefeat, Characteristics characteristics) {
        this.agentId = agentId;
        this.nbrVictory = nbrVictory;
        this.nbrDefeat = nbrDefeat;
        this.characteristics = characteristics;
    }

    public float getWinrate(){
        if (nbrVictory+nbrDefeat>0){
            return nbrVictory*100f/(nbrDefeat+nbrVictory);
        } else return 0;
    }

    public Integer getLevel(){
        return characteristics.getNiveau();
    }
    /*public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }*/

    public void setNbrVictory(int nbrVictory) {
        this.nbrVictory = nbrVictory;
    }

    public void setNbrDefeat(int nbrDefeat) {
        this.nbrDefeat = nbrDefeat;
    }

    public void setLevel(int level) {
        this.characteristics.setNiveau(level);
    }

    public int getAttack() {
        return characteristics.getAttaque();
    }

    public void setAttack(int attack) {
        this.characteristics.setAttaque(attack);
    }

    public int getDefense() {
        return characteristics.getDefense();
    }

    public void setDefense(int defense) {
        this.characteristics.setDefense(defense);
    }

    public int getLife() {
        return characteristics.getVie();
    }

    public void setLife(int life) {
        this.characteristics.setVie(life);
    }

    public int getInitiative() {
        return characteristics.getInitiative();
    }

    public void setInitiative(int initiative) {
        this.characteristics.setInitiative(initiative);
    }

    public int getDodge() {
        return characteristics.getEsquive();
    }

    public void setDodge(int dodge) {
        this.characteristics.setEsquive(dodge);
    }

    public int getExperience() {
        return characteristics.getExperience();
    }

    public void setExperience(int experience) {
        this.characteristics.setExperience(experience);
    }

    public AID getAgentId() {
        return agentId;
    }

    public void setAgentId(AID agentId) {
        this.agentId = agentId;
    }

    public int getNbrVictory() {
        return nbrVictory;
    }

    public int getNbrDefeat() {
        return nbrDefeat;
    }
    
    public Characteristics getCharacteristics() {
        return characteristics;
    }
    
    public void setCharacteristics(Characteristics characteristics) {
        this.characteristics = characteristics;
    }

}
