package logicpackage;

import java.nio.file.Path;
import java.nio.file.Paths;

public class RepositoryManager {

    private Folder m_RootFolder;
    private Path m_Repostory;


    public RepositoryManager() {
        Path testPath = Paths.get("c:\\test");
        m_RootFolder = new Folder(testPath, "repository");
        m_Repostory = Paths.get("c:\\test\\repository");
        Path WCPath = Paths.get(m_Repostory.toString() + "\\WC");
        Path magitPath = Paths.get(m_Repostory.toString() + "\\.magit");
        Path objectsPath= Paths.get(magitPath.toString()+"\\objects");

        intializeRepository();
        IFilesManagement.createZipFile(WCPath.toString()+"\\txt1.txt");
        IFilesManagement.createFolderDescriptionFile(WCPath.toString());
    }

    private void intializeRepository() {
        Path magitPath = Paths.get(m_Repostory.toString() + "\\.magit");
        new Folder(m_Repostory, "WC");
        new Folder(m_Repostory, ".magit");
        new Folder(magitPath, "objects");
        new Folder(magitPath, "branches");
    }


}
