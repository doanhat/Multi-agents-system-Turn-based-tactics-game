package tools;

public class PlayerRanking extends Model{
    private int winrateR;
    private int levelR;

    public PlayerRanking() {
        this.winrateR = -1;
        this.levelR = -1;

    }

    public PlayerRanking(int winrateR, int levelR) {
        this.winrateR = winrateR;
        this.levelR = levelR;
    }

    public int getWinrateR() {
        return winrateR;
    }

    public void setWinrateR(int winrateR) {
        this.winrateR = winrateR;
    }

    public int getLevelR() {
        return levelR;
    }

    public void setLevelR(int levelR) {
        this.levelR = levelR;
    }
}
