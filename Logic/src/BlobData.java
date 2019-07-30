import java.text.SimpleDateFormat;
import java.util.Objects;

public class BlobData {
    private String m_Name;
    private String m_SHA1;
    private String m_Type;
    private String m_LastChangedBY;
    private SimpleDateFormat m_LastChangedTime;
    private int m_HashCode;

    public BlobData(
            String i_Name,
            String i_SHA1,
            String i_Type,
            String i_LastChangedBY,
            SimpleDateFormat i_LastChangedTime
    ) {
        this.m_Name = i_Name;
        this.m_SHA1 = i_SHA1;
        this.m_Type = i_Type;
        this.m_LastChangedBY = i_LastChangedBY;
        this.m_LastChangedTime = i_LastChangedTime;
    }

    public String getM_Name() {
        return m_Name;
    }

    public void setName(String m_Name) {
        this.m_Name = m_Name;
    }

    public String getSHA1() {
        return m_SHA1;
    }

    public void setSHA1(String m_SHA1) {
        this.m_SHA1 = m_SHA1;
    }

    public String getType() {
        return m_Type;
    }

    public void setType(String m_Type) {
        this.m_Type = m_Type;
    }

    public String getLastChangedBY() {
        return m_LastChangedBY;
    }

    public void setLastChangedBY(String m_LastChangedBY) {
        this.m_LastChangedBY = m_LastChangedBY;
    }

    public SimpleDateFormat getM_LastChangedTime() {
        return m_LastChangedTime;
    }

    public void setLastChangedTime(SimpleDateFormat m_LastChangedTime) {
        this.m_LastChangedTime = m_LastChangedTime;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BlobData blobData = (BlobData) o;
        return m_Name.equals(blobData.m_Name) &&
                m_SHA1.equals(blobData.m_SHA1) &&
                m_Type.equals(blobData.m_Type) &&
                m_LastChangedBY.equals(blobData.m_LastChangedBY) &&
                m_LastChangedTime.equals(blobData.m_LastChangedTime);
    }

    @Override
    public int hashCode() {
        if(m_HashCode==0)
        {
            m_HashCode= Objects.hash(m_Name, m_SHA1, m_Type, m_LastChangedBY, m_LastChangedTime);
        }
        return m_HashCode;
    }

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
