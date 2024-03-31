package server.main;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {
    private static final List<String> storage = new ArrayList<>();
    private static final CommandInterpreter interpreter = new CommandInterpreter();
    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        // The entire runtime of the program
        inputLoop();
    }

    /**
     * The entire runtime of the program where the user continuously inputs commands until they input exit.
     */
    private static void inputLoop() {
        inputloop:
        while (true) {
            String[] command = scanner.nextLine().split(" ");
            // Contains the information about how to act upon the command that the user inputted
            Interpretation interpretation = interpreter.interpret(command);
            // The id is the operation id. It shows which operation should be executed
            switch (interpretation.getId()) {
                // Add file
                case 1:
                    // getData()[0] for all of these is the name of the file
                    // The if statement checks if the operation was successful. That is the same for all of these
                    if (addFile(interpretation.getData()[0])) {
                        System.out.printf("The file %s added successfully\n", interpretation.getData()[0]);
                    } else {
                        System.out.printf("Cannot add the file %s\n", interpretation.getData()[0]);
                    }
                    break;
                // Get file
                case 2:
                    if (getFile(interpretation.getData()[0])) {
                        System.out.printf("The file %s was sent\n", interpretation.getData()[0]);
                    } else {
                        System.out.printf("The file %s not found\n", interpretation.getData()[0]);
                    }
                    break;
                // Delete file
                case 3:
                    if (deleteFile(interpretation.getData()[0])) {
                        System.out.printf("The file %s was deleted\n", interpretation.getData()[0]);
                    } else {
                        System.out.printf("The file %s not found\n", interpretation.getData()[0]);
                    }
                    break;
                // Exit system
                case 0:
                    break inputloop;
                // User input an incorrect operation
                case -1:
                    System.out.println("Invalid command. You must choose the add, get, delete, or exit command.");
                    break;
                // Somehow, the operation id is wrong, probably due to a programming error which I may make in the future
                default:
                    System.out.println("Could not act upon interpretation. Operation id is invalid.");
            }
        }
    }

    /**
     * Adds a file to the storage
     * @param name the name of the file which should be added
     * @return whether or not adding the file was successful
     */
    private static boolean addFile(String name) {
        // Checks if the storage already contains the file, the storage is full, or if the file name is incorrect (not "file1" through "file10")
        if (storage.contains(name) || storage.size() == 10 || !(name.matches("file[0-9]|file10"))) {
            return false;
        } else {
            storage.add(name);
            return true;
        }
    }

    /**
     * Gets a file from the storage
     * @param name the name of the file to be retrieved
     * @return whether or not the retrieval was successful
     */
    private static boolean getFile(String name) {
        return storage.contains(name);
    }

    /**
     * Deletes a file from the storage
     * @param name the name of the file to be deleted
     * @return whether or not the deletion was successful
     */
    private static boolean deleteFile(String name) {
        if (storage.contains(name)) {
            storage.remove(name);
            return true;
        } else {
            return false;
        }
    }
}