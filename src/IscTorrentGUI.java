import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class IscTorrentGUI extends JFrame {
    private JTextField searchField;
    private JButton searchButton, downloadButton, connectButton;
    private JList<String> resultsList;
    private DefaultListModel<String> resultsModel;

    public IscTorrentGUI(int port, String sharedFolder) {
        setTitle("IscTorrent - File Sharing System");
        setSize(700, 290);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        setupUI();
        setupActions(sharedFolder);
        setVisible(true);
    }

    private void setupUI() {
        JPanel mainPanel = new JPanel(new BorderLayout());

        JPanel searchPanel = new JPanel(new BorderLayout());
        searchPanel.add(new JLabel("Search Text:"), BorderLayout.WEST);
        searchField = new JTextField();
        searchPanel.add(searchField, BorderLayout.CENTER);
        searchButton = new JButton("Search");
        searchPanel.add(searchButton, BorderLayout.EAST);
        mainPanel.add(searchPanel, BorderLayout.NORTH);

        resultsModel = new DefaultListModel<>();
        resultsList = new JList<>(resultsModel);
        JScrollPane scrollPane = new JScrollPane(resultsList);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new GridLayout(2, 1));
        downloadButton = new JButton("Download");
        connectButton = new JButton("Connect to Node");
        buttonPanel.add(downloadButton);
        buttonPanel.add(connectButton);
        mainPanel.add(buttonPanel, BorderLayout.EAST);

        add(mainPanel);
    }

    private void setupActions(String sharedFolder) {
        connectButton.addActionListener(e -> {
            String address = JOptionPane.showInputDialog(this, "Enter node address:");
            // Implement connection logic here
        });

        searchButton.addActionListener(e -> {
            String keyword = searchField.getText();
            // Implement search logic here
        });

        downloadButton.addActionListener(e -> {
            String selectedFile = resultsList.getSelectedValue();
            if (selectedFile != null) {
                File file = new File(sharedFolder, selectedFile);
                DownloadTasksManager manager = new DownloadTasksManager();
                List<FileBlockRequestMessage> requests = manager.createBlockRequestList(file);
                // Now, handle the download requests
            }
        });


    }


}
