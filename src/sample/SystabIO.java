package sample;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.*;


public class SystabIO {

    private final static String FILE_PATH_IO = "..\\Systab"; // Better at the folder of the app at - ReadDemo\runtime\bin\
    private final static String FILE_EXE_IO = "systabio.exe";

    private String folderName = "";

    private final static String UTIL_FOLDER = "UtilFiles\\";

    private final static String FILE_LIST_IO_FRESH = UTIL_FOLDER +"FilteredsystabIOTablet7900.txt";
    private final static String FILE_LIST_IO_CURR = "IOCurrent.txt";
    private final static String FILE_LIST_IO_TEMP = "IOTemp.txt";
    private final static String FILE_LIST_IO_LOG = "IOLog.txt";
    // TODO - Add the folder name (open array , append and save)
    private final static String [] FILE_LIST_IO_STEPS = {UTIL_FOLDER + /* TODO - IOStep1.. */ "Step1IO.txt", UTIL_FOLDER + "Step2IO.txt", UTIL_FOLDER + "Step3IO.txt", UTIL_FOLDER + "Step4IO.txt", UTIL_FOLDER + "Step5IO.txt"};

    private final static int NUM_STEPS = FILE_LIST_IO_STEPS.length;
    private static int numCurStep = 0;



    private Util util = new Util();


