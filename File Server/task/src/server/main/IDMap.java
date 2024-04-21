package server.main;

import java.util.HashMap;
import java.util.Map;

/**
 * Class which contains a hashmap containing all the id to filename pairs. Also contains methods for adding, getting, and deleting pairs.
 */
public class IDMap {
    final HashMap<Integer, String> idMap;

    IDMap() {
        this.idMap = new HashMap<>();
    }

    /**
     * Adds a new id to filename pair in the map. The id is automatically generated.
     * @param filename the filename which is being saved on the server and should also be saved on the map.
     */
    synchronized void addPair(String filename) {
        idMap.putIfAbsent(getLeastAvailableID(), filename);
        //
        //
        //
        // For debugging, REMOVE WHEN DONE
        // |
        // |
        // V
        System.out.println(idMap);
    }

    /**
     * Gets the name of a file, including the format, by the id of the file.
     * @param id the id of the file
     * @return the filename which corresponds to the passed id
     */
    synchronized String getByID(int id) {
        return idMap.get(id);
    }

    /**
     * Gets the id of a file by the name of the file.
     * @param filename the filename
     * @return the id of the passed filename
     */
    synchronized int getIDByName(String filename) {
        for (Map.Entry<Integer, String> entry : idMap.entrySet()) {
            if (entry.getValue().equals(filename)) {
                return entry.getKey();
            }
        }
        return -1;
    }

    /**
     * Deletes an id to filename pair using the id of the pair.
     * @param id the id of the pair which should be deleted
     */
    synchronized void deleteByID(int id) {
        idMap.remove(id);
    }

    /**
     * Deletes an id to filename pair using the filename of the pair.
     * @param filename the filename of the pair which should be deleted
     */
    synchronized void deleteByName(String filename) {
        for (Map.Entry<Integer, String> entry : idMap.entrySet()) {
            if (entry.getValue().equals(filename)) {
                idMap.remove(entry.getKey());
                break;
            }
        }
    }

    /**
     * Gets the smallest available id. If, for example, there are pairs with ids 0, 1, 2, and 4, then this method will return 3.
     * @return the smallest available id
     */
    synchronized int getLeastAvailableID() {
        int largestID = getLargestID();
        // When largestID is -1, that means that idMap is empty
        if (largestID == -1) {
            return 0;
        } else {
            // Iterates through the range of 0 - the largest taken id and returns the first available id it finds
            for (int i = 0; i < largestID; i++) {
                if (!idMap.containsKey(i)) {
                    return i;
                }
            }
            return largestID + 1;
        }
    }

    /**
     * Gets the largest id which is being used. If the map is empty, then it returns -1.
     * @return the largest id in use if the map is not empty, or -1 if it is empty
     */
    synchronized private int getLargestID() {
        if (!idMap.isEmpty()) {
            int id = 0;
            for (Map.Entry<Integer, String> entry : idMap.entrySet()) {
                if (entry.getKey() > id) {
                    id = entry.getKey();
                }
            }
            return id;
        } else {
            return -1;
        }
    }

    void printMap() {
        System.out.println(idMap);
    }
}
