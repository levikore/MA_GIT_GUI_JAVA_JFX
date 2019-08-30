package logicpackage;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

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

    public RepositoryManager(Path i_RepositoryPath, String i_CurrentUserName, boolean i_IsNewRepository, boolean i_IsEmptyFolders) {
        m_RepositoryPath = i_RepositoryPath;
        m_RepositoryName = m_RepositoryPath.toFile().getName();
        m_CurrentUserName = i_CurrentUserName;
        m_MagitPath = Paths.get(m_RepositoryPath.toString() + "\\" + c_GitFolderName);
        if (i_IsNewRepository) {
            initializeRepository(i_IsEmptyFolders);
        } else {
            m_IsFirstCommit = false;
            recoverRepositoryFromFiles();
        }
    }

    public boolean HandleMerge(String i_BranchName) {
        Branch branchToMerge = findBranchByName(i_BranchName);
        boolean retVal = false;
        if (branchToMerge != null) {
            m_HeadBranch.Merge(branchToMerge);
            ///
            //maybe more actions......
            ///
            retVal = true;
        }
        return retVal;
    }

    public Path GetRepositoryPath() {
        return m_RepositoryPath;
    }

    public String GetCurrentUserName() {
        return m_CurrentUserName;
    }

    public void SetCurrentUserName(String i_CurrentUserName) {
        this.m_CurrentUserName = i_CurrentUserName;
    }

    private void initializeRepository(boolean i_IsEmptyFolders) {
        m_RootFolder = getInitializedRootFolder(m_CurrentUserName);
        createSystemFolders();

        if (!i_IsEmptyFolders) {
            Branch branch = new Branch("master", m_CurrentCommit, m_RepositoryPath, true, "");
            m_AllBranchesList.add(branch);
            m_HeadBranch = new HeadBranch(branch, m_RepositoryPath, true, "");
        }
    }

    private void createSystemFolders() {
        FilesManagement.CreateFolder(m_RepositoryPath.getParent(), m_RepositoryName);
        FilesManagement.CreateFolder(m_RepositoryPath, c_GitFolderName);
        FilesManagement.CreateFolder(m_MagitPath, c_ObjectsFolderName);
        FilesManagement.CreateFolder(m_MagitPath, c_BranchesFolderName);
    }

    private void createNewCommit(String i_CommitComment) {
        Commit newCommit;

        if (m_IsFirstCommit) {
            newCommit = new Commit(m_RootFolder, i_CommitComment, m_CurrentUserName, null, "", "");
            m_IsFirstCommit = false;
        } else {
            List<Commit> prevCommitsList = new LinkedList<>();
            prevCommitsList.add(m_CurrentCommit);
            newCommit = new Commit(m_RootFolder, i_CommitComment, m_CurrentUserName, prevCommitsList, "", "");
        }

        m_CurrentCommit = newCommit;
        String sha1 = FilesManagement.CreateCommitDescriptionFile(m_CurrentCommit, m_RepositoryPath, false);
        m_CurrentCommit.SetCurrentCommitSHA1(sha1);

        if (m_HeadBranch == null) {
            Branch branch = new Branch("master", m_CurrentCommit, m_RepositoryPath, true, "");
            removeBranFromBranchesListByName("master");
            m_AllBranchesList.add(branch);
            m_HeadBranch = new HeadBranch(branch, m_RepositoryPath, true, "");
        } else {
            m_HeadBranch.UpdateCurrentBranch(m_CurrentCommit);
        }
    }

    private RootFolder getInitializedRootFolder(String i_UserName) {
        Folder rootFolder = new Folder();
        BlobData rootFolderBlobData = new BlobData(m_RepositoryPath, m_RepositoryPath.toFile().toString(), rootFolder, i_UserName);
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
        Branch branch = new Branch(i_BranchName, m_HeadBranch.GetBranch(), m_RepositoryPath, true, "");
        m_AllBranchesList.add(branch);
    }

    public Boolean IsBranchExist(String i_BranchName) {
        Branch fountBranch = m_AllBranchesList.stream()
                .filter(branch -> i_BranchName.equals(branch.GetBranchName()))
                .findAny()
                .orElse(null);

        return fountBranch != null;
    }

    private void removeBranFromBranchesListByName(String i_BranchName) {
        Branch branchToRemove = findBranchByName(i_BranchName);
        if (branchToRemove != null) {
            m_AllBranchesList.remove(branchToRemove);
        }
    }

    public boolean RemoveBranch(String i_BranchName) {
        boolean returnValue = true;
        Branch branchToRemove = findBranchByName(i_BranchName);

        if (branchToRemove == m_HeadBranch.GetBranch()) {
            returnValue = false;
        } else if (branchToRemove == null) {
            returnValue = false;
        } else {
            FilesManagement.RemoveFileByPath(branchToRemove.GetBranchPath());
            FilesManagement.RemoveFileByPath(Paths.get(m_MagitPath.toString() + "\\" + c_ObjectsFolderName + "\\" + branchToRemove.GetBranchSha1() + ".zip"));
            m_AllBranchesList.remove(branchToRemove);
        }

        return returnValue;
    }

    private void handleFirstCommit(String i_CommitComment) throws IOException {
        m_RootFolder = getInitializedRootFolder(m_CurrentUserName);
        m_RootFolder.UpdateCurrentRootFolderSha1(m_CurrentUserName, "", null, false);
        createNewCommit(i_CommitComment);
    }

    public boolean HandleCheckout(String i_BranchName) {
        Branch branchToCheckout = findBranchByName(i_BranchName);
        boolean retVal = false;

        if (branchToCheckout != null) {
            m_HeadBranch.Checkout(branchToCheckout);
            m_RootFolder = m_HeadBranch.GetBranch().GetCurrentCommit().GetRootFolder();
            retVal = true;
        }

        return retVal;
    }

    private Branch findBranchByName(String i_BranchName) {
        Branch branchToReturn = null;
        if (m_AllBranchesList != null) {
            for (Branch branch : m_AllBranchesList) {
                if (branch.GetBranchName().equals(i_BranchName)) {
                    branchToReturn = branch;
                }
            }
        }
        return branchToReturn;
    }

    public List<String> GetListOfUnCommittedFiles() throws IOException {
        RootFolder testRootFolder = createFolderWithZipsOfUnCommittedFiles();
        String testFolderPath = m_MagitPath + "\\" + c_TestFolderName;
        List<String> unCommittedFilesList = new LinkedList<>();
        if (!testRootFolder.GetSHA1().equals(m_RootFolder.GetSHA1())) {
            getAllUncommittedFiles(testRootFolder, unCommittedFilesList);
        }

        FileUtils.deleteDirectory((Paths.get(testFolderPath).toFile()));
        return unCommittedFilesList;
    }

    private void getAllUncommittedFiles(RootFolder io_TestRootFolder, List<String> io_UnCommittedFilesList) {
        Comparator<BlobData> pathComparator
                = Comparator.comparing(BlobData::GetPath);
        addUncommittedBlobToListRecursively(pathComparator,
                m_RootFolder.GetRootFolder().GetCurrentFolder(),
                io_TestRootFolder.GetRootFolder().GetCurrentFolder(),
                io_UnCommittedFilesList);
    }

    private void addUncommittedBlobToListRecursively(Comparator<BlobData> i_PathComparator, Folder i_Folder, Folder i_TestFolder, List<String> io_UnCommittedFilesList) {

        List<BlobData> currentBlobList = i_Folder.GetBlobList();
        List<BlobData> testBlobList = i_TestFolder.GetBlobList();
        int savedJ = 0;
        int savedI = 0;
        int j = 0;

        for (int i = 0; i < currentBlobList.size(); i++) {
            BlobData blob = currentBlobList.get(i);
            if (savedJ > j) {
                j = savedJ;
            }
            while (j < testBlobList.size()) {
                BlobData testBlob = testBlobList.get(j);
                if (!blob.GetPath().equals(testBlob.GetPath()) && isPath1FolowsPath2(blob.GetPath(), testBlob.GetPath())) {
                    handleUncommittedNewFile(testBlob, io_UnCommittedFilesList);
                } else if (blob.GetPath().equals(testBlob.GetPath())) {
                    if (!blob.GetSHA1().equals(testBlob.GetSHA1())) {
                        if (testBlob.GetIsFolder()) {
                            addUncommittedBlobToListRecursively(i_PathComparator, blob.GetCurrentFolder(), testBlob.GetCurrentFolder(), io_UnCommittedFilesList);
                        } else {
                            io_UnCommittedFilesList.add(testBlob.GetPath());
                        }
                    }
                    j++;
                    savedJ = j;
                    break;
                }
                if (!isPath1FolowsPath2(blob.GetPath(), testBlob.GetPath())) {
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

        for (int i = savedI + 1; i < currentBlobList.size(); i++) {
            BlobData blob = currentBlobList.get(i);
            io_UnCommittedFilesList.add(blob.GetPath());
            if (blob.GetIsFolder()) {
                addUncommittedFolderToList(blob.GetCurrentFolder(), io_UnCommittedFilesList);
            }
        }

        for (j = savedJ; j < testBlobList.size(); j++) {
            BlobData testBlob = testBlobList.get(j);
            io_UnCommittedFilesList.add(testBlob.GetPath());
            if (testBlob.GetIsFolder()) {
                addUncommittedFolderToList(testBlob.GetCurrentFolder(), io_UnCommittedFilesList);
            }
        }

    }

    private void addUncommittedFolderToList(Folder i_Folder, List<String> i_List) {
        List<BlobData> blobDataList = i_Folder.GetBlobList();

        for (BlobData blob : blobDataList) {
            i_List.add(blob.GetPath());
            if (blob.GetIsFolder()) {
                addUncommittedFolderToList(blob.GetCurrentFolder(), i_List);
            }
        }

    }

    private void handleUncommittedNewFile(BlobData i_TestBlob, List<String> io_UnCommittedFilesList) {
        io_UnCommittedFilesList.add(i_TestBlob.GetPath());
        if (i_TestBlob.GetIsFolder()) {
            addUncommittedFolderToList(i_TestBlob.GetCurrentFolder(), io_UnCommittedFilesList);
        }
    }

    private boolean isPath1FolowsPath2(String path1, String path2) {
        boolean retVal = false;
        if (path1.compareTo(path2) > 0) {
            retVal = true;
        }
        return retVal;
    }

    private RootFolder createFolderWithZipsOfUnCommittedFiles() throws IOException {

        FilesManagement.CreateFolder(m_MagitPath, c_TestFolderName);
        RootFolder testRootFolder = getInitializedRootFolder(m_CurrentUserName);
        Folder currentRootFolder = new Folder(m_RootFolder.GetRootFolder().GetCurrentFolder().GetFolderSha1());
        List<BlobData> allFilesFromCurrentRootFolder=new LinkedList<>();

        if(m_RootFolder!=null) {
            BlobData rootFolderBlobDataTemp = new BlobData(m_RepositoryPath,
                    m_RepositoryPath.toFile().toString(),
                    m_RootFolder.GetRootFolder().GetLastChangedBY(),
                    m_RootFolder.GetRootFolder().GetLastChangedTime(),
                    true,
                    m_RootFolder.GetSHA1(),
                    currentRootFolder);
            recoverRootFolder(rootFolderBlobDataTemp, allFilesFromCurrentRootFolder);
        }

        testRootFolder.UpdateCurrentRootFolderSha1(m_CurrentUserName, c_TestFolderName, allFilesFromCurrentRootFolder, false);
        return testRootFolder;
    }

    public boolean IsUncommittedFilesInRepository() throws IOException {
        RootFolder testRootFolder = createFolderWithZipsOfUnCommittedFiles();
        boolean isCommitNecessary = !(testRootFolder.GetSHA1().equals(m_RootFolder.GetSHA1()));
        clearDirectory((Paths.get(m_MagitPath.toString() + "\\" + c_TestFolderName).toFile()));
        return isCommitNecessary;
    }

    private Boolean handleSecondCommit(String i_CommitComment) throws IOException {
        boolean isCommitNecessary = false;
        RootFolder testRootFolder = createFolderWithZipsOfUnCommittedFiles();

        if (!testRootFolder.GetSHA1().equals(m_RootFolder.GetSHA1())) {
            copyFiles(m_MagitPath + "\\" + c_TestFolderName, m_MagitPath + "\\" + c_ObjectsFolderName);
            m_RootFolder = testRootFolder;
            createNewCommit(i_CommitComment);
            isCommitNecessary = true;
        }

        clearDirectory((Paths.get(m_MagitPath.toString() + "\\" + c_TestFolderName).toFile()));

        return isCommitNecessary;
    }

    private void clearDirectory(File i_directory) {
        File[] fileList = i_directory.listFiles();

        for (File file : Objects.requireNonNull(fileList)) {
            file.delete();
        }

        i_directory.delete();
    }

    private void copyFiles(String i_From, String i_To){

        Path source = Paths.get(i_From);
        Path destination = Paths.get(i_To);
        File[] fileList = source.toFile().listFiles();

        for (File file : Objects.requireNonNull(fileList)) {
            if (file.renameTo(new File(destination + "\\" + file.getName()))) {
                file.delete();
            }
        }
    }

    public HeadBranch GetHeadBranch() {
        return m_HeadBranch;
    }

    public List<Branch> GetAllBranchesList() {
        return m_AllBranchesList;
    }

    public List<String> GetAllBranchesStringList() {
        List<String> branchesList = new LinkedList<>();
        if (m_AllBranchesList != null) {
            String headBranchName = GetHeadBranch().GetBranch().GetBranchName();
            m_AllBranchesList.forEach(branch -> {

                String currentCommitSha1 = branch.GetCurrentCommit() != null ? branch.GetCurrentCommit().GetCurrentCommitSHA1() : "";
                String currentCommitComment = branch.GetCurrentCommit() != null ? branch.GetCurrentCommit().GetCommitComment() : "";
                branchesList.add("Branch name:" + branch.GetBranchName() + (headBranchName.equals(branch.GetBranchName()) ? " IS HEAD" : "") + '\n'
                        + "Commit SHA1 of Branch:" + currentCommitSha1
                        + '\n' + "Commit comment:"
                        + currentCommitComment);
            });
        }
        return branchesList;
    }

    private Commit recoverCommit(String i_CommitSha1) {
        Commit commit = new Commit();
        recoverCommitRecursively(commit, i_CommitSha1);
        return commit;
    }

    private void recoverCommitRecursively(Commit i_CurrentCommit, String i_CurrentCommitSha1) {
        List<String> commitLines = FilesManagement.GetCommitData(i_CurrentCommitSha1, m_RepositoryPath.toString());
        String rootFolderSha1 = Objects.requireNonNull(commitLines).get(0);
        List<String> prevCommitsSha1List = FilesManagement.ConvertCommaSeparatedStringToList(commitLines.get(1));
        String commitComment = commitLines.get(2);
        String time = commitLines.get(3);
        String userName = commitLines.get(4);

        int parentIndex = 0;
        List<Commit> prevCommitsList = null;
        if (!prevCommitsSha1List.get(0).equals("")) {
            prevCommitsList = new LinkedList<>();
            for (String prevCommitSha1 : prevCommitsSha1List) {
                Commit commit = new Commit();
                prevCommitsList.add(commit);
                recoverCommitRecursively(prevCommitsList.get(parentIndex), prevCommitSha1);
                parentIndex++;
            }
        }
        Folder currentRootFolder = new Folder(rootFolderSha1);
        BlobData rootFolderBlobData = new BlobData(m_RepositoryPath, m_RepositoryPath.toFile().toString(), userName, time, true, rootFolderSha1, currentRootFolder);
        recoverRootFolder(rootFolderBlobData, null);
        RootFolder rootFolder = new RootFolder(rootFolderBlobData, m_RepositoryPath);
        i_CurrentCommit.UpdateCommit(rootFolder, commitComment, userName, prevCommitsList, i_CurrentCommitSha1, time);
    }

    private void recoverRootFolder(BlobData i_Root, List<BlobData> i_FilesList) {
        List<String> lines = FilesManagement.GetDataFilesList(m_RepositoryPath.toString(), i_Root.GetSHA1());
        List<String> fileDataList;

        if (lines != null) {
            for (String fileData : lines) {
                if (!fileData.equals("")) {
                    fileDataList = FilesManagement.ConvertCommaSeparatedStringToList(fileData);
                    BlobData blob;
                    if (fileDataList.get(1).equals("file")) {
                        blob = new BlobData(m_RepositoryPath, i_Root.GetPath() + "\\" + fileDataList.get(0), fileDataList.get(3), fileDataList.get(4), false, fileDataList.get(2), null);
                        i_Root.GetCurrentFolder().AddBlobToList(blob);
                    } else {
                        Folder currentRootFolder = new Folder(fileDataList.get(2));
                        blob = new BlobData(m_RepositoryPath, i_Root.GetPath() + "\\" + fileDataList.get(0), fileDataList.get(3), fileDataList.get(4), true, fileDataList.get(2), currentRootFolder);
                        i_Root.GetCurrentFolder().AddBlobToList(blob);
                        recoverRootFolder(blob, i_FilesList);
                    }
                    if (i_FilesList != null) {
                        i_FilesList.add(blob);
                    }
                }
            }
        }
    }

    private void recoverRepositoryFromFiles() {
        List<String> branchesList = FilesManagement.GetBranchesList(m_RepositoryPath.toString());
        String headBranchContent = FilesManagement.GetHeadBranchSha1(m_RepositoryPath.toString());
        String BranchDataOfHeadBranch = FilesManagement.GetCommitNameInZipFromObjects(headBranchContent, m_RepositoryPath.toString());

        for (String sha1AndName : branchesList) {
            List<String> data = FilesManagement.ConvertCommaSeparatedStringToList(sha1AndName);
            String nameBranch = data.get(0);
            String currentCommitSha1 = data.get(1);

            Branch branch;
            Commit commit = recoverCommit(currentCommitSha1);
            String branchContent = FilenameUtils.removeExtension(FilesManagement.FindFileByNameInZipFileInPath(nameBranch + ".txt", Paths.get(m_RepositoryPath.toString() + "\\" + c_GitFolderName + "\\" + c_ObjectsFolderName)).getName());
            String headSha1 = FilenameUtils.removeExtension(FilesManagement.FindFileByNameInZipFileInPath("HEAD.txt", Paths.get(m_RepositoryPath.toString() + "\\" + c_GitFolderName + "\\" + c_ObjectsFolderName)).getName());
            branch = new Branch(nameBranch, commit, m_RepositoryPath, false, branchContent);
            removeBranFromBranchesListByName(nameBranch);
            m_AllBranchesList.add(branch);
            if (BranchDataOfHeadBranch.equals(nameBranch)) {
                m_HeadBranch = new HeadBranch(branch, m_RepositoryPath, false, headSha1);
                m_RootFolder = m_HeadBranch.GetHeadBranch().GetCurrentCommit().GetRootFolder();
                m_CurrentCommit = commit;
            }
        }


    }


///////////////////////////////////////////////////////
//    public List<String> GetHeadBranchCommitHistory() {
//        List<String> commitStringList = new LinkedList<>();
//        Commit currentCommit = m_HeadBranch.getBranch().getCurrentCommit();
//        setHeadBranchCommitHistoryRec(commitStringList, currentCommit);
//        return commitStringList;
//    }
//
//    private void setHeadBranchCommitHistoryRec(List<String> i_CommitStringList, Commit i_CurrentCommit) {
//        i_CommitStringList.add(i_CurrentCommit.toString());
//        if (i_CurrentCommit.getPrevCommit() != null) {
//            setHeadBranchCommitHistoryRec(i_CommitStringList, i_CurrentCommit.getPrevCommit());
//        }
//    }
    //////////////////////////////////////////////////////////
}
