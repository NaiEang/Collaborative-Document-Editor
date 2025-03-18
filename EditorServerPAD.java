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
    private StringBuilder noteContent = new StringBuilder(); // Make noteContent a class member

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
                if (inputLine.equals("CREATE")) {
                    out.println("Enter note content. Type 'END' to finish:");
                    noteContent.setLength(0); // Clear previous content
                    while (!(inputLine = in.readLine()).equals("END")) {
                        noteContent.append(inputLine).append("\n");
                    }
                    // Save noteContent to a file or database
                    out.println("Note created successfully.");
                } else if (inputLine.equals("SAVE")) {
                    out.println("Enter filename to save:");
                    String filename = in.readLine();
                    // Save the note content to the specified filename
                    try (FileWriter fileWriter = new FileWriter(filename)) {
                        fileWriter.write(noteContent.toString());
                        out.println("Note saved successfully.");
                    } catch (IOException e) {
                        out.println("Error saving note: " + e.getMessage());
                    }
                } else if (inputLine.equals("LOAD")) {
                    out.println("Enter filename to load:");
                    String filename = in.readLine();
                    // Load the note content from the specified filename
                    try (BufferedReader fileReader = new BufferedReader(new FileReader(filename))) {
                        out.println("Note content:");
                        String noteLine;
                        noteContent.setLength(0); // Clear previous content
                        while ((noteLine = fileReader.readLine()) != null) {
                            out.println(noteLine);
                            noteContent.append(noteLine).append("\n"); // Store loaded content
                        }
                        out.println("END");
                    } catch (IOException e) {
                        out.println("Error loading note: " + e.getMessage());
                    }
                } else if (inputLine.equals("EDIT")) {
                    out.println("Enter filename to edit:");
                    String filename = in.readLine();
                    // Load the current note content from the specified filename
                    try (BufferedReader fileReader = new BufferedReader(new FileReader(filename))) {
                        out.println("Current note content:");
                        String noteLine;
                        noteContent.setLength(0); // Clear previous content
                        while ((noteLine = fileReader.readLine()) != null) {
                            out.println(noteLine);
                            noteContent.append(noteLine).append("\n"); // Store loaded content
                        }
                        out.println("END");
                    } catch (IOException e) {
                        out.println("Error loading note: " + e.getMessage());
                    }
                    out.println("Enter your changes. Type 'END' to finish:");
                    while (!(inputLine = in.readLine()).equals("END")) {
                        noteContent.append(inputLine).append("\n");
                    }
                    // Save the edited note content
                    try (FileWriter fileWriter = new FileWriter(filename)) {
                        fileWriter.write(noteContent.toString());
                        out.println("Note edited successfully.");
                    } catch (IOException e) {
                        out.println("Error saving note: " + e.getMessage());
                    }
                } else if (inputLine.equals("EXIT")) {
                    out.println("Goodbye!");
                    break;
                } else {
                    out.println("Invalid command.");
                }
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