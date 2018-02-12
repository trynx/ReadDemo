package sample;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.*;


public class SystabIO {

    private final static String FILE_PATH_IO = "..\\Systab"; // Better at the folder of the app at - ReadDemo\runtime\bin\
    private final static String FILE_EXE_IO = "systabio.exe";

    private String folderName = "";

    private final static String UTIL_FOLDER = "..\\UtilFiles\\";

    private final static String FILE_LIST_IO_FRESH = UTIL_FOLDER +"FilteredsystabIOTablet7900.txt";
    private final static String FILE_LIST_IO_CURR = "IOCurrent.txt";
    private final static String FILE_LIST_IO_TEMP = "IOTemp.txt";
    private final static String FILE_LIST_IO_LOG = "IOLog.txt";
    private final static String SYSTAB_STEPIO_FOLDER = "StepIO\\";
    private final static String FILE_LIST_STEPS_COMPLETED = SYSTAB_STEPIO_FOLDER + "CompletedIO.txt";

    private final static int STEPS_TO_READ = 10; // How many steps to read at once

    // TODO - Add the folder name (open array , append and save)
    private final static String [] FILE_LIST_IO_STEPS = {UTIL_FOLDER + /* TODO - IOStep1.. */ "Step1IO.txt", UTIL_FOLDER + "Step2IO.txt", UTIL_FOLDER + "Step3IO.txt", UTIL_FOLDER + "Step4IO.txt", UTIL_FOLDER + "Step5IO.txt"};

    private final static int NUM_STEPS = FILE_LIST_IO_STEPS.length;
    private static int numCurStep = 0;

    private static boolean reading = false;



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

