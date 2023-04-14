import java.io.*;
import java.net.*;
import java.util.HashMap;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
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

    // GUI Attributes
    private Stage stage;
    private Scene scene;
    private AnchorPane root;

    // Graphics
    private final static String CREWMATE_MASTER = "playervec.png";
    private final static String CREWMATE_MASTER_LEFT = "playerLeftfootvec.png";
    private final static String CREWMATE_MASTER_RIGHT = "playerRightfootvec.png";
    private final static String MAP_BOTTOM = "mapFinalBottom.png";
    private final static String MAP_TOP = "mapFinalTop.png";
    private final static String MAP_RGB = "mapRGB.png";
    private final static String TASKEVENT_TEST = "TaskEvent.png";

    // Crewmates
    private int playerID;
    private CrewmateRacer crewmateMaster = null;
    private HashMap<Integer, CrewmateRacer> playerList = new HashMap<>();

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
    private Task genericTask;
    boolean taskArea;
    boolean taskControl;

    @Override
    public void start(Stage stage) throws Exception {
        this.stage = stage;
        stage.setTitle("AmongUs - Best Team");

        // When closing with X, disconnect from server and stop app
        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                if (socket != null) {
                    System.exit(0); /************ NOT IMPLEMENTED ***************** */
                }
                System.exit(0);
            }
        });
        root = new AnchorPane();

        connectToServer();
        initializeScene();
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

    // Function for initializing the whole game
    public void initializeScene() {

        // Create Player Character
        crewmateMaster = new CrewmateRacer(true);

        // Create Map
        movableRGB = new MovableBackground(MAP_RGB);
        movableBottom = new MovableBackground(MAP_BOTTOM);
        movableTop = new MovableBackground(MAP_TOP);

        // Create Tasks
        genericTask = new Task();

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

                // Doing tasks
                if(taskArea) {
                    switch (event.getCode()) {
                        case SPACE:
                            taskControl = true;
                        break;
                        default:
                        break;
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
                genericTask.update();
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
            Player player = new Player(playerID, playerPosX, playerPosY);
            try {
                if (oos != null) {
                    oos.writeObject(player);
                }
                try {
                    Thread.sleep(5);
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
                // Read data
                Object data = ois.readObject();

                // If its player data
                if (data instanceof Player) {
                    Player player = (Player) data;

                    // And it's the players own ID
                    if (player.getPlayerID() == playerID) {
                        // Then assign the player to the HashMap
                        playerList.put(playerID, crewmateMaster);
                    }
                    // If it's not the players ID
                    else {

                        // And the other player is not in the HashMap
                        if (!playerList.containsKey(player.getPlayerID())) {
                            // Create a new Crewmate, and assign player to HashMap
                            CrewmateRacer newPlayer = new CrewmateRacer(false);
                            playerList.put(player.getPlayerID(), newPlayer);
                            Platform.runLater(() -> movableBottom.getChildren().add(newPlayer));
                        }
                        if(waitOnConnect) {
                            try {
                                Thread.sleep(500);
                                waitOnConnect = false;
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        Thread t = new Thread(() -> {
                            synchronized(playerList){

                            playerList.get(player.getPlayerID()).model.relocate(
                                    player.getPlayerPosX() + movableRGB.getPosX() - 20,/*I get a null pointer exception here, no idea why */
                                    player.getPlayerPosY() + movableRGB.getPosY() - 70);
                                }
                        });
                        /*
                         * for some reason this offset
                         * seems effective, and putting
                         * it in a thread helps it update quickly/effectively.
                         */
                        t.start();

                        //System.out.println("X: " + player.getPlayerPosX() + " Y: " + player.getPlayerPosY());
                    }
                }

                // Receive playerID that server assigned
                if (data instanceof Integer) {
                    playerID = (Integer) data;
                    System.out.println("My player ID: " + playerID);
                    // And then start talking to the server
                    new Thread(() -> {
                        talkToServer();
                    }).start();
                }
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
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

        public CrewmateRacer(boolean isMaster) {
            this.isMaster = isMaster;

            // If crewmate is player, give him desired model and place him on 400,250
            // coordinates
            if (this.isMaster) {
                this.modelList = new ImageView[] {
                    new ImageView(CREWMATE_MASTER),
                    new ImageView(CREWMATE_MASTER_LEFT),
                    new ImageView(CREWMATE_MASTER),
                    new ImageView(CREWMATE_MASTER_RIGHT)
                };
                this.model = modelList[modelFrame];
            }
            // If crewmate is something else, do something else
            else {
                this.modelList = new ImageView[] {
                    new ImageView(CREWMATE_MASTER),
                    new ImageView(CREWMATE_MASTER_LEFT),
                    new ImageView(CREWMATE_MASTER),
                    new ImageView(CREWMATE_MASTER_RIGHT)
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
            }
            else {
               // movementAnimation();
               // counter++;

            }
            if(counter > 200000000) counter = 0;
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

            // For testing RGB collision
            // System.out.printf("Red: %.0f Green: %.0f Blue: %.0f\n",
            // collisionCheckUp.getRed(), collisionCheckUp.getGreen(),
            // collisionCheckUp.getBlue());
            // System.out.printf("Up: %f\nDown: %f\nLeft: %f\nRight: %f\n",
            // collisionCheckUp.getRed(), collisionCheckDown.getRed(),
            // collisionCheckLeft.getRed(), collisionCheckRight.getRed());

            // Restrict movement based on collision checkers
            if (collisionCheckUp.getRed() > 0.3 && collisionCheckUp.getGreen() < 0.3) {

                canGoUp = false;

            } else
                canGoUp = true;

            if (collisionCheckDown.getRed() > 0.3 && collisionCheckDown.getGreen() < 0.3) {

                canGoDown = false;

            } else
                canGoDown = true;

            if (collisionCheckLeft.getRed() > 0.3 && collisionCheckLeft.getGreen() < 0.3) {

                canGoLeft = false;

            } else
                canGoLeft = true;

            if (collisionCheckRight.getRed() > 0.3 && collisionCheckRight.getGreen() < 0.3) {

                canGoRight = false;

            } else
                canGoRight = true;

            // Noclip for testing
           /*  canGoRight = true;
            canGoDown = true;
            canGoLeft = true;
            canGoUp = true;*/

            // If movement allowed, then move playerhk
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

    class Task extends Pane{
        private ImageView taskEvent;

        public Task() {
            taskEvent = new ImageView(TASKEVENT_TEST);
        }

        public void update() {

            Color taskCheck = pr.getColor(movableRGB.getPlayerPosX(), movableRGB.getPlayerPosY());
            System.out.println(movableRGB.getPlayerPosX() + ", " + movableRGB.getPlayerPosY());

            if (taskCheck.getBlue() > 0.3 && taskCheck.getGreen() < 0.3) {
                if (!taskControl) {//checkng if the player completed task
                    if (!root.getChildren().contains(taskEvent)) {//seeing if the task exists in the first place
                        root.getChildren().add(taskEvent);//adding the task ImageView
                        taskArea = true;//allowing task player control
    
                    }
                } else {
                    //testing if player completes task
                    if (root.getChildren().contains(taskEvent)) {
                        root.getChildren().remove(taskEvent);
    
                    }
                }
    
            } else {
                //checking if out of task area
                if (root.getChildren().contains(taskEvent)) {
                    root.getChildren().remove(taskEvent);
    
                }
                //resetting task Controls
                taskControl = false;
                
    
            }
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}