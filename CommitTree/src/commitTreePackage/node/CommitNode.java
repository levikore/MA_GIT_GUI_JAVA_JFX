package commitTreePackage.node;

import com.fxgraph.cells.AbstractCell;
import com.fxgraph.graph.Graph;
import com.fxgraph.graph.IEdge;
import javafx.beans.binding.DoubleBinding;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;

import java.io.IOException;
import java.net.URL;
import java.util.List;

public class CommitNode extends AbstractCell {

    private String timestamp;
    private String committer;
    private String message;

    private String Sha1;
    private String prevCommitSHA1;
    private String delta;
    private Integer branchNumber;
    private String branchName;

    private CommitNodeController commitNodeController;

    public CommitNode(String timestamp, String committer, String message, String Sha1, String prevCommitSHA1, String delta, Integer branchNumber, String branchName) {
        this.timestamp = timestamp;
        this.committer = committer;
        this.message = message;

        this.Sha1 = Sha1;
        this.prevCommitSHA1 = prevCommitSHA1;
        this.delta = delta;
        this.branchNumber = branchNumber;
        this.branchName = branchName;
    }

    public Integer GetBranchNumber() {
        return this.branchNumber;
    }

    private String getCommitInformationString() {
        return String.format(
                "SHA1: %s \n" +
                        "Previous Commits SHA1: %s\n" +
                        "Delta: %s",
                Sha1, prevCommitSHA1, delta);
    }

    @Override
    public Region getGraphic(Graph graph) {

        try {

            FXMLLoader fxmlLoader = new FXMLLoader();
            URL url = getClass().getResource("commitNode.fxml");
            fxmlLoader.setLocation(url);
            GridPane root = fxmlLoader.load(url.openStream());

            commitNodeController = fxmlLoader.getController();
            commitNodeController.setCommitMessage(message);
            commitNodeController.setCommitter(committer);
            commitNodeController.setCommitTimeStamp(timestamp);
            commitNodeController.setCommitBranchName(branchName);
            commitNodeController.SetInformationText(getCommitInformationString());

            return root;
        } catch (IOException e) {
            return new Label("Error when tried to create graphic node !");
        }
    }

    @Override
    public DoubleBinding getXAnchor(Graph graph, IEdge edge) {
        final Region graphic = graph.getGraphic(this);
        return graphic.layoutXProperty().add(commitNodeController.getCircleRadius());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CommitNode that = (CommitNode) o;

        return timestamp != null ? timestamp.equals(that.timestamp) : that.timestamp == null;
    }

    @Override
    public int hashCode() {
        return timestamp != null ? timestamp.hashCode() : 0;
    }
}
