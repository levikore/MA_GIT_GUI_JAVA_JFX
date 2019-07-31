package logicpackage;

import java.io.*;
import java.nio.file.*;


public interface filesManagement {

    static Path getResourcesPath() {
        return Paths.get(System.getProperty("user.dir") + "/resources");
    }

    static void createFileToDirectory() {
        Path resourcesPath = getResourcesPath();
        Path newDirectoryPath = Paths.get(resourcesPath + "/dir1");
        Path filePath = Paths.get(newDirectoryPath + "/TEXT1.txt");


        File directory = new File(newDirectoryPath.toString());
        if (!directory.exists()) {
            directory.mkdir();
            // If you require it to make the entire directory path including parents,
            // use directory.mkdirs(); here instead.
        }

        File file = new File(filePath.toString());

        //whenWriteStringUsingBufferedWritter_thenCorrect(filePath,"hi!! ~~~~~~~");
//
//
//
//
//         try{
//             FileWriter fw = new FileWriter(file.getAbsoluteFile());
//             BufferedWriter bw = new BufferedWriter(fw);
//             bw.write(value);
//             bw.close();
//         }
//         catch (IOException e){
//             e.printStackTrace();
//             System.exit(-1);
//         }
    }

//    static void whenWriteStringUsingBufferedWritter_thenCorrect(Path fileName, String text )
//            throws IOException {
//        String str = text;
//        //BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));
//        writer.write(str);
//
//        writer.close();
//    }

}
