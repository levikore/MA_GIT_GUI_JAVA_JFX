package logicpackage;

import java.nio.file.Path;

public class HeadBranch {


    private Branch m_HeadBranch;
    private String m_HeadBranchSha1;
   private Path m_RepositoryPath;

    public HeadBranch(Branch i_HeadBranch, Path i_RepositoryPath)
    {
        m_RepositoryPath=i_RepositoryPath;
        m_HeadBranch=i_HeadBranch;
        m_HeadBranchSha1=FilesManagement.CreateHeadFile(i_HeadBranch, m_RepositoryPath);
    }

    public void updateCurrentBranch(Commit i_NewCommit){
        m_HeadBranch.UpdateBranchCommit(i_NewCommit);
        m_HeadBranchSha1=FilesManagement.UpdateHeadFile(m_HeadBranch,m_RepositoryPath);
    }

    public Branch getBranch() {
        return m_HeadBranch;
    }

    public void setHeadBranch(Branch i_HeadBranch) {
        this.m_HeadBranch = i_HeadBranch;
    }
}

