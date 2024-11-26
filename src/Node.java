import java.io.*;
import java.net.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Node {
    private ServerSocket serverSocket;
    private ExecutorService threadPool;
    private List<Socket> connectedNodes;
    private FileManager fileManager;
    private int ThreadPoolnumber =5;
    private IscTorrentGUI gui;
    public Node(int port, FileManager fileManager) throws IOException {
        this.serverSocket = new ServerSocket(port);
        this.threadPool = Executors.newFixedThreadPool(ThreadPoolnumber);
        this.connectedNodes = new ArrayList<>();
        this.fileManager = fileManager;
    }

    public void start() {
        while (true) {
            try {
                Socket clientSocket = serverSocket.accept();
                connectedNodes.add(clientSocket);
                threadPool.execute(new ClientHandler(clientSocket));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public FileManager getFileManager() {
        return fileManager;
    }
//Esta é a tarefa de uma theead, sacar o ClientHandler e fazer Run daquilo que será a comunicação cliente servidor,
// os Nodes
    private class ClientHandler implements Runnable {
        private Socket clientSocket;

        public ClientHandler(Socket clientSocket) {
            this.clientSocket = clientSocket;
        }
    @Override
    public void run() {
        try (ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream())) {
            Object message;
            while ((message = in.readObject()) != null) {
                if (message instanceof WordSearchMessage) {
                    System.out.println("Received WordSearchMessage: " + ((WordSearchMessage) message).getKeyword());
                    handleSearchRequest((WordSearchMessage) message, out);
                }
            }
        } catch (EOFException e) {
            System.out.println("Client closed the connection: " + clientSocket);
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error in communication: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                clientSocket.close();
                System.out.println("Socket closed for client: " + clientSocket);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}

    public void connectToNode(String address, int port) {
        try {
            Socket socket = new Socket(address, port);
            if (!connectedNodes.contains(socket)) {
                connectedNodes.add(socket);
                System.out.println("Connected to node at " + address + ":" + port);
            }
        } catch (IOException e) {
            System.err.println("Failed to connect to node at " + address + ":" + port);
            e.printStackTrace();
        }
    }


    public void sendSearchRequest(String keyword) {
        WordSearchMessage searchMessage = new WordSearchMessage(keyword);
        for (Socket socket : connectedNodes) {
            try (ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                 ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

                System.out.println("Client: Sending search request with keyword '" + keyword + "' to node " + socket.getRemoteSocketAddress());
                out.writeObject(searchMessage);
                out.flush();
                System.out.println("Client: Search request sent. Awaiting response...");

                // Ler a resposta do servidor
                Object response = in.readObject();
                if (response instanceof List) {
                    List<?> rawResults = (List<?>) response;
                    List<FileSearchResult> results = (List<FileSearchResult>) rawResults;
                    System.out.println("These are treated Results" + results);
                    System.out.println("Client: Response received with " + results.size() + " result(s).");
                    if (gui == null) {
                        System.err.println("GUI não está configurada no Node.");
                        return;
                    }else {
                        gui.updateSearchResults(results);
                    }
                } else {
                    System.out.println("Client: Unexpected response type received.");
                }

            } catch (IOException | ClassNotFoundException e) {
                System.err.println("Client: Error in communication - " + e.getMessage());
                e.printStackTrace();
            }
        }
    }


    public void setGui(IscTorrentGUI gui) {
        this.gui = gui;
        System.out.println("GUI configurada com sucesso no Node.");
    }

    private void handleSearchRequest(WordSearchMessage searchMessage, ObjectOutputStream out) {
        try {
            List<FileSearchResult> results = searchFiles(searchMessage.getKeyword());
            System.out.println("Server: Found " + results.size() + " result(s) for keyword '" + searchMessage.getKeyword() + "'");
            System.out.println("Server: Sending results to client...");
            out.writeObject(results);
            out.flush();
            System.out.println("Server: Results sent to client.");
        } catch (IOException e) {
            System.err.println("Server: Error writing response - " + e.getMessage());
            e.printStackTrace();
        }
    }


    private List<FileSearchResult> searchFiles(String keyword) {
        List<FileSearchResult> results = new ArrayList<>();
        for (File file : fileManager.getSharedFiles()) {
            if (file.getName().contains(keyword)) {
                results.add(new FileSearchResult(
                        file.getName(),
                        file.length(),
                        calculateHash(file),  // Hash do arquivo
                        "127.0.0.1",          // Endereço do nó (ajustar para real)
                        serverSocket.getLocalPort()
                ));
            }
        }
        return results;
    }

    private void handleFileBlockRequest(FileBlockRequestMessage requestMessage, ObjectOutputStream out) {
        File file = new File("path/to/shared/files", requestMessage.getFileHash());
        try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
            byte[] buffer = new byte[requestMessage.getLength()];
            raf.seek(requestMessage.getOffset());
            raf.read(buffer);
            FileBlockAnswerMessage answerMessage = new FileBlockAnswerMessage(buffer);
            out.writeObject(answerMessage);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
// Por confirmar ainda não está a ser utilizada
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
