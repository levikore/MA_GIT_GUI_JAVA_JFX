package logicpackage;

import java.nio.file.Path;

public class Commit {
    private String m_RootFolderSha1;
    private Path m_RootFolderPath;
    private Commit m_PrevCommit;

    public Commit(String i_RootFolderSha1, Path i_RootFolderPath, Commit i_PrevCommit) {
        this.m_RootFolderSha1 = i_RootFolderSha1;
        this.m_RootFolderPath = i_RootFolderPath;
        this.m_PrevCommit = i_PrevCommit;
    }


    public Commit(String i_RootFolderSha1, Path i_RootFolderPath) {
        this.m_RootFolderSha1 = i_RootFolderSha1;
        this.m_RootFolderPath = i_RootFolderPath;
        this.m_PrevCommit = null;
    }

    public String getRootFolderSha1() {
        return m_RootFolderSha1;
    }

    public void setRootFolderSha1(String i_RootFolderSha1) {
        this.m_RootFolderSha1 = i_RootFolderSha1;
    }

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
