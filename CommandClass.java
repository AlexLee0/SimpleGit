package gitlet;

import java.io.File;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.Set;

public class CommandClass {

    /**
     * Length.
     **/
    public static final int LENGTH = 40;

    /**
     * Head String.
     **/
    private String head = "master";

    /**
     * Stage Add.
     **/
    private StagingAdd stageAdd;

    /**
     * Stage Remove.
     **/
    private StagingRemove stageRemove;

    /**
     * Current Working Directory.
     **/
    private File cwd;

    /**
     * toHead.
     **/
    private File toHead = new File(".gitlet/HEAD");


    public CommandClass() {
        cwd = new File(System.getProperty("user.dir"));
        if (toHead.exists()) {
            head = Utils.readContentsAsString(toHead);
        }
    }

    public void init() {
        File gitletFolder = new File(".gitlet");
        if (!gitletFolder.exists()) {
            gitletFolder.mkdir();

            File commitDir = Utils.join(gitletFolder, "commits");
            commitDir.mkdir();

            File globalLogDir = Utils.join(gitletFolder, "globalLog");
            globalLogDir.mkdir();

            File branchDir = Utils.join(gitletFolder, "branches");
            branchDir.mkdir();

            stageAdd = new StagingAdd();
            stageRemove = new StagingRemove();

            Commit initCommit = new Commit("initial commit",
                    null, new TreeMap<>(), null);
            File toInitCommit = Utils.join(commitDir,
                    Utils.sha1(initCommit.getMessage(),
                            initCommit.getTimestamp()));
            Utils.writeObject(toInitCommit, initCommit);
            Utils.writeObject(new File(".gitlet/globalLog/"
                    + Utils.sha1(initCommit.getMessage(),
                    initCommit.getTimestamp())), initCommit);

            File toStageAdd = new File(".gitlet/stagingAdd");
            Utils.writeObject(toStageAdd, stageAdd);

            File toStageRemove = new File(".gitlet/stagingRemove");
            Utils.writeObject(toStageRemove, stageRemove);

            File toHEAD = Utils.join(gitletFolder, "HEAD");
            Utils.writeContents(toHEAD, "master");

            File toMaster = Utils.join(branchDir, "master");
            Utils.writeObject(toMaster, Utils.readObject
                    (toInitCommit, Commit.class));

        } else {
            System.out.print("A Gitlet version-control system "
                    + "already exists in the current directory.");
        }
    }

    public void add(String file) {
        File toFile = new File(file);
        if (!toFile.exists()) {
            System.out.print("File does not exist.");
            return;
        }
        String blob = Utils.readContentsAsString(toFile);
        stageAdd = Utils.readObject(new File(
                ".gitlet/stagingAdd"), StagingAdd.class);
        stageRemove = Utils.readObject(new File(
                ".gitlet/stagingRemove"), StagingRemove.class);
        if (stageRemove.getStagingRemoveHash().containsKey(file)) {
            stageRemove.removeFromRemoveHash(file);
            Utils.writeObject(new File(
                    ".gitlet/stagingRemove"), stageRemove);
            return;
        }
        if (headCommit().getTree().get(file) != null) {
            String headBlob = headCommit().getTree().get(file);
            if (headBlob.equals(blob)) {
                if (stageAdd.contains(file)) {
                    stageAdd.removeFromAddHash(file);
                    Utils.writeObject(new File(
                            ".gitlet/stagingAdd"), stageAdd);
                    return;
                }
                return;
            }
        }
        stageAdd.getStagingAddHash().put(file, blob);
        Utils.writeObject(new File(".gitlet/stagingAdd"), stageAdd);
    }

