import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

public class EditorClient2 {
    private static final String SERVER_ADDRESS = "192.168.241.95";  // Change as needed
    private static final int PORT = 12345;
    private static final String DIRECTORY = "C:\\Users\\n" + //
                "aiea\\OneDrive\\Desktop\\ITC\\IntroductionToSE\\collab\\Collaborative-Document-Editor"; // Change to your library path

    public static void main(String[] args) {
        // Ensure directory exists
        File dir = new File(DIRECTORY);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        // Generate filename (if editing, create a new version)
        String baseFilename = "SavedText";
        File file = new File(DIRECTORY, baseFilename + ".txt");

        if (file.exists()) {
            // Create a new file with a timestamp
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            file = new File(DIRECTORY, baseFilename + "_" + timestamp + ".txt");
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
                    int skipLines =2;
                    while ((serverMessage = in.readLine()) != null) {
                        System.out.println(serverMessage);
                        if (skipLines > 0) {
                        skipLines--;
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

