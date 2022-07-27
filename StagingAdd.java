package gitlet;

import java.util.HashMap;
import java.io.Serializable;

public class StagingAdd implements Serializable {

    /**
     * Hashmap for stage.
     */
    private HashMap<String, String> stagingAddHash;

    public StagingAdd() {
        stagingAddHash = new HashMap<>();
    }

    public void addToAddHash(String file, String blob) {
        stagingAddHash.put(file, blob);
    }

    public void clearAddHash() {
        stagingAddHash = new HashMap<>();
    }

    public HashMap<String, String> getStagingAddHash() {
        return stagingAddHash;
    }

    public boolean contains(String fileName) {
        return stagingAddHash.containsKey(fileName);
    }
    public void removeFromAddHash(String filename) {
        stagingAddHash.remove(filename);
    }

    public String getBlob(String fileName, HashMap<String, String> hash) {
        return hash.get(fileName);
    }
}
