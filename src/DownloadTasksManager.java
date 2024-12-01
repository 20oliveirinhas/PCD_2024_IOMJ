import java.io.File;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

public class DownloadTasksManager {
    private List<FileBlockRequestMessage> blockRequests;
    private Map<Integer, byte[]> receivedBlocks; // Para armazenar blocos descarregados
    private int totalBlocks;
    private String outputFilePath;


    public DownloadTasksManager(File file, String outputFilePath) {
        this.blockRequests = new ArrayList<>();
        this.receivedBlocks = new HashMap<>();
        this.outputFilePath = outputFilePath;

        try {
            if (!file.exists()) {
                file.createNewFile(); // Garante que o ficheiro é criado se não existir
            }
        } catch (IOException e) {
            System.err.println("Erro ao criar o ficheiro de download: " + file.getAbsolutePath());
            e.printStackTrace();
        }

        String fileHash = calculateHash(file);
        int blockSize = 10240; // 10 KB
        long fileSize = file.length();
        this.totalBlocks = (int) Math.ceil((double) fileSize / blockSize);

        for (int offset = 0; offset < fileSize; offset += blockSize) {
            int length = (int) Math.min(blockSize, fileSize - offset);
            blockRequests.add(new FileBlockRequestMessage(fileHash, offset, length));
        }
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

    public List<FileBlockRequestMessage> getBlockRequests() {
        return blockRequests;
    }
    public synchronized void addRequest(FileBlockRequestMessage request) {
        blockRequests.add(request);
    }

    public synchronized FileBlockRequestMessage getNextRequest() {
       System.out.println("Cheguei ao GetNextRequest");
        return blockRequests.isEmpty() ? null : blockRequests.remove(0);
    }

    public synchronized void addReceivedBlock(int offset, byte[] data) {
        receivedBlocks.put(offset, data);
        if (receivedBlocks.size() == totalBlocks) {
            notifyAll(); // Notificar que todos os blocos foram descarregados
        }
    }

    public synchronized void waitForCompletion() {
        while (receivedBlocks.size() < totalBlocks) {
            try {
                wait(); // Esperar até todos os blocos estarem descarregados
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public void writeToDisk() throws IOException {
        try (RandomAccessFile raf = new RandomAccessFile(outputFilePath, "rw")) {
            for (int offset = 0; offset < totalBlocks * 10240; offset += 10240) {
                byte[] data = receivedBlocks.get(offset);
                if (data != null) {
                    raf.seek(offset);
                    raf.write(data);
                }
            }
            System.out.println("Ficheiro escrito em: " + outputFilePath);
        } catch (IOException e) {
            System.err.println("Erro ao escrever o ficheiro: " + outputFilePath);
            throw e;
        }
    }
}

