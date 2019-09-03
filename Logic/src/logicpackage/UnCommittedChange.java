package logicpackage;

public class UnCommittedChange {
    BlobData m_File;
    String m_ChangeType;//deleted, updated, added

    public UnCommittedChange(BlobData m_File, String m_ChangeType) {
        this.m_File = m_File;
        this.m_ChangeType = m_ChangeType;
    }

    public BlobData getFile() {
        return m_File;
    }

    public String getChangeType() {
        return m_ChangeType;
    }
}
