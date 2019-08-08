package logicpackage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;

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

    public void UpdateCurrentRootFolderSha1(String userName) {
        List<File> emptyFilesList = new LinkedList<>();
        updateRootTreeSHA1Recursively(m_RootFolder, m_RootFolderPath, userName, emptyFilesList);
        //
//        IFilesManagement.createFolderDescriptionFile(m_RootFolderPath,"yair");
//        m_SHA1 =new String();
    }

    private void updateRootTreeSHA1Recursively(BlobData i_BlobDataOfCurrentFolder, Path i_RootFolderPath, String i_UserName, List<File> emptyFilesList) {
        try {
            Files.list(i_RootFolderPath).filter(name -> (!name.equals(Paths.get(i_RootFolderPath.toString() + "\\.magit")))).forEach((file) -> {
                System.out.println(i_RootFolderPath.toString());

                if (!file.toFile().isDirectory()&&!IFilesManagement.isFileEmpty(file.toFile())) {
                    BlobData simpleBlob = IFilesManagement.createSimpleFileDescription(m_RootFolderPath, file.toAbsolutePath(), i_UserName);
                    i_BlobDataOfCurrentFolder.getCurrentFolder().addBlobToList(simpleBlob);
                }

                else if(file.toFile().isDirectory()&&!IFilesManagement.isDirectoryEmpty(file.toFile())) {
                    Folder folder = new Folder(i_RootFolderPath, file.toFile().getName());
                    BlobData blob = new BlobData(file.toString(), folder);
                    i_BlobDataOfCurrentFolder.getCurrentFolder().addBlobToList(blob);
                    updateRootTreeSHA1Recursively(blob, file.toAbsolutePath(), i_UserName, emptyFilesList);
            }else{
                    emptyFilesList.add(file.toFile());
                }

            });
            deleteEmptyFiles(emptyFilesList);

            if (i_RootFolderPath.toFile().isDirectory() && !IFilesManagement.isDirectoryEmpty(i_RootFolderPath.toFile())) {
                String sha1 = IFilesManagement.createFolderDescriptionFile(
                        i_BlobDataOfCurrentFolder,
                        m_RootFolderPath,
                        Paths.get(i_RootFolderPath.toAbsolutePath().toString()),
                        i_UserName);
                i_BlobDataOfCurrentFolder.setSHA1(sha1);
                i_BlobDataOfCurrentFolder.getCurrentFolder().setFolderSha1(sha1);
                i_BlobDataOfCurrentFolder.setLastChangedTime(IFilesManagement.convertLongToSimpleDateTime(i_RootFolderPath.toFile().lastModified()));
            }
//            else if(i_RootFolderPath.toFile().isDirectory()){
//                i_RootFolderPath.toFile().delete();
//            }

//            deleteEmptyFiles(emptyFilesList);
        } catch (IOException ex) {
            System.err.println("(func)UpdateCurrentFile: I/O error: " + ex);
        }
    }

    public String getSHA1() {
        return m_SHA1;
    }

    private void deleteEmptyFiles(List<File> emptyFilesList){
        System.out.println(emptyFilesList.toString());

        emptyFilesList.forEach(file -> {
            System.out.println(String.format("Empty file deleted %s\n", file.getAbsolutePath()));
            file.delete();
        });

        emptyFilesList = new LinkedList<>();
    }


}