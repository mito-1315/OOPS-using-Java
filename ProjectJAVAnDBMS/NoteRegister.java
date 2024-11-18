import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class NoteRegister extends Application {

    private TextField userIdField;
    private PasswordField passwordField;
    private Button loginButton, signupButton, addNoteButton, logoutButton, backButton, saveEditButton;
    private VBox loginLayout, notesLayout, addNoteLayout, viewNoteLayout;
    private String currentUser;
    private Stage primaryStage;
    private ListView<String> notesListView;
    private int selectedNoteId;
    private ListView<String> noteListView;
    private Button viewNoteButton;
    private String selectedNoteTitle; 
    private static final String DB_URL = "jdbc:mysql://localhost:3306/notes_register";
    private static final String DB_USERNAME = "root";
    private static final String DB_PASSWORD = "mithu133";

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        primaryStage.setTitle("Notes Application");

        // Login Page
        loginLayout = createLoginPage();
        
        // Set the scene for the login page
        Scene loginScene = new Scene(loginLayout, 400, 300);
        loginScene.setFill(Color.web("#f5f5dc"));
        primaryStage.setScene(loginScene);
        primaryStage.show();
    }

    private VBox createLoginPage() {
        VBox layout = new VBox(10);
        layout.setStyle("-fx-background-color: #f5f5dc; -fx-padding: 20;");

        Label userIdLabel = new Label("UserID:");
        userIdLabel.setFont(new Font("Arial", 16));
        userIdField = new TextField();
        userIdField.setFont(new Font("Arial", 14));
        
        Label passwordLabel = new Label("Password:");
        passwordLabel.setFont(new Font("Arial", 16));
        passwordField = new PasswordField();
        passwordField.setFont(new Font("Arial", 14));
        
        loginButton = new Button("Login");
        loginButton.setStyle("-fx-background-color: #8b4513; -fx-text-fill: white;");
        loginButton.setFont(new Font("Arial", 16));
        loginButton.setOnAction(e -> loginUser());

        signupButton = new Button("Sign Up");
        signupButton.setStyle("-fx-background-color: #8b4513; -fx-text-fill: white;");
        signupButton.setFont(new Font("Arial", 16));
        signupButton.setOnAction(e -> signupUser());

        layout.getChildren().addAll(userIdLabel, userIdField, passwordLabel, passwordField, loginButton, signupButton);
        return layout;
    }

    private void loginUser() {
        String userId = userIdField.getText();
        String password = passwordField.getText();

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD)) {
            String sql = "SELECT * FROM users WHERE user = ? AND password = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, userId);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                currentUser = userId;
                showNotesPage();
            } else {
                showAlert("Login Failed", "Invalid UserID or Password");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
    private void showAddNotePage() {
        // Create a layout for adding a new note
        VBox layout = new VBox(10);
        layout.setStyle("-fx-background-color: #f5f5dc; -fx-padding: 20;");
        
        // Add title and content fields
        Label titleLabel = new Label("Title:");
        titleLabel.setFont(new Font("Arial", 16));
        TextField titleField = new TextField();
        titleField.setFont(new Font("Arial", 14));
        
        Label contentLabel = new Label("Content:");
        contentLabel.setFont(new Font("Arial", 16));
        TextArea contentArea = new TextArea();
        contentArea.setFont(new Font("Arial", 14));
        contentArea.setWrapText(true);
        
        // Button to save the new note
        Button saveNoteButton = new Button("Save Note");
        saveNoteButton.setStyle("-fx-background-color: #8b4513; -fx-text-fill: white;");
        saveNoteButton.setFont(new Font("Arial", 16));
        saveNoteButton.setOnAction(e -> saveNewNote(titleField.getText(), contentArea.getText()));
        
        // Back button to return to the notes page
        Button backButton = new Button("Back");
        backButton.setStyle("-fx-background-color: #8b4513; -fx-text-fill: white;");
        backButton.setOnAction(e -> showNotesPage());
        
        layout.getChildren().addAll(titleLabel, titleField, contentLabel, contentArea, saveNoteButton, backButton);
        
        // Show the Add Note Page scene
        Scene addNoteScene = new Scene(layout, 400, 400);
        primaryStage.setScene(addNoteScene);
    }

    private void saveNewNote(String title, String content) {
        if (title.isEmpty() || content.isEmpty()) {
            showAlert("Error", "Both title and content must be filled out.");
            return;
        }

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD)) {
            String sql = "INSERT INTO notes (user, title, content) VALUES (?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, currentUser);
            stmt.setString(2, title);
            stmt.setString(3, content);
            stmt.executeUpdate();
            showAlert("Note Saved", "Your note has been saved.");
            showNotesPage();  // Go back to notes list after saving
        } catch (SQLException ex) {
            ex.printStackTrace();
            showAlert("Error", "An error occurred while saving the note.");
        }
    }

    private void signupUser() {
        String userId = userIdField.getText();
        String password = passwordField.getText();

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD)) {
            String sql = "INSERT INTO users (user, password) VALUES (?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, userId);
            stmt.setString(2, password);
            stmt.executeUpdate();
            showAlert("Sign Up Success", "User registered successfully!");
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
    private List<String> getNoteTitles() {
        List<String> titles = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD)) {
            String sql = "SELECT title FROM notes WHERE user = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, currentUser);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                titles.add(rs.getString("title"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return titles;
    }

    private void showEditNotePage() {
    // Create a layout for editing the note
    VBox layout = new VBox(10);
    layout.setStyle("-fx-background-color: #f5f5dc; -fx-padding: 20;");

    // Retrieve the content of the note to be edited
    String currentContent = getNoteContent(selectedNoteTitle);

    Label titleLabel = new Label("Edit Note: " + selectedNoteTitle);
    titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

    TextArea contentArea = new TextArea(currentContent);
    contentArea.setWrapText(true);

    Button saveButton = new Button("Save Edit");
    saveButton.setStyle("-fx-background-color: #8b4513; -fx-text-fill: white;");
    saveButton.setOnAction(e -> saveEditedNote(contentArea.getText()));

    Button backButton = new Button("Back");
    backButton.setStyle("-fx-background-color: #8b4513; -fx-text-fill: white;");
    backButton.setOnAction(e -> openNotePage());

    layout.getChildren().addAll(titleLabel, contentArea, saveButton, backButton);

    // Show the edit page scene
    Scene editNoteScene = new Scene(layout, 400, 400);
    primaryStage.setScene(editNoteScene);
}


    private void openNotePage() {
        // Retrieve the selected note content by title
        String content = getNoteContent(selectedNoteTitle);

        // Show the content of the note
        VBox layout = new VBox(10);
        layout.setStyle("-fx-background-color: #f5f5dc; -fx-padding: 20;");

        Label titleLabel = new Label("Title: " + selectedNoteTitle);
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        TextArea contentArea = new TextArea(content);
        contentArea.setWrapText(true);
        contentArea.setEditable(false); // Set the TextArea to be non-editable

        Button editButton = new Button("Edit Note");
        editButton.setStyle("-fx-background-color: #8b4513; -fx-text-fill: white;");
        editButton.setOnAction(e -> showEditNotePage());

        Button backButton = new Button("Back");
        backButton.setStyle("-fx-background-color: #8b4513; -fx-text-fill: white;");
        backButton.setOnAction(e -> showNotesPage());

        layout.getChildren().addAll(titleLabel, contentArea, editButton, backButton);

        // Show the note page scene
        Scene notePageScene = new Scene(layout, 400, 400);
        primaryStage.setScene(notePageScene);
    }

    // Retrieve note content by title
    private String getNoteContent(String title) {
        String content = "";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD)) {
            String sql = "SELECT content FROM notes WHERE user = ? AND title = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, currentUser);
            stmt.setString(2, title);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                content = rs.getString("content");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return content;
    }

    private int getNoteId(String title) {
        int id = -1;
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD)) {
            String sql = "SELECT id FROM notes WHERE user = ? AND title = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, currentUser);
            stmt.setString(2, title);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                id = rs.getInt("id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return id;
    }


    private void showNotesPage() {
        VBox layout = new VBox(10);
        layout.setStyle("-fx-background-color: #f5f5dc; -fx-padding: 20;");

        noteListView = new ListView<>();
        noteListView.setItems(FXCollections.observableArrayList(getNoteTitles()));
        noteListView.setPrefHeight(300);
        noteListView.setStyle("-fx-font-size: 16px;");

        viewNoteButton = new Button("View Note");
        viewNoteButton.setStyle("-fx-background-color: #8b4513; -fx-text-fill: white;");
        viewNoteButton.setDisable(true);  // Initially disabled until a note is selected
        viewNoteButton.setOnAction(e -> openNotePage());

        // Enable the "View Note" button when a note is selected
        noteListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                selectedNoteTitle = newValue;
                selectedNoteId = getNoteId(selectedNoteTitle);  // Set the selected note ID
                viewNoteButton.setDisable(false);
            }
        });


        Button addNoteButton = new Button("Add New Note");
        addNoteButton.setStyle("-fx-background-color: #8b4513; -fx-text-fill: white;");
        addNoteButton.setOnAction(e -> showAddNotePage());

        Button logoutButton = new Button("Logout");
        logoutButton.setStyle("-fx-background-color: #8b4513; -fx-text-fill: white;");
        logoutButton.setOnAction(e -> showLoginPage());

        layout.getChildren().addAll(noteListView, viewNoteButton, addNoteButton, logoutButton);

        // Show the notes scene
        Scene notesScene = new Scene(layout, 400, 500);
        primaryStage.setScene(notesScene);
    }

    private VBox createNotesPage() {
        VBox layout = new VBox(20);
        layout.setStyle("-fx-background-color: #f5f5dc; -fx-padding: 20;");
        
        Label titleLabel = new Label("Your Notes");
        titleLabel.setFont(new Font("Arial", 24));
        titleLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #8b4513;");
        
        // ListView to display notes
        notesListView = new ListView<>();
        notesListView.setStyle("-fx-font-size: 14px;");
        loadNotes();

        // Add listener to handle click and view the selected note
        notesListView.setOnMouseClicked(event -> {
            String selectedNote = notesListView.getSelectionModel().getSelectedItem();
            if (selectedNote != null) {
                openNotePage();
            }
        });

        // Button to add new note
        addNoteButton = new Button("Add Note");
        addNoteButton.setStyle("-fx-background-color: #8b4513; -fx-text-fill: white;");
        addNoteButton.setOnAction(e -> showAddNotePage());

        logoutButton = new Button("Logout");
        logoutButton.setStyle("-fx-background-color: #8b4513; -fx-text-fill: white;");
        logoutButton.setOnAction(e -> showLoginPage());

        layout.getChildren().addAll(titleLabel, notesListView, addNoteButton, logoutButton);
        return layout;
    }

    private void loadNotes() {
        notesListView.getItems().clear();

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD)) {
            String sql = "SELECT id, title, content FROM notes WHERE user = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, currentUser);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int id = rs.getInt("id");
                String title = rs.getString("title");
                String content = rs.getString("content");
                String preview = content.length() > 30 ? content.substring(0, 30) + "..." : content;
                String note = title + "\n" + preview;
                notesListView.getItems().add(note);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private VBox createViewNotePage() {
        VBox layout = new VBox(10);
        layout.setStyle("-fx-background-color: #f5f5dc; -fx-padding: 20;");

        Label titleLabel = new Label("View Note");
        titleLabel.setFont(new Font("Arial", 24));

        // Get the full content of the selected note
        TextArea noteContentArea = new TextArea();
        noteContentArea.setStyle("-fx-font-size: 14px;");
        noteContentArea.setWrapText(true);
        loadNoteContent(noteContentArea);

        // Edit button to modify the note
        saveEditButton = new Button("Save Edit");
        saveEditButton.setStyle("-fx-background-color: #8b4513; -fx-text-fill: white;");
        saveEditButton.setOnAction(e -> saveEditedNote(noteContentArea.getText()));

        // Back button
        backButton = new Button("Back");
        backButton.setStyle("-fx-background-color: #8b4513; -fx-text-fill: white;");
        backButton.setOnAction(e -> showNotesPage());

        layout.getChildren().addAll(titleLabel, noteContentArea, saveEditButton, backButton);
        return layout;
    }

    private void loadNoteContent(TextArea noteContentArea) {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD)) {
            String sql = "SELECT content FROM notes WHERE id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, selectedNoteId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                noteContentArea.setText(rs.getString("content"));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void saveEditedNote(String newContent) {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD)) {
            String sql = "UPDATE notes SET content = ? WHERE id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, newContent);
            stmt.setInt(2, selectedNoteId);
            stmt.executeUpdate();
            showAlert("Note Updated", "Your note has been updated successfully.");
            showNotesPage();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showLoginPage() {
        loginLayout = createLoginPage();
        Scene loginScene = new Scene(loginLayout, 400, 300);
        loginScene.setFill(Color.web("#f5f5dc"));
        primaryStage.setScene(loginScene);
    }
}