        if (!listIOTemp.exists()) { // TODO - When the program is close - delete listIOTemp
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

    // Read all the steps of IO
    String readAll(String pressNum){ // TODO - Make a chooseable way for which steps to check . Each step contains a series of checks that the user will do , only verify those checks
        // All files to write to / read from
        File listIOLog = new File(folderName + FILE_LIST_IO_LOG); // Write only the values which are different
        File listTodo = new File(folderName + "Todosteps.txt"); // This file have all the steps that are needed
        File completedStep = new File( folderName + FILE_LIST_STEPS_COMPLETED);


        BufferedReader fileBr = null;
        BufferedReader fileTodoBr = null;
        BufferedReader fileCompletedBr = null;
        BufferedWriter fileTodoBw = null;
        BufferedWriter fileBw = null; // Temp to used in many places
        BufferedWriter fileBwLog = null;
        BufferedWriter fileCompletedBw = null;

        String completed = "Completed";
        StringBuilder resultUser = new StringBuilder(); // Append the end result to later return it to controller and show to the user

        if (!reading) {
            reading = true; // Started to read the files
            try {
                fileTodoBr = new BufferedReader(new FileReader(listTodo));
                fileCompletedBr = new BufferedReader(new FileReader(completedStep));
                fileTodoBw = new BufferedWriter(new FileWriter(listTodo));
                fileBwLog = new BufferedWriter(new FileWriter(listIOLog, true));
                fileCompletedBw = new BufferedWriter(new FileWriter(completedStep, true));

                String fileTodo = fileTodoBr.readLine(); // Start reading
                String fileCompleted = fileCompletedBr.readLine(); // Start reading

                ExecutorService executor = Executors.newFixedThreadPool(util.getNumThread());

                CompletionService<String> completionService = new ExecutorCompletionService<>(executor); // Initialize  the completion service

                String topicValue = "";
                String topicName = "";
                StringBuilder results = new StringBuilder();
                int counterRequest = 0; // Counter to know how many completion service where open
                int topicCounter = 0;
                Map<String, String> topicMap = new HashMap<>();
                Map<String, Integer> stepMap = new HashMap<>();

                List<String> completedList = new ArrayList<>(); // List of completed steps
                List<String> todoList = new ArrayList<>();
                List<String> stepList = new ArrayList<>(); // Save all the steps (Step1, Step2 ...)

                // File read all the completed steps and save
                while (fileCompleted != null && fileCompleted.contains(completed)) { // While the there is next line and the line contains completed

                    completedList.add(fileCompleted);
                    fileCompleted = fileCompletedBr.readLine(); // Next line

                } // File read end


                // Read the amount of steps needed and add them to the todolist, then save in an arraylist
                for (int stepsReading = 0 ; fileTodo != null && stepsReading < STEPS_TO_READ ; stepsReading++) { // TODO - Add check if the number isn't missing" (1,2,3,[missing],5 ..)

                    todoList.add(fileTodo);
                    fileTodo = fileTodoBr.readLine(); // Next line
                } // File read end

                // Save in a list all the steps of IO
                for(String steps : todoList){ // Maybe write a method in util/interface as it will be needed for Reg
                    String[] stepName = steps.trim().split(" "); // [0] = step number , [1] = step title , [2] = completed / not
                    String step = stepName[0] + " " + stepName[1];
                    stepList.add(step);
                }


                for(String step : stepList){

                    String[] splitStep = step.trim().split(" "); // [0] = step number , [1] = step title
                    String newStep = splitStep[0];
                    String stepFile = SYSTAB_STEPIO_FOLDER + newStep + "IO.text"; // Check if it create the folder if doesn't exist

                    File stepFileCurr = new File(pressNum + SYSTAB_STEPIO_FOLDER + "Curr" + newStep + "IO.text"); // FolderName\StepIO\CurrStepxIO.txt - File with value of current moment & name of systabIO
                    File stepFileFresh = new File(pressNum + stepFile); // FolderName\StepIO\StepxIO.txt - File without value , only the name of the systabIO


                    if (!stepFileCurr.exists()) { // Change file if the current still doesn't exist
                        fileBr = new BufferedReader(new FileReader(stepFileFresh)); // Maybe better way ?
                    } else {
                        fileBr = new BufferedReader(new FileReader(stepFileCurr)); // Add exception for only file not found ?
                    }
//                    String[] stepName = step.trim().split(" "); // [0] = step number , [1] = step name
                    String fileData = fileBr.readLine(); // Start reading

                    while (fileData != null) {

                        String[] topicDetails = fileData.trim().split(" "); // [0] = topic name , [1] = value , [2] = completed(only when completed)

                        if(topicDetails.length > 2 && topicDetails[2].equals(completed)) continue; // The current topic is already completed and don't need to check again -> continue to the next topic

                        topicName = topicDetails[0]; // Readability
                        if (topicDetails.length > 1) { // -> there is a value
                            topicValue = topicDetails[1]; // Readability , verify only here if it really exist
                            topicMap.put(topicName,topicValue); // Save the topic name and value as my temp
                        } else {
                            topicMap.put(topicName,""); // No topic value
                        }

                        // Start futuretask completion service -> Start process to read the SystabIO
                        completionService.submit(new CallSystabIo(topicName, newStep)); // TEST
                        counterRequest++;
                        stepMap.put(newStep, ++topicCounter); // Save the step with the counter of how many topics there is

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
                        String stepNumber = endResultArr[0];
                        String newTopicName = endResultArr[1];
                        String newValue = endResultArr[2];
                        String endResult = newTopicName + " " + newValue;

                        // Create the new current file with new values
                        File stepFileCurr = new File(pressNum + SYSTAB_STEPIO_FOLDER + "Curr" + stepNumber + "IO.text"); // FolderName\StepIO\CurrStepxIO.txt - File with value of current moment & name of systabIO


                        // Writes data into IOLog file - only those which aren't equal to the value of inputIO(from press) -> Done current topic check
                        if (!endResultArr[2].equals(topicMap.get(endResultArr[1]))){ // Check if the new value and old value are equal , not equal -> show to user and save in log
                            String timeStamp = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss").format(new Date());

                            // TODO - Change the values to On / Off
                            String logResult = timeStamp + " " + stepNumber + " " + endResult; // The result which the log file will save & the one which will show the user

                            resultUser.append(logResult).append("\n"); // Append the log result to later show to the user

                            fileBwLog.write(logResult);
                            fileBwLog.newLine();

                            fileBw = new BufferedWriter(new FileWriter(stepFileCurr));
                            fileBw.write(endResult + " " + completed);
                            fileBw.newLine();

                            int remainTopics = stepMap.get(stepNumber) - 1; // This topic is done -> one less topic in the step
                            if(remainTopics == 0){ // When the step don't have any more topics to check , it will remove from the step list and add to the completed step list
                                stepList.remove(stepNumber);
                                completedList.add(stepNumber);
                            }
                            stepMap.put(stepNumber, remainTopics);

                        } else { // When the topic isn't complete
                            fileBw = new BufferedWriter(new FileWriter(stepFileCurr));
                            fileBw.write(endResult);
                            fileBw.newLine();
                        }




                    } catch (InterruptedException | ExecutionException e) {
                        e.printStackTrace();
                        return "CompletionService of SystabIO process Exception";
                    }
                }

                listTodo.delete(); // Delete the file to reset the list and write the remain steps
                completedStep.delete(); // Delete the file to reset the list and write the remain steps

                // First write all the completed steps
                for (String step: completedList){
                    fileCompletedBw.write(step + " " + completed);
                    fileCompletedBw.newLine();
                }
                // Then all the undone steps
                for (String step: stepList){
                    // Write in the completed file
                    fileCompletedBw.write(step + " Undone");
                    fileCompletedBw.newLine();
                    // Write in the todosteps , the next time it read only the remaining steps
                    fileTodoBw.write(step);
                    fileTodoBw.newLine();
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
                if (fileTodoBr != null) try {
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
        private String stepNumber = "";

        CallSystabIo(String topicName, String stepNumber){
            this.topicName = topicName; // Save the topic
            this.stepNumber = stepNumber;
        }

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
                if(stepNumber.isEmpty()){ // No need to append the stepNumber as it doesn't was insert
                    lineText.append(topicName).append(" "); // Append the name of topic and leave a space to append the new value
                } else {
                    lineText.append(stepNumber).append(" ").append(topicName).append(" "); // Append the name of topic and leave a space to append the new value
                }

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

