package logicpackage;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.Comparator;
import java.util.stream.Collectors;


public class Folder {

 private List<BlobData> blobList =new LinkedList<>();

         public Folder(Path path, String name){

              IFilesManagement.CreateFolder(path, name);
         }

         public void addBlobToList(BlobData blobData){
             blobList.add(blobData);
             blobList.stream()
                     .sorted(Comparator.comparing(BlobData::getName))
                     .collect(Collectors.toList());
         }

}
