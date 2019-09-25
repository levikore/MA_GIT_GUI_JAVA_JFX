package logicpackage;

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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class CollaborationManager {


    public static void CloneRepository(Path i_RemotePath, Path i_LocalPath) {
        HashMap<String, Commit> commitHashMap = cloneCommits(i_RemotePath, i_LocalPath);
        //buildBranchesFromXMLDocument(xmlDocument, i_RepositoryPath, commitHashMap);
        //FilesManagement.CreateRemoteReferenceFile(getRemoteReferencePath(xmlDocument), i_RepositoryPath);
    }

    private static HashMap<String, Commit> cloneCommits(Path i_RemotePath, Path i_LocalPath){
        HashMap<String, Commit> commitHashMap = new HashMap<>();

        for (File file : Objects.requireNonNull(Paths.get(i_RemotePath + "\\.magit\\objects").toFile().listFiles())) {
            if (isCommit(file, i_RemotePath.toFile().getName())) {

            }
        }


//        for (int i = 0; i < commitsNodeList.getLength(); i++) {
//            Element commitElement = (Element) commitsNodeList.item(i);
//            String id = commitElement.getAttribute("id");
//            String message = commitElement.getElementsByTagName("message").item(0).getTextContent();
//            String author = commitElement.getElementsByTagName("author").item(0).getTextContent();
//            String dateOfCreation = commitElement.getElementsByTagName("date-of-creation").item(0).getTextContent();
//            Element rootFolderElement = (Element) commitElement.getElementsByTagName("root-folder").item(0);
//            RootFolder rootFolder = buildRootFolderFromElement(rootFolderElement, i_XMLDocument, i_LocalPath);
//
//            Commit commit = new Commit(rootFolder, message, author, null, null, dateOfCreation);
//
//            commitHashMap.put(id, commit);
//            FilesManagement.CleanWC(i_LocalPath);
//        }
//
//        HashMap<String, List<String>> commitIDChainMap = setCommitChains(commitHashMap, commitsNodeList);
//        createCommitObjects(commitHashMap, commitIDChainMap, i_LocalPath);

        return commitHashMap;
    }

    private static boolean isCommit(File i_File, String i_RemoteRepositoryName) {
        boolean isCommit = false;
        List<String> lines = FilesManagement.ReadZipIntoString(i_File.getAbsolutePath());
        String firstRow = lines.get(0);
        File file = Paths.get(i_File.getParent() + "\\" + firstRow + ".zip").toFile();
        if (file.exists()) {
            if (FilenameUtils.removeExtension(file.getName()).equals(FilenameUtils.removeExtension(i_RemoteRepositoryName))) {
                isCommit = true;
            }
        }

        return isCommit;
    }

//    private static Path getRemoteReferencePath(Document i_XmlDocument) {
//        String locationString = null;
//        NodeList remoteReferenceList = i_XmlDocument.getElementsByTagName("MagitRemoteReference");
//        if (remoteReferenceList != null && remoteReferenceList.getLength() != 0) {
//            Element currentRemoteReference = (Element) remoteReferenceList.item(0);
//            Node locationNode = currentRemoteReference.getElementsByTagName("location").item(0);
//            locationString = locationNode == null ? null : locationNode.getTextContent();
//        }
//
//        return locationString == null || locationString.isEmpty() ? null : Paths.get(locationString);
//    }


//    private static void buildBranchesFromXMLDocument(Document i_XMLDocument, Path i_RootPath, HashMap<String, Commit> i_CommitHashMap) {
//        NodeList branchesNodeList = i_XMLDocument.getElementsByTagName(s_MagitBranches);
//        String headBranchName = i_XMLDocument.getElementsByTagName("head").item(0).getTextContent();
//        for (int i = 0; i < branchesNodeList.getLength(); i++) {
//            Element branchElement = (Element) branchesNodeList.item(i);
//            Boolean isRemote = getIsRemote(branchElement);
//            String trackingAfter = getTrackingAfter(branchElement);
//            String currentBranchName = branchElement.getElementsByTagName("name").item(0).getTextContent();
//            Element pointedCommit = (Element) branchElement.getElementsByTagName("pointed-commit").item(0);
//            String pointedCommitID = pointedCommit.getAttribute("id");
//            Commit commit = i_CommitHashMap.get(pointedCommitID);
//            Branch branch = new Branch(currentBranchName, commit, i_RootPath, true, null, isRemote, trackingAfter);
//
//            if (currentBranchName.equals(headBranchName)) {
//                HeadBranch headBranch = new HeadBranch(branch, i_RootPath, true, null);
//            }
//        }
//    }
//
//    private static Boolean getIsRemote(Element i_BranchElement) {
//        String isRemoteString = i_BranchElement.getAttribute("is-remote");
//        Boolean isRemote = isRemoteString.isEmpty() || isRemoteString == null ? false : isRemoteString.equals("true");
//        return isRemote;
//    }
//
//    private static String getTrackingAfter(Element i_BranchElement) {
//        String trackingAfter = null;
//        String isBranchTracking = i_BranchElement.getAttribute("tracking");
//        if (!isBranchTracking.isEmpty() && isBranchTracking != null) {
//            if (isBranchTracking.equals("true")) {
//                trackingAfter = i_BranchElement.getElementsByTagName("tracking-after") != null ? i_BranchElement.getElementsByTagName("tracking-after").item(0).getTextContent() : "";
//            }
//        }
//
//        return trackingAfter;
//    }
//
//
//

//
//    private static HashMap<String, List<String>> setCommitChains(HashMap<String, Commit> i_CommitHashMap, NodeList i_CommitsNodeList) {
//        HashMap<String, List<String>> commitChainMap = new HashMap<>();
//        for (int i = 0; i < i_CommitsNodeList.getLength(); i++) {
//            Element currentCommitElement = (Element) i_CommitsNodeList.item(i);
//            String currentCommitID = currentCommitElement.getAttribute("id");
//            List<String> prevCommitIdsList = getListOfPreviousCommitIDs(currentCommitElement);
//            if (!prevCommitIdsList.isEmpty()) {
//                List<Commit> prevCommitsList = new LinkedList<>();
//                prevCommitIdsList.forEach(commitId -> prevCommitsList.add(i_CommitHashMap.get(commitId)));
//                i_CommitHashMap.get(currentCommitID).SetPrevCommitsList(prevCommitsList); //set prev commit
//            } else {
//                prevCommitIdsList = null;
//            }
//
//            commitChainMap.put(currentCommitID, prevCommitIdsList);
//        }
//
//        return commitChainMap;
//    }
//
//    private static List<String> getListOfPreviousCommitIDs(Element i_CommitElement) {
//        List<String> listOfPrevCommitIDs = new LinkedList<>();
//        NodeList precedingCommitsNodeList = i_CommitElement.getElementsByTagName("preceding-commit");
//
//        for (int i = 0; i < precedingCommitsNodeList.getLength(); i++) {
//            Element prevCommitElement = (Element) precedingCommitsNodeList.item(i);
//
//            if (prevCommitElement != null) {
//                String prevCommitID = prevCommitElement.getAttribute("id");
//                listOfPrevCommitIDs.add(prevCommitID);
//            }
//        }
//
//        return listOfPrevCommitIDs;
//    }
//
//    private static void createCommitObjects(HashMap<String, Commit> i_CommitHashMap, HashMap<String, List<String>> i_CommitIDChainMap, Path i_RootPath) {
//        for (String id : i_CommitHashMap.keySet()) {// set head commit
//            if (i_CommitHashMap.get(id).GetPrevCommitsList() == null) {
//                String sha1 = FilesManagement.CreateCommitDescriptionFile(i_CommitHashMap.get(id), i_RootPath, true);
//                i_CommitHashMap.get(id).SetCurrentCommitSHA1(sha1);
//                break;
//            }
//        }
//
//        for (String id : i_CommitHashMap.keySet()) {
//            createCommitObjectsUntilHead(i_CommitHashMap, i_CommitIDChainMap, id, i_RootPath);
//        }
//
//    }
//
//    private static void createCommitObjectsUntilHead(HashMap<String, Commit> i_CommitHashMap, HashMap<String, List<String>> i_CommitIDChainMap, String i_CurrentID, Path i_RootPath) {
//        if (i_CommitHashMap.get(i_CurrentID).GetCurrentCommitSHA1() == null) {
//            List<String> previousCommitIDsList = i_CommitIDChainMap.get(i_CurrentID);
//            previousCommitIDsList.forEach(previousID -> createCommitObjectsUntilHead(i_CommitHashMap, i_CommitIDChainMap, previousID, i_RootPath));
//        }
//
//        String sha1 = FilesManagement.CreateCommitDescriptionFile(i_CommitHashMap.get(i_CurrentID), i_RootPath, true);
//        i_CommitHashMap.get(i_CurrentID).SetCurrentCommitSHA1(sha1);
//    }
//
//    private static RootFolder buildRootFolderFromElement(Element i_RootFolderElement, Document i_XMLDocument, Path i_RootPath) throws FileNotFoundException, UnsupportedEncodingException {
//        String containedRootFolderID = i_RootFolderElement.getAttribute("id");
//        Element folderElement = GetXMLElementByID(i_XMLDocument, s_MagitFolders, containedRootFolderID);
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
//
//
//    public boolean HandleClone() {
//        boolean retVal = true;
//        String remoteMagitFolderPath = m_RemoteReference + "\\" + c_GitFolderName;
//        Path remoteBranchesFolderPath = Paths.get(remoteMagitFolderPath + "\\" + c_BranchesFolderName);
//        String remoteRepositoryName = m_RemoteReference.toFile().getName();
//        String localBranchesFolderPath = m_MagitPath.toString() + "\\" + c_BranchesFolderName;
//        Path remoteBranchesFolderInLocalRepository = Paths.get(localBranchesFolderPath + "\\" + remoteRepositoryName);
//
//        FilesManagement.CreateFolder(Paths.get(localBranchesFolderPath), remoteRepositoryName);
//        FilesManagement.CopyAllTXTFiles(remoteBranchesFolderPath, remoteBranchesFolderInLocalRepository);
//        FilesManagement.RemoveFileByPath(Paths.get(localBranchesFolderPath + "\\" + remoteRepositoryName + "\\HEAD.txt"));
//        FilesManagement.RemoveFileByPath(Paths.get(localBranchesFolderPath + "\\HEAD.txt"));
//        FilesManagement.RemoveFileByPath(Paths.get(localBranchesFolderPath + "\\master.txt"));
//        FilesManagement.CopyTXTFile(Paths.get(remoteBranchesFolderPath + "\\HEAD.txt"), Paths.get(localBranchesFolderPath));
//
//        String headBranchSha1 = FilesManagement.ReadTextFileContent(remoteBranchesFolderPath + "\\HEAD.txt");
//        FilesManagement.ExtractZipFileToPath(Paths.get(remoteMagitFolderPath + "\\" + c_ObjectsFolderName + "\\" + headBranchSha1 + ".zip"), Paths.get(localBranchesFolderPath));
//        String headBranchName = FilesManagement.GetFileNameInZipFromObjects(headBranchSha1, m_RemoteReference.toString());
//        FilesManagement.HandleTrackingFileOfTrackingBranch(headBranchName, remoteRepositoryName + "\\" + headBranchName, Paths.get(m_MagitPath.toString() + "\\tracking"));
//
//        try {
//            FilesManagement.CreateRemoteReferenceFile(m_RemoteReference, m_RepositoryPath);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        copyObjectsFolderFromRemoteRepository();
//
//        // try {
//        // recoverRepositoryFromFiles();
//        //  } catch (IOException e) {
//        // e.printStackTrace();
//        // }
//
//
//        return retVal;
//    }
//
//    private void copyObjectsFolderFromRemoteRepository() {
//        String remoteObjectsFolderPath = m_RemoteReference + "\\" + c_GitFolderName + "\\" + c_ObjectsFolderName;
//        String localObjectsFolderPath = m_MagitPath + "\\" + c_ObjectsFolderName;
//        try {
//            FileUtils.deleteDirectory(Paths.get(localObjectsFolderPath).toFile());
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        FilesManagement.CreateFolder(m_MagitPath, c_ObjectsFolderName);
//        FilesManagement.CopyAllTXTFiles(Paths.get(remoteObjectsFolderPath), Paths.get(localObjectsFolderPath));
//    }


}
