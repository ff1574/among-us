import java.io.*;
import java.net.*;

public class Player implements Serializable {
    private static final long serialVersionUID = 1L;
    private int playerPosX;
    private int playerPosY;
    private int playerID;
    private Socket socket;

    public Player(int playerID, int playerPosX, int playerPosY, Socket socket) {
        this.playerPosX = playerPosX;
        this.playerPosY = playerPosY;
        this.playerID = playerID;
        this.socket = socket;
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

    public Socket getSocket() {
        return socket;
    }
}