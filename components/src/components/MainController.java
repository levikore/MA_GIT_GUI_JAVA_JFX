package components;

import javafx.application.Platform;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
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
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Optional;


public class MainController {
    @FXML
    Label labelUserName;
    @FXML
    Label labelCurrentRepository;
    @FXML
    Tab tabCommit;
    @FXML
    Tab tabBranch;
    @FXML
    Tab tabMerge;
    @FXML
    MenuItem menuItemExportRepository;
    @FXML
    ListView listViewWorkingCopy;
    @FXML
    ListView listViewBranchList;
    @FXML
    Button buttonCommit;
    @FXML
    TextArea textAreaCommitComment;
    @FXML
    Button buttonAddBranch;
    @FXML
    Button buttonDeleteBranch;
    @FXML
    Button buttonCheckoutBranch;
    @FXML
    Button buttonMerge;

    private Stage m_PrimaryStage;
    private RepositoryManager m_RepositoryManager;
    private SimpleStringProperty m_UserName;
    private SimpleStringProperty m_RepositoryAddress;
    private SimpleBooleanProperty m_IsRepositorySelected;
    private ListProperty<String> m_UnCommittedList;
    private ListProperty<String> m_BranchesList;

    public void setPrimaryStage(Stage i_PrimaryStage) {
        m_PrimaryStage = i_PrimaryStage;
    }

    public MainController() {
        m_RepositoryManager = null;
        m_UserName = new SimpleStringProperty("Administrator");
        m_RepositoryAddress = new SimpleStringProperty("No repository");
        m_IsRepositorySelected = new SimpleBooleanProperty(false);
        m_UnCommittedList = new SimpleListProperty<>();
        m_BranchesList = new SimpleListProperty<>();
    }

    @FXML
    private void openXMLFileChooser(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Import xml file");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("XML Files", "*.xml"));
        File file = fileChooser.showOpenDialog(m_PrimaryStage);

        if (file != null) {
            handleGetRepositoryDataFromXML(file);
        }
    }

    private File getFileFromDirectoryChooser() {
        File result = null;
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Choose directory");
        File directory = directoryChooser.showDialog(m_PrimaryStage);

        if (directory != null) {
            String loweCaseDirectoryPathString = directory.getAbsolutePath().toLowerCase();
            result = Paths.get(loweCaseDirectoryPathString).toFile();
        }

        return result;
    }

    @FXML
    private void openEmptyRepositoryDirectoryChooser(ActionEvent event) {
        File directory = getFileFromDirectoryChooser();

        if (directory != null) {
            if (!FilesManagement.IsRepositoryExistInPath(directory.toString()) && Paths.get(directory.toString()).toFile().exists()) {
                createRepository(directory.toPath(), true);
            } else {
                new Alert(Alert.AlertType.ERROR, "The requested path already contains repository").showAndWait();
            }
        }
    }

    @FXML
    private void openSwitchRepositoryDirectoryChooser(ActionEvent event) {
        File directory = getFileFromDirectoryChooser();

        if (directory != null) {
            if (FilesManagement.IsRepositoryExistInPath(directory.toString()) && Paths.get(directory.toString()).toFile().exists()) {
                createRepository(directory.toPath(), false);
            } else {
                new Alert(Alert.AlertType.ERROR, "The requested path doesnt contain repository").showAndWait();
            }
        }
    }

    @FXML
    private void changeUserName(ActionEvent event) {
        TextInputDialog dialog = new TextInputDialog(m_UserName.getValue());
        dialog.setTitle("Change user name");
        dialog.setContentText("Please enter user name:");
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(this::handleChangeUserName);
    }

    @FXML
    private void closeProgram(ActionEvent event) {
        System.exit(0);
    }

