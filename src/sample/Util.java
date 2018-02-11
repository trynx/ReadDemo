package sample;

import javafx.scene.control.TreeItem;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

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

    void createFolder(String folderName) throws IOException {
        File file = new File("..\\" + folderName); // First create the folder
        file.mkdir();

        copyTodo(folderName); // Takes the list of steps to be done and copy in the folder

        SystabIO systabIO = new SystabIO();
        File file2 = new File(file.getPath() + systabIO.getFileListIoFresh());// TODO - Maybe don't need?
    }

    private void copyTodo(String pressNum) throws IOException { // TODO - Create the table depend on the start of the folder name (ex : 471 = 7900)
        // What to copy todoList / steps
        // Check which type of machine this is (directory from pressNum)
        final String [] machineNums = {"47100", "48300"};
        HashMap<String,Integer> machineMap =  new HashMap<>();
        machineMap.put("47100",7900);
        machineMap.put("48300",6900);

        for (String num : machineNums){ // Loop until the right machine is found
            if(pressNum.contains(num)){ // Check which type of machine list is needed
                String todoFolder = "Todo" + machineMap.get(pressNum);
                File srcTodoFile = new File("..\\UtilFiles\\" + todoFolder);
                File destTodoFile = new File("..\\" + todoFolder);

                if(!destTodoFile.exists()) copyFolder(srcTodoFile, destTodoFile); // Only copy when the folder if it  isn't there (to not overwrite)



                break;
            }
        }
    }


    private static void copyFile(File source, File dest) throws IOException { // TODO - Check to do it better with buffered writer / reader
//        InputStream is = null;
//        OutputStream os = null;
        try (InputStream is = new FileInputStream(source) ; OutputStream os = new FileOutputStream(dest)) {

            byte[] buffer = new byte[1024];
            int length;
            while ((length = is.read(buffer)) > 0) {
                os.write(buffer, 0, length);
            }
        }
    }

    private static void copyFolder(File src, File dest) throws IOException{ // TODO - do some testing with real files and see how to improve
        if(src.isDirectory()){

            //if directory not exists, create it
            if(!dest.exists()){
                dest.mkdir();

            }

            //list all the directory contents
            String files[] = src.list();

            for (String file : files) {
                //construct the src and dest file structure
                File srcFile = new File(src, file);
                File destFile = new File(dest, file);
                //recursive copy
                copyFolder(srcFile,destFile);
            }

        }else{
            //if file, then copy it
            //Use bytes stream to support all file types
            InputStream in = new FileInputStream(src);
            OutputStream out = new FileOutputStream(dest);

            byte[] buffer = new byte[1024];

            int length;
            //copy the file content in bytes
            while ((length = in.read(buffer)) > 0){
                out.write(buffer, 0, length);
            }

            in.close();
            out.close();
        }
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
