package gitlet;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.TreeMap;

public class Commit implements Serializable {
    public Commit(String message, Commit parent1,
                  TreeMap<String, String> tree, Commit parent2) {
        _message = message;
        DateTimeFormatter timeFormat = DateTimeFormatter
                .ofPattern("EEE MMM dd HH:mm:ss yyyy" + " -0800");
        if (Objects.equals(_message, "initial commit")) {
            _timestamp = "Thu Jan 1 00:00:00 1970 -0800";
        } else {
            _timestamp = LocalDateTime.now().format(timeFormat);
        }
        _parent = parent1;
        _parent2 = parent2;
        _tree = tree;
    }

    public String getMessage() {
        return _message;
    }

    public String getTimestamp() {
        return _timestamp;
    }

    public Commit getParent() {
        return _parent;
    }

    public Commit getParent2() {
        return _parent2;
    }

    public TreeMap<String, String> getTree() {
        return _tree;
    }

    public String getSha1() {
        return Utils.sha1(_message, _timestamp);
    }

    /**
     * TreeMap.
     */
    private TreeMap<String, String> _tree;

    /**
     * Message.
     */
    private String _message;

    /**
     * Timestamp.
     */
    private String _timestamp;

    /**
     * Parent.
     */
    private Commit _parent;

    /**
     * Parent2.
     */
    private Commit _parent2;
}
