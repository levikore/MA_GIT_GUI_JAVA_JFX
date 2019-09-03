package logicpackage;

public class Conflict {
    private UnCommittedChange m_OurFile;
    private UnCommittedChange m_TheirsFile;
    private BlobData m_Ancestor;

    public Conflict(UnCommittedChange m_OurFile, UnCommittedChange m_TheirsFile, BlobData m_Ancestor) {
        this.m_OurFile = m_OurFile;
        this.m_TheirsFile = m_TheirsFile;
        this.m_Ancestor = m_Ancestor;
    }

    public UnCommittedChange getOurFile() {
        return m_OurFile;
    }

    public UnCommittedChange getTheirsFile() {
        return m_TheirsFile;
    }

    public BlobData getAncestor() {
        return m_Ancestor;
    }

    public void setAncestor(BlobData i_Ancestor) {
        this.m_Ancestor = i_Ancestor;
    }

    @Override
    public String toString() {
        return
                "OurFile:" + m_OurFile.getFile().GetPath()+m_OurFile.getChangeType()+
                "\nTheirsFile:" + m_TheirsFile.getFile().GetPath() + m_TheirsFile.getChangeType();
    }
}
