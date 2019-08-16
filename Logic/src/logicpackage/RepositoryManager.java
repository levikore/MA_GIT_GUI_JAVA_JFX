package logicpackage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class RepositoryManager {

    private String m_CurrentUserName;
    private String m_RepositoryName;
    private Path m_RepositoryPath;
    private RootFolder m_RootFolder;
    private HeadBranch m_HeadBranch;
    private Commit m_CurrentCommit;
    private Boolean isFirstCommit = true;
    private Path m_MagitPath;
    private List<Branch> m_AllBranchesList = new LinkedList<>();

    private final String c_GitFolderName = ".magit";
    private final String c_ObjectsFolderName = "objects";
    private final String c_BranchesFolderName = "branches";
    private final String c_TestFolderName = "test";

    public RepositoryManager(Path i_RepositoryPath, String i_CurrentUserName, boolean i_IsNewRepository) {
        m_RepositoryPath = i_RepositoryPath;
        m_RepositoryName = m_RepositoryPath.toFile().getName();
        m_CurrentUserName = i_CurrentUserName;
        m_MagitPath = Paths.get(m_RepositoryPath.toString() + "\\" + c_GitFolderName);
        if (i_IsNewRepository) {
            intializeRepository();
        } else {
            recoverRepositoryFromFiles();
        }
    }

    private void intializeRepository() {
        m_RootFolder = getInitializedRootFolder();
        createSystemFolders();
    }

    private void createSystemFolders() {
        FilesManagement.CreateFolder(m_RepositoryPath.getParent(), m_RepositoryName);
        FilesManagement.CreateFolder(m_RepositoryPath, c_GitFolderName);
        FilesManagement.CreateFolder(m_MagitPath, c_ObjectsFolderName);
        FilesManagement.CreateFolder(m_MagitPath, c_BranchesFolderName);
    }

    private void createNewCommit(String i_CommitComment/*, String i_NameBranch*/) {
        Commit newCommit = null;
        if (isFirstCommit) {
            newCommit = new Commit(m_RootFolder, i_CommitComment, m_CurrentUserName, null,"", "");
            isFirstCommit = false;
        } else {
            newCommit = new Commit(m_RootFolder, i_CommitComment, m_CurrentUserName, m_CurrentCommit,"", "");
        }

        m_CurrentCommit = newCommit;
        String sha1 = FilesManagement.CreateCommitDescriptionFile(m_CurrentCommit, m_RepositoryPath);
        m_CurrentCommit.setCurrentCommitSHA1(sha1);
        if (m_HeadBranch == null) {
            Branch branch = new Branch("master", m_CurrentCommit, m_RepositoryPath, true,"");
            m_AllBranchesList.add(branch);
            m_HeadBranch = new HeadBranch(branch, m_RepositoryPath, true, "");
        } else {
            m_HeadBranch.updateCurrentBranch(m_CurrentCommit);
        }
    }

    private RootFolder getInitializedRootFolder() {
        Folder rootFolder = new Folder();//new Folder(m_RepositoryPath.getParent(), m_RepositoryPath.toFile().getName());
        BlobData rootFolderBlobData = new BlobData(m_RepositoryPath, m_RepositoryPath.toFile().toString(), rootFolder);
        return new RootFolder(rootFolderBlobData, m_RepositoryPath);
    }

    public Boolean HandleCommit(String i_CommitComment) throws IOException {
        Boolean isCommitNecessary;

        if (isFirstCommit) {
            handleFirstCommit(i_CommitComment);
            isCommitNecessary = true;
        } else {
            isCommitNecessary = handleSecondCommit(i_CommitComment);
        }

        return isCommitNecessary;
    }

    public void HandleBranch(String i_BranchName) {
        Branch branch = new Branch(i_BranchName, m_HeadBranch.getBranch(), m_HeadBranch, m_RepositoryPath,true,"");
        m_AllBranchesList.add(branch);
    }

    public boolean removeBranch(String i_BranchName) {
        boolean returnValue = true;
        Branch branchToRemove = findBranchByName(i_BranchName);
        if (branchToRemove == m_HeadBranch.getBranch()) {
            returnValue = false;
        } else if (branchToRemove == null) {
            returnValue = false;
        } else {
            FilesManagement.removeFileByPath(branchToRemove.getBranchPath());
            FilesManagement.removeFileByPath(Paths.get(m_MagitPath.toString() + "\\" + c_ObjectsFolderName + "\\" + branchToRemove.getBranchSha1() + ".zip"));
            m_AllBranchesList.remove(branchToRemove);
            branchToRemove = null;
        }
        return returnValue;
    }

    private void handleFirstCommit(String i_CommitComment/*, String i_NameBranch*/) {
        m_RootFolder = getInitializedRootFolder();
        m_RootFolder.UpdateCurrentRootFolderSha1(m_CurrentUserName, "");
        createNewCommit(i_CommitComment);
    }

    public boolean handleCheckout(String i_BranchName) {
        Branch branchToCheckout = findBranchByName(i_BranchName);
        boolean retVal = false;
        if (branchToCheckout != null) {
            m_HeadBranch.checkout(branchToCheckout);
            retVal = true;
        }
        return retVal;
    }

    private Branch findBranchByName(String i_BranchName) {
        Branch branchToReturn = null;
        if (m_AllBranchesList != null) {
            branchToReturn = m_AllBranchesList.stream()
                    .filter(branch -> branch.getBranchName()
                            .equals(i_BranchName)).findFirst().get();
        }
        return branchToReturn;
    }

    private Boolean handleSecondCommit(String i_CommitComment) throws IOException {
        Boolean isCommitNecessary = false;
        //new Folder(m_MagitPath, c_TestFolderName);
        FilesManagement.CreateFolder(m_MagitPath, c_TestFolderName);
        RootFolder testRootFolder = getInitializedRootFolder();
        testRootFolder.UpdateCurrentRootFolderSha1(m_CurrentUserName, c_TestFolderName);

        if (!testRootFolder.getSHA1().equals(m_RootFolder.getSHA1())) {
            copyFiles(m_MagitPath + "\\" + c_TestFolderName, m_MagitPath + "\\" + c_ObjectsFolderName);
            m_RootFolder = testRootFolder;
            createNewCommit(i_CommitComment);
            isCommitNecessary = true;
        }

        ClearDirectory((Paths.get(m_MagitPath.toString() + "\\" + c_TestFolderName).toFile()));
        //Paths.get(m_MagitPath.toString() +"\\"+ c_TestFolderName).toFile().delete();

        return isCommitNecessary;
    }

    private void ClearDirectory(File i_directory) {
        File[] fileList = i_directory.listFiles();

        for (File file : fileList) {
            file.delete();
        }

        i_directory.delete();
    }

    private void copyFiles(String from, String to) throws IOException {
        Path source = Paths.get(from);
        Path destination = Paths.get(to);

        File[] fileList = source.toFile().listFiles();

        for (File file : fileList) {
            //Files.copy(Paths.get(file.toString()), destination);
            if (file.renameTo
                    (new File(destination + "\\" + file.getName()))) {
                // if file copied successfully then delete the original file
                file.delete();
                System.out.println("File moved successfully");
            }
        }
    }

    public HeadBranch getHeadBranch() {
        return m_HeadBranch;
    }

    public List<Branch> getAllBranchesList() {
        return m_AllBranchesList;
    }

    public Commit recoverCommit(String i_BranchSha1) {
        List<String> commitsHistoryList = FilesManagement.commitsHistoryList(i_BranchSha1, m_RepositoryPath.toString());
        List<String> commitLines = null;
        if(commitsHistoryList!=null) {
            Collections.reverse(commitsHistoryList);
        }
        Commit commit = null;
        Commit prevCommit = null;

        for (String sha1 : commitsHistoryList) {
            //commitLinesFormat:
            //             1)rootFoldersha1
            //             2)commitComment
            //             3)time
            //             4)userName
            commitLines = FilesManagement.GetCommitData(sha1, m_RepositoryPath.toString());
            String rootFolderSha1 = commitLines.get(0);
            String commitComment = commitLines.get(1);
            String time = commitLines.get(2);
            String userName = commitLines.get(3);

            Folder currentRootFolder = new Folder(rootFolderSha1);
            BlobData rootFolderBlobData = new BlobData(m_RepositoryPath, m_RepositoryPath.toFile().toString(),userName,time,true,rootFolderSha1, currentRootFolder);
            RecoverRootFolder(rootFolderBlobData);
            RootFolder rootFolder=new RootFolder(rootFolderBlobData,m_RepositoryPath);

            commit = new Commit(rootFolder, commitComment, userName, prevCommit, sha1, time );
            if (prevCommit == null) {
                prevCommit = commit;

            }
        }
        return commit;
    }

    private  void RecoverRootFolder( BlobData i_Root ) {
       List<String> lines= FilesManagement.getDataFilesList(m_RepositoryPath.toString(),i_Root.getSHA1());
       List<String> fileDataList=null;

       for(String fileData:lines)
        {
           if(!fileData.equals("")) {
               fileDataList = FilesManagement.ConvertCommaSeparatedStringToList(fileData);
               if (fileDataList.get(1).equals("file")) {
                   BlobData blob = new BlobData(m_RepositoryPath, i_Root.getPath() + "\\" + fileDataList.get(0), fileDataList.get(3), fileDataList.get(4), false, fileDataList.get(2), null);
                   i_Root.getCurrentFolder().addBlobToList(blob);
               } else {
                   Folder currentRootFolder = new Folder(fileDataList.get(2));
                   BlobData blob = new BlobData(m_RepositoryPath, i_Root.getPath() + "\\" + fileDataList.get(0), fileDataList.get(3), fileDataList.get(4), false, fileDataList.get(2), currentRootFolder);
                   i_Root.getCurrentFolder().addBlobToList(blob);
                   RecoverRootFolder(blob);
               }
           }
        }
    }

    private void recoverRepositoryFromFiles() {
        List<String> branchesList = FilesManagement.getBranchesList(m_RepositoryPath.toString());
       // List<String> headBranchData = FilesManagement.ConvertCommaSeparatedStringToList(branchesList.get(0));

        String headBranchContent = FilesManagement.getHeadBranchSha1(m_RepositoryPath.toString());
        String BranchDataOfHeadBranch=FilesManagement.GetFileNameInZip(headBranchContent,m_RepositoryPath.toString());

        for (String sha1AndName : branchesList) {
            List<String> data = FilesManagement.ConvertCommaSeparatedStringToList(sha1AndName);
            String nameBranch = data.get(0);
            String currentBranchSha1 = data.get(1);

                Branch branch=null;
                Commit commit = recoverCommit(currentBranchSha1);
                branch = new Branch(nameBranch, commit, m_RepositoryPath,false,currentBranchSha1);
               m_AllBranchesList.add(branch);
                if (BranchDataOfHeadBranch.equals(nameBranch)) {
                    m_HeadBranch = new HeadBranch(branch, m_RepositoryPath, false,currentBranchSha1 );
                    m_RootFolder = m_HeadBranch.getHeadBranch().getCurrentCommit().getRootFolder();
                    m_CurrentCommit=commit;
                }
        }
    }

    public List<String> GetHeadBranchCommitHistory(){
        List<String> commitStringList = new LinkedList<>();
        Commit currentCommit = m_HeadBranch.getBranch().getCurrentCommit();
        setHeadBranchCommitHistoryRec(commitStringList, currentCommit);
        return commitStringList;
    }

    private void setHeadBranchCommitHistoryRec(List<String> i_CommitStringList, Commit i_CurrentCommit){
        i_CommitStringList.add(i_CurrentCommit.toString());
       if(i_CurrentCommit.getPrevCommit()!=null){
           setHeadBranchCommitHistoryRec(i_CommitStringList, i_CurrentCommit.getPrevCommit());
       }
    }
}
