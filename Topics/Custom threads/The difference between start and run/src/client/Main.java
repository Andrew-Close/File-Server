package client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);
        try (
                Socket socket = new Socket("127.0.0.1", 11111);
                DataInputStream input = new DataInputStream(socket.getInputStream());
                DataOutputStream output = new DataOutputStream(socket.getOutputStream())
                )
        {
            while (true) {
                output.writeUTF(scanner.nextLine());
                System.out.println(input.readUTF());
            }
        }
    }
}
