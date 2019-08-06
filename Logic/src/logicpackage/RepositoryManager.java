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
        m_RepositoryName= m_RepostoryPath.toFile().getName();
        m_CurrentUserName=i_CurrentUserName;
        //Path magitPath = Paths.get(m_Repostory.toString() + "\\.magit");

        intializeRepository();

        //IFilesManagement.createFolderDescriptionFile(m_Repostory,m_CurrentUserName);
        //IFilesManagement.createZipFile(m_Repostory.toString()+"\\txt1.txt");

    }

    private void intializeRepository() {

        Path magitPath = Paths.get(m_RepostoryPath.toString() + "\\.magit");
        Path objectsPath = Paths.get(magitPath.toString() + "\\objects");
        Folder rootFolder = new Folder(m_RepostoryPath.getParent(), m_RepostoryPath.toFile().getName());
        BlobData rootFolderBlobData = new BlobData(m_RepostoryPath.toFile().getName(),rootFolder);
        new Folder(m_RepostoryPath, ".magit");
        new Folder(magitPath, "objects");
        new Folder(magitPath, "branches");

        //String rootFolderSha1=((objectsPath.toFile()).listFiles())[0].getName();
        m_RootFolder = new RootFolder(rootFolderBlobData, m_RepostoryPath);
    }

    public void CreateNewCommit() {
        // public Commit(RootFolder i_RootFolder Commit i_PrevCommit)
        if (isFirstCommit) {
            m_CurruntCommit = new Commit(m_RootFolder);
            isFirstCommit = false;
        } else {
            m_CurruntCommit = new Commit(m_RootFolder, m_CurruntCommit.getPrevCommit());
        }
    }


}
