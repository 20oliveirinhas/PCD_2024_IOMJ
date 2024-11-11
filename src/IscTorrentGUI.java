import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
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
        SwingUtilities.invokeLater(this::updateFileList); // Ensure GUI updates on EDT
    }

    private void updateFileList() {
        if (node != null) {
            FileManager fileManager = node.getFileManager();
            List<File> files = fileManager.getSharedFiles();
            listModel.clear();
            System.out.println("Updating file list in GUI..."); // Debugging line
            for (File file : files) {
                System.out.println("Adding file to listModel: " + file.getName()); // Debugging line
                listModel.addElement(file.getName());
            }
        }
    }

    private void performSearch(String keyword) {
        if (node != null) {
            node.sendSearchRequest(keyword);
        }
    }

    private void startDownload(String selectedFile) {
        if (node != null) {
            // Lógica de Download????
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

    public static void main(String[] args) {
        // Define Portas e um SharedDirectory para ver se tem ficheiros
        int port = 8081;
        String sharedDirectory = "path/to/shared/files";

        // Inicializa o FileManager
        FileManager fileManager = new FileManager(sharedDirectory);

        // Inicializa e faz start ao Node
        try {
            Node node = new Node(port, fileManager);
            Thread serverThread = new Thread(() -> {
                node.start();
            });
            serverThread.start();

            // Inicializa o GUI
            SwingUtilities.invokeLater(() -> {
                IscTorrentGUI gui = new IscTorrentGUI();
                gui.setNode(node);
            });

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
