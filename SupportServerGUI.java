import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.*;
import java.util.LinkedList;
import java.util.Queue;

public class SupportServerGUI {
    private static final int PORT = 6060;
    private ServerSocket serverSocket;
    private Queue<Socket> clientQueue = new LinkedList<>();
    private BufferedReader in;
    private PrintWriter out;
    private JTextArea chatArea;
    private JTextField inputField;
    private JButton nextClientButton;
    private Socket currentClient;

    public SupportServerGUI() {
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception ignored) {}

        JFrame frame = new JFrame("Support Dashboard (Technician)");
        frame.setSize(550, 700);
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

        nextClientButton = new JButton("Next Client");
        nextClientButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        nextClientButton.setBackground(new Color(137, 180, 250));
        nextClientButton.setForeground(new Color(24, 24, 37));
        nextClientButton.setFocusPainted(false);
        nextClientButton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(137, 180, 250), 10),
                BorderFactory.createEmptyBorder(5, 15, 5, 15)
        ));
        nextClientButton.addActionListener(e -> serveNextClient());

        JPanel bottomPanel = new JPanel(new BorderLayout(10, 0));
        bottomPanel.setBackground(new Color(30, 30, 46));
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        bottomPanel.add(inputField, BorderLayout.CENTER);
        bottomPanel.add(nextClientButton, BorderLayout.EAST);

        frame.add(scrollPane, BorderLayout.CENTER);
        frame.add(bottomPanel, BorderLayout.SOUTH);
        frame.setVisible(true);

        startServer();
    }

    private void startServer() {
        try {
            serverSocket = new ServerSocket(PORT);
            chatArea.append("Support Server started...\nWaiting for clients...\n");

            new Thread(() -> {
                while (true) {
                    try {
                        Socket newClient = serverSocket.accept();
                        synchronized (clientQueue) {
                            clientQueue.offer(newClient);
                        }
                        chatArea.append("New client in queue: " + newClient + "\n");
                    } catch (IOException e) {
                        chatArea.append("Error accepting client connection.\n");
                        e.printStackTrace();
                    }
                }
            }).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void serveNextClient() {
        synchronized (clientQueue) {
            if (currentClient != null) {
                try {
                    currentClient.close(); // ✅ Close previous connection
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (!clientQueue.isEmpty()) {
                currentClient = clientQueue.poll();
                chatArea.append("Serving new client: " + currentClient + "\n");

                try {
                    in = new BufferedReader(new InputStreamReader(currentClient.getInputStream()));
                    out = new PrintWriter(currentClient.getOutputStream(), true);

                    // ✅ Start a thread to receive messages continuously
                    new Thread(() -> {
                        try {
                            String message;
                            while ((message = in.readLine()) != null) {
                                chatArea.append("Client: " + message + "\n");
                                saveChat("Client: " + message);
                            }
                        } catch (IOException ex) {
                            chatArea.append("Client disconnected.\n");
                        }
                    }).start();

                } catch (IOException e) {
                    chatArea.append("Error connecting to client.\n");
                }
            } else {
                chatArea.append("No more clients in queue.\n");
            }
        }
    }

    private void sendMessage(String message) {
        if (out != null && !message.trim().isEmpty()) {
            out.println(message);
            out.flush(); // ✅ Ensure message is sent immediately
            chatArea.append("Technician: " + message + "\n");
            saveChat("Technician: " + message);
        } else {
            chatArea.append("⚠️ No active client connection.\n");
        }
    }

    private void saveChat(String message) {
        try (FileWriter fw = new FileWriter("chat_history.txt", true);
             BufferedWriter bw = new BufferedWriter(fw)) {
            bw.write(message + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new SupportServerGUI();
    }
}