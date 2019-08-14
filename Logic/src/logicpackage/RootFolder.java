package logicpackage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;

public class RootFolder {

    private BlobData m_RootFolder;
    private Path m_RootFolderPath;

    public RootFolder(BlobData i_Folder, Path i_RootFolderPath) {
        m_RootFolder = i_Folder;
        m_RootFolderPath = i_RootFolderPath;
    }

    public void UpdateCurrentRootFolderSha1(String userName, String i_TestFolderName) {
        List<File> emptyFilesList = new LinkedList<>();
        updateRootTreeSHA1Recursively(m_RootFolder, m_RootFolderPath, userName, emptyFilesList, i_TestFolderName);
    }

    private void enterRootTreeBranchAndUpdate(BlobData i_BlobDataOfCurrentFolder, Path i_RootFolderPath, String i_UserName, List<File> emptyFilesList, String i_TestFolderName) throws IOException {

          //  Files.list(i_RootFolderPath).filter(name -> (!name.equals(Paths.get(i_RootFolderPath.toString() + "\\.magit")))).forEach((file) -> {

                for(File file:i_RootFolderPath.toFile().listFiles())
                {
                    System.out.println(i_RootFolderPath.toString());
                    String path=(i_RootFolderPath.toString()+"\\.magit");
                if(!file.getAbsolutePath().equals(path))
                {
                if (!file.isDirectory() && !FilesManagement.IsFileEmpty(file)) {
                    BlobData simpleBlob = FilesManagement.CreateSimpleFileDescription(m_RootFolderPath, Paths.get(file.getAbsolutePath()), i_UserName, i_TestFolderName);
                    i_BlobDataOfCurrentFolder.getCurrentFolder().addBlobToList(simpleBlob);
                }

                else if (file.isDirectory() && !FilesManagement.IsDirectoryEmpty(file)) {
                    Folder folder = new Folder();//new Folder(i_RootFolderPath, fileName.toFile().getName());
                    BlobData blob = new BlobData(file.toString(), folder);
                    i_BlobDataOfCurrentFolder.getCurrentFolder().addBlobToList(blob);
                    updateRootTreeSHA1Recursively(blob, Paths.get(file.getAbsolutePath()), i_UserName, emptyFilesList, i_TestFolderName);
                } else {
                    emptyFilesList.add(file);
                }}}


       //     });
            deleteEmptyFiles(emptyFilesList);
    }

    private void exitRootTreeBranchAndUpdate(BlobData i_BlobDataOfCurrentFolder, Path i_RootFolderPath, String i_UserName, List<File> emptyFilesList, String i_TestFolderName) {
        if (i_RootFolderPath.toFile().isDirectory() && !FilesManagement.IsDirectoryEmpty(i_RootFolderPath.toFile())) {
            String sha1 = FilesManagement.CreateFolderDescriptionFile(
                    i_BlobDataOfCurrentFolder,
                    m_RootFolderPath,
                    Paths.get(i_RootFolderPath.toAbsolutePath().toString()),
                    i_UserName,
                    i_TestFolderName);
            System.out.println("in exitRootTreeBranchAndUpdate: sha1="+sha1);
            i_BlobDataOfCurrentFolder.setSHA1(sha1);
            i_BlobDataOfCurrentFolder.getCurrentFolder().setFolderSha1(sha1);
            i_BlobDataOfCurrentFolder.setLastChangedTime(FilesManagement.ConvertLongToSimpleDateTime(i_RootFolderPath.toFile().lastModified()));
        }
//            else if(i_RootFolderPath.toFile().isDirectory()){
//                i_RootFolderPath.toFile().delete();
//            }

//            deleteEmptyFiles(emptyFilesList);
    }

    private void updateRootTreeSHA1Recursively(BlobData i_BlobDataOfCurrentFolder, Path i_RootFolderPath, String i_UserName, List<File> emptyFilesList, String i_TestFolderName) {
        try{
            enterRootTreeBranchAndUpdate(i_BlobDataOfCurrentFolder, i_RootFolderPath, i_UserName, emptyFilesList, i_TestFolderName);
            exitRootTreeBranchAndUpdate(i_BlobDataOfCurrentFolder, i_RootFolderPath, i_UserName, emptyFilesList, i_TestFolderName);
        } catch (IOException ex) {
            System.err.println("(func)UpdateCurrentFile: I/O error: " + ex);
        }
    }

    public String getSHA1() {
        return m_RootFolder.getSHA1();
    }

    private void deleteEmptyFiles(List<File> emptyFilesList){
//        System.out.println(emptyFilesList.toString());
//
        emptyFilesList.forEach(file -> {
            System.out.println(String.format("Empty file deleted %s\n", file.getAbsolutePath()));
            file.delete();
        });

        emptyFilesList = new LinkedList<>();
    }

    public Path getRootFolderPath() {
        return m_RootFolderPath;
    }

}