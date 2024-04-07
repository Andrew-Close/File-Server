package client.main;

/**
 * Enum which keeps track of which action names and ids are correct.
 */
enum Actions {
    GET("1"), PUT("2"), DELETE("3"), EXIT("exit");
    private final String id;
    Actions(String id) {
        this.id = id;
    }
    String getId() {
        return id;
    }
}
