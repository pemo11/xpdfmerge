package xpdfmergeV1;

import java.util.Hashtable;

public class PdfInfo {
    private String filePath;
    private String fileName;
    private Integer pageCount;
    private String displayName;
    private Hashtable<String, String> bookmarks;

    public PdfInfo() {
        this.bookmarks = new Hashtable<>();
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Integer getPageCount() {
        return pageCount;
    }

    public void setPageCount(Integer pageCount) {
        this.pageCount = pageCount;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public Hashtable<String, String> getBookmarks() {
        return this.bookmarks;
    }

    public  void setBookmarks(Hashtable<String, String> ht) {
        this.bookmarks = ht;
    }

}
