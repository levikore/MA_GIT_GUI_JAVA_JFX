import java.util.HashSet;
import java.util.Set;

public class Folder {
   private Set<BlobData> m_Files=new HashSet<>();

    public Set<BlobData> getFiles() {
        return m_Files;
    }

    //public getByName(String i_Name)

    public void addFileToFolder(BlobData i_BlobData) {
        m_Files.add(i_BlobData);
    }


}
