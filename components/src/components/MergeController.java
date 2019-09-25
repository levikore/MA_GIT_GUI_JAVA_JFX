package components;

import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import logicpackage.Commit;
import logicpackage.Conflict;
import logicpackage.FilesManagement;
import logicpackage.RepositoryManager;
import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;

public class MergeController {

    @FXML
    private TextArea TextAreaMergeOutput;

    @FXML
    private Button ButtonSubmit;

    @FXML
    private TextArea TextAreaOursBranch;

    @FXML
    private TextArea TextAreaTheirBranch;

    @FXML
    private TextArea TextAreaAncestorBranch;

    @FXML
    private ListView<Conflict> ListViewConflicts;

    private SimpleListProperty<Conflict> m_ConflictsList;

    @FXML
    private Button ButtonMergeCommit;

    @FXML
    private TextArea textAreaCommitComment;

    private Commit m_CommitToMerge;

    private RepositoryManager m_RepositoryManager;

    //private List<Conflict> m_ConflictsList;

    @FXML
    private void initialize() {

    }

    @FXML
    void handleButtonMergeCommitClick(ActionEvent event) {
        String commitComment = textAreaCommitComment.getText();
        if (commitComment.isEmpty()) {
            new Alert(Alert.AlertType.INFORMATION, "Insert commit comment").showAndWait();
        } else {
            Boolean isCommitNecessary = false;
            try {
                isCommitNecessary = m_RepositoryManager.HandleCommit(commitComment, m_CommitToMerge);
            } catch (Exception e) {
                new Alert(Alert.AlertType.ERROR, e.getMessage()).showAndWait();
            }

            String reportString = isCommitNecessary ? "Commit successful" : "No changes were made, commit unnecessary";
            new Alert(Alert.AlertType.INFORMATION, reportString).showAndWait();
            textAreaCommitComment.clear();
            Stage stage = (Stage) ButtonMergeCommit.getScene().getWindow();
            stage.close();
        }
    }

    public MergeController() {
        m_CommitToMerge = new Commit();
        ListViewConflicts = new ListView<>();
        m_ConflictsList = new SimpleListProperty<>();
    }

    public void SetCommitToMerge(Commit i_CommitToMerge) {
        m_CommitToMerge = i_CommitToMerge;
    }

    public void SetRepository(RepositoryManager i_Repository) {
        m_RepositoryManager = i_Repository;
    }

    public void SetConflictsListProperty(List<Conflict> io_Conflicts) {
        List<String> conflictsList = new LinkedList<>();
        io_Conflicts.forEach(conflict -> conflictsList.add(conflict.toString()));
        m_ConflictsList.set(FXCollections.observableArrayList(io_Conflicts));
        ListViewConflicts.itemsProperty().bind(m_ConflictsList);
        ListViewConflicts.setOnMouseClicked(mouseEvent -> {
            Conflict selectedConflict = ListViewConflicts.getSelectionModel().getSelectedItem();
            updateTextAreas(selectedConflict);
        });

        if (m_ConflictsList.size() == 0) {
            ButtonMergeCommit.setDisable(false);
        }
    }

    private void updateTextAreas(Conflict i_Conflict) {
        if(!(i_Conflict.getOurFile().getChangeType().equals("deleted"))){
            TextAreaOursBranch.setText(i_Conflict.getOurFile().getFile().GetFileContent());
        }
        if(!(i_Conflict.getTheirsFile().getChangeType().equals("deleted"))) {
            TextAreaTheirBranch.setText(i_Conflict.getTheirsFile().getFile().GetFileContent());
        }
        if (i_Conflict.getAncestor() != null) {
            TextAreaAncestorBranch.setText(i_Conflict.getAncestor().GetFileContent());
        } else {
            TextAreaAncestorBranch.clear();
        }
        TextAreaMergeOutput.setDisable(false);
        ButtonSubmit.setDisable(false);
    }

    @FXML
    void handleButtonSubmitClick(ActionEvent event) {
        String content = TextAreaMergeOutput.getText();
        Conflict selectedConflict = ListViewConflicts.getSelectionModel().getSelectedItem();
        Path pathToSave= Paths.get(selectedConflict.getOurFile().getFile().GetPath());
        FilesManagement.RemoveFileByPath(pathToSave);
        try {
            FileUtils.writeStringToFile(pathToSave.toFile(), content, "utf-8");
        }
        catch (IOException ex)
        {
            System.out.println(ex.getMessage());
        }
        m_ConflictsList.remove(selectedConflict);
        SetConflictsListProperty(m_ConflictsList);
        clearAllTextAreas();
    }

    void clearAllTextAreas()
    {
        TextAreaAncestorBranch.clear();
        TextAreaTheirBranch.clear();
        TextAreaMergeOutput.clear();
        TextAreaOursBranch.clear();
        TextAreaMergeOutput.setDisable(true);
        ButtonSubmit.setDisable(true);
    }


}
