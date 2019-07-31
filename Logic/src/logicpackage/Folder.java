package logicpackage;

import java.util.HashMap;

public class Folder {

   private HashMap<Integer,BlobData> m_Files=new HashMap<>();


//    public Set<BlobData> getFiles() {
//        return m_Files;
//    }
//
//    //public getByName(String i_Name)
//

    public void addFileToFolder(BlobData i_BlobData) {

        filesManagement.createFileToDirectory();
        m_Files.put(i_BlobData.getId(),i_BlobData);

}

    //sha1Hex(InputStream data)



}
