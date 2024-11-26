import java.io.Serializable;

public class FileSearchResult implements Serializable {
    private static final long serialVersionUID = 1L;
    private String fileName;
    private long fileSize;
    private String fileHash;
    private String nodeAddress;
    private int nodePort;

    public FileSearchResult(String fileName, long fileSize, String fileHash, String nodeAddress, int nodePort) {
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.fileHash = fileHash;
        this.nodeAddress = nodeAddress;
        this.nodePort = nodePort;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public String getFileHash() {
        return fileHash;
    }

    public void setFileHash(String fileHash) {
        this.fileHash = fileHash;
    }

    public String getNodeAddress() {
        return nodeAddress;
    }

    public void setNodeAddress(String nodeAddress) {
        this.nodeAddress = nodeAddress;
    }

    public int getNodePort() {
        return nodePort;
    }

    public void setNodePort(int nodePort) {
        this.nodePort = nodePort;
    }
}
