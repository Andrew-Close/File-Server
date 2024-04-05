package server.main;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import static config.Config.storageFolder;

public class Main {
    private static final List<String> storage = new ArrayList<>();
    private static final CommandInterpreter interpreter = new CommandInterpreter();
    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) throws IOException {
        // The entire runtime of the server
        serverRuntime();
    }

    /**
     * The entire runtime of the program where the user continuously inputs commands until they input exit.
     */
    private static void serverRuntime() throws IOException {
        try (ServerSocket server = new ServerSocket(23456))
        {
            System.out.println("Server started!");
            serverloop:
            while (true) {
                try (Socket socket = server.accept();
                     DataInputStream input = new DataInputStream(socket.getInputStream());
                     DataOutputStream output = new DataOutputStream(socket.getOutputStream()))
                {
                    String[] command = input.readUTF().split(" ");
                    // Contains the information about how to act upon the command that the user inputted
                    Interpretation interpretation = interpreter.interpret(command);
                    String statusCode = null;
                    // The id is the operation id. It shows which operation should be executed
                    switch (interpretation.getId()) {
                        // Get file
                        case 1:
                            statusCode = getFile(interpretation.getData()[0]);
                            break;
                        // Add file
                        case 2:
                            statusCode = addFile(interpretation.getData());
                            break;
                        // Delete file
                        case 3:
                            statusCode = deleteFile(interpretation.getData()[0]);
                            break;
                        // Exit system
                        case 0:
                            break serverloop;
                        // User input an incorrect operation
                        case -1:
                            output.writeUTF("Invalid command. You must choose the add, get, delete, or exit command.");
                            break;
                        // Somehow, the operation id is wrong, probably due to a programming error which I may make in the future
                        default:
                            output.writeUTF("Could not act upon interpretation. Operation id is invalid.");
                    }
                    if (statusCode != null) {
                        output.writeUTF(statusCode);
                    }
                }
            }
        }
    }

    /**
     * Adds a file to the storage
     * @param data the data of the interpretation, which includes the file name and the file content
     * @return the status code of the operation
     */
    private static String addFile(String[] data) throws IOException {
        String filepath = String.format(storageFolder, data[0]);
        File file = new File(filepath);
        // Checks if the storage already contains the file, the storage is full, or if the file name is incorrect (not "file1" through "file10")
        /*
        if (storage.contains(data) || storage.size() == 10 || !(data.matches("file[0-9]|file10"))) {
            return false;
        } else {
            storage.add(data);
            return true;
        }
         */
        if (file.createNewFile()) {
            try (Writer writer = new FileWriter(file)) {
                writer.write(String.join(" ", Arrays.copyOfRange(data, 1, data.length)));
            }
            return "200";
        } else {
            return "403";
        }
    }

    /**
     * Gets a file from the storage
     * @param name the name of the file to be retrieved
     * @return the status code of the operation + the content read from the file if the code is 200
     */
    private static String getFile(String name) throws IOException {
        String filepath = String.format(storageFolder, name);
        File file = new File(filepath);
        if (file.exists()) {
            try (Reader reader = new FileReader(file)) {
                StringBuilder string = new StringBuilder("200 ");
                while (true) {
                    int readChar = reader.read();
                    if (readChar == -1) {
                        break;
                    } else {
                        string.append((char) readChar);
                    }
                }
                return string.toString();
            }
        } else {
            return "404";
        }
    }

    /**
     * Deletes a file from the storage
     * @param name the name of the file to be deleted
     * @return the status code of the operation
     */
    private static String deleteFile(String name) {
        String filepath = String.format(storageFolder, name);
        File file = new File(filepath);
        if (file.delete()) {
            return "200";
        } else {
            return "404";
        }
    }
}