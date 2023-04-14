import java.io.*;

public class Player implements Serializable {
    private static final long serialVersionUID = 1L;
    private int playerPosX;
    private int playerPosY;
    private int playerID;

    public Player(int playerID, int playerPosX, int playerPosY) {
        this.playerPosX = playerPosX;
        this.playerPosY = playerPosY;
        this.playerID = playerID;
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
}