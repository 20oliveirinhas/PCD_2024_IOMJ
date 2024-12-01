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
            // Extraindo informações do ficheiro
            String[] fileInfo = selectedFile.split(" - ");
            String fileName = fileInfo[0]; // Nome do ficheiro

            // Obter a lista de nós que possuem o ficheiro
            List<Socket> availableNodes = node.getNodesWithFile(fileName);
            if (availableNodes.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Nenhum nó disponível para este ficheiro!");
                return;
            }

            // Configuração do diretório de download
            File downloadDir = new File("C:/Users/Inês Oliveira/Downloads");
            if (!downloadDir.exists()) {
                downloadDir.mkdirs();
            }
            // Preparar a localização e criar o `DownloadTasksManager`
            String outputFilePath = downloadDir.getAbsolutePath() + File.separator + fileName;
            File outputFile = new File(outputFilePath);

            // Criar o gestor de tarefas de download
            DownloadTasksManager manager = new DownloadTasksManager(outputFile, outputFilePath);

            // Iniciar a transferência
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
}
