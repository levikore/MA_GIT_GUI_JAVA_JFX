import java.io.File;
import java.io.IOException;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Scanner;

import logicpackage.RepositoryManager;
import logicpackage.XMLManager;


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
        INITIALISE_REPOSITORY
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
                        "11) INITIAL_REPOSITORY\n",
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

    private Path handleRepositoryPathUserInput() {
        String repositoryName, repositoryPath;
        Path result = null;
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter repository name:");
        repositoryName = scanner.nextLine();
        System.out.println("Enter repository path:");
        repositoryPath = scanner.nextLine();

        try {
            result = Paths.get(repositoryPath + "\\" + repositoryName);
        } catch (InvalidPathException e) {
            System.out.println("Invalid Path: " + repositoryPath + "\\" + repositoryName);
            handleRepositoryPathUserInput();
        }

        return result;
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

    private void handleNewBranchOption(){
        String result = null;
        String branchName;
        Scanner scanner = new Scanner(System.in);

        System.out.println("Enter branch name:");
        branchName = scanner.nextLine();
        try {
            result = branchName;
        } catch (InvalidPathException e) {
            handleRepositoryUserNameInput();
        }

    }

    private void handleCommit() {
            Scanner scanner = new Scanner(System.in);
            System.out.println("Insert commit comment:");
            String commitComment = scanner.nextLine();
            Boolean isCommitNecessary = false;

            try {
                isCommitNecessary = m_RepositoryManager.HandleCommit(commitComment);
            } catch (IOException e) {
                System.out.println("Commit error");
                System.out.println(e.toString());
                run();
            }

            String reportString = isCommitNecessary ? "Commit successful" : "No changes were made, commit unnecessary";
            System.out.println(reportString);
    }

    private void handleGetRepositoryDataFromXML(){
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter xml file directory:");
        String xmlPathString = scanner.nextLine();
        File xmlFile = Paths.get(xmlPathString).toFile();
        List<String> errors = XMLManager.GetXMLFileErrors(xmlFile);
        if(errors.isEmpty()){
            try {
                Path repositoryPath = XMLManager.GetRepositoryPathFromXML(xmlFile);
                if(repositoryPath.toFile().isDirectory()) {
                    //******************************************************************
                }else{
                    m_RepositoryManager = new RepositoryManager(repositoryPath, m_UserName);
                    XMLManager.BuildRepositoryObjectsFromXML(xmlFile, repositoryPath);
                }
            } catch (Exception e) {
                System.out.println("Failed to open repository");
                run();
            }

        }else{
            printXMLErrors(errors);
        }

        run();
    }

    private void printXMLErrors(List<String> i_ErrorList){
        int index =1;
        System.out.println("Errors in XML file:");
        for(String error: i_ErrorList){
            System.out.println(index+") "+error);
            index++;
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
                handleGetRepositoryDataFromXML();

            } else if (select == ESELECT.CHANGE_REPOSITORY.ordinal()) {
                System.out.println("CHANGE_REPOSITORY");

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
                System.out.println("BRANCH");

            } else if (select == ESELECT.DELETE_BRANCH.ordinal()) {
                System.out.println("DELETE_BRANCH");

            } else if (select == ESELECT.GET_ACTIVE_BRANCH_HISTORY.ordinal()) {
                System.out.println("GET_ACTIVE_BRANCH_HISTORY");
            } else if (select == ESELECT.INITIALISE_REPOSITORY.ordinal()) {
                m_RepositoryManager = new RepositoryManager(handleRepositoryPathUserInput(), m_UserName);
            } else {
                System.out.println("invalid select");
            }
        }

    }
}
