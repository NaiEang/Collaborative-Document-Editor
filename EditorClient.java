import java.io.*;
import java.net.*;
import java.util.Scanner;


public class EditorClient {
    private static final String SERVER_ADDRESS = "192.168.240.255";  // Change to the server's IP 
    private static final int PORT = 12345;

    public static void main(String[] args) {
        try (
            Socket socket = new Socket(SERVER_ADDRESS, PORT);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            Scanner scanner = new Scanner(System.in);
            FileWriter fileWriter = new FileWriter("output.txt");
        ) {
            // Start a thread to listen for incoming messages from the server
            new Thread(() -> {
                try {
                    String serverMessage;
                    while ((serverMessage = in.readLine()) != null) {
                        System.out.println(serverMessage);

                        // Write the message to a file
                        fileWriter.write(serverMessage + "\n");
                        fileWriter.flush();
                    }
                } catch (IOException e) {
                    System.out.println("Connection closed.");
                }
            }).start();

            System.out.println("Start typing. Type ':q' to exit.");

            // Read user input and send to server
            StringBuilder fileContent= new StringBuilder();
            while (true) {
                String userInput = scanner.nextLine();
                if (userInput.equalsIgnoreCase(":q")) {
                    break;
                }else if(userInput.equalsIgnoreCase(":save")){
                    out.println(":save"); // Send the command to the server
                    out.println(fileContent.toString()); // Send the file content to the server
                    System.out.println("File saved.");
                    fileContent.setLength(0); // Clear the file content
                }
                out.println(userInput);
                fileContent.append(userInput).append("\n"); //Store content
            }
        } catch (IOException e) {
            System.out.println("Unable to connect to server: " + e.getMessage());
        }
    }
}
