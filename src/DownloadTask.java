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
            File file = new File("path/to/shared/files", requestMessage.getFileHash());
            try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
                byte[] buffer = new byte[requestMessage.getLength()];
                raf.seek(requestMessage.getOffset());
                raf.read(buffer);
                FileBlockAnswerMessage answerMessage = new FileBlockAnswerMessage(buffer);
                out.writeObject(answerMessage);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
