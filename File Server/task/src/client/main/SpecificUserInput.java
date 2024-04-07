package client.main;

import java.util.NoSuchElementException;
import java.util.Scanner;

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
                Scanner scannerTemp = new Scanner(System.in);
                id = scannerTemp.next();
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
}
