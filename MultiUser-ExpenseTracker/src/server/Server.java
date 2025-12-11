package server;

import java.io.*;
import java.net.*;
import java.util.*;

public class Server {
    private static final int PORT = 5000;
    private static List<ClientHandler> activeClients = new ArrayList<>();
    private static ExpenseStorage storage;

    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("   Expense Tracker Server v1.1");
        System.out.println("========================================");
        System.out.println("[INIT] Initializing storage system...");

        // this initilizes the storage
        storage = new ExpenseStorage();

        System.out.println("[INIT] Starting server on port " + PORT + "...");

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("[SERVER] ✓ Server started successfully on port " + PORT);
            System.out.println("[SERVER] Waiting for client connections...");
            System.out.println("[SERVER] Press Ctrl+C to stop the server\n");

            // always accepts client conections
            while (true) {
                try {
                    // accepting new client connections
                    Socket clientSocket = serverSocket.accept();
                    String clientAddress = clientSocket.getInetAddress().getHostAddress();

                    System.out.println("[CONNECTION] New client connected from " + clientAddress);

                    // creating and starting a new thread
                    ClientHandler clientHandler = new ClientHandler(clientSocket, storage);
                    synchronized (activeClients) {
                        activeClients.add(clientHandler);
                    }

                    Thread thread = new Thread(clientHandler);
                    thread.setDaemon(true);
                    thread.start();

                    System.out.println("[STATUS] Active clients: " + activeClients.size() + 
                                     " | Users: " + storage.getUserCount() + 
                                     " | Total expenses: " + storage.getTotalExpenseCount());

                } catch (IOException e) {
                    System.err.println("[ERROR] Failed to accept client connection: " + e.getMessage());
                }
            }

        } catch (IOException e) {
            System.err.println("[FATAL] Could not start server on port " + PORT);
            System.err.println("[FATAL] Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // removing disconnecting clients
    public static synchronized void removeClient(ClientHandler client) {
        synchronized (activeClients) {
            activeClients.remove(client);
        }
        System.out.println("[DISCONNECT] Client disconnected. Active clients: " + activeClients.size());
    }

    public static ExpenseStorage getStorage() {
        return storage;
    }
}