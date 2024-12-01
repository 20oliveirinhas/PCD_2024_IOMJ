import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class DownloadWorker implements Runnable {
    private Socket nodeSocket;
    private DownloadTasksManager manager;
    private ObjectOutputStream out;
    private ObjectInputStream in;

    public DownloadWorker(Socket nodeSocket, DownloadTasksManager manager) {
        this.nodeSocket = nodeSocket;
        this.manager = manager;
    }


    @Override
    public void run() {
        System.out.println(nodeSocket);
        try {
            // Garantir inicialização única dos streams
            synchronized (this) {
                if (out == null && in == null) {
                    System.out.println("Cliente: Inicializando streams.");
                    this.out = new ObjectOutputStream(nodeSocket.getOutputStream());
                    this.in = new ObjectInputStream(nodeSocket.getInputStream());
                }
            }

            // Processar pedidos de download
            while (!nodeSocket.isClosed()) {
                FileBlockRequestMessage request = manager.getNextRequest();
                if (request == null) {
                    System.out.println("Cliente: Todos os pedidos de download processados.");
                    break;
                }

                try {
                    // Enviar pedido
                    out.writeObject(request);
                    out.flush();
                    System.out.println("Cliente: Pedido de bloco enviado.");

                    // Ler resposta
                    Object response = in.readObject();
                    if (response instanceof FileBlockAnswerMessage) {
                        FileBlockAnswerMessage answer = (FileBlockAnswerMessage) response;
                        manager.addReceivedBlock(request.getOffset(), answer.getData());
                        System.out.println("Cliente: Bloco recebido.");
                    } else {
                        System.err.println("Cliente: Resposta inesperada ou inválida recebida.");
                    }
                } catch (EOFException e) {
                    System.err.println("EOFException: Conexão encerrada pelo servidor durante o download.");
                    break;
                } catch (IOException | ClassNotFoundException e) {
                    System.err.println("Cliente: Erro ao processar comunicação.");
                    e.printStackTrace();
                    break;
                }
            }
        } catch (IOException e) {
            System.err.println("Cliente: Erro ao inicializar os streams. Conexão pode ter sido encerrada.");
            e.printStackTrace();
        } finally {
            try {
                if (nodeSocket != null && !nodeSocket.isClosed()) {
                    nodeSocket.close();
                    System.out.println("Cliente: Socket encerrado.");
                }
            } catch (IOException e) {
                System.err.println("Cliente: Erro ao fechar o socket.");
                e.printStackTrace();
            }
        }
    }


}


