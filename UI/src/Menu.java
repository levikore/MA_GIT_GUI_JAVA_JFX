import java.util.Scanner;
import logicpackage.Folder;
import logicpackage.Blob;

public class Menu {
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
        ///////////////////////
        //////////////////////
        ///TEST:          ///
        CREATE_TEXT_FILE
    }

   // private String m_UserName;

    Menu(){
       // m_UserName = "Administrator";

    }

    public static void main(String[] args) {
        runMenu();
    }

    private static void printInstructionsString(){
        String instructions = String.format( "hello %s\n" +
                "Please select one of the following option and press 'Enter'\n" +
                "0) EXIT\n" +
                "1) CHANGE_USER_NAME\n" +
                "2) GET_REPOSITORY_DATA\n"+
                "3) CHANGE_REPOSITORY\n"+
                "4) DISPLAY_CURRENT_COMMIT\n"+
                "5) DISPLAY_WORKING_COPY\n" +
                "6) COMMIT\n" +
                "7) DISPLAY_ALL_BRANCHES\n" +
                "8) BRANCH\n"+
                "9) DELETE_BRANCH\n"+
                "10) GET_ACTIVE_BRANCH_HISTORY\n",
                "Temp");

        System.out.println(instructions);
    }

    private static int getUserSelection(){
        int select = -1;
        Scanner selector = new Scanner(System.in);

        try {
            select = selector.nextInt();
        }
        catch (Exception e)
        {
            System.out.println("invalid selection, please select number");
        }

        selector.nextLine();
        return select;
    }

    private static void runMenu() {
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
            }
            //test for us
            else if(select== ESELECT.CREATE_TEXT_FILE.ordinal())
            {
                System.out.println("CREATE_TEXT_FILE");
            }
            else {
                System.out.println("invalid select");
            }
        }


    }


}
