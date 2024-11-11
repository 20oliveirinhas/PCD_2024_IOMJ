import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ConnectionWindow extends JFrame {
    private JTextField addressField;
    private JTextField portField;
    private JButton connectButton;
    private Node node;

    public ConnectionWindow(Node node) {
        this.node = node;
        setTitle("Connect to Node");
        setSize(300, 150);
        //Fechar a janela da Connection mas não fechar a janela totalmente e não terminar o programa
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridLayout(3, 2));
        panel.add(new JLabel("Address:"));
        addressField = new JTextField();
        panel.add(addressField);

        panel.add(new JLabel("Port:"));
        portField = new JTextField();
        panel.add(portField);

        connectButton = new JButton("Connect");
        panel.add(connectButton);

        add(panel);

        connectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //Depois de 
                String address = addressField.getText();
                int port = Integer.parseInt(portField.getText());
                node.connectToNode(address, port);
                //Para fechar apenas a janela da Connection e não fechar tudo
                dispose();
            }
        });
    }
}
