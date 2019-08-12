package logicpackage;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;

public class Branch {
    private Commit m_CurrentCommit;
    private String m_BranchName;
    private String m_BranchSha1;
    private Branch m_ParntBranch;
    private boolean m_IsHeadBranch;
    private Path m_RepositoryPath;


    public Branch(String i_BranchName, Commit i_Commit, Path i_RepositoryPath)//for the first Branch in the git.
    {
        m_RepositoryPath = i_RepositoryPath;
        m_IsHeadBranch = true;
        m_BranchName = i_BranchName;
        m_CurrentCommit = i_Commit;
        m_ParntBranch = null;
        m_BranchSha1 = FilesManagement.CreateBranchFile(i_BranchName, i_Commit, i_RepositoryPath);
    }

    public Branch(String i_BranchName, Branch i_ParentBranch,HeadBranch i_HeadBranch, Path i_RepositoryPath) {
        m_RepositoryPath = i_RepositoryPath;
        m_IsHeadBranch = false;
        m_BranchName = i_BranchName;
        m_CurrentCommit = i_ParentBranch.m_CurrentCommit;
        m_ParntBranch = i_ParentBranch;
        m_BranchSha1 = FilesManagement.CreateBranchFile(i_BranchName, i_ParentBranch.m_CurrentCommit, i_RepositoryPath);
    }

    public void UpdateBranchCommit(Commit newCommit) {
        m_BranchSha1 =  FilesManagement.UpdateBranchFile(this, newCommit, m_RepositoryPath);
    }


    public String getBranchName() {
        return m_BranchName;
    }

    public void setBranchName(String i_BranchName) {
        this.m_BranchName = i_BranchName;
    }

    public String getBranchSha1() {
        return m_BranchSha1;
    }

}
