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
                    System.out.println("New client connected: " + clientSocket.getInetAddress().getHostAddress());
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
    private StringBuilder noteContent = new StringBuilder();
    private String username; 

    public ClientHandler(Socket socket) {
        this.clientSocket = socket;
    }

    public void run() {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {

            // Authentication Section
            out.println("=== Welcome to Collaborative Document Editor. ===");
            out.println("Type 'register' to register or 'login' to login:");

            String authChoice = in.readLine().trim().toLowerCase();
            if (authChoice.equals("register")) {
                out.println("Enter new username:");
                username = in.readLine(); 
                out.println("Enter new password:");
                String password = in.readLine();
                
                if (AuthSystem.userExists(username)) {
                    out.println("Username already exists. Please choose another name.");
                } else {
                    String hashedPassword = AuthSystem.hashPassword(password);
                    AuthSystem.saveUser(username, hashedPassword);
                    out.println("Registration successful.");
                }
            } else if (authChoice.equals("login")) {
                out.println("Enter username:");
                username = in.readLine();
                out.println("Enter password:");
                String password = in.readLine();
                boolean isValidUser = AuthSystem.authenticateUser(username, password); 
                if (isValidUser) {
                    out.println("Login successful.");
                } else {
                    out.println("Invalid username or password.");
                    clientSocket.close();
                    return;
                }
            } else {
                out.println("Invalid option. Please restart and choose 'register' or 'login'.");
                clientSocket.close();
                return;
            }

            // Main communication loop
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                switch (inputLine) {
                    case "CREATE":
                        handleCreateCommand(in, out);
                        break;
                    case "SAVE":
                        handleSaveCommand(in, out);
                        break;
                    case "LOAD":
                        handleLoadCommand(in, out);
                        break;
                    case "EDIT":
                        handleEditCommand(in, out);
                        break;
                    case "DELETE":
                        handleDeleteCommand(in, out);
                        break;
                    case "EXIT":
                        out.println("Goodbye!");
                        return;
                    default:
                        out.println("Invalid command. Please try again.");
                        break;
                }
            }
        } catch (IOException e) {
            System.out.println("Error handling client: " + e.getMessage());
        } finally {
            try {
                clientSocket.close();
                System.out.println("Client disconnected: " + clientSocket.getInetAddress().getHostAddress());
            } catch (IOException e) {
                System.out.println("Error closing client socket: " + e.getMessage());
            }
        }   
    }
    
    private void handleCreateCommand(BufferedReader in, PrintWriter out) throws IOException {
        out.println("Enter note content. Type 'END' to finish:");
        noteContent.setLength(0); // Clear previous content
        
        String line;
        while (!(line = in.readLine()).equals("END")) {
            noteContent.append(line).append("\n");
        }
        
        out.println("Note created successfully.");
    }
    
    private void handleSaveCommand(BufferedReader in, PrintWriter out) throws IOException {
        out.println("Enter filename to save:");
        String filename = in.readLine();
        String lastModified = in.readLine(); // Read timestamp from client
        
        // Create directory if it doesn't exist
        File file = new File(filename);
        File parentDir = file.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }

        try (BufferedWriter fileWriter = new BufferedWriter(new FileWriter(file,true))) {
            fileWriter.write(noteContent.toString());
            fileWriter.write("\nLast Modified by " + username + ": " + lastModified + "\n");
            out.println("Note saved successfully.");
            System.out.println("Note saved to: " + filename);
        } catch (IOException e) {
            out.println("Error saving note: " + e.getMessage());
            System.out.println("Error saving note: " + e.getMessage());
        }
    }
    
    private void handleLoadCommand(BufferedReader in, PrintWriter out) throws IOException {
        out.println("Enter filename to load:");
        String filename = in.readLine();
        File file = new File(filename);
        
        if (!file.exists()) {
            out.println("Error: File not found.");
            return;
        }
        
        try (BufferedReader fileReader = new BufferedReader(new FileReader(filename))) {
            out.println("Note content:");
            String noteLine;
            noteContent.setLength(0); // Clear previous content
            while ((noteLine = fileReader.readLine()) != null) {
                out.println(noteLine);
                noteContent.append(noteLine).append("\n");
            }
            out.println("END");
            System.out.println("File loaded: " + filename);
        } catch (IOException e) {
            out.println("Error loading note: " + e.getMessage());
            System.out.println("Error loading note: " + e.getMessage());
        }
    }
    
    private void handleEditCommand(BufferedReader in, PrintWriter out) throws IOException {
        out.println("Enter filename to edit:");
        String filename = in.readLine();
        File file = new File(filename);

        if (file.exists()) {
            // Load the current note content
            // noteContent.setLength(0); // Clear previous content
            java.util.List<String> lines = new java.util.ArrayList<>(); // Declare and initialize 'lines'
            try (BufferedReader fileReader = new BufferedReader(new FileReader(filename))) {
                String noteLine;
                while ((noteLine = fileReader.readLine()) != null) {
                    if (!noteLine.startsWith("Last Modified by ")) {
                        lines.add(noteLine);
                        noteContent.append(noteLine).append("\n");
                    }
                }
            } catch (IOException e) {
                out.println("Error loading note: " + e.getMessage());
                System.out.println("Error loading note: " + e.getMessage());
                return;
            }
            out.println("Note content:");
            for (String line : lines) {
                out.println(line);
            }
            out.println("END"); // Signal end of current content
            
            out.println("Enter your changes. Type 'END' to finish:");
            
            // Create new content using input from client
            StringBuilder updatedContent = new StringBuilder();
            String inputLine;
            while (!(inputLine = in.readLine()).equals("END")) {
                updatedContent.append(inputLine).append("\n");
            }
            
            // Get last modified timestamp from client
            String lastModifiedEdit = in.readLine();
            
            if (updatedContent.length() == 0) {
                out.println("No changes made.");
            } else {
                //remove last line and add new modification date
                if(!lines.isEmpty()) {
                    lines.remove(lines.size() - 1);//delete last line
                }
                lines.add(updatedContent.toString());
                lines.add("Last Modified by "+username+": " + lastModifiedEdit);

                try (FileWriter fileWriter = new FileWriter(filename)) {
                    for (String line : lines) {
                        fileWriter.write(line + "\n");
                    }
                    noteContent = updatedContent; // Update the noteContent with new content
                    out.println("Note edited successfully.");
                    System.out.println("File edited: " + filename);
                } catch (IOException e) {
                    out.println("Error saving note: " + e.getMessage());
                    System.out.println("Error saving note: " + e.getMessage());
                }
            }
        } else {
            out.println("File not found.");
        }
    }
    
    private void handleDeleteCommand(BufferedReader in, PrintWriter out) throws IOException {
        out.println("Enter filename to delete:");
        String filename = in.readLine();
        File file = new File(filename);
        
        if (!file.exists()) {
            out.println("Error: File not found.");
            return;
        }
        
        if (file.delete()) {
            out.println("Note deleted successfully.");
            System.out.println("File deleted: " + filename);
        } else {
            out.println("Error deleting note.");
            System.out.println("Error deleting file: " + filename);
        }
    }
}