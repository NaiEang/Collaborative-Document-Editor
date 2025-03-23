import java.io.*;
import java.net.Socket;
import java.nio.file.*;
import java.util.Scanner;

public class EditorClientPAD {
    private static final String FILES_FOLDER = "ClientNotes";
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 12345;
    
    public static void main(String[] args) {
        try (Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
             BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
             Scanner scanner = new Scanner(System.in)) {
            
            System.out.println("Connected to Document Editing Server");
            
            // Authentication (Login/Register)
            while (true) {
                System.out.println("1. Register\n2. Login\n3. Exit");
                String choice = scanner.nextLine().trim();
                writer.println(choice);
                
                if (choice.equals("3")) {
                    System.out.println("Exiting...");
                    return;
                }
                
                System.out.print("Enter username: ");
                String username = scanner.nextLine().trim();
                writer.println(username);
                
                System.out.print("Enter password: ");
                String password = scanner.nextLine().trim();
                writer.println(password);
                
                String response = reader.readLine();
                System.out.println(response);
                
                if (response.equals("Login successful") || response.equals("Registration successful")) {
                    break;
                }
            }
            
            // Main menu loop
            while (true) {
                System.out.println("\n1. Create Note\n2. Save Note\n3. Load Note\n4. Edit Note\n5. Delete Note\n6. Exit");
                String option = scanner.nextLine().trim();
                writer.println(option);
                
                if (option.equals("6")) {
                    System.out.println("Exiting...");
                    break;
                }
                
                switch (option) {
                    case "1": // Create note
                        System.out.print("Enter note title: ");
                        String newTitle = scanner.nextLine().trim();
                        writer.println(newTitle);
                        System.out.println(reader.readLine());
                        break;
                    
                    case "2": // Save note
                        System.out.print("Enter filename: ");
                        String saveFile = scanner.nextLine().trim();
                        writer.println(saveFile);
                        System.out.println(reader.readLine());
                        break;
                    
                    case "3": // Load note
                        System.out.print("Enter filename: ");
                        String loadFile = scanner.nextLine().trim();
                        writer.println(loadFile);
                        System.out.println(reader.readLine());
                        break;
                    
                    case "4": // Edit note (append text, retain original content)
                        System.out.print("Enter filename to edit: ");
                        String editFile = scanner.nextLine().trim();
                        writer.println(editFile);
                        
                        String filePath = FILES_FOLDER + File.separator + editFile;
                        Path path = Paths.get(filePath);
                        
                        if (!Files.exists(path)) {
                            System.out.println("File does not exist.");
                            break;
                        }
                        
                        System.out.println("Enter new content (type END on a new line to finish):");
                        StringBuilder newContent = new StringBuilder();
                        while (true) {
                            String line = scanner.nextLine();
                            if (line.equalsIgnoreCase("END")) break;
                            newContent.append(line).append("\n");
                        }
                        
                        // Read current content
                        String oldContent = Files.readString(path);
                        
                        // Remove last line (previous modification date)
                        String[] lines = oldContent.split("\n");
                        StringBuilder updatedContent = new StringBuilder();
                        for (int i = 0; i < lines.length - 1; i++) {
                            updatedContent.append(lines[i]).append("\n");
                        }
                        
                        // Append new content
                        updatedContent.append(newContent);
                        
                        // Append new modification date
                        updatedContent.append("Last Modified: ").append(java.time.LocalDateTime.now()).append("\n");
                        
                        // Write back to file
                        Files.write(path, updatedContent.toString().getBytes());
                        System.out.println("Note updated successfully!");
                        break;
                    
                    case "5": // Delete note
                        System.out.print("Enter filename to delete: ");
                        String deleteFile = scanner.nextLine().trim();
                        writer.println(deleteFile);
                        System.out.println(reader.readLine());
                        break;
                    
                    default:
                        System.out.println("Invalid option. Try again.");
                        break;
                }
            }
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}
