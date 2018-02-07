package sample;


import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

/** Press number window
 * This is a generic alert box , will keep it as simple and generic for reuse
 * Maybe with time will improve it
 */

public class PressNumWindow {

    public static String display(String title, String message){
        Stage window = new Stage();

        // Blocks any other action until the window(stage) is done
        window.initModality(Modality.APPLICATION_MODAL);
        window.setTitle(title);

        // Label message
        Label label1 = new Label();
        label1.setText(message);
        // Client input about the press number
        TextField pressNum = new TextField();
        pressNum.setPromptText("Press Number");
        pressNum.setAlignment(Pos.CENTER);

        Button acceptBtn = new Button("OK");
        acceptBtn.setOnAction(e -> {
            if(!pressNum.getText().isEmpty()) {
                window.close();
            } else {
                AlertWindow.display("Error", "Please enter press number");
            }
                }
        );

        Button exitBtn = new Button("Cancel");
        exitBtn.setOnAction(e -> window.close());

        VBox layout = new VBox(30);
        layout.getChildren().addAll(label1, pressNum, acceptBtn);
        layout.setAlignment(Pos.CENTER);

        Scene scene = new Scene(layout, 300,250); // Width & height must be at least like the minWidth & minHeight
        window.setScene(scene);
        window.setMinWidth(250);
        window.setMinHeight(200);
        window.setOnCloseRequest(e -> {
            // Takes the close button of the window and cancel it -> lead it to the button
            e.consume();
            window.close();
        });
        window.showAndWait(); // Freeze the reading of code which comes after this line until the window is close or hidden

        return pressNum.getText();
    }

}
