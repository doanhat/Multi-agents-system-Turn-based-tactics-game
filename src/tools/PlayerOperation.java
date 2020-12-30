package tools;

public class PlayerOperation extends Model{
    private String operation;
    private Player player;

    public PlayerOperation() {
    }

    public PlayerOperation(String operation, Player player) {
        this.operation = operation;
        this.player = player;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }
}
