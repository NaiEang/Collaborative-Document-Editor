import java.io.*;
import java.net.*;
import java.util.Scanner;

public class EditorClient {
    private static final String SERVER_ADDRESS = "localhost";  // Change to the server's IP if needed
    private static final int PORT = 12345;

    public static void main(String[] args) {
        try (
            Socket socket = new Socket(SERVER_ADDRESS, PORT);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            Scanner scanner = new Scanner(System.in)
        ) {
            // Start a thread to listen for incoming messages from the server
            new Thread(() -> {
                try {
                    String serverMessage;
                    while ((serverMessage = in.readLine()) != null) {
                        System.out.println(serverMessage);
                    }
                } catch (IOException e) {
                    System.out.println("Connection closed.");
                }
            }).start();

            System.out.println("Start typing. Type ':q' to exit.");

            // Read user input and send to server
            while (true) {
                String userInput = scanner.nextLine();
                if (userInput.equalsIgnoreCase(":q")) {
                    break;
                }
                out.println(userInput);
            }
        } catch (IOException e) {
            System.out.println("Unable to connect to server: " + e.getMessage());
        }
    }
}
