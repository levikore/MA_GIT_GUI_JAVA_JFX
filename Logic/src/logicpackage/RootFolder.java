package logicpackage;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

public class RootFolder {

   private Folder m_RootFolder ;
    private String m_SHA1;

    public RootFolder(Path i_ObjectsFolderPath, Folder i_Folder, Path i_RepositoryPath){
        m_RootFolder = i_Folder;
        //IFilesManagement.createFolderDescriptionFile(Paths.get(i_Folder+"\\"+i_RepositoryPath.getFileName()),"yair");
        IFilesManagement.createFolderDescriptionFile(i_RepositoryPath,"yair");
        m_SHA1 = ((i_ObjectsFolderPath.toFile()).listFiles())[0].getName();
    }

    public String getSHA1() {
        return m_SHA1;
    }
}