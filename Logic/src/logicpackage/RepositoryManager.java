package logicpackage;

import java.nio.file.Path;
import java.nio.file.Paths;

public class RepositoryManager {

    private String m_CurrentUserName;
    private String m_RepositoryName;
    private Path m_RepostoryPath;
    private RootFolder m_RootFolder;
    private Commit m_CurruntCommit;
    private Boolean isFirstCommit = true;
//

    public RepositoryManager(Path i_RepositoryPath, String i_CurrentUserName) {
        m_RepostoryPath = i_RepositoryPath;
        m_RepositoryName = m_RepostoryPath.toFile().getName();
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


        Path magitPath = Paths.get(m_RepostoryPath.toString() + "\\.magit");
        Path objectsPath = Paths.get(magitPath.toString() + "\\objects");
        Folder rootFolder = new Folder(m_RepostoryPath.getParent(), m_RepostoryPath.toFile().getName());
        BlobData rootFolderBlobData = new BlobData(m_RepostoryPath.toFile().toString(), rootFolder);
        new Folder(m_RepostoryPath, ".magit");
        new Folder(magitPath, "objects");
        new Folder(magitPath, "branches");

        //String rootFolderSha1=((objectsPath.toFile()).listFiles())[0].getName();
        m_RootFolder = new RootFolder(rootFolderBlobData, m_RepostoryPath);
    }

    // public Commit(RootFolder i_RootFolder, String i_CommitComment, String i_CreatedBy, String i_UserName, Commit i_PrevCommit)
    public void CreateNewCommit(String i_CommitComment) {
        Commit newCommit = null;
        if (isFirstCommit) {
            newCommit = new Commit(m_RootFolder, i_CommitComment, m_CurrentUserName);
            isFirstCommit = false;
        } else {
            newCommit = new Commit(m_RootFolder, i_CommitComment, m_CurrentUserName, m_CurruntCommit.getPrevCommit());
        }
        m_CurruntCommit = newCommit;
        String sha1 = FilesManagement.CreateCommitDescriptionFile(m_CurruntCommit, m_RepostoryPath);
        m_CurruntCommit.setCurrentCommitSHA1(sha1);
    }


}
