import java.io.IOException;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Scanner;

import logicpackage.FilesManagement;
import logicpackage.RepositoryManager;


public class Menu implements Runnable {
    private enum ESELECT {
        EXIT,
        CHANGE_USER_NAME,
        GET_REPOSITORY_DATA,
        CHANGE_REPOSITORY,
        DISPLAY_CURRENT_COMMIT,
        DISPLAY_WORKING_COPY,
        COMMIT,
        DISPLAY_ALL_BRANCHES,
        BRANCH,
        DELETE_BRANCH,
        GET_ACTIVE_BRANCH_HISTORY,
        INITIALISE_REPOSITORY,
        CHECKOUT
    }

    private RepositoryManager m_RepositoryManager;
    private String m_UserName;


    Menu() {
        m_UserName = "Administrator";
    }


    private void printInstructionsString() {
        String instructions = String.format("hello %s\n" +
                        "Please select one of the following option and press 'Enter'\n" +
                        "0) EXIT\n" +
                        "1) CHANGE_USER_NAME\n" +
                        "2) GET_REPOSITORY_DATA\n" +
                        "3) CHANGE_REPOSITORY\n" +
                        "4) DISPLAY_CURRENT_COMMIT\n" +
                        "5) DISPLAY_WORKING_COPY\n" +
                        "6) COMMIT\n" +
                        "7) DISPLAY_ALL_BRANCHES\n" +
                        "8) BRANCH\n" +
                        "9) DELETE_BRANCH\n" +
                        "10) GET_ACTIVE_BRANCH_HISTORY\n" +
                        "11) INITIAL_REPOSITORY\n" +
                        "12) CHECKOUT\n",
                m_UserName);

        System.out.println(instructions);
    }

    private int getUserSelection() {
        int select = -1;
        Scanner selector = new Scanner(System.in);

        try {
            select = selector.nextInt();
        } catch (Exception e) {
            System.out.println("invalid selection, please select number");
        }

        selector.nextLine();
        return select;
    }

    private void handleGetRepositoryData() {
        if (m_RepositoryManager != null && m_RepositoryManager.getHeadBranch() != null) {
            String result = null;
            String branchNameToCheckout;
            Scanner scanner = new Scanner(System.in);
            List<String> dataList = m_RepositoryManager.getHeadBranch().getBranch().getCurrentCommit().GetAllCommitFiles();
            dataList.stream().forEach(System.out::println);
        } else if (m_RepositoryManager == null) {
            System.out.println("you must to be in repository that");
            run();
        } else {
            System.out.println("you must to commit once at least");
            run();
        }
    }

    private void handleInitializeRepository() {
        String repositoryName, repositoryPath;
        Path result = null;
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter repository name:");
        repositoryName = scanner.nextLine();
        System.out.println("Enter repository path:");
        repositoryPath = scanner.nextLine();
        if (!FilesManagement.IsRepositoryExistInPath(repositoryPath + "\\" + repositoryName)) {
            try {
                result = Paths.get(repositoryPath + "\\" + repositoryName);
            } catch (InvalidPathException e) {
                System.out.println("Invalid Path: " + repositoryPath + "\\" + repositoryName);
                handleInitializeRepository();
            }
            m_RepositoryManager = new RepositoryManager(result, m_UserName);
        } else {
            System.out.println("The requested path already contains repository");
            run();
        }
    }

    private void handleRepositoryUserNameInput() {
        String userName;
        String result = null;
        Scanner scanner = new Scanner(System.in);

        System.out.println("Enter your name:");
        userName = scanner.nextLine();
        try {
            result = userName;
        } catch (InvalidPathException e) {
            handleRepositoryUserNameInput();
        }
        m_UserName = result;
    }

    private void handleChangeRepository() {
        String repositoryName, fullRepositoryPath;
        Path result = null;
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter repository path:");
        fullRepositoryPath = scanner.nextLine();
        if (FilesManagement.IsRepositoryExistInPath(fullRepositoryPath)) {
            try {
                result = Paths.get(fullRepositoryPath);
            } catch (InvalidPathException e) {
                System.out.println("Invalid Path: " + fullRepositoryPath);
                handleChangeRepository();
            }
            m_RepositoryManager = new RepositoryManager(result, m_UserName);
        } else {
            System.out.println("The requested path dose not contains repository");
            run();
        }
    }

    private void handleNewBranchOption() {
        if (m_RepositoryManager != null && m_RepositoryManager.getHeadBranch() != null) {
            String result = null;
            String branchName;
            Scanner scanner = new Scanner(System.in);

            System.out.println("Enter branch name:");
            branchName = scanner.nextLine();
            try {
                result = branchName;
            } catch (InvalidPathException e) {
                handleNewBranchOption();
            }
            m_RepositoryManager.HandleBranch(result);
        } else if (m_RepositoryManager == null) {
            System.out.println("you must to be in repository for create branch");
            run();
        } else {
            System.out.println("you must to commit once at least, before you create new Branch");
            run();
        }

    }

