package server;

import common.Expense;
import common.ExpenseJson;

import java.io.*;
import java.net.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ClientHandler implements Runnable {
    private Socket clientSocket;
    private BufferedReader input;
    private PrintWriter output;
    private String clientAddress;
    private ExpenseStorage storage;
    private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE;

    public ClientHandler(Socket socket, ExpenseStorage storage) {
        this.clientSocket = socket;
        this.clientAddress = socket.getInetAddress().getHostAddress();
        this.storage = storage;
    }

    @Override
    public void run() {
        try {
            // setting up the input/output streams
            input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            output = new PrintWriter(clientSocket.getOutputStream(), true);

            // this sends a welcome message after a successful connection
            output.println("CONNECTION SUCCESSFUL|Connected to Expense Tracker Server");
            System.out.println("[HANDLER] Thread started for client " + clientAddress);

            // listening for client messages
            String message;
            while ((message = input.readLine()) != null) {
                if (message.trim().isEmpty()) {
                    continue;
                }

                String[] parts = message.split("\\|", 2);
                String command = parts.length > 0 ? parts[0] : ""; // added command extraction just to be safe!
                System.out.println("[REQUEST] " + clientAddress + " -> " + command);

                String response = processCommand(message);
                output.println(response);

                if (response != null && response.startsWith("SUCCESS")) {
                    System.out.println("[RESPONSE] " + clientAddress + " <- SUCCESS");
                } else if (response != null && response.startsWith("ERROR")) {
                    System.err.println("[RESPONSE] " + clientAddress + " <- ERROR: " + response);
                }

                if (message.equalsIgnoreCase("QUIT") || message.equalsIgnoreCase("EXIT")) {
                    System.out.println("[DISCONNECT] Client " + clientAddress + " requested disconnect");
                    break;
                }
            }

        } catch (IOException e) {
            System.err.println("[ERROR] Connection error with " + clientAddress + ": " + e.getMessage());
        } finally {
            cleanup();
        }
    }

    private String processCommand(String message) {
        try {
            String[] parts = message.split("\\|");

            if (parts.length == 0) {
                return "ERROR|Invalid command format";
            }

            String command = parts[0].toUpperCase();

            switch (command) {
                case "ADD_EXPENSE":
                    return handleAddExpense(parts);

                case "GET_EXPENSES":
                    return handleGetExpenses(parts);

                case "QUIT":
                case "EXIT":
                    return "SUCCESS|Goodbye";

                default:
                    return "ERROR|Unknown command: " + command;
            }

        } catch (Exception e) {
            System.err.println("[ERROR] Error processing command from " + clientAddress + ": " + e.getMessage());
            e.printStackTrace();
            return "ERROR|" + e.getMessage();
        }
    }

    private String handleAddExpense(String[] parts) {
        if (parts.length < 5) {
            return "ERROR|Invalid ADD_EXPENSE format";
        }

        try {
            String username = sanitizeUsername(parts[1]);
            if (username == null || username.trim().isEmpty()) {
                return "ERROR|Invalid username";
            }

            double amount = Double.parseDouble(parts[2]);
            String category = parts[3];
            
            if (category == null || category.trim().isEmpty()) {
                return "ERROR|Category cannot be empty";
            }

            LocalDate date;
            try {
                date = LocalDate.parse(parts[4], dateFormatter);
            } catch (Exception e) {
                return "ERROR|Invalid date format. Use YYYY-MM-DD";
            }

            String note = parts.length > 5 ? parts[5] : "";

            if (amount <= 0 || Double.isNaN(amount) || Double.isInfinite(amount)) {
                return "ERROR|Amount must be a positive number";
            }

            Expense expense = new Expense(amount, category, date, note);
            storage.addExpense(username, expense);

            System.out.println("[ADD_EXPENSE] User: " + username + 
                             " | Amount: $" + String.format("%.2f", amount) + 
                             " | Category: " + category);

            return "SUCCESS|Expense added successfully";

        } catch (NumberFormatException e) {
            return "ERROR|Invalid amount format";
        } catch (Exception e) {
            return "ERROR|Failed to add expense: " + e.getMessage();
        }
    }

    private String handleGetExpenses(String[] parts) {
        if (parts.length < 2) {
            return "ERROR|Invalid GET_EXPENSES format";
        }

        try {
            String username = parts[1];
            List<Expense> expenses = storage.getExpenses(username);

            if (expenses.isEmpty()) {
                return "SUCCESS|0";
            }

            StringBuilder response = new StringBuilder();
            response.append("SUCCESS|").append(expenses.size()).append("\n");

            for (Expense expense : expenses) {
                response.append(ExpenseJson.toJson(expense)).append("\n");
            }

            System.out.println("[GET_EXPENSES] User: " + username + 
                             " | Retrieved " + expenses.size() + " expense(s)");

            return response.toString().trim();

        } catch (Exception e) {
            return "ERROR|Failed to retrieve expenses: " + e.getMessage();
        }
    }

    private String sanitizeUsername(String username) {
        if (username == null) {
            return null;
        }
        String sanitized = username.trim();
        if (sanitized.isEmpty() || sanitized.length() > 50) {
            return null;
        }
        return sanitized.replaceAll("[^a-zA-Z0-9_-]", "");
    }

    private void cleanup() {
        try {
            if (input != null) input.close();
            if (output != null) output.close();
            if (clientSocket != null) clientSocket.close();

            System.out.println("[CLEANUP] Connection closed for " + clientAddress);
            Server.removeClient(this);

        } catch (IOException e) {
            System.err.println("[ERROR] Error closing client resources: " + e.getMessage());
        }
    }
}