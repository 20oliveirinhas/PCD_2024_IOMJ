import java.io.*;
import java.net.Socket;

public class DownloadTask implements Runnable {
    private Socket clientSocket;
    private FileBlockRequestMessage requestMessage;

    public DownloadTask(Socket clientSocket, FileBlockRequestMessage requestMessage) {
        this.clientSocket = clientSocket;
        this.requestMessage = requestMessage;
    }

    @Override
    public void run() {
        try (ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream())) {
            // Lê o bloco do ficheiro e envia a resposta
            File file = new File("path/to/shared/files", requestMessage.getFileHash());
            try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
                byte[] buffer = new byte[requestMessage.getLength()];
                raf.seek(requestMessage.getOffset());
                int bytesRead = raf.read(buffer);
                if (bytesRead > 0) {
                    FileBlockAnswerMessage answerMessage = new FileBlockAnswerMessage(buffer);
                    out.writeObject(answerMessage);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
