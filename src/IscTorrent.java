import javax.swing.SwingUtilities;
import java.io.File;

public class IscTorrent {
    public static void main(String[] args) {
        // Check if the correct number of arguments is provided
        if (args.length != 2) {
            System.err.println("Usage: java IscTorrent <port> <sharedFolder>");
            System.exit(1);
        }

        int port;
        try {
            port = Integer.parseInt(args[0]);  // Convert the first argument to an integer for the port
        } catch (NumberFormatException e) {
            System.err.println("Error: Port must be an integer.");
            System.exit(2);
            return;
        }

        String sharedFolder = args[1];
        File folder = new File(sharedFolder);
        if (!folder.exists() || !folder.isDirectory()) {
            System.err.println("Error: The specified shared folder does not exist or is not a directory.");
            System.exit(3);
        }

        // Assuming you have a method to start the network manager
        // You might want to create a NetworkManager class that handles all network-related tasks
        // NetworkManager networkManager = new NetworkManager(port, sharedFolder);
        // networkManager.start();

        // Setup and display the GUI
        SwingUtilities.invokeLater(() -> {
            IscTorrentGUI gui = new IscTorrentGUI(port, sharedFolder);
            gui.setVisible(true);
        });
    }
}
