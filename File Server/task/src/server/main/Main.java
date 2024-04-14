package server.main;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.Scanner;

import static config.Config.CLIENT_STORAGE_FOLDER;
import static config.Config.SERVER_STORAGE_FOLDER;

public class Main {
    private static final CommandInterpreter interpreter = new CommandInterpreter();
    // Contains the id to filename pairs. Keeps track of which files on the server belong to which id
    private static IDMap idMap;

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
                if (idMap == null) {
                    idMap = new IDMap();
                }
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
                        output.writeBytes(statusCode);
                    }
                }
            }
        }
    }

    /**
     * Adds a file to the storage
     * @param data the data of the interpretation. For this method, data[0] is the locally saved file, data[1] is the name which will go on the server, and data[2] is the file format
     * @return the status code of the operation
     */
    private static String addFile(String[] data) throws IOException {
        // Checks if a name was passed in the data (data[1] is not equal to ""). If not, a name is automatically generated
        String serverFileName = "".equals(data[1]) ? generateFileName(data[2]) : data[1];
        String clientFilePath = String.format(CLIENT_STORAGE_FOLDER, data[0]);
        String serverFilePath = String.format(SERVER_STORAGE_FOLDER, serverFileName);
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
     * @param identifier the name of the file to be retrieved
     * @return the status code of the operation + the content read from the file if the code is 200
     */
    private static String getFile(String identifier) throws IOException {
        String serverFilePath;
        // If the identifier is an integer
        if (identifier.matches("[0-9]+")) {
            String filename = idMap.getByID(Integer.parseInt(identifier));
            serverFilePath = String.format(SERVER_STORAGE_FOLDER, filename);
        } else {
            serverFilePath = String.format(SERVER_STORAGE_FOLDER, identifier);
        }
        File serverFile = new File(serverFilePath);
        if (serverFile.exists()) {
            try (InputStream input = new FileInputStream(serverFile);
                InputStream buffer = new BufferedInputStream(input)) {
                Scanner scanner = new Scanner(System.in);
                System.out.print("The file was downloaded! Specify a name for it: ");
                String clientFilePath = String.format(CLIENT_STORAGE_FOLDER, scanner.nextLine());
                File clientFile = new File(clientFilePath);
                try (OutputStream output = new FileOutputStream(clientFile)) {
                    output.write(buffer.readAllBytes());
                    return "200";
                }
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

    /**
     * Automatically generated a name for a file to be called on the server if the user did not input a custom name.
     * @param format the file format of the file being saved
     * @return the name of the file including the format
     */
    private static String generateFileName(String format) {
        return "no_name_id_" + idMap.getLeastAvailableID() + format;
    }
}