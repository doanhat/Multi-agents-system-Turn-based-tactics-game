package tools;

public class Player extends Model{
    //private UUID id;
    private String agentName; //unique
    private int nbrVictory;
    private int nbrDefeat;
    private Characteristics characteristics;

    public Player() {
    }

    public Player(String agentName, int nbrVictory, int nbrDefeat, Characteristics characteristics) {
        this.agentName = agentName;
        this.nbrVictory = nbrVictory;
        this.nbrDefeat = nbrDefeat;
        this.characteristics = characteristics;
    }

    public static float getWinrate(Player p){
        if (p.getNbrVictory()+p.getNbrDefeat()>0){
            return p.getNbrVictory()*100f/(p.getNbrDefeat()+p.getNbrVictory());
        } else return 0;
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

    public String getAgentName() {
        return agentName;
    }

    public void setAgentName(String agentName) {
        this.agentName = agentName;
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

    public static int getLevel(Player p) {
        return p.getCharacteristics().getLevel();
    }

    public void receiveAttack(int attackDmg){
        if (attackDmg != -1){
            int dmg = Math.max(attackDmg - getCharacteristics().getDefense(),1);
            this.getCharacteristics().setHealth(this.getCharacteristics().getHealth()-dmg);
        }
    }

    public void levelUp(){
        this.getCharacteristics().setLevel(this.getCharacteristics().getLevel()+1);
    }
}
