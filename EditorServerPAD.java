import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class EditorServerPAD {
    private static final int PORT = 11111;

    public static void main(String[] args) {
        System.out.println("Starting server on port " + PORT);

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Editor Server started on port " + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connected: " + clientSocket.getInetAddress());
                new ClientHandler(clientSocket).start();
            }
        } catch (IOException e) {
            System.out.println("Error starting server: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

class ClientHandler extends Thread {
    private Socket socket;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try (
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter writer = new PrintWriter(socket.getOutputStream(), true)
        ) {
            writer.println("Welcome to Collaborative Document Editor.");
            writer.println("Type 'register' to register or 'login' to login:");

            String authChoice = reader.readLine();

            if ("register".equalsIgnoreCase(authChoice)) {
                writer.println("Enter new username:");
                String username = reader.readLine();
                writer.println("Enter new password:");
                String password = reader.readLine();
                // Dummy registration logic (replace with actual DB storage)
                writer.println("Registration successful for user: " + username);
            } else if ("login".equalsIgnoreCase(authChoice)) {
                writer.println("Enter username:");
                String username = reader.readLine();
                writer.println("Enter password:");
                String password = reader.readLine();

                // Dummy authentication logic (replace with actual DB check)
                if ("admin".equals(username) && "password".equals(password)) {
                    writer.println("Login successful. Welcome, " + username + "!");
                } else {
                    writer.println("Invalid credentials. Disconnecting...");
                    socket.close();
                    return;
                }
            } else {
                writer.println("Invalid option. Disconnecting...");
                socket.close();
                return;
            }

            // Handle client commands
            while (true) {
                String command = reader.readLine();
                if (command == null) break;

                switch (command) {
                    case "CREATE":
                        writer.println("Enter note content (type 'END' to finish):");
                        StringBuilder note = new StringBuilder();
                        String line;
                        while (!(line = reader.readLine()).equals("END")) {
                            note.append(line).append("\n");
                        }
                        writer.println("Note created successfully.");
                        break;

                    case "SAVE":
                        writer.println("Enter filename to save:");
                        String filename = reader.readLine();
                        writer.println("File '" + filename + "' saved successfully.");
                        break;

                    case "LOAD":
                        writer.println("Enter filename to load:");
                        String fileToLoad = reader.readLine();
                        writer.println("Loaded content of '" + fileToLoad + "':");
                        writer.println("Sample content...");
                        writer.println("END");
                        break;

                    case "EDIT":
                        writer.println("Current content: Sample content...");
                        writer.println("Enter your changes (type 'END' to finish):");
                        while (!(line = reader.readLine()).equals("END")) {
                            // Handle editing logic
                        }
                        writer.println("Note edited successfully.");
                        break;

                    case "EXIT":
                        writer.println("Goodbye!");
                        socket.close();
                        return;

                    default:
                        writer.println("Invalid command.");
                        break;
                }
            }
        } catch (IOException e) {
            System.out.println("Client disconnected: " + e.getMessage());
        }
    }
}
