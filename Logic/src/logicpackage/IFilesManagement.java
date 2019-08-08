package logicpackage;

import org.apache.commons.codec.digest.DigestUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;


public interface IFilesManagement {

    static Path getProjectPath() {
        return Paths.get(System.getProperty("user.dir"));
    }


//    static String getSha1(String path) {
//        String sha1 = null;
//        try {
//            InputStream is = new FileInputStream(path);
//            sha1 = DigestUtils.sha1Hex(is);
//            System.out.println("Digest          = " + sha1);
//            is.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return sha1;
//    }

    static String getSha1FromString(String fileContent) {
        return DigestUtils.sha1Hex(fileContent);
    }

    static void CreateFolder(Path path, String name) {
        Path newDirectoryPath = Paths.get(path.toString() + "/" + name);

        File directory = new File(newDirectoryPath.toString());
        if (!directory.exists()) {
            directory.mkdir();
        }
    }

    static String convertLongToSimpleDateTime(long i_Time) {
        Date date = new Date(i_Time);
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.mm.yyyy-hh:mm:ss:sss");
        String dateText = dateFormat.format(date);

        return dateText;
    }

    //input: c:\\..\\[repositoryName]\\[nameFile.txt]
    static BlobData createSimpleFileDescription(Path repositoryPath, Path filePathOrigin, String i_UserName) {
        return createTemporaryFileDescription(repositoryPath, filePathOrigin, i_UserName);
    }

    static BlobData createTemporaryFileDescription(Path repositoryPath, Path i_FilePath, String i_UserName) {
        File file = i_FilePath.toFile();
        Path parentFolder = i_FilePath.getParent();
        //repositoryPath.toString() + "\\.magit\\objects\\" + zipFileName
        String fileDescriptionFilePathString = repositoryPath.toString() + "\\.magit\\objects\\" + file.getName(); //+ ".txt";
        String descriptionStringForGenerateSha1 = "";
        String description = "";
        FileWriter outputFile = null;
        String sha1 = "";
        String type = file.isFile() ? "file" : "folder";
        BlobData simpleBlob = null;

        try {
            outputFile = new FileWriter(fileDescriptionFilePathString);
            BufferedWriter bf = new BufferedWriter(outputFile);
            description = readLineByLineJava8(i_FilePath.toString());
            descriptionStringForGenerateSha1 = String.format("%s,%s,%s", file.getAbsolutePath(), type, description);
            bf.write(String.format("%s\n", description));
            bf.close();
            sha1 =  DigestUtils.sha1Hex(descriptionStringForGenerateSha1);
            createZipFileIntoObjectsFolder(repositoryPath, Paths.get(fileDescriptionFilePathString), sha1);
            Paths.get(fileDescriptionFilePathString).toFile().delete();
            simpleBlob = new BlobData(file.getAbsolutePath(), i_UserName, convertLongToSimpleDateTime(file.lastModified()), false, sha1);
        } catch (IOException e) {

        }
        return simpleBlob;
    }


    static void createZipFileIntoObjectsFolder(Path repositoryPath, Path filePath, String sha1) {
        try {
            File file = filePath.toFile();
            //String sha1 = getSha1(filePath.toString());

            String zipFileName = sha1.concat(".zip");
            FileOutputStream fos = new FileOutputStream(repositoryPath.toString() + "\\.magit\\objects\\" + zipFileName);
            ZipOutputStream zos = new ZipOutputStream(fos);

            zos.putNextEntry(new ZipEntry(file.getName()));

            byte[] bytes = Files.readAllBytes(filePath);
            zos.write(bytes, 0, bytes.length);
            zos.closeEntry();
            zos.close();

        } catch (FileNotFoundException ex) {
            System.err.format("createZipFile: The file %s does not exist", filePath.toString());
        } catch (IOException ex) {
            System.err.println("createZipFile: I/O error: " + ex);
        }
    }

