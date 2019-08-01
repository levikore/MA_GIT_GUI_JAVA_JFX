package logicpackage;

import org.apache.commons.codec.digest.DigestUtils;

import java.io.*;
import java.nio.file.*;
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

    static void createZipFile(String filePath) {
        try {
            File file = new File(filePath);
            String sha1 = getSha1(filePath);

            String zipFileName = sha1.concat(".zip");
            FileOutputStream fos = new FileOutputStream("C:\\test\\repository\\.magit\\objects\\"+zipFileName);
            ZipOutputStream zos = new ZipOutputStream(fos);

            zos.putNextEntry(new ZipEntry(file.getName()));

            byte[] bytes = Files.readAllBytes(Paths.get(filePath));
            zos.write(bytes, 0, bytes.length);
            zos.closeEntry();
            zos.close();

        } catch (FileNotFoundException ex) {
            System.err.format("The file %s does not exist", filePath);
        } catch (IOException ex) {
            System.err.println("I/O error: " + ex);
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
