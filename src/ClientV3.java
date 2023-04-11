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
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class ClientV3 extends Application {

    //Client Attributes
    private static final String SERVER_IP = "127.0.0.1";
    private static final int SERVER_PORT = 12345;
    private Socket socket = null;

    //Communication Attributes
    private ObjectOutputStream oos = null;
    private ObjectInputStream ois = null;
    
    //GUI Attributes
    private Stage stage;
    private Scene scene;
    private StackPane root;

    //Graphics
    private final static String CREWMATE_MASTER = "playervec.png";
    private final static String MAP_BOTTOM = "mapFinalBottom.png";
    private final static String MAP_TOP = "mapFinalTop.png";
    private final static String MAP_RGB = "mapRGY.png";

    //Crewmates
    private int playerID;
    private int playerPosX, playerPosY;
    private Player masterPlayer;
    private CrewmateRacer crewmateMaster = null;
    private HashMap<Integer, CrewmateRacer> playerList = new HashMap<>();

    //Movable Background
    private MovableBackground movableBottom = null;
    private MovableBackground movableTop = null;
    private MovableBackground movableRGB = null;

    //Update Timer
    private AnimationTimer updateTimer = null;
    
    //Player Controls
    private boolean up = false, down = false, right = false, left = false;

    //Collision Detection
    private PixelReader pr = null;
    private Image rgbMap;

    @Override
    public void start(Stage stage) throws Exception {
        this.stage = stage;
        stage.setTitle("AmongUs - Best Team");

        //When closing with X, disconnect from server and stop app
        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                if(socket != null) {
                    System.exit(0);     /************NOT IMPLEMENTED ***************** */
                }
                System.exit(0);
            }
        });
        root = new StackPane();

        connectToServer();
        initializeScene();
    }

    //Function for connecting client to server
    public void connectToServer() {
        try {
            socket = new Socket(SERVER_IP, SERVER_PORT);
            System.out.println("Connected.");

            //Initialize communication
            oos = new ObjectOutputStream(socket.getOutputStream());
            ois = new ObjectInputStream(socket.getInputStream());

            new Thread(() -> {listenToServer();}).start();

        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //Function for initializing the whole game
    public void initializeScene(){

        //Create Player Character
        crewmateMaster = new CrewmateRacer(true);

        //Create Map
        movableRGB = new MovableBackground(MAP_RGB);
        movableBottom = new MovableBackground(MAP_BOTTOM);
        movableTop = new MovableBackground(MAP_TOP);

        //Collision Detection
        rgbMap = new Image(MAP_RGB);
        pr = rgbMap.getPixelReader();

        //Add components to root
        this.root.getChildren().addAll(movableRGB, movableBottom, crewmateMaster,movableTop);

        //Initialize window
        scene = new Scene(root, 1200, 800);
        stage.setScene(scene);
        stage.show();

        //Keyboard Control
        scene.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
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

        //Timer updates everything
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

    //Function that actively sends data to server
    public void talkToServer() {
        masterPlayer = new Player(playerID, playerPosX, playerPosY);
        while(true) {
            masterPlayer.setPlayerPosX(playerPosX);
            masterPlayer.setPlayerPosY(playerPosY);
            try {
                oos.writeObject(masterPlayer);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //Function that actively receives data from server
    public void listenToServer() {
        while(true) {
            try {
                Object data = ois.readObject();

                if(data instanceof Player) {
                    Player player = (Player) data;

                    if(player.getPlayerID() == playerID) {
                        playerList.put(playerID, crewmateMaster);
                    }
                    else {
                        if(!playerList.containsKey(player.getPlayerID())) {
                            CrewmateRacer newPlayer = new CrewmateRacer(false);
                            playerList.put(player.getPlayerID(), newPlayer);
                            Platform.runLater(() -> root.getChildren().add(newPlayer));
                        }
                    }
                }

                //Receive playerID that server assigned
                if(data instanceof Integer) {
                    playerID = (Integer) data;
                    System.out.println("My player ID: " + playerID);
                    new Thread(() -> {talkToServer();}).start();
                }
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //Crewmate Class
    class CrewmateRacer extends Pane {

        //Crewmate Attributes
        private ImageView model = null;
        private boolean isMaster;

        public CrewmateRacer(boolean isMaster){
            this.isMaster = isMaster;

            //If crewmate is player, give him desired model and place him on 400,250 coordinates
            if(this.isMaster) {
                model = new ImageView(CREWMATE_MASTER);
            } 
            //If crewmate is something else, do something else
            else {
                model = new ImageView(CREWMATE_MASTER);
            }
            this.getChildren().add(model);
        }

        //Function for updating crewmates
        public void update() {

            if(isMaster) {

                // Check background RGB for collision

                // Make crewmate always be in the middle of the screen
                model.setX(scene.getWidth() / 2 - model.getImage().getWidth() / 2);     //Responsive when resizing window
                model.setY(scene.getHeight() / 2 - model.getImage().getHeight() / 2);

                // Flip character image so it's facing the direction it is heading
                if(left) model.setScaleX(1);
                if(right) model.setScaleX(-1);
            }
        }
    }

    class MovableBackground extends Pane {

        //Map attributes
        private int posX = -1500, posY = -200, speed = 5;
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
            playerPosX = -posX + (int)(scene.getWidth() / 2); //Calculating the fact that our player is now in the middle of the screen
            playerPosY = -posY + (int)(scene.getHeight() / 2 + 30); //Taking into account that collision happens at players feet

            // For testing position
            // System.out.println(playerPosX + " " + playerPosY); 

            // Movement / Collision

            // Get colors in radius at crewmates feet
            Color collisionCheckUp = pr.getColor(playerPosX, playerPosY-10);
            Color collisionCheckDown = pr.getColor(playerPosX, playerPosY+10);
            Color collisionCheckLeft = pr.getColor(playerPosX-10, playerPosY);
            Color collisionCheckRight = pr.getColor(playerPosX+10, playerPosY);

            // For testing RGB collision
            // System.out.printf("Red: %.0f Green: %.0f Blue: %.0f\n", collisionCheckUp.getRed(), collisionCheckUp.getGreen(), collisionCheckUp.getBlue()); 
            // System.out.printf("Up:    %f\nDown:  %f\nLeft:  %f\nRight: %f\n", collisionCheckUp.getRed(), collisionCheckDown.getRed(), collisionCheckLeft.getRed(), collisionCheckRight.getRed());

            // Restrict movement based on collision checkers
            if(collisionCheckUp.getRed() > 0.3 && collisionCheckUp.getGreen() < 0.3){

                canGoUp = false;

            } else canGoUp = true;

            if(collisionCheckDown.getRed() > 0.3 && collisionCheckDown.getGreen() < 0.3){

                canGoDown = false;

            } else canGoDown = true;

            if(collisionCheckLeft.getRed() > 0.3 && collisionCheckLeft.getGreen() < 0.3){

                canGoLeft = false;

            } else canGoLeft = true;

            if(collisionCheckRight.getRed() > 0.3 && collisionCheckRight.getGreen() < 0.3){

                canGoRight = false;

            } else canGoRight = true;

            //If movement allowed, then move playerhk
            if(canGoUp && up) {
                posY += speed;
            }
            if(canGoDown && down) {
                posY -= speed;
            }
            if(canGoLeft && left) {
                posX += speed;
            }
            if(canGoRight && right) {
                posX -= speed;
            }

            mapLayer.relocate(posX, posY);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
