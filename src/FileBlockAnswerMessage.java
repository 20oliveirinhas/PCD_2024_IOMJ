import java.io.Serializable;

public class FileBlockAnswerMessage implements Serializable {
    private static final long serialVersionUID = 1L;
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
