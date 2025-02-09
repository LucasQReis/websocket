package com.example.websocket_demo.client;

import com.example.websocket_demo.Message;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class ClientGUI extends JFrame implements MessageListener {
    private final JPanel connectedUsersPanel;
    private final JPanel messagePanel;
    private final MyStompClient myStompClient;
    private final String username;
    private final JScrollPane messagePanelScrollPane;

    public ClientGUI(String username) throws ExecutionException, InterruptedException {
        super("User: " + username);
        this.username = username;
        myStompClient = new MyStompClient(this, username);

        setSize(1218, 685);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setupWindowListener();
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                updateMessageSize();
            }
        });

        getContentPane().setBackground(Utilities.PRIMARY_COLOR);

        connectedUsersPanel = createConnectedUsersPanel();
        messagePanel = createMessagePanel();
        messagePanelScrollPane = createScrollPane(messagePanel);

        add(connectedUsersPanel, BorderLayout.WEST);
        add(createChatPanel(), BorderLayout.CENTER);
    }

    private void setupWindowListener() {
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                int option = JOptionPane.showConfirmDialog(ClientGUI.this,
                        "Do you really want to leave?", "Exit", JOptionPane.YES_NO_OPTION);
                if (option == JOptionPane.YES_OPTION) {
                    myStompClient.disconnectUser(username);
                    dispose();
                }
            }
        });
    }

    private JPanel createConnectedUsersPanel() {
        JPanel panel = new JPanel();
        panel.setBorder(Utilities.addPadding(10, 10, 10, 10));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Utilities.SECONDARY_COLOR);
        panel.setPreferredSize(new Dimension(200, getHeight()));

        JLabel label = new JLabel("Connected Users");
        label.setFont(new Font("Inter", Font.BOLD, 18));
        label.setForeground(Utilities.TEXT_COLOR);
        panel.add(label);

        return panel;
    }

    private JPanel createMessagePanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Utilities.TRANSPARENT);
        return panel;
    }

    private JScrollPane createScrollPane(JPanel panel) {
        JScrollPane scrollPane = new JScrollPane(panel);
        scrollPane.setBackground(Utilities.TRANSPARENT);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getViewport().addChangeListener(e -> {
            revalidate();
            repaint();
        });
        return scrollPane;
    }

    private JPanel createChatPanel() {
        JPanel chatPanel = new JPanel(new BorderLayout());
        chatPanel.setBackground(Utilities.TRANSPARENT);
        chatPanel.add(messagePanelScrollPane, BorderLayout.CENTER);
        chatPanel.add(createInputPanel(), BorderLayout.SOUTH);
        return chatPanel;
    }

    private JPanel createInputPanel() {
        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.setBorder(Utilities.addPadding(10, 10, 10, 10));
        inputPanel.setBackground(Utilities.TRANSPARENT);

        JTextField inputField = new JTextField();
        inputField.setBackground(Utilities.SECONDARY_COLOR);
        inputField.setForeground(Utilities.TEXT_COLOR);
        inputField.setBorder(Utilities.addPadding(0, 10, 0, 10));
        inputField.setFont(new Font("Inter", Font.PLAIN, 16));
        inputField.setPreferredSize(new Dimension(inputPanel.getWidth(), 50));
        inputField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                if (e.getKeyChar() == KeyEvent.VK_ENTER) {
                    String input = inputField.getText().trim();
                    if (!input.isEmpty()) {
                        inputField.setText("");
                        myStompClient.sendMessage(new Message(username, input));
                    }
                }
            }
        });

        inputPanel.add(inputField, BorderLayout.CENTER);
        return inputPanel;
    }

    private JPanel createChatMessageComponent(Message message) {
        JPanel chatMessage = new JPanel();
        chatMessage.setBackground(Utilities.TRANSPARENT);
        chatMessage.setLayout(new BoxLayout(chatMessage, BoxLayout.Y_AXIS));
        chatMessage.setBorder(Utilities.addPadding(20, 20, 10, 20));

        JLabel usernameLabel = new JLabel(message.getUser());
        usernameLabel.setFont(new Font("Inter", Font.BOLD, 18));
        usernameLabel.setForeground(Utilities.TEXT_COLOR);

        JLabel messageLabel = new JLabel(formatMessageHtml(message.getMessage()));
        messageLabel.setFont(new Font("Inter", Font.PLAIN, 18));
        messageLabel.setForeground(Utilities.TEXT_COLOR);

        chatMessage.add(usernameLabel);
        chatMessage.add(messageLabel);

        return chatMessage;
    }

    private String formatMessageHtml(String text) {
        return String.format("<html><body style='width:%.0fpx'>%s</body></html>", 0.60 * getWidth(), text);
    }

    @Override
    public void onMessageRecieve(Message message) {
        messagePanel.add(createChatMessageComponent(message));
        revalidate();
        repaint();
        messagePanelScrollPane.getVerticalScrollBar().setValue(Integer.MAX_VALUE);
    }

    @Override
    public void onActiveUsersUpdated(ArrayList<String> users) {
        SwingUtilities.invokeLater(() -> {
            connectedUsersPanel.removeAll();

            JLabel label = new JLabel("Connected Users");
            label.setFont(new Font("Inter", Font.BOLD, 18));
            label.setForeground(Utilities.TEXT_COLOR);
            connectedUsersPanel.add(label);

            users.forEach(user -> {
                JLabel usernameLabel = new JLabel(user);
                usernameLabel.setForeground(Utilities.TEXT_COLOR);
                usernameLabel.setFont(new Font("Inter", Font.BOLD, 16));
                connectedUsersPanel.add(usernameLabel);
            });

            revalidate();
            repaint();
        });
    }

    private void updateMessageSize() {
        for (Component component : messagePanel.getComponents()) {
            if (component instanceof JPanel chatMessage) {
                if (chatMessage.getComponent(1) instanceof JLabel messageLabel) {
                    messageLabel.setText(formatMessageHtml(messageLabel.getText()));
                }
            }
        }
    }
}
