package logicpackage;

import java.nio.file.Path;

public class Commit {
    private Path m_RootFolderPath;
    private Commit m_PrevCommit;
    private RootFolder m_RootFolder;

    public Commit(RootFolder i_RootFolder, Commit i_PrevCommit) {
        this.m_RootFolder=i_RootFolder;
        this.m_PrevCommit = i_PrevCommit;
        i_RootFolder.UpdateCurrentRootFolderSha1();
    }


    public Commit(RootFolder i_RootFolder) {
        this.m_RootFolder=i_RootFolder;
        this.m_PrevCommit = null;
        i_RootFolder.UpdateCurrentRootFolderSha1();
    }

    public String getRootFolderSha1() {
        return m_RootFolder.getSHA1();
    }

//    public void setRootFolderSha1(String i_RootFolderSha1) {
//        this.m_RootFolderSha1 = i_RootFolderSha1;
//    }

    public Path getRootFolderPath() {
        return m_RootFolderPath;
    }

    public void setRootFolderPath(Path i_RootFolderPath) {
        this.m_RootFolderPath = i_RootFolderPath;
    }

    public Commit getPrevCommit() {
        return m_PrevCommit;
    }

    public void setPrevCommit(Commit i_PrevCommit) {
        this.m_PrevCommit = i_PrevCommit;
    }

}
