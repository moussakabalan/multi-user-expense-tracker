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
import javafx.scene.layout.BorderPane;
import javafx.scene.chart.PieChart;
import javafx.stage.Stage;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert.AlertType;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private PieChart pieChart;

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;
        this.connection = new ClientConnection(HOST, PORT);

        loginScene = createLoginScene();
        dashboardScene = createDashboardScene();
        addExpenseScene = createAddExpenseScene();
        viewExpensesScene = createViewExpensesScene();

        // Start app on the login screen
        try {
            String cssPath = getClass().getResource("styles.css").toExternalForm();
            loginScene.getStylesheets().add(cssPath);
            dashboardScene.getStylesheets().add(cssPath);
            addExpenseScene.getStylesheets().add(cssPath);
            viewExpensesScene.getStylesheets().add(cssPath);
        } catch (Exception e) {
            System.err.println("Could not load CSS: " + e.getMessage());
        }

        primaryStage.setTitle("Multi-User Expense Tracker");
        primaryStage.setScene(loginScene);
        primaryStage.show();
    }

    // ---------- LOGIN SCREEN ----------
    private Scene createLoginScene() {
        Label titleLabel = new Label("Multi-User Expense Tracker");
        titleLabel.getStyleClass().add("title-label");

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
                showAlert(AlertType.ERROR, "Connection Error", 
                    "Failed to connect to server.", 
                    "Make sure the server is running on " + HOST + ":" + PORT);
                errorLabel.setText("Failed to connect to server.");
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
            updatePieChart(pieChart);
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
                String amountText = amountField.getText().trim();
                if (amountText.isEmpty()) {
                    showAlert(AlertType.WARNING, "Validation Error", 
                        "Amount is required", "Please enter an amount.");
                    return;
                }

                double amount = Double.parseDouble(amountText);
                
                if (amount <= 0) {
                    showAlert(AlertType.WARNING, "Validation Error", 
                        "Invalid Amount", "Amount must be greater than zero.");
                    return;
                }

                String category = categoryComboBox.getValue();
                if (category == null || category.trim().isEmpty()) {
                    showAlert(AlertType.WARNING, "Validation Error", 
                        "Category Required", "Please select a category.");
                    return;
                }

                LocalDate date = datePicker.getValue();
                if (date == null) {
                    showAlert(AlertType.WARNING, "Validation Error", 
                        "Date Required", "Please select a date.");
                    return;
                }

                String note = noteField.getText().trim();

                if (!connection.isConnected()) {
                    showAlert(AlertType.ERROR, "Connection Error", 
                        "Not connected to server", "Please reconnect and try again.");
                    return;
                }

                Expense expense = new Expense(amount, category.trim(), date, note);
                String command = ExpenseProtocol.toServerMessage(expense, currentUsername);
                String response = connection.sendCommand(command);

                if (response != null && response.startsWith("SUCCESS")) {
                    statusLabel.setText("Expense added successfully!");
                    statusLabel.getStyleClass().add("success-label");
                    errorLabel.setText("");
                    amountField.clear();
                    categoryComboBox.setValue(null);
                    datePicker.setValue(LocalDate.now());
                    noteField.clear();
                    loadExpenses();
                } else {
                    showAlert(AlertType.ERROR, "Server Error", 
                        "Failed to add expense", response != null ? response : "Unknown error");
                    errorLabel.setText("Error: " + response);
                    statusLabel.setText("");
                }
            } catch (NumberFormatException ex) {
                showAlert(AlertType.WARNING, "Validation Error", 
                    "Invalid Amount", "Please enter a valid number.");
                errorLabel.setText("Invalid amount format.");
                statusLabel.setText("");
            } catch (Exception ex) {
                showAlert(AlertType.ERROR, "Error", 
                    "Unexpected error", ex.getMessage());
                errorLabel.setText("Error: " + ex.getMessage());
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
        titleLabel.getStyleClass().add("title-label");

        expensesData = FXCollections.observableArrayList();
        expensesTable = new TableView<>(expensesData);
        expensesTable.setPrefHeight(250);
        expensesTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        pieChart = new PieChart();
        pieChart.setPrefHeight(250);
        pieChart.setTitle("Spending by Category");

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

        refreshButton.setOnAction(e -> {
            loadExpenses();
            updatePieChart(pieChart);
        });
        backButton.setOnAction(e -> primaryStage.setScene(dashboardScene));

        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.getChildren().addAll(refreshButton, backButton);

        HBox chartsBox = new HBox(20);
        chartsBox.setAlignment(Pos.CENTER);
        chartsBox.getChildren().addAll(expensesTable, pieChart);

        VBox root = new VBox(10);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(20));
        root.getChildren().addAll(
                titleLabel,
                chartsBox,
                statusLabel,
                buttonBox
        );

        return new Scene(root, 1000, 600);
    }

    private void updatePieChart(PieChart pieChart) {
        pieChart.getData().clear();

        if (expensesData == null || expensesData.isEmpty()) {
            return;
        }

        Map<String, Double> categoryTotals = new HashMap<>();
        for (Expense expense : expensesData) {
            String category = expense.getCategory();
            categoryTotals.put(category, 
                categoryTotals.getOrDefault(category, 0.0) + expense.getAmount());
        }

        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
        for (Map.Entry<String, Double> entry : categoryTotals.entrySet()) {
            pieChartData.add(new PieChart.Data(entry.getKey(), entry.getValue()));
        }

        pieChart.setData(pieChartData);
    }

    private void showAlert(AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
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
            showAlert(AlertType.ERROR, "Connection Error", 
                "Not connected to server", "Please reconnect and try again.");
            return;
        }

        try {
            String command = "GET_EXPENSES|" + currentUsername;
            String response = connection.sendCommand(command);

            if (response == null || !response.startsWith("SUCCESS")) {
                expensesData.clear();
                if (response != null && response.startsWith("ERROR")) {
                    showAlert(AlertType.ERROR, "Server Error", 
                        "Failed to load expenses", response);
                }
                return;
            }

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
                    if (expense != null) {
                        expenses.add(expense);
                    }
                }
            }

            expensesData.clear();
            expensesData.addAll(expenses);
            if (pieChart != null) {
                updatePieChart(pieChart);
            }
        } catch (Exception e) {
            expensesData.clear();
            showAlert(AlertType.ERROR, "Error", 
                "Failed to load expenses", e.getMessage());
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}