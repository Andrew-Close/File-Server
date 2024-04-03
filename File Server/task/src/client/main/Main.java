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
        SpecificUserInput userInput = new SpecificUserInput();
        Scanner scanner = new Scanner(System.in);
        try (Socket socket = new Socket(ADDRESS, PORT);
             DataInputStream input = new DataInputStream(socket.getInputStream());
             DataOutputStream output = new DataOutputStream(socket.getOutputStream()))
        {
            String action = userInput.getValidAction();
            System.out.print("Enter filename: ");
            String filename = scanner.nextLine();
            String fileContent = "";
            if ("PUT".equals(action)) {
                System.out.print("\nEnter file content: ");
                fileContent = scanner.nextLine();
            }
            System.out.println();

            StringBuilder request = new StringBuilder().append(action).append(" ").append(filename);
            if ("PUT".equals(action)) {
                request.append(" ").append(fileContent);
            }
            output.writeUTF(request.toString());
        }
    }
}
