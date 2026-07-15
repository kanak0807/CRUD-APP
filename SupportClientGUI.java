import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.*;

public class SupportClientGUI {
    private static final String SERVER_IP = "127.0.0.100"; // Localhost
    private static final int PORT = 6060;
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private JTextArea chatArea;
    private JTextField inputField;

    public SupportClientGUI() {
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception ignored) {}

        JFrame frame = new JFrame("Customer Support");
        frame.setSize(450, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().setBackground(new Color(30, 30, 46));

        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setBackground(new Color(30, 30, 46));
        chatArea.setForeground(new Color(205, 214, 244));
        chatArea.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);
        chatArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JScrollPane scrollPane = new JScrollPane(chatArea);
        scrollPane.setBorder(null);

        inputField = new JTextField();
        inputField.setBackground(new Color(49, 50, 68));
        inputField.setForeground(new Color(205, 214, 244));
        inputField.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        inputField.setCaretColor(new Color(205, 214, 244));
        inputField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(49, 50, 68), 10),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        inputField.addActionListener(e -> {
            sendMessage(inputField.getText());
            inputField.setText("");
        });

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(new Color(30, 30, 46));
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        bottomPanel.add(inputField, BorderLayout.CENTER);

        frame.add(scrollPane, BorderLayout.CENTER);
        frame.add(bottomPanel, BorderLayout.SOUTH);
        frame.setVisible(true);

        connectToServer();
    }

    private void connectToServer() {
        try {
            chatArea.append("Waiting for technician to accept your request...\n");
            socket = new Socket(SERVER_IP, PORT);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            chatArea.append("Connected to Support Server!\n");

            // ✅ Start a background thread to receive messages continuously
            new Thread(() -> {
                try {
                    String message;
                    while ((message = in.readLine()) != null) {
                        chatArea.append("Technician: " + message + "\n");
                    }
                } catch (IOException ex) {
                    chatArea.append("Connection lost.\n");
                }
            }).start();

        } catch (IOException e) {
            chatArea.append("Failed to connect to server. Please wait...\n");
        }
    }

    private void sendMessage(String message) {
        if (out != null && !message.trim().isEmpty()) {
            out.println(message);
            out.flush(); // ✅ Ensures message is sent immediately
            chatArea.append("You: " + message + "\n");
        } else {
            chatArea.append("⚠️ Message not sent. Check connection.\n");
        }
    }

    public static void main(String[] args) {
        new SupportClientGUI();
    }
}
