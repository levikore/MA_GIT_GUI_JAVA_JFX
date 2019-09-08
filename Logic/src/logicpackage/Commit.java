package logicpackage;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class Commit {
    private String m_CurrentCommitSHA1;
    private List<Commit> m_PrevCommitsList;
    private String m_CommitComment;
    private String m_CreationDate;
    private String m_CreatedBy;
    private RootFolder m_RootFolder;

    public Commit() {

    }

    public Commit(RootFolder i_RootFolder, String i_CommitComment, String i_CreatedBy, List<Commit> i_PrevCommitsList, String i_Sha1, String i_CreationDate) {//commit 2
        m_RootFolder = i_RootFolder;
        m_PrevCommitsList = i_PrevCommitsList;
        m_CommitComment = i_CommitComment;
        m_CreatedBy = i_CreatedBy;
        m_CreationDate = i_CreationDate;
        m_CurrentCommitSHA1 = i_Sha1;
    }

    public void UpdateCommit(RootFolder i_RootFolder, String i_CommitComment, String i_CreatedBy, List<Commit> i_PrevCommitsList, String i_Sha1, String i_CreationDate) {
        m_RootFolder = i_RootFolder;
        m_PrevCommitsList = i_PrevCommitsList;
        m_CommitComment = i_CommitComment;
        m_CreatedBy = i_CreatedBy;
        m_CreationDate = i_CreationDate;
        m_CurrentCommitSHA1 = i_Sha1;
    }

    public String GetRootSHA1() {
        return m_RootFolder.GetSHA1();
    }

    public String GetCurrentCommitSHA1() {
        return this.m_CurrentCommitSHA1;
    }

    public void SetCurrentCommitSHA1(String i_CurrentCommitSHA1) {
        this.m_CurrentCommitSHA1 = i_CurrentCommitSHA1;
    }

    public void SetCreationDate(String i_CreationDate) {
        this.m_CreationDate = i_CreationDate;
    }

    public String GetCommitComment() {
        return m_CommitComment;
    }

    public String GetCreationDate() {
        return m_CreationDate;
    }

    public String GetCreatedBy() {
        return m_CreatedBy;
    }

    public String GetRootFolderSha1() {
        return m_RootFolder.GetSHA1();
    }

    public List<Commit> GetPrevCommitsList() {
        return m_PrevCommitsList;
    }

    public void SetPrevCommitsList(List<Commit> i_PrevCommitsList) {
        this.m_PrevCommitsList = i_PrevCommitsList;
    }

    public String GetPreviousCommitsSHA1String() {
        String previousCommitsSHA1String = "";

        if (m_PrevCommitsList != null) {
            for (Commit commit : m_PrevCommitsList) {
                previousCommitsSHA1String = previousCommitsSHA1String.concat(commit.GetCurrentCommitSHA1() + ",");
            }
        }

        previousCommitsSHA1String = previousCommitsSHA1String.length() != 0 ? previousCommitsSHA1String.substring(0, previousCommitsSHA1String.length() - 1) : ""; //remove last comma from string

        return previousCommitsSHA1String;
    }

    public RootFolder GetCommitRootFolder() {
        return m_RootFolder;
    }

    public long GetCreationDateInMilliseconds(){
        long milliseconds = 0;
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy-HH:mm:ss:sss");
        try {
            Date d = dateFormat.parse(m_CreationDate);
            milliseconds = d.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return milliseconds;
    }

    @Override
    public String toString() {
        return
                "SHA1: " + m_CurrentCommitSHA1 + '\n' +
                        "Commit Comment: " + m_CommitComment + '\n' +
                        "Date Created: " + m_CreationDate + '\n' +
                        "Created by: " + m_CreatedBy + '\n' +
                        '\n';
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Commit) {
            return ((Commit) obj).GetCurrentCommitSHA1().equals(m_CurrentCommitSHA1);
        }

        return false;
    }
}
