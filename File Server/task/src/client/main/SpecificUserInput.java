package client.main;

import java.util.Scanner;

/**
 * Class for handling user input which is specific to the program
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
     * Gets a valid action id. Uses the Actions enum to check for validity.
     * @return the id
     */
    int getValidAction() {
        while (true) {
            int id = scanner.nextInt();
            for (Actions action : Actions.values()) {
                if (action.getId() == id) {
                    return id;
                }
            }
        }
    }
}
