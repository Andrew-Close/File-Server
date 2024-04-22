package server.main;

import javax.xml.crypto.Data;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static config.Config.CLIENT_STORAGE_FOLDER;
import static config.Config.SERVER_STORAGE_FOLDER;

public class Main {
    private static final CommandInterpreter interpreter = new CommandInterpreter();
    // Contains the id to filename pairs. Keeps track of which files on the server belong to which id
    private static final IDMap idMap = new IDMap();
    private static final ExecutorService threadPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    public static void main(String[] args) throws IOException, InterruptedException {
        // The entire runtime of the server
        serverRuntime();
    }

    /**
     * The entire runtime of the server where the user continuously inputs commands until they input exit.
     */
    private static void serverRuntime() throws IOException, InterruptedException {
        try (ServerSocket server = new ServerSocket(23456))
        {
            System.out.println("Server started!");
            serverloop:
            while (true) {
                //Thread.sleep(2000L);
                Socket socket = server.accept();
                threadPool.submit(new Session(socket));
            }
        }
    }

    private static class Session implements Callable<Void> {
        private final Socket socket;

        Session(Socket socket) {
            this.socket = socket;
        }

        @Override
        public Void call() throws Exception {
            try (DataInputStream input = new DataInputStream(socket.getInputStream());
                 DataOutputStream output = new DataOutputStream(socket.getOutputStream()))
            {
                String[] command = input.readUTF().split(" ");
                // Contains the information about how to act upon the command that the user inputted
                Interpretation interpretation = interpreter.interpret(command);
                // The id is the operation id. It shows which operation should be executed
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
                    // Exit system
                    case 0:
                        //
                        //
                        // Add exit implementation later !!
                        //
                        //
                        System.out.println("Placeholder.");
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
     * A callable which adds a file to the server storage.
     */
    private static class addFile implements Callable<Void> {
        private final String[] data;
        private final DataOutputStream clientOutput;

        addFile(String[] data, DataOutputStream clientOutput) {
            this.data = data;
            this.clientOutput = clientOutput;
        }

        /**
         * The method which adds a file to the storage.
         * @return the status code of the operation
         */
        @Override
        public Void call() throws IOException {
            String clientFilePath = String.format(CLIENT_STORAGE_FOLDER, data[0]);
            // Checks if a name was passed in the data (data[1] is not equal to ""). If not, a name is automatically generated
            String serverFileName = "".equals(data[1]) ? generateFileName(data[2]) : data[1];
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
                idMap.addPair(serverFileName);
                clientOutput.writeBytes("200 " + idMap.getIDByName(serverFileName));
            } else {
                clientOutput.writeBytes("403");
            }
            return null;
        }
    }

    /**
     * A callable which gets a file from the server storage.
     */
    private static class getFile implements Callable<Void> {
        private final String identifier;
        private final DataOutputStream clientOutput;
        getFile(String identifier, DataOutputStream clientOutput) {
            this.identifier = identifier;
            this.clientOutput = clientOutput;
        }

        /**
         * The method which gets a file to the storage.
         */
        @Override
        public Void call() throws IOException {
            String serverFilePath;
            // If the identifier is an integer id
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
            return null;
        }

    }

    /**
     * A callable which deletes a file from the server storage.
     */
    private static class deleteFile implements Callable<Void> {
        private final String name;
        private final DataOutputStream clientOutput;

        deleteFile(String identifier, DataOutputStream clientOutput) {
            this.name = identifier;
            this.clientOutput = clientOutput;
        }
        /**
         * The method which deletes a file from the storage.
         *  @return the status code of the operation
         */
        @Override
        public Void call() throws IOException {
            String filepath;
            // Used for the filepath but also for the deletion of the pair in the idmap
            String filename;
            // ID is set to -1 because, if it doesn't change, then the pair in the idmap is deleted by name, and if it does change, it's deleted by id
            int id = -1;
            if (name.matches("[0-9]+")) {
                filename = idMap.getByID(Integer.parseInt(name));
                id = Integer.parseInt(name);
                filepath = String.format(SERVER_STORAGE_FOLDER, filename);
            } else {
                filename = name;
                filepath = String.format(SERVER_STORAGE_FOLDER, name);
            }
            File file = new File(filepath);
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
            return null;
        }
    }

    private static String addFile(String[] data, DataOutputStream clientOutput) throws IOException {
        String clientFilePath = String.format(CLIENT_STORAGE_FOLDER, data[0]);
        // Checks if a name was passed in the data (data[1] is not equal to ""). If not, a name is automatically generated
        String serverFileName = "".equals(data[1]) ? generateFileName(data[2]) : data[1];
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
            idMap.addPair(serverFileName);
            clientOutput.writeUTF("200 " + idMap.getIDByName(serverFileName));
        } else {
            clientOutput.writeUTF("403");
        }
        return null;
    }
    /**
     * Gets a file from the storage
     * @param identifier the name of the file to be retrieved
     */
    private static void getFile(String identifier, DataOutputStream clientOutput) throws IOException {
        String serverFilePath;
        // If the identifier is an integer id
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
     * @return the status code of the operation
     */
    private static String deleteFile(String name, DataOutputStream clientOutput) throws IOException {
        String filepath;
        // Used for the filepath but also for the deletion of the pair in the idmap
        String filename;
        // ID is set to -1 because, if it doesn't change, then the pair in the idmap is deleted by name, and if it does change, it's deleted by id
        int id = -1;
        if (name.matches("[0-9]+")) {
            filename = idMap.getByID(Integer.parseInt(name));
            id = Integer.parseInt(name);
            filepath = String.format(SERVER_STORAGE_FOLDER, filename);
        } else {
            filename = name;
            filepath = String.format(SERVER_STORAGE_FOLDER, name);
        }
        File file = new File(filepath);
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
        return null;
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