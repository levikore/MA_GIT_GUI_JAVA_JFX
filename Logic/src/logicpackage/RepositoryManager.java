package logicpackage;

import java.nio.file.Path;
import java.nio.file.Paths;

public class RepositoryManager {

    private Path m_Repostory;


    public RepositoryManager(Path i_RepositoryPath) {
        m_Repostory = i_RepositoryPath;
        //Path magitPath = Paths.get(m_Repostory.toString() + "\\.magit");

        intializeRepository();
        IFilesManagement.createFolderDescriptionFile(m_Repostory,"yair");
        //IFilesManagement.createZipFile(m_Repostory.toString()+"\\txt1.txt");

    }

    private void intializeRepository() {
        Path magitPath = Paths.get(m_Repostory.toString() + "\\.magit");
        new Folder(m_Repostory.getParent(), m_Repostory.toFile().getName());
        new Folder(m_Repostory, ".magit");
        new Folder(magitPath, "objects");
        new Folder(magitPath, "branches");
    }


}
