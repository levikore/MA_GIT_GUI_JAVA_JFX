package logicpackage;

import java.util.List;

public class Commit {
    private String m_CurrentCommitSHA1;
    private List<Commit> m_PrevCommitsList;
    private String m_CommitComment;
    private String m_CreationDate;
    private String m_CreatedBy;
    private RootFolder m_RootFolder;

    public Commit(){

    }

    public Commit(RootFolder i_RootFolder, String i_CommitComment, String i_CreatedBy, List<Commit> i_PrevCommitsList, String i_Sha1, String i_CreationDate) {//commit 2
        m_RootFolder = i_RootFolder;
        m_PrevCommitsList=i_PrevCommitsList;
        m_CommitComment = i_CommitComment;
        m_CreatedBy = i_CreatedBy;
        m_CreationDate = i_CreationDate;
        m_CurrentCommitSHA1 = i_Sha1;
    }

    public void UpdateCommit(RootFolder i_RootFolder, String i_CommitComment, String i_CreatedBy, List<Commit> i_PrevCommitsList, String i_Sha1, String i_CreationDate)
    {
        m_RootFolder = i_RootFolder;
        m_PrevCommitsList=i_PrevCommitsList;
        m_CommitComment = i_CommitComment;
        m_CreatedBy = i_CreatedBy;
        m_CreationDate = i_CreationDate;
        m_CurrentCommitSHA1 = i_Sha1;
    }

    public String getRootSHA1() {
        return m_RootFolder.getSHA1();
    }

    public String getCurrentCommitSHA1() {
        return this.m_CurrentCommitSHA1;
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

    public List<Commit> getPrevCommitsList() {
        return m_PrevCommitsList;
    }

    public void setPrevCommitsList(List<Commit> i_PrevCommitsList) {
        this.m_PrevCommitsList = i_PrevCommitsList;
    }
//    public Commit getPrevCommit() {
//        return m_PrevCommit;
//    }

//    public void setPrevCommit(Commit i_PrevCommit) {
//        this.m_PrevCommit = i_PrevCommit;
//    }



    public String GetPreviousCommitsSHA1String() {
       String previousCommitsSHA1String = "";
      if(m_PrevCommitsList!=null) {
          for(Commit commit:m_PrevCommitsList)
          {
              previousCommitsSHA1String=previousCommitsSHA1String.concat(commit.getCurrentCommitSHA1());
          }

      }
//        Commit currentCommit = this.m_PrevCommit;
//
//        while (currentCommit != null) {
//            previousCommitsSHA1String = previousCommitsSHA1String.concat(currentCommit.getCurrentCommitSHA1() + ",");
//            currentCommit = currentCommit.getPrevCommit();
//        }
//
//        previousCommitsSHA1String = previousCommitsSHA1String.length() != 0 ? previousCommitsSHA1String.substring(0, previousCommitsSHA1String.length() - 1) : ""; //remove last comma from string

        return previousCommitsSHA1String;
    }

    public RootFolder getRootFolder() {
        return m_RootFolder;
    }

    public List<String> GetAllCommitFiles() {
        return m_RootFolder.GetAllFilesData();
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

}
