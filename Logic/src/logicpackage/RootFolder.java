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

    public void UpdateCurrentRootFolderSha1(String userName, String i_TestFolderName) throws IOException {
        List<File> emptyFilesList = new LinkedList<>();
        updateRootTreeSHA1Recursively(m_RootFolder, m_RootFolderPath, userName, emptyFilesList, i_TestFolderName);
    }

    private void enterRootTreeBranchAndUpdate(BlobData i_BlobDataOfCurrentFolder, Path i_RootFolderPath, String i_UserName, List<File> emptyFilesList, String i_TestFolderName) throws IOException {
        for (File file : i_RootFolderPath.toFile().listFiles()) {
            if (!file.getAbsolutePath().equals(m_RootFolderPath + "\\.magit")) {
                if (!file.isDirectory() && !FilesManagement.IsFileEmpty(file)) {
                    BlobData simpleBlob = FilesManagement.CreateSimpleFileDescription(m_RootFolderPath, Paths.get(file.getAbsolutePath()), i_UserName, null, i_TestFolderName);
                    i_BlobDataOfCurrentFolder.getCurrentFolder().addBlobToList(simpleBlob);
                } else if (file.isDirectory() && !FilesManagement.IsDirectoryEmpty(file)) {
                    Folder folder = new Folder();
                    BlobData blob = new BlobData(m_RootFolderPath, file.toString(), folder, i_UserName);
                    i_BlobDataOfCurrentFolder.getCurrentFolder().addBlobToList(blob);
                    updateRootTreeSHA1Recursively(blob, Paths.get(file.getAbsolutePath()), i_UserName, emptyFilesList, i_TestFolderName);
                } else {
                    emptyFilesList.add(file);
                }
            }
        }

        deleteEmptyFiles(emptyFilesList);
    }

    private void exitRootTreeBranchAndUpdate(BlobData i_BlobDataOfCurrentFolder, Path i_RootFolderPath, String i_UserName, List<File> emptyFilesList, String i_TestFolderName) {
        if (i_RootFolderPath.toFile().isDirectory() && !FilesManagement.IsDirectoryEmpty(i_RootFolderPath.toFile())) {
            String sha1 = FilesManagement.CreateFolderDescriptionFile(
                    i_BlobDataOfCurrentFolder,
                    m_RootFolderPath,
                    Paths.get(i_RootFolderPath.toAbsolutePath().toString()),
                    i_UserName,
                    i_TestFolderName,
                    false);
            i_BlobDataOfCurrentFolder.setSHA1(sha1);
            i_BlobDataOfCurrentFolder.getCurrentFolder().setFolderSha1(sha1);
            if (i_BlobDataOfCurrentFolder.getLastChangedTime() == null) {
                i_BlobDataOfCurrentFolder.setLastChangedTime(FilesManagement.ConvertLongToSimpleDateTime(i_RootFolderPath.toFile().lastModified()));
            }
        } else if (i_RootFolderPath.toFile().isDirectory()) {
            i_RootFolderPath.toFile().delete();
        }

        deleteEmptyFiles(emptyFilesList);
    }

    private void updateRootTreeSHA1Recursively(BlobData i_BlobDataOfCurrentFolder, Path i_RootFolderPath, String i_UserName, List<File> emptyFilesList, String i_TestFolderName) throws IOException {
        enterRootTreeBranchAndUpdate(i_BlobDataOfCurrentFolder, i_RootFolderPath, i_UserName, emptyFilesList, i_TestFolderName);
        exitRootTreeBranchAndUpdate(i_BlobDataOfCurrentFolder, i_RootFolderPath, i_UserName, emptyFilesList, i_TestFolderName);
    }

    public String getSHA1() {
        return m_RootFolder.getSHA1();
    }

    private void deleteEmptyFiles(List<File> emptyFilesList) {
        emptyFilesList.forEach(file -> {
            file.delete();
        });
        emptyFilesList = new LinkedList<>();
    }

    public Path getRootFolderPath() {
        return m_RootFolderPath;
    }

    public void RecoverWCFromCurrentRootFolderObj() {
        m_RootFolder.RecoverWCFromCurrentBlobData();
    }

    public List<String> GetAllFilesData() {
        List<String> list = new LinkedList<>();
        m_RootFolder.AddBlobDataToList(list);
        return list;
    }

    public BlobData getRootFolder() {
        return m_RootFolder;
    }
}