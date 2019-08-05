package logicpackage;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.Comparator;
import java.util.stream.Collectors;


public class Folder {

 private List<BlobData> blobList =new LinkedList<>();
 private Path m_Path;
 private String m_FolderSha1;

         public Folder(Path path, String name){

            IFilesManagement.CreateFolder(path, name);

           //IFilesManagement.createFolderDescriptionFile(Paths.get(path+"\\"+name),"yair");
             //IFilesManagement.createFolderDescriptionFile(path,"yair");


         }

         public void addBlobToList(BlobData blobData){
             blobList.add(blobData);
             blobList.stream()
                     .sorted(Comparator.comparing(BlobData::getName))
                     .collect(Collectors.toList());
         }

}
