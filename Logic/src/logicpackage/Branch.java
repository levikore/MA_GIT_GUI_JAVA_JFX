package logicpackage;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class Branch {
    private Commit m_CurrentCommit;
    private String m_BranchName;
    private String m_BranchSha1;
    //private Branch m_ParntBranch;
    private boolean m_IsHeadBranch;
    private Path m_RepositoryPath;
    private Path m_BranchPath;


    public Branch(String i_BranchName, Commit i_Commit, Path i_RepositoryPath)//for the first Branch in the git.
    {
        m_RepositoryPath = i_RepositoryPath;
        m_IsHeadBranch = true;
        m_BranchName = i_BranchName;
        m_CurrentCommit = i_Commit;
       // m_ParntBranch = null;
        m_BranchSha1 = FilesManagement.CreateBranchFile(i_BranchName, i_Commit, i_RepositoryPath);
        m_BranchPath=Paths.get(m_RepositoryPath+"\\.magit\\branches\\"+i_BranchName+".txt");
    }

    public Branch(String i_BranchName, Branch i_ParentBranch,HeadBranch i_HeadBranch, Path i_RepositoryPath) {
        m_RepositoryPath = i_RepositoryPath;
        m_IsHeadBranch = false;
        m_BranchName = i_BranchName;
        m_CurrentCommit = i_ParentBranch.m_CurrentCommit;
        //m_ParntBranch = i_ParentBranch;
        m_BranchSha1 = FilesManagement.CreateBranchFile(i_BranchName, i_ParentBranch.m_CurrentCommit, i_RepositoryPath);
        m_BranchPath=Paths.get(m_RepositoryPath+"\\.magit\\branches\\"+i_BranchName+".txt");
    }

    public void UpdateBranchCommit(Commit newCommit) {
        m_BranchSha1 =  FilesManagement.UpdateBranchFile(this, newCommit, m_RepositoryPath);
        m_CurrentCommit=newCommit;
    }


    public String getBranchName() {
        return m_BranchName;
    }

    public void setBranchName(String i_BranchName) {
        this.m_BranchName = i_BranchName;
    }

    public String getBranchSha1() {
        return m_BranchSha1;
    }

    public Commit getCurrentCommit() {
        return m_CurrentCommit;
    }

    public Path getBranchPath() {
        return m_BranchPath;
    }

    public void recoverCommit(String repositoryPath)
    {
List<String> commitsHistoryList=FilesManagement.commitsHistoryList( m_BranchSha1,repositoryPath);
Collections.reverse(commitsHistoryList);
Commit commit=null;
for(String sha1: commitsHistoryList)
{
   String rootFolderSha1= FilesManagement.getRootFolderSha1ByCommitSha1(sha1);

}
    }



}
