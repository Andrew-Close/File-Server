package server;

import java.util.ArrayList;
import java.util.List;

/**
 * Simply a class for holding information about an interpretation.
 */
public class Interpretation {
    private final int id;
    private String[] data = new String[1];
    public Interpretation(int id) {
        this.id = id;
    }

    public Interpretation(int id, String[] data) {
        this.id = id;
        this.data = data;
    }

    public int getId() {
        return id;
    }

    public String[] getData() {
        return data;
    }
}
