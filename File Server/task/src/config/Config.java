package config;

/**
 * A class for holding configuration.
 */
public class Config {
    // Networking configuration
    public static final String ADDRESS = "127.0.0.1";
    public static final int PORT = 23456;

    // File management
    /**
     * This constant needs to be used with formatting because of the %s at the end.
     */
    //
    //
    // Use this when testing: src\server\data\%s
    //
    //
    public static final String SERVER_STORAGE_FOLDER = "File Server\\task\\src\\server\\data\\%s";
    /**
     * This constant needs to be used with formatting because of the %s at the end.
     */
    //
    //
    // Use this when testing: src\client\data\%s
    //
    //
    public static final String CLIENT_STORAGE_FOLDER = "File Server\\task\\src\\client\\data\\%s";
}
