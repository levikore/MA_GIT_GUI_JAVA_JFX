package logicpackage;

import java.nio.file.Path;
import java.nio.file.Paths;

public class RepositoryManager {

    private Folder m_RootFolder;

    public RepositoryManager(String path) {
        Path testPath = Paths.get("c:\\test");
        m_RootFolder = new Folder(testPath,  ".magit");

        Path magitPath = Paths.get("c:\\test\\.magit");
       new Folder(magitPath,"objects");
        new Folder(magitPath,"branches");
    }

}
