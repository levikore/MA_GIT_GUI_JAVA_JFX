package logicpackage;

import java.nio.file.Path;
import java.nio.file.Paths;

public class RepositoryManager {

    private String m_CurrentUserName;
    private String m_RepositoryName;
    private Path m_RepositoryPath;
    private RootFolder m_RootFolder;
    private Commit m_CurrentCommit;
    private Boolean isFirstCommit = true;

    //public RootFolder GetRootFolder(){return m_RootFolder;}

    public RepositoryManager(Path i_RepositoryPath, String i_CurrentUserName) {
        m_RepositoryPath = i_RepositoryPath;
        m_RepositoryName = m_RepositoryPath.toFile().getName();
        m_CurrentUserName = i_CurrentUserName;
        //Path magitPath = Paths.get(m_Repostory.toString() + "\\.magit");

        intializeRepository();

        //IFilesManagement.createFolderDescriptionFile(m_Repostory,m_CurrentUserName);
        //IFilesManagement.createZipFile(m_Repostory.toString()+"\\txt1.txt");

    }

    private void intializeRepository() {
//    public BlobData(
//            String i_Path,
//            String i_SHA1,
//           Boolean i_IsFolder,
//           String i_LastChangedBY,
//           SimpleDateFormat i_LastChangedTime
//    ) {
//        m_Path = i_Path;
//        m_SHA1 = i_SHA1;
//        m_IsFolder = i_IsFolder;
//        m_LastChangedBY = i_LastChangedBY;
//       m_LastChangedTime = i_LastChangedTime;
//    }


        Path magitPath = Paths.get(m_RepositoryPath.toString() + "\\.magit");
        Path objectsPath = Paths.get(magitPath.toString() + "\\objects");
        Folder rootFolder = new Folder(m_RepositoryPath.getParent(), m_RepositoryPath.toFile().getName());
        BlobData rootFolderBlobData = new BlobData(m_RepositoryPath.toFile().toString(), rootFolder);
        new Folder(m_RepositoryPath, ".magit");
        new Folder(magitPath, "objects");
        new Folder(magitPath, "branches");

        //String rootFolderSha1=((objectsPath.toFile()).listFiles())[0].getName();
        m_RootFolder = new RootFolder(rootFolderBlobData, m_RepositoryPath);
    }

    private void createNewCommit(String i_CommitComment) {
        Commit newCommit = null;
        if (isFirstCommit) {
            newCommit = new Commit(m_RootFolder, i_CommitComment, m_CurrentUserName);
            isFirstCommit = false;
        } else {
            newCommit = new Commit(m_RootFolder, i_CommitComment, m_CurrentUserName, m_CurrentCommit);
        }
        m_CurrentCommit = newCommit;
        String sha1 = FilesManagement.CreateCommitDescriptionFile(m_CurrentCommit, m_RepositoryPath);
        m_CurrentCommit.setCurrentCommitSHA1(sha1);
    }

    public void HandleCommit(String i_CommitComment){
        m_RootFolder.UpdateCurrentRootFolderSha1(m_CurrentUserName);
        createNewCommit(i_CommitComment);
    }

    public static Boolean IsCommitNecessary(){
        return true;
    }


}
