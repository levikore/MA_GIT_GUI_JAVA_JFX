package logicpackage;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;


import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import static java.nio.file.Files.delete;


public class FilesManagement {
    private final static String s_ObjectsFolderDirectoryString = "\\.magit\\objects\\";
    private final static String s_BranchesFolderDirectoryString = "\\.magit\\branches\\";
    private final static String s_GitDirectory = "\\.magit\\";


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
    public static BlobData CreateSimpleFileDescription(Path repositoryPath, Path filePathOrigin, String i_UserName, String i_TestFolderName) {
        return createTemporaryFileDescription(repositoryPath, filePathOrigin, i_UserName, i_TestFolderName);
    }

    private static BlobData createTemporaryFileDescription(Path repositoryPath, Path i_FilePath, String i_UserName, String i_TestFolderName) {
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
            simpleBlob = new BlobData(repositoryPath, file.getAbsolutePath(), i_UserName, ConvertLongToSimpleDateTime(file.lastModified()), false, sha1);
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


    public static void ExtractZipFileToPath(Path i_ZipFilePath, Path i_DestinitionPath){
        //try catch finally!!!!!!!!!!!
        ZipInputStream zis=null;
        String fileZip = i_ZipFilePath.toString();
        File destDir = new File(i_DestinitionPath.toString());
        byte[] buffer = new byte[1024];
        try {
            zis= new ZipInputStream(new FileInputStream(fileZip));
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

        }catch (IOException ex){

        }finally {
            try {
                zis.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
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

    public static String CreateCommitDescriptionFile(Commit i_Commit, Path i_RepositoryPath) {//clean thin function!!!!!!
        BufferedWriter bf = null;
        String commitDescriptionFilePathString = i_RepositoryPath.toString() + s_ObjectsFolderDirectoryString + i_Commit.getCommitComment() + ".txt";
        System.out.println("commitDescriptionFilePathString" + commitDescriptionFilePathString);
        FileWriter outputFile = null;
        String commitInformationString = "";
        String commitDataForGenerateSha1 = "";
        String sha1String = "";
        String fileCreationDateString = "";

        try {
            outputFile = new FileWriter(commitDescriptionFilePathString);
            bf = new BufferedWriter(outputFile);
            fileCreationDateString = getFileCreationDateByPath(Paths.get(commitDescriptionFilePathString));

            i_Commit.setCreationDate(fileCreationDateString);


            commitDataForGenerateSha1 = commitDataForGenerateSha1.concat(i_Commit.getRootSHA1() + "," + i_Commit.getCommitComment());

            commitInformationString = commitInformationString.concat(
                    commitDataForGenerateSha1 + "," +
                            i_Commit.GetPreviousCommitsSHA1String() + "," +
                            i_Commit.getCreationDate() + "," +
                            i_Commit.getCreatedBy());

            System.out.println("commitInformationString" + commitInformationString);

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
        System.out.println("in isFileOrDirectoryEmpty: "
                + "!file.isDirectory()&&isFileEmpty(file) || file.isDirectory()&&isDirectoryEmpty(file): "
                + (!file.isDirectory() && IsFileEmpty(file) || file.isDirectory() && IsDirectoryEmpty(file)));
        return !file.isDirectory() && IsFileEmpty(file) || file.isDirectory() && IsDirectoryEmpty(file);
    }

    public static Boolean IsFileEmpty(File file) {
        boolean isEmpty = false;
        if (!file.isDirectory()) {
            if (file.length() == 0) {
                isEmpty = true;
            }
        }
        System.out.println("isFileEmpty: " + isEmpty);
        return isEmpty;
    }

    public static Boolean IsDirectoryEmpty(File file) {
        boolean isEmpty = false;
        if (file.isDirectory()) {
            if (file.list().length == 0) {
                isEmpty = true;
            }
        }
        System.out.println("isDirectoryEmpty: " + isEmpty + "ile.list().length: " + file.list().length);
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
    private static String getStringForFolderSHA1(BlobData i_BlobDataOfCurrentFolder, Path folderPath, String userName, String folderDescriptionFilePathString) {
        File currentFolderFile = folderPath.toFile();
        String stringForSha1 = "";
        String basicDataString = "";
        String fullDataString = "";
        FileWriter outputFile = null;
        BufferedWriter bf = null;
        try {
            outputFile = new FileWriter(folderDescriptionFilePathString);
            bf = new BufferedWriter(outputFile);

            for (File file : currentFolderFile.listFiles()) {
                if (isFileValidForScanning(file, folderDescriptionFilePathString, folderPath)) {
                    basicDataString = getCurrentBasicData(file, i_BlobDataOfCurrentFolder);
                    fullDataString = fullDataString.concat(basicDataString + "," + userName + "," + ConvertLongToSimpleDateTime(file.lastModified()) + '\n');
                    stringForSha1 = stringForSha1.concat(basicDataString);
                }
            }

            bf.write(String.format("%s\n", fullDataString));

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

    //    private void exitRootTreeBranchAndUpdate(BlobData i_BlobDataOfCurrentFolder, Path i_RootFolderPath, String i_UserName, List<File> emptyFilesList) {
//        if (i_RootFolderPath.toFile().isDirectory() && !FilesManagement.IsDirectoryEmpty(i_RootFolderPath.toFile())) {
//            String sha1 = FilesManagement.CreateFolderDescriptionFile(
//                    i_BlobDataOfCurrentFolder,
//                    m_RootFolderPath,
//                    Paths.get(i_RootFolderPath.toAbsolutePath().toString()),
//                    i_UserName);
//            i_BlobDataOfCurrentFolder.setSHA1(sha1);
//            i_BlobDataOfCurrentFolder.getCurrentFolder().setFolderSha1(sha1);
//            i_BlobDataOfCurrentFolder.setLastChangedTime(FilesManagement.ConvertLongToSimpleDateTime(i_RootFolderPath.toFile().lastModified()));
//        }
////            else if(i_RootFolderPath.toFile().isDirectory()){
////                i_RootFolderPath.toFile().delete();
////            }
//
////            deleteEmptyFiles(emptyFilesList);
//    }
    public static String CreateFolderDescriptionFile(BlobData i_BlobDataOfCurrentFolder, Path repositoryPath, Path folderPath, String userName, String i_TestFolderName) {
        String folderDescriptionFilePathString = getFolderDescriptionFilePathStaring(folderPath);
        String stringForSha1 = getStringForFolderSHA1(i_BlobDataOfCurrentFolder, folderPath, userName, folderDescriptionFilePathString);
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
        StringBuilder contentBuilder = new StringBuilder();
        try (Stream<String> stream = Files.lines(Paths.get(filePath), StandardCharsets.UTF_8)) {
            stream.forEach(s -> contentBuilder.append(s).append("\n"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return contentBuilder.toString();
    }
}
