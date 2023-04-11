import java.io.*;
import java.net.*;
import java.util.*;

public class ServerV1 {

    // Server Attributes
    private ServerSocket sSocket = null;
    private static final int SERVER_PORT = 12345;

    // Player Attributes
    private int playerID = 1;
    HashMap<Integer, ClientThread> playerList = new HashMap<>();

    private boolean keepGoing = true;

    // Class for accepting players
    class ServerThread extends Thread {
        
        private Socket cSocket = null;

        public void run() {
            try {
                // Start server
                sSocket = new ServerSocket(SERVER_PORT);

                System.out.println("Server started. Waiting for players...");
                while(keepGoing) {

                    // Accept players
                    cSocket = sSocket.accept();
                    ClientThread ct = new ClientThread(playerID, cSocket);
                    playerList.put(playerID, ct);
                    ct.start();
                    playerID++;
                }
            } catch (IOException e) {
                System.out.println("Server stopped.");
            }
        }
    }

    // Class that handles everything when player connects
    class ClientThread extends Thread {

        // Player attributes
        private Socket socket;
        private int playerID;
        private Player player;

        // Communication Attributes
        private ObjectOutputStream oos = null;
        private ObjectInputStream ois = null;

        public ClientThread(int playerID, Socket socket) {
            this.playerID = playerID;
            this.socket = socket;
            this.player = new Player(playerID, 1500, 200);
        }

        public void run() {
            System.out.println("Player " + playerID + " connected.");

            try {
                oos = new ObjectOutputStream(socket.getOutputStream());
                ois = new ObjectInputStream(socket.getInputStream());

                // Tell client what ID they were assigned
                oos.writeObject(playerID);

                while(!socket.isClosed()) {
                    try {
                        Object data = ois.readObject();

                        if(data instanceof Player) {
                            Player player = (Player) data;
                            broadcast(player);
                        }
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } 

        private void broadcast(Player player) {
            for(int i = 1; i <= playerList.size(); i++) {
                try {
                    playerList.get(i).oos.writeObject(player);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void main(String[] args) {
        ServerV1 server = new ServerV1();
        ServerThread st = server.new ServerThread();
        st.start();
    }
}
