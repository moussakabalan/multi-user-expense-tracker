package server;

import common.Expense;
import common.ExpenseJson;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ExpenseStorage {
    private static final String DATA_DIRECTORY = "data";
    private ConcurrentHashMap<String, List<Expense>> userExpenses;

    public ExpenseStorage() {
        this.userExpenses = new ConcurrentHashMap<>();

        try {
            Files.createDirectories(Paths.get(DATA_DIRECTORY));
            System.out.println("[STORAGE] ✓ Data directory ready: " + DATA_DIRECTORY);
        } catch (IOException e) {
            System.err.println("[ERROR] Error creating data directory: " + e.getMessage());
        }

        loadAllUserData();
    }

    public synchronized void addExpense(String username, Expense expense) {
        if (username == null || username.trim().isEmpty() || expense == null) {
            System.err.println("[STORAGE] Invalid expense data - username or expense is null");
            return;
        }

        userExpenses.putIfAbsent(username, new ArrayList<>());
        List<Expense> expenses = userExpenses.get(username);
        synchronized (expenses) {
            expenses.add(expense);
        }
        saveUserData(username);

        System.out.println("[STORAGE] Added expense for " + username +
                ": $" + String.format("%.2f", expense.getAmount()));
    }

    public synchronized List<Expense> getExpenses(String username) {
        List<Expense> expenses = userExpenses.get(username);
        if (expenses == null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(expenses);
    }

    private void saveUserData(String username) {
        if (username == null || username.trim().isEmpty()) {
            return;
        }

        String filename = DATA_DIRECTORY + "/" + username + ".json";

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            List<Expense> expenses = userExpenses.get(username);

            if (expenses != null) {
                synchronized (expenses) {
                    for (Expense expense : expenses) {
                        if (expense != null) {
                            writer.write(ExpenseJson.toJson(expense));
                            writer.newLine();
                        }
                    }
                }
            }

        } catch (IOException e) {
            System.err.println("[ERROR] Error saving data for " + username + ": " + e.getMessage());
        }
    }

    private void loadUserData(String username, File file) {
        List<Expense> expenses = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;

            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    Expense expense = ExpenseJson.fromJson(line);
                    if (expense != null) {
                        expenses.add(expense);
                    }
                }
            }

            userExpenses.put(username, expenses);
            System.out.println("[STORAGE] Loaded " + expenses.size() + " expenses for: " + username);

        } catch (IOException e) {
            System.err.println("[ERROR] Error loading data for " + username);
        }
    }

    private void loadAllUserData() {
        File dataDir = new File(DATA_DIRECTORY);
        File[] files = dataDir.listFiles((dir, name) -> name.endsWith(".json"));

        if (files == null || files.length == 0) {
            System.out.println("[STORAGE] No existing user data found");
            return;
        }

        System.out.println("[STORAGE] Loading existing user data...");
        for (File file : files) {
            String username = file.getName().replace(".json", "");
            loadUserData(username, file);
        }

        System.out.println("[STORAGE] Loaded data for " + userExpenses.size() + " users\n");
    }

    public int getTotalExpenseCount() {
        return userExpenses.values().stream()
                .mapToInt(List::size)
                .sum();
    }

    public int getUserCount() {
        return userExpenses.size();
    }
}