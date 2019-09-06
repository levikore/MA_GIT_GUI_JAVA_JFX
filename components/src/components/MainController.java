package components;

import javafx.application.Platform;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import logicpackage.*;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.LinkedList;
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
    ListView listViewUncommittedNewFiles;
    @FXML
    ListView listViewFilesThatChanged;
    @FXML
    ListView listViewUncommittedRemovedFiles;
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
    @FXML
    Button buttonResetHeadBranch;
    @FXML
    private CheckBox CheckBoxCommitSha1;

    @FXML
    private TextField TextFieldCommitSha1;

    @FXML
    private ListView<BlobData> ListViewBlobsData;

    @FXML
    private TextField TextPropertyCommitSha1;

    @FXML
    private Button ButtonSelectCommit;

    @FXML
    private TextArea TextAreaBlobContent;


    private Stage m_PrimaryStage;
    private RepositoryManager m_RepositoryManager;
    private SimpleStringProperty m_UserName;
    private SimpleStringProperty m_RepositoryAddress;
    private SimpleBooleanProperty m_IsRepositorySelected;
    private SimpleBooleanProperty m_IsCommitSha1CheckBoxSelectd;
    private ListProperty<String> m_UnCommittedNewFilesList;
    private ListProperty<String> m_UnCommittedListFilesThatChanged;
    private ListProperty<String> m_UncommittedRemovedFilesList;
    private ListProperty<String> m_BranchesList;
    private SimpleListProperty<BlobData> m_BlobsList;


    @FXML
    void handleSelectCommitSha1Click(ActionEvent event) {
        m_BlobsList.set(FXCollections.observableArrayList(Collections.emptyList()));
        TextAreaBlobContent.clear();
        String commitSha1 = TextPropertyCommitSha1.getText();
        Commit commit = m_RepositoryManager.FindCommitInAllBranches(commitSha1);
        if (commit == null) {
            new Alert(Alert.AlertType.ERROR, "Unable to find commit for this SHA1").showAndWait();
        } else {
            SetBlobsListProperty(commit.GetCommitRootFolder().GetFilesDataList());
        }
        TextPropertyCommitSha1.clear();
    }

    public void SetBlobsListProperty(List<BlobData> io_BlobsList) {
        List<String> blobsList = new LinkedList<>();
        io_BlobsList.forEach(blobData -> blobsList.add(blobData.GetPath()));
        m_BlobsList.set(FXCollections.observableArrayList(io_BlobsList));
        ListViewBlobsData.itemsProperty().bind(m_BlobsList);
        ListViewBlobsData.setOnMouseClicked(mouseEvent -> {
            BlobData selectedBlob = ListViewBlobsData.getSelectionModel().getSelectedItem();
            TextAreaBlobContent.setText(selectedBlob.GetFileContent());
        });
    }


    public void setPrimaryStage(Stage i_PrimaryStage) {
        m_PrimaryStage = i_PrimaryStage;
    }

    private void initializeUncommittedFilesList() {
        m_UnCommittedNewFilesList = new SimpleListProperty<>();
        m_UnCommittedListFilesThatChanged = new SimpleListProperty<>();
        m_UncommittedRemovedFilesList = new SimpleListProperty<>();
    }

    private void clearUncommittedFilesList() {
        m_UnCommittedNewFilesList.clear();
        m_UnCommittedListFilesThatChanged.clear();
        m_UncommittedRemovedFilesList.clear();
    }

    private void resetUncommittedFilesList() {
        m_UnCommittedNewFilesList.set(FXCollections.observableArrayList(Collections.emptyList()));
        m_UnCommittedListFilesThatChanged.set(FXCollections.observableArrayList(Collections.emptyList()));
        m_UncommittedRemovedFilesList.set(FXCollections.observableArrayList(Collections.emptyList()));
        listViewUncommittedNewFiles.itemsProperty().unbind();
        listViewFilesThatChanged.itemsProperty().unbind();
        listViewUncommittedRemovedFiles.itemsProperty().unbind();
        listViewUncommittedNewFiles.itemsProperty().bind(m_UnCommittedNewFilesList);
        listViewFilesThatChanged.itemsProperty().bind(m_UnCommittedListFilesThatChanged);
        listViewUncommittedRemovedFiles.itemsProperty().bind(m_UncommittedRemovedFilesList);
    }

    public MainController() {
        m_RepositoryManager = null;
        m_UserName = new SimpleStringProperty("Administrator");
        m_RepositoryAddress = new SimpleStringProperty("No repository");
        m_IsRepositorySelected = new SimpleBooleanProperty(false);
        m_IsCommitSha1CheckBoxSelectd = new SimpleBooleanProperty(false);
        initializeUncommittedFilesList();
        m_BranchesList = new SimpleListProperty<>();
        m_BlobsList = new SimpleListProperty<>();
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
                isCommitNecessary = m_RepositoryManager.HandleCommit(commitComment, null);
            } catch (Exception e) {
                new Alert(Alert.AlertType.ERROR, e.getMessage()).showAndWait();
            }

            String reportString = isCommitNecessary ? "Commit successful" : "No changes were made, commit unnecessary";
            new Alert(Alert.AlertType.INFORMATION, reportString).showAndWait();
            textAreaCommitComment.clear();
            buildBranchList();
            clearUncommittedFilesList();
        }
    }


