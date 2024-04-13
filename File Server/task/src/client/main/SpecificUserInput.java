package client.main;

import server.main.IDMap;
import server.main.Main;

import java.io.File;
import java.util.NoSuchElementException;
import java.util.Scanner;

import static config.Config.CLIENT_STORAGE_FOLDER;
import static server.main.Main.idMap;

/**
 * Class for handling user input which is specific to the program.
 */
public class SpecificUserInput {
    /**
     * Gets a valid action id from the user and returns it as a String. Uses the Actions enum to check for validity.
     * @return the action as a String to be used in the request to the server
     */
    String getValidAction() {
        System.out.print("Enter action (1 - get a file, 2 - create a file, 3 - delete a file): ");
        while (true) {
            String id;
            try {
                Scanner scanner = new Scanner(System.in);
                id = scanner.next();
            // Catch if the user doesn't input an integer
            } catch (NoSuchElementException e) {
                System.out.print("Invalid action. Choose from 1 (get file), 2 (create file), or 3 (delete file). ");
                continue;
            }
            for (Actions action : Actions.values()) {
                if (action.getId().equals(id)) {
                    return action.toString();
                }
            }
            System.out.print("Invalid action. Choose from 1 (get file), 2 (create file), or 3 (delete file). ");
        }
    }

    /**
     * Gets a valid retrieval mode from the user. Uses the RetrievalModes enum to check for validity. System query is different depending on whether the user wants to get or delete.
     * @param action the action which specifies if the user wants to get a file or delete a file
     * @return the retrieval mode
     */
    int getValidRetrievalMode(Actions action) {
        Scanner scanner = new Scanner(System.in);
        // Different system printing depending on the specified action
        if (action.equals(Actions.GET)) {
            System.out.print("Do you want to get the file by name or by id (1 - name, 2 - id): ");
        } else if (action.equals(Actions.DELETE)) {
            System.out.print("Do you want to delete the file by name or by id (1 - name, 2 - id): ");
        }
        while (true) {
            int retrievalMode = scanner.nextInt();
            // Checking validity
            for (RetrievalModes retrievalModeEnum : RetrievalModes.values()) {
                if (retrievalMode == retrievalModeEnum.getId()) {
                    return retrievalMode;
                }
            }
            System.out.print("Invalid retrieval mode. Choose from 1 (get using name) or 2 (get using id). ");
        }
    }

    /**
     * Gets a valid local filename from the user. The file must be in the client/data folder. Also, the format must be specified in the user input.
     * @return the filename
     */
    String getExistingLocalFile() {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter name of the file: ");
        while (true) {
            String filename = scanner.next();
            // Checks if the file is in the client/data folder
            if (new File(String.format(CLIENT_STORAGE_FOLDER, filename)).exists()) {
                return filename;
            }
            System.out.print("That file does not exist. Make sure the file is in the client/data folder and type it again. ");
        }
    }

    /**
     * Gets a valid filename from the user to be used as the name of a file to be put on the server. The format must be specified in the user input.
     * @return the filename
     */
    String getNewServerFileName() {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter name of the file to be saved on the server: ");
        String filename = scanner.nextLine();
        // If the user did not input a name, a name is automatically generated
        if (filename.isEmpty()) {
            return "no_name_id_" + idMap.getLeastAvailableID();
        } else {
            return filename;
        }
    }
}
