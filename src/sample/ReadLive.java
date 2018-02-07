package sample;

import java.io.*;

public class ReadLive implements Runnable{

    private final static String FILE_PATH_IO = "..\\"; // Better at the folder of the app at - ReadDemo\runtime\bin\
    private final static String FILE_EXE_IO = "systabio.exe";
    private final static String FILE_LIST_IO = "FilteredsystabIOTablet7900.txt";


    void readAll(){

        // Open file of list and save all in array ?
        File listIO = new File(FILE_LIST_IO);

        BufferedReader fileBr = null;
        try {

            fileBr = new BufferedReader(new FileReader(listIO)); // Add exception for only file not found ?

            String topicName = fileBr.readLine();
            while (topicName != null) {
                System.out.println(topicName);

                // Order of parameter to read : GET, topicName, topicIndex, SYSTAB_SOURCE.MASTER
                String[] proParameters = {FILE_EXE_IO, "GET", topicName, "0", "SYSTAB_SOURCE.MASTER"};

                // Build the process
                ProcessBuilder pb = new ProcessBuilder(proParameters); // GET & SYSTAB_SOURCE.MASTER - needed to use readerIO

//            pb.directory(new File(FILE_PATH_IO)); Check different folder later

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
                String machineInput = lineText.toString();
                System.out.println( "Result : " + machineInput);

//                util.writeFile(machineInput + " " + topicName, fileName, "IO");

            }
            }catch(IOException  e ){
                e.printStackTrace();
                System.out.println("Process IO Error");
            }
        }

    // Would write to a file where all the changes happen (log type)
    @Override
    public void run() {

    }
}
