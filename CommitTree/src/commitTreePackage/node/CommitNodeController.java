package commitTreePackage.node;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.shape.Circle;

import java.awt.event.ActionEvent;

public class CommitNodeController {

    @FXML
    private Label commitTimeStampLabel;
    @FXML
    private Label messageLabel;
    @FXML
    private Label committerLabel;
    @FXML
    private Label branchLabel;
    @FXML
    private Circle CommitCircle;

    private String m_InformationText;

    public void setCommitTimeStamp(String timeStamp) {
        commitTimeStampLabel.setText(timeStamp);
        commitTimeStampLabel.setTooltip(new Tooltip(timeStamp));
    }

    public void setCommitter(String committerName) {
        committerLabel.setText(committerName);
        committerLabel.setTooltip(new Tooltip(committerName));
    }

    public void setCommitMessage(String commitMessage) {
        messageLabel.setText(commitMessage);
        messageLabel.setTooltip(new Tooltip(commitMessage));
    }

    public void setCommitBranchName(String branchName, String i_TrackingAfter) {
        if (!branchName.isEmpty()) {
            String trackingAfter = i_TrackingAfter == null ? "" : ", " + i_TrackingAfter;
            branchName = "[" + branchName + trackingAfter + "]";
            branchLabel.setTooltip(new Tooltip(branchName));
        }

        branchLabel.setText(branchName);
    }

    public int getCircleRadius() {
        return (int) CommitCircle.getRadius();
    }

    public void SetInformationText(String i_InformationText) {
        m_InformationText = i_InformationText;
    }

    @FXML
    private void handleClick() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Commit Information");
        alert.setHeaderText(branchLabel.getText() + "\n" + commitTimeStampLabel.getText() + "\n" + committerLabel.getText() + "\n" + messageLabel.getText());

        TextArea textArea = new TextArea(m_InformationText);
        textArea.setEditable(false);
        textArea.setWrapText(true);

        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);
        GridPane.setVgrow(textArea, Priority.ALWAYS);
        GridPane.setHgrow(textArea, Priority.ALWAYS);

        GridPane content = new GridPane();
        content.setMaxWidth(Double.MAX_VALUE);
        content.add(textArea, 0, 1);

        alert.getDialogPane().setExpandableContent(content);
        alert.showAndWait();
    }

}
