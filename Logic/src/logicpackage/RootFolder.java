package logicpackage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

public class RootFolder {

    private BlobData m_RootFolder;
    private String m_SHA1;
    private Path m_RootFolderPath;

    public RootFolder(BlobData i_Folder, Path i_RootFolderPath) {
        m_RootFolder = i_Folder;
        m_RootFolderPath = i_RootFolderPath;
    }

//    public RootFolder(String i_Sha1, Folder i_Folder, Path i_RepositoryPath){
//        m_RootFolder = i_Folder;
//
//    }

    public void UpdateCurrentRootFolderSha1() {
        updateRootTreeSBA1Recursively(m_RootFolder,m_RootFolderPath, m_RootFolderPath.toFile().getName());

        //
//        IFilesManagement.createFolderDescriptionFile(m_RootFolderPath,"yair");
//        m_SHA1 =new String();
    }

    private void updateRootTreeSBA1Recursively(BlobData i_BlobDataOfCurrentFolder, Path i_RootFolderPath, String userName) {
        try {

            Files.list(i_RootFolderPath).filter(name -> (!name.equals(Paths.get(i_RootFolderPath.toString() + "\\.magit")))).forEach((file) -> {
                        System.out.println(i_RootFolderPath.toString());

                        if (file.toFile().isFile()) {
                            String fileSha1 = IFilesManagement.createSimpleFileDescription(m_RootFolderPath, file.toAbsolutePath());
                            BlobData simpleBlob = new BlobData(file.toFile().getName(), fileSha1, false);
                            i_BlobDataOfCurrentFolder.getCurrentFolder().addBlobToList(simpleBlob);
                        } else {
                            Folder folder=new Folder(i_RootFolderPath,file.toFile().getName());
                            BlobData blob = new BlobData(file.toFile().getName(), folder);
                            i_BlobDataOfCurrentFolder.getCurrentFolder().addBlobToList(blob);
                            updateRootTreeSBA1Recursively(blob, file.toAbsolutePath(), userName);
                        }
                    }
            );
            if (i_RootFolderPath.toFile().isDirectory()) {
                String sha1=IFilesManagement.createFolderDescriptionFile(
                        i_BlobDataOfCurrentFolder,
                        m_RootFolderPath,
                        Paths.get(i_RootFolderPath.toAbsolutePath().toString()),
                        userName);
                i_BlobDataOfCurrentFolder.setSHA1(sha1);
                i_BlobDataOfCurrentFolder.getCurrentFolder().setFolderSha1(sha1);
            }
        } catch (IOException ex) {
            System.err.println("(func)UpdateCurrentFile: I/O error: " + ex);
        }
    }

    public String getSHA1() {
        return m_SHA1;
    }


}