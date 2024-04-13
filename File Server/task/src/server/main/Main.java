package server.main;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

import static config.Config.CLIENT_STORAGE_FOLDER;
import static config.Config.SERVER_STORAGE_FOLDER;

public class Main {
    private static final CommandInterpreter interpreter = new CommandInterpreter();
    // Contains the id to filename pairs. Keeps track of which files on the server belong to which id
    public static final IDMap idMap = new IDMap();

    public static void main(String[] args) throws IOException {
        // The entire runtime of the server
        serverRuntime();
    }

    /**
     * The entire runtime of the server where the user continuously inputs commands until they input exit.
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
        String clientFilePath = String.format(CLIENT_STORAGE_FOLDER, data[0]);
        String serverFilePath = String.format(SERVER_STORAGE_FOLDER, data[1]);
        File clientFile = new File(clientFilePath);
        File serverFile = new File(serverFilePath);
        if (serverFile.createNewFile()) {
            // Reads from the file, stores the data in a buffer, then writes to the server file location using the data from the buffer. Input stream -> Buffer -> Output stream
            try (InputStream input = new FileInputStream(clientFile);
                 BufferedInputStream buffer = new BufferedInputStream(input);
                 OutputStream output = new FileOutputStream(serverFile)) {
                output.write(buffer.readAllBytes());
            }
            // Gives a new id to the server filename and returns it in the status code
            idMap.addPair(data[1]);
            return "200 " + idMap.getIDByName(data[1]);
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
        String filepath = String.format(SERVER_STORAGE_FOLDER, name);
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
        String filepath = String.format(SERVER_STORAGE_FOLDER, name);
        File file = new File(filepath);
        if (file.delete()) {
            return "200";
        } else {
            return "404";
        }
    }
}