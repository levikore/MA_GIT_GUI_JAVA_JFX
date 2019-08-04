package logicpackage;

import org.apache.commons.codec.digest.DigestUtils;

import java.io.*;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.Date;
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

    static void CreateFolder(Path path, String name) {
        Path newDirectoryPath = Paths.get(path.toString() + "/" + name);

        File directory = new File(newDirectoryPath.toString());
        if (!directory.exists()) {
            directory.mkdir();
        }
    }

    static void createZipFile(Path filePath) {
        try {
            File file = new File(filePath.toString());
            String sha1 = getSha1(filePath.toString());

            String zipFileName = sha1.concat(".zip");
            FileOutputStream fos = new FileOutputStream(filePath.getParent().toString() +"\\.magit\\objects\\" + zipFileName);
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

    static void createFolderDescriptionFile(Path folderPath, String userName) {
        File currentFolder = folderPath.toFile();
        String folderPathString  = folderPath.toString() + "\\" + currentFolder.getName();

        try {

            FileWriter outputFile = new FileWriter(folderPathString);
            BufferedWriter bf = new BufferedWriter(outputFile);

            Files.list(folderPath).filter(name -> (!name.equals(Paths.get(folderPath.toString() + "\\.magit"))))
                    .filter(name -> (!name.equals(Paths.get(folderPathString))))
                    .forEach((line) -> {
                        try {
                            File currentFileInFolder = line.toFile();
                            String sha1 = getSha1(line.toString());
                            String type = currentFileInFolder.isFile() ? "file" : "folder";
                            bf.write(currentFileInFolder.getName() + ',' + type + ',' + sha1 + ',' + userName + ',' +
                                    colnvertLongToSimpleDatetTime(currentFileInFolder.lastModified() + '\n'));
                        } catch (IOException ex) {
                            System.err.println("createFolderDescriptionFile: I/O error: " + ex);
                        }
                    });
            bf.close();
            createZipFile(Paths.get(folderPathString));

        } catch (FileNotFoundException ex) {
            System.err.format("createFolderDescriptionFile: The folder %s does not exist", folderPath.toString());
        } catch (IOException ex) {
            System.err.println("createFolderDescriptionFile: I/O error: " + ex);
        } finally {
            try {
                Files.deleteIfExists(Paths.get(folderPathString));
            } catch (IOException ex) {
                System.err.println("createFolderDescriptionFile(in finally): I/O error: " + ex);
            }
        }
    }


//    static void createFileToDirectory() {
//        Path resourcesPath = getResourcesPath();
//        Path newDirectoryPath = Paths.get(resourcesPath + "/dir1");
//        Path filePath = Paths.get(newDirectoryPath + "/TEXT1.txt");
//
//
//        File directory = new File(newDirectoryPath.toString());
//        if (!directory.exists()) {
//            directory.mkdir();
//            // If you require it to make the entire directory path including parents,
//            // use directory.mkdirs(); here instead.
//        }
//
//        File file = new File(filePath.toString());
//
//        //whenWriteStringUsingBufferedWritter_thenCorrect(filePath,"hi!! ~~~~~~~");
////
////
////
////
////         try{
////             FileWriter fw = new FileWriter(file.getAbsoluteFile());
////             BufferedWriter bw = new BufferedWriter(fw);
////             bw.write(value);
////             bw.close();
////         }
////         catch (IOException e){
////             e.printStackTrace();
////             System.exit(-1);
////         }
//    }
//
////    static void whenWriteStringUsingBufferedWritter_thenCorrect(Path fileName, String text )
////            throws IOException {
////        String str = text;
////        //BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));
////        writer.write(str);
////
////        writer.close();
////    }

}
