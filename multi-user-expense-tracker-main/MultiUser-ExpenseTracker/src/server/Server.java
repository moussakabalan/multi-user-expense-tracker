package server;

import java.io.*;
import java.net.*;
import java.util.*;

public class Server {
    private static final int PORT = 5000;
    private static List<ClientHandler> activeClients = new ArrayList<>();
    private static ExpenseStorage storage;

    public static void main(String[] args) {
        System.out.println("=== Expense Tracker Server ===");
        System.out.println("Initializing storage system...\n");

        // this initilizes the storage
        storage = new ExpenseStorage();

        System.out.println("\nStarting server on port " + PORT + "...");

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("✓ Server started successfully!");
            System.out.println("Waiting for clients to connect...\n");

            // always accepts client conections
            while (true) {
                try {
                    // accepting new client connections
                    Socket clientSocket = serverSocket.accept();

                    System.out.println("✓ New client connected: " +
                            clientSocket.getInetAddress().getHostAddress());

                    // creating and starting a new thread
                    ClientHandler clientHandler = new ClientHandler(clientSocket, storage);
                    activeClients.add(clientHandler);

                    Thread thread = new Thread(clientHandler);
                    thread.start();

                    System.out.println("→ Active clients: " + activeClients.size());
                    System.out.println("→ Total users: " + storage.getUserCount());
                    System.out.println("→ Total expenses: " + storage.getTotalExpenseCount() + "\n");

                } catch (IOException e) {
                    System.err.println("Error accepting client connection: " + e.getMessage());
                }
            }

        } catch (IOException e) {
            System.err.println("Could not start server on port " + PORT);
            e.printStackTrace();
        }
    }

    // removing disconnecting clients
    public static synchronized void removeClient(ClientHandler client) {
        activeClients.remove(client);
        System.out.println("→ Client disconnected. Active clients: " + activeClients.size());
    }

    public static ExpenseStorage getStorage() {
        return storage;
    }
}