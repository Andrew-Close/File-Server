package client.main;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

import static config.Config.ADDRESS;
import static config.Config.PORT;

public class Main {
    public static void main(String[] args) throws IOException {
        // Gets valid user input
        SpecificUserInput userInput = new SpecificUserInput();
        Scanner scanner = new Scanner(System.in);
        try (Socket socket = new Socket(ADDRESS, PORT);
             DataInputStream input = new DataInputStream(socket.getInputStream());
             DataOutputStream output = new DataOutputStream(socket.getOutputStream()))
        {
            // User input
            String action = userInput.getValidAction();
            System.out.print("Enter filename: ");
            String filename = scanner.nextLine();
            String fileContent = null;
            if ("PUT".equals(action)) {
                System.out.print("Enter file content: ");
                fileContent = scanner.nextLine();
            }

            // Building the request
            StringBuilder request = new StringBuilder().append(action).append(" ").append(filename);
            if (fileContent != null) {
                request.append(" ").append(fileContent);
            }
            output.writeUTF(request.toString());
            System.out.println(input.readUTF());
        }
    }
}
