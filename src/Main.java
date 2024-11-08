import javax.swing.*;
import java.io.IOException;

public static void main(String[] args) {
    // Define the port and the directory for shared files
    int port = 8081;
    String sharedDirectory = "C:/Users/Inês Oliveira/Downloads/sharedfolder";

    // Initialize the FileManager
    FileManager fileManager = new FileManager(sharedDirectory);

    // Initialize and start the Node
    try {
        Node node = new Node(port, fileManager);
        Thread serverThread = new Thread(() -> {
            node.start();
        });
        serverThread.start();

        // Initialize the GUI
        SwingUtilities.invokeLater(() -> {
            IscTorrentGUI gui = new IscTorrentGUI();
            gui.setNode(node);
        });

    } catch (IOException e) {
        e.printStackTrace();
    }
}