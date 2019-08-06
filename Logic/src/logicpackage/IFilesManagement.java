package logicpackage;

import org.apache.commons.codec.digest.DigestUtils;

import java.io.*;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;


public interface IFilesManagement {

    static Path getProjectPath() {
        return Paths.get(System.getProperty("user.dir"));
    }


    static String getSha1(String path) {
        String sha1 = null;
        try {
            InputStream is = new FileInputStream(path);
            sha1 = DigestUtils.sha1Hex(is);
            System.out.println("Digest          = " + sha1);
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }


        return sha1;
    }

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

    static void createZipFileIntoObjectsFolder(Path repositoryPath, Path filePath, String sha1) {
        try {
            File file = new File(filePath.toString());
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

    static String colnvertLongToSimpleDatetTime(long i_Time) {
        Date date = new Date(i_Time);
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.mm.yyyy-hh:mm:ss:sss");
        String dateText = dateFormat.format(date);

        return dateText;
    }

    //input: c:\\..\\[repositoryName]\\[nameFile.txt]
    static String createSimpleFileDescription(Path repositoryPath, Path filePathOrigin) {
        String sha1 = getSha1(filePathOrigin.toString());
        createZipFileIntoObjectsFolder(repositoryPath, filePathOrigin, sha1);
        return sha1;
    }


    static String createFolderDescriptionFile(BlobData i_Blob, Path repositoryPath, Path folderPath, String userName) {
        File currentFolder = folderPath.toFile();
        String folderPathString = folderPath.toString() + "\\" + currentFolder.getName() + ".txt";
        String sha1String = "";
        String stringForSha1 = "";
        FileWriter outputFile = null;

        try {
            outputFile = new FileWriter(folderPathString);
            BufferedWriter bf = new BufferedWriter(outputFile);
            List<BlobData> blobList = i_Blob.getCurrentFolder().getBlobList();

            for (Path path : folderPath) {
                if (!path.equals(Paths.get(folderPath.toString() + "\\.magit")) && !path.equals(Paths.get(folderPathString))) {
                    File currentFileInFolder = path.toFile();
                    String type = currentFileInFolder.isFile() ? "file" : "folder";

                    for (BlobData blob : blobList) {
                        if (blob.getName().equals(currentFileInFolder.getName())) {
                            sha1String = blob.getSHA1();
                        }
                    }
                    String basicDataString = String.format(
                            "%s,%s,%s,%s",
                            currentFileInFolder.getName(),
                            type,
                            sha1String,
                            userName);

                    try {
                        bf.write(String.format(
                                "%s%s\n",
                                basicDataString,
                                colnvertLongToSimpleDatetTime(currentFileInFolder.lastModified())));

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    stringForSha1.concat(basicDataString);
                }
            }
            sha1String = DigestUtils.sha1Hex(stringForSha1);
            createZipFileIntoObjectsFolder(repositoryPath, Paths.get(folderPathString), sha1String);
            bf.close();
        } catch (IOException e) {

        }
        return sha1String;
    }
}