    private void handleCommit() {
        if (m_RepositoryManager != null) {
            Scanner scanner = new Scanner(System.in);
            System.out.println("Insert commit comment:");
            String commitComment = scanner.nextLine();
            Boolean isCommitNecessary = false;
            try {
                isCommitNecessary = m_RepositoryManager.HandleCommit(commitComment);
                m_RepositoryManager.getHeadBranch().getBranch().recoverCommit("c:\\test\\rep1");
            } catch (IOException e) {
                System.out.println("Commit error");
                System.out.println(e.toString());
                run();
            }

            String reportString = isCommitNecessary ? "Commit successful" : "No changes were made, commit unnecessary";
            System.out.println(reportString);
        } else {
            System.out.println("you must to be in repository for delete branch");
            run();
        }
    }

    private void handleDeleteBranch() {
        if (m_RepositoryManager != null && m_RepositoryManager.getHeadBranch() != null) {
            String result = null;
            String branchNameToCheckout;
            Scanner scanner = new Scanner(System.in);
            System.out.println("Enter branch name to delete:");
            branchNameToCheckout = scanner.nextLine();
            try {
                result = branchNameToCheckout;
            } catch (InvalidPathException e) {
                handleDeleteBranch();
            }
            boolean returnVal = false;
            returnVal = m_RepositoryManager.removeBranch(branchNameToCheckout);
            if (!returnVal) {
                System.out.println("you trying to delete HEAD branch, Or Branch that does not exist.");
                run();
            }
        } else if (m_RepositoryManager == null) {
            System.out.println("you must to be in repository for delete branch");
            run();
        } else {
            System.out.println("you must to commit once at least, before delete branch.");
            run();
        }

    }

    private void handleCheckout() {
        if (m_RepositoryManager != null && m_RepositoryManager.getHeadBranch() != null) {
            String result = null;
            String branchNameToCheckout;
            Scanner scanner = new Scanner(System.in);
            System.out.println("Enter branch name:");
            branchNameToCheckout = scanner.nextLine();
            try {
                result = branchNameToCheckout;
            } catch (InvalidPathException e) {
                handleCheckout();
            }
            boolean returnVal = m_RepositoryManager.handleCheckout(branchNameToCheckout);
            if (!returnVal) {

                System.out.println("you trying to checkout into Branch that does not exist.");
                run();
            }
        } else if (m_RepositoryManager == null) {
            System.out.println("you must to be in repository for checkout.");
            run();
        } else {
            System.out.println("you must to commit once at least, before delete branch.");
            run();
        }
    }

    @Override
    public void run() {
        boolean isRunMenu = true;

        while (isRunMenu) {
            printInstructionsString();
            int select = getUserSelection();

            if (select == ESELECT.EXIT.ordinal()) {
                isRunMenu = false;
                System.out.println("EXIT");
            } else if (select == ESELECT.CHANGE_USER_NAME.ordinal()) {
                System.out.println("CHANGE_USER_NAME");
                handleRepositoryUserNameInput();

            } else if (select == ESELECT.GET_REPOSITORY_DATA.ordinal()) {
                System.out.println("GET_REPOSITORY_DATA");
                handleGetRepositoryData();
            } else if (select == ESELECT.CHANGE_REPOSITORY.ordinal()) {
                System.out.println("CHANGE_REPOSITORY");
                handleChangeRepository();
            } else if (select == ESELECT.DISPLAY_CURRENT_COMMIT.ordinal()) {
                System.out.println("DISPLAY_CURRENT_COMMIT");

            } else if (select == ESELECT.DISPLAY_WORKING_COPY.ordinal()) {
                System.out.println("DISPLAY_WORKING_COPY");

            } else if (select == ESELECT.COMMIT.ordinal()) {
                System.out.println("COMMIT");
                handleCommit();

            } else if (select == ESELECT.DISPLAY_ALL_BRANCHES.ordinal()) {
                System.out.println("DISPLAY_ALL_BRANCHES");

            } else if (select == ESELECT.BRANCH.ordinal()) {
                handleNewBranchOption();
                System.out.println("BRANCH");

            } else if (select == ESELECT.DELETE_BRANCH.ordinal()) {
                System.out.println("DELETE_BRANCH");
                handleDeleteBranch();
            } else if (select == ESELECT.GET_ACTIVE_BRANCH_HISTORY.ordinal()) {
                System.out.println("GET_ACTIVE_BRANCH_HISTORY");
            } else if (select == ESELECT.INITIALISE_REPOSITORY.ordinal()) {
                handleInitializeRepository();
            } else if (select == ESELECT.CHECKOUT.ordinal()) {
                handleCheckout();
                //FilesManagement.CleanWC(m_RepositoryManager.getRepositoryPath());
            } else {
                System.out.println("invalid select");
            }
        }

    }
}
