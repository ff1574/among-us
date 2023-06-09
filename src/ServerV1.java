import java.io.*;
import java.net.*;
import java.util.*;

public class ServerV1 {

    // Server Attributes
    private ServerSocket sSocket = null;
    private static final int SERVER_PORT = 12345;

    // Player Attributes
    private int playerID = 1;
    Vector<ObjectOutputStream> playerList = new Vector<>();
    Object lock = new Object();

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

        // Communication Attributes
        private ObjectOutputStream oos = null;
        private ObjectInputStream ois = null;

        public ClientThread(int playerID, Socket socket) {
            this.playerID = playerID;
            this.socket = socket;
        }

        public void run() {
            System.out.println("Player " + playerID + " connected.");

            try {
                oos = new ObjectOutputStream(socket.getOutputStream());
                ois = new ObjectInputStream(socket.getInputStream());

                playerList.add(oos);

                // Tell client what ID they were assigned
                oos.writeObject(playerID);
                // Tell client which tasks they need to complete
                oos.writeObject(giveRandomTasks());

                // While client is connected
                while(!socket.isClosed()) {
                    try {
                        // Read data
                        Object data = ois.readObject();

                        // If its clients player data, take it and broadcast it to other players
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

        // Function that broadcasts to all players on the list
        private void broadcast(Player player) {
            synchronized(lock) {
                for(int i = 0; i < playerList.size(); i++) {
                    try {
                        playerList.get(i).writeObject(player);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        // Constant for the amount of tasks the players receive
        private static final int TASK_AMOUNT = 5;
        // Method that randomly gives tasks to the players
        private String giveRandomTasks() {
            // Attributes
            String returnTasks = "TASKS:";
            String[] taskList = {"MAP", "RUDDER", "GUN", "MAST", "CANNON", "BED", "CAPTAINQUARTERS", "UPPERSTORAGE", "SLEEPINGQUARTERS", "DINEHALL", "PUMP"};
            ArrayList<String> selectedTasks = new ArrayList<>();
            Random random = new Random();

            // While there is less than 5 tasks given
            while(selectedTasks.size() < TASK_AMOUNT) {
                // Select a new random task from the array
                String task = taskList[random.nextInt(taskList.length)];

                // Ensure it is unique/not duplicate
                if(!selectedTasks.contains(task)) {
                    // Set the task to the list, if it is the last task in the list don't set a ',' at the end
                    if(selectedTasks.size() == TASK_AMOUNT-1) {
                        selectedTasks.add(task);
                        returnTasks += task;
                    } else {
                        selectedTasks.add(task);
                        returnTasks += task + ",";
                    }
                }
            }
            return returnTasks;
        }
    }

    public static void main(String[] args) {
        ServerV1 server = new ServerV1();
        ServerThread st = server.new ServerThread();
        st.start();
    }
}
