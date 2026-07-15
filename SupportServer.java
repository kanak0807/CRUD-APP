import java.io.*;
import java.net.*;
import java.util.*;

public class SupportServer {
    private static List<ClientHandler> clients = new ArrayList<>();

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(5050)) {
            System.out.println("Support Server started...");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected: " + clientSocket);

                // Handle each client in a separate thread
                ClientHandler clientHandler = new ClientHandler(clientSocket);
                clients.add(clientHandler);
                new Thread(clientHandler).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Inner class to handle each client
    static class ClientHandler implements Runnable {
        private Socket socket;
        private BufferedReader input;
        private PrintWriter output;

        public ClientHandler(Socket socket) {
            this.socket = socket;
            try {
                input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                output = new PrintWriter(socket.getOutputStream(), true);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            try {
                // Thread to listen for messages from the client
                new Thread(() -> {
                    try {
                        String message;
                        while ((message = input.readLine()) != null) {
                            System.out.println("Client: " + message);
                            broadcastMessage("Client: " + message);
                        }
                    } catch (IOException e) {
                        System.out.println("Client disconnected.");
                    }
                }).start();

                // Thread to take input from the technician and send to clients
                BufferedReader technicianInput = new BufferedReader(new InputStreamReader(System.in));
                String serverMessage;
                while ((serverMessage = technicianInput.readLine()) != null) {
                    broadcastMessage("Technician: " + serverMessage);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private void broadcastMessage(String message) {
            for (ClientHandler client : clients) {
                client.output.println(message);
            }
        }
    }
}