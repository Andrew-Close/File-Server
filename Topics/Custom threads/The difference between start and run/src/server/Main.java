package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {
    public static void main(String[] args) throws IOException {
        try (ServerSocket server = new ServerSocket(11111)) {
            while (true) {
                try (
                        Socket socket = server.accept();
                        DataInputStream input = new DataInputStream(socket.getInputStream());
                        DataOutputStream output = new DataOutputStream(socket.getOutputStream())
                )
                {
                    while (true) {
                        String msg = input.readUTF();
                        output.writeUTF("Message read! Your message: " + msg);
                    }
                }
            }
        }
    }
}
