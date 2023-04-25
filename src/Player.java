import java.io.*;

public class Player implements Serializable {
    private static final long serialVersionUID = 1L;
    private int playerPosX;
    private int playerPosY;
    private int playerID;
    private String playerName;
    private String playerColor;
    private String playerRole;

    

    public Player(int playerPosX, int playerPosY, int playerID, String playerName, String playerColor, String playerRole) {
        this.playerPosX = playerPosX;
        this.playerPosY = playerPosY;
        this.playerID = playerID;
        this.playerName = playerName;
        this.playerColor = playerColor;
        this.playerRole = playerRole;
    }

    public int getPlayerPosX() {
        return playerPosX;
    }

    public void setPlayerPosX(int playerPosX) {
        this.playerPosX = playerPosX;
    }

    public int getPlayerPosY() {
        return playerPosY;
    }

    public void setPlayerPosY(int playerPosY) {
        this.playerPosY = playerPosY;
    }

    public static long getSerialversionuid() {
        return serialVersionUID;
    }

    public int getPlayerID() {
        return playerID;
    }

    public void setPlayerID(int playerID) {
        this.playerID = playerID;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public String getplayerColor() {
        return playerColor;
    }

    public void setplayerColor(String playerColor) {
        this.playerColor = playerColor;
    }

    public String getPlayerRole() {
        return playerRole;
    }

    public void setPlayerRole(String playerRole) {
        this.playerRole = playerRole;
    }
}