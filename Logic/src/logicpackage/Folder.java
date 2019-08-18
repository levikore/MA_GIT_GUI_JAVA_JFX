package logicpackage;

import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.Comparator;
import java.util.stream.Collectors;


public class Folder {
    private List<BlobData> m_BlobList = new LinkedList<>();
    private Path m_Path;
    private String m_FolderSha1;


    public Folder() {

    }

    public Folder(String i_FolderSha1) {
        m_FolderSha1=i_FolderSha1;
    }

    public void addBlobToList(BlobData blobData) {
        m_BlobList.add(blobData);
        m_BlobList.sort(Comparator.comparing(BlobData::getPath));
    }

    public List<BlobData> getBlobList() {
        return m_BlobList;
    }

    public String getFolderSha1() {
        return m_FolderSha1;
    }

    public void setFolderSha1(String i_FolderSha1) {
        this.m_FolderSha1 = i_FolderSha1;
    }

    public void ScanBlobListIntoWc()
    {
        m_BlobList.stream().forEach(blobData -> blobData.RecoverWCFromCurrentBlobData());
    }

    public void addAllBlobsUnderCurrentFolderToList(List<String> i_DataList)
    {
        m_BlobList.stream().forEach(blobData -> blobData.AddBlobDataToList(i_DataList));
    }




}
