package logicpackage;

import java.nio.file.Path;

public class HeadBranch {
    private Branch m_HeadBranch;
    private String m_HeadBranchSha1;
    private Path m_RepositoryPath;

    public HeadBranch(Branch i_HeadBranch, Path i_RepositoryPath, boolean i_IsNewHead, String i_HeadBrenchSha1) {
        m_RepositoryPath = i_RepositoryPath;
        m_HeadBranch = i_HeadBranch;
        if (i_IsNewHead)
            m_HeadBranchSha1 = FilesManagement.CreateHeadFile(i_HeadBranch, m_RepositoryPath);
        else
            m_HeadBranchSha1 = i_HeadBrenchSha1;
    }

    public void updateCurrentBranch(Commit i_NewCommit) {
        m_HeadBranch.UpdateBranchCommit(i_NewCommit);
        m_HeadBranchSha1 = FilesManagement.UpdateHeadFile(m_HeadBranch, m_RepositoryPath);
    }

    public Branch getBranch() {
        return m_HeadBranch;
    }

    public void checkout(Branch i_HeadBranch) {
        setHeadBranch(i_HeadBranch);
        FilesManagement.CleanWC(m_RepositoryPath);
        m_HeadBranch.getCurrentCommit().getRootFolder().RecoverWCFromCurrentRootFolderObj();
    }

    public void setHeadBranch(Branch i_HeadBranch) {
        this.m_HeadBranch = i_HeadBranch;
        m_HeadBranchSha1 = FilesManagement.UpdateHeadFile(i_HeadBranch, m_RepositoryPath);
    }


    public Branch getHeadBranch() {
        return m_HeadBranch;
    }

    public String getHeadBranchSha1() {
        return m_HeadBranchSha1;
    }

    public Path getRepositoryPath() {
        return m_RepositoryPath;
    }

}

