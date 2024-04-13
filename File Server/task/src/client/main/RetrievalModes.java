package client.main;

/**
 * An enum which keeps track of which retrieval modes are correct. 1 is get or delete by name, 2 is get or delete by id.
 */
enum RetrievalModes {
    NAME(1), ID(2);

    private final int id;

    RetrievalModes(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }
}
