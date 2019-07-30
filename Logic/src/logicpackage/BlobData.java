package logicpackage;

import java.text.SimpleDateFormat;

public class BlobData {
    private String m_Name;
    private String m_SHA1;
    private String m_Type;
    private Integer m_Id;
    private String m_LastChangedBY;
    private SimpleDateFormat m_LastChangedTime;


    public BlobData(
            String i_Name,
            String i_SHA1,
            String i_Type,
            String i_LastChangedBY,
            String i_Id,
            SimpleDateFormat i_LastChangedTime
    ) {
        m_Name = i_Name;
        m_SHA1 = i_SHA1;
        m_Type = i_Type;
        m_LastChangedBY = i_LastChangedBY;
        m_LastChangedTime = i_LastChangedTime;
        m_Id = Integer.parseInt(i_Id);

    }

    public Integer getId() {
        return m_Id;
    }

    public String getName() {
        return m_Name;
    }

    public String getSHA1() {
        return m_SHA1;
    }

    public String getType() {
        return m_Type;
    }

    public String getLastChangedBY() {
        return m_LastChangedBY;
    }

    public SimpleDateFormat getLastChangedTime() {
        return m_LastChangedTime;
    }



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
                ", Type='" + m_Type + '\'' +
                ", LastChangedBY='" + m_LastChangedBY + '\'' +
                ", LastChangedTime=" + m_LastChangedTime +
                '}';
    }
}
