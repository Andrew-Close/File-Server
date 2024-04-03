package client.main;

import java.util.Scanner;

/**
 * Class for handling user input which is specific to the program.
 */
public class SpecificUserInput {
    /**
     * Enum which keeps track of which action ids are correct.
     */
    private enum Actions {
        GET(1), CREATE(2), DELETE(3);
        private final int id;
        Actions(int id) {
            this.id = id;
        }
        public int getId() {
            return id;
        }
    }
    private final Scanner scanner = new Scanner(System.in);

    /**
     * Gets a valid action id from the user and returns it as a String. Uses the Actions enum to check for validity.
     * @return the action as a String to be used in the request to the server
     */
    String getValidAction() {
        System.out.print("Enter action (1 - get a file, 2 - create a file, 3 - delete a file): ");
        while (true) {
            int id = scanner.nextInt();
            for (Actions action : Actions.values()) {
                if (action.getId() == id) {
                    // Sets System.out on a newline rather than on the same line as the previous message
                    System.out.println();
                    return switch (id) {
                        case 1 -> "GET";
                        case 2 -> "PUT";
                        case 3 -> "DELETE";
                        default -> "NULL";
                    };
                }
            }
            System.out.print("\nInvalid action. Choose from 1 (get file), 2 (create file), or 3 (delete file).");
        }
    }
}
