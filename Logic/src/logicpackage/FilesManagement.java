package logicpackage;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.lang.reflect.Array;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;


public class FilesManagement {
    public final static String s_ObjectsFolderDirectoryString = "\\.magit\\objects\\";
    public final static String s_BranchesFolderDirectoryString = "\\.magit\\branches\\";
    public final static String s_GitDirectory = "\\.magit\\";
    public final static String s_XmlBuildFolderName = "XML Build";


    public static boolean IsRepositoryExistInPath(String path) {
        return Paths.get(path + "\\.magit").toFile().exists();
    }

    public static void CleanWC(Path i_PathToClean) {
        File[] listFiles = i_PathToClean.toFile().listFiles(pathname -> (!pathname.getAbsolutePath().contains(".magit")));
        for (File file : listFiles) {
            if (file.isDirectory()) {
                try {
                    FileUtils.cleanDirectory(file);
                } catch (IOException e) {
                    System.out.println("clear wc failed");
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
           if(i_Commit!=null) {
               bf.write(i_Commit.getCurrentCommitSHA1());
               sha1 = DigestUtils.sha1Hex(i_Commit.getCurrentCommitSHA1() + i_BranchName);
           }
           else{
               sha1= DigestUtils.sha1Hex(i_BranchName);
           }
        } catch (IOException ex) {
            System.out.println("create branch failed");
        } finally {
            try {
                bf.close();
            } catch (IOException e) {
                System.out.println("Action failed");
            }
        }

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
            System.out.println("Action failed");

        } finally {
            try {
                bf.close();
            } catch (IOException e) {
                System.out.println("Action failed");
            }
        }

        createZipFileIntoObjectsFolder(i_RepositoryPath, headPath, sha1, "");
        return sha1;
    }

    public static String ConvertLongToSimpleDateTime(long i_Time) {
        Date date = new Date(i_Time);
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy-HH:mm:ss:sss");
        String dateText = dateFormat.format(date);

        return dateText;
    }

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
            bf.write(String.format("%s", description));
            sha1 = DigestUtils.sha1Hex(descriptionStringForGenerateSha1);
            File testFile = Paths.get(repositoryPath.toString() + s_ObjectsFolderDirectoryString + "\\" + sha1 + ".zip").toFile();
            if (testFile.exists() && !file.getAbsolutePath().equals(repositoryPath.toString())) {
                simpleBlob = CreateUchangedBlob(file, repositoryPath, sha1);
            } else {
                simpleBlob = new BlobData(repositoryPath, file.getAbsolutePath(), i_UserName, getUpdateDate(i_DateCreated, file), false, sha1, null);
            }
        } catch (IOException e) {
            System.out.println("Action failed");

        } finally {
            try {
                bf.close();
            } catch (IOException e) {
                System.out.println("Action failed");
            }
        }

