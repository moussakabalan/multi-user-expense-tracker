import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Day 6 (Person B):
 * - Login screen (username field)
 * - Dashboard screen (Add Expense / View Expenses buttons)
 * - Scene switching between Login and Dashboard
 * NOTE: No real server calls yet. Only UI.
 */
public class Main extends Application {

    private Stage primaryStage;

    // Two screens (scenes)
    private Scene loginScene;
    private Scene dashboardScene;

    // Store the username after login
    private String currentUsername;

    // Label on the dashboard that shows "Welcome, <username>!"
    private Label welcomeLabel;

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;

        // Build both screens
        loginScene = createLoginScene();
        dashboardScene = createDashboardScene();

        // Start app on the login screen
        primaryStage.setTitle("Multi-User Expense Tracker");
        primaryStage.setScene(loginScene);
        primaryStage.show();
    }

    // ---------- LOGIN SCREEN ----------
    private Scene createLoginScene() {
        Label titleLabel = new Label("Multi-User Expense Tracker");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        Label usernameLabel = new Label("Username:");
        TextField usernameField = new TextField();
        usernameField.setPromptText("Enter your username");

        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: red;");

        Button loginButton = new Button("Login");

        // What happens when you click Login
        loginButton.setOnAction(e -> {
            String username = usernameField.getText().trim();

            if (username.isEmpty()) {
                errorLabel.setText("Please enter a username.");
                return;
            }

            currentUsername = username;   // save username
            errorLabel.setText("");

            showDashboard();              // move to dashboard screen
        });

        VBox root = new VBox(10);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(20));
        root.getChildren().addAll(
                titleLabel,
                usernameLabel,
                usernameField,
                loginButton,
                errorLabel
        );

        return new Scene(root, 600, 400);
    }

    // ---------- DASHBOARD SCREEN ----------
    private Scene createDashboardScene() {
        welcomeLabel = new Label("Welcome!");
        welcomeLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        Button addExpenseButton = new Button("Add Expense");
        Button viewExpensesButton = new Button("View Expenses");
        Button logoutButton = new Button("Log Out");

        // Let buttons grow horizontally
        addExpenseButton.setMaxWidth(Double.MAX_VALUE);
        viewExpensesButton.setMaxWidth(Double.MAX_VALUE);
        logoutButton.setMaxWidth(Double.MAX_VALUE);

        // For Day 6: just print to console (no real behavior yet)
        addExpenseButton.setOnAction(e -> {
            System.out.println("[Dashboard] Add Expense clicked by: " + currentUsername);
        });

        viewExpensesButton.setOnAction(e -> {
            System.out.println("[Dashboard] View Expenses clicked by: " + currentUsername);
        });

        logoutButton.setOnAction(e -> {
            currentUsername = null;
            primaryStage.setScene(loginScene);
        });

        VBox root = new VBox(12);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(20));
        root.getChildren().addAll(
                welcomeLabel,
                addExpenseButton,
                viewExpensesButton,
                logoutButton
        );

        return new Scene(root, 600, 400);
    }

    // ---------- SWITCH TO DASHBOARD ----------
    private void showDashboard() {
        if (currentUsername != null && !currentUsername.isEmpty()) {
            welcomeLabel.setText("Welcome, " + currentUsername + "!");
        } else {
            welcomeLabel.setText("Welcome!");
        }
        primaryStage.setScene(dashboardScene);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
