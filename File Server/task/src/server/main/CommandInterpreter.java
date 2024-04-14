package server.main;

import java.util.Arrays;

/**
 * Interprets given commands. Returns data and values corresponding to what the program should do with the data
 */
public class CommandInterpreter {
    /**
     * The method that actually interprets commands. It returns a String array that represents what operation Main should execute and any data that it might need. For example, if the operation
     * is "PUT", then it returns an array with the operation identifier of 1 and the name of the file that the program should add. But if the operation is "EXIT", then it returns an array with
     * only the operation identifier of 0, since the program doesn't need any additional data.
     * @param command the String array of each "word" in the command
     * @return the String array which tells Main what to do
     */
    Interpretation interpret(String[] command) {
        String operation = command[0];
        return switch (operation) {
            case "GET_BY_NAME", "GET_BY_ID" -> new Interpretation(1, new String[]{command[1]});
            case "PUT" -> new Interpretation(2, Arrays.copyOfRange(command, 1, command.length));
            case "DELETE_BY_NAME", "DELETE_BY_ID" -> new Interpretation(3, new String[]{command[1]});
            case "EXIT" -> new Interpretation(0);
            default -> new Interpretation(-1);
        };
    }
}
