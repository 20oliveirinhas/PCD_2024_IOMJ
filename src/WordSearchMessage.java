import java.io.Serializable;

public class WordSearchMessage implements Serializable {
    private String keyword;
    private static final long serialVersionUID = 1L;
    public WordSearchMessage(String keyword) {
        this.keyword = keyword;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }
}
