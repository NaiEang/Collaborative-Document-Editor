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
            System.out.print("Your choice: ");
            String authChoice = scanner.nextLine();
            writer.println(authChoice);

            if (authChoice.equalsIgnoreCase("register")) {
                System.out.println(reader.readLine()); // "Enter new username:"
                System.out.print("Username: ");
                writer.println(scanner.nextLine());
                System.out.println(reader.readLine()); // "Enter new password:"
                System.out.print("Password: ");
                writer.println(scanner.nextLine());
                System.out.println(reader.readLine()); // Registration success message
            } else if (authChoice.equalsIgnoreCase("login")) {
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
            } else {
                System.out.println(reader.readLine()); // "Invalid option. Disconnecting."
                socket.close();
                return;
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
                System.out.print("Choose an option: ");

                int choice = Integer.parseInt(scanner.nextLine());
                String line;

                switch (choice) {
                    case 1:
                        writer.println("CREATE");
                        System.out.println(reader.readLine()); // Prompt for note content
                        noteContent.setLength(0); // Clear previous content
                        while (!(line = scanner.nextLine()).equals("END")) {
                            writer.println(line);
                            noteContent.append(line).append("\n");
                        }
                        writer.println("END");
                        System.out.println(reader.readLine()); // Confirmation message
                        break;

                    case 2:
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

                    case 3:
                        writer.println("LOAD");
                        System.out.println(reader.readLine()); // "Enter filename to load:"
                        System.out.print("Filename: ");
                        writer.println(FILES_FOLDER + File.separator + scanner.nextLine());
                        String response = reader.readLine(); // Confirmation or error message
                        System.out.println(response);
                        if (!response.startsWith("Error")) {
                            while (true) {
                                String noteLine = reader.readLine();
                                if (noteLine == null || noteLine.equals("END"))
                                    break;
                                System.out.println(noteLine);
                            }
                        }
                        break;

                    case 4:
                        writer.println("EDIT");
                        System.out.println(reader.readLine()); // "Enter filename to edit:"
                        System.out.print("Filename: ");
                        writer.println(FILES_FOLDER + File.separator + scanner.nextLine());

                        //Display the current note content
                        while (!(line = reader.readLine()).equals("END")) {
                            System.out.println(line);
                        }

                        System.out.println(reader.readLine()); // "Enter your changes..." prompt
                        StringBuilder updatedContent = new StringBuilder();
                        while (!(line = scanner.nextLine()).equals("END")) {
                            writer.println(line);
                        }
                        writer.println("END");

                        //Send update last modified
                        String lastModifiedEdit = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
                        writer.println(lastModifiedEdit); // Send the timestamp to the server

                        System.out.println(reader.readLine()); // Edit confirmation
                        break;

                    case 5:
                        writer.println("DELETE");
                        System.out.println(reader.readLine()); // "Enter filename to delete:"
                        System.out.print("Filename: ");
                        writer.println(FILES_FOLDER + File.separator + scanner.nextLine());
                        System.out.println(reader.readLine()); // Delete confirmation
                        break;

                    case 6:
                        writer.println("EXIT");
                        System.out.println(reader.readLine()); // Goodbye message
                        return;

                    default:
                        System.out.println("Invalid choice. Try again.");
                        break;
                }
            }
        } catch (IOException e) {
            System.out.println("Error connecting to server: " + e.getMessage());
        }
    }
}