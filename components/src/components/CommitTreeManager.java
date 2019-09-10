package components;

import com.fxgraph.edges.Edge;
import com.fxgraph.graph.Graph;
import com.fxgraph.graph.ICell;
import com.fxgraph.graph.Model;
import commitTreePackage.layout.CommitTreeLayout;
import commitTreePackage.node.CommitNode;
import javafx.collections.ObservableList;
import logicpackage.Commit;
import logicpackage.RepositoryManager;

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

public class CommitTreeManager {
    public static void BuildTree(Graph graph, RepositoryManager i_RepositoryManager) {
        if (i_RepositoryManager.GetHeadBranch().GetBranch().GetCurrentCommit() != null) {
            final Model model = graph.getModel();
            List<Commit> branchCommitList = i_RepositoryManager.GetSortedAccessibleCommitList();
            graph.beginUpdate();
            buildTreeModel(branchCommitList, model, i_RepositoryManager);
            graph.endUpdate();
            graph.layout(new CommitTreeLayout());
        }
    }

    private static void buildTreeModel(List<Commit> i_CommitList, Model i_Model, RepositoryManager i_RepositoryManager) {
        List<Commit> onePrevCommitList = new LinkedList<>();
        List<Commit> twoPrevCommitList = new LinkedList<>();
        buildOpenCommitLists(i_CommitList, onePrevCommitList, twoPrevCommitList);
        for (Commit commit : i_CommitList) {
            ICell cell = new CommitNode(
                    commit.GetCreationDate(),
                    commit.GetCreatedBy(),
                    commit.GetCommitComment(),
                    commit.GetCurrentCommitSHA1(),
                    commit.GetPreviousCommitsSHA1String(),
                    commit.GetDeltaString(),
                    i_RepositoryManager.GetBranchNumberByCommit(commit));
            i_Model.addCell(cell);
            List<Commit> fatherCommits = findAndHandleFatherCommits(commit, onePrevCommitList, twoPrevCommitList);
            for (Commit fatherCommit : fatherCommits) {
                ICell fatherCell = new CommitNode(
                        fatherCommit.GetCreationDate(),
                        fatherCommit.GetCreatedBy(),
                        fatherCommit.GetCommitComment(),
                        fatherCommit.GetCurrentCommitSHA1(),
                        fatherCommit.GetPreviousCommitsSHA1String(),
                        fatherCommit.GetDeltaString(),
                        i_RepositoryManager.GetBranchNumberByCommit(commit));

                ICell fatherCellInModel = findCellInMode(fatherCell, i_Model);
                if (fatherCellInModel == null) {
                    i_Model.addCell(fatherCell);
                    fatherCellInModel = fatherCell;
                }

                i_Model.addEdge(new Edge(fatherCellInModel, cell));
            }
        }
    }

    private static ICell findCellInMode(ICell i_Cell, Model i_Model) {
        ObservableList<ICell> cellsList = i_Model.getAddedCells();
        ICell returnValue = null;
        for (ICell cell : cellsList) {
            if (cell.equals(i_Cell)) {
                returnValue = cell;
                break;
            }
        }

        return returnValue;
    }

    private static List<Commit> findAndHandleFatherCommits(Commit i_Commit, List<Commit> io_OnePrevCommitList, List<Commit> io_TwoPrevCommitList) {
        List<Commit> fathersList = findAndHandleFatherCommitsInCommitCollection(i_Commit, io_TwoPrevCommitList);
        List<Commit> newOnePrevCommitsList = new LinkedList<>();
        newOnePrevCommitsList.addAll(fathersList);

        fathersList = findAndHandleFatherCommitsInCommitCollection(i_Commit, io_OnePrevCommitList);
        io_OnePrevCommitList.addAll(newOnePrevCommitsList);

        return fathersList;
    }

    private static List<Commit> findAndHandleFatherCommitsInCommitCollection(Commit i_Commit, List<Commit> io_CommitList) {
        List<Commit> fathersList = new LinkedList<>();
        //List<Commit> newOnePrevCommitsList = new LinkedList<>();
        for (ListIterator<Commit> iter = io_CommitList.listIterator(); iter.hasNext(); ) {
            Commit commit = iter.next();
            for (Commit prevCommit : commit.GetPrevCommitsList()) {
                if (prevCommit.equals(i_Commit)) {
                    //newOnePrevCommitsList.add(commit);
                    fathersList.add(commit);
                    iter.remove();
                }
            }
        }

        return fathersList;
    }

    private static void buildOpenCommitLists(List<Commit> i_CommitList, List<Commit> i_onePrevCommitList, List<Commit> twoPrevCommitList) {
        for (Commit commit : i_CommitList) {
            if (commit.GetPrevCommitsList() != null) {
                if (commit.GetPrevCommitsList().size() == 1) {
                    i_onePrevCommitList.add(commit);
                } else if (commit.GetPrevCommitsList().size() == 2) {
                    twoPrevCommitList.add(commit);
                }
            }
        }
    }
}