    static String createCommitDescriptionFile(Commit i_Commit, Path i_SaveFolderPath) {
        String commitDescriptionFileString = i_SaveFolderPath.toString() + "\\" + i_Commit.getCommitComment() + ".txt";
        FileWriter outputFile = null;
        String commitInformationString = "";

        try {
            outputFile = new FileWriter(commitDescriptionFileString);
            BufferedWriter bf = new BufferedWriter(outputFile);

            commitInformationString = String.format(
                    "%s,%s,%s,%s",
                    i_Commit.getRootSHA1(),
                    i_Commit.getCommitComment(),
                    i_Commit.getCreationDate(),
                    i_Commit.getCreatedBy());

            try {
                bf.write(commitInformationString + "\n");
            } catch (IOException e) {
                e.printStackTrace();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return "";
    }

    static Boolean isFileOrDirectoryEmpty(File file) {
        System.out.println("in isFileOrDirectoryEmpty: "
                +"!file.isDirectory()&&isFileEmpty(file) || file.isDirectory()&&isDirectoryEmpty(file): "
                + (!file.isDirectory()&&isFileEmpty(file) || file.isDirectory()&&isDirectoryEmpty(file)));
        return !file.isDirectory()&&isFileEmpty(file) || file.isDirectory()&&isDirectoryEmpty(file);
    }

    static Boolean isFileEmpty(File file) {
        boolean isEmpty = false;
        if (!file.isDirectory()) {
            if (file.length() == 0) {
                isEmpty = true;
            }
        }
        System.out.println("isFileEmpty: "+ isEmpty);
        return isEmpty;
    }

    static Boolean isDirectoryEmpty(File file) {
        boolean isEmpty = false;
        if (file.isDirectory()) {
            if (file.list().length == 0) {
                isEmpty = true;
            }
        }
        System.out.println("isDirectoryEmpty: "+ isEmpty+"ile.list().length: "+file.list().length);
        return isEmpty;
    }

    static String createFolderDescriptionFile(BlobData i_Blob, Path repositoryPath, Path folderPath, String userName) {
        File currentFolder = folderPath.toFile();
        String folderDescriptionFilePathString = folderPath.toString() + "\\" + currentFolder.getName(); //+ ".txt";
        String sha1String = "";
        String stringForSha1 = "";
        String basicDataString = "";
        String fullDataString = "";
        FileWriter outputFile = null;

        try {
            outputFile = new FileWriter(folderDescriptionFilePathString);
            BufferedWriter bf = new BufferedWriter(outputFile);
            List<BlobData> blobList = i_Blob.getCurrentFolder().getBlobList();
            File currentFileInFolder;

            for (File file : currentFolder.listFiles()) {

                if (!isFileOrDirectoryEmpty(file) && !file.toString().equals(folderPath.toString() + "\\.magit")//!isFileOrDirectoryEmpty(file)!!!!!! remove maybe
                        && (!(file.toString()).equals(folderDescriptionFilePathString))) {

                    System.out.println(!file.getAbsolutePath().equals(folderDescriptionFilePathString) == true);
                    currentFileInFolder = file;
                    String type = currentFileInFolder.isFile() ? "file" : "folder";

                    for (BlobData blob : blobList) {
                        if (blob.getPath().equals(currentFileInFolder.toString())) {
                            sha1String = blob.getSHA1();
                            break;
                        }
                    }

                    basicDataString = String.format(
                            "%s,%s,%s",
                            currentFileInFolder.getName(),
                            type,
                            sha1String
                    );

                    fullDataString = fullDataString.concat(basicDataString + "," + userName + "," +
                            convertLongToSimpleDateTime(currentFileInFolder.lastModified()) + '\n');


                    stringForSha1 = stringForSha1.concat(basicDataString);
                }
            }
            try {
                bf.write(String.format("%s\n", fullDataString));
            } catch (IOException e) {
                e.printStackTrace();
            }

            bf.close();
            sha1String = DigestUtils.sha1Hex(stringForSha1);
            createZipFileIntoObjectsFolder(repositoryPath, Paths.get(folderDescriptionFilePathString), sha1String);
            Paths.get(folderDescriptionFilePathString).toFile().delete();
        } catch (IOException e) {

        }
        return sha1String;
    }

    static String readLineByLineJava8(String filePath) {
        StringBuilder contentBuilder = new StringBuilder();
        try (Stream<String> stream = Files.lines(Paths.get(filePath), StandardCharsets.UTF_8)) {
            stream.forEach(s -> contentBuilder.append(s).append("\n"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return contentBuilder.toString();
    }
}
