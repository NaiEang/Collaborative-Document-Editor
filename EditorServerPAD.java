
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class EditorServerPAD {
    private static final int PORT = 11111;

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Editor Server started on port " + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                new ClientHandler(clientSocket).start();
            }
        } catch (IOException e) {
            System.out.println("Error starting server: " + e.getMessage());
        }
    }
}

class ClientHandler extends Thread {
    private Socket socket;
    private String currentFilename;
    private StringBuilder currentNote = new StringBuilder();

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter writer = new PrintWriter(socket.getOutputStream(), true)) {

            String command;
            while ((command = reader.readLine()) != null) {
                switch (command) {
                    case "CREATE":
                        writer.println("Enter your note (type 'END' on a new line to finish):");
                        currentNote.setLength(0);
                        String line;
                        while (!(line = reader.readLine()).equals("END")) {
                            currentNote.append(line).append(System.lineSeparator());
                        }
                        writer.println("Note created.");
                        break;

                    case "SAVE":
                        writer.println("Enter filename to save:");
                        currentFilename = reader.readLine();
                        try (BufferedWriter fileWriter = new BufferedWriter(new FileWriter(currentFilename))) {
                            fileWriter.write(currentNote.toString());
                            writer.println("Note saved to " + currentFilename);
                        } catch (IOException e) {
                            writer.println("Error saving note: " + e.getMessage());
                        }
                        break;

                    case "LOAD":
                        writer.println("Enter filename to load:");
                        currentFilename = reader.readLine();
                        currentNote.setLength(0);

                        try (BufferedReader fileReader = new BufferedReader(new FileReader(currentFilename))) {
                            while ((line = fileReader.readLine()) != null) {
                                currentNote.append(line).append(System.lineSeparator());
                            }
                            writer.println("Note loaded from " + currentFilename);

                            // Send note line by line
                            BufferedReader noteReader = new BufferedReader(new StringReader(currentNote.toString()));
                            String noteLine;
                            while ((noteLine = noteReader.readLine()) != null) {
                                writer.println(noteLine);
                            }

                            writer.println("END"); // Send termination signal
                        } catch (IOException e) {
                            writer.println("Error loading note: " + e.getMessage());
                        }
                        break;

                    case "EDIT":
                        if (currentNote.length() == 0) {
                            writer.println("No note loaded. Please load a note first.");
                        } else {
                            writer.println("Current note:\n" + currentNote.toString());
                            writer.println("Enter your changes (type 'END' on a new line to finish):");
                            StringBuilder newNote = new StringBuilder();
                            while (!(line = reader.readLine()).equals("END")) {
                                newNote.append(line).append(System.lineSeparator());
                            }
                            currentNote = newNote;
                            writer.println("Note edited.");
                        }
                        break;

                    case "EXIT":
                        writer.println("Goodbye!");
                        socket.close();
                        return;

                    default:
                        writer.println("Invalid command.");
                        break;
                }
            }
        } catch (IOException e) {
            System.out.println("Client connection error: " + e.getMessage());
        }
    }
}
