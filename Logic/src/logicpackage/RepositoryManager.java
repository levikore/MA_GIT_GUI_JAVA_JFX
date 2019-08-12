package logicpackage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;

public class RepositoryManager {

    private String m_CurrentUserName;
    private String m_RepositoryName;
    private Path m_RepositoryPath;
    private RootFolder m_RootFolder;
    private HeadBranch m_HeadBranch;
    private Commit m_CurrentCommit;
    private Boolean isFirstCommit = true;
    private Path m_MagitPath;
    private List<Branch> m_AllBranchesList = new LinkedList<>();


    private final String c_GitFolderName = ".magit";
    private final String c_ObjectsFolderName = "objects";
    private final String c_BranchesFolderName = "branches";
    private final String c_TestFolderName = "test";

    public RepositoryManager(Path i_RepositoryPath, String i_CurrentUserName) {
        m_RepositoryPath = i_RepositoryPath;
        m_RepositoryName = m_RepositoryPath.toFile().getName();
        m_CurrentUserName = i_CurrentUserName;
        m_MagitPath = Paths.get(m_RepositoryPath.toString() + "\\" + c_GitFolderName);

        intializeRepository();
    }

    private void intializeRepository() {
        m_RootFolder = getInitializedRootFolder();
        createSystemFolders();
    }

    private void createSystemFolders() {
        new Folder(m_RepositoryPath, c_GitFolderName);
        new Folder(m_MagitPath, c_ObjectsFolderName);
        new Folder(m_MagitPath, c_BranchesFolderName);
    }

    private void createNewCommit(String i_CommitComment/*, String i_NameBranch*/) {
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
        if (m_HeadBranch == null) {
            Branch branch = new Branch("master", m_CurrentCommit, m_RepositoryPath);
            m_AllBranchesList.add(branch);
            m_HeadBranch = new HeadBranch(branch, m_RepositoryPath);
        } else {
            m_HeadBranch.updateCurrentBranch(m_CurrentCommit);
        }
    }

    private RootFolder getInitializedRootFolder() {
        Folder rootFolder = new Folder(m_RepositoryPath.getParent(), m_RepositoryPath.toFile().getName());
        BlobData rootFolderBlobData = new BlobData(m_RepositoryPath.toFile().toString(), rootFolder);
        return new RootFolder(rootFolderBlobData, m_RepositoryPath);
    }

    public Boolean HandleCommit(String i_CommitComment) throws IOException {
        Boolean isCommitNecessary;

        if (isFirstCommit) {
            handleFirstCommit(i_CommitComment);
            isCommitNecessary = true;
        } else {
            isCommitNecessary = handleSecondCommit(i_CommitComment);
        }

        return isCommitNecessary;
    }

    public void HandleBranch(String i_BranchName) {
        Branch branch = new Branch(i_BranchName, m_HeadBranch.getBranch(), m_HeadBranch, m_RepositoryPath);
        m_AllBranchesList.add(branch);
    }

    private void handleFirstCommit(String i_CommitComment/*, String i_NameBranch*/) {
        m_RootFolder = getInitializedRootFolder();
        m_RootFolder.UpdateCurrentRootFolderSha1(m_CurrentUserName, "");
        createNewCommit(i_CommitComment);
    }

    private Boolean handleSecondCommit(String i_CommitComment) throws IOException {
        Boolean isCommitNecessary = false;
        new Folder(m_MagitPath, c_TestFolderName);
        RootFolder testRootFolder = getInitializedRootFolder();
        testRootFolder.UpdateCurrentRootFolderSha1(m_CurrentUserName, c_TestFolderName);

        if (!testRootFolder.getSHA1().equals(m_RootFolder.getSHA1())) {
            copyFiles(m_MagitPath + "\\" + c_TestFolderName, m_MagitPath + "\\" + c_ObjectsFolderName);
            m_RootFolder = testRootFolder;
            createNewCommit(i_CommitComment);
            isCommitNecessary = true;
        }

        ClearDirectory((Paths.get(m_MagitPath.toString() + "\\" + c_TestFolderName).toFile()));
        //Paths.get(m_MagitPath.toString() +"\\"+ c_TestFolderName).toFile().delete();

        return isCommitNecessary;
    }

    private void ClearDirectory(File i_directory) {
        File[] fileList = i_directory.listFiles();

        for (File file : fileList) {
            file.delete();
        }

        i_directory.delete();
    }

    private void copyFiles(String from, String to) throws IOException {
        Path source = Paths.get(from);
        Path destination = Paths.get(to);

        File[] fileList = source.toFile().listFiles();

        for (File file : fileList) {
            //Files.copy(Paths.get(file.toString()), destination);
            if (file.renameTo
                    (new File(destination + "\\" + file.getName()))) {
                // if file copied successfully then delete the original file
                file.delete();
                System.out.println("File moved successfully");
            }
        }
    }


}