        createZipFileIntoObjectsFolder(repositoryPath, Paths.get(fileDescriptionFilePathString), sha1, i_TestFolderName);
        Paths.get(fileDescriptionFilePathString).toFile().delete();
        return simpleBlob;
    }


    public static BlobData CreateUchangedBlob(File file, Path repositoryPath, String sha1) {
        File parentFolderFile = file.getParentFile();
        String parenFolderFile= parentFolderFile.getName();
        if(FilenameUtils.getExtension(parenFolderFile).equals(""))
        {
            parenFolderFile=parenFolderFile.concat(".txt");
        }

        File zipFile = FindFileByNameInZipFileInPath(
                parenFolderFile, Paths.get(repositoryPath.toString() + s_ObjectsFolderDirectoryString));

        List<String> parentFolderFileContentList = FilesManagement.GetDataFilesListOfZipByPath(zipFile.getAbsolutePath());
        String userName = "";
        String lastModifiedDate = "";
        for (String line : parentFolderFileContentList) {
            List<String> dataLine = ConvertCommaSeparatedStringToList(line);
            if (dataLine.get(0).equals(file.getName())) {
                userName = dataLine.get(3);
                lastModifiedDate = dataLine.get(4);
                break;
            }
        }

        return new BlobData(repositoryPath, file.getAbsolutePath(), userName, lastModifiedDate, false, sha1, null);
    }

    private static String getUpdateDate(String i_Date, File i_File) {
        String result = i_Date;
        if (i_Date == null || i_Date == "") {
            result = ConvertLongToSimpleDateTime(i_File.lastModified());
        }

        return result;
    }


    public static void ExtractZipFileToPath(Path i_ZipFilePath, Path i_DestinitionPath) {
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
            System.out.println("Action failed");

        } finally {
            try {
                zis.close();
            } catch (IOException e) {
                System.out.println("Action failed");
            }
        }
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
        try {
            zipFile = new ZipFile(i_ZipPath);
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            stream = zipFile.getInputStream(entries.nextElement());
            lines = IOUtils.readLines(stream, "utf-8");
        } catch (IOException e) {
            System.out.println("Action failed");
        } finally {
            try {
                zipFile.close();
                stream.close();
            } catch (IOException ex) {
                System.out.println("Action failed");
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
                System.out.println("Action failed");
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
            fileCreationDateString = i_IsGeneratedFromXML ? i_Commit.getCreationDate() : getFileCreationDateByPath(Paths.get(commitDescriptionFilePathString));
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
            System.out.println("Action failed");
        } finally {
            try {
                bf.close();
            } catch (IOException e) {
                System.out.println("Action failed");
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

    private static String getStringForFolderSHA1(BlobData i_BlobDataOfCurrentFolder, Path folderPath, String userName, String folderDescriptionFilePathString, boolean i_IsGeneretedFromXml) {
        File currentFolderFile = folderPath.toFile();
        String stringForSha1 = "";
        String basicDataString = "";
        String fullDataString = "";
        String sha1 = "";
        FileWriter outputFile = null;
        BufferedWriter bf = null;
        List<BlobData> blobDataList = null;
        String lastUpdateTime = "";
        Path repositoryPath = i_BlobDataOfCurrentFolder.GetRepositoryPath();
        try {
            outputFile = new FileWriter(folderDescriptionFilePathString);
            bf = new BufferedWriter(outputFile);

            for (File file : currentFolderFile.listFiles()) {
                if (isFileValidForScanning(file, folderDescriptionFilePathString, folderPath)) {
                    BlobData currentBlob = getBlobByFile(i_BlobDataOfCurrentFolder.getCurrentFolder(), file);
                    lastUpdateTime = i_IsGeneretedFromXml ? currentBlob.getLastChangedTime() : ConvertLongToSimpleDateTime(file.lastModified());
                    basicDataString = getCurrentBasicData(file, i_BlobDataOfCurrentFolder);
                    String lastChangedBy = currentBlob.getLastChangedBY().isEmpty() ? userName : currentBlob.getLastChangedBY();//****
                    fullDataString = fullDataString.concat(basicDataString + "," + lastChangedBy + "," + lastUpdateTime + '\n');
                    stringForSha1 = stringForSha1.concat(basicDataString);
                }
            }

            sha1 = DigestUtils.sha1Hex(stringForSha1);
            setUnchaingedFolderDetatiles(i_BlobDataOfCurrentFolder, repositoryPath, folderPath, sha1);
            bf.write(String.format('\n' + fullDataString));

        } catch (IOException e) {
            System.out.println("Action failed");
        } finally {
            try {
                bf.close();
            } catch (IOException e) {
                System.out.println("Action failed");
            }
        }
        return sha1;
    }

    private static void setUnchaingedFolderDetatiles(BlobData i_BlobDataOfCurrentFolder, Path repositoryPath, Path folderPath, String sha1) {
        File testFile = Paths.get(repositoryPath.toString() + s_ObjectsFolderDirectoryString + "\\" + sha1 + ".zip").toFile();
        if (testFile.exists() && !folderPath.toFile().getAbsolutePath().equals(repositoryPath.toString())) {
            BlobData tempBlobForGetSpecificData = FilesManagement.CreateUchangedBlob(folderPath.toFile(), repositoryPath, sha1);
            i_BlobDataOfCurrentFolder.setLastChangedBY(tempBlobForGetSpecificData.getLastChangedBY());
            i_BlobDataOfCurrentFolder.setLastChangedTime(tempBlobForGetSpecificData.getLastChangedTime());
        }

    }


    public static String CreateFolderDescriptionFile(BlobData i_BlobDataOfCurrentFolder, Path repositoryPath, Path folderPath, String userName, String i_TestFolderName, boolean isGeneretedFromXml) {
        String folderDescriptionFilePathString = getFolderDescriptionFilePathStaring(folderPath);
        String sha1 = getStringForFolderSHA1(i_BlobDataOfCurrentFolder, folderPath, userName, folderDescriptionFilePathString, isGeneretedFromXml);
        createZipFileIntoObjectsFolder(repositoryPath, Paths.get(folderDescriptionFilePathString), sha1, i_TestFolderName);
        Paths.get(folderDescriptionFilePathString).toFile().delete();

        return sha1;
    }

    private static boolean isFileValidForScanning(File file, String folderDescriptionFilePathString, Path folderPath) {
        return (!IsFileOrDirectoryEmpty(file) && !file.toString().equals(folderPath.toString() + "\\.magit")
                && (!(file.toString()).equals(folderDescriptionFilePathString)));
    }

    private static String readLineByLine(String filePath) {
        String returnValue = "";
        try {
            returnValue = FileUtils.readFileToString(Paths.get(filePath).toFile(), "utf-8");
        } catch (IOException e) {
            System.out.println("Action failed");
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
            System.out.println("Action failed");
        }
        return sha1;
    }

    public static List<String> getBranchesList(String repositoryPath) {
        File branchesFolder = Paths.get(repositoryPath + "\\.magit\\branches").toFile();
        List<String> branchesList = new LinkedList<>();

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

    public static String GetFileNameInZip(String i_Path) {
        String fileName = "";
            try (ZipFile zipFile = new ZipFile(i_Path)) {
                Enumeration zipEntries = zipFile.entries();
                fileName = ((ZipEntry) zipEntries.nextElement()).getName();
            } catch (IOException e) {
                System.out.println("Action failed in method:GetFileNameInZip class: FileManagement line: 535 with path:" + i_Path);
            }
        return fileName;
    }


    public static List<String> getDataFilesList(String repositoryPath, String i_RootSha1) {
        List<String> lines = readZipIntoString(repositoryPath + "\\.magit\\objects\\" + i_RootSha1 + ".zip");
        if (lines.size() == 1 && lines.get(0).equals(""))
            return null;
        return lines;
    }

    public static List<String> GetDataFilesListOfZipByPath(String i_Path) {
        List<String> lines = readZipIntoString(i_Path);
        if (lines.size() == 1 && lines.get(0).equals(""))
            return null;
        return lines;
    }

    public static File FindFileByNameInZipFileInPath(String i_NameFile, Path i_Path) {
        File fileToReturn = null;
         File[]files=i_Path.toFile().listFiles();
         Arrays.sort(files,Comparator.comparingLong(File::lastModified));
        for (File zipFile : files) {
           if(FilenameUtils.getExtension(zipFile.getName()).equals("zip")) {
               if (GetFileNameInZip(zipFile.getAbsolutePath()).equals(i_NameFile)) {
                   fileToReturn = zipFile;
                   break;
               }
           }
        }
        return fileToReturn;
    }


    public static List<String> GetCommitsHistoryList(String m_CommitSha1, String repositoryPath) {
        String data = m_CommitSha1 + ",";
        data = data.concat(readZipIntoString(repositoryPath + "\\.magit\\objects\\" + m_CommitSha1 + ".zip").get(1));
        return ConvertCommaSeparatedStringToList(data);

    }


}


