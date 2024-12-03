import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
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
                    System.out.println("File found: " + file.getName()); // Linha de teste
                }
            }
        } else {
            System.out.println("Directory not found or is not a directory."); // Não existe diretórios
        }
    }

    public List<File> getSharedFiles() {
        return sharedFiles;
    }

    public File getFileByHash(String hash) {
        for (File file : sharedFiles) {
            if (calculateHash(file).equals(hash)) {
                return file;
            }
        }
        return null;
    }

    private String calculateHash(File file) {
        try (FileInputStream fis = new FileInputStream(file)) {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] byteArray = new byte[1024];
            int bytesCount;
            while ((bytesCount = fis.read(byteArray)) != -1) {
                digest.update(byteArray, 0, bytesCount);
            }
            byte[] bytes = digest.digest();
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException | IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