    /** All read methohds START **/
    /**
     * Read the whole systab IO in a specific parameter (topic)
     */
    String readSystabIO(String topicName, int topicIndex, String folderName) {
        // Order of parameter to read : GET, topicName, topicIndex, SYSTAB_SOURCE.MASTER
        // Validation
        String [] proParameters = {FILE_EXE_IO, "GET", topicName, Integer.toString(topicIndex), "SYSTAB_SOURCE.MASTER"};

        if (util.validationNotNull(proParameters)) { // Validate everything -> true = no null

            // Build the process
            ProcessBuilder pb = new ProcessBuilder(proParameters); // GET & SYSTAB_SOURCE.MASTER - needed to use readerIO

//            pb.directory(new File(FILE_PATH_IO)); Check different folder later
            try {
                Process p = pb.start();

                BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));


                StringBuilder lineText = new StringBuilder();
                String line = br.readLine();
                // Start reading in loop until there isn't any more
                while (line != null) {
                    lineText.append(line);
                    line = br.readLine();
                    if (!br.ready()) {
                        br.close(); // Maybe not ?
                        break;
                    }
                }

                String userInput = topicName + " " + topicIndex ;
                String machineInput = lineText.toString();

                util.writeFile(machineInput + " " + topicName, folderName, "SystabIO");
                return "User request : "  + userInput + "\n"
                        + "Answer : " + machineInput;


            }catch(IOException e){
                e.printStackTrace();
                System.out.println("Process IO Error");
            }
            return "No data";
        }
        return "Nothing";
    }

    String readSystabIOTest(String input){
        return input;
    }
    /** All read methohds END **/

    /** All write methohds START **/
    boolean writeSystabIO(String topicName, int topicIndex, String value){
        // Order of parameter to write : SET, topicName, topicIndex, SYSTAB_SOURCE.MASTER, value
        // Validation
        String [] proParameters = {FILE_EXE_IO,"SET", topicName, Integer.toString(topicIndex), "SYSTAB_SOURCE.MASTER", value}; // The order of parameters are important for Process Builder!!

        if (util.validationNotNull(proParameters)){ // Validate everything -> true = no null

            BufferedWriter bw = null; // Outside of try/catch scope so it can be used in finally scope
            // Build the process
            ProcessBuilder pb = new ProcessBuilder(proParameters);
            pb.directory(new File(FILE_PATH_IO)); // Directory of the FILE_EXE_REG
            try {
                Process p = pb.start();
                bw = new BufferedWriter(new OutputStreamWriter(p.getOutputStream()));

                // Verification vars - TEST
                String alive1 = "notAlive";
                String alive = "notAlive";

                if(p.isAlive()) alive1 = "alive"; // TEST

                bw.write(value);
                bw.flush();

                if(p.isAlive()) alive = "alive"; // TEST
                return true; // Succeed to write

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if(bw != null)
                    try {
                        bw.close(); // if it couldn't be close at the try block because an exception
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
            }
        }
        return false; // Failed to write
    }
    /** All write methohds END **/

    /** Loop through all IO **/
    String readAll(){
        // All files to write to / read from
        File listIOFresh = new File(FILE_LIST_IO_FRESH); // Only used when there still isn't a current file - template of list for IO
        File listIOCurr = new File(folderName + FILE_LIST_IO_CURR); // The file which the values and list are taken and compare with input from IO
        File listIOTemp = new File(folderName + FILE_LIST_IO_TEMP); // While runtime it will write the values in this file
        File listIOLog = new File(folderName + FILE_LIST_IO_LOG); // Write only the values which are different

        BufferedReader IOBr = null;
        BufferedReader fileBr = null;
        BufferedWriter fileBw = null;
        BufferedWriter fileBwLog = null;

        StringBuilder resultUser = new StringBuilder(); // Append the end result to later return it to controller and show to the user

        if (!listIOTemp.exists()) {
            try {
                fileBw = new BufferedWriter(new FileWriter(listIOTemp));
                fileBwLog = new BufferedWriter(new FileWriter(listIOLog, true));
                if (!listIOCurr.exists()) { // Change file if the current still doesn't exist
                    fileBr = new BufferedReader(new FileReader(listIOFresh)); // Maybe better way ?
                } else {
                    fileBr = new BufferedReader(new FileReader(listIOCurr)); // Add exception for only file not found ?
                }

                String fileData = fileBr.readLine(); // Start reading

                ExecutorService executor = Executors.newFixedThreadPool(util.getNumThread());

                CompletionService<String> completionService = new ExecutorCompletionService<>(executor); // Initialize  the completion service

                String topicValue = "";
                String topicName = "";
                StringBuilder results = new StringBuilder();
                int counterRequest = 0;
                HashMap<String, String> topicMap = new HashMap<>();

                // File data start
                while (fileData != null) {

                    String[] topicDetails = fileData.trim().split(" "); // [0] = topic name , [1] = value
                    topicName = topicDetails[0]; // Readability
                    if (topicDetails.length > 1) {
                        topicValue = topicDetails[1]; // Readability , verify only here if it really exist
                        topicMap.put(topicName,topicValue); //
                    } else {
                        topicMap.put(topicName,""); // No topic value
                    }

                    // Start futuretask completion service -> Start process to read the SystabIO
                    completionService.submit(new CallSystabIo(topicName)); // TEST
                    counterRequest++;
                    fileData = fileBr.readLine(); // Next line
                } // File data end

                // Take results from process
                for(int i = 0 ; i < counterRequest ; i++){ // Loop through the results -> counterRequests have the number of how many request
                    try {
                        Future<String> resultFuture = completionService.take(); // Wait for one completed task
                        results.append(resultFuture.get()); // Get a completed task , results = topicName + new Value

                        String [] endResultArr = results.toString().trim().split(" "); // [0] = topic name , [1] = value
                        String endResult = endResultArr[0] + " " + endResultArr[1];

                        fileBw.write(endResult);
                        fileBw.newLine();

                        // Writes data into IOLog file - only those which aren't equal to the value of inputIO(from press)
                        if (!topicMap.get(topicName).isEmpty() && !endResultArr[1].equalsIgnoreCase(topicValue)) { // If the map in that key is empty -> first search
                            String timeStamp = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss").format(new Date());

                            String logResult = timeStamp + " " + endResult; // The result which the log file will save & the one which will show the user

                            resultUser.append(logResult).append("\n"); // Append the log result to later show to the user

                            fileBwLog.write(logResult);
                            fileBwLog.newLine();
                        }

                    } catch (InterruptedException | ExecutionException e) {
                        e.printStackTrace();
                        return "CompletionService of SystabIO process Exception";
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("Process IO Error");
                return "Read error" + "\n"; // Don't change , this return stops the flow !

            } finally { // Maybe something better later on ?

                if (fileBw != null) try {
                    fileBw.close(); // Close to finish the stream and write everything
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (IOBr != null) try {
                    IOBr.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (fileBr != null) try {
                    fileBr.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (fileBwLog != null) try {
                    fileBwLog.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (listIOCurr.exists()) listIOCurr.delete();
            listIOTemp.renameTo(listIOCurr);

            return resultUser.toString();
        }
        return "No File";
    }



    String readAllSteps(){

        if(numCurStep >= NUM_STEPS) return "complete"; // Finished all the steps - stop the automation check
        // TODO - Make the steps file - IOStep n - read a number of files at the same time (depend on the n), through a loop it will read only those
        // TODO Continue - when n steps are done , will save (or delete) from a list and won't read them anymore
        // TODO Continue - it will first check that all the steps exists (1-n , no missing , if there is a missing step will throw a window alert of which step is missing)

        // All files to write to / read from
        File listIOCurr = new File(folderName + FILE_LIST_IO_CURR); // The file which the values and list are taken and compare with input from IO
        File listIOTemp = new File(folderName + FILE_LIST_IO_TEMP); // While runtime it will write the values in this file
        File listIOLog = new File(folderName + FILE_LIST_IO_LOG); // Write only the values which are different
        File listIOStep = new File(FILE_LIST_IO_STEPS[numCurStep]); // -1 -> index start at 0 , length start at 1

        BufferedReader IOBr = null;
        BufferedReader fileBr = null;
        BufferedWriter fileBw = null;
        BufferedWriter fileBwLog = null;

        StringBuilder resultUser = new StringBuilder(); // Append the end result to later return it to controller and show to the user
        boolean stepComplete;
        // Loop through all the steps
//            int test = 0 ; // TEST
        stepComplete = false;
        while (!stepComplete){
//                System.out.println(++test); // TEST
            if (!listIOTemp.exists()) {
                try {
                    fileBw = new BufferedWriter(new FileWriter(listIOTemp));
                    fileBwLog = new BufferedWriter(new FileWriter(listIOLog, true));

                    if (!listIOCurr.exists()) { // Change file if the current still doesn't exist
                        fileBr = new BufferedReader(new FileReader(listIOStep)); // Maybe better way ?
                    } else {
                        fileBr = new BufferedReader(new FileReader(listIOCurr)); // Add exception for only file not found ?
                    }

//                   int temp = 0; // TEST - dummy data
                    String fileData = fileBr.readLine(); // Start reading

                    while (fileData != null) {
//                            System.out.println(fileData); // TEST

                        String[] topicDetails = fileData.trim().split(" "); // [0] = topic name , [1] = value

                        // Order of parameter to read : GET, fileData, topicIndex, SYSTAB_SOURCE.MASTER
                        String[] proParameters = {FILE_EXE_IO, "GET", topicDetails[0], "0", "SYSTAB_SOURCE.MASTER"};

                        // Build the process
                        ProcessBuilder pb = new ProcessBuilder(proParameters); // GET & SYSTAB_SOURCE.MASTER - needed implement to read from readerIO
//           pb.directory(new File(FILE_PATH_IO)); Check different folder later
                        Process p = pb.start();

                        IOBr = new BufferedReader(new InputStreamReader(p.getInputStream()));
                        StringBuilder lineText = new StringBuilder(); // Maybe only string ?
                        String line = IOBr.readLine();
                        // Start reading in loop until there isn't any more
                        while (line != null) {
                            lineText.append(line); // Maybe don't need ?
                            line = IOBr.readLine(); // Only read one line
                            if (!IOBr.ready()) {
                                IOBr.close(); // Maybe not ?
                                break;
                            }
                        }

//                            String machineInput = "2"; //  TEST
                        String machineInput = lineText.toString(); // Save the value from the reading - Readability
                        String endResult = topicDetails[0] + " " + machineInput; // IO name , value - better readability

                        System.out.println(endResult);
                        fileBw.write(endResult);
                        fileBw.newLine();

                        if (topicDetails.length > 1 && !machineInput.equalsIgnoreCase(topicDetails[1])) { // Better check about array? Maybe both to use integer?
                            String timeStamp = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss").format(new Date());

                            String logResult = timeStamp + " " + endResult; // The result which the log file will save & the one which will show the user

                            resultUser.append(logResult).append("\n"); // Append the log result to later show to save at the log file
                            // Writes data into IOLog file - only those which aren't equal to the value of inputIO(from press)
                            fileBwLog.write(logResult);// Name , value
                            fileBwLog.newLine();
                            // TODO - Method to send the different data
                            numCurStep++; // Iterate to next step
                            stepComplete = true;
                        }

                        System.out.println("Result : " + machineInput);


                        fileData = fileBr.readLine(); // Next line
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    System.out.println("Process IO Error");
                    return "IO Exceptio" + "\n";

                } finally { // Maybe something better later on ?

                    if (fileBw != null) try {
                        fileBw.close(); // Close to finish the stream and write everything
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if (IOBr != null) try {
                        IOBr.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if (fileBr != null) try {
                        fileBr.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if (fileBwLog != null) try {
                        fileBwLog.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                if (listIOCurr.exists()) listIOCurr.delete();
                if (listIOTemp.exists()) listIOTemp.renameTo(listIOCurr);

            }
        }
        listIOCurr.delete(); // Clean up
        listIOTemp.delete(); // Clean up
        return "Step " + numCurStep + " finish, result : " + resultUser.toString();

    }

    // ...
    private class CallSystabIo implements Callable<String> {

        private String topicName = "";

        CallSystabIo(String topicName){
            this.topicName = topicName; // Save the topic
        }

        @Override
        public String call() throws Exception {
            // Order of parameter to read : GET, fileData, topicIndex, SYSTAB_SOURCE.MASTER
            String[] proParameters = {FILE_EXE_IO, "GET", topicName, "0", "SYSTAB_SOURCE.MASTER"}; // TODO - Check if really all 0

            if(util.validationNotNull(proParameters)){
                // Build the process
                ProcessBuilder pb = new ProcessBuilder(proParameters); // GET & SYSTAB_SOURCE.MASTER - needed implement to read from readerIO
                pb.directory(new File(FILE_PATH_IO));
                Process p = pb.start();

                StringBuilder lineText = new StringBuilder(); // Maybe only string ?
                lineText.append(topicName).append(" "); // Append the name of topic and leave a space to append the new value
                try(BufferedReader IOBr = new BufferedReader(new InputStreamReader(p.getInputStream()))){ // Try with resource (auto close)

                    String line = IOBr.readLine();
                    // Start reading in loop until there isn't any more
                    while (line != null) {
                        lineText.append(line); // Add the new value
                        line = IOBr.readLine(); // Only read one line
                        if (!IOBr.ready()) {
                            break;
                        }
                    }
                } catch (IOException e){
                    e.printStackTrace();
                }
                return lineText.toString(); // Return the topicName + new value
            }
            return "Null values";
        }

    }

    String getFileListIoFresh() {
        return FILE_LIST_IO_FRESH;
    }

    // Save the folder name
    void setSystabIOFolderName(String name){
        this.folderName = name + "\\"; // Append the \\ to get inside the folder
    }

}

