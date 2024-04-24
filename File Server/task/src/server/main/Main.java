package server.main;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static config.Config.*;

public class Main {
    // ServerSocket is a field so a session can access it and shutdown the server
    private static ServerSocket server;
    private static final CommandInterpreter interpreter = new CommandInterpreter();
    // Contains the id to filename pairs. Keeps track of which files on the server belong to which id
    private static IDMap idMap;
    // Sessions are submitted to this thread pool
    private static final ExecutorService threadPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        // Deserialization of idmap
        File idMapFile = new File(IDMAP_FILEPATH);
        if (idMapFile.exists()) {
            idMap = (IDMap) IDMap.deserialize();
            // In case the server tries to load an old version of the map
            idMapFile.delete();
        } else {
            idMap = new IDMap();
        }
        // The entire runtime of the server
        serverRuntime();
    }

    /**
     * The entire runtime of the server where connections are established and the user continuously inputs commands until they input exit. Connections are handled in a session
     * and the sessions are submitted to the thread pool.
     */
    private static void serverRuntime() throws IOException {
        server = new ServerSocket(PORT);
        System.out.println("Server started!");
        while (true) {
            Socket socket = server.accept();
            threadPool.submit(new Session(socket));
        }
    }

    /**
     * This is where commands are received and acted upon. The socket is closed at the end. If "exit" is received, it closes the server.
     * @param socket the socket for interaction with the client
     */
    private record Session(Socket socket) implements Callable<Void> {

        @Override
            public Void call() throws Exception {
                try (DataInputStream input = new DataInputStream(socket.getInputStream());
                     DataOutputStream output = new DataOutputStream(socket.getOutputStream())) {
                    String[] command = input.readUTF().split(" ");
                    // Contains the information about how to act upon the command that the user inputted
                    Interpretation interpretation = interpreter.interpret(command);
                    // Executes operations depending on the id
                    switch (interpretation.getId()) {
                        // Get file
                        case 1:
                            getFile(interpretation.getData()[0], output);
                            break;
                        // Add file
                        case 2:
                            addFile(interpretation.getData(), output);
                            break;
                        // Delete file
                        case 3:
                            deleteFile(interpretation.getData()[0], output);
                            break;
                        // Exit system. Closes socket first, then the server, then serializes the id map, then uses System.exit
                        case 0:
                            socket.close();
                            server.close();
                            idMap.serialize(idMap);
                            System.exit(0);
                            break;
                        // User input an incorrect operation
                        case -1:
                            output.writeUTF("Invalid command. You must choose the add, get, delete, or exit command.");
                            break;
                        // Somehow, the operation id is wrong, probably due to a programming error which I may make in the future
                        default:
                            output.writeUTF("Could not act upon interpretation. Operation id is invalid.");
                    }
                }
                socket.close();
                return null;
            }
        }

    /**
     * Adds a file which is contained on the client's data folder to the server.
     * @param data contains information about filenames. First the client filename, then the to-be server filename, and lastly the format, including the period.
     * @param clientOutput the output stream to which to write the status code.
     * @throws IOException;
     */
    private static void addFile(String[] data, DataOutputStream clientOutput) throws IOException {
        // File to be added
        String clientFilePath = String.format(CLIENT_STORAGE_FOLDER, data[0]);
        // Checks if a name was passed in the data (data[1] is not equal to ""). If not, a name is automatically generated
        String serverFileName = "".equals(data[1]) ? generateFileName(data[2]) : data[1];
        String serverFilePath = String.format(SERVER_STORAGE_FOLDER, serverFileName);
        File clientFile = new File(clientFilePath);
        File serverFile = new File(serverFilePath);
        // Creates the server file location if it doesn't exist
        if (serverFile.createNewFile()) {
            // Reads from the file, stores the data in a buffer, then writes to the server file location using the data from the buffer. Input stream -> Buffer -> Output stream
            try (InputStream input = new FileInputStream(clientFile);
                 BufferedInputStream buffer = new BufferedInputStream(input);
                 OutputStream output = new FileOutputStream(serverFile)) {
                output.write(buffer.readAllBytes());
            }
            // Gives a new id to the server filename and writes it alongside the status code
            idMap.addPair(serverFileName);
            clientOutput.writeUTF("200 " + idMap.getIDByName(serverFileName));
        } else {
            clientOutput.writeUTF("403");
        }
    }
    /**
     * Gets a file from the storage. The file contents are written to the output stream as bytes.
     * @param identifier the name of the file to be retrieved.
     * @param clientOutput the output stream to which to write the status code and the file contents.
     */
    private static void getFile(String identifier, DataOutputStream clientOutput) throws IOException {
        String serverFilePath;
        // Checks if getting by id or by name
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
                clientOutput.writeUTF("200");
                clientOutput.writeInt(buffer.available());
                clientOutput.write(buffer.readAllBytes());
            }
        } else {
            clientOutput.writeUTF("404");
        }
    }

    /**
     * Deletes a file from the storage
     * @param name the name of the file to be deleted or the id of the file
     * @param clientOutput the output stream to which to write the status code and the file contents.
     */
    private static void deleteFile(String name, DataOutputStream clientOutput) throws IOException {
        String filepath;
        // Used for the filepath but also for the deletion of the pair in the idmap
        String filename;
        // ID is set to -1 because, if it doesn't change, then the pair in the idmap is deleted by name, and if it does change, it's deleted by id
        int id = -1;
        // Checks if trying to delete by id or by name
        if (name.matches("[0-9]+")) {
            filename = idMap.getByID(Integer.parseInt(name));
            id = Integer.parseInt(name);
            filepath = String.format(SERVER_STORAGE_FOLDER, filename);
        } else {
            filename = name;
            filepath = String.format(SERVER_STORAGE_FOLDER, name);
        }
        File file = new File(filepath);
        // Checks if the file existed and was subsequently deleted
        if (file.delete()) {
            if (id == -1) {
                idMap.deleteByName(filename);
            } else {
                idMap.deleteByID(id);
            }
            clientOutput.writeUTF("200");
        } else {
            clientOutput.writeUTF("404");
        }
    }

    /**
     * Automatically generates a name for a file to be called on the server if the user did not input a custom name.
     * @param format the file format of the file being saved
     * @return the name of the file including the format
     */
    private static String generateFileName(String format) {
        return "no_name_id_" + idMap.getLeastAvailableID() + format;
    }
}