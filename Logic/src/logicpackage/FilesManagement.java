package logicpackage;

import com.sun.xml.internal.stream.writers.UTF8OutputStreamWriter;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;


import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;


import org.apache.commons.io.FilenameUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import static java.nio.file.Files.delete;


public class FilesManagement {
    public final static String s_ObjectsFolderDirectoryString = "\\.magit\\objects\\";
    public final static String s_BranchesFolderDirectoryString = "\\.magit\\branches\\";
    public final static String s_GitDirectory = "\\.magit\\";
    public final static String s_XmlBuildFolderName = "XML Build";


    public static boolean IsRepositoryExistInPath(String path) {
        return Paths.get(path + "\\.magit").toFile().exists();
    }

    public static void RemoveFileContent(Path i_PathToRemoveContent) {
        try (PrintWriter writer = new PrintWriter(i_PathToRemoveContent.toFile())) {
            writer.print("");
        } catch (FileNotFoundException e) {

        }
    }

    public static void CleanWC(Path i_PathToClean) {
        File[] listFiles = i_PathToClean.toFile().listFiles(pathname -> (!pathname.getAbsolutePath().contains(".magit")));
        for (File file : listFiles) {
            if (file.isDirectory()) {
                try {
                    FileUtils.cleanDirectory(file);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            file.delete();
        }
    }

    public static void removeFileByPath(Path i_PathToRemove) {
        i_PathToRemove.toFile().delete();
    }

    public static void CreateFolder(Path path, String name) {
        Path newDirectoryPath = Paths.get(path.toString() + "/" + name);

        File directory = new File(newDirectoryPath.toString());
        if (!directory.exists()) {
            directory.mkdir();
        }
    }

    public static String UpdateBranchFile(Branch i_Branch, Commit i_Commit, Path i_RepositoryPath) {
        removeFileByPath(Paths.get(i_RepositoryPath.toString() + s_BranchesFolderDirectoryString + i_Branch.getBranchName() + ".txt"));
        removeFileByPath(Paths.get(i_RepositoryPath.toString() + s_ObjectsFolderDirectoryString + i_Branch.getBranchSha1() + ".zip"));
        return CreateBranchFile(i_Branch.getBranchName(), i_Commit, i_RepositoryPath);
    }

    public static String UpdateHeadFile(Branch i_Branch, Path i_RepositoryPath) {
        removeFileByPath(Paths.get(i_RepositoryPath.toString() + s_BranchesFolderDirectoryString + "HEAD.txt"));
        removeFileByPath(Paths.get(i_RepositoryPath.toString() + s_ObjectsFolderDirectoryString + "HEAD.txt"));
        return CreateHeadFile(i_Branch, i_RepositoryPath);
    }


    public static String CreateBranchFile(String i_BranchName, Commit i_Commit, Path i_RepositoryPath) {
        FileWriter outputFile = null;
        Path branchPath = Paths.get(i_RepositoryPath.toString() + s_BranchesFolderDirectoryString + i_BranchName + ".txt");
        BufferedWriter bf = null;
        String sha1 = "";
        try {
            outputFile = new FileWriter(branchPath.toString());
            bf = new BufferedWriter(outputFile);
            bf.write(i_Commit.getCurrentCommitSHA1());
            sha1 = DigestUtils.sha1Hex(i_Commit.getCurrentCommitSHA1() + i_BranchName);
        } catch (IOException ex) {

        } finally {
            try {
                bf.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        //s_BranchesFolderDirectoryString
        createZipFileIntoObjectsFolder(i_RepositoryPath, branchPath, sha1, "");
        return sha1;
    }

    public static String CreateHeadFile(Branch i_HeadBranch, Path i_RepositoryPath) {
        FileWriter outputFile = null;
        BufferedWriter bf = null;
        Path headPath = Paths.get(i_RepositoryPath.toString() + s_BranchesFolderDirectoryString + "HEAD.txt");
        String sha1 = "";
        try {
            outputFile = new FileWriter(headPath.toString());
            bf = new BufferedWriter(outputFile);
            bf.write(i_HeadBranch.getBranchSha1());
            sha1 = DigestUtils.sha1Hex(i_HeadBranch.getBranchSha1());
        } catch (IOException ex) {

        } finally {
            try {
                bf.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        //s_BranchesFolderDirectoryString
        createZipFileIntoObjectsFolder(i_RepositoryPath, headPath, sha1, "");
        return sha1;
    }

    public static String ConvertLongToSimpleDateTime(long i_Time) {
        Date date = new Date(i_Time);
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.mm.yyyy-hh:mm:ss:sss");
        String dateText = dateFormat.format(date);

        return dateText;
    }

    //input: c:\\..\\[repositoryName]\\[nameFile.txt]
    public static BlobData CreateSimpleFileDescription(Path repositoryPath, Path filePathOrigin, String i_UserName, String i_DateCreated, String i_TestFolderName) {
        return createTemporaryFileDescription(repositoryPath, filePathOrigin, i_UserName, i_DateCreated, i_TestFolderName);
    }

    private static BlobData createTemporaryFileDescription(Path repositoryPath, Path i_FilePath, String i_UserName, String i_DateCreated, String i_TestFolderName) {
        BufferedWriter bf = null;
        File file = i_FilePath.toFile();
        String fileDescriptionFilePathString = repositoryPath.toString() + s_ObjectsFolderDirectoryString + file.getName(); //+ ".txt";
        String descriptionStringForGenerateSha1 = "";
        String description = "";
        FileWriter outputFile = null;
        String sha1 = "";
        String type = file.isFile() ? "file" : "folder";
        BlobData simpleBlob = null;

        try {
            outputFile = new FileWriter(fileDescriptionFilePathString);
            bf = new BufferedWriter(outputFile);
            description = readLineByLine(i_FilePath.toString());
            descriptionStringForGenerateSha1 = String.format("%s,%s,%s", file.getAbsolutePath(), type, description);
            bf.write(String.format("%s\n", description));
            sha1 = DigestUtils.sha1Hex(descriptionStringForGenerateSha1);
            simpleBlob = new BlobData(repositoryPath, file.getAbsolutePath(), i_UserName, getUpdateDate(i_DateCreated, file), false, sha1, null);
        } catch (IOException e) {

        } finally {
            try {
                bf.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        createZipFileIntoObjectsFolder(repositoryPath, Paths.get(fileDescriptionFilePathString), sha1, i_TestFolderName);
        Paths.get(fileDescriptionFilePathString).toFile().delete();
        return simpleBlob;
    }

    private static String getUpdateDate(String i_Date, File i_File) {
        String result = i_Date;
        if (i_Date == null || i_Date == "") {
            result = ConvertLongToSimpleDateTime(i_File.lastModified());
        }

        return result;
    }


    public static void ExtractZipFileToPath(Path i_ZipFilePath, Path i_DestinitionPath) {
        //try catch finally!!!!!!!!!!!
        ZipInputStream zis = null;
        String fileZip = i_ZipFilePath.toString();
        File destDir = new File(i_DestinitionPath.toString());
        byte[] buffer = new byte[1024];
        try {
            zis = new ZipInputStream(new FileInputStream(fileZip));
            ZipEntry zipEntry = zis.getNextEntry();
            while (zipEntry != null) {
                File newFile = newFile(destDir, zipEntry);
                FileOutputStream fos = new FileOutputStream(newFile);
                int len;
                while ((len = zis.read(buffer)) > 0) {
                    fos.write(buffer, 0, len);
                }
                fos.close();
                zipEntry = zis.getNextEntry();
            }
            zis.closeEntry();

        } catch (IOException ex) {

        } finally {
            try {
                zis.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static String getActiveCommitSha1ByBranchSha1(String i_BranchSha1, String i_RepositoryPath) {
        String BranchSha1 = i_BranchSha1;
        ZipFile zipFile = null;
        InputStream stream = null;
        String sha1 = "";
        Path path = Paths.get(i_RepositoryPath + "\\.magit\\objects\\" + BranchSha1 + ".zip");
        sha1 = readZipIntoString(path.toString()).get(0);
        return sha1;
    }

    public static String getRootFolderSha1ByCommitSha1(String repositoryPath) {
//       String activeCommitSha1= getActiveCommitSha1ByBranchSha1(repositoryPath);
//        ZipFile zipFile=null;
//        InputStream stream=null;
//        String sha1="";
//        String commitFileContent;
//
//        Path path= Paths.get(repositoryPath+"\\.magit\\objects\\"+activeCommitSha1+".zip");
//        commitFileContent=readZipIntoString(path.toString());
//        sha1=ConvertCommaSeparatedStringToList(commitFileContent).get(0);
//      return sha1;
        return "";
    }

    public static List<String> ConvertCommaSeparatedStringToList(String commaSeparatedStr) {
        String[] commaSeparatedArr = commaSeparatedStr.split("\\s*,\\s*");
        List<String> result = Arrays.stream(commaSeparatedArr).collect(Collectors.toList());
        return result;
    }


    private static String getRootFolderSha1ByCommitFile(String i_CommitSha1, String repositoryPath) {
        return readZipIntoString(repositoryPath + "\\.magit\\objects\\" + i_CommitSha1 + ".zip").get(0);
    }

    private static List<String> readZipIntoString(String i_ZipPath) {
        ZipFile zipFile = null;
        InputStream stream = null;
        List<String> lines = null;
        //  String stringToReturn="";
        try {
            zipFile = new ZipFile(i_ZipPath);
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            stream = zipFile.getInputStream(entries.nextElement());
            //stringToReturn=IOUtils.toString(stream,"utf-8");
            lines = IOUtils.readLines(stream, "utf-8");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                zipFile.close();
                stream.close();
            } catch (IOException ex) {
            }

        }
        return lines;
    }

    public static File newFile(File destinationDir, ZipEntry zipEntry) throws IOException {
        File destFile = new File(destinationDir, zipEntry.getName());

        String destDirPath = destinationDir.getCanonicalPath();
        String destFilePath = destFile.getCanonicalPath();

        if (!destFilePath.startsWith(destDirPath + File.separator)) {
            throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
        }

        return destFile;
    }

    private static void createZipFileIntoObjectsFolder(Path repositoryPath, Path filePath, String sha1, String i_TestFolderName) {
        ZipOutputStream zos = null;
        FileOutputStream fos = null;
        try {
            File file = filePath.toFile();
            String zipFileName = sha1.concat(".zip");

            fos = new FileOutputStream(repositoryPath.toString() + getZipSaveFolderName(i_TestFolderName) + "\\" + zipFileName);
            zos = new ZipOutputStream(fos);
            zos.putNextEntry(new ZipEntry(file.getName()));
            byte[] bytes = Files.readAllBytes(filePath);
            zos.write(bytes, 0, bytes.length);
            zos.closeEntry();

        } catch (FileNotFoundException ex) {
            System.err.format("createZipFile: The file %s does not exist", filePath.toString());
        } catch (IOException ex) {
            System.err.println("createZipFile: I/O error: " + ex);
        } finally {
            try {
                zos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static String getZipSaveFolderName(String i_TestFolderName) {
        return i_TestFolderName != "" ? s_GitDirectory + i_TestFolderName : s_ObjectsFolderDirectoryString;
    }

    public static String CreateCommitDescriptionFile(Commit i_Commit, Path i_RepositoryPath, Boolean i_IsGeneratedFromXML) {//clean thin function!!!!!!
        BufferedWriter bf = null;
        String commitDescriptionFilePathString = i_RepositoryPath.toString() + s_ObjectsFolderDirectoryString + i_Commit.getCommitComment() + ".txt";
        FileWriter outputFile = null;
        String commitInformationString = "";
        String commitDataForGenerateSha1 = "";
        String sha1String = "";
        String fileCreationDateString = "";

        try {
            outputFile = new FileWriter(commitDescriptionFilePathString);
            bf = new BufferedWriter(outputFile);
            fileCreationDateString = i_IsGeneratedFromXML ? i_Commit.getCreationDate() :  getFileCreationDateByPath(Paths.get(commitDescriptionFilePathString));
            i_Commit.setCreationDate(fileCreationDateString);
            commitDataForGenerateSha1 = commitDataForGenerateSha1.concat(i_Commit.getRootSHA1() + "," + i_Commit.getCommitComment());

            commitInformationString = commitInformationString.concat(
                    i_Commit.getRootSHA1() + '\n' +
                            i_Commit.GetPreviousCommitsSHA1String() + '\n' +
                            i_Commit.getCommitComment() + '\n' +
                            i_Commit.getCreationDate() + '\n' +
                            i_Commit.getCreatedBy());

            sha1String = DigestUtils.sha1Hex(commitDataForGenerateSha1);
            bf.write(commitInformationString);

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                bf.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        createZipFileIntoObjectsFolder(i_RepositoryPath, Paths.get(commitDescriptionFilePathString), sha1String, "");
        Paths.get(commitDescriptionFilePathString).toFile().delete();
        return sha1String;
    }

    private static String getFileCreationDateByPath(Path path) throws IOException {
        return ConvertLongToSimpleDateTime(Files.readAttributes(path, BasicFileAttributes.class).creationTime().toMillis());
    }

    public static Boolean IsFileOrDirectoryEmpty(File file) {
        return !file.isDirectory() && IsFileEmpty(file) || file.isDirectory() && IsDirectoryEmpty(file);
    }

    public static Boolean IsFileEmpty(File file) {
        boolean isEmpty = false;
        if (!file.isDirectory()) {
            if (file.length() == 0) {
                isEmpty = true;
            }
        }
        return isEmpty;
    }

    public static Boolean IsDirectoryEmpty(File file) {
        boolean isEmpty = false;
        if (file.isDirectory()) {
            if (file.list().length == 0) {
                isEmpty = true;
            }
        }
        return isEmpty;
    }

    private static BlobData getBlobByFile(Folder i_CurrentFolder, File i_CurrentFileInFolder) {
        List<BlobData> blobList = i_CurrentFolder.getBlobList();
        BlobData resultBlob = null;
        for (BlobData blob : blobList) {
            if (blob.getPath().equals(i_CurrentFileInFolder.toString())) {
                resultBlob = blob;
                //sha1String = blob.getSHA1();
                break;
            }
        }

        return resultBlob;
    }

    private static String getCurrentBasicData(File i_CurrentFileInFolder, BlobData i_CurrentFolderBlob) {
        String sha1String = getBlobByFile(i_CurrentFolderBlob.getCurrentFolder(), i_CurrentFileInFolder).getSHA1();
        String type = i_CurrentFileInFolder.isFile() ? "file" : "folder";
        String basicDataString = String.format(
                "%s,%s,%s",
                i_CurrentFileInFolder.getName(),
                type,
                sha1String
        );

        return basicDataString;
    }

    private static String getFolderDescriptionFilePathStaring(Path folderPath) {
        return folderPath.toString() + "\\" + folderPath.toFile().getName() + ".txt";
    }

    //        String stringForSha1 = getStringForFolderSHA1(i_BlobDataOfCurrentFolder, folderPath, userName, folderDescriptionFilePathString);
    private static String getStringForFolderSHA1(BlobData i_BlobDataOfCurrentFolder, Path folderPath, String userName, String folderDescriptionFilePathString, boolean i_IsGeneretedFromXml) {
        File currentFolderFile = folderPath.toFile();
        String stringForSha1 = "";
        String basicDataString = "";
        String fullDataString = "";
        FileWriter outputFile = null;
        BufferedWriter bf = null;
        List<BlobData> blobDataList = null;
        String lastUpdateTime = "";
        try {
            outputFile = new FileWriter(folderDescriptionFilePathString);
            bf = new BufferedWriter(outputFile);

            for (File file : currentFolderFile.listFiles()) {
                if (isFileValidForScanning(file, folderDescriptionFilePathString, folderPath)) {
                    BlobData currentBlob = getBlobByFile(i_BlobDataOfCurrentFolder.getCurrentFolder(), file);
                    lastUpdateTime = i_IsGeneretedFromXml ? currentBlob.getLastChangedTime() : ConvertLongToSimpleDateTime(file.lastModified());
                    basicDataString = getCurrentBasicData(file, i_BlobDataOfCurrentFolder);
                    fullDataString = fullDataString.concat(basicDataString + "," + userName + "," + lastUpdateTime + '\n');
                    stringForSha1 = stringForSha1.concat(basicDataString);
                }
            }

            bf.write(String.format('\n' + fullDataString));
        } catch (IOException e) {
        } finally {
            try {
                bf.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return stringForSha1;
    }


    public static String CreateFolderDescriptionFile(BlobData i_BlobDataOfCurrentFolder, Path repositoryPath, Path folderPath, String userName, String i_TestFolderName, boolean isGeneretedFromXml) {
        String folderDescriptionFilePathString = getFolderDescriptionFilePathStaring(folderPath);
        String stringForSha1 = getStringForFolderSHA1(i_BlobDataOfCurrentFolder, folderPath, userName, folderDescriptionFilePathString, isGeneretedFromXml);
        String sha1String = DigestUtils.sha1Hex(stringForSha1);
        createZipFileIntoObjectsFolder(repositoryPath, Paths.get(folderDescriptionFilePathString), sha1String, i_TestFolderName);
        Paths.get(folderDescriptionFilePathString).toFile().delete();

        return sha1String;
    }

    private static boolean isFileValidForScanning(File file, String folderDescriptionFilePathString, Path folderPath) {
        return (!IsFileOrDirectoryEmpty(file) && !file.toString().equals(folderPath.toString() + "\\.magit")//!isFileOrDirectoryEmpty(file)!!!!!! remove maybe
                && (!(file.toString()).equals(folderDescriptionFilePathString)));
    }

    private static String readLineByLine(String filePath) {
//        StringBuilder contentBuilder = new StringBuilder();
//        try (Stream<String> stream = Files.lines(Paths.get(filePath), StandardCharsets.UTF_8)) {
//            stream.forEach(s -> contentBuilder.append(s).append("\n"));
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return contentBuilder.toString();
        String returnValue = "";
        try {
            returnValue = FileUtils.readFileToString(Paths.get(filePath).toFile(), "utf-8");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return returnValue;
    }

    public static List<String> GetCommitData(String i_CommitSha1, String repositoryPath) {
        List<String> lines = readZipIntoString(repositoryPath + "\\.magit\\objects\\" + i_CommitSha1 + ".zip");
        lines.remove(1);
        if (lines.size() == 1 && lines.get(0).equals(""))
            return null;
        return lines;
    }

    public static String getHeadBranchSha1(String repositoryPath) {
        Path path = Paths.get(repositoryPath + "\\.magit\\branches\\HEAD.txt");
        String sha1 = "";

        try {
            sha1 = FileUtils.readFileToString(path.toFile(), "utf-8");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sha1;
    }

    public static List<String> getBranchesList(String repositoryPath) {
        File branchesFolder = Paths.get(repositoryPath + "\\.magit\\branches").toFile();
        List<String> branchesList = new LinkedList<>();
//        //the head of list is HEAD.txt (HEAD branch)
//        branchesList.add("HEAD,"+getHeadBranchSha1(repositoryPath));
        for (File file : branchesFolder.listFiles()) {
            if (!file.getName().equals("HEAD.txt")) {
                branchesList.add(FilenameUtils.removeExtension(file.getName()) + ',' + readLineByLine(file.getPath()));
            }
        }
        return branchesList;
    }


    public static String GetCommitNameInZipFromObjects(String i_CommitSha1, String repositoryPath) {
        return FilenameUtils.removeExtension(GetFileNameInZip(repositoryPath + "\\.magit\\objects\\" + i_CommitSha1 + ".zip"));
    }

    public static Path GetPathInObjectsBySha1(String Sha1, String repositoryPath) {
        return Paths.get(repositoryPath + "\\.magit\\objects\\" + Sha1 + ".zip");
    }

    public static String GetFileNameInZip(String i_Path) {
        String fileName = "";
        try (ZipFile zipFile = new ZipFile(i_Path)) {
            Enumeration zipEntries = zipFile.entries();
            fileName = ((ZipEntry) zipEntries.nextElement()).getName();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return fileName;
    }


    public static List<String> getDataFilesList(String repositoryPath, String i_RootSha1) {
        List<String> lines = readZipIntoString(repositoryPath + "\\.magit\\objects\\" + i_RootSha1 + ".zip");
        if (lines.size() == 1 && lines.get(0).equals(""))
            return null;
        return lines;
    }

    public static List<String> GetDataFilesListByPath(String i_Path) {
        List<String> lines = readZipIntoString(i_Path);
        if (lines.size() == 1 && lines.get(0).equals(""))
            return null;
        return lines;
    }


    public static List<String> commitsHistoryList(String m_CommitSha1, String repositoryPath) {
        String data = m_CommitSha1 + ",";
        data = data.concat(readZipIntoString(repositoryPath + "\\.magit\\objects\\" + m_CommitSha1 + ".zip").get(1));
        return ConvertCommaSeparatedStringToList(data);

    }


}


