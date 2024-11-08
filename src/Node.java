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

    public Node(int port, FileManager fileManager) throws IOException {
        this.serverSocket = new ServerSocket(port);
        this.threadPool = Executors.newFixedThreadPool(5); // Example with 5 threads
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

    private class ClientHandler implements Runnable {
        private Socket clientSocket;

        public ClientHandler(Socket clientSocket) {
            this.clientSocket = clientSocket;
        }

        @Override
        public void run() {
            try (ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream());
                 ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream())) {
                Object message;
                while ((message = in.readObject()) != null) {
                    if (message instanceof WordSearchMessage) {
                        handleSearchRequest((WordSearchMessage) message, out);
                    } else if (message instanceof FileBlockRequestMessage) {
                        handleFileBlockRequest((FileBlockRequestMessage) message, out);
                    }
                    // Handle other message types...
                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    public void connectToNode(String address, int port) {
        try {
            Socket socket = new Socket(address, port);
            connectedNodes.add(socket);
            // Handle connection logic
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendSearchRequest(String keyword) {
        WordSearchMessage searchMessage = new WordSearchMessage(keyword);
        for (Socket socket : connectedNodes) {
            try (ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream())) {
                out.writeObject(searchMessage);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void handleSearchRequest(WordSearchMessage searchMessage, ObjectOutputStream out) {
        List<FileSearchResult> results = searchFiles(searchMessage.getKeyword());
        try {
            out.writeObject(results);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private List<FileSearchResult> searchFiles(String keyword) {
        List<FileSearchResult> results = new ArrayList<>();
        for (File file : fileManager.getSharedFiles()) {
            if (file.getName().contains(keyword)) {
                results.add(new FileSearchResult(file.getName(), file.length(), calculateHash(file), "nodeAddress", 8081));
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
