package client.main;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;
// import java.util.concurrent.TimeUnit;
import static config.Config.*;
import static server.main.Main.printMap;


public class Main {
    private static final Scanner SCANNER = new Scanner(System.in);
    private static final SpecificUserInput USER_INPUT = new SpecificUserInput();
    public static void main(String[] args) throws IOException, InterruptedException/*, InterruptedException*/ {
        //
        //
        // Use this when running tests. Also uncomment TimeUnit import and the InterruptedException in the throws list
        // |
        // |
        // V
        //
        // TimeUnit.SECONDS.sleep(1);

        // Gets valid user input
        try (Socket socket = new Socket(ADDRESS, PORT);
             DataInputStream input = new DataInputStream(socket.getInputStream());
             DataOutputStream output = new DataOutputStream(socket.getOutputStream()))
        {
            //
            //
            //
            // DEBUGGING
            //
            //
            //
            // System.out.println("Map at beginning of connection");
            // printMap();
            // User input
            String action = USER_INPUT.getValidAction();
            // Specifies whether to get or delete the file by id or by name
            int retrievalMode;
            // Chooses which request to build and send based on the inputted action
            switch (action) {
                case "GET":
                    retrievalMode = USER_INPUT.getValidRetrievalMode(Actions.GET);
                    sendGetRequest(input, output, retrievalMode);
                    break;
                case "PUT":
                    sendPutRequest(input, output);
                    break;
                case "DELETE":
                    retrievalMode = USER_INPUT.getValidRetrievalMode(Actions.DELETE);
                    sendDeleteRequest(input, output, retrievalMode);
                    break;
                case "EXIT":
                    sendExitRequest(output);
            }
        }
    }

    /**
     * Constructs and sends a GET request to the server and receives the status code. Can either get by id or by name depending on the passed retrieval mode.
     * @param input the input stream from which the status code is retrieved
     * @param output the output stream to which the GET request is sent
     * @param retrievalMode specifies whether to get by id or by name. name is 1, id is 2
     * @throws IOException thrown if the input or output mess up somehow
     */
    private static void sendGetRequest(DataInputStream input, DataOutputStream output, int retrievalMode) throws IOException, InterruptedException {
        switch (retrievalMode) {
            case 1:
                String filename = USER_INPUT.getExistingFile(true);
                // The GET request
                output.writeUTF(Actions.GET + "_BY_NAME" + " " + filename);
                break;
            case 2:
                //
                //
                //
                // DEBUGGING
                //
                //
                //
                // System.out.println("Before accessing map in client");
                // printMap();
                // I can't just use an integer because, for some reason, when I use nextInt, it just skips the user input for the filename later on
                String id = USER_INPUT.getExistingID();
                // The GET request
                output.writeUTF(Actions.GET + "_BY_ID" + " " + id);
                break;
        }
        System.out.println("The request was sent.");
        // Getting the content of the file and status code
        int length = input.readInt();
        byte[] content = input.readNBytes(length);
        String statusCode = input.readUTF();
        // Success
        if ("200".equals(statusCode)) {
            // This repositions the scanner to the beginning of the user input or something. Basically, without this line of code, the program sometimes skips the user input for the filename
            // SCANNER.nextLine();
            System.out.print("The file was downloaded! Specify a name for it: ");
            String filename = SCANNER.nextLine();
            File clientFile = new File(String.format(CLIENT_STORAGE_FOLDER, filename));
            try (OutputStream fileOutput = new FileOutputStream(clientFile)) {
                fileOutput.write(content);
            }
        // Failure
        } else if ("404".equals(statusCode)) {
            System.out.println("The response says that the file was not found!");
        }
    }

    /**
     * Constructs and sends a PUT request to the server and receives the status code.
     * @param input the input stream from which the status code is retrieved
     * @param output the output stream to which the PUT request is sent
     * @throws IOException thrown if the input or output mess up somehow
     */
    private static void sendPutRequest(DataInputStream input, DataOutputStream output) throws IOException, InterruptedException {
        // Locally existing file that the user wants to save on the server
        String filenameLocal = USER_INPUT.getExistingFile(false);
        // The format of the file, including the  period
        String format = filenameLocal.substring(filenameLocal.lastIndexOf("."));
        // Name that the file should be called on the server
        System.out.print("Enter name of the file to be saved on server: ");
        String filenameServer = SCANNER.nextLine();
        // The PUT request
        output.writeUTF(Actions.PUT + " " + filenameLocal + " " + filenameServer + " " + format);
        System.out.println("The request was sent.");
        // Checking the status code and printing the respective message
        String statusCode = input.readUTF();
        // Success, checking only the first three digits of the status (the actual status code)
        if ("200".equals(statusCode.substring(0, 3))) {
            // substring(4) is the id
            System.out.println("Response says that file is saved! ID = " + statusCode.substring(4));
        // Failure
        } else if ("403".equals(statusCode)) {
            System.out.println("The response says that creating the file was forbidden!");
        }
    }

    /**
     * Constructs and sends a DELETE request to the server and receives the status code. Can either delete by id or by name depending on the passed retrieval mode.
     * @param input the input stream from which the status code is retrieved
     * @param output the output stream to which the DELETE request is sent
     * @param retrievalMode specifies whether to get by id or by name. name is 1, id is 2
     * @throws IOException thrown if the input or output mess up somehow
     */
    private static void sendDeleteRequest(DataInputStream input, DataOutputStream output, int retrievalMode) throws IOException {
        switch (retrievalMode) {
            case 1:
                String filename = USER_INPUT.getExistingFile(true);
                // The GET request
                output.writeUTF(Actions.DELETE + "_BY_NAME" + " " + filename);
                break;
            case 2:
                String id = USER_INPUT.getExistingID();
                // The GET request
                output.writeUTF(Actions.DELETE + "_BY_ID" + " " + id);
                break;
        }
        System.out.println("The request was sent.");
        // Checking the status code and printing the respective message
        String statusCode = input.readUTF();
        // Success
        if ("200".equals(statusCode)) {
            System.out.println("The response says that this file was deleted successfully!");
        // Failure
        } else if ("404".equals(statusCode)) {
            System.out.println("The response says that this file is not found!");
        }
    }

    /**
     * Sends an exit request to the server. Does not receive a status code.
     * @param output the output stream to which the exit request is sent
     * @throws IOException thrown if the output messes up somehow
     */
    private static void sendExitRequest(DataOutputStream output) throws IOException {
        output.writeUTF(Actions.EXIT.toString());
        System.out.println("The request was sent.");
    }
}
