package logicpackage;

import java.text.SimpleDateFormat;
import java.util.LinkedList;
import java.util.List;

public class BlobData {
    private String m_Name;
    private String m_SHA1;
    private boolean m_IsFolder;
    // private String m_LastChangedBY;
   // private SimpleDateFormat m_LastChangedTime;
    private Folder m_CurrentFolder;

    public BlobData(
            String i_Name,
            String i_SHA1,
           Boolean i_IsFolder
           // String i_LastChangedBY,
           // SimpleDateFormat i_LastChangedTime
    ) {
        m_Name = i_Name;
        m_SHA1 = i_SHA1;
        m_IsFolder = i_IsFolder;
     //   m_LastChangedBY = i_LastChangedBY;
      //  m_LastChangedTime = i_LastChangedTime;
    }

    public BlobData(
            String i_Name,
            Folder i_CurrentFolder
            // String i_LastChangedBY,
            // SimpleDateFormat i_LastChangedTime
    ) {
        m_Name = i_Name;
        m_IsFolder = true;
        m_CurrentFolder=i_CurrentFolder;
        //   m_LastChangedBY = i_LastChangedBY;
        //  m_LastChangedTime = i_LastChangedTime;
    }


    public Folder getCurrentFolder() {
        return m_CurrentFolder;
    }

    public boolean GetIsFolder() {
        return m_IsFolder;
    }


    public String getName() {
        return m_Name;
    }

    public String getSHA1() {
        return m_SHA1;
    }

    public void setSHA1(String i_SHA1) {
        this.m_SHA1 = i_SHA1;
    }



    public Boolean getType() {
        return m_IsFolder;
    }
//
//    public String getLastChangedBY() {
//        return m_LastChangedBY;
//    }
//
//    public SimpleDateFormat getLastChangedTime() {
//        return m_LastChangedTime;
//    }


//    @Override
//    public boolean equals(Object o) {
//        if (this == o) return true;
//        if (o == null || getClass() != o.getClass()) return false;
//        BlobData blobData = (BlobData) o;
//        return m_Name.equals(blobData.m_Name) &&
//                m_SHA1.equals(blobData.m_SHA1) &&
//                m_Type.equals(blobData.m_Type) &&
//                m_LastChangedBY.equals(blobData.m_LastChangedBY) &&
//                m_LastChangedTime.equals(blobData.m_LastChangedTime);
//    }

//    @Override
//    public int hashCode() {
//        if(m_HashCode==0)
//        {
//            m_HashCode= Objects.hash(m_Name, m_SHA1, m_Type, m_LastChangedBY, m_LastChangedTime);
//        }
//        return m_HashCode;
//    }
//
//    public String

    @Override
    public String toString() {
        return "BlobData{" +
                " Name='" + m_Name + '\'' +
                ", SHA1='" + m_SHA1 + '\'' +
                ", Type='" + m_IsFolder + '\'' +
                ", LastChangedBY='" + //m_LastChangedBY + '\'' +
                ", LastChangedTime=" + //m_LastChangedTime +
                '}';
    }
}
