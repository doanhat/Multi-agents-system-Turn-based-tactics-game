public class Caracteristiques {
	
	private int Attaque;
	private int Defense;
	private int Vie;
	private int Initiative;
	private int Esquive;
	private int Experience;
	private int Niveau;
	
	public Caracteristiques() {

	}
	
	public Caracteristiques(int Attaque,int Defense,int Vie,int Initiative,int Esquive,int Experience, int Niveau) {
		this.Attaque = Attaque;
		this.Defense = Defense;
		this.Vie = Vie;
		this.Initiative = Initiative;
		this.Esquive = Esquive;
		this.Experience = Experience;
		this.Niveau = Niveau;
	}
	
	public int getAttaque() {
		return Attaque;
	}
	public int getDefense() {
		return Defense;
	}
	public int getVie() {
		return Vie;
	}
	public int getInitiative() {
		return Initiative;
	}
	public int getEsquive() {
		return Esquive;	
	}
	public int getExperience() {
		return Experience;
	}
	public int getNiveau() {
		return Niveau;
	}
	
	public void setAttaque(int Attaque) {
		this.Attaque = Attaque;
	}
	public void setDefense(int Defense) {
		this.Defense = Defense;
	}
	public void setVie(int Vie) {
		this.Vie = Vie;
	}
	public void setInitiative(int Initiative) {
		this.Initiative = Initiative;
	}
	public void setEsquive(int Esquive) {
		this.Esquive = Esquive;	
	}
	public void setExperience(int Experience) {
		this.Experience = Experience;
	}
	public void setNiveau(int Niveau){
		this.Niveau = Niveau;
	}
	
}