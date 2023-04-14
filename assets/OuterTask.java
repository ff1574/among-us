import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.stage.PopupWindow.AnchorLocation;

/**
 * this exists as a Super Class for different tasks with different events
 */

public class OuterTask extends Pane {
    AnchorPane root;
    ImageView taskView;

    Scene scene;

    public OuterTask(AnchorPane root, String taskImage, Scene scene) {
        this.root = root;
        taskView = new ImageView(taskImage);
      
        
        this.scene = scene;

    }

    public void isActive() {

        root.getChildren().add(taskView);
        taskView.setScaleX(.25);
        taskView.setScaleY(.25);
        

      scene.setOnKeyPressed(new EventHandler<KeyEvent>() {

            @Override
            public void handle(KeyEvent event) {
                switch (event.getCode()) {
                    case T:
                        System.out.println("TASK COMPLETE");
                        isNotActive();
                }
            }

        });

    }

    public void isNotActive() {
        root.getChildren().remove(taskView);

    }

}
