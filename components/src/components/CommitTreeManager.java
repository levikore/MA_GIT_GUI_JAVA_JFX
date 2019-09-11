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

import java.util.*;

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
        HashMap<ICell, List<ICell>> treeNodeMap = new HashMap<>();
        List<ICell> addLaterList = new LinkedList<>();

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

            List<ICell> prevCells = getPreviousCellsList(commit, i_RepositoryManager, addLaterList);
            treeNodeMap.put(cell, prevCells);
        }

        connectNodes(treeNodeMap, i_Model);
    }

    private static void connectNodes(HashMap<ICell, List<ICell>> i_TreeNodeMap, Model i_Model){

        for(Map.Entry<ICell, List<ICell>> node : i_TreeNodeMap.entrySet()) {
            ICell cell = node.getKey();
            List<ICell> previousCells = node.getValue();

            for(ICell prevCell : previousCells){
                ICell prevCellInModel =  findCellInModel(prevCell, i_Model);
                i_Model.addEdge(new Edge(cell, prevCellInModel));
            }
        }
    }

    private static List<ICell> getPreviousCellsList(Commit i_Commit, RepositoryManager i_RepositoryManager, List<ICell> io_AddLaterList) {
        List<ICell> prevCells = new LinkedList<>();
        if (i_Commit.GetPrevCommitsList() != null) {
            for (Commit prevCommit : i_Commit.GetPrevCommitsList()) {
                ICell prevCell = new CommitNode(
                        prevCommit.GetCreationDate(),
                        prevCommit.GetCreatedBy(),
                        prevCommit.GetCommitComment(),
                        prevCommit.GetCurrentCommitSHA1(),
                        prevCommit.GetPreviousCommitsSHA1String(),
                        prevCommit.GetDeltaString(),
                        i_RepositoryManager.GetBranchNumberByCommit(prevCommit));
                io_AddLaterList.add(prevCell);
                prevCells.add(prevCell);
            }
        }

        return prevCells;
    }

    private static ICell findCellInModel(ICell i_Cell, Model i_Model) {
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
}
