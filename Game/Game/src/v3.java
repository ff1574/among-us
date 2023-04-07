import javafx.animation.AnimationTimer;
import javafx.application.Application;
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

public class v3 extends Application {
    
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
    CrewmateRacer crewmateMaster = null;

    //Movable Background
    MovableBackground movableBottom = null;
    MovableBackground movableTop = null;
    MovableBackground movableRGB = null;

    //Update Timer
    AnimationTimer updateTimer = null;
    int counter = 0;
    
    //Player Controls
    boolean up = false, down = false, right = false, left = false;

    //Collision Detection
    PixelReader pr = null;
    Image rgbMap;

    @Override
    public void start(Stage stage) throws Exception {
        this.stage = stage;
        stage.setTitle("AmongUs - Best Team");

        //When closing with X, disconnect from server and stop app
        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                System.exit(0);
            }
        });

        root = new StackPane();
        initializeScene();
    }

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

            }
            this.getChildren().add(model);
        }

        //Function for updating crewmates
        public void update() {

            if(isMaster) {

                //Check background RGB for collision

                //Make crewmate always be in the middle of the screen
                model.setX(scene.getWidth() / 2 - model.getImage().getWidth() / 2);     //Responsive when resizing window
                model.setY(scene.getHeight() / 2 - model.getImage().getHeight() / 2);
            }
        }
    }

    class MovableBackground extends Pane {

        //Map attributes
        private int posX = -500, posY = -500, playerPosX, playerPosY, collisionPosX, collisionPosY, speed = 5;
        boolean slide = false;
        private ImageView mapLayer = null;

        public MovableBackground(String path) {
            mapLayer = new ImageView(path);
            this.getChildren().add(mapLayer);
        }

        //Function for moving map, updating everything needed
        public void update() {

            // Player Position
            playerPosX = -posX + (int)(scene.getWidth() / 2); //Calculating the fact that our player is now in the middle of the screen
            playerPosY = -posY + (int)(scene.getHeight() / 2 + 30); //Taking into account that collision happens at players feet

            // For testing position
            // System.out.println(playerPosX + " " + playerPosY); 

            // Movement / Collision

            // Calculate collision position based on player position and direction held
            if (up) {
                collisionPosX = playerPosX;
                collisionPosY = playerPosY - 10;
            }
            if (down) {
                collisionPosX = playerPosX;
                collisionPosY = playerPosY + 10;
            }
            if (left) {
                collisionPosX = playerPosX - 10;
                collisionPosY = playerPosY;
            }
            if (right) {
                collisionPosX = playerPosX + 10;
                collisionPosY = playerPosY;
            }

            // Get color at crewmates feet
            Color color = pr.getColor(collisionPosX, collisionPosY);

            // System.out.printf("Red: %.0f Green: %.0f Blue: %.0f\n", color.getRed(), color.getGreen(), color.getBlue()); //For testing RGB collision

            if(color.getRed() > 0.5 && color.getGreen() < 0.5){

                // Check if player is holding two directions down, slide him accordingly

                

            } else {
                if(up)      posY += speed;
                if(down)    posY -= speed;
                if(left)    posX += speed;
                if(right)   posX -= speed;
            }

            mapLayer.relocate(posX, posY);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
