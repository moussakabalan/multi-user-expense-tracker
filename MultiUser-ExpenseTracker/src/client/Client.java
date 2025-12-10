package client;

import common.Expense;
import common.ExpenseJson;
import common.ExpenseProtocol;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Client extends Application {
    private static final String HOST = "localhost";
    private static final int PORT = 5000;

    private Stage primaryStage;
    private ClientConnection connection;
    private String currentUsername;

    private Scene loginScene;
    private Scene dashboardScene;
    private Scene addExpenseScene;
    private Scene viewExpensesScene;

    private Label welcomeLabel;
    private TableView<Expense> expensesTable;
    private ObservableList<Expense> expensesData;

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;
        this.connection = new ClientConnection(HOST, PORT);

        loginScene = createLoginScene();
        dashboardScene = createDashboardScene();
        addExpenseScene = createAddExpenseScene();
        viewExpensesScene = createViewExpensesScene();

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

        Label statusLabel = new Label();
        statusLabel.setStyle("-fx-text-fill: blue;");

        Button loginButton = new Button("Login");

        loginButton.setOnAction(e -> {
            String username = usernameField.getText().trim();

            if (username.isEmpty()) {
                errorLabel.setText("Please enter a username.");
                return;
            }

            statusLabel.setText("Connecting to server...");
            errorLabel.setText("");

            if (connection.connect()) {
                currentUsername = username;
                statusLabel.setText("");
                errorLabel.setText("");
                showDashboard();
            } else {
                errorLabel.setText("Failed to connect to server. Make sure server is running.");
                statusLabel.setText("");
            }
        });

        VBox root = new VBox(10);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(20));
        root.getChildren().addAll(
                titleLabel,
                usernameLabel,
                usernameField,
                loginButton,
                statusLabel,
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

        addExpenseButton.setMaxWidth(Double.MAX_VALUE);
        viewExpensesButton.setMaxWidth(Double.MAX_VALUE);
        logoutButton.setMaxWidth(Double.MAX_VALUE);

        addExpenseButton.setOnAction(e -> primaryStage.setScene(addExpenseScene));
        viewExpensesButton.setOnAction(e -> {
            loadExpenses();
            primaryStage.setScene(viewExpensesScene);
        });
        logoutButton.setOnAction(e -> {
            connection.disconnect();
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

    private Scene createAddExpenseScene() {
        Label titleLabel = new Label("Add New Expense");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        Label amountLabel = new Label("Amount:");
        TextField amountField = new TextField();
        amountField.setPromptText("0.00");

        Label categoryLabel = new Label("Category:");
        ComboBox<String> categoryComboBox = new ComboBox<>();
        categoryComboBox.getItems().addAll(
                "Food", "Transport", "Entertainment", "Shopping",
                "Bills", "Healthcare", "Education", "Other"
        );
        categoryComboBox.setPromptText("Select Category");
        categoryComboBox.setEditable(true);

        Label dateLabel = new Label("Date:");
        DatePicker datePicker = new DatePicker();
        datePicker.setValue(LocalDate.now());

        Label noteLabel = new Label("Note:");
        TextArea noteField = new TextArea();
        noteField.setPromptText("Optional Note");
        noteField.setPrefRowCount(3);

        Label statusLabel = new Label();
        statusLabel.setStyle("-fx-text-fill: blue;");

        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: red;");

        Button submitButton = new Button("Add Expense");
        Button backButton = new Button("Back to Dashboard");

        submitButton.setOnAction(e -> {
            try {
                double amount = Double.parseDouble(amountField.getText().trim());
                String category = categoryComboBox.getValue();
                LocalDate date = datePicker.getValue();
                String note = noteField.getText().trim();

                if (category == null || category.trim().isEmpty()) {
                    errorLabel.setText("Please select a Category.");
                    return;
                }

                if (date == null) {
                    errorLabel.setText("Please select a Date.");
                    return;
                }

                Expense expense = new Expense(amount, category.trim(), date, note);
                String command = ExpenseProtocol.toServerMessage(expense, currentUsername);
                String response = connection.sendCommand(command);

                if (response != null && response.startsWith("SUCCESS")) {
                    statusLabel.setText("Expense added successfully!");
                    errorLabel.setText("");
                    amountField.clear();
                    categoryComboBox.setValue(null);
                    datePicker.setValue(LocalDate.now());
                    noteField.clear();
                    loadExpenses();
                } else {
                    errorLabel.setText("Error: " + response);
                    statusLabel.setText("");
                }
            } catch (NumberFormatException ex) {
                errorLabel.setText("Invalid amount. Please enter a number.");
                statusLabel.setText("");
            } catch (Exception ex) {
                errorLabel.setText("Invalid input: " + ex.getMessage());
                statusLabel.setText("");
            }
        });

        backButton.setOnAction(e -> primaryStage.setScene(dashboardScene));

        VBox root = new VBox(10);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(20));
        root.getChildren().addAll(
                titleLabel,
                amountLabel,
                amountField,
                categoryLabel,
                categoryComboBox,
                dateLabel,
                datePicker,
                noteLabel,
                noteField,
                submitButton,
                statusLabel,
                errorLabel,
                backButton
        );

        return new Scene(root, 600, 550);
    }

    private Scene createViewExpensesScene() {
        Label titleLabel = new Label("Your Expenses");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        expensesData = FXCollections.observableArrayList();
        expensesTable = new TableView<>(expensesData);
        expensesTable.setPrefHeight(300);
        expensesTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Expense, Double> amountColumn = new TableColumn<>("Amount");
        amountColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));
        amountColumn.setCellFactory(column -> new TableCell<Expense, Double>() {
            @Override
            protected void updateItem(Double amount, boolean empty) {
                super.updateItem(amount, empty);
                if (empty || amount == null) {
                    setText(null);
                } else {
                    setText(String.format("$%.2f", amount));
                }
            }
        });

        TableColumn<Expense, String> categoryColumn = new TableColumn<>("Category");
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));

        TableColumn<Expense, LocalDate> dateColumn = new TableColumn<>("Date");
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));

        TableColumn<Expense, String> noteColumn = new TableColumn<>("Note");
        noteColumn.setCellValueFactory(new PropertyValueFactory<>("note"));

        expensesTable.getColumns().addAll(amountColumn, categoryColumn, dateColumn, noteColumn);

        Label statusLabel = new Label();
        statusLabel.setStyle("-fx-text-fill: blue;");

        Button refreshButton = new Button("Refresh");
        Button backButton = new Button("Back to Dashboard");

        refreshButton.setOnAction(e -> loadExpenses());
        backButton.setOnAction(e -> primaryStage.setScene(dashboardScene));

        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.getChildren().addAll(refreshButton, backButton);

        VBox root = new VBox(10);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(20));
        root.getChildren().addAll(
                titleLabel,
                expensesTable,
                statusLabel,
                buttonBox
        );

        return new Scene(root, 700, 500);
    }

    private void showDashboard() {
        if (currentUsername != null && !currentUsername.isEmpty()) {
            welcomeLabel.setText("Welcome, " + currentUsername + "!");
        } else {
            welcomeLabel.setText("Welcome!");
        }
        primaryStage.setScene(dashboardScene);
    }

    private void loadExpenses() {
        if (expensesData == null) {
            return;
        }

        if (!connection.isConnected()) {
            expensesData.clear();
            return;
        }

        String command = "GET_EXPENSES|" + currentUsername;
        String response = connection.sendCommand(command);

        if (response == null || !response.startsWith("SUCCESS")) {
            expensesData.clear();
            return;
        }

        try {
            String[] parts = response.split("\\|");
            if (parts.length < 2) {
                expensesData.clear();
                return;
            }

            int count = Integer.parseInt(parts[1]);
            List<Expense> expenses = new ArrayList<>();

            for (int i = 0; i < count; i++) {
                String json = connection.receiveResponse();
                if (json != null) {
                    Expense expense = ExpenseJson.fromJson(json);
                    expenses.add(expense);
                }
            }

            expensesData.clear();
            expensesData.addAll(expenses);
        } catch (Exception e) {
            expensesData.clear();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}