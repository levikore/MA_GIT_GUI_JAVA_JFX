package components;

import com.fxgraph.edges.Edge;
import com.fxgraph.graph.Graph;
import com.fxgraph.graph.ICell;
import com.fxgraph.graph.Model;
import commitTreePackage.layout.CommitTreeLayout;
import commitTreePackage.node.CommitNode;
import javafx.collections.ObservableList;
import logicpackage.Branch;
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

        i_RepositoryManager.SortBranchesList();//sorted branches mew change!!!!!!!

        for (Commit commit : i_CommitList) {
            ICell cell =getCellByCommit(commit, i_RepositoryManager);

            i_Model.addCell(cell);

            List<ICell> prevCells = getPreviousCellsList(commit, i_RepositoryManager, addLaterList);
            treeNodeMap.put(cell, prevCells);
        }

        connectNodes(treeNodeMap, i_Model);
    }

    private static ICell getCellByCommit(Commit i_Commit, RepositoryManager i_RepositoryManager){
        Branch branch = i_RepositoryManager.GetBranchByCommit(i_Commit);
        Integer branchNumber = i_RepositoryManager.GetBranchNumber(branch);
        String BranchName = branch!=null ? (branch.GetCurrentCommit().equals(i_Commit) ? branch.GetBranchName() : "") : "";
        ICell cell = new CommitNode(
                i_Commit.GetCreationDate(),
                i_Commit.GetCreatedBy(),
                i_Commit.GetCommitComment(),
                i_Commit.GetCurrentCommitSHA1(),
                i_Commit.GetPreviousCommitsSHA1String(),
                i_Commit.GetDeltaString(),
                branchNumber,
                BranchName);

        return cell;
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
                ICell prevCell = getCellByCommit(prevCommit, i_RepositoryManager);
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
