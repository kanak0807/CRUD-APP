import java.io.*;
import java.net.*;

public class SupportClient {
    public static void main(String[] args) {
        try (Socket socket = new Socket("localhost", 5050);
             BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter output = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in))) {

            System.out.println("Connected to Support Server. Start chatting!");
            System.out.println("Type 'exit' to quit.");

            // Thread to listen for incoming messages from the technician
            new Thread(() -> {
                try {
                    String message;
                    while ((message = input.readLine()) != null) {
                        System.out.println(message);
                    }
                } catch (IOException e) {
                    System.out.println("Disconnected from server.");
                }
            }).start();

            // Main loop to send messages from client to technician
            String userMessage;
            while ((userMessage = userInput.readLine()) != null) {
                if (userMessage.equalsIgnoreCase("exit")) {
                    break;
                }
                output.println(userMessage);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}