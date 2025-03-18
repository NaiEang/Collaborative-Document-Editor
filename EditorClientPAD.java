import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class EditorClientPAD {
    private static final int PORT = 11111;
    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Usage: java EditorClientPAD <server-ip>");
            return;
        }

        String serverAddress = args[0];

        try (Socket socket = new Socket(serverAddress, PORT);
             BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter writer = new PrintWriter(socket.getOutputStream(), true)) {

            System.out.println("Connected to Editor Server at " + serverAddress);

            // --- Authentication Section ---
            System.out.println(reader.readLine());
            System.out.println(reader.readLine());
            System.out.print("Your choice: ");
            String authChoice = scanner.nextLine();
            writer.println(authChoice);

            if (authChoice.equalsIgnoreCase("register")) {
                System.out.println(reader.readLine());
                System.out.print("Username: ");
                writer.println(scanner.nextLine());
                System.out.println(reader.readLine());
                System.out.print("Password: ");
                writer.println(scanner.nextLine());
                System.out.println(reader.readLine());
            } else if (authChoice.equalsIgnoreCase("login")) {
                System.out.println(reader.readLine());
                System.out.print("Username: ");
                writer.println(scanner.nextLine());
                System.out.println(reader.readLine());
                System.out.print("Password: ");
                writer.println(scanner.nextLine());
                String authResponse = reader.readLine();
                System.out.println(authResponse);
                if (authResponse.startsWith("Invalid")) {
                    return;
                }
            } else {
                System.out.println(reader.readLine());
                return;
            }
            // --- End Authentication ---

            // --- Main menu ---
            while (true) {
                System.out.println("\nConsole Notepad Client");
                System.out.println("1. Create new note");
                System.out.println("2. Save note");
                System.out.println("3. Load note");
                System.out.println("4. Edit note");
                System.out.println("5. Exit");
                System.out.print("Choose an option: ");

                int choice = Integer.parseInt(scanner.nextLine());

                switch (choice) {
                    case 1:
                        writer.println("CREATE");
                        System.out.println(reader.readLine());
                        String line;
                        while (!(line = scanner.nextLine()).equals("END")) {
                            writer.println(line);
                        }
                        writer.println("END");
                        System.out.println(reader.readLine());
                        break;

                    case 2:
                        writer.println("SAVE");
                        System.out.println(reader.readLine());
                        writer.println(scanner.nextLine());
                        System.out.println(reader.readLine());
                        break;

                    case 3:
                        writer.println("LOAD");
                        System.out.println(reader.readLine());
                        writer.println(scanner.nextLine());
                        String response = reader.readLine();
                        System.out.println(response);
                        if (!response.startsWith("Error")) {
                            while (!(line = reader.readLine()).equals("END")) {
                                System.out.println(line);
                            }
                        }
                        break;

                    case 4:
                        writer.println("EDIT");
                        String content;
                        while (!(content = reader.readLine()).equals("END")) {
                            System.out.println(content);
                        }
                        System.out.println(reader.readLine());
                        while (!(line = scanner.nextLine()).equals("END")) {
                            writer.println(line);
                        }
                        writer.println("END");
                        System.out.println(reader.readLine());
                        break;

                    case 5:
                        writer.println("EXIT");
                        System.out.println(reader.readLine());
                        return;

                    default:
                        System.out.println("Invalid choice.");
                        break;
                }
            }
        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
