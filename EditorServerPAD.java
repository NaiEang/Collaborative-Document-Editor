import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

public class EditorServerPAD {
    private static final int PORT = 11111;

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Editor Server started on port " + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                new ClientHandler(clientSocket).start();
            }
        } catch (IOException e) {
            System.out.println("Error starting server: " + e.getMessage());
        }
    }
}

class ClientHandler extends Thread {
    private Socket socket;
    private String currentFilename;
    private StringBuilder currentNote = new StringBuilder();
    private String username; // Authenticated username

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(socket.getInputStream()));
             PrintWriter writer = new PrintWriter(socket.getOutputStream(), true)) {

            // --- Authentication Section ---
            writer.println("Welcome to Collaborative Document Editor.");
            writer.println("Type 'register' to register or 'login' to login:");
            String authChoice = reader.readLine();
            if (authChoice.equalsIgnoreCase("register")) {
                writer.println("Enter new username:");
                String newUsername = reader.readLine();
                if (AuthSystem.userExists(newUsername)) {
                    writer.println("Username already exists. Disconnecting.");
                    socket.close();
                    return;
                }
                writer.println("Enter new password:");
                String newPassword = reader.readLine();
                String hashedPassword = AuthSystem.hashPassword(newPassword);
                AuthSystem.saveUser(newUsername, hashedPassword);
                writer.println("Registration successful! You are now logged in as " + newUsername);
                username = newUsername;
            } else if (authChoice.equalsIgnoreCase("login")) {
                writer.println("Enter username:");
                String loginUsername = reader.readLine();
                writer.println("Enter password:");
                String loginPassword = reader.readLine();
                if (AuthSystem.authenticateUser(loginUsername, loginPassword)) {
                    writer.println("Login successful! Welcome, " + loginUsername);
                    username = loginUsername;
                } else {
                    writer.println("Invalid username or password. Disconnecting.");
                    socket.close();
                    return;
                }
            } else {
                writer.println("Invalid option. Disconnecting.");
                socket.close();
                return;
            }
            // --- End Authentication ---

            // Main command loop
            String command;
            while ((command = reader.readLine()) != null) {
                switch (command) {
                    case "CREATE":
                        writer.println("Enter your note (type 'END' on a new line to finish):");
                        currentNote.setLength(0);
                        String line;
                        while (!(line = reader.readLine()).equals("END")) {
                            currentNote.append(line).append(System.lineSeparator());
                        }
                        writer.println("Note created.");
                        break;

                    case "SAVE":
                        writer.println("Enter filename to save:");
                        currentFilename = reader.readLine();
                        try (BufferedWriter fileWriter = new BufferedWriter(new FileWriter(currentFilename))) {
                            fileWriter.write(currentNote.toString());
                            writer.println("Note saved to " + currentFilename);
                        } catch (IOException e) {
                            writer.println("Error saving note: " + e.getMessage());
                        }
                        break;

                    case "LOAD":
                        writer.println("Enter filename to load:");
                        currentFilename = reader.readLine();
                        currentNote.setLength(0);

                        try (BufferedReader fileReader = new BufferedReader(new FileReader(currentFilename))) {
                            while ((line = fileReader.readLine()) != null) {
                                currentNote.append(line).append(System.lineSeparator());
                            }
                            writer.println("Note loaded from " + currentFilename);

                            // Send note line by line
                            BufferedReader noteReader = new BufferedReader(new StringReader(currentNote.toString()));
                            String noteLine;
                            while ((noteLine = noteReader.readLine()) != null) {
                                writer.println(noteLine);
                            }
                            writer.println("END"); // Termination signal
                        } catch (IOException e) {
                            writer.println("Error loading note: " + e.getMessage());
                        }
                        break;

                    case "EDIT":
                        if (currentNote.length() == 0) {
                            writer.println("No note loaded. Please load a note first.");
                        } else {
                            writer.println("Current note:");
                            BufferedReader noteReader = new BufferedReader(new StringReader(currentNote.toString()));
                            String noteLine;
                            while ((noteLine = noteReader.readLine()) != null) {
                                writer.println(noteLine);
                            }
                            writer.println("END"); // Signal end of current note
                            writer.println("Enter your changes (type 'END' on a new line to finish):");
                            StringBuilder newNote = new StringBuilder();
                            while (!(line = reader.readLine()).equals("END")) {
                                newNote.append(line).append(System.lineSeparator());
                            }
                            currentNote = newNote;
                            writer.println("Note edited.");
                        }
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
        } catch (SocketException se) {
            System.out.println("Client disconnected: " + se.getMessage());
        } catch (IOException e) {
            System.out.println("Client connection error: " + e.getMessage());
        }
    }
}