    public void commit(String message, Boolean isMerge, Commit parent2) {
        stageAdd = Utils.readObject(new File(
                ".gitlet/stagingAdd"), StagingAdd.class);
        stageRemove = Utils.readObject(new File(
                ".gitlet/stagingRemove"), StagingRemove.class);
        Commit newCommit = null;
        if (stageAdd.getStagingAddHash().size() == 0
                && stageRemove.getStagingRemoveHash().size() == 0) {
            System.out.print("No changes added to the commit.");
            return;
        }
        if (message.equals("")) {
            System.out.print("Please enter a commit message.");
            return;
        }
        if (!isMerge) {
            newCommit = new Commit(message, headCommit(),
                    headCommit().getTree(), null);
        }
        if (isMerge) {
            newCommit = new Commit(message, headCommit(),
                    headCommit().getTree(), parent2);
        }
        File toNewCommit = Utils.join(".gitlet/commits",
                Utils.sha1(newCommit.getMessage(), newCommit.getTimestamp()));
        Utils.writeObject(new File(".gitlet/globalLog/"
                + Utils.sha1(newCommit.getMessage(),
                newCommit.getTimestamp())), newCommit);
        stageAdd = Utils.readObject(new File(
                ".gitlet/stagingAdd"), StagingAdd.class);
        stageRemove = Utils.readObject(new File(
                ".gitlet/stagingRemove"), StagingRemove.class);
        for (String adds : stageAdd.getStagingAddHash().keySet()) {
            String blob = stageAdd.getStagingAddHash().get(adds);
            newCommit.getTree().put(adds, blob);
        }

        for (String removes : stageRemove.getStagingRemoveHash().keySet()) {
            newCommit.getTree().remove(removes);
        }

        Utils.writeObject(toNewCommit, newCommit);
        Utils.writeObject(new File(".gitlet/branches/" + head), newCommit);

        stageAdd.clearAddHash();
        Utils.writeObject(Utils.join(".gitlet/stagingAdd"), stageAdd);
        stageRemove.clearRemoveHash();
        Utils.writeObject(Utils.join(".gitlet/stagingRemove"), stageRemove);
    }

    public void checkout3Args(String[] args) {
        if ((new File(args[2])).exists()) {
            String headBlob = headCommit().getTree().get(args[2]);
            File toCWDFile = new File(args[2]);
            Utils.writeContents(toCWDFile, headBlob);
        } else {
            System.out.print("File does not exist in that commit.");
        }
    }

    public void checkout4Args(String[] args) {
        if (!args[2].equals("--")) {
            System.out.print("Incorrect operands.\n");
            return;
        }
        if (args[1].length() < LENGTH) {
            args[1] = shortID(args[1]);
        }
        if ((new File(".gitlet/commits/" + args[1])).exists()) {
            if ((new File(args[3])).exists()) {
                Commit commit = Utils.readObject(new File(
                        ".gitlet/commits/" + args[1]), Commit.class);
                String commitBlob = commit.getTree().get(args[3]);
                File toCWDFile = new File(args[3]);
                Utils.writeContents(toCWDFile, commitBlob);
            } else {
                System.out.print("File does not exist in that commit.");
            }
        } else {
            System.out.print("No commit with that id exists.");
        }
    }

    public void checkout2Args(String[] args) {
        if (args[1].equals(head)) {
            System.out.print("No need to checkout the current branch.");
            return;
        }
        if (!Utils.plainFilenamesIn(new File(
                ".gitlet/branches")).contains(args[1])) {
            System.out.print("No such branch exists.");
            return;
        }
        if (!(new File(".gitlet/branches/" + args[1]).exists())) {
            System.out.print("No commit with that id exists.");
            return;
        }
        Commit commit = Utils.readObject((new File(
                ".gitlet/branches/" + args[1])), Commit.class);
        Set<String> commitFiles = commit.getTree().keySet();
        List<String> cwdFiles = Utils.plainFilenamesIn(cwd);
        Set<String> headFiles = headCommit().getTree().keySet();
        for (String file : cwdFiles) {
            if (!headFiles.contains(file)) {
                System.out.println("There is an untracked file "
                    + "in the way; delete it, "
                    + "or add and commit it first.");
                System.exit(0);
            }
        }
        for (String file : cwdFiles) {
            if (!commitFiles.contains(file)) {
                Utils.restrictedDelete(new File(file));
            } else {
                Utils.writeContents(new File(file),
                        commit.getTree().get(file));
            }
        }
        for (String file : commitFiles) {
            if (!cwdFiles.contains(file)) {
                File toFile = new File(file);
                Utils.writeContents(toFile, commit.getTree().get(file));
            }
        }
        stageAdd.clearAddHash();
        Utils.writeObject(new File(".gitlet/stagingAdd"), stageAdd);
        stageRemove.clearRemoveHash();
        Utils.writeObject(new File(".gitlet/stagingRemove"), stageRemove);

        head = args[1];
        Utils.writeContents(new File(".gitlet/HEAD"), head);
        Utils.writeObject(new File(".gitlet/branches/" + head), commit);
    }

