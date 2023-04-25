import java.io.*;
import java.net.*;
import java.util.*;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class ServerV2 extends Application implements EventHandler<ActionEvent> {

    // Server Attributes
    private ServerV2 server;
    private ServerThread st;
    private ServerSocket sSocket = null;
    private static final int SERVER_PORT = 12345;
    private boolean keepGoing = true;

    // Player Attributes
    private int playerID = 1;
    Vector<ObjectOutputStream> playerList = new Vector<>();
    Object lock = new Object();
    // Amount of impostors
    private int impostorCount = 0;
    // Capacity of impostors
    private int impostorAmount = 1;

    // GUI Attributes
    private Stage stage;
    private Scene scene;
    private Button startStopBtn;
    private TextArea serverLog;
    private ListView<String> playerListView;

    @Override
    public void start(Stage primaryStage) throws Exception {
        this.stage = primaryStage;

        startStopBtn = new Button("Start");
        startStopBtn.setOnAction(this);

        serverLog = new TextArea();
        serverLog.setPrefHeight(270);

        playerListView = new ListView<>();
        playerListView.setPrefHeight(150);

        VBox root = new VBox(10, startStopBtn, serverLog, playerListView);
        scene = new Scene(root, 400, 450);
        stage.setTitle("Amogus Server");
        stage.setScene(scene);
        stage.show();

        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                try {
                    // Stop the server
                    keepGoing = false;
                    sSocket.close();
                    System.exit(0);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void handle(ActionEvent event) {
        if (event.getSource() == startStopBtn) {
            if (server == null) {
                // Start the server
                server = this;
                st = new ServerThread();
                st.start();
                startStopBtn.setText("Stop");
                serverLog.appendText("Server started. Waiting for players...\n");
            } else {
                // Stop the server

                keepGoing = false;
                try {
                    sSocket.close();
                    st.join();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                server = null;
                startStopBtn.setText("Start");
                serverLog.appendText("Server stopped.\n");
            }
        }
    }

    // Class for accepting players
    class ServerThread extends Thread {

        private Socket cSocket = null;

        public void run() {
            try {
                // Start server
                sSocket = new ServerSocket(SERVER_PORT);

                System.out.println("Server started. Waiting for players...");
                while (keepGoing) {

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
            serverLog.appendText("Player " + playerID + " connected.\n");
            try {
                oos = new ObjectOutputStream(socket.getOutputStream());
                ois = new ObjectInputStream(socket.getInputStream());

                playerList.add(oos);

                // Tell client what ID they were assigned
                oos.writeObject(playerID);
                // Tell client which role they have received
                String role = assignRole();
                oos.writeObject(role);

                // If client is crewmate
                if (role.equals("ROLE:CREWMATE")) {
                    // Tell client which tasks they need to complete
                    oos.writeObject(giveRandomTasks());
                }
                // If client is impostor
                if (role.equals("ROLE:IMPOSTOR")) {

                }

                String listInput = "Player ID: " + playerID + " | " + "ROLE: " + role.substring(5);
                Platform.runLater(() -> playerListView.getItems().add(listInput));

                // While client is connected
                while (!socket.isClosed()) {
                    try {
                        // Read data
                        Object data = ois.readObject();

                        // If its clients player data, take it and broadcast it to other players
                        if (data instanceof Player) {
                            Player player = (Player) data;
                            synchronized (playerList) {
                                broadcast(player);
                            }
                        }
                        if (data instanceof Chat) {
                            Chat chat = (Chat) data;
                            synchronized (playerList) {
                                broadcast(chat);
                            }
                        }

                        if (data instanceof Vote) {
                            Vote vote = (Vote) data;
                            synchronized (playerList) {
                                broadcast(vote);
                            }
                        }

                        if (data instanceof String) {
                            String string = (String) data;

                            String[] dataType = string.split(":");
                            if (dataType[0].equals("DISCONNECTING")) {
                                playerList.remove(oos);
                                oos.close();
                                ois.close();
                                socket.close();
                                System.out.println("Player " + playerID + " disconnected.");
                                serverLog.appendText("Player " + playerID + " disconnected.\n");

                                Platform.runLater(() -> playerListView.getItems().remove(listInput));
                            }
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
        private void broadcast(Object object) {
            synchronized (lock) {
                for (int i = 0; i < playerList.size(); i++) {
                    try {
                        playerList.get(i).writeObject(object);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        // Method that will assign a role to the player, either crewmate or impostor
        private String assignRole() {
            if (impostorCount < impostorAmount) {
                if ((Math.random() * 10) > 5) {
                    impostorCount++;
                    return "ROLE:IMPOSTOR";
                } else
                    return "ROLE:CREWMATE";
            } else
                return "ROLE:CREWMATE";
        }

        // Constant for the amount of tasks the players receive
        private static final int TASK_AMOUNT = 4;

        // Method that randomly gives tasks to the players
        private String giveRandomTasks() {
            // Attributes
            String returnTasks = "TASKS:";
            String[] taskList = { "TaskNav", "TaskHelm", "TaskVote", "TaskCannons", "TaskSickBay", "TaskPump",
                    "TaskStorage" };
            // Removed tasks: , "TaskAmmo", "TaskQuarters", "TaskMess", "TaskMast"
            ArrayList<String> selectedTasks = new ArrayList<>();
            Random random = new Random();

            // While there is less than 4 tasks given
            while (selectedTasks.size() < TASK_AMOUNT) {
                // Select a new random task from the array
                String task = taskList[random.nextInt(taskList.length)];

                // Ensure it is unique/not duplicate
                if (!selectedTasks.contains(task)) {
                    // Set the task to the list, if it is the last task in the list don't set a ','
                    // at the end
                    if (selectedTasks.size() == TASK_AMOUNT - 1) {
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
        launch(args);
    }
}
