package client.main;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

import static config.Config.ADDRESS;
import static config.Config.PORT;

public class Main {
    private static final Scanner SCANNER = new Scanner(System.in);
    public static void main(String[] args) throws IOException {
        // Gets valid user input
        SpecificUserInput userInput = new SpecificUserInput();

        try (Socket socket = new Socket(ADDRESS, PORT);
             DataInputStream input = new DataInputStream(socket.getInputStream());
             DataOutputStream output = new DataOutputStream(socket.getOutputStream()))
        {
            // User input
            String action = userInput.getValidAction();
            // User input and building the request to be sent
            String request = switch (action) {
                case "GET" -> getGetRequest();
                case "PUT" -> getPutRequest();
                case "DELETE" -> getDeleteRequest();
                case "EXIT" -> getExitRequest();
                default -> "NULL";
            };
            output.writeUTF(request);
            System.out.println("The request was sent!");
            System.out.println(input.readUTF());
        }
    }

    private static String getGetRequest() {
        System.out.print("Enter filename: ");
        String filename = SCANNER.nextLine();
        // The GET request
        return Actions.GET + " " + filename;
    }

    private static String getPutRequest() {
        System.out.print("Enter filename: ");
        String filename = SCANNER.nextLine();
        System.out.print("Enter file content: ");
        String fileContent = SCANNER.nextLine();
        // The PUT request
        return Actions.PUT + " " + filename + " " + fileContent;
    }

    private static String getDeleteRequest() {
        System.out.print("Enter filename: ");
        String filename = SCANNER.nextLine();
        // The DELETE request
        return Actions.DELETE + " " + filename;
    }

    private static String getExitRequest() {
        return Actions.EXIT.toString();
    }
}
