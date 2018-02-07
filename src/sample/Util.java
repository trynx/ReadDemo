package sample;

import javafx.scene.control.TreeItem;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

class Util {

    private final static int NUM_THREAD = 5; // Runtime.getRuntime().availableProcessors()

    int getNumThread(){
        return NUM_THREAD;
    }

    // Does a validation of an array of string
    boolean validationNotNull(String [] validate){

        // Loop (for each) the array
        for(String v : validate){
            if(v == null) return false; // if found a null, return false
        }

        return true; // No null found and the validation passed
    }

    void createFolder(String folderName){ // TODO - Create the tablet depend on the start of the folder name (ex : 471 = 7900)
        File file = new File("..\\" + folderName); // First create the folder
        file.mkdir();

        SystabIO systabIO = new SystabIO();
        File file2 = new File(file.getPath() + systabIO.getFileListIoFresh());// TODO - Maybe don't need?
    }

    // Write to a new file - file name depend on the press number
    // TODO - In the future to make the filename depend on the user input + type
    void writeFile(String inputText,String folderName, String fileName){

        BufferedWriter bw = null;

        File textFile = new File("..\\" + folderName + "\\" + fileName +".txt"); // Second create the file where the data will be saved
        try {

            String timeStamp = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss").format(new Date());

            bw = new BufferedWriter(new FileWriter(textFile,true));
            // Write all the input text
            bw.write(timeStamp + " : " +inputText);
            bw.newLine();
            bw.flush();


        }catch (IOException e){
            e.printStackTrace();
            System.out.println("IO Exception - File");
        } finally {
            // Close the buffered writer when it's done writing
            if(bw != null ){
                try {
                    bw.close();
                } catch (IOException e){
                    System.out.println("Buffered Writer didn't close");
                }
            }
        }
    }

    // Create branches
    TreeItem<String> makeBranch(String title, TreeItem<String> parent) {
        TreeItem<String> item = new TreeItem<>(title);
        item.setExpanded(true);
        parent.getChildren().add(item);

        return item;
    }




}
