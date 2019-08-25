package components;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import logicpackage.FilesManagement;
import logicpackage.RepositoryManager;
import logicpackage.XMLManager;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

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

    private File getFileFromDirectoryChooser(){
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Choose directory");
        return directoryChooser.showDialog(m_PrimaryStage);
    }

    @FXML
    private void openEmptyRepositoryDirectoryChooser(ActionEvent event){
        File directory = getFileFromDirectoryChooser();

        if(directory != null){
            if (!FilesManagement.IsRepositoryExistInPath(directory.toString()) &&  Paths.get(directory.toString()).toFile().exists()) {
                createRepository(directory.toPath(), true);
            } else {
                new Alert(Alert.AlertType.ERROR, "The requested path already contains repository").showAndWait();
                //System.out.println("The requested path already contains repository or doesnt exist");
            }
        }
    }

    @FXML
    private void openSwitchRepositoryDirectoryChooser(ActionEvent event){
        File directory = getFileFromDirectoryChooser();

        if(directory != null){
            if (FilesManagement.IsRepositoryExistInPath(directory.toString()) &&  Paths.get(directory.toString()).toFile().exists()) {
                createRepository(directory.toPath(), false);
            } else {
                new Alert(Alert.AlertType.ERROR, "The requested path doesnt contain repository").showAndWait();
                //System.out.println("The requested path already contains repository or doesnt exist");
            }
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
                        showExistingRepositoryDialogue(repositoryPath, i_XMLFile);
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

    private void printXMLErrors(List<String> i_ErrorList) {
        String errorString = "Errors in XML file:\n";
        int index = 1;
        for (String error : i_ErrorList) {
            errorString = errorString.concat(index + ") " + error+"\n");
            index++;
        }

        new Alert(Alert.AlertType.ERROR, errorString).showAndWait();
    }

    private void showExistingRepositoryDialogue(Path i_RepositoryPath, File i_XMLFile){
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Existing Repository Dialogue");
        alert.setHeaderText("There is already a repository in this directory");
        alert.setContentText("Choose your option.");
        ButtonType buttonTypeOverride = new ButtonType("Override");
        ButtonType buttonTypeUseExisting = new ButtonType("Use Existing");
        ButtonType buttonTypeCancel = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(buttonTypeOverride, buttonTypeUseExisting, buttonTypeCancel);
        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == buttonTypeOverride){
            try {
                FileUtils.deleteDirectory(i_RepositoryPath.toFile());
                createRepositoryFromXML(i_RepositoryPath, i_XMLFile);
            } catch (Exception e) {
                new Alert(Alert.AlertType.INFORMATION, e.getMessage()).showAndWait();
                //System.out.println("Delete existing repository failed, make sure all local files in repository are not in use");
            }
        } else if (result.get() == buttonTypeUseExisting) {
            //m_RepositoryManager = new RepositoryManager(i_RepositoryPath, m_UserName.toString(), false);
            createRepository(i_RepositoryPath, false);
        } else {
            // ... user chose CANCEL or closed the dialog
        }
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
