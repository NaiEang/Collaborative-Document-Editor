import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

public class EditorClient3 {
    private static final String SERVER_ADDRESS = "172.20.10.2";  // Change as needed
    private static final int PORT = 12345;

    public static void main(String[] args) {
        // Get user's default Documents directory
        String userHome = System.getProperty("user.home");
        String documentsPath = userHome + File.separator + "Documents";
        File dir = new File(documentsPath);

        // Ensure directory exists
        if (!dir.exists()) {
            dir.mkdirs();
        }

        // Generate filename
        String baseFilename = "SavedText";
        File file = new File(dir, baseFilename + ".txt");

        if (file.exists()) {
            // Create a new file with a timestamp
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            file = new File(dir, baseFilename + "_" + timestamp + ".txt");
        }

        try (
            Socket socket = new Socket(SERVER_ADDRESS, PORT);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            Scanner scanner = new Scanner(System.in);
            BufferedWriter writer = new BufferedWriter(new FileWriter(file))
        ) {
            System.out.println("Start typing. Type ':q' to exit.");

            // Start a thread to listen for messages from the server
            new Thread(() -> {
                try {
                    String serverMessage;
                    int skipLines = 2; // Skip first two lines
                    while ((serverMessage = in.readLine()) != null) {
                        System.out.println(serverMessage); // Show message in console
                        if (skipLines > 0) {
                            skipLines--; // Skip first two lines in file
                            continue;
                        }
                        writer.write(serverMessage);
                        writer.newLine();
                        writer.flush();
                    }
                } catch (IOException e) {
                    System.out.println("Connection closed.");
                }
            }).start();

            // Read user input and send to server
            while (true) {
                String userInput = scanner.nextLine();
                if (userInput.equalsIgnoreCase(":q")) {
                    break;
                }
                out.println(userInput);
                writer.write(userInput);
                writer.newLine();
                writer.flush();
            }

            System.out.println("Text saved to: " + file.getAbsolutePath());

        } catch (IOException e) {
            System.out.println("Unable to connect to server: " + e.getMessage());
        }
    }
}
