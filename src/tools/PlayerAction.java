package tools;

public class PlayerAction extends Model {
    private String playerName;
    private String action;

    public PlayerAction() {
    }

    public PlayerAction(String playerName, String action) {
        this.playerName = playerName;
        this.action = action;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }
}
