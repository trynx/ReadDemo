package sample;


import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

/** AlertBox
 * This is a generic alert box , will keep it as simple and generic for reuse
 * Maybe with time will improve it
 */

public class AlertWindow {

    public static void display(String title, String message){
        Stage window = new Stage();

        // Blocks any other action until the window(stage) is done
        window.initModality(Modality.APPLICATION_MODAL);
        window.setTitle(title);


        Label label1 = new Label();
        label1.setText(message);

        Button closeBtn = new Button("OK");
        closeBtn.setOnAction(e -> window.close());

        VBox layout = new VBox(30);
        layout.getChildren().addAll(label1, closeBtn);
        layout.setAlignment(Pos.CENTER);

        Scene scene = new Scene(layout, 200,150); // Width & must be at least like the minWidth & minHeight
        window.setScene(scene);
        window.showAndWait(); // Freeze the reading of code which comes after this line until the window is close or hidden




    }
}
