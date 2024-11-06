import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FileLoader {
    private String directoryPath;

    public FileLoader(String directoryPath) {
        this.directoryPath = directoryPath;
    }

    public List<File> loadFiles() {
        File directory = new File(directoryPath);
        List<File> fileList = new ArrayList<>();
        if (directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile()) {
                        fileList.add(file);
                    }
                }
            }
        }
        return fileList;
    }
}
