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

    // GUI Attributes
    private Stage stage;
    private Scene scene;
    private StackPane root;

    // Graphics
    private final static String CREWMATE_MASTER = "playervec.png";
    private final static String CREWMATE_MASTER_LEFT = "playerLeftfootvec.png";
    private final static String CREWMATE_MASTER_RIGHT = "playerRightfootvec.png";

    private final static String TASKEVENT_TEST = "TaskEvent.png";

    private final static String MAP_BOTTOM = "mapFinalBottom.png";
    private final static String MAP_TOP = "mapFinalTop.png";
    private final static String MAP_RGB = "mapRGB.png";

    // Crewmates
    CrewmateRacer crewmateMaster = null;

    // Movable Background
    MovableBackground movableBottom = null;
    MovableBackground movableTop = null;
    MovableBackground movableRGB = null;
    Task task = null;

    // Update Timer
    AnimationTimer updateTimer = null;
    int counter = 0;

    // Player Controls
    boolean up = false, down = false, right = false, left = false;

    // Collision Detection
    PixelReader pr = null;
    Image rgbMap;

    @Override
    public void start(Stage stage) throws Exception {
        this.stage = stage;
        stage.setTitle("AmongUs - Best Team");

        // When closing with X, disconnect from server and stop app
        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                System.exit(0);
            }
        });

        root = new StackPane();
        initializeScene();
    }

    public void initializeScene() {

        // Create Player Character
        crewmateMaster = new CrewmateRacer(true);

        // Create Map
        movableRGB = new MovableBackground(MAP_RGB);
        movableBottom = new MovableBackground(MAP_BOTTOM);
        movableTop = new MovableBackground(MAP_TOP);

        // Collision Detection
        rgbMap = new Image(MAP_RGB);
        pr = rgbMap.getPixelReader();

        // New task object
        task = new Task(TASKEVENT_TEST);

        // Add components to root
        this.root.getChildren().addAll(movableRGB, movableBottom, crewmateMaster, movableTop,task);

        // Initialize window
        scene = new Scene(root, 1200, 800);
        stage.setScene(scene);
        stage.show();

        // Keyboard Control
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

    // Crewmate Class
    class CrewmateRacer extends Pane {

        // Crewmate Attributes
        private ImageView model = null;
        private boolean isMaster;
        private ImageView[] modelList = null;
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
                        new ImageView(CREWMATE_MASTER_RIGHT),

                };

                this.model = modelList[modelFrame];
            }
            // If crewmate is something else, do something else
            else {

            }
            this.getChildren().add(model);
        }

        public void movementAnimation() {
            if (left || right || up || down) {
                if (counter % 7 == 0) {
                    modelFrame = (modelFrame + 1) % modelList.length;
                    model = modelList[modelFrame];
                    this.getChildren().set(0, model);
                }

            }

        }

        // Function for updating crewmates
        public void update() {

            if (isMaster) {

                movementAnimation();

                counter++;

                // Check background RGB for collision

                // Make crewmate always be in the middle of the screen
                model.setX(scene.getWidth() / 2 - model.getImage().getWidth() / 2); // Responsive when resizing window
                model.setY(scene.getHeight() / 2 - model.getImage().getHeight() / 2);

                // Flip character image so it's facing the direction it is heading
                if (left)
                    model.setScaleX(1);
                if (right)
                    model.setScaleX(-1);
            }
        }
    }

    //starter for task events, requires an update method maybe? definitely requires some kind of Key Event Listener
    class Task extends Pane {
        public Task(String path) {
            this.path = path;
            this.taskImage = new ImageView(path);
            if (TaskEvent) {
                this.getChildren().add(taskImage);
                System.out.println("Task Time");
            } else {
                this.getChildren().remove(taskImage);
            }
        }
        
        boolean TaskEvent;
        String path;
        ImageView taskImage = null;

    }

    class MovableBackground extends Pane {

        // Map attributes
        private int posX = -500, posY = -500, playerPosX, playerPosY, speed = 5;
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

            Color TaskCheck = pr.getColor(playerPosX, playerPosY);

            // For testing RGB collision
           // System.out.printf("Red: %.0f Green: %.0f Blue: %.0f\n",
          //collisionCheckUp.getRed(), collisionCheckUp.getGreen(),
        //collisionCheckUp.getBlue());

        System.out.println(TaskCheck.getBlue());
            // System.out.printf("Up: %f\nDown: %f\nLeft: %f\nRight: %f\n",
            // collisionCheckUp.getRed(), collisionCheckDown.getRed(),
            // collisionCheckLeft.getRed(), collisionCheckRight.getRed());

            // Restrict movement based on collision checkers
            if (TaskCheck.getBlue() > 0.6) {
                task.TaskEvent = true;
                

            } else
                task.TaskEvent=false;

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
    }

    public static void main(String[] args) {
        launch(args);
    }
}
