import java.io.Serializable;

public class FileBlockAnswerMessage implements Serializable {
    private byte[] data;

    public FileBlockAnswerMessage(byte[] data) {
        this.data = data;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }
}
