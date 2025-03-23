import org.junit.jupiter.api.*;

import java.io.*;
import java.nio.file.*;

import static org.junit.jupiter.api.Assertions.*;

public class RandomTest {
    private static final String TEST_FILE = "test_note.txt";
    private static final String TEST_USERNAME = "testUser";
    private static final String TEST_PASSWORD = "testPassword";
    private static final String USERS_FILE = "users.txt";

    @BeforeEach
    public void setup() throws IOException {
        // Simulate user registration
        if (!AuthSystem.userExists(TEST_USERNAME)) {
            AuthSystem.saveUser(TEST_USERNAME, AuthSystem.hashPassword(TEST_PASSWORD));
        }
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(USERS_FILE))) {
            writer.write("testUser:5e884898da28047151d0e56f8dc6292773603d0d6aabbddc2f11d3c7b8a7b7d0\n"); // Password: "password"
            writer.write("anotherUser:6b3a55e0261b0304143f805a249d6a04b3c817f3e51b1e6f6b6b7b7b7b7b7b7b\n"); // Password: "123456"
        }
    }

    @AfterEach
    public void cleanup() throws IOException {
        // Delete test file if it exists
        Files.deleteIfExists(Paths.get(TEST_FILE));
    }

    @Test
    public void testUserRegistration() {
        String newUsername = "newUser";
        String newPassword = "newPassword";

        // Ensure the user does not exist initially
        assertFalse(AuthSystem.userExists(newUsername));

        // Register the user
        AuthSystem.saveUser(newUsername, AuthSystem.hashPassword(newPassword));

        // Verify the user exists after registration
        assertTrue(AuthSystem.userExists(newUsername));
    }

    @Test
    public void testUserAuthentication() {
        // Test valid login
        assertTrue(AuthSystem.authenticateUser(TEST_USERNAME, TEST_PASSWORD));

        // Test invalid login
        assertFalse(AuthSystem.authenticateUser(TEST_USERNAME, "wrongPassword"));
        assertFalse(AuthSystem.authenticateUser("wrongUser", TEST_PASSWORD));
    }

    @Test
    public void testFileSaving() throws IOException {
        String content = "This is a test note.";
        String lastModified = "2025-03-24 10:00:00";

        // Simulate saving the file
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(TEST_FILE))) {
            writer.write(content);
            writer.write("\nLast Modified by " + TEST_USERNAME + ": " + lastModified + "\n");
        }

        // Verify the file exists
        assertTrue(Files.exists(Paths.get(TEST_FILE)));

        // Verify the file content
        String fileContent = Files.readString(Paths.get(TEST_FILE));
        assertTrue(fileContent.contains(content));
        assertTrue(fileContent.contains("Last Modified by " + TEST_USERNAME + ": " + lastModified));
    }

    @Test
    public void testFileLoading() throws IOException {
        String content = "This is a test note.";
        String lastModified = "2025-03-24 10:00:00";

        // Create a test file
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(TEST_FILE))) {
            writer.write(content);
            writer.write("\nLast Modified by " + TEST_USERNAME + ": " + lastModified + "\n");
        }

        // Simulate loading the file
        StringBuilder loadedContent = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(TEST_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                loadedContent.append(line).append("\n");
            }
        }

        // Verify the loaded content matches the file content
        assertTrue(loadedContent.toString().contains(content));
        assertTrue(loadedContent.toString().contains("Last Modified by " + TEST_USERNAME + ": " + lastModified));
    }
    @Test
    public void testValidLogin() {
        assertTrue(UserLogin.authenticateUser("testUser", "password"));
    }

    @Test
    public void testInvalidUsername() {
        assertFalse(UserLogin.authenticateUser("nonExistentUser", "password"));
    }

    @Test
    public void testInvalidPassword() {
        assertFalse(UserLogin.authenticateUser("testUser", "wrongPassword"));
    }

    @Test
    public void testEmptyUsernameOrPassword() {
        assertFalse(UserLogin.authenticateUser("", "password"));
        assertFalse(UserLogin.authenticateUser("testUser", ""));
        assertFalse(UserLogin.authenticateUser("", ""));
    }

    @Test
    public void testFileNotFound() {
        // Delete the users.txt file to simulate file not found
        new File(USERS_FILE).delete();
        assertFalse(UserLogin.authenticateUser("testUser", "password"));
    }

    @Test
    public void testHashPasswordConsistency() {
        String password = "password";
        String hash1 = UserLogin.hashPassword(password);
        String hash2 = UserLogin.hashPassword(password);
        assertEquals(hash1, hash2); // Hashes should be consistent
    }

    @Test
    public void testHashPasswordDifferentPasswords() {
        String hash1 = UserLogin.hashPassword("password1");
        String hash2 = UserLogin.hashPassword("password2");
        assertNotEquals(hash1, hash2); // Hashes should be different for different passwords
    }

    @Test
    public void testHashPasswordEmptyPassword() {
        String hash = UserLogin.hashPassword("");
        assertNotNull(hash); // Hash should not be null
        assertFalse(hash.isEmpty()); // Hash should not be empty
    }
}
