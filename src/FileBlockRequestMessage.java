import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FileBlockRequestMessage {
    private String fileHash;
    private int offset;
    private int length;

    public FileBlockRequestMessage(String fileHash, int offset, int length) {
        this.fileHash = fileHash;
        this.offset = offset;
        this.length = length;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public String getFileHash() {
        return fileHash;
    }

    public void setFileHash(String fileHash) {
        this.fileHash = fileHash;
    }


}

