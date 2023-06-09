import java.io.*;
import java.net.*;
import java.util.*;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class ClientV4 extends Application {

    // Client Attributes
    private static final String SERVER_IP = "127.0.0.1";
    private static final int SERVER_PORT = 12345;
    private Socket socket = null;

    // Communication Attributes
    private ObjectOutputStream oos = null;
    private ObjectInputStream ois = null;

    // GUI Attributes - start
    private Stage stageStart;
    private Scene sceneStart;
    private VBox rootStart;
    private Label ipLabel;
    private TextField ipTextField;
    private Label playerNameLabel;
    private TextField playerNameTextField;
    private Button startButton;

    private ComboBox<?> colorMenu;
    ObservableList<String> colorOptions = FXCollections.observableArrayList("yellow", "red", "blue", "grey");

    // GUI Attributes - chat
    private Stage stageChat;
    private Scene sceneChat;
    private VBox rootChat;
    private Label lblchatName;
    private TextArea taChatRoom;
    private TextArea taChatMsg;
    private Button sendButton;
    private Button voteButton;
    private ComboBox<?> voteMenu;
    ObservableList<String> voteOptions;

    ArrayList<Vote> voteTally = new ArrayList<Vote>();

    // GUI Attributes - game
    private Stage stage;
    private Scene scene;
    private AnchorPane root;

    // Graphics
    private final static String MAP_BOTTOM = "mapFinalBottom.png";
    private final static String MAP_TOP = "mapFinalTop.png";
    private final static String MAP_RGB = "mapRGB.png";

    // Crewmates
    private int playerID;
    private String masterUsername;
    private String playerColor;
    private String playerRole;
    private CrewmateRacer crewmateMaster = null;
    private HashMap<Integer, CrewmateRacer> playerList = new HashMap<>();
    private HashMap<Integer, Player> playerObjList = new HashMap<>();

    // Movable Background
    private MovableBackground movableBottom = null;
    private MovableBackground movableTop = null;
    private MovableBackground movableRGB = null;

    // Update Timer
    private AnimationTimer updateTimer = null;

    // Player Controls
    private boolean up = false, down = false, right = false, left = false;

    // Collision Detection
    private PixelReader pr = null;
    private Image rgbMap;

    // Task Attributes
    private ArrayList<Task> taskList = new ArrayList<>();
    private boolean tasksGotten;
    private boolean taskActivated = false;

    @Override
    public void start(Stage stageStart) throws Exception {// initialize start first
        this.stageStart = stageStart;
        stageStart.setTitle("Start Menu");

        rootStart = new VBox();

        initializeStart();
    }

    // setting up start menu/handling
    public void initializeStart() {

        // row 1, ip info
        ipLabel = new Label("Enter IP:");
        ipTextField = new TextField("127.0.0.1");
        HBox row1 = new HBox(ipLabel, ipTextField);

        // row 2, player info
        playerNameLabel = new Label("Enter a Username:");
        playerNameTextField = new TextField("testing");
        HBox row2 = new HBox(playerNameLabel, playerNameTextField);

        // color options

        colorMenu = new ComboBox<>(colorOptions);
        colorMenu.setEditable(false);
        HBox row3 = new HBox(colorMenu);

        startButton = new Button("Start Game");

        rootStart.getChildren().addAll(row1, row2, row3, startButton);

        sceneStart = new Scene(rootStart, 400, 400);
        stageStart.setScene(sceneStart);
        stageStart.show();

        startButton.setOnAction(event -> {
            stageStart.close();
            masterUsername = playerNameTextField.getText();
            if(colorMenu.getValue().toString() == null) playerColor = "grey";
            else playerColor = colorMenu.getValue().toString();
            System.out.println("My color is: " + playerColor);

            initializeScene();
            initializeChat();
            connectToServer();
        });
    }

    // Function for connecting client to server
    public void connectToServer() {
        try {
            socket = new Socket(SERVER_IP, SERVER_PORT);
            System.out.println("Connected.");

            // Initialize communication
            oos = new ObjectOutputStream(socket.getOutputStream());
            ois = new ObjectInputStream(socket.getInputStream());

            new Thread(() -> {
                listenToServer();
            }).start();

        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void initializeChat() {
        this.stageChat = new Stage();
        stageChat.setTitle("Chat");

        rootChat = new VBox();

        // Username label
        lblchatName = new Label(masterUsername);
        HBox hbox1 = new HBox(lblchatName);
        // chat area
        taChatRoom = new TextArea();
        taChatRoom.setEditable(false);
        HBox hbox2 = new HBox(taChatRoom);
        // message area
        Label lblMsg = new Label("Write message");
        HBox hbox3 = new HBox(lblMsg);

        taChatMsg = new TextArea();
        HBox hbox4 = new HBox(taChatMsg);

        voteOptions = FXCollections.observableArrayList();
        
        voteMenu = new ComboBox<>(voteOptions);

        HBox hbox5 = new HBox(voteMenu);
        voteButton = new Button("VOTE");

        sendButton = new Button("SEND");
        HBox hbox6 = new HBox(sendButton, voteButton);

        rootChat.getChildren().addAll(hbox1, hbox2, hbox3, hbox4, hbox5, hbox6);
        sceneChat = new Scene(rootChat);
        stageChat.setScene(sceneChat);
        stageChat.show();

        sendButton.setOnAction(event -> {
            Chat chat = new Chat(masterUsername, taChatMsg.getText());
         
            if (oos != null) {
                try {
                    synchronized (playerList) {
                        oos.writeObject(chat);
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
            taChatMsg.clear();
        });

        voteButton.setOnAction(event -> {
            Vote vote = new Vote(voteMenu.getValue().toString());

            /***
             * until the voteOptions lists can take all player names, voteValue is a
             * generic "1"
             * 
             */
            if (oos != null) {
                try {
                    synchronized (playerList) {
                        oos.writeObject(vote);
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
            taChatMsg.clear();
        });

    }

    // Function for initializing the whole game
    public void initializeScene() {

        this.stage = new Stage();// values for the stage/scene have to be located here since it's called by the
                                 // start screen

        stage.setTitle("AmongUs - Best Team");

        // When closing with X, disconnect from server and stop app
        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                try {
                    oos.writeObject("DISCONNECTING:" + playerID);
                    System.exit(0);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        root = new AnchorPane();

        // Create Player Character
        crewmateMaster = new CrewmateRacer(true, playerColor);

        // Create Map
        movableRGB = new MovableBackground(MAP_RGB);
        movableBottom = new MovableBackground(MAP_BOTTOM);
        movableTop = new MovableBackground(MAP_TOP);

        // Collision Detection
        rgbMap = new Image(MAP_RGB);
        pr = rgbMap.getPixelReader();

        // Add components to root
        this.root.getChildren().addAll(movableRGB, movableBottom, crewmateMaster, movableTop);

        // Initialize window
        scene = new Scene(root, 1200, 800);
        stage.setScene(scene);
        stage.show();

        // Keyboard Control
        scene.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {

                // Movement
                switch (event.getCode()) {
                    case UP:
                        up = true;
                        break;
                    case DOWN:
                        down = true;
                        break;
                    case LEFT:
                        left = true;
                        break;
                    case RIGHT:
                        right = true;
                        break;
                    default:
                        break;
                }

                if(event.getCode() == KeyCode.E) {
                    for(Task task : taskList) {
                        ImageView taskImage = task.getTaskImage();
                        if(taskImage.isVisible()) {
                            taskActivated = true;
                            taskImage.setImage(new Image(task.getTaskType() + "Event.png"));
                        }
                    }
                }
                if(event.getCode() == KeyCode.SPACE && taskActivated) {
                    for(Task task : taskList) {
                        ImageView taskImage = task.getTaskImage();
                        if(taskImage.isVisible()) {
                            Platform.runLater(() -> {
                                taskImage.setVisible(false);
                                root.getChildren().remove(taskImage);
                            });
                            task = null;
                            
                            taskActivated = false;
                            try {
                                oos.writeObject("TASKDONE");
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        });

        scene.setOnKeyReleased(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                switch (event.getCode()) {
                    case UP:
                        up = false;
                        break;
                    case DOWN:
                        down = false;
                        break;
                    case LEFT:
                        left = false;
                        break;
                    case RIGHT:
                        right = false;
                        break;
                    default:
                        break;
                }
            }
        });

        // Timer updates everything
        updateTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                crewmateMaster.update();
                movableRGB.update();
                movableBottom.update();
                movableTop.update();

            }
        };
        updateTimer.start();
    }

    // Function that actively sends data to server
    public void talkToServer() {
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        while (true) {
            // Always update player position, then send it to server
            int playerPosX = movableRGB.getPlayerPosX();
            int playerPosY = movableRGB.getPlayerPosY();

            Player player = new Player(playerPosX, playerPosY, playerID, masterUsername, playerColor, playerRole);// updated class
                                                                                                      // requires these
                                                                                                      // values
            try {
                if (oos != null) {
                    oos.writeObject(player);
                }
                try {
                    Thread.sleep(3);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // Function that actively receives data from server
    public void listenToServer() {
        boolean waitOnConnect = true;
        while (true) {
            try {
                // Handles null pointers, client tried to move other players with no information
                // gotten from MovableBG
                if (waitOnConnect) {
                    try {
                        Thread.sleep(1000);
                        waitOnConnect = false;
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                // Read data
                Object data = ois.readObject();

                // If its player data
                if (data instanceof Player) {
                    Player player = (Player) data;
                   
                    // And it's the players own ID
                    if (player.getPlayerID() == playerID) {
                        // Then assign the player to the HashMap
                        playerList.put(playerID, crewmateMaster);
                        playerObjList.put(playerID, player);// adding to an object list so color/name
                        // can be extracted

                    }
                    // If it's not the players ID
                    else {

                        // And the other player is not in the HashMap
                        if (!playerList.containsKey(player.getPlayerID())) {
                            // Create a new Crewmate, and assign player to HashMap
                            CrewmateRacer newPlayer = new CrewmateRacer(false, player.getplayerColor());
                            playerList.put(player.getPlayerID(), newPlayer);
                            playerObjList.put(player.getPlayerID(), player);// adding to an object list so color/name
                                                                            // can be extracted
                            voteOptions.add(player.getPlayerName());
                            voteOptions.sort(null);
                            Platform.runLater(() -> movableBottom.getChildren().add(newPlayer));
                        }
                        // Thread that handles only moving the player sprites based on their position
                        new Thread(() -> {
                            int posX = player.getPlayerPosX() + movableRGB.getPosX() - 20;
                            int posY = player.getPlayerPosY() + movableRGB.getPosY() - 65;

                            synchronized (playerList) {
                                Platform.runLater(() -> playerList.get(player.getPlayerID()).model.relocate(posX, posY));
                            }

                        }).start();

                        // System.out.println("X: " + player.getPlayerPosX() + " Y: " +
                        // player.getPlayerPosY());
                    }
                }

                // Receive playerID that server assigned
                if (data instanceof Integer) {
                    playerID = (Integer) data;
                    System.out.println("My player ID: " + playerID);
                }

                if (data instanceof Chat) {//add chat messages
                    taChatRoom.appendText(((Chat) data).toString());

                }
                if (data instanceof Vote) {//add votes
                    voteTally.add((Vote) data);
                    System.out.println(((Vote) data).voteValue);
                }

                // Receive different kinds of String dataa
                if (data instanceof String) {
                    String string = (String) data;

                    // See what type of data it is
                    String[] dataType = string.split(":");

                    // If it is task data
                    if (dataType[0].equals("TASKS")) {

                        // Split to see which tasks have been received
                        String[] tasks = dataType[1].split(",");
                        System.out.print("My tasks are:");

                        // For each task given
                        for (String task : tasks) {
                            System.out.print(" " + task);

                            // Create it and add it to the task list
                            Task newTask = new Task(task);
                            taskList.add(newTask);
                        }
                        tasksGotten = true;
                    }
                    // If it is role data
                    if (dataType[0].equals("ROLE")) {
                        // Set the master crewmates role
                        playerRole = dataType[1];
                        crewmateMaster.setRole(playerRole);
                        
                        // And then start talking to the server
                        new Thread(() -> {
                            talkToServer();
                        }).start();
                    }
                }

            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (EOFException e) {

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // Crewmate Class
    class CrewmateRacer extends Pane {

        // Crewmate Attributes
        private ImageView model = null;
        private ImageView[] modelList = null;
        private boolean isMaster;
        private int modelFrame = 0;
        private int counter = 0;
        private String role;
        private String playerColor;

        public CrewmateRacer(boolean isMaster, String color) {
            this.playerColor = color;
            String mainSprite = "playervec_" + playerColor + ".png";
            String leftSprite = "playerLeftfootvec_" + playerColor + ".png";
            String rightSprite = "playerRightfootvec_" + playerColor + ".png";

            this.isMaster = isMaster;
            if (isMaster) {

                this.modelList = new ImageView[] {
                        new ImageView(mainSprite),
                        new ImageView(leftSprite),
                        new ImageView(mainSprite),
                        new ImageView(rightSprite)
                };
                this.model = modelList[modelFrame];
            } else {
                this.modelList = new ImageView[] {
                        new ImageView(mainSprite),
                        new ImageView(leftSprite),
                        new ImageView(mainSprite),
                        new ImageView(rightSprite)
                };
                this.model = modelList[modelFrame];
            }

            this.getChildren().add(model);
        }

        // Function for updating crewmates
        public void update() {
            if (isMaster) {

                // Call movement animation
                movementAnimation();
                counter++;

                // Make crewmate always be in the middle of the screen
                model.setX(scene.getWidth() / 2 - model.getImage().getWidth() / 2); // Responsive when resizing window
                model.setY(scene.getHeight() / 2 - model.getImage().getHeight() / 2);

                // Flip character image so it's facing the direction it is heading
                if (left)
                    model.setScaleX(1);
                if (right)
                    model.setScaleX(-1);
            } else {
                // movementAnimation();
                // counter++;

            }
            if (counter > 8)
                counter = 0;
        }

        // Function for animating the character sprite
        public void movementAnimation() {
            if (left || right || up || down) {
                if (counter % 7 == 0) {
                    modelFrame = (modelFrame + 1) % modelList.length;
                    model = modelList[modelFrame];
                    this.getChildren().set(0, model);
                }
            }
        }

        public void racerMovement() {
            if (counter % 7 == 0) {
                modelFrame = (modelFrame + 1) % modelList.length;
                model = modelList[modelFrame];
                this.getChildren().set(0, model);
            }

        }

        public void setRole(String role) {
            this.role = role;
        }

        public String getRole() {
            return this.role;
        }
    }

    /**
     * Event blocks
     * for voting
     * - if isMaster between Y-650 to 450 X-2200 to 1900
     */

    class MovableBackground extends Pane {

        // Map attributes
        private int posX = -1500, posY = -200, playerPosX, playerPosY, speed = 5;
        boolean canGoUp = true;
        boolean canGoDown = true;
        boolean canGoLeft = true;
        boolean canGoRight = true;
        private ImageView mapLayer = null;

        public MovableBackground(String path) {
            mapLayer = new ImageView(path);
            this.getChildren().add(mapLayer);

        }

        // Function for moving map, updating everything needed
        public void update() {

            // Player Position
            playerPosX = -posX + (int) (scene.getWidth() / 2); // Calculating the fact that our player is now in the
                                                               // middle of the screen
            playerPosY = -posY + (int) (scene.getHeight() / 2 + 30); // Taking into account that collision happens at
                                                                     // players feet

            // For testing position
            // System.out.println(playerPosX + " " + playerPosY);

            // Movement / Collision

            // Get colors in radius at crewmates feet
            Color collisionCheckUp = pr.getColor(playerPosX, playerPosY - 10);
            Color collisionCheckDown = pr.getColor(playerPosX, playerPosY + 10);
            Color collisionCheckLeft = pr.getColor(playerPosX - 10, playerPosY);
            Color collisionCheckRight = pr.getColor(playerPosX + 10, playerPosY);

            // Restrict movement based on collision checkers
            if (collisionCheckUp.getRed() > 0.3 && collisionCheckUp.getGreen() < 0.3) {
                canGoUp = false;
            } else canGoUp = true;

            if (collisionCheckDown.getRed() > 0.3 && collisionCheckDown.getGreen() < 0.3) {
                canGoDown = false;
            } else canGoDown = true;

            if (collisionCheckLeft.getRed() > 0.3 && collisionCheckLeft.getGreen() < 0.3) {
                canGoLeft = false;
            } else canGoLeft = true;

            if (collisionCheckRight.getRed() > 0.3 && collisionCheckRight.getGreen() < 0.3) {
                canGoRight = false;
            } else canGoRight = true;

            // If movement allowed, then move player
            if (canGoUp && up) {
                posY += speed;
            }
            if (canGoDown && down) {
                posY -= speed;
            }
            if (canGoLeft && left) {
                posX += speed;
            }
            if (canGoRight && right) {
                posX -= speed;
            }

            mapLayer.relocate(posX, posY);

            if (tasksGotten) {
                for (Task task : taskList) {
                    ImageView taskImage = task.getTaskImage();
                    if (!root.getChildren().contains(taskImage)) {
                        root.getChildren().add(taskImage);
                        taskImage.setVisible(false);
                    }

                    if (task.getTaskType().equals("TaskVote")) {
                        if (playerPosX >= 1900 && playerPosX <= 2200 && playerPosY >= 450 && playerPosY <= 650) {
                            taskImage.setVisible(true);
                        } else {
                            taskImage.setVisible(false);
                        }
                    }
                    if (task.getTaskType().equals("TaskHelm")) {
                        if (playerPosX >= 1380 && playerPosX <= 1580 && playerPosY >= 420 && playerPosY <= 640) {
                            taskImage.setVisible(true);
                        } else {
                            taskImage.setVisible(false);
                        }
                    }
                    if (task.getTaskType().equals("TaskMast")) {
                        if (playerPosX >= 2320 && playerPosX <= 2545 && playerPosY >= 450 && playerPosY <= 650) {
                            taskImage.setVisible(true);
                        } else {
                            taskImage.setVisible(false);
                        }
                    }
                    if (task.getTaskType().equals("TaskNav")) {
                        if (playerPosX >= 750 && playerPosX <= 1130 && playerPosY >= 680 && playerPosY <= 975) {
                            taskImage.setVisible(true);
                        } else {
                            taskImage.setVisible(false);
                        }
                    }
                    if (task.getTaskType().equals("TaskSickBay")) {
                        if (playerPosX >= 1325 && playerPosX <= 1725 && playerPosY >= 850 && playerPosY <= 1075) {
                            taskImage.setVisible(true);
                        } else {
                            taskImage.setVisible(false);
                        }
                    }
                    if (task.getTaskType().equals("TaskQuarters")) {
                        if (playerPosX >= 1025 && playerPosX <= 1480 && playerPosY >= 1255 && playerPosY <= 1565) {
                            taskImage.setVisible(true);
                        } else {
                            taskImage.setVisible(false);
                        }
                    }
                    if (task.getTaskType().equals("TaskStorage")) {
                        if (playerPosX >= 1540 && playerPosX <= 2110 && playerPosY >= 1625 && playerPosY <= 1890) {
                            taskImage.setVisible(true);
                        } else {
                            taskImage.setVisible(false);
                        }
                    }
                    if (task.getTaskType().equals("TaskPump")) {
                        if (playerPosX >= 2595 && playerPosX <= 3095 && playerPosY >= 1370 && playerPosY <= 1600) {
                            taskImage.setVisible(true);
                        } else {
                            taskImage.setVisible(false);
                        }
                    }
                    if (task.getTaskType().equals("TaskCannons")) {
                        if (playerPosX >= 3005 && playerPosX <= 3395 && playerPosY >= 610 && playerPosY <= 730) {
                            taskImage.setVisible(true);
                        } else {
                            taskImage.setVisible(false);
                        }
                    }
                }
            }
        }

        public int getPosX() {
            return posX;
        }

        public int getPosY() {
            return posY;
        }

        public int getPlayerPosX() {
            return playerPosX;
        }

        public int getPlayerPosY() {
            return playerPosY;
        }
    }

    class Task extends Pane {
        private String taskType;
        private ImageView taskImage;

        public Task(String taskType) {
            this.taskType = taskType;
            taskImage = new ImageView(taskType + ".png");
        }

        public String getTaskType() {
            return taskType;
        }

        public ImageView getTaskImage() {
            return taskImage;
        }

        /**
         * Event blocks - tasks are generally named (i'll figure out specifics in coming
         * days)
         * Voting block
         * - Y (650 - 450), X (2200 - 1900)
         */
        /*
         * Steer ship
         * - X (1380 - 1580), Y (420 - 640)
         */
        /*
         * Hoist Mast
         * - X (2320 - 2545), Y (450 - 650)
         */
        /*
         * Read Map/plot course
         * - X (750 - 1130), Y (680 - 975)
         */
        /*
         * Sick Bay
         * - X (1325 - 1725), Y (850 - 1075)
         */
        /*
         * Crew Dorms
         * - X (1025 - 1480), Y (1255 - 1565)
         */
        /*
         * Storage
         * - X (1540 - 2110), Y (1625 - 1890)
         */
        /*
         * Mess Hall/Cleaning task
         * - X (1685 - 1960), Y (1275 - 1560)
         */
        /*
         * Pump room
         * - X (2595 - 3095), Y (1370 - 1600)
         */
        /*
         * Ammunition room
         * - X (3080, 3580), Y (875, 1110)
         */
        /*
         * Cannons
         * - X (3005 - 3395), Y (610 - 730)
         */

    }

    public static void main(String[] args) {
        launch(args);
    }
}
