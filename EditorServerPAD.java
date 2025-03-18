import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

public class EditorServerPAD {
    private static final int PORT = 11111;

    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Usage: java EditorServerPAD <IPv4 address>");
            return;
        }

        String serverAddress = args[0];
        System.out.println("Starting server on: " + serverAddress);

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Editor Server started on port " + PORT);

            while (true) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    new ClientHandler(clientSocket).start();
                } catch (SocketException e) {
                    System.out.println("Socket exception: " + e.getMessage());
                } catch (IOException e) {
                    System.out.println("I/O error: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.out.println("Error starting server: " + e.getMessage());
        }
    }
}

class ClientHandler extends Thread {
    private Socket clientSocket;

    public ClientHandler(Socket socket) {
        this.clientSocket = socket;
    }

    public void run() {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {

            // Authentication Section
            out.println("Welcome to Collaborative Document Editor.");
            out.println("Type 'register' to register or 'login' to login:");

            String authChoice = in.readLine();
            if (authChoice.equalsIgnoreCase("register")) {
                out.println("Enter new username:");
                String username = in.readLine();
                out.println("Enter new password:");
                String password = in.readLine();
                // Here you would add code to save the new user credentials
                out.println("Registration successful. You can now login.");
            } else if (authChoice.equalsIgnoreCase("login")) {
                out.println("Enter username:");
                String username = in.readLine();
                out.println("Enter password:");
                String password = in.readLine();
                // Here you would add code to validate the user credentials
                boolean isValidUser = true; // Replace with actual validation
                if (isValidUser) {
                    out.println("Login successful.");
                } else {
                    out.println("Invalid username or password.");
                    clientSocket.close();
                    return;
                }
            } else {
                out.println("Invalid option. Disconnecting.");
                clientSocket.close();
                return;
            }

            // Main communication loop
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                System.out.println("Received: " + inputLine);
                out.println("Echo: " + inputLine);
            }
        } catch (IOException e) {
            System.out.println("Client handler I/O error: " + e.getMessage());
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                System.out.println("Error closing client socket: " + e.getMessage());
            }
        }
    }
}