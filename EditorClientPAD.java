import java.io.*;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Scanner;
import java.util.Date;

public class EditorClientPAD {
    private static String SERVER_ADDRESS;
    private static final int PORT = 11111;
    private static final Scanner scanner = new Scanner(System.in);
    private static final String FILES_FOLDER = "Files";
    
    public static void main(String[] args) {
        File folder = new File(FILES_FOLDER);
        if (!folder.exists()) {
            folder.mkdir();
        }
        if (args.length == 0) {
            System.out.println("Usage: java EditorClientPAD <server-ip>");
            return;
        }

        SERVER_ADDRESS = args[0]; // Take IP address from command line

        try (Socket socket = new Socket(SERVER_ADDRESS, PORT);
             BufferedReader reader = new BufferedReader(
                     new InputStreamReader(socket.getInputStream()));
             PrintWriter writer = new PrintWriter(socket.getOutputStream(), true)) {

            System.out.println("Connected to Editor Server at " + SERVER_ADDRESS);

            // --- Authentication Section ---
            System.out.println(reader.readLine()); // "Welcome to Collaborative Document Editor."
            System.out.println(reader.readLine()); // "Type 'register' to register or 'login' to login:"
            
            String authChoice;
            boolean validAuthChoice = false;
            
            do {
                System.out.print("Your choice (register/login): ");
                authChoice = scanner.nextLine().trim().toLowerCase();
                
                if (authChoice.equals("register") || authChoice.equals("login")) {
                    validAuthChoice = true;
                } else {
                    System.out.println("Invalid choice. Please enter 'register' or 'login'.");
                }
            } while (!validAuthChoice);
            
            writer.println(authChoice);

            if (authChoice.equals("register")) {
                System.out.println(reader.readLine()); // "Enter new username:"
                System.out.print("Username: ");
                writer.println(scanner.nextLine());
                System.out.println(reader.readLine()); // "Enter new password:"
                System.out.print("Password: ");
                writer.println(scanner.nextLine());
                System.out.println(reader.readLine()); // Registration success/failure message
            } else {  // login
                System.out.println(reader.readLine()); // "Enter username:"
                System.out.print("Username: ");
                writer.println(scanner.nextLine());
                System.out.println(reader.readLine()); // "Enter password:"
                System.out.print("Password: ");
                writer.println(scanner.nextLine());
                String authResponse = reader.readLine();
                System.out.println(authResponse);
                if (authResponse.startsWith("Invalid")) {
                    socket.close();
                    return;
                }
            }
            // --- End Authentication ---

            StringBuilder noteContent = new StringBuilder();

            // Main menu loop
            while (true) {
                System.out.println("\nConsole Notepad Client");
                System.out.println("1. Create new note");
                System.out.println("2. Save note");
                System.out.println("3. Load note");
                System.out.println("4. Edit note");
                System.out.println("5. Delete note");
                System.out.println("6. Exit");
                
                int choice = 0;
                boolean validChoice = false;
                
                do {
                    System.out.print("Choose an option (1-6): ");
                    try {
                        String input = scanner.nextLine().trim();
                        choice = Integer.parseInt(input);
                        if (choice >= 1 && choice <= 6) {
                            validChoice = true;
                        } else {
                            System.out.println("Please enter a number between 1 and 6.");
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid input. Please enter a number.");
                    }
                } while (!validChoice);
                
                String line;

                switch (choice) {
                    case 1: // Create new note
                        writer.println("CREATE");
                        System.out.println(reader.readLine()); // Prompt for note content
                        noteContent.setLength(0); // Clear previous content
                        System.out.println("Enter note content (type 'END' to finish):");
                        while (!(line = scanner.nextLine()).equals("END")) {
                            writer.println(line);
                            noteContent.append(line).append("\n");
                        }
                        writer.println("END");
                        System.out.println(reader.readLine()); // Confirmation message
                        break;

                    case 2: // Save note
                        writer.println("SAVE");
                        System.out.println(reader.readLine()); // "Enter filename to save:"
                        System.out.print("Filename: ");
                        String filename = scanner.nextLine();

                        writer.println(FILES_FOLDER + File.separator + filename);
                        
                        //Send last modified to server
                        String lastModified = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
                        writer.println(lastModified); // Send the timestamp to the server
                        System.out.println(reader.readLine()); // Save confirmation
                        break;

                    case 3: // Load note
                        writer.println("LOAD");
                        System.out.println(reader.readLine()); // "Enter filename to load:"
                        System.out.print("Filename: ");
                        writer.println(FILES_FOLDER + File.separator + scanner.nextLine());
                        String response = reader.readLine(); // Confirmation or error message
                        System.out.println(response);
                        
                        if (!response.startsWith("Error")) {
                            noteContent.setLength(0); // Clear previous content
                            System.out.println("--- Note Content ---");
                            while (true) {
                                String noteLine = reader.readLine();
                                if (noteLine == null || noteLine.equals("END"))
                                    break;
                                System.out.println(noteLine);
                                noteContent.append(noteLine).append("\n");
                            }
                            System.out.println("-------------------");
                        }
                        break;

                    case 4: // Edit note
                        writer.println("EDIT");
                        System.out.println(reader.readLine()); // "Enter filename to edit:"
                        System.out.print("Filename: ");
                        writer.println(FILES_FOLDER + File.separator + scanner.nextLine());

                        //Display the current note content
                        StringBuilder existingContent = new StringBuilder();
                        System.out.println("--- Current Content ---");
                        while (true) {
                            line = reader.readLine();
                            if (line == null || line.equals("END"))
                                break;
                            System.out.println(line);
                            existingContent.append(line).append("\n");
                        }
                        System.out.println("---------------------");

                        System.out.println(reader.readLine()); // "Enter your changes..." prompt
                        System.out.println("Enter your updated content (type 'END' to finish):");
                        StringBuilder updatedContent = new StringBuilder();
                        while (!(line = scanner.nextLine()).equals("END")) {
                            writer.println(line);
                            updatedContent.append(line).append("\n");
                        }
                        writer.println("END");

                        //Send update last modified
                        String lastModifiedEdit = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
                        writer.println(lastModifiedEdit); // Send the timestamp to the server
                        
                        // Update noteContent with new content
                        if (updatedContent.length() > 0) {
                            noteContent = updatedContent;
                        }

                        System.out.println(reader.readLine()); // Edit confirmation
                        break;

                    case 5: // Delete note
                        writer.println("DELETE");
                        System.out.println(reader.readLine()); // "Enter filename to delete:"
                        System.out.print("Filename: ");
                        writer.println(FILES_FOLDER + File.separator + scanner.nextLine());
                        System.out.println(reader.readLine()); // Delete confirmation
                        break;

                    case 6: // Exit
                        writer.println("EXIT");
                        System.out.println(reader.readLine()); // Goodbye message
                        return;
                }
            }
        } catch (IOException e) {
            System.out.println("Error connecting to server: " + e.getMessage());
        }
    }
}