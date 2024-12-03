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
    private final List<FileBlockRequestMessage> blockRequests;
    private final Map<Integer, byte[]> receivedBlocks; // Para armazenar blocos descarregados
    private final int totalBlocks;
    private final String outputFilePath;
    private long startTime;
    private String fileName;
    public DownloadTasksManager(File file, String outputFilePath, long fileSize, String fileHash) {
        System.out.println("Criando DownloadTasksManager para o ficheiro: " + file);
        this.blockRequests = new ArrayList<>();
        this.receivedBlocks = new HashMap<>();
        this.outputFilePath = outputFilePath;
        this.fileName = file.getName();
        // Verificar e criar o ficheiro local, se necessário
        try {
            if (!file.exists()) {
                System.out.println("Ficheiro não existe. Criando: " + file.getAbsolutePath());
                file.createNewFile();
            }
        } catch (IOException e) {
            System.err.println("Erro ao criar o ficheiro local: " + file.getAbsolutePath());
            throw new RuntimeException(e);
        }

        // Validar o tamanho do ficheiro
        if (fileSize <= 0) {
            throw new IllegalArgumentException("Erro: O tamanho do ficheiro é inválido: " + fileSize);
        }

        // Criar os blocos
        int blockSize = 10240; // Tamanho de bloco: 10 KB
        this.totalBlocks = (int) Math.ceil((double) fileSize / blockSize);
        for (int offset = 0; offset < fileSize; offset += blockSize) {
            int length = (int) Math.min(blockSize, fileSize - offset);
            blockRequests.add(new FileBlockRequestMessage(fileHash, offset, length));
        }

        System.out.println("Total de blocos criados: " + blockRequests.size());
    }

    public List<FileBlockRequestMessage> getBlockRequests() {
        return blockRequests;
    }

    public synchronized FileBlockRequestMessage getNextRequest() {
        System.out.println("Obtendo próximo pedido de bloco...");
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
                    System.out.println("Escrevendo bloco: Offset " + offset + ", Bytes: " + data.length);
                } else {
                    System.err.println("Bloco em falta no offset: " + offset);
                }
            }
            System.out.println("Ficheiro escrito com sucesso: " + outputFilePath);
        } catch (IOException e) {
            System.err.println("Erro ao escrever o ficheiro: " + outputFilePath);
            throw e;
        }
    }
    public void startDownloadTimer() {
        this.startTime = System.currentTimeMillis();
    }
    public long getDownloadDuration() {
        return System.currentTimeMillis() - this.startTime;
    }

    public int getTotalBlocks(){
        return this.totalBlocks;
    }
    public int getTotalBytesDownloaded() {
        // Soma o tamanho de todos os dados descarregados
        return receivedBlocks.values().stream().mapToInt(data -> data.length).sum();
    }
    public String getFileName() {
        return fileName;
    }
}