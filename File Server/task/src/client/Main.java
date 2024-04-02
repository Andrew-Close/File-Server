package client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class Main {
    public static void main(String[] args) throws IOException {
        try (Socket socket = new Socket("127.0.0.1", 23456);
             DataInputStream input = new DataInputStream(socket.getInputStream());
             DataOutputStream output = new DataOutputStream(socket.getOutputStream()))
        {
            System.out.println("Client started!");
            String sentMessage = "Give me everything you have!";
            output.writeUTF(sentMessage);
            System.out.println("Sent: " + sentMessage);
            System.out.println("Received: " + input.readUTF());
        }
    }
}
