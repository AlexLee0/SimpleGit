package gitlet;

/**
 * Driver class for Gitlet, the tiny stupid version-control system.
 *
 * @author Alex Lee
 */
public class Main {

    /**
     * Usage: java gitlet.Main ARGS, where ARGS contains
     * <COMMAND> <OPERAND> ....
     * java gitlet.Main add hello.txt
     */
    public static void main(String... args) {
        CommandClass repository = new CommandClass();
        if (args.length == 0) {
            System.out.println("Please enter a command.");
            System.exit(0);
        }
        switch (args[0]) {
        case "init":
            if (correctInputs(1, args)) {
                repository.init();
            }
            break;
        case "add":
            if (correctInputs(2, args)) {
                repository.add(args[1]);
            }
            break;
        case "commit":
            if (correctInputs(2, args)) {
                repository.commit(args[1], false, null);
            }
            break;
        case "checkout":
            repository.checkout(args);
            break;
        case "log":
            if (correctInputs(1, args)) {
                repository.log();
            }
            break;
        default:
            main2(repository, args);
        }
    }

    public static void main2(CommandClass repository, String... args) {
        switch (args[0]) {
        case "rm":
            if (correctInputs(2, args)) {
                repository.rm(args[1]);
            }
            break;
        case "rm-branch":
            if (correctInputs(2, args)) {
                repository.rmBranch(args[1]);
            }
            break;
        case "find":
            if (correctInputs(2, args)) {
                repository.find(args[1]);
            }
            break;
        case "branch":
            if (correctInputs(2, args)) {
                repository.branch(args[1]);
            }
            break;
        case "global-log":
            if (correctInputs(1, args)) {
                repository.globalLog();
            }
            break;
        case "status":
            if (correctInputs(1, args)) {
                repository.status();
            }
            break;
        case "reset":
            if (correctInputs(2, args)) {
                repository.reset(args[1]);
            }
            break;
        case "merge":
            if (correctInputs(2, args)) {
                repository.merge(args[1]);
            }
            break;
        default:
            System.out.println("No command with that name exists.");
        }
    }

    public static boolean correctInputs(int num, String... args) {
        if (args.length != num) {
            System.out.println("Incorrect operands");
        }
        return args.length == num;
    }
}
