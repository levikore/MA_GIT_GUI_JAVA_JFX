package components;

import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

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

    @FXML
    private void initialize() {
        textFieldRemoteRepository.textProperty().bind(m_RemotePath);
        textFieldLocalRepository.textProperty().bind(m_LocalPath);
    }

    public void SetStage(Stage i_Stage){
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
    private void handleRemoteDirectoryChooser(){
        File directory = getFileFromDirectoryChooser("Remote repository chooser");
        if(directory!=null) {
            m_RemotePath.set(directory.toString());
        }
    }

}
