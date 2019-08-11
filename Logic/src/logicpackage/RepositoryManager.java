package logicpackage;

import java.nio.file.Path;
import java.nio.file.Paths;

public class RepositoryManager {

    private String m_CurrentUserName;
    private String m_RepositoryName;
    private Path m_RepositoryPath;
    private RootFolder m_RootFolder;
    private HeadBranch m_HeadBranch;
    private Commit m_CurrentCommit;
    private Boolean isFirstCommit = true;
    private Path m_MagitPath;

    public RepositoryManager(Path i_RepositoryPath, String i_CurrentUserName) {
        m_RepositoryPath = i_RepositoryPath;
        m_RepositoryName = m_RepositoryPath.toFile().getName();
        m_CurrentUserName = i_CurrentUserName;
        m_MagitPath = Paths.get(m_RepositoryPath.toString() + "\\.magit");

        intializeRepository();
    }

    private void intializeRepository() {
        initializeRootFolder();
        createSystemFolders();
    }

    private void createSystemFolders() {
        new Folder(m_RepositoryPath, ".magit");
        new Folder(m_MagitPath, "objects");
        new Folder(m_MagitPath, "branches");
    }

    private void createNewCommit(String i_CommitComment, String i_NameBranch) {
        Commit newCommit = null;
        if (isFirstCommit) {
            newCommit = new Commit(m_RootFolder, i_CommitComment, m_CurrentUserName, null);
            isFirstCommit = false;
        } else {
            newCommit = new Commit(m_RootFolder, i_CommitComment, m_CurrentUserName, m_CurrentCommit);
        }

        m_CurrentCommit = newCommit;
        String sha1 = FilesManagement.CreateCommitDescriptionFile(m_CurrentCommit, m_RepositoryPath);
        m_CurrentCommit.setCurrentCommitSHA1(sha1);
        if(m_HeadBranch==null)
        {
            Branch branch=new Branch("myHeadBranch", m_CurrentCommit, m_RepositoryPath);
            m_HeadBranch=new HeadBranch(branch, m_RepositoryPath);
        }
    }

    private void initializeRootFolder() {
        Folder rootFolder = new Folder(m_RepositoryPath.getParent(), m_RepositoryPath.toFile().getName());
        BlobData rootFolderBlobData = new BlobData(m_RepositoryPath.toFile().toString(), rootFolder);
        m_RootFolder = new RootFolder(rootFolderBlobData, m_RepositoryPath);
    }

    public void HandleCommit(String i_CommitComment) {

        initializeRootFolder();
        m_RootFolder.UpdateCurrentRootFolderSha1(m_CurrentUserName);
        createNewCommit(i_CommitComment,"");
    }

    public static Boolean IsCommitNecessary() {
        return true;
    }


}