//    private List<BlobData> importUnCommittedFilesList() {
//        List<BlobData> unCommittedFilesList = null;
//        try {
//            unCommittedFilesList = new LinkedList<>();
//            List<List<BlobData>> allUnCommittedFilesList = m_RepositoryManager.GetListOfUnCommittedFiles(m_RepositoryManager.getRootFolder(), m_RepositoryManager.GetCurrentUserName());
//            unCommittedFilesList.addAll(allUnCommittedFilesList.get(0));
//            unCommittedFilesList.addAll(allUnCommittedFilesList.get(1));
//            unCommittedFilesList.addAll(allUnCommittedFilesList.get(2));
//        } catch (IOException ex) {
//            new Alert(Alert.AlertType.ERROR, "cant reload uncommitted changes").showAndWait();
//            //System.out.println("Action failed");
//        }
//        return unCommittedFilesList;
//    }

    @FXML
    private void handleShowWorkingCopyList(ActionEvent event) {
        //List<String> unCommittedFilesList = importUnCommittedFilesList();
        updateWCList();
    }

    private void updateWCList() {
        List<UnCommittedChange> allUnCommittedFilesList = null;
        try {
            allUnCommittedFilesList = m_RepositoryManager.GetListOfUnCommittedFiles(m_RepositoryManager.getRootFolder(), m_RepositoryManager.GetCurrentUserName());

            List<String> unCommittedNewFilesList = new LinkedList<>();
            allUnCommittedFilesList
                    .stream()
                    .filter(unCommittedChange -> unCommittedChange.getChangeType().equals("added"))
                    .forEach(blobData -> unCommittedNewFilesList.add(blobData.getFile().GetPath()));

            List<String> unCommittedListFilesThatChanged = new LinkedList<>();
            allUnCommittedFilesList
                    .stream()
                    .filter(unCommittedChange -> unCommittedChange.getChangeType().equals("updated"))
                    .forEach(blobData -> unCommittedListFilesThatChanged.add(blobData.getFile().GetPath()));

            List<String> unCommittedRemovedFilesList = new LinkedList<>();
            allUnCommittedFilesList
                    .stream()
                    .filter(unCommittedChange -> unCommittedChange.getChangeType().equals("deleted"))
                    .forEach(blobData -> unCommittedRemovedFilesList.add(blobData.getFile().GetPath()));

            m_UnCommittedNewFilesList.set(FXCollections.observableArrayList(unCommittedNewFilesList));
            m_UnCommittedListFilesThatChanged.set(FXCollections.observableArrayList(unCommittedListFilesThatChanged));
            m_UncommittedRemovedFilesList.set(FXCollections.observableArrayList(unCommittedRemovedFilesList));
        } catch (IOException e) {
            e.printStackTrace();
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
                    buildBranchList();
                    clearUncommittedFilesList();
                }
            }

        } else {
            new Alert(Alert.AlertType.ERROR, "can't merge without at least one commit ").showAndWait();
        }
    }

    private void drawConflictDialog(List<Conflict> io_Conflicts, Commit i_CommitToMerge) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("MergeComponentGui.fxml"));
            Parent parent = fxmlLoader.load();
            MergeController dialogController = fxmlLoader.getController();
            // dialogController.setAppMainObservableList(tvObservableList);
            Scene scene = new Scene(parent, 1250, 700);
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(scene);
            dialogController.SetCommitToMerge(i_CommitToMerge);
            dialogController.SetRepository(m_RepositoryManager);
            dialogController.SetConflictsListProperty(io_Conflicts);
            stage.showAndWait();
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
    }

    private Boolean handleMerge(String i_BranchName) {
        boolean returnVal = false;
        if (m_RepositoryManager != null && m_RepositoryManager.GetHeadBranch() != null) {
            try {
                if (!m_RepositoryManager.IsUncommittedFilesInRepository(m_RepositoryManager.getRootFolder(), m_RepositoryManager.GetCurrentUserName())) {

                    List<Conflict> conflictsList = new LinkedList<>();
                    returnVal = m_RepositoryManager.HandleMerge(i_BranchName, conflictsList);
                    boolean isUncommittedFile = m_RepositoryManager.IsUncommittedFilesInRepository(m_RepositoryManager.getRootFolder(), m_RepositoryManager.GetCurrentUserName());
                    if (returnVal && (conflictsList.size() > 0 || isUncommittedFile)) {
                        drawConflictDialog(conflictsList, m_RepositoryManager.FindBranchByName(i_BranchName).GetCurrentCommit());
                    } else if (returnVal) {
                        new Alert(Alert.AlertType.ERROR, "you trying to merge 2 branches that fully merged").showAndWait();
                        returnVal = false;
                    } else {
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
        clearUncommittedFilesList();
    }

    private boolean handleCheckout(String i_BranchName) {
        boolean returnVal = false;
        if (m_RepositoryManager != null && m_RepositoryManager.GetHeadBranch() != null) {

            try {
                if (!m_RepositoryManager.IsUncommittedFilesInRepository(m_RepositoryManager.getRootFolder(), m_RepositoryManager.GetCurrentUserName())) {
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
                clearUncommittedFilesList();
            }
        }
    }

    private void openNewBranchDialog() {
        TextInputDialog dialog = new TextInputDialog("New Branch");
        dialog.setTitle("New Branch");
        dialog.setContentText("Please enter branch name:");

        Optional<String> result = dialog.showAndWait();
        if (!result.equals(Optional.empty())) {

            String branchName = result.get();
            if (!TextFieldCommitSha1.isDisable()) {
                if (TextFieldCommitSha1.getText().equals("")) {
                    new Alert(Alert.AlertType.ERROR, "Enter Commit Sha1, Or Remove the sign From the CheckBox").showAndWait();
                } else {
                    handleBranchNewCreation(branchName, TextFieldCommitSha1.getText());
                    TextFieldCommitSha1.clear();
                    CheckBoxCommitSha1.setDisable(true);
                }
            } else {
                handleBranchNewCreation(branchName, "");
            }
        }

    }

    private void handleBranchNewCreation(String i_BranchName, String i_CommitSha1) {
        Commit commit = null;

        if (!i_CommitSha1.equals("")) {
            commit = m_RepositoryManager.FindCommitInAllBranches(i_CommitSha1);
        }
        if (!m_RepositoryManager.IsBranchExist(i_BranchName) && !i_CommitSha1.equals("") && commit == null) {
            new Alert(Alert.AlertType.ERROR, "The Commit with sha1:" + i_CommitSha1 + " doesnt exist").showAndWait();
        } else if (!m_RepositoryManager.IsBranchExist(i_BranchName)) {
            m_RepositoryManager.HandleBranch(i_BranchName, commit);
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

    @FXML
    void handelCommitSha1CheckBox(ActionEvent event) {
        TextFieldCommitSha1.setDisable(m_IsCommitSha1CheckBoxSelectd.getValue());
        m_IsCommitSha1CheckBoxSelectd.set(!m_IsCommitSha1CheckBoxSelectd.getValue());
    }

    private void rebindListViews() {
        initializeUncommittedFilesList();
        m_BranchesList = new SimpleListProperty<>();
        buildBranchList();
        listViewBranchList.itemsProperty().unbind();
        listViewBranchList.itemsProperty().bind(m_BranchesList);
        resetUncommittedFilesList();
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

    @FXML
    private void handleResetHeadBranchClick(ActionEvent event) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Reset Head");
        dialog.setHeaderText("Look, a Text Input Dialog");
        dialog.setContentText("Please enter commit sha1");
        Optional<String> result = dialog.showAndWait();
        try {
            Commit commit = null;
            String commitSha1 = "";
            if (!result.equals(Optional.empty())) {
                commitSha1 = result.get();
                commit = m_RepositoryManager.FindCommitInAllBranches(commitSha1);
            }
            if (commit == null) {
                new Alert(Alert.AlertType.ERROR, "The Commit with sha1: " + commitSha1 + " doesnt exist").showAndWait();
            } else if (m_RepositoryManager.IsUncommittedFilesInRepository(m_RepositoryManager.getRootFolder(), m_RepositoryManager.GetCurrentUserName())) {
                showUncommittedFilesinRepositoryDialogue(commit);

            } else {
                m_RepositoryManager.GetHeadBranch().GetHeadBranch().SetCurrentCommit(commit);
                m_RepositoryManager.HandleCheckout(m_RepositoryManager.GetHeadBranch().GetHeadBranch().GetBranchName());
                buildBranchList();
                clearUncommittedFilesList();
            }

        } catch (Exception e) {

        }
    }

    private void showUncommittedFilesinRepositoryDialogue(Commit i_Commit) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Uncommitted Files Dialogue");
        alert.setHeaderText("There are Uncommitted Files in this branch");
        alert.setContentText("Choose your option.");
        ButtonType buttonTypeRevert = new ButtonType("Revert all");
        ButtonType buttonTypeCancel = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(buttonTypeRevert, buttonTypeCancel);
        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == buttonTypeRevert) {
            m_RepositoryManager.GetHeadBranch().GetHeadBranch().SetCurrentCommit(i_Commit);
            m_RepositoryManager.HandleCheckout(m_RepositoryManager.GetHeadBranch().GetHeadBranch().GetBranchName());
            buildBranchList();
            clearUncommittedFilesList();
        } else {
            // ... user chose CANCEL or closed the dialog
        }
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
                System.out.println("Delete existing repository failed, make sure all local files in repository are not in use");
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
        tabCommit.selectedProperty().addListener((observable, oldValue, newValue) -> updateWCList());
    }
}
