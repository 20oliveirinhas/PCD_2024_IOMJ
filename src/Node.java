import java.io.*;
import java.net.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Node {
    private ServerSocket serverSocket;
    private ExecutorService threadPool;
    private List<Socket> connectedNodes;
    private FileManager fileManager;
    private int ThreadPoolnumber =5;
    private IscTorrentGUI gui;
    private ObjectOutputStream out;
    private ObjectInputStream in;
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
                System.out.println("Servidor iniciado. Aguardando conexões...");
                System.out.println("Cliente conectado: " + clientSocket.getInetAddress());
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
    private ObjectOutputStream out;
    private ObjectInputStream in;
        public ClientHandler(Socket clientSocket) {
            this.clientSocket = clientSocket;
        }
    @Override
    public void run() {
        try {
            this.out = new ObjectOutputStream(clientSocket.getOutputStream());
            this.in = new ObjectInputStream(clientSocket.getInputStream());

            while (true) {
                Object message = in.readObject();
                System.out.println("Servidor: Mensagem recebida do tipo " + message.getClass().getSimpleName());

                if (message instanceof WordSearchMessage) {
                    handleSearchRequest((WordSearchMessage) message, out);
                } else if (message instanceof FileBlockRequestMessage) {
                    System.out.println("Cheguei ao fileblockrequestmessage do lado do Run do ClientHandler");
                    handleFileBlockRequest((FileBlockRequestMessage) message, out);

                } else {
                    System.out.println("Mensagem inesperada recebida: " + message.getClass().getName());
                }
            }
        } catch (EOFException e) {
            System.out.println("Cliente encerrou a conexão: " + clientSocket);
        } catch (IOException e) {
            System.err.println("Erro de comunicação com o cliente: " + e.getMessage());
        } catch (ClassNotFoundException e) {
            System.err.println("Erro ao interpretar mensagem recebida: " + e.getMessage());
        } finally {
            // Certifique-se de que o socket é desconectado ao sair do loop
            if (!clientSocket.isClosed() && clientSocket != null) {
                disconnectNode(clientSocket);
            }

            System.out.println("Conexão encerrada com o cliente: " + clientSocket);
        }
    }

}

    public void connectToNode(String address, int port) {
        try {
            Socket socket = new Socket(address, port);
            if (!connectedNodes.contains(socket)) {
                connectedNodes.add(socket);
                System.out.println("Connected to node at " + address + ":" + port);

                // Inicializar os fluxos apenas uma vez
                out = new ObjectOutputStream(socket.getOutputStream());
                in = new ObjectInputStream(socket.getInputStream());
            }
        } catch (IOException e) {
            System.err.println("Failed to connect to node at " + address + ":" + port);
            e.printStackTrace();
        }
    }

    public void sendSearchRequest(String keyword) {
        try {
            WordSearchMessage searchMessage = new WordSearchMessage(keyword);
            System.out.println("Client: Sending search request with keyword '" + keyword + "'");

            // Enviar a mensagem
            out.writeObject(searchMessage);
            out.flush();

            // Ler a resposta
            Object response = in.readObject();
            if (response instanceof List) {
                List<FileSearchResult> results = (List<FileSearchResult>) response;
                System.out.println("Client: Response received with " + results.size() + " result(s).");

                if (gui != null) {
                    gui.updateSearchResults(results);
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Client: Error in communication - " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void setGui(IscTorrentGUI gui) {
        this.gui = gui;
    }
    private void handleSearchRequest(WordSearchMessage searchMessage, ObjectOutputStream out) {
        try {
            List<FileSearchResult> results =  searchFiles(searchMessage.getKeyword());
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
                        calculateHash(file),  // Hash do ficheiro
                        "127.0.0.1",          // Endereço do nó
                        serverSocket.getLocalPort()
                ));
                System.out.println("Server: Ficheiro encontrado: " + file.getName() );
            }
        }
        System.out.println("Server: Total de ficheiros encontrados para '" + keyword + "': " + results.size());
        return results;
    }
    private void handleFileBlockRequest(FileBlockRequestMessage requestMessage, ObjectOutputStream out) {
        try {
            System.out.println("Servidor: Pedido de bloco recebido. Offset: " + requestMessage.getOffset() +
                    ", Tamanho: " + requestMessage.getLength());

            File file = fileManager.getFileByHash(requestMessage.getFileHash());
            if (!file.exists() || !file.isFile()) {
                System.err.println("Servidor: Ficheiro não encontrado para o hash: " + requestMessage.getFileHash());
                return;
            }

            try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
                byte[] buffer = new byte[requestMessage.getLength()];
                raf.seek(requestMessage.getOffset());
                int bytesRead = raf.read(buffer);

                if (bytesRead > 0) {
                    FileBlockAnswerMessage answerMessage = new FileBlockAnswerMessage(buffer);
                    out.writeObject(answerMessage);
                    out.flush();
                    System.out.println("Servidor: Bloco enviado com tamanho: " + bytesRead);
                } else {
                    System.err.println("Servidor: Bloco vazio lido do ficheiro.");
                }
            }
        } catch (IOException e) {
            System.err.println("Servidor: Erro ao processar o pedido de bloco - " + e.getMessage());
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
    public void disconnectNode(Socket socket) {
        try {
            if (connectedNodes.contains(socket)) {
                connectedNodes.remove(socket);
                if (!socket.isClosed()) {
                    socket.close();
                }
                System.out.println("Socket desconectado: " + socket);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public List<Socket> getNodesWithFile(String fileName) {
        List<Socket> nodesWithFile = new ArrayList<>();
        System.out.println("Node: Verificando quais nós possuem o ficheiro: " + fileName);

        for (Socket socket : connectedNodes) {
            System.out.println("Numero de nos Conectados:"+ connectedNodes.size());
            try {

                // Enviar mensagem de pesquisa
                WordSearchMessage searchMessage = new WordSearchMessage(fileName);
                out.writeObject(searchMessage);
                out.flush();
                System.out.println("Node: Mensagem de pesquisa enviada ao nó: " + socket.getRemoteSocketAddress());

                // Ler a resposta
                Object response = in.readObject();
                System.out.println("Resposta Recebida no GetNodesWithFiles" + response);
                if (response instanceof List) {
                    List<FileSearchResult> results = (List<FileSearchResult>) response;
                    System.out.println("Cliente: Resultados recebidos: " + results.size());
                    for (FileSearchResult result : results) {
                        System.out.println("Cliente: Ficheiro encontrado: " + result.getFileName() + " no nó " + result.getNodeAddress());
                        if (result.getFileName().equals(fileName)) {
                            nodesWithFile.add(socket);
                            System.out.println("Node: Nó encontrado com o ficheiro: " + result.getNodeAddress() + ":" + result.getNodePort());
                            break;
                        }
                    }
                } else {
                    System.err.println("Node: Resposta inesperada do nó: " + socket.getRemoteSocketAddress());
                }
            } catch (EOFException e) {
                System.err.println("Node: O stream foi encerrado prematuramente: " + e.getMessage());
            } catch (IOException | ClassNotFoundException e) {
                System.err.println("Node: Erro ao verificar nó: " + socket.getRemoteSocketAddress());
                e.printStackTrace();
            }
        }

        System.out.println("Node: Total de nós encontrados com o ficheiro: " + nodesWithFile.size());
        return nodesWithFile;
    }

    public void startFileDownload(List<Socket> nodeSockets, DownloadTasksManager manager) {
        ExecutorService threadPool = Executors.newFixedThreadPool(nodeSockets.size());
        System.out.println("Cheguei ao StartFileDownload");
        // Cria uma thread para cada socket
        for (Socket socket : nodeSockets) {
            threadPool.execute(() -> handleFileDownload(socket, manager));
            manager.setMainSupplier(socket.getRemoteSocketAddress().toString());
        }
        threadPool.shutdown();
        try {
            threadPool.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        // Espera pela conclusão do descarregamento
        manager.waitForCompletion();
        try {
            manager.writeToDisk();
            System.out.println("Download completo e ficheiro escrito em disco!");
            long duration = manager.getDownloadDuration();
            int totalBlocks = manager.getTotalBlocks();
            int bytesDownloaded = manager.getTotalBytesDownloaded();
            gui.showDownloadResults(manager.getFileName(), duration,totalBlocks,bytesDownloaded, manager.getMainSupplier());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleFileDownload(Socket socket, DownloadTasksManager manager) {
        try {
            System.out.println("Node: Usando streams já inicializados para " + socket.getRemoteSocketAddress());
            while (!socket.isClosed()) {
                FileBlockRequestMessage request = manager.getNextRequest();
                if (request == null) {
                    System.err.println("Nenhum pedido de bloco encontrado no manager.");
                    break;
                }
                // Envia o pedido de bloco
                synchronized (out) {
                    out.writeObject(request);
                    out.flush();
                    System.out.println("Node: Pedido enviado.");
                }
                // Recebe a resposta
                synchronized (in) {
                    Object response = in.readObject();
                    if (response instanceof FileBlockAnswerMessage) {
                        FileBlockAnswerMessage answer = (FileBlockAnswerMessage) response;
                        manager.addReceivedBlock(request.getOffset(), answer.getData());
                        System.out.println("Node: Bloco recebido.");
                    } else {
                        System.err.println("Node: Resposta inesperada.");
                    }
                }
            }
        } catch (EOFException e) {
            System.err.println("Node: Conexão encerrada inesperadamente.");
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

}

