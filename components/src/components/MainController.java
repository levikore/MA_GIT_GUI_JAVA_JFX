package components;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import logicpackage.RepositoryManager;
import logicpackage.XMLManager;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

public class MainController {
    @FXML Label labelUserName;
    @FXML Label labelCurrentRepository;
    @FXML Tab tabCommit;
    @FXML Tab tabBranch;
    @FXML Tab tabMerge;
    @FXML MenuItem menuItemExportRepository;
    @FXML  Button buttonCommit;

    private Stage m_PrimaryStage;
    private RepositoryManager m_RepositoryManager;
    private SimpleStringProperty m_UserName;
    private SimpleStringProperty m_RepositoryAddress;
    private SimpleBooleanProperty m_IsRepositorySelected;

    public void setPrimaryStage(Stage i_PrimaryStage){
        m_PrimaryStage = i_PrimaryStage;
    }

    public MainController(){
        m_RepositoryManager = null;
        m_UserName = new SimpleStringProperty("Administrator");
        m_RepositoryAddress = new SimpleStringProperty("No repository");
        m_IsRepositorySelected = new SimpleBooleanProperty(false);
    }

   @FXML
     private void openXMLFileChooser(ActionEvent event){
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Import xml file");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("XML Files", "*.xml"));
        File file = fileChooser.showOpenDialog(m_PrimaryStage);

        if(file != null){
            handleGetRepositoryDataFromXML(file);
            //new Alert(Alert.AlertType.ERROR, "This is an error!").showAndWait();
        }
    }

    private void createRepository(Path i_RepositoryPath, Boolean i_IsNewRepository){
        m_RepositoryManager = new RepositoryManager(i_RepositoryPath, m_UserName.toString(), i_IsNewRepository);
        m_IsRepositorySelected.set(true);
        m_RepositoryAddress.set(m_RepositoryManager.GetRepositoryPath().toString());
    }

    private void handleGetRepositoryDataFromXML(File i_XMLFile ) {
        try {
            Path repositoryPath = XMLManager.GetRepositoryPathFromXML(i_XMLFile);
            if(XMLManager.IsEmptyRepository(i_XMLFile)){
                new Alert(Alert.AlertType.INFORMATION, "No branches detected, creating empty repository").showAndWait();
                //System.out.println("No branches detected, creating empty repository");
                //m_RepositoryManager = new RepositoryManager(repositoryPath, m_UserName.toString(), true);
                createRepository(repositoryPath, true);
            }

            List<String> errors = XMLManager.GetXMLFileErrors(i_XMLFile);
            if (errors.isEmpty()) {
                try {
                    if (repositoryPath.toFile().isDirectory()) {
                        //handleExistingRepository(i_XMLFile, repositoryPath);
                    } else {
                        createRepositoryFromXML(repositoryPath, i_XMLFile);
                    }

                    //labelCurrentRepository.textProperty().bind(Bindings.format("%,s", m_RepositoryManager.GetRepositoryPath()));//****
                } catch (Exception e) {
                    new Alert(Alert.AlertType.ERROR, e.getMessage()).showAndWait();
                    //System.out.println("Failed to open repository");
                }

            } else {
                printXMLErrors(errors);
            }
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, e.getMessage()).showAndWait();
            //System.out.println("Failed, finding errors in xml");
        }
    }

    private void createRepositoryFromXML(Path i_RepositoryPath, File i_XMLFile) {
        try {
            new RepositoryManager(i_RepositoryPath, m_UserName.toString(), true);
            XMLManager.BuildRepositoryObjectsFromXML(i_XMLFile, i_RepositoryPath);
            //m_RepositoryManager = new RepositoryManager(i_RepositoryPath, m_UserName.toString(), false);
            createRepository(i_RepositoryPath, false);
            m_RepositoryManager.handleCheckout(m_RepositoryManager.getHeadBranch().getBranch().getBranchName());
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, e.getMessage()).showAndWait();
            //System.out.println("Build repository from xml failed");
        }
    }

    /*private void handleExistingRepository(File i_XMLFile, Path i_RepositoryPath) {
        System.out.println("There is already a repository in this directory");
        System.out.println(String.format("%d- Exit,   %d- override  %d- use existing",
                EXISTING_OPTIONS.Exit.ordinal(), EXISTING_OPTIONS.OVERRIDE.ordinal(), EXISTING_OPTIONS.USE_EXISTING.ordinal()));
        Scanner selector = new Scanner(System.in);
        int select = selector.nextInt();
        if (select == EXISTING_OPTIONS.Exit.ordinal()) {
            run();
        } else if (select == EXISTING_OPTIONS.OVERRIDE.ordinal()) {
            try {
                FileUtils.deleteDirectory(i_RepositoryPath.toFile());
                createRepositoryFromXML(i_RepositoryPath, i_XMLFile);
            } catch (Exception e) {
                System.out.println("Delete existing repository failed, make sure all local files in repository are not in use");
                run();
            }
        } else if (select == EXISTING_OPTIONS.USE_EXISTING.ordinal()) {
            m_RepositoryManager = new RepositoryManager(i_RepositoryPath, m_UserName.toString(), false);
        } else {
            System.out.println("Invalid input, try again");
            handleExistingRepository(i_XMLFile, i_RepositoryPath);
        }
    }*/

    private void printXMLErrors(List<String> i_ErrorList) {
        String errorString = "Errors in XML file:\n";
        int index = 1;
        for (String error : i_ErrorList) {
            errorString = errorString.concat(index + ") " + error+"\n");
            index++;
        }

        new Alert(Alert.AlertType.ERROR, errorString).showAndWait();
    }

    @FXML
    private void initialize(){
        //labelCurrentRepository.textProperty().bind(Bindings.format("%,s", m_RepositoryManager.GetRepositoryPath()));
        //tabCommit.textProperty().bind();
        labelUserName.textProperty().bind(m_UserName);
        labelCurrentRepository.textProperty().bind(m_RepositoryAddress);
        tabCommit.disableProperty().bind(m_IsRepositorySelected.not());
        tabBranch.disableProperty().bind(m_IsRepositorySelected.not());
        tabMerge.disableProperty().bind(m_IsRepositorySelected.not());
        menuItemExportRepository.disableProperty().bind(m_IsRepositorySelected.not());
    }
}
