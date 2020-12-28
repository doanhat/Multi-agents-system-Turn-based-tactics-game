package tools;

public class Characteristics extends Model{
	
	private int attack;
	private int defense;
	private int health;
	private int initiative;
	private int dodge;
	private int experience;
	private int level;
	
	public Characteristics() {
		this.attack = 0;
		this.defense = 0;
		this.health = 0;
		this.initiative = 0;
		this.dodge = 0;
		this.experience = 0;
		this.level = 0;
	}
	
	public Characteristics(int attack, int defense, int health, int initiative, int dodge, int experience, int level) {
		this.attack = attack;
		this.defense = defense;
		this.health = health;
		this.initiative = initiative;
		this.dodge = dodge;
		this.experience = experience;
		this.level = level;
	}
	
	public int getAttack() {
		return attack;
	}
	public int getDefense() {
		return defense;
	}
	public int getHealth() {
		return health;
	}
	public int getInitiative() {
		return initiative;
	}
	public int getDodge() {
		return dodge;
	}
	public int getExperience() {
		return experience;
	}
	public int getLevel() {
		return level;
	}
	
	public void setAttack(int attack) {
		this.attack = attack;
	}
	public void setDefense(int defense) {
		this.defense = defense;
	}
	public void setHealth(int health) {
		this.health = health;
	}
	public void setInitiative(int initiative) {
		this.initiative = initiative;
	}
	public void setDodge(int dodge) {
		this.dodge = dodge;
	}
	public void setExperience(int experience) {
		this.experience = experience;
	}
	public void setLevel(int level){
		this.level = level;
	}
	
}