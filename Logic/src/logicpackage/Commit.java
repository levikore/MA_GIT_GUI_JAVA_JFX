package logicpackage;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class Commit {

    private String m_CurrentCommitSHA1;
    private Commit m_PrevCommit;
    private String m_CommitComment;
    private String m_CreationDate;
    private String m_CreatedBy;
    private RootFolder m_RootFolder;


    private String m_UserName;

    public Commit(RootFolder i_RootFolder, String i_CommitComment, String i_CreatedBy, Commit i_PrevCommit) {//commit 2
        this.m_RootFolder = i_RootFolder;
        this.m_PrevCommit = i_PrevCommit;
        i_RootFolder.UpdateCurrentRootFolderSha1(i_CreatedBy);
        m_UserName = i_CreatedBy;
        m_CommitComment=i_CommitComment;
        m_CreatedBy=i_CreatedBy;
    }

    public Commit(RootFolder i_RootFolder, String i_CommitComment, String i_CreatedBy) {
        this.m_RootFolder = i_RootFolder;
        this.m_PrevCommit = null;
        i_RootFolder.UpdateCurrentRootFolderSha1(i_CreatedBy);
        m_UserName = i_CreatedBy;
        m_CommitComment=i_CommitComment;
        m_CreatedBy=i_CreatedBy;
    }

    public String getRootSHA1() {
        return m_RootFolder.getSHA1();
    }

    public void setCurrentCommitSHA1(String i_CurrentCommitSHA1) {
        this.m_CurrentCommitSHA1 = i_CurrentCommitSHA1;
    }

    public void setCreationDate(String i_CreationDate) {
        this.m_CreationDate = i_CreationDate;
    }

    public String getCommitComment() {
        return m_CommitComment;
    }

    public String getCreationDate() {
        return m_CreationDate;
    }

    public String getCreatedBy() {
        return m_CreatedBy;
    }

    public String getRootFolderSha1() {
        return m_RootFolder.getSHA1();
    }

//    public void setRootFolderSha1(String i_RootFolderSha1) {
//        this.m_RootFolderSha1 = i_RootFolderSha1;
//    }

    public Path getRootFolderPath() {
        return m_RootFolder.getRootFolderPath();
    }


    public Commit getPrevCommit() {
        return m_PrevCommit;
    }

    public void setPrevCommit(Commit i_PrevCommit) {
        this.m_PrevCommit = i_PrevCommit;
    }

}
