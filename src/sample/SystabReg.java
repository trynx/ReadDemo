package sample;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.*;

public class SystabReg {

    // TODO - Add folder name to files + lists
    private final static String FILE_PATH_REG = "C:\\Unicorn\\release\\";
    private final static String FILE_EXE_READER_REG = "systabreader.exe";
    private final static String FILE_EXE_WRITER_REG = "systabwriter.exe";

    private final static String UTIL_FOLDER = "..\\UtilFiles\\";

    private final static String FILE_LIST_REG_FRESH = UTIL_FOLDER +"systabRegList7900.txt";
    private final static String FILE_LIST_REG_CURR = "RegCurrent.txt";
    private final static String FILE_LIST_REG_TEMP = "RegTemp.txt";
    private final static String FILE_LIST_REG_LOG = "RegLog.txt";

    private final static String [] FILE_LIST_REG_STEPS = {UTIL_FOLDER + "Step1Reg.txt", UTIL_FOLDER + "Step2Reg.txt", UTIL_FOLDER + "Step3Reg.txt", UTIL_FOLDER + "Step4Reg.txt", UTIL_FOLDER + "Step5Reg.txt"};

    private String folderName = "";


   private Util util = new Util();

   /** All read methohds START **/
   String readSystabReg(String topicName, String topicIndex, String itemName, String itemIndex, String dynamicSaved, String folderName) {
       // Order of parameter to write : topicName, topicIndex,  itemName, itemIndex, value, dynamicSaved
        // Validation
        String [] proParameters = {FILE_EXE_READER_REG,topicName, topicIndex,itemName, itemIndex , dynamicSaved}; // The order of parameters are important for Process Builder!!

        if (util.validationNotNull(proParameters)) { // Validate everything -> true = no null

            BufferedReader br = null; // Outside of try/catch scope so it can be used in finally scope
            // Build the process
            ProcessBuilder pb = new ProcessBuilder(proParameters);

            pb.directory(new File(FILE_PATH_REG));
            try {
                Process p = pb.start();

                br = new BufferedReader(new InputStreamReader(p.getInputStream()));

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

                String userInput = topicName + " " + topicIndex + " " + itemName + " " + itemIndex;
                String machineInput = lineText.toString();

                util.writeFile(machineInput + " " + topicName + " " + itemName,folderName, "SystabRegular");
                return "User request : "  + userInput + "\n"
                        + "Answer : " + machineInput;

            }catch(IOException e){
                e.printStackTrace();
                System.out.println("Process IO Error");
            } finally {
                if(br != null) try {
                    br.close(); // if it couldn't be close at the try block because an exception
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return "No data";
        }
        return "Nothing";
    }


    String readSystabRegTest(String input, String fileName){
        util.writeFile(input,fileName, "Regular");
        return input;
    }

    /** Loop through all Regular **/
    String readAll(){
        // All files to write to / read from
        File listIOFresh = new File(FILE_LIST_REG_FRESH); // Only used when there still isn't a current file - template of list for IO
        File listIOCurr = new File(folderName + FILE_LIST_REG_CURR); // The file which the values and list are taken and compare with input from IO
        File listIOTemp = new File(folderName + FILE_LIST_REG_TEMP); // While runtime it will write the values in this file
        File listIOLog = new File(folderName + FILE_LIST_REG_LOG); // Write only the values which are different

        BufferedReader IOBr = null;
        BufferedReader fileBr = null;
        BufferedWriter fileBw = null;
        BufferedWriter fileBwLog = null;

        StringBuilder resultUser = new StringBuilder(); // Append the end result to later return it to controller and show to the user
        List<String> topicList = new ArrayList<>();

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

                ExecutorService executor = Executors.newFixedThreadPool(util.getNumThread()); // Changed to a global variable for readability
                CompletionService<String> completionService = new ExecutorCompletionService<>(executor); // Initialize  the completion service

                String topicValue = ""; // Save the current value , which will be compare with the new value
                String topicFullName = "";
                StringBuilder results = new StringBuilder();
                int counterRequest = 0;
                HashMap<String, String> topicMap = new HashMap<>();

                // File data start
                while (fileData != null) {

                    topicList.add(fileData);
                    String[] topicDetails = fileData.trim().split(" "); // [0] = topicName, [1] = topicIndex, [2] = itemName, [3] = itemIndex, [4] = value
                    topicFullName = topicDetails[0] +  " " + topicDetails[1] +  " " + topicDetails[2] +  " " + topicDetails[3] ; // Readability
                    if (topicDetails.length > 4) { // Bigger than 4 -> there is value at [4]
                        topicValue = topicDetails[4]; // Readability , verify only here if it really exist
                        topicMap.put(topicFullName,topicValue);
                    } else { // No value
                        topicMap.put(topicFullName,""); // No value to compare later
                    }

                    // Start futuretask completion service -> Start process to read the SystabReg
                    completionService.submit(new SystabReg.CallSystabReg(topicDetails[0], topicDetails[1], topicDetails[2], topicDetails[3]));  // [0] = topicName, [1] = topicIndex, [2] = itemName, [3] = itemIndex
                    counterRequest++;
                    fileData = fileBr.readLine(); // Next line
                }
                // File data end

                // Take results from process , as the result are random topics, use the help of listMap(HashMap) to save the topic full name and value to verify with the new value
                for(int i = 0 ; i < counterRequest ; i++){ // Loop through the results -> counterRequests have the number of how many request
                    try {
                        Future<String> resultFuture = completionService.take(); // Wait for one completed task
                        results.append(resultFuture.get()); // Get a completed task , results = topicName + new Value

                        String [] endResultArr = results.toString().trim().split(" "); // [0] = topicName, [1] = topicIndex, [2] = itemName, [3] = itemIndex, [4] = value
                        String endResult = endResultArr[0] +  " " + endResultArr[1] +  " " + endResultArr[2] +  " " + endResultArr[3] + " " + endResultArr[4];

                        fileBw.write(endResult);
                        fileBw.newLine();

                        // Writes data into IOLog file - only those which aren't equal to the value of inputIO(from press)
                        if (!topicMap.get(topicFullName).isEmpty() && !endResultArr[4].equalsIgnoreCase(topicValue)) { // If the map in that key is empty -> first search , new value is different than current value
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

    // Read all the steps of IO
    String readAll(String pressNum){
        // All files to write to / read from
        File listIOLog = new File(folderName + FILE_LIST_IO_LOG); // Write only the values which are different
        File listTodo = new File(folderName + "Todosteps.txt"); // This file have all the steps that are needed


        BufferedReader fileBr = null;
        BufferedReader fileTodoBw = null;
        BufferedWriter fileBw = null; // Temp to used in many places
        BufferedWriter fileBwLog = null;


        StringBuilder resultUser = new StringBuilder(); // Append the end result to later return it to controller and show to the user

        if (!reading) { // TODO - When the program is close - delete listIOTemp
            reading = true; // Started to read the files
            try {
                fileTodoBw = new BufferedReader(new FileReader(listTodo));
                fileBwLog = new BufferedWriter(new FileWriter(listIOLog, true));
            /*    if (!listIOCurr.exists()) { // Change file if the current still doesn't exist
                    fileBr = new BufferedReader(new FileReader(listIOFresh)); // Maybe better way ?
                } else {
                    fileBr = new BufferedReader(new FileReader(listIOCurr)); // Add exception for only file not found ?
                }*/

//                String fileData = fileBr.readLine(); // Start reading
                String fileTodo = fileTodoBw.readLine(); // Start reading

                ExecutorService executor = Executors.newFixedThreadPool(util.getNumThread());

                CompletionService<String> completionService = new ExecutorCompletionService<>(executor); // Initialize  the completion service

                String topicValue = "";
                String topicName = "";
                StringBuilder results = new StringBuilder();
                int counterRequest = 0; // Counter to know how many completion service where open
                Map<String, String> topicMap = new HashMap<>();
                List<String> todoList = new ArrayList<>();
                List<String> stepList = new ArrayList<>(); // Save all the steps (Step1, Step2 ...)

                // File read  all todo list and save in an arraylist
                while (fileTodo != null) { // TODO - Add check if the number isn't missing" (1,2,3,[missing],5 ..)

                    todoList.add(fileTodo);
                    fileTodo = fileTodoBw.readLine(); // Next line
                } // File read end

                // Save in a list all the steps of IO
                for(String steps : todoList){ // Maybe write a method in util/interface as it will be needed for Reg
                    String[] stepName = steps.trim().split(" "); // [0] = step number , [1] = step title

                    stepList.add(stepName[0].trim());
                }


                for(String step : stepList){

                    String stepFile ="StepIO\\" + step + "IO.text"; // Check if it create the folder if doesn't exist

                    File stepFileCurr = new File(pressNum + "StepIO\\Curr" + step + "IO.text"); // FolderName\SystabIO\CurrStepxIO.txt - File with value of current moment & name of systabIO
                    File stepFileFresh = new File(pressNum + stepFile); // FolderName\SystabIO\StepxIO.txt - File without value , only the name of the systabIO


                    if (!stepFileCurr.exists()) { // Change file if the current still doesn't exist
                        fileBr = new BufferedReader(new FileReader(stepFileFresh)); // Maybe better way ?
                    } else {
                        fileBr = new BufferedReader(new FileReader(stepFileCurr)); // Add exception for only file not found ?
                    }
//                    String[] stepName = step.trim().split(" "); // [0] = step number , [1] = step name
                    String fileData = fileBr.readLine(); // Start reading

                    while (fileData != null) {

                        String[] topicDetails = fileData.trim().split(" "); // [0] = topic name , [1] = value
                        topicName = topicDetails[0]; // Readability
                        if (topicDetails.length > 1) { // -> there is a value
                            topicValue = topicDetails[1]; // Readability , verify only here if it really exist
                            topicMap.put(topicName,topicValue); // Save the topic name and value as my temp
                        } else {
                            topicMap.put(topicName,""); // No topic value
                        }

                        // Start futuretask completion service -> Start process to read the SystabIO
                        completionService.submit(new SystabIO.CallSystabReg(topicName, step)); // TEST
                        counterRequest++;
                        fileData = fileBr.readLine(); // Next line
                    } // No more topics to read from , continue to the next step

                    stepFileCurr.delete(); // Delete it as all the data was read and saved , for later create a new current file with the new values
                }


                // Take results from process
                for(int i = 0 ; i < counterRequest ; i++){ // Loop through the results -> counterRequests have the number of how many request were submit to completion service
                    try {
                        Future<String> resultFuture = completionService.take(); // Wait for one completed task
                        results.append(resultFuture.get()); // Get a completed task , results = topicName + new Value

                        String [] endResultArr = results.toString().trim().split(" "); // [0] = step number . [1] = topic name , [2] = value
                        String endResult = endResultArr[1] + " " + endResultArr[2];


                        // Writes data into IOLog file - only those which aren't equal to the value of inputIO(from press)
                        if (!endResultArr[2].equals(topicMap.get(endResultArr[1]))){ // Check if the new value and old value are equal , not equal -> show to user and save in log
                            String timeStamp = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss").format(new Date());

                            // TODO - Change the values to On / Off
                            String logResult = timeStamp + " " + endResultArr[0] + " " + endResult; // The result which the log file will save & the one which will show the user

                            resultUser.append(logResult).append("\n"); // Append the log result to later show to the user

                            fileBwLog.write(logResult);
                            fileBwLog.newLine();
                        }

                        // Create the new current file with new values
                        File stepFileCurr = new File(pressNum + "StepIO\\Curr" + endResultArr[0] + "IO.text"); // FolderName\SystabIO\CurrStepxIO.txt - File with value of current moment & name of systabIO

                        fileBw = new BufferedWriter(new FileWriter(stepFileCurr));
                        fileBw.write(endResult);
                        fileBw.newLine();

                    } catch (InterruptedException | ExecutionException e) {
                        e.printStackTrace();
                        return "CompletionService of SystabIO process Exception";
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("Process IO Error");
                return "Read error IO" + "\n"; // Don't change , this return stops the flow !

            } finally { // Maybe something better later on ?
                reading = false; // Finish to read all
                if (fileBw != null) try {
                    fileBw.close(); // Close to finish the stream and write everything
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (fileBr != null) try {
                    fileBr.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (fileTodoBw != null) try {
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

            return resultUser.toString();
        }
        return "No File";
    }

    private class CallSystabReg implements Callable<String> {

        private String topicName = "";
        private String topicIndex = "";
        private String itemName = "";
        private String itemIndex = "";
        private String stepNumber = "";


        private String fullName = topicName + " " + topicIndex + " " + itemName + " " + itemIndex;

        CallSystabReg(String topicName, String topicIndex, String itemName, String itemIndex) {
            this.topicName = topicName; // Save the topic
            this.topicIndex = topicIndex; // Save the topic
            this.itemName = itemName; // Save the topic
            this.itemIndex = itemIndex; // Save the topic
        }

        CallSystabReg(String topicName, String topicIndex, String itemName, String itemIndex, String stepNumber) {
            this.topicName = topicName; // Save the topic
            this.topicIndex = topicIndex; // Save the topic
            this.itemName = itemName; // Save the topic
            this.itemIndex = itemIndex; // Save the topic
            this.stepNumber = stepNumber; // Save the step number
        }

        // TODO - Continue here
        @Override
        public String call() throws Exception {
            // Order of parameter to write : topicName, topicIndex,  itemName, itemIndex, value, dynamicSaved
            String[] proParameters = {FILE_EXE_READER_REG, topicName, topicIndex, itemName, itemIndex, "DYNAMIC"}; // Always read Dynamic , as it's the only value that will change

            if (util.validationNotNull(proParameters)) {
                // Build the process
                ProcessBuilder pb = new ProcessBuilder(proParameters); //
                pb.directory(new File(FILE_PATH_REG));
                Process p = pb.start();

                StringBuilder lineText = new StringBuilder(); // Maybe only string ?
                if(stepNumber.isEmpty()){
                    lineText.append(fullName).append(" "); // Append the name of topic and leave a space to append the new value
                } else {
                    lineText.append(stepNumber).append(" ").append(fullName).append(" "); // Append the step number with the name of topic and leave a space to append the new value
                }
                try (BufferedReader IOBr = new BufferedReader(new InputStreamReader(p.getInputStream()))) { // Try with resource (auto close)

                    String line = IOBr.readLine();
                    // Start reading in loop until there isn't any more
                    while (line != null) {
                        lineText.append(line); // Add the new value
                        line = IOBr.readLine(); // Only read one line
                        if (!IOBr.ready()) {
                            break;
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return lineText.toString(); // Return the topicName + new value
            }
            return "Null values";
        }
    }

    /** All read methohds END **/


    /** All write methohds START **/
    boolean writeSystabReg(String topicName, String topicIndex, String itemName, String itemIndex, String value, String dynamicSaved){
        // Order of parameter to write : topicName, topicIndex,  itemName, itemIndex, value, dynamicSaved
        // Validation
        String [] proParameters = {FILE_EXE_WRITER_REG, topicName, topicIndex, itemName, itemIndex, value, dynamicSaved}; // The order of parameters are important for Process Builder!!

        if (util.validationNotNull(proParameters)){ // Validate everything -> true = no null

            BufferedWriter bw = null; // Outside of try/catch scope so it can be used in finally scope
            // Build the process
            ProcessBuilder pb = new ProcessBuilder(proParameters);
            pb.directory(new File(FILE_PATH_REG)); // Directory of the FILE_EXE_READER_REG
            try {
                Process p = pb.start();

                bw = new BufferedWriter(new OutputStreamWriter(p.getOutputStream()));

                bw.write(value);
                bw.flush();

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



    // Save the folder name
    void setSystabRegFolderName(String name){
        this.folderName = name + "\\"; // Append the \\ to get inside the folder
    }

}
