package logicpackage;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

public class RepositoryManager {
    private String m_CurrentUserName;
    private String m_RepositoryName;
    private Path m_RepositoryPath;
    private RootFolder m_RootFolder;
    private HeadBranch m_HeadBranch;
    private Commit m_CurrentCommit;
    private Boolean m_IsFirstCommit = true;
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
            m_IsFirstCommit = false;
            recoverRepositoryFromFiles();
        }
    }

    public Path GetRepositoryPath(){
        return m_RepositoryPath;
    }

    public String GetCurrentUserName(){
        return m_CurrentUserName;
    }

    public void SetCurrentUserName(String i_CurrentUserName) {
        this.m_CurrentUserName = i_CurrentUserName;
    }

    private void intializeRepository() {
        m_RootFolder = getInitializedRootFolder(m_CurrentUserName);
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
        if (m_IsFirstCommit) {
            newCommit = new Commit(m_RootFolder, i_CommitComment, m_CurrentUserName, null, "", "");
            m_IsFirstCommit = false;
        } else {
            newCommit = new Commit(m_RootFolder, i_CommitComment, m_CurrentUserName, m_CurrentCommit, "", "");
        }

        m_CurrentCommit = newCommit;
        String sha1 = FilesManagement.CreateCommitDescriptionFile(m_CurrentCommit, m_RepositoryPath, false);
        m_CurrentCommit.setCurrentCommitSHA1(sha1);
        if (m_HeadBranch == null) {
            Branch branch = new Branch("master", m_CurrentCommit, m_RepositoryPath, true, "");
            m_AllBranchesList.add(branch);
            m_HeadBranch = new HeadBranch(branch, m_RepositoryPath, true, "");
        } else {
            m_HeadBranch.updateCurrentBranch(m_CurrentCommit);
        }
    }

    private RootFolder getInitializedRootFolder(String i_UserName) {
        Folder rootFolder = new Folder();//new Folder(m_RepositoryPath.getParent(), m_RepositoryPath.toFile().getName());
        BlobData rootFolderBlobData = new BlobData(m_RepositoryPath, m_RepositoryPath.toFile().toString(), rootFolder, i_UserName );
        return new RootFolder(rootFolderBlobData, m_RepositoryPath);
    }

    public Boolean HandleCommit(String i_CommitComment) throws IOException {
        Boolean isCommitNecessary;

        if (m_IsFirstCommit) {
            handleFirstCommit(i_CommitComment);
            isCommitNecessary = true;
        } else {
            isCommitNecessary = handleSecondCommit(i_CommitComment);
        }

        return isCommitNecessary;
    }

    public void HandleBranch(String i_BranchName) {
        Branch branch = new Branch(i_BranchName, m_HeadBranch.getBranch(), m_HeadBranch, m_RepositoryPath, true, "");
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
        m_RootFolder = getInitializedRootFolder(m_CurrentUserName);
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

    public List<String> GetListOfUnCommitedFiles() {
        RootFolder testRootFolder = createFolderWithZipsOfUnCommitedFiles();
        String testFolderPath = m_MagitPath + "\\" + c_TestFolderName;
        File testRootFolderFile = Paths.get(testFolderPath).toFile();
        File[] filesList = null;
        if (testRootFolderFile.exists()) {
            filesList = testRootFolderFile.listFiles();
        }
        List<String> unCommittedFilesList = new LinkedList<>();
        if (!testRootFolder.getSHA1().equals(m_RootFolder.getSHA1())) {
            getAllUncommittedFiles(testRootFolder, unCommittedFilesList);
        }
        try {
            FileUtils.deleteDirectory((Paths.get(testFolderPath).toFile()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return unCommittedFilesList;
    }


    private void getAllUncommittedFiles(RootFolder io_TestRootFolder, List<String> io_UnCommittedFilesList) {
        Comparator<BlobData> pathComparator
                = Comparator.comparing(BlobData::getPath);
        addUncommittedBlobToListRecursively(pathComparator,
                m_RootFolder.getRootFolder().getCurrentFolder(),
                io_TestRootFolder.getRootFolder().getCurrentFolder(),
                io_UnCommittedFilesList);
    }


    private void addUncommittedBlobToListRecursively(Comparator<BlobData> i_PathComparator, Folder i_Folder, Folder i_TestFolder, List<String> io_UnCommittedFilesList) {

        List<BlobData> currentBlobList = i_Folder.getBlobList();
        List<BlobData> testBlobList = i_TestFolder.getBlobList();
        int savedJ = 0;
        int savedI = 0;
        int j = 0;

        for (int i = 0; i < currentBlobList.size(); i++) {
            System.out.println("i:" + i);
            BlobData blob = currentBlobList.get(i);
            if (savedJ > j) {
                j = savedJ;
            }
            while (j < testBlobList.size()) {
                BlobData testBlob = testBlobList.get(j);
                if (!blob.getPath().equals(testBlob.getPath()) && isPath1FolowsPath2(blob.getPath(), testBlob.getPath())) {
                    handleUncommittedNewFile(testBlob, io_UnCommittedFilesList);
                } else if (blob.getPath().equals(testBlob.getPath())) {
                    if (!blob.getSHA1().equals(testBlob.getSHA1())) {
                        if (testBlob.GetIsFolder()) {
                            addUncommittedBlobToListRecursively(i_PathComparator, blob.getCurrentFolder(), testBlob.getCurrentFolder(), io_UnCommittedFilesList);
                        } else {
                            io_UnCommittedFilesList.add(testBlob.getPath());
                        }
                    }
                    j++;
                    savedJ = j;
                    break;
                }
                if (!isPath1FolowsPath2(blob.getPath(), testBlob.getPath())) {
                    handleUncommittedNewFile(blob, io_UnCommittedFilesList);
                    savedJ = j;
                    break;
                }
                j++;
            }
            savedI = i;
            if (savedJ == testBlobList.size()) {
                break;
            }
        }

        for (int i = savedI+1; i < currentBlobList.size(); i++) {
            BlobData blob = currentBlobList.get(i);
            io_UnCommittedFilesList.add(blob.getPath());
            if (blob.GetIsFolder()) {
                addUncommittedFolderToList(blob.getCurrentFolder(), io_UnCommittedFilesList);
            }
        }

        for (j = savedJ; j < testBlobList.size(); j++) {
            BlobData testBlob = testBlobList.get(j);
            io_UnCommittedFilesList.add(testBlob.getPath());
            if (testBlob.GetIsFolder()) {
                addUncommittedFolderToList(testBlob.getCurrentFolder(), io_UnCommittedFilesList);
            }
        }

    }


    private void addUncommittedFolderToList(Folder folder, List<String> list) {
        List<BlobData> blobDataList = folder.getBlobList();

        for (BlobData blob : blobDataList) {
            list.add(blob.getPath());
            if (blob.GetIsFolder()) {
                addUncommittedFolderToList(blob.getCurrentFolder(), list);
            }
        }

    }

    private void handleUncommittedNewFile(BlobData testBlob, List<String> io_UnCommittedFilesList) {
        io_UnCommittedFilesList.add(testBlob.getPath());
        if (testBlob.GetIsFolder()) {
            addUncommittedFolderToList(testBlob.getCurrentFolder(), io_UnCommittedFilesList);
        }
    }


    private boolean isPath1FolowsPath2(String path1, String path2) {
        boolean retVal = false;
        if (path1.compareTo(path2) > 0) {
            retVal = true;
        } else if (path1.compareTo(path2) < 0) {
            retVal = false;
        }
        return retVal;
    }


    private RootFolder createFolderWithZipsOfUnCommitedFiles() {//*****
        Boolean isCommitNecessary = false;
        //new Folder(m_MagitPath, c_TestFolderName);
        FilesManagement.CreateFolder(m_MagitPath, c_TestFolderName);
        RootFolder testRootFolder = getInitializedRootFolder(m_CurrentUserName);
        testRootFolder.UpdateCurrentRootFolderSha1(m_CurrentUserName, c_TestFolderName);
        return testRootFolder;
    }

    private Boolean handleSecondCommit(String i_CommitComment) throws IOException {
        //*****
        Boolean isCommitNecessary = false;
        //new Folder(m_MagitPath, c_TestFolderName);
        FilesManagement.CreateFolder(m_MagitPath, c_TestFolderName);
        RootFolder testRootFolder = getInitializedRootFolder(m_CurrentUserName);
        testRootFolder.UpdateCurrentRootFolderSha1(m_CurrentUserName, c_TestFolderName);
        //*****
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
        if (commitsHistoryList != null) {
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
            BlobData rootFolderBlobData = new BlobData(m_RepositoryPath, m_RepositoryPath.toFile().toString(), userName, time, true, rootFolderSha1, currentRootFolder);
            RecoverRootFolder(rootFolderBlobData);
            RootFolder rootFolder = new RootFolder(rootFolderBlobData, m_RepositoryPath);
            commit = new Commit(rootFolder, commitComment, userName, prevCommit, sha1, time);
            if (prevCommit == null) {
                prevCommit = commit;

            }
        }
        return commit;
    }

    private void RecoverRootFolder(BlobData i_Root) {
        List<String> lines = FilesManagement.getDataFilesList(m_RepositoryPath.toString(), i_Root.getSHA1());
        List<String> fileDataList = null;

        if (lines != null) {
            for (String fileData : lines) {
                if (!fileData.equals("")) {
                    fileDataList = FilesManagement.ConvertCommaSeparatedStringToList(fileData);
                    if (fileDataList.get(1).equals("file")) {
                        BlobData blob = new BlobData(m_RepositoryPath, i_Root.getPath() + "\\" + fileDataList.get(0), fileDataList.get(3), fileDataList.get(4), false, fileDataList.get(2), null);
                        i_Root.getCurrentFolder().addBlobToList(blob);
                    } else {
                        Folder currentRootFolder = new Folder(fileDataList.get(2));
                        BlobData blob = new BlobData(m_RepositoryPath, i_Root.getPath() + "\\" + fileDataList.get(0), fileDataList.get(3), fileDataList.get(4), true, fileDataList.get(2), currentRootFolder);
                        i_Root.getCurrentFolder().addBlobToList(blob);
                        RecoverRootFolder(blob);
                    }
                }
            }
        }
    }

    private void recoverRepositoryFromFiles() {
        List<String> branchesList = FilesManagement.getBranchesList(m_RepositoryPath.toString());
        // List<String> headBranchData = FilesManagement.ConvertCommaSeparatedStringToList(branchesList.get(0));

        String headBranchContent = FilesManagement.getHeadBranchSha1(m_RepositoryPath.toString());
        String BranchDataOfHeadBranch = FilesManagement.GetCommitNameInZipFromObjects(headBranchContent, m_RepositoryPath.toString());

        for (String sha1AndName : branchesList) {
            List<String> data = FilesManagement.ConvertCommaSeparatedStringToList(sha1AndName);
            String nameBranch = data.get(0);
            String currentCommitSha1 = data.get(1);

            Branch branch = null;
            Commit commit = recoverCommit(currentCommitSha1);

            branch = new Branch(nameBranch, commit, m_RepositoryPath, false, headBranchContent);
            m_AllBranchesList.add(branch);
            if (BranchDataOfHeadBranch.equals(nameBranch)) {
                m_HeadBranch = new HeadBranch(branch, m_RepositoryPath, false, branch.getBranchSha1());
                m_RootFolder = m_HeadBranch.getHeadBranch().getCurrentCommit().getRootFolder();
                m_CurrentCommit = commit;
            }
        }
    }

    public List<String> GetHeadBranchCommitHistory() {
        List<String> commitStringList = new LinkedList<>();
        Commit currentCommit = m_HeadBranch.getBranch().getCurrentCommit();
        setHeadBranchCommitHistoryRec(commitStringList, currentCommit);
        return commitStringList;
    }

    private void setHeadBranchCommitHistoryRec(List<String> i_CommitStringList, Commit i_CurrentCommit) {
        i_CommitStringList.add(i_CurrentCommit.toString());
        if (i_CurrentCommit.getPrevCommit() != null) {
            setHeadBranchCommitHistoryRec(i_CommitStringList, i_CurrentCommit.getPrevCommit());
        }
    }
}
