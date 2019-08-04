import java.io.IOException;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

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
        ///////////////////////
        //////////////////////
        ///TEST:          ///
        CREATE_TEXT_FILE
    }

    private RepositoryManager m_RepositoryManager;


    Menu() {
        // m_UserName = "Administrator";
        //m_RepositoryManager = new RepositoryManager();
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
                        "11) INITIAL_REPOSITORY\n",
                "Temp");

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

    private Path handleRepositoryDataUserInput()  {
        String repositoryName, repositoryPath;
        Path result = null;
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter repository name:");
        repositoryName = scanner.nextLine();
        System.out.println("Enter repository path:");
        repositoryPath = scanner.nextLine();

        try {
            result = Paths.get(repositoryPath + "\\" + repositoryName);
        }catch(InvalidPathException e){
            System.out.println("Invalid Path: " + repositoryPath + "\\" + repositoryName);
            handleRepositoryDataUserInput();
        }

        return result;
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

            } else if (select == ESELECT.GET_REPOSITORY_DATA.ordinal()) {
                System.out.println("GET_REPOSITORY_DATA");

            } else if (select == ESELECT.CHANGE_REPOSITORY.ordinal()) {
                System.out.println("CHANGE_REPOSITORY");

            } else if (select == ESELECT.DISPLAY_CURRENT_COMMIT.ordinal()) {
                System.out.println("DISPLAY_CURRENT_COMMIT");

            } else if (select == ESELECT.DISPLAY_WORKING_COPY.ordinal()) {
                System.out.println("DISPLAY_WORKING_COPY");

            } else if (select == ESELECT.COMMIT.ordinal()) {
                System.out.println("COMMIT");

            } else if (select == ESELECT.DISPLAY_ALL_BRANCHES.ordinal()) {
                System.out.println("DISPLAY_ALL_BRANCHES");

            } else if (select == ESELECT.BRANCH.ordinal()) {
                System.out.println("BRANCH");

            } else if (select == ESELECT.DELETE_BRANCH.ordinal()) {
                System.out.println("DELETE_BRANCH");

            } else if (select == ESELECT.GET_ACTIVE_BRANCH_HISTORY.ordinal()) {
                System.out.println("GET_ACTIVE_BRANCH_HISTORY");
            } else if (select == ESELECT.INITIALISE_REPOSITORY.ordinal()) {
                m_RepositoryManager = new RepositoryManager( handleRepositoryDataUserInput());
            }
            //test for us
            else if (select == ESELECT.CREATE_TEXT_FILE.ordinal()) {
                System.out.println("CREATE_SHA1");

            } else {
                System.out.println("invalid select");
            }
        }

    }
}
