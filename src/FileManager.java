import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FileManager {
    private List<File> sharedFiles;

    public FileManager(String directoryPath) {
        sharedFiles = new ArrayList<>();
        File directory = new File(directoryPath);
        if (directory.exists() && directory.isDirectory()) {
            for (File file : directory.listFiles()) {
                if (file.isFile()) {
                    sharedFiles.add(file);
                    System.out.println("File found: " + file.getName()); // Debugging line
                }
            }
        } else {
            System.out.println("Directory not found or is not a directory."); // Debugging line
        }
    }

    public List<File> getSharedFiles() {
        return sharedFiles;
    }
}
