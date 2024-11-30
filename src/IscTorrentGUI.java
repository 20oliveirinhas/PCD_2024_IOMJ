import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.util.List;

public class IscTorrentGUI {
    private Node node;
    private JFrame frame;
    private JTextField searchField;
    private JButton searchButton;
    private JList<String> resultList;
    private JButton downloadButton;
    private JButton connectButton;
    private DefaultListModel<String> listModel;

    public IscTorrentGUI() {
        frame = new JFrame("IscTorrent");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 400);

        searchField = new JTextField(30);
        searchButton = new JButton("Procurar");
        listModel = new DefaultListModel<>();
        resultList = new JList<>(listModel);
        downloadButton = new JButton("Descarregar");
        connectButton = new JButton("Ligar a Nó");

        JPanel panel = new JPanel();
        panel.add(searchField);
        panel.add(searchButton);
        frame.add(panel, BorderLayout.NORTH);

        frame.add(new JScrollPane(resultList), BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel();
        bottomPanel.add(downloadButton);
        bottomPanel.add(connectButton);
        frame.add(bottomPanel, BorderLayout.SOUTH);

        frame.setVisible(true);

        searchButton.addActionListener(e -> performSearch(searchField.getText()));
        downloadButton.addActionListener(e -> startDownload(resultList.getSelectedValue()));
        connectButton.addActionListener(e -> showConnectDialog());
    }

    public void setNode(Node node) {
        this.node = node;
        SwingUtilities.invokeLater(this::updateFileList); // Ter a certeza que o GUI faz updates da lista de ficheiros
        //e mostra
    }

    private void updateFileList() {
        if (node != null) {
            FileManager fileManager = node.getFileManager();
            List<File> files = fileManager.getSharedFiles();
            listModel.clear();
            System.out.println("Updating file list in GUI..."); // Linha de teste
            for (File file : files) {
                System.out.println("Adding file to listModel: " + file.getName()); //Linha de teste
                listModel.addElement(file.getName());
            }
        }
    }

    private void performSearch(String keyword) {
        if (node != null) {
            node.sendSearchRequest(keyword);
        }
    }

    public void updateSearchResults(List<FileSearchResult> results) {
        SwingUtilities.invokeLater(() -> {
            listModel.clear();
            for (FileSearchResult result : results) {
                listModel.addElement(result.getFileName() + " - " + result.getNodeAddress() + ":" + result.getNodePort());
            }
        });
    }

    private void startDownload(String selectedFile) {
        if (node != null && selectedFile != null) {
            String[] fileInfo = selectedFile.split(" - ");
            String fileName = fileInfo[0]; // Nome do ficheiro

            // Obter a lista de nós com o ficheiro
            List<Socket> availableNodes = node.getNodesWithFile(fileName);
            if (availableNodes.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Nenhum nó disponível para este ficheiro!");
                return;
            }

            // Configurar o DownloadTasksManager
            String outputFilePath = "downloads/" + fileName;
            File fileToDownload = new File(outputFilePath);
            DownloadTasksManager manager = new DownloadTasksManager(fileToDownload, outputFilePath);

            // Iniciar o processo de descarregamento
            node.startFileDownload(availableNodes, manager);
        }
    }

    private void showConnectDialog() {
        if (node != null) {
            SwingUtilities.invokeLater(() -> {
                ConnectionWindow connectionWindow = new ConnectionWindow(node);
                connectionWindow.setVisible(true);
            });
        }
    }

    public void main(String[] args) {
        // Define Portas e um SharedDirectory para ver se tem ficheiros
        int port = 8081;
        String sharedDirectory = "path/to/shared/files";

        // Inicializa o FileManager
        FileManager fileManager = new FileManager(sharedDirectory);

        // Inicializa e faz start ao Node
        try {
            Node node = new Node(port, fileManager);

            // Inicializa o GUI
            SwingUtilities.invokeLater(() -> {
                IscTorrentGUI gui = new IscTorrentGUI();
                gui.setNode(node); // Passa o nó para a GUI
                node.setGui(gui);  // Passa a GUI para o nó
            });

            // Faz o start do Node num thread separado
            Thread serverThread = new Thread(node::start);
            serverThread.start();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
