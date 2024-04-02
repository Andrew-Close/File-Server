package client.main;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import static config.Config.ADDRESS;
import static config.Config.PORT;

public class Main {
    public static void main(String[] args) throws IOException {
        try (Socket socket = new Socket(ADDRESS, PORT);
             DataInputStream input = new DataInputStream(socket.getInputStream());
             DataOutputStream output = new DataOutputStream(socket.getOutputStream()))
        {

        }
    }
}
