import java.util.HashMap;
import java.lang.Object;
import org.apache.commons.codec.digest.DigestUtils;
import java.util.HashSet;
import java.util.Set;

public class Folder {

   private HashMap<Integer,BlobData> m_Files=new HashMap<>();


//    public Set<BlobData> getFiles() {
//        return m_Files;
//    }
//
//    //public getByName(String i_Name)
//

    public void addFileToFolder(BlobData i_BlobData) {
        m_Files.put(i_BlobData.getId(),i_BlobData);
}

    //sha1Hex(InputStream data)


}
