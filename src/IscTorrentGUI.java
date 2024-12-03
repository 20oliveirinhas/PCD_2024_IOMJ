import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
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
    private List<FileSearchResult> searchResults = new ArrayList<>();
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
                listModel.addElement(result.getFileName() + " - " +"Tamanho" + result.getFileSize() + result.getNodeAddress() + ":" + result.getNodePort());
                searchResults.add(result);

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

            if (!outputFile.exists()) {
                System.out.println("Ficheiro não existe, criando: " + outputFilePath);
                try {
                    outputFile.createNewFile();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } else {
                System.out.println("Ficheiro já existe: " + outputFilePath + ", Tamanho: " + outputFile.length());
            }
            if (outputFile.exists() && outputFile.length() > 0) {
                System.out.println("Aviso: O ficheiro já existe e contém dados.");
                return;
            } else {
                System.out.println("Ficheiro vazio ou inexistente. Preparando para descarregar blocos do servidor.");
            }

            // Declarar o manager fora do bloco
            DownloadTasksManager manager = null;

            String thisfile = resultList.getSelectedValue();
            int selectedIndex = resultList.getSelectedIndex();

            if (selectedIndex >= 0) {
                FileSearchResult fileResult = searchResults.get(selectedIndex); // Obtém o objeto associado
                String fileHash = fileResult.getFileHash();
                long fileSize = fileResult.getFileSize(); // Obtém o tamanho do ficheiro
                manager = new DownloadTasksManager(outputFile, outputFilePath, fileSize, fileHash); // Inicializa o manager
                manager.startDownloadTimer();
            }

            // Verificar se o manager foi inicializado
            if (manager == null || manager.getBlockRequests().isEmpty()) {
                System.err.println("Erro: Nenhum bloco para descarregar. Verifique o ficheiro no servidor.");
                return;
            }

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
    public void showDownloadResults(String filename, long duration, int TotalBlocks, int BytesDownloaded){
        JDialog dialog = new JDialog(frame, "Resultados do Descarregamento", true);
        dialog.setSize(400, 250);
        dialog.setLayout(new BorderLayout());
        String message = String.format(
                "<html><h2>Descarregamento concluído!</h2>" +
                        "<p><b>Ficheiro:</b> %s</p>" +
                        "<p><b>Duração:</b> %d ms</p>" +
                        "<p><b>Total de Blocos:</b> %d</p>" +
                        "<p><b>Total de Bytes:</b> %d</p></html>",
                filename, duration, TotalBlocks, BytesDownloaded
        );
        JLabel messageLabel = new JLabel(message, SwingConstants.CENTER);
        JButton okButton = new JButton("OK");
        okButton.addActionListener(e -> dialog.dispose());
        dialog.add(messageLabel, BorderLayout.CENTER);
        dialog.add(okButton, BorderLayout.SOUTH);

        // Mostrar o dialog
        dialog.setVisible(true);
    }

}
