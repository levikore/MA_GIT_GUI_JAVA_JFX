package logicpackage;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class CollaborationManager {


    public static void CloneRepository(Path i_RemotePath, Path i_LocalPath) throws IOException {
        RepositoryManager remoteRepositoryManager = new RepositoryManager(i_RemotePath, "Administrator", false, false, null);
        new RepositoryManager(i_LocalPath, "Administrator", true, true, null);
        handleClone(remoteRepositoryManager, i_LocalPath);
    }

    private static void handleClone(RepositoryManager i_RepositoryManager, Path i_LocalPath) throws FileNotFoundException, UnsupportedEncodingException {
        List<Commit> remoteCommitList = i_RepositoryManager.GetSortedAccessibleCommitList();
        HashMap<Integer, List<Integer>> commitMap = getCommitMap(remoteCommitList);
        List<Commit> clonedCommits = new LinkedList<>();

        for (Commit remoteCommit : remoteCommitList) {
            RootFolder clonedRootFolder = cloneRootFolder(remoteCommit.GetCommitRootFolder(), i_LocalPath);
            Commit clonedCommit = new Commit(clonedRootFolder, remoteCommit.GetCommitComment(), remoteCommit.GetCreatedBy(), null, null, remoteCommit.GetCreationDate());
            clonedCommits.add(clonedCommit);
            FilesManagement.CleanWC(i_LocalPath);
        }

        connectClonedCommits(clonedCommits, commitMap);
        createCommitObjects(clonedCommits, i_LocalPath);
    }

    private static void createCommitObjects(List<Commit> i_CommitList, Path i_TargetRootPath){
        Collections.reverse(i_CommitList);
        i_CommitList.forEach(commit -> {
            String sha1 = FilesManagement.CreateCommitDescriptionFile(commit, i_TargetRootPath, true);
            commit.SetCurrentCommitSHA1(sha1);
        });
    }

    private static void connectClonedCommits(List<Commit> io_ClonedCommits, HashMap<Integer, List<Integer>> i_CommitMap){
        for(int i=0; i<io_ClonedCommits.size(); i++){
            List<Integer> prevCommitIndexList = i_CommitMap.get(i);
            List<Commit> prevCommitsList = new LinkedList<>();
            for(Integer index : prevCommitIndexList){
                prevCommitsList.add(io_ClonedCommits.get(index));
            }

            prevCommitsList = prevCommitsList.isEmpty() ? null : prevCommitsList;
            io_ClonedCommits.get(i).SetPrevCommitsList(prevCommitsList);
        }
    }

    private static HashMap<Integer, List<Integer>> getCommitMap(List<Commit> i_SortedCommitList) {
        HashMap<Integer, List<Integer>> commitMap = new HashMap<>();

        for (int i = 0; i < i_SortedCommitList.size(); i++) {
            Commit currentCommit = i_SortedCommitList.get(i);
            List<Commit> prevCommits = currentCommit.GetPrevCommitsList();
            List<Integer> prevCommitIndexList = new LinkedList<>();

            if (prevCommits != null) {
                for (Commit prevCommit : prevCommits) {
                    prevCommitIndexList.add(i_SortedCommitList.indexOf(prevCommit));
                }
            }

            commitMap.put(i, prevCommitIndexList);

        }

        return commitMap;
    }

    private static RootFolder cloneRootFolder(RootFolder i_RootFolder, Path i_TargetPath) throws FileNotFoundException, UnsupportedEncodingException {
        BlobData clonedFolder = cloneFolder(i_RootFolder.GetBloBDataOfRootFolder(), true, i_TargetPath, i_TargetPath);
        return new RootFolder(clonedFolder, i_TargetPath);
    }

    private static BlobData cloneFolder(BlobData i_RemoteFolder, Boolean i_IsRootFolder, Path i_TargetRootPath, Path i_TargetPath) throws FileNotFoundException, UnsupportedEncodingException {
        String folderName = "";
        if (!i_IsRootFolder) {
            folderName = Paths.get(i_RemoteFolder.GetPath()).toFile().getName();
            FilesManagement.CreateFolder(i_TargetPath, folderName);
        }

        Folder folder = new Folder();
        Path folderPath = Paths.get(i_TargetPath + "\\" + folderName);
        BlobData clonedFolder = new BlobData(i_TargetRootPath, folderPath.toString(), i_RemoteFolder.GetLastChangedBY(), i_RemoteFolder.GetLastChangedTime(), true, "", folder);
        List<BlobData> containedItems = i_RemoteFolder.GetCurrentFolder().GetBlobList();
        for(BlobData blob : containedItems){
            if(blob.GetIsFolder()){
                BlobData containedFolder = cloneFolder(blob, false, i_TargetPath, Paths.get(clonedFolder.GetPath()));
                clonedFolder.GetCurrentFolder().AddBlobToList(containedFolder);
            }else{
                BlobData containedElement = cloneSimpleBlob(blob, i_TargetRootPath, Paths.get(clonedFolder.GetPath()));
                clonedFolder.GetCurrentFolder().AddBlobToList(containedElement);
            }
        }

        String sha1 = FilesManagement.CreateFolderDescriptionFile(clonedFolder, i_TargetRootPath, Paths.get(clonedFolder.GetPath()), clonedFolder.GetLastChangedBY(), "", true, null);
        clonedFolder.SetSHA1(sha1);

        return clonedFolder;
    }

    private static BlobData cloneSimpleBlob(BlobData i_Blob, Path i_TargetRootPath, Path i_TargetPath) throws FileNotFoundException, UnsupportedEncodingException {
        String fileName = Paths.get(i_Blob.GetPath()).toFile().getName();
        String lastUpdater = i_Blob.GetLastChangedBY();
        String lastUpdateDate = i_Blob.GetLastChangedTime();
        String content = i_Blob.GetFileContent();
        PrintWriter writer = new PrintWriter(i_TargetPath + "\\" + fileName, "UTF-8");
        writer.println(content);
        writer.close();

        BlobData blob = FilesManagement.CreateSimpleFileDescription(
                i_TargetRootPath,
                Paths.get(i_TargetPath + "\\" + fileName),
                lastUpdater, lastUpdateDate,
                "",
                null,
                true);
        return blob;
    }


//    private static RootFolder cloneRootFolder(RootFolder i_RemoteRootFolder, Path i_RootPath) throws FileNotFoundException, UnsupportedEncodingException {
//        BlobData folderBlobData = i_RemoteRootFolder.GetBloBDataOfRootFolder();
//        BlobData blobDataFolder = buildFolderFromElement(folderElement, i_RootPath, i_RootPath, i_XMLDocument);
//
//        return new RootFolder(blobDataFolder, i_RootPath);
//    }
//
//    private static BlobData buildFolderFromElement(Element i_FolderElement, Path i_RootPath, Path i_Path, Document i_XMLDocument) throws FileNotFoundException, UnsupportedEncodingException {
//        String lastUpdater = i_FolderElement.getElementsByTagName("last-updater").item(0).getTextContent();
//        String lastUpdateDate = i_FolderElement.getElementsByTagName("last-update-date").item(0).getTextContent();
//        String isRootFolder = i_FolderElement.getAttribute("is-root");
//        String folderName = "";
//        if (!isRootFolder.equals("true")) {
//            folderName = i_FolderElement.getElementsByTagName("name").item(0).getTextContent();
//            FilesManagement.CreateFolder(i_Path, folderName);
//        }
//
//        Folder folder = new Folder();
//        Path folderPath = Paths.get(i_Path + "\\" + folderName);
//        BlobData folderData = new BlobData(i_RootPath, folderPath.toString(), lastUpdater, lastUpdateDate, true, "", folder);
//        NodeList containedItems = i_FolderElement.getElementsByTagName("item");
//        for (int i = 0; i < containedItems.getLength(); i++) {
//            Element currentItem = (Element) containedItems.item(i);
//            String itemID = currentItem.getAttribute("id");
//            String itemType = currentItem.getAttribute("type");
//            if (itemType.equals("blob")) {
//                Element blobElement = GetXMLElementByID(i_XMLDocument, s_MagitBlobs, itemID);
//                folderData.GetCurrentFolder().AddBlobToList(buildBlobFromElement(blobElement, i_RootPath, folderPath));
//            } else if (itemType.equals("folder")) {
//                Element folderElement = GetXMLElementByID(i_XMLDocument, s_MagitFolders, itemID);
//                BlobData containedFolder = buildFolderFromElement(folderElement, i_RootPath, folderPath, i_XMLDocument);
//                folderData.GetCurrentFolder().AddBlobToList(containedFolder);
//            }
//        }
//
//        String sha1 = FilesManagement.CreateFolderDescriptionFile(folderData, i_RootPath, folderPath, lastUpdater, "", true, null);
//        folderData.SetSHA1(sha1);
//
//        return folderData;
//    }
//
//    private static BlobData buildBlobFromElement(Element i_BlobElement, Path i_RepositoryPath, Path i_FilePath) throws FileNotFoundException, UnsupportedEncodingException {
//        String fileName = i_BlobElement.getElementsByTagName("name").item(0).getTextContent();
//        String lastUpdater = i_BlobElement.getElementsByTagName("last-updater").item(0).getTextContent();
//        String lastUpdateDate = i_BlobElement.getElementsByTagName("last-update-date").item(0).getTextContent();
//        String content = i_BlobElement.getElementsByTagName("content").item(0).getTextContent();
//        PrintWriter writer = new PrintWriter(i_FilePath + "\\" + fileName, "UTF-8");
//        writer.println(content);
//        writer.close();
//
//        BlobData blob = FilesManagement.CreateSimpleFileDescription(
//                i_RepositoryPath,
//                Paths.get(i_FilePath + "\\" + fileName),
//                lastUpdater, lastUpdateDate,
//                "",
//                null,
//                true);
//        return blob;
//    }


}
