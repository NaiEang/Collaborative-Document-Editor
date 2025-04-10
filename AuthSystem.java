import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class AuthSystem {
    private static final String USERS_FILE = "users.txt";

    public static boolean authenticateUser(String username, String password) {
        try (BufferedReader reader = new BufferedReader(new FileReader(USERS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] userData = line.split(":"); // Format: username:hashed_password
                if (userData.length >= 2 && userData[0].equals(username)) {
                    String storedHashedPassword = userData[1];
                    String enteredHashedPassword = hashPassword(password);
                    return storedHashedPassword.equals(enteredHashedPassword);
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading user data.");
        }
        return false;
    }

    public static boolean userExists(String username) {
        File file = new File(USERS_FILE);
        if (!file.exists()) {
            return false;
        }
        
        try (BufferedReader reader = new BufferedReader(new FileReader(USERS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(":");
                if (parts.length >= 1 && parts[0].equals(username)) {
                    return true;
                }
            }
        } catch (IOException e) {
            // File might not exist yet
            System.out.println("Error checking if user exists: " + e.getMessage());
        }
        return false;
    }

    public static void saveUser(String username, String hashedPassword) {
        // Create the directory if it doesn't exist
        File file = new File(USERS_FILE);
        File parentDir = file.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }
        
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(USERS_FILE, true))) {
            writer.write(username + ":" + hashedPassword);
            writer.newLine();
        } catch (IOException e) {
            System.out.println("Error saving user: " + e.getMessage());
        }
    }

    public static String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                hexString.append(String.format("%02x", b));
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}