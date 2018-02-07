package sample;

import javafx.collections.ListChangeListener;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.io.*;
import java.net.URL;
import java.util.ResourceBundle;

public class Controller implements Initializable {


    // Other Vars
    private String pressNum = ""; // TODO - KNOW WHICH MACHINE IT IS AND THEN USE THE FILE DEPEND ON THE NUMBER

    // Classes
    private SystabReg systabReg = new SystabReg();
    private SystabIO systabIO = new SystabIO();
    private Util util = new Util();

    // Systab Regular - Read/Write
    // Read
    public TextField topicReadReg;
    public TextField itemReadReg;
    public ComboBox savedDynReadReg;
    public ComboBox topicIndexReadReg;
    public ComboBox itemIndexReadReg;
    public TextArea textAreaReg;

    // Write
    public TextField topicWriteReg;
    public TextField itemWriteReg;
    public TextField valueReg;
    public ComboBox topicIndexWriteReg;
    public ComboBox itemIndexWriteReg;
    public ComboBox savedDynWriteReg;

    // Systab IO - Read/Write
    // Read
    public Button searchReadIO;
    public TextField topicReadIO;
    public TextArea textAreaIO;
    // Write
    public TextField topicWriteIO;
    public TextField valueIO;
    public Button searchWriteIO;


    // Read only vars tab
    public TextArea textAreaReg2;
    public TextArea textAreaIO2;

    // Control IO / Regular
    public TreeView treeViewReg;
    public TreeView<String> treeViewIO; // TODO - Continue with the treeview

    // Flow - IO
    public ToggleButton toggleIOFlow;
    public ToggleButton toggleIOFlowAuto;
    public TextArea textAreaIOFLow;

    // Tests
    public ComboBox comboBoxSteps;
    public ToggleButton toggleWrite;



    @Override // What to do when the program start
    public void initialize(URL location, ResourceBundle resources) {
        pressNum = PressNumWindow.display("Press Number", "Please insert press number");
        if (!pressNum.isEmpty()) { // TODO - Check how to stop the user if the press number is empty
            //Systab Regular - insert into combo box
            // Read
            savedDynReadReg.getItems().clear();
            savedDynReadReg.getItems().addAll("SAVED", "DYNAMIC", "DEFAULT");

            itemIndexReadReg.getItems().clear();
            itemIndexReadReg.getItems().addAll(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);

            topicIndexReadReg.getItems().clear();
            topicIndexReadReg.getItems().addAll(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
            // Write
            savedDynWriteReg.getItems().clear();
            savedDynWriteReg.getItems().addAll("SAVED", "DYNAMIC");

            topicIndexWriteReg.getItems().clear();
            topicIndexWriteReg.getItems().addAll(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);

            itemIndexWriteReg.getItems().clear();
            itemIndexWriteReg.getItems().addAll(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);


            // Steps - TEST
            comboBoxSteps.getItems().clear();
            comboBoxSteps.getItems().addAll(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);

            // Binds
            textAreaReg2.textProperty().bind(textAreaReg.textProperty());
            textAreaIO2.textProperty().bind(textAreaIO.textProperty());

            // Create folder for the press
            util.createFolder(pressNum);

            // TODO - Tree view - multiple search
            // Tree Control - START
            // Tree View construction
            // Start root
            TreeItem<String> root = new TreeItem<>();
            root.setExpanded(true); // First time it show , open be default

            TreeItem<String> main;

            main = util.makeBranch("Main", root);
            util.makeBranch("Yey", main);
            util.makeBranch("Da", main);
            util.makeBranch("Kio", main);
            util.makeBranch("Rori", main);


            // Create the tree
            treeViewIO.setRoot(root);
            treeViewIO.showRootProperty();

            treeViewIO.getSelectionModel().getSelectedItems().addListener(new ListChangeListener<TreeItem>() {
                @Override
                public void onChanged(Change<? extends TreeItem> change) {
                    // myTreeView.getSelectionModel().getSelectedItems() contains all the selected items
                }
            });
            // Tree Control - END

            // Save the folder name for Systabs to use
            systabIO.setSystabIOFolderName(pressNum);
            systabReg.setSystabRegFolderName(pressNum);

        }
    }

    // Read from systab regular
    public void printReg() {
        // Set all user input from the text fields / combo boxes
        // Systab Regular
        String topicReadReg = this.topicReadReg.getText().trim();
        String topicIndexReadReg = this.topicIndexReadReg.getValue().toString();
        String itemReadReg = this.itemReadReg.getText().trim();
        String itemIndexReadReg = this.itemIndexReadReg.getValue().toString();
        String SDComboSelectedReadReg = savedDynReadReg.getValue().toString();

        // ReadMethod - save data in String
        // Systab Regular
        String responseReg = systabReg.readSystabReg(topicReadReg, topicIndexReadReg, itemReadReg, itemIndexReadReg, SDComboSelectedReadReg, pressNum);
        if (toggleWrite.isSelected()) { // When in test mode to check some things
            responseReg = systabReg.readSystabRegTest(topicReadReg, pressNum); // TEST
        }

        textAreaReg.appendText("Systab Reg : " + responseReg + "\n");
    }

    // Write to systab regular
    public void writeReg() {
        // Set all user input from the text fields / combo boxes
        // Systab Regular
        String topicWriteReg = this.topicWriteReg.getText().trim();
        String topicIndexWriteReg = this.topicIndexWriteReg.getValue().toString();
        String itemWriteReg = this.itemWriteReg.getText().trim();
        String itemIndexWriteReg = this.itemIndexWriteReg.getValue().toString();
        String SDComboSelectedWriteReg = savedDynWriteReg.getValue().toString();
        String valueReg = this.valueReg.getText().trim();

        // ReadMethod - True = succeed , False = failed
        boolean responseReg = systabReg.writeSystabReg(topicWriteReg, topicIndexWriteReg, itemWriteReg, itemIndexWriteReg, valueReg, SDComboSelectedWriteReg);

        textAreaReg.appendText("Systab Write Reg : " + responseReg + "\n");

    }

    // Read from systab IO
    public void printIO(ActionEvent actionEvent) {
        // Systab IO
        String topicReadIO = this.topicReadIO.getText().trim();
        int topicIndexReadIO = 0;

        String responseIO = "";
        if (toggleWrite.isSelected()) { //When in test mode to check some things
            responseIO = systabIO.readSystabIOTest(topicReadIO); // Test
        } else {
            responseIO = systabIO.readSystabIO(topicReadIO, topicIndexReadIO, pressNum);
        }

        textAreaIO.appendText("Systab IO : " + responseIO + "\n");
    }

    // Write to systab IO
    public void writeIO(ActionEvent actionEvent) {
        // Systab IO
        String topicWriteIO = this.topicWriteIO.getText().trim();
        int topicIndexWriteIO = 0;
        String valueIo = this.valueIO.getText().trim();

        boolean responseIO = systabIO.writeSystabIO(topicWriteIO, topicIndexWriteIO, valueIo);

        textAreaIO.appendText("Systab Write IO : " + responseIO + "\n");
    }

    // Flow of IO data - all data , continues checking the steps until they are done or toggle is off
    public void startIOFlow() {// TODO - Maybe add a field that say how many steps
        // Anonymous class - Service works at different thread - ONLY STARTED FROM FX THREAD
        Service<String> autoToggleService = new Service<>() {

            @Override
            protected Task<String> createTask() {
                return new Task<>() {

                    @Override
                    protected String call() throws Exception {
//                        int test = 0; // TEST

                        textAreaIOFLow.appendText("Auto check start\n");

                        String result = "";
                        // Loops until the steps are done or toggle is off
                        while (toggleIOFlowAuto.isSelected() && !result.equalsIgnoreCase("complete")) {
                            result = systabIO.readAllSteps();
                            textAreaIOFLow.appendText(result);

//                            test++; // TEST
//                            System.out.println(test); // TEST

                        }
                        if(result.equalsIgnoreCase("complete")) {
                            textAreaIOFLow.appendText(" all steps  , congratulations! =] \n");
                        }

                        return result;
                    }
                };
            }
        };

        if(toggleIOFlowAuto.isSelected()){
                    autoToggleService.reset();
                    autoToggleService.start();
                } // Else for not toggle -> cancel service

        autoToggleService.setOnSucceeded(e -> { // When the Service done it
            textAreaIOFLow.appendText("Auto check stop\n");
        });

        autoToggleService.setOnRunning(e ->
                System.out.println("Start\n"));
    }






    public void startIOFlowToggle(){

        // Anonymous class - Service works at different thread - ONLY STARTED FROM FX THREAD
        Service<String> toggleService = new Service<>() {

            @Override
            protected Task<String> createTask() {
                return new Task<>(){

                    @Override
                    protected String call() throws Exception {
//                        int res = 0; // TEST

                        String resultUser = "";
                        // Loops until the client un-toggled the button
                        while(toggleIOFlow.isSelected()) {
                            resultUser = systabIO.readAll();// Read the process
                            if(resultUser.contains("IO Exception")) break;// When there is an exception it will stop the read(flow)
                            textAreaIOFLow.appendText(resultUser); // Show result to client

//                          resultUser = Integer.toString(++res); // TEST
//                          System.out.println(resultUser); // TEST
                        }
                        return resultUser;
                    }
                };
            }
        };
        if(toggleIOFlow.isSelected()) { // When the client toggle the button , service will start
            toggleService.reset();
            toggleService.start();
        }

        toggleService.setOnSucceeded(e -> {
            String result = toggleService.getValue();
            textAreaIOFLow.appendText(result + " out\n");
        });
    }

    // TODO - Continue here !! - RegFlowToggle
    public void startRegFlowToggle(){

        // Anonymous class - Service works at different thread - ONLY STARTED FROM FX THREAD
        Service<String> toggleService = new Service<>() {

            @Override
            protected Task<String> createTask() {
                return new Task<>(){

                    @Override
                    protected String call() throws Exception {
//                        int res = 0; // TEST

                        String resultUser = "";
                        // Loops until the client un-toggled the button
                        while(toggleIOFlow.isSelected()) {
                            resultUser = systabIO.readAll();// Read the process
                            if(resultUser.contains("IO Exception")) break;// When there is an exception it will stop the read(flow)
                            textAreaIOFLow.appendText(resultUser); // Show result to client

//                          resultUser = Integer.toString(++res); // TEST
//                          System.out.println(resultUser); // TEST
                        }
                        return resultUser;
                    }
                };
            }
        };
        if(toggleIOFlow.isSelected()) { // When the client toggle the button , service will start
            toggleService.reset();
            toggleService.start();
        }

        toggleService.setOnSucceeded(e -> {
            String result = toggleService.getValue();
            textAreaIOFLow.appendText(result + " out\n");
        });
    }
}
