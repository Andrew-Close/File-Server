package client.main;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;
// import java.util.concurrent.TimeUnit;

import static config.Config.ADDRESS;
import static config.Config.PORT;

public class Main {
    private static final Scanner SCANNER = new Scanner(System.in);
    public static void main(String[] args) throws IOException/*, InterruptedException*/ {
        //
        //
        // Use this when running tests. Also uncomment TimeUnit import and the InterruptedException in the throws list
        // |
        // |
        // V
        //
        // TimeUnit.SECONDS.sleep(1);
        // Gets valid user input
        SpecificUserInput userInput = new SpecificUserInput();

        try (Socket socket = new Socket(ADDRESS, PORT);
             DataInputStream input = new DataInputStream(socket.getInputStream());
             DataOutputStream output = new DataOutputStream(socket.getOutputStream()))
        {
            // User input
            String action = userInput.getValidAction();
            // User input and building the request to be sent
            switch (action) {
                case "GET":
                    sendGetRequest(input, output);
                    break;
                case "PUT":
                    sendPutRequest(input, output);
                    break;
                case "DELETE":
                    sendDeleteRequest(input, output);
                    break;
                case "EXIT":
                    sendExitRequest(output);
            }
        }
    }

    private static void sendGetRequest(DataInputStream input, DataOutputStream output) throws IOException {
        System.out.print("Enter filename: ");
        String filename = SCANNER.nextLine();
        // The GET request
        output.writeUTF(Actions.GET + " " + filename);
        System.out.println("The request was sent.");
        // Checking the status code and printing the respective message
        String statusCode = input.readUTF();
        // Success
        if ("200".equals(statusCode.substring(0, 3))) {
            System.out.printf("The content of the file is: %s\n", statusCode.substring(4));
        // Failure
        } else if ("404".equals(statusCode)) {
            System.out.println("The response says that the file was not found!");
        }
    }

    private static void sendPutRequest(DataInputStream input, DataOutputStream output) throws IOException {
        System.out.print("Enter filename: ");
        String filename = SCANNER.nextLine();
        System.out.print("Enter file content: ");
        String fileContent = SCANNER.nextLine();
        // The PUT request
        output.writeUTF(Actions.PUT + " " + filename + " " + fileContent);
        System.out.println("The request was sent.");
        // Checking the status code and printing the respective message
        String statusCode = input.readUTF();
        // Success
        if ("200".equals(statusCode)) {
            System.out.println("The response says that the file was created!");
        // Failure
        } else if ("403".equals(statusCode)) {
            System.out.println("The response says that creating the file was forbidden!");
        }
    }

    private static void sendDeleteRequest(DataInputStream input, DataOutputStream output) throws IOException {
        System.out.print("Enter filename: ");
        String filename = SCANNER.nextLine();
        // The DELETE request
        output.writeUTF(Actions.DELETE + " " + filename);
        System.out.println("The request was sent.");
        // Checking the status code and printing the respective message
        String statusCode = input.readUTF();
        // Success
        if ("200".equals(statusCode)) {
            System.out.println("The response says that the file was successfully deleted!");
        // Failure
        } else if ("404".equals(statusCode)) {
            System.out.println("The response says that the file was not found!");
        }
    }

    private static void sendExitRequest(DataOutputStream output) throws IOException {
        output.writeUTF(Actions.EXIT.toString());
        System.out.println("The request was sent.");
    }
}
