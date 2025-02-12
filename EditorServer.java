import java.io.*;
import java.net.*;
import java.util.*;

public class EditorServer {
    private static final int PORT = 12345;  // Port number for the server
    private static List<PrintWriter> clients = new ArrayList<>();
    private static StringBuilder document = new StringBuilder("Start editing...\n");

    public static void main(String[] args) {
        System.out.println("Server started. Waiting for clients...");
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                new ClientHandler(serverSocket.accept()).start();
            }
        } catch (IOException e) {
            System.out.println("Server error: " + e.getMessage());
        }
    }

    private static class ClientHandler extends Thread {
        private Socket socket;
        private PrintWriter out;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try (
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true)
            ) {
                this.out = out;
                synchronized (clients) {
                    clients.add(out);
                }

                // Send the current document content to the new client
                out.println("Connected to server! Current document:\n" + document.toString());

                String input;
                while ((input = in.readLine()) != null) {
                    synchronized (document) {
                        document.append(input).append("\n");
                    }
                    broadcast(input);
                }
            } catch (IOException e) {
                System.out.println("Client disconnected.");
            } finally {
                synchronized (clients) {
                    clients.remove(out);
                }
            }
        }

        private void broadcast(String message) {
            synchronized (clients) {
                for (PrintWriter client : clients) {
                    client.println("UPDATE: " + message);
                }
            }
        }
    }
}
