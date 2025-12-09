package client;

import common.Expense;
import common.ExpenseJson;
import common.ExpenseProtocol;

import java.time.LocalDate;
import java.util.Scanner;

// This test script is intended to demo the client-server interaction and the functionality of the expense tracke.
// Will be removed on production! - Moose
// *Or maybe kept?*

public class ClientTest {
    private static final String HOST = "localhost";
    private static final int PORT = 5000;

    public static void main(String[] args) {
        ClientConnection connection = new ClientConnection(HOST, PORT);
        
        System.out.println("Connecting to server...");
        if (!connection.connect()) {
            System.out.println("Failed to connect to server!");
            return;
        }
        System.out.println("Connected to server!\n");

        Scanner scanner = new Scanner(System.in);
        String username = "";

        while (true) {
            System.out.println("Expense Tracker Client");
            System.out.println("1. Set username");
            System.out.println("2. Add expense");
            System.out.println("3. Get expenses");
            System.out.println("4. Quit");
            System.out.print("Choose option: ");

            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    System.out.print("Enter username: ");
                    username = scanner.nextLine().trim();
                    System.out.println("Username set to: " + username + "\n");
                    break;

                case "2":
                    if (username.isEmpty()) {
                        System.out.println("Please set username first!\n");
                        break;
                    }
                    addExpense(connection, username, scanner);
                    break;

                case "3":
                    if (username.isEmpty()) {
                        System.out.println("Please set username first!\n");
                        break;
                    }
                    getExpenses(connection, username);
                    break;

                case "4":
                    connection.disconnect();
                    System.out.println("Goodbye!");
                    scanner.close();
                    return;

                default:
                    System.out.println("Invalid option!\n");
            }
        }
    }

    private static void addExpense(ClientConnection connection, String username, Scanner scanner) {
        try {
            System.out.print("Amount: ");
            double amount = Double.parseDouble(scanner.nextLine());
            
            System.out.print("Category: ");
            String category = scanner.nextLine().trim();
            
            System.out.print("Date (Formatted like so: YYYY-MM-DD): ");
            LocalDate date = LocalDate.parse(scanner.nextLine().trim());
            
            System.out.print("Note: ");
            String note = scanner.nextLine().trim();

            Expense expense = new Expense(amount, category, date, note);
            String command = ExpenseProtocol.toServerMessage(expense, username);
            
            String response = connection.sendCommand(command);
            System.out.println("Server response: " + response + "\n");

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage() + "\n");
        }
    }

    private static void getExpenses(ClientConnection connection, String username) {
        String command = "GET_EXPENSES|" + username;
        String response = connection.sendCommand(command);
        
        if (response == null) {
            System.out.println("No response from server\n");
            return;
        }

        String[] parts = response.split("\\|");
        if (parts.length < 2 || !parts[0].equals("SUCCESS")) {
            System.out.println("Error: " + response + "\n");
            return;
        }

        try {
            int count = Integer.parseInt(parts[1]);
            System.out.println("Found " + count + " expense(s):");
            
            for (int i = 0; i < count; i++) {
                String json = connection.receiveResponse();
                if (json != null) {
                    Expense expense = ExpenseJson.fromJson(json);
                    System.out.println("  - $" + expense.getAmount() + " | " + 
                                     expense.getCategory() + " | " + expense.getDate());
                }
            }
            System.out.println();
        } catch (Exception e) {
            System.out.println("Error parsing response: " + e.getMessage() + "\n");
        }
    }
}