    @FXML
    private void handelCommit(ActionEvent event) {
        String commitComment = textAreaCommitComment.getText();
        if (commitComment.isEmpty()) {
            new Alert(Alert.AlertType.INFORMATION, "Insert commit comment").showAndWait();
        } else {
            Boolean isCommitNecessary = false;
            try {
                isCommitNecessary = m_RepositoryManager.HandleCommit(commitComment);
            } catch (Exception e) {
                new Alert(Alert.AlertType.ERROR, e.getMessage()).showAndWait();
            }

            String reportString = isCommitNecessary ? "Commit successful" : "No changes were made, commit unnecessary";
            new Alert(Alert.AlertType.INFORMATION, reportString).showAndWait();
            textAreaCommitComment.clear();
            buildBranchList();
            m_UnCommittedList.clear();
        }
    }

    @FXML
    private void handleShowWorkingCopyList(ActionEvent event) {
        try {
            List<String> unCommittedFilesList = m_RepositoryManager.GetListOfUnCommittedFiles();
            m_UnCommittedList.set(FXCollections.observableArrayList(unCommittedFilesList));
        } catch (IOException ex) {
            new Alert(Alert.AlertType.ERROR, "cant reload uncommitted changes").showAndWait();
            //System.out.println("Action failed");
        }
    }

    @FXML
    private void handleShowBranchList(ActionEvent event) {
        buildBranchList();
    }

    private void buildBranchList() {
        List<String> branchesList = m_RepositoryManager.GetAllBranchesStringList();
        m_BranchesList.set(FXCollections.observableArrayList(branchesList));
    }

    @FXML
    private void handleAddNewBranch(ActionEvent event) {
        if (m_RepositoryManager.GetHeadBranch().GetBranch().GetCurrentCommit() != null) {
            openNewBranchDialog();
        } else {
            new Alert(Alert.AlertType.ERROR, "can't create branch without at least one commit ").showAndWait();
        }
    }

    @FXML
    private void handleButtonMergeClick(ActionEvent event) {
        if (m_RepositoryManager.GetHeadBranch().GetBranch().GetCurrentCommit() != null) {
            TextInputDialog dialog = new TextInputDialog("<BranchName>");
            dialog.setTitle("Merge");
            dialog.setContentText("Please enter branch name to merge with Head Branch:");
            Optional<String> result = dialog.showAndWait();

            if (!result.equals(Optional.empty())) {
                String branchName = result.get();
                boolean isMergeSucceed = handleMerge(branchName);

                if (isMergeSucceed) {
                    String reportString = isMergeSucceed ? "Merge successful" : "Merge Unsuccessful";
                    new Alert(Alert.AlertType.INFORMATION, reportString).showAndWait();
                }
            }

        } else {
            new Alert(Alert.AlertType.ERROR, "can't merge without at least one commit ").showAndWait();
        }
    }

    private Boolean handleMerge(String i_BranchName) {
        boolean returnVal = false;
        if (m_RepositoryManager != null && m_RepositoryManager.GetHeadBranch() != null) {
            try {
                if (!m_RepositoryManager.IsUncommittedFilesInRepository()) {
                    returnVal = m_RepositoryManager.HandleMerge(i_BranchName);
                    if (!returnVal) {
                        new Alert(Alert.AlertType.ERROR, "you trying to merge branch that doesnt exist, to head branch.").showAndWait();
                    }
                } else {
                    new Alert(Alert.AlertType.ERROR, "Uncommitted changes in the branch, you must do commit before this action.").showAndWait();
                }
            } catch (IOException e) {
                new Alert(Alert.AlertType.ERROR, "Unable to read the files.").showAndWait();
            }


        } else if (m_RepositoryManager == null) {
            new Alert(Alert.AlertType.ERROR, "you must be in repository for merge.").showAndWait();

        } else {
            new Alert(Alert.AlertType.ERROR, "you must do commit once at least").showAndWait();
        }
        return returnVal;
    }

    private void handleDeleteBranch(String i_BranchName) {
        if (m_RepositoryManager != null && m_RepositoryManager.GetHeadBranch() != null) {
            boolean returnVal = m_RepositoryManager.RemoveBranch(i_BranchName);
            if (!returnVal) {
                new Alert(Alert.AlertType.ERROR, "You trying to delete HEAD branch, Or Branch that doesnt exist.").showAndWait();
            }
        } else if (m_RepositoryManager == null) {
            new Alert(Alert.AlertType.ERROR, "you must be in repository for delete branch").showAndWait();
        } else {
            new Alert(Alert.AlertType.ERROR, "you must do commit once at least, before delete branch.").showAndWait();
        }

    }

