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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class CollaborationManager {


    public static void CloneRepository(Path i_RemotePath, Path i_LocalPath) throws IOException {
        RepositoryManager remoteRepositoryManager = new RepositoryManager(i_RemotePath, "Administrator", false, false, null);
        new RepositoryManager(i_LocalPath, "Administrator", true, true, null);
        handleClone(remoteRepositoryManager, i_LocalPath);
    }

    private static void handleClone(RepositoryManager i_RepositoryManager, Path i_LocalPath){
        List<Branch> remoteBranches = i_RepositoryManager.GetAllBranchesList();

        for(Branch remoteBranch : remoteBranches){
            List<Commit> remoteCommitList = i_RepositoryManager.GetAccessibleCommitsFromBranch(remoteBranch.GetCurrentCommit());
            for(Commit remoteCommit : remoteCommitList){
               // remoteCommit.GetCommitRootFolder();//<-------------cloneRootFolder
            }

        }
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
