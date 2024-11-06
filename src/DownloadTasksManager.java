import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class DownloadTasksManager {
    private static final int BLOCK_SIZE = 10240; // Size of each file block

    public List<FileBlockRequestMessage> createBlockRequestList(File file) {
        List<FileBlockRequestMessage> blockRequestMessages = new ArrayList<>();
        long fileSize = file.length();
        String fileHash = calculateFileHash(file); // This method should calculate the hash of the file

        for (long offset = 0; offset < fileSize; offset += BLOCK_SIZE) {
            int length = (int) Math.min(BLOCK_SIZE, fileSize - offset);
            blockRequestMessages.add(new FileBlockRequestMessage(fileHash, offset, length));
        }
        return blockRequestMessages;
    }

    private String calculateFileHash(File file) {
        // Placeholder method for hash calculation
        return "hash_placeholder";
    }
}