    @FXML
    private void handleRemoveBranchClick(ActionEvent event) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Delete branch");
        dialog.setHeaderText("Look, a Text Input Dialog");
        dialog.setContentText("Please enter branch name to delete");
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(this::handleDeleteBranch);
        buildBranchList();
        m_UnCommittedList.clear();
    }

    private boolean handleCheckout(String i_BranchName) {
        boolean returnVal = false;
        if (m_RepositoryManager != null && m_RepositoryManager.GetHeadBranch() != null) {
            try {
                if (!m_RepositoryManager.IsUncommittedFilesInRepository()) {
                    returnVal = m_RepositoryManager.HandleCheckout(i_BranchName);
                    if (!returnVal) {
                        new Alert(Alert.AlertType.ERROR, "you trying to checkout into branch that doesnt exist.").showAndWait();
                    }
                } else {
                    new Alert(Alert.AlertType.ERROR, "Uncommitted changes in the branch, you must do commit before this action.").showAndWait();
                }
            } catch (IOException e) {
                new Alert(Alert.AlertType.ERROR, "Unable to read the files.").showAndWait();
            }
        } else if (m_RepositoryManager == null) {
            new Alert(Alert.AlertType.ERROR, "you must be in repository for checkout.").showAndWait();

        } else {
            new Alert(Alert.AlertType.ERROR, "you must do commit once at least").showAndWait();
        }
        return returnVal;
    }

    @FXML
    private void handleCheckoutButtonClick(ActionEvent event) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Checkout");
        dialog.setHeaderText("Look, a Text Input Dialog");
        dialog.setContentText("Please enter branch name to Checkout");
        Optional<String> result = dialog.showAndWait();

        if (!result.equals(Optional.empty())) {
            String branchName = result.get();
            boolean isCheckoutSucceed = handleCheckout(branchName);

            if (isCheckoutSucceed) {
                buildBranchList();
                m_UnCommittedList.clear();
            }
        }
    }

    private void openNewBranchDialog() {
        TextInputDialog dialog = new TextInputDialog("New Branch");
        dialog.setTitle("New Branch");
        dialog.setContentText("Please enter branch name:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(this::handleBranchNewCreation);
    }

    private void handleBranchNewCreation(String i_BranchName) {
        if (!m_RepositoryManager.IsBranchExist(i_BranchName)) {
            m_RepositoryManager.HandleBranch(i_BranchName);
            buildBranchList();
        } else {
            new Alert(Alert.AlertType.ERROR, i_BranchName + " already exists").showAndWait();
        }
    }

    private void handleChangeUserName(String i_UserName) {
        m_UserName.set(i_UserName);

        if (m_RepositoryManager != null) {
            m_RepositoryManager.SetCurrentUserName(m_UserName.getValue());
        }

    }

    private void createRepository(Path i_RepositoryPath, Boolean i_IsNewRepository) {
        m_RepositoryManager = new RepositoryManager(i_RepositoryPath, m_UserName.getValue(), i_IsNewRepository, false);
        m_IsRepositorySelected.set(true);
        m_RepositoryAddress.set(m_RepositoryManager.GetRepositoryPath().toString());

        rebindListViews();
    }

    private void rebindListViews() {
        m_UnCommittedList = new SimpleListProperty<>();
        m_BranchesList = new SimpleListProperty<>();
        buildBranchList();
        m_UnCommittedList.set(FXCollections.observableArrayList(Collections.emptyList()));
        listViewWorkingCopy.itemsProperty().unbind();
        listViewBranchList.itemsProperty().unbind();
        listViewWorkingCopy.itemsProperty().bind(m_UnCommittedList);
        listViewBranchList.itemsProperty().bind(m_BranchesList);
    }

    private void handleGetRepositoryDataFromXML(File i_XMLFile) {
        try {
            Path repositoryPath = XMLManager.GetRepositoryPathFromXML(i_XMLFile);
            if (XMLManager.IsEmptyRepository(i_XMLFile)) {
                if (!FilesManagement.IsRepositoryExistInPath(repositoryPath.toString())) {
                    new Alert(Alert.AlertType.INFORMATION, "Creating empty repository").showAndWait();
                    createRepository(repositoryPath, true);
                } else {
                    new Alert(Alert.AlertType.ERROR, "The requested path already contains repository").showAndWait();
                }
                return;
            }
            List<String> errors = XMLManager.GetXMLFileErrors(i_XMLFile);
            if (errors.isEmpty()) {
                try {
                    if (FilesManagement.IsRepositoryExistInPath(repositoryPath.toString())) {
                        showExistingRepositoryDialogue(repositoryPath, i_XMLFile);
                    } else {
                        //createRepositoryFromXML(repositoryPath, i_XMLFile);
                        createRepositoryFromXMLInDifferentThread(repositoryPath, i_XMLFile);
                    }
                } catch (Exception e) {
                    new Alert(Alert.AlertType.ERROR, e.getMessage()).showAndWait();
                }

            } else {
                printXMLErrors(errors);
            }
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, e.getMessage()).showAndWait();
        }
    }

    private void createRepositoryFromXMLInDifferentThread(Path i_RepositoryPath, File i_XMLFile) {
        new Thread(() -> createRepositoryFromXML(i_RepositoryPath, i_XMLFile)).start();
    }

    private void createRepositoryFromXML(Path i_RepositoryPath, File i_XMLFile) {
        try {
            new RepositoryManager(i_RepositoryPath, m_UserName.getValue(), true, true);
            XMLManager.BuildRepositoryObjectsFromXML(i_XMLFile, i_RepositoryPath);
            Platform.runLater(() -> {

                createRepository(i_RepositoryPath, false);
                m_RepositoryManager.HandleCheckout(m_RepositoryManager.GetHeadBranch().GetBranch().GetBranchName());
            });
        } catch (Exception e) {
            Platform.runLater(() -> new Alert(Alert.AlertType.ERROR, e.getMessage()).showAndWait());
        }
    }

    private void printXMLErrors(List<String> i_ErrorList) {
        String errorString = "Errors in XML file:\n";
        int index = 1;
        for (String error : i_ErrorList) {
            errorString = errorString.concat(index + ") " + error + "\n");
            index++;
        }

        new Alert(Alert.AlertType.ERROR, errorString).showAndWait();
    }

    private void showExistingRepositoryDialogue(Path i_RepositoryPath, File i_XMLFile) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Existing Repository Dialogue");
        alert.setHeaderText("There is already a repository in this directory");
        alert.setContentText("Choose your option.");
        ButtonType buttonTypeOverride = new ButtonType("Override");
        ButtonType buttonTypeUseExisting = new ButtonType("Use Existing");
        ButtonType buttonTypeCancel = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(buttonTypeOverride, buttonTypeUseExisting, buttonTypeCancel);
        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == buttonTypeOverride) {
            try {
                FileUtils.deleteDirectory(i_RepositoryPath.toFile());
                createRepositoryFromXMLInDifferentThread(i_RepositoryPath, i_XMLFile);
            } catch (Exception e) {
                new Alert(Alert.AlertType.INFORMATION, e.getMessage()).showAndWait();
                //System.out.println("Delete existing repository failed, make sure all local files in repository are not in use");
            }
        } else if (result.get() == buttonTypeUseExisting) {
            createRepository(i_RepositoryPath, false);
        } else {
            // ... user chose CANCEL or closed the dialog
        }
    }

    @FXML
    private void initialize() {
        labelUserName.textProperty().bind(m_UserName);
        labelCurrentRepository.textProperty().bind(m_RepositoryAddress);
        tabCommit.disableProperty().bind(m_IsRepositorySelected.not());
        tabBranch.disableProperty().bind(m_IsRepositorySelected.not());
        tabMerge.disableProperty().bind(m_IsRepositorySelected.not());
        menuItemExportRepository.disableProperty().bind(m_IsRepositorySelected.not());
    }
}
