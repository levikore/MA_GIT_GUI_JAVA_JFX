package logicpackage;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class Branch {
    private Commit m_CurrentCommit;
    private String m_BranchName;
    private String m_BranchSha1;
    private boolean m_IsHeadBranch;
    private Path m_RepositoryPath;
    private Path m_BranchPath;
    private Branch m_ParentBranch;
    private Integer m_Depth;

    public Branch(String i_BranchName, Commit i_Commit, Path i_RepositoryPath, boolean i_IsNewBranch, String i_BranchSha1)//for the first Branch in the git.
    {
        m_RepositoryPath = i_RepositoryPath;
        m_BranchName = i_BranchName;
        m_CurrentCommit = i_Commit;
        m_BranchPath = Paths.get(m_RepositoryPath + "\\.magit\\branches\\" + i_BranchName + ".txt");
        m_ParentBranch = null;
        m_Depth = 0;

        if (i_IsNewBranch) {
            m_BranchSha1 = FilesManagement.CreateBranchFile(i_BranchName, i_Commit, i_RepositoryPath);
        } else {
            m_BranchSha1 = i_BranchSha1;
        }
    }

    public Branch(String i_BranchName, Branch i_ParentBranch, Path i_RepositoryPath, boolean i_IsNewBranch, String i_BranchSha1) {
        m_RepositoryPath = i_RepositoryPath;
        m_BranchName = i_BranchName;
        m_BranchPath = Paths.get(m_RepositoryPath + "\\.magit\\branches\\" + i_BranchName + ".txt");
        m_CurrentCommit = i_ParentBranch.m_CurrentCommit;
        m_ParentBranch = i_ParentBranch;
        m_Depth = i_ParentBranch.m_Depth + 1;

        if (i_IsNewBranch) {
            m_BranchSha1 = FilesManagement.CreateBranchFile(i_BranchName, i_ParentBranch.m_CurrentCommit, i_RepositoryPath);
        } else {
            m_BranchSha1 = i_BranchSha1;
        }
    }

    public void UpdateBranchCommit(Commit i_NewCommit) {
        m_BranchSha1 = FilesManagement.UpdateBranchFile(this, i_NewCommit, m_RepositoryPath);
        m_CurrentCommit = i_NewCommit;
    }

    public String GetBranchName() {
        return m_BranchName;
    }

    public void SetBranchName(String i_BranchName) {
        this.m_BranchName = i_BranchName;
    }

    public String GetBranchSha1() {
        return m_BranchSha1;
    }

    public Commit GetCurrentCommit() {
        return m_CurrentCommit;
    }

    public Branch GetParentBranch() {
        return m_ParentBranch;
    }

    public Path GetBranchPath() {
        return m_BranchPath;
    }
}
