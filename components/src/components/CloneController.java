package components;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import logicpackage.CollaborationManager;
import logicpackage.FilesManagement;

import java.awt.*;
import java.io.File;
import java.nio.file.Paths;

public class CloneController {
    @FXML
    private TextField textFieldRemoteRepository;
    @FXML
    private TextField textFieldRepositoryName;
    @FXML
    private TextField textFieldLocalRepository;
    @FXML
    public Button buttonRemoteRepository;
    @FXML
    private Button buttonLocalRepository;


    private Stage m_Stage;
    private StringProperty m_RemotePath;
    private StringProperty m_LocalPath;

    public CloneController() {
        m_RemotePath = new SimpleStringProperty("");
        m_LocalPath = new SimpleStringProperty("");
    }

    @FXML
    private void initialize() {
        textFieldRemoteRepository.textProperty().bind(m_RemotePath);
        textFieldLocalRepository.textProperty().bind(m_LocalPath);
    }

    public void SetStage(Stage i_Stage) {
        m_Stage = i_Stage;
    }

    private File getFileFromDirectoryChooser(String i_Title) {
        File result = null;
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle(i_Title);
        File directory = directoryChooser.showDialog(m_Stage);

        if (directory != null) {
            String loweCaseDirectoryPathString = directory.getAbsolutePath().toLowerCase();
            result = Paths.get(loweCaseDirectoryPathString).toFile();
        }

        return result;
    }


    @FXML
    private void handleRemoteDirectoryChooser() {
        File directory = getFileFromDirectoryChooser("Remote repository chooser");
        if (directory != null) {
            if (FilesManagement.IsRepositoryExistInPath(directory.toString())) {
                m_RemotePath.set(directory.toString());
            } else {
                new Alert(Alert.AlertType.ERROR, "No directory in path").showAndWait();
            }
        }
    }

    @FXML
    private void handleLocalDirectoryChoose() {
        String repositoryName = textFieldRepositoryName.getText();
        if (repositoryName.isEmpty()) {
            new Alert(Alert.AlertType.INFORMATION, "Write local repository name").showAndWait();
        } else if (isInValidName(repositoryName)) {
            new Alert(Alert.AlertType.INFORMATION, "Invalid repository name").showAndWait();
        } else {
            File directory = getFileFromDirectoryChooser("Local repository directory chooser");
            m_LocalPath.set(directory.toString() + "\\" + textFieldRepositoryName.getText());
        }
    }

    @FXML
    private void handleConfirmButton() {
        String remoteRepositoryPath = textFieldRemoteRepository.getText();
        String localRepositoryPath = textFieldLocalRepository.getText();

        CollaborationManager.CloneRepository(Paths.get(remoteRepositoryPath), Paths.get(localRepositoryPath));

        /*if (remoteRepositoryPath.isEmpty() && localRepositoryPath.isEmpty()) {
            new Alert(Alert.AlertType.INFORMATION, "Choose remote and local path").showAndWait();
        } else {
            System.out.println(remoteRepositoryPath + "\n" + localRepositoryPath);
        }*/

    }

    private boolean isInValidName(String i_Name) {
        return i_Name.contains("\\") ||
                i_Name.contains("/") || i_Name.contains(":") ||
                i_Name.contains("*") || i_Name.contains("?") ||
                i_Name.contains(">") || i_Name.contains("<") ||
                i_Name.contains("|");
    }


}
