package tools;

import jade.core.AID;

public class Player extends Model{
    //private UUID id;
    private AID agentId; //unique
    private int nbrVictory;
    private int nbrDefeat;
    private int level;
    private int attack;
    private int defense;
    private int life;
    private int initiative;
    private int dodge;
    private int experience;

    public Player() {
    }

    public Player(AID agentId, int nbrVictory, int nbrDefeat, int level, int attack, int defense, int life, int initiative, int dodge, int experience) {
        this.agentId = agentId;
        this.nbrVictory = nbrVictory;
        this.nbrDefeat = nbrDefeat;
        this.level = level;
        this.attack = attack;
        this.defense = defense;
        this.life = life;
        this.initiative = initiative;
        this.dodge = dodge;
        this.experience = experience;
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

    public void setNbrVictory(int nbrVictory) {
        this.nbrVictory = nbrVictory;
    }

    public void setNbrDefeat(int nbrDefeat) {
        this.nbrDefeat = nbrDefeat;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getAttack() {
        return attack;
    }

    public void setAttack(int attack) {
        this.attack = attack;
    }

    public int getDefense() {
        return defense;
    }

    public void setDefense(int defense) {
        this.defense = defense;
    }

    public int getLife() {
        return life;
    }

    public void setLife(int life) {
        this.life = life;
    }

    public int getInitiative() {
        return initiative;
    }

    public void setInitiative(int initiative) {
        this.initiative = initiative;
    }

    public int getDodge() {
        return dodge;
    }

    public void setDodge(int dodge) {
        this.dodge = dodge;
    }

    public int getExperience() {
        return experience;
    }

    public void setExperience(int experience) {
        this.experience = experience;
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
