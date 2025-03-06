
import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class EditorClientPAD {
    private static final String SERVER_ADDRESS = "192.168.241.95";
    private static final int PORT = 11111;
    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        try (Socket socket = new Socket(SERVER_ADDRESS, PORT);
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter writer = new PrintWriter(socket.getOutputStream(), true)) {

            System.out.println("Connected to Editor Server");

            while (true) {
                System.out.println("\nConsole Notepad Client");
                System.out.println("1. Create new note");
                System.out.println("2. Save note");
                System.out.println("3. Load note");
                System.out.println("4. Edit note");
                System.out.println("5. Exit");
                System.out.print("Choose an option: ");

                int choice = scanner.nextInt();
                scanner.nextLine(); // Consume newline

                switch (choice) {
                    case 1:
                        writer.println("CREATE");
                        System.out.println(reader.readLine()); // Server prompt

                        String line;
                        while (!(line = scanner.nextLine()).equals("END")) {
                            writer.println(line);
                        }
                        writer.println("END");
                        System.out.println(reader.readLine()); // Note created message
                        break;

                    case 2:
                        writer.println("SAVE");
                        System.out.print("Enter filename to save: ");
                        writer.println(scanner.nextLine());
                        System.out.println(reader.readLine()); // Save confirmation
                        break;

                    case 3:
                        writer.println("LOAD");
                        System.out.print("Enter filename to load: ");
                        writer.println(scanner.nextLine());

                        String response = reader.readLine(); // Read confirmation or error message
                        System.out.println(response);

                        if (!response.startsWith("Error")) { // If there's no error, read the note
                            while (true) {
                                String noteLine = reader.readLine();
                                if (noteLine == null || noteLine.equals("END"))
                                    break; // Stop at "END"
                                System.out.println(noteLine);
                            }
                        }
                        break;

                    case 4:
                        writer.println("EDIT");
                        System.out.println(reader.readLine()); // Current note or error message
                        while (reader.ready()) {
                            System.out.println(reader.readLine());
                        }
                        System.out.println("Enter your changes (type 'END' on a new line to finish):");

                        while (!(line = scanner.nextLine()).equals("END")) {
                            writer.println(line);
                        }
                        writer.println("END");
                        System.out.println(reader.readLine()); // Edit confirmation
                        break;

                    case 5:
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