    public void checkout(String[] args) {
        stageAdd = Utils.readObject(new File(
                ".gitlet/stagingAdd"), StagingAdd.class);
        stageRemove = Utils.readObject(new File(
                ".gitlet/stagingRemove"), StagingRemove.class);
        if (args.length == 3) {
            checkout3Args(args);
        } else if (args.length == 4) {
            checkout4Args(args);
        } else if (args.length == 2) {
            checkout2Args(args);
        }
    }

    public void log() {
        Commit currentCommit = headCommit();
        while (currentCommit.getParent() != null) {
            System.out.println("===");
            System.out.println("commit " + Utils.sha1(currentCommit.
                    getMessage(), currentCommit.getTimestamp()));
            System.out.println("Date: " + currentCommit.getTimestamp());
            System.out.println(currentCommit.getMessage());
            System.out.println();
            currentCommit = Utils.readObject(new File(".gitlet/commits/"
                            + Utils.sha1(currentCommit.getParent().getMessage(),
                            currentCommit.getParent().getTimestamp())),
                    Commit.class);
        }
        System.out.println("===");
        System.out.println("commit " + Utils.sha1(currentCommit.getMessage(),
                currentCommit.getTimestamp()));
        System.out.println("Date: " + currentCommit.getTimestamp());
        System.out.println(currentCommit.getMessage());
    }

    public void rm(String file) {
        stageAdd = Utils.readObject(new File(
                ".gitlet/stagingAdd"), StagingAdd.class);
        stageRemove = Utils.readObject(new File(
                ".gitlet/stagingRemove"), StagingRemove.class);
        boolean fileExists = false;
        String removeBlob = null;
        if (Utils.readObject(new File(".gitlet/branches/" + head),
                Commit.class).getTree().containsKey(file)) {
            fileExists = true;
        }
        if ((new File(file)).exists()) {
            removeBlob = Utils.readContentsAsString(new File(file));
        } else if (fileExists) {
            removeBlob = Utils.readObject(new File(".gitlet/branches/" + head),
                    Commit.class).getTree().get(file);
        }
        TreeMap<String, String> currentCommitTree = headCommit().getTree();
        if (currentCommitTree.containsKey(file)) {
            Utils.restrictedDelete(file);
            stageRemove.addToRemoveHash(file, removeBlob);
            if (stageAdd.contains(file)) {
                stageAdd.removeFromAddHash(file);
                Utils.writeObject(new File(".gitlet/stagingAdd"), stageAdd);
            }
            Utils.writeObject(new File(".gitlet/stagingRemove"), stageRemove);
        } else if (stageAdd.contains(file)) {
            stageAdd.removeFromAddHash(file);
            Utils.writeObject(new File(".gitlet/stagingAdd"), stageAdd);
        } else {
            System.out.print("No reason to remove the file.");
        }
    }

    public void rmBranch(String branch) {
        String headBranch = Utils.readContentsAsString(toHead);
        File toBranch = new File(".gitlet/branches/" + branch);
        if (headBranch.equals(branch)) {
            System.out.print("Cannot remove the current branch.");
            return;
        } else if (toBranch.exists()) {
            toBranch.delete();
            return;
        } else {
            System.out.print("A branch with that name does not exist.");
        }
    }

    public void find(String message) {
        File toCommits = new File(".gitlet/commits");
        List<String> names = Utils.plainFilenamesIn(toCommits);
        boolean anyMatch = false;
        for (String name : names) {
            String thisCommitMessage = Utils.readObject(new File(
                    ".gitlet/commits/" + name), Commit.class).getMessage();
            if (thisCommitMessage.equals(message)) {
                System.out.println(name);
                anyMatch = true;
            }
        }
        if (!anyMatch) {
            System.out.print("Found no commit with that message.");
            System.exit(0);
        }
    }

    public void branch(String name) {
        if ((new File(".gitlet/branches/" + name)).exists()) {
            System.out.print("A branch with that name already exists.");
        } else {
            File newBranch = new File(".gitlet/branches/" + name);
            Utils.writeObject(newBranch, headCommit());
        }
    }

    public void globalLog() {
        File toAllCommits = new File(".gitlet/globalLog");
        String[] nameAllCommits = toAllCommits.list();
        for (int commitCounter = 0; commitCounter
                < nameAllCommits.length - 1; commitCounter++) {
            String thisCommitMessage = Utils.readObject(new File(
                            ".gitlet/globalLog/"
                                    + nameAllCommits[commitCounter]),
                    Commit.class).getMessage();
            String thisCommitTimestamp = Utils.readObject(new File(
                    ".gitlet/globalLog/" + nameAllCommits[commitCounter]),
                    Commit.class).getTimestamp();
            System.out.println("===");
            System.out.println("commit " + nameAllCommits[commitCounter]);
            System.out.println("Date: " + thisCommitTimestamp);
            System.out.println(thisCommitMessage);
            System.out.println();
        }
        String lastCommitMessage = Utils.readObject(new File(
                        ".gitlet/globalLog/"
                                + nameAllCommits[nameAllCommits.length - 1]),
                Commit.class).getMessage();
        String lastCommitTimestamp = Utils.readObject(new File(
                        ".gitlet/globalLog/"
                                + nameAllCommits[nameAllCommits.length - 1]),
                Commit.class).getTimestamp();
        System.out.println("===");
        System.out.println("commit "
                + nameAllCommits[nameAllCommits.length - 1]);
        System.out.println("Date: " + lastCommitTimestamp);
        System.out.println(lastCommitMessage);
    }

    public void status() {
        if (!(new File(".gitlet")).exists()) {
            System.out.print("Not in an initialized Gitlet directory.");
            return;
        }
        File toBranches = new File(".gitlet/branches");
        stageAdd = Utils.readObject(new File(
                ".gitlet/stagingAdd"), StagingAdd.class);
        stageRemove = Utils.readObject(new File(
                ".gitlet/stagingRemove"), StagingRemove.class);
        System.out.println("=== Branches ===");
        System.out.println("*" + head);
        List<String> theBranches = Utils.plainFilenamesIn(toBranches);
        List<String> theCWDFiles = Utils.plainFilenamesIn(cwd);
        Set<String> inStageAdd = stageAdd.getStagingAddHash().keySet();
        Set<String> inStageRemove = stageRemove.getStagingRemoveHash().keySet();
        Set<String> inCurrentCommit = headCommit().getTree().keySet();
        for (String branch : theBranches) {
            if (!branch.equals(head)) {
                System.out.println(branch);
            }
        }
        System.out.println();
        System.out.println("=== Staged Files ===");
        for (String file : stageAdd.getStagingAddHash().keySet()) {
            System.out.println(file);
        }

        System.out.println();
        System.out.println("=== Removed Files ===");
        for (String file : stageRemove.getStagingRemoveHash().keySet()) {
            System.out.println(file);
        }
        System.out.println();
        System.out.println("=== Modifications Not Staged For Commit ===");
        modifiedNotStaged(theCWDFiles, inStageAdd,
                inCurrentCommit, inStageRemove);
        System.out.println();
        System.out.println("=== Untracked Files ===");
        for (String cwdFile : theCWDFiles) {
            if (!headCommit().getTree().keySet().contains(cwdFile)
                    && !stageAdd.getStagingAddHash().containsKey(cwdFile)
                    && !stageRemove.getStagingRemoveHash()
                    .containsKey(cwdFile)) {
                System.out.println(cwdFile);
            }
        }
    }

    public void modifiedNotStaged(List<String> theCWDFiles,
                                  Set<String> inStageAdd,
                                  Set<String> inCurrentCommit,
                                  Set<String> inStageRemove) {
        for (String cwdFile : theCWDFiles) {
            if (!inStageAdd.contains(cwdFile)
                    && inCurrentCommit.contains(cwdFile)) {
                String headBlob = headCommit().getTree().get(cwdFile);
                String cwdBlob = Utils.readContentsAsString(new File(cwdFile));
                if (!headBlob.equals(cwdBlob)) {
                    System.out.println(cwdFile + " (modified)");
                }
            }
        }
        for (String stageFile : inStageAdd) {
            String stageBlob = stageAdd.getStagingAddHash().get(stageFile);
            if ((new File(stageFile).exists())) {
                String cwdBlob = Utils.readContentsAsString(
                        new File(stageFile));
                if (!cwdBlob.equals(stageBlob)) {
                    System.out.println(stageFile + " (modified)");
                }
            } else {
                System.out.println(stageFile + " (deleted)");
            }
        }
        for (String headFile : inCurrentCommit) {
            if (!(new File(headFile).exists())
                    && !(inStageRemove.contains(headFile))) {
                System.out.println(headFile + " (deleted)");
            }
        }
    }
    public void reset(String commitID) {
        File commitFile = new File(".gitlet/commits/" + commitID);
        stageAdd = Utils.readObject(new File(
                ".gitlet/stagingAdd"), StagingAdd.class);
        stageRemove = Utils.readObject(new File(
                ".gitlet/stagingRemove"), StagingRemove.class);
        if (!Utils.plainFilenamesIn(new File(
                ".gitlet/commits")).contains(commitID)) {
            System.out.print("No commit with that id exists.");
            return;
        }
        Commit commit = Utils.readObject(commitFile, Commit.class);
        Set<String> commitFiles = commit.getTree().keySet();
        Set<String> headFiles = headCommit().getTree().keySet();
        List<String> cwdFiles = Utils.plainFilenamesIn(cwd);
        for (String file : cwdFiles) {
            if (!headFiles.contains(file) && !stageAdd.contains(file)
                    && !stageRemove.contains(file)) {
                System.out.println("There is an untracked file in the "
                        + "way; delete it, or add and commit it first.");
                System.exit(0);
            }
        }
        for (String file : cwdFiles) {
            if (!commitFiles.contains(file)) {
                Utils.restrictedDelete(new File(file));
            } else {
                Utils.writeContents(new File(file), commit.getTree().get(file));
            }
        }
        for (String file : commitFiles) {
            if (!cwdFiles.contains(file)) {
                File toFile = new File(file);
                Utils.writeContents(toFile, commit.getTree().get(file));
            }
        }
        stageAdd.clearAddHash();
        Utils.writeObject(new File(".gitlet/stagingAdd"), stageAdd);
        stageRemove.clearRemoveHash();
        Utils.writeObject(new File(".gitlet/stagingRemove"), stageRemove);

        Utils.writeObject(new File(".gitlet/branches/" + head), commit);
    }

    public String ancestorFinder(Commit branchCommit) {
        ArrayDeque<Commit> headCommitAndParents = new ArrayDeque<>();
        ArrayDeque<Commit> branchCommitAndParents = new ArrayDeque<>();
        List<String> allHeadAncestors = new ArrayList<>();
        List<String> allBranchAncestors = new ArrayList<>();
        Commit headCommit = headCommit();
        headCommitAndParents.push(headCommit);
        branchCommitAndParents.push(branchCommit);
        String ancestor = "";
        while (!branchCommitAndParents.isEmpty()) {
            Commit nowCommit = branchCommitAndParents.remove();
            allBranchAncestors.add(nowCommit.getSha1());
            if (nowCommit.getParent() != null) {
                branchCommitAndParents.add(nowCommit.getParent());
            }
            if (nowCommit.getParent2() != null) {
                branchCommitAndParents.add(nowCommit.getParent2());
            }
        }
        while (!headCommitAndParents.isEmpty()) {
            Commit nowCommit = headCommitAndParents.poll();
            allHeadAncestors.add(nowCommit.getSha1());
            if (allBranchAncestors.contains(nowCommit.getSha1())) {
                ancestor = nowCommit.getSha1();
                return ancestor;
            }
            if (nowCommit.getParent() != null) {
                headCommitAndParents.add(nowCommit.getParent());
            }
            if (nowCommit.getParent2() != null) {
                headCommitAndParents.add(nowCommit.getParent2());
            }
        }
        return null;
    }

    public void mergeErrors(StagingRemove stageRemove0,
                            StagingAdd stageAdd0,
                            String branch0, String head0) {
        if (!stageRemove0.getStagingRemoveHash().isEmpty()
                || !stageAdd0.getStagingAddHash().isEmpty()) {
            System.out.println("You have uncommitted changes.");
            System.exit(0);
        }
        if (!(new File(".gitlet/branches/" + branch0)).exists()) {
            System.out.println("A branch with that name does not exist.");
            System.exit(0);
        }
        if (branch0.equals(head0)) {
            System.out.println("Cannot merge a branch with itself.");
            System.exit(0);
        }
    }

    public void specialMerge(Commit ancestorCommit,
                             Commit branchCommit,
                             Commit headCommit, String branch) {
        if (Utils.sha1(ancestorCommit.getMessage(),
                        ancestorCommit.getTimestamp()).
                equals(Utils.sha1(branchCommit.getMessage(),
                        branchCommit.getTimestamp()))) {
            System.out.print("Given branch is an "
                    + "ancestor of the current branch.");
            System.exit(0);
        }
        if (Utils.sha1(ancestorCommit.getMessage(),
                ancestorCommit.getTimestamp()).equals
                (Utils.sha1(headCommit.getMessage(),
                        headCommit.getTimestamp()))) {
            String[] args = {"checkout", branch};
            checkout(args);
            System.out.print("Current branch fast-forwarded.");
            System.exit(0);
        }
    }

    public void allFilesHelper(List<String> allFileNames,
                                Set<String> headFiles, Set<String> branchFiles,
                                Commit ancestorCommit) {
        for (String fileName : headFiles) {
            allFileNames.add(fileName);
        }
        for (String fileName : branchFiles) {
            if (!allFileNames.contains(fileName)) {
                allFileNames.add(fileName);
            }
        }
        for (String fileName : ancestorCommit.getTree().keySet()) {
            if (!allFileNames.contains(fileName)) {
                allFileNames.add(fileName);
            }
        }
    }

    public void conflictHelper(String headBlob, String branchBlob,
                               String fileName) {
        String concatBlob = "";
        if (headBlob.equals("") && !branchBlob.equals("")) {
            concatBlob = String.format("<<<<<<< HEAD"
                            + "%n=======%n%s>>>>>>>%n",
                    headBlob, branchBlob);
        } else if (branchBlob.equals("") && !headBlob.equals("")) {
            concatBlob = String.format("<<<<<<< HEAD"
                    + "%n%s=======%n>>>>>>>%n", headBlob);
        } else if (!branchBlob.equals("") && !headBlob.equals("")) {
            concatBlob = String.format("<<<<<<< HEAD"
                            + "%n%s=======%n%s>>>>>>>%n",
                    headBlob, branchBlob);
        }
        if (!concatBlob.equals("")) {
            File conflictFile = new File(fileName);
            Utils.writeContents(conflictFile, concatBlob);
            add(fileName);
        }
    }

    public void merge(String branch) {
        head = Utils.readContentsAsString(toHead);
        stageAdd = Utils.readObject(new File(
                ".gitlet/stagingAdd"), StagingAdd.class);
        stageRemove = Utils.readObject(new File(
                ".gitlet/stagingRemove"), StagingRemove.class);
        mergeErrors(stageRemove, stageAdd, branch, head);
        Commit headCommit = headCommit();
        Commit branchCommit = Utils.readObject(new File(
                ".gitlet/branches/" + branch), Commit.class);
        Commit ancestorCommit = Utils.readObject(new File(
                        ".gitlet/commits/" + ancestorFinder(branchCommit)),
                Commit.class);
        specialMerge(ancestorCommit, branchCommit, headCommit, branch);
        List<String> allFileNames = new ArrayList<>();
        Set<String> headFiles = headCommit().getTree().keySet();
        Set<String> branchFiles = branchCommit.getTree().keySet();
        List<String> cwdFiles = Utils.plainFilenamesIn(cwd);
        boolean conflict = false;
        allFilesHelper(allFileNames, headFiles, branchFiles, ancestorCommit);
        untrackedHelper(cwdFiles, headFiles);
        for (String fileName : allFileNames) {
            String ancestorBlob = "";
            String headBlob = "";
            String branchBlob = "";
            if (ancestorCommit.getTree().containsKey(fileName)) {
                ancestorBlob = ancestorCommit.getTree().get(fileName);
            }
            if (headCommit.getTree().containsKey(fileName)) {
                headBlob = headCommit.getTree().get(fileName);
            }
            if (branchCommit.getTree().containsKey(fileName)) {
                branchBlob = branchCommit.getTree().get(fileName);
            }
            if (!branchBlob.equals(ancestorBlob)
                    && headBlob.equals(ancestorBlob)
                    && blobChecker(ancestorBlob, headBlob, branchBlob)) {
                mergeCaseHelper1(branchCommit, fileName);
            }
            if (!headBlob.equals(ancestorBlob)
                    && !branchBlob.equals(ancestorBlob)) {
                if (!branchBlob.equals(headBlob)) {
                    conflict = true;
                    conflictHelper(headBlob, branchBlob, fileName);
                }
            }
            if (!ancestorCommit.getTree().containsKey(fileName)) {
                if (!headCommit.getTree().containsKey(fileName)
                        && branchCommit.getTree().containsKey(fileName)) {
                    mergeCaseHelper(fileName, branchBlob, branchCommit);
                }
            }
            if (headBlob.equals(ancestorBlob)
                    && !branchCommit.getTree().containsKey(fileName)) {
                rm(fileName);
            }
        }
        finishMerge(branch, branchCommit, conflict);
        System.exit(0);
    }

    public boolean blobChecker(String ancestorBlob,
                               String headBlob, String branchBlob) {
        if (!ancestorBlob.equals("")) {
            if (!headBlob.equals("")) {
                if (!branchBlob.equals("")) {
                    return true;
                }
            }
        }
        return false;
    }

    public void mergeCaseHelper(String fileName,
                                String branchBlob, Commit branchCommit) {
        File recoverFile = new File(fileName);
        Utils.writeContents(recoverFile, branchBlob);
        String[] args = {"checkout", Utils.sha1
            (branchCommit.getMessage(), branchCommit.
            getTimestamp()), "--", fileName};
        checkout(args);
        add(fileName);
    }

    public void finishMerge(String branch,
                            Commit branchCommit, boolean conflict) {
        commit("Merged " + branch + " into "
                + head + ".", true, branchCommit);
        if (conflict) {
            System.out.println("Encountered a merge conflict.");
        }
    }

    public void untrackedHelper(List<String> cwdFiles, Set<String> headFiles) {
        for (String file : cwdFiles) {
            if (!headFiles.contains(file) && !stageAdd.contains(file)
                    && !stageRemove.contains(file)) {
                System.out.println("There is an untracked file in the "
                        + "way; delete it, or add and commit it first.");
                System.exit(0);
            }
        }
    }

    public void mergeCaseHelper1(Commit branchCommit, String fileName) {
        String[] args = {"checkout", Utils.sha1
            (branchCommit.getMessage(), branchCommit.getTimestamp()),
            "--", fileName};
        checkout(args);
    }
    public Commit headCommit() {
        head = Utils.readContentsAsString(toHead);
        return Utils.readObject(new File(
                ".gitlet/branches/" + head), Commit.class);
    }

    public String shortID(String shortID) {
        List<String> commitKeys = Utils.plainFilenamesIn
                (new File(".gitlet/commits"));
        for (String commitID : commitKeys) {
            if (commitID.startsWith(shortID)) {
                return commitID;
            }
        }
        return null;
    }
}
