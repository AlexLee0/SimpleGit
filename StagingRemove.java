package gitlet;

import java.io.Serializable;
import java.util.HashMap;

public class StagingRemove implements Serializable {

    public StagingRemove() {
        stagingRemoveHash = new HashMap<>();
    }

    public void addToRemoveHash(String file, String serial) {
        stagingRemoveHash.put(file, serial);
    }

    public void clearRemoveHash() {
        stagingRemoveHash = new HashMap<>();
    }

    public HashMap<String, String> getStagingRemoveHash() {
        return stagingRemoveHash;
    }

    /**
     * Hashmap for stage.
     */
    private HashMap<String, String> stagingRemoveHash;

    public boolean contains(String fileName) {
        return stagingRemoveHash.containsKey(fileName);
    }

    public void removeFromRemoveHash(String filename) {
        stagingRemoveHash.remove(filename);
    }

    public String getBlob(String fileName, HashMap<String, String> hash) {
        return hash.get(fileName);
    }
}

