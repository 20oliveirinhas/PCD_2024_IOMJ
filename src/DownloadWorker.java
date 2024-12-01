import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class DownloadWorker implements Runnable {
    private Socket nodeSocket;
    private DownloadTasksManager manager;

    public DownloadWorker(Socket nodeSocket, DownloadTasksManager manager) {
        this.nodeSocket = nodeSocket;
        this.manager = manager;
    }

    @Override
    public void run() {
        try (ObjectOutputStream out = new ObjectOutputStream(nodeSocket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(nodeSocket.getInputStream())) {

            while (true) {
                FileBlockRequestMessage request = manager.getNextRequest();
                if (request == null) break; // Sem mais blocos para descarregar

                out.writeObject(request);
                out.flush();

                Object response = in.readObject();
                if (response instanceof FileBlockAnswerMessage) {
                    FileBlockAnswerMessage answer = (FileBlockAnswerMessage) response;
                    manager.addReceivedBlock(request.getOffset(), answer.getData());
                }
            }
        } catch (EOFException e) {
            System.out.println("Conexão encerrada corretamente.");
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}

