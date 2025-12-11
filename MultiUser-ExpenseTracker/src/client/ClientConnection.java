package client;

import java.io.*;
import java.net.Socket;

public class ClientConnection {
    private Socket socket;
    private BufferedReader input;
    private PrintWriter output;
    private String host;
    private int port;
    private boolean connected;

    public ClientConnection(String host, int port) {
        this.host = host;
        this.port = port;
        this.connected = false;
    }

    public boolean connect() {
        try {
            socket = new Socket(host, port);
            socket.setSoTimeout(30000);
            input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            output = new PrintWriter(socket.getOutputStream(), true);
            
            String welcome = input.readLine();
            if (welcome != null && welcome.startsWith("CONNECTION SUCCESSFUL")) {
                connected = true;
                return true;
            }
            disconnect();
            return false;
        } catch (IOException e) {
            System.err.println("Connection failed: " + e.getMessage());
            disconnect();
            return false;
        }
    }

    public String sendCommand(String command) {
        if (!connected) {
            return "ERROR|Not connected to server";
        }

        if (command == null || command.trim().isEmpty()) {
            return "ERROR|Invalid command";
        }

        try {
            output.println(command);
            String response = input.readLine();
            if (response == null) {
                connected = false;
                return "ERROR|Connection lost: Server closed connection";
            }
            return response;
        } catch (java.net.SocketTimeoutException e) {
            connected = false;
            return "ERROR|Connection timeout";
        } catch (IOException e) {
            connected = false;
            return "ERROR|Connection lost: " + e.getMessage();
        }
    }

    public String receiveResponse() {
        if (!connected) {
            return null;
        }

        try {
            return input.readLine();
        } catch (IOException e) {
            connected = false;
            return null;
        }
    }

    public void disconnect() {
        try {
            if (output != null) {
                output.println("QUIT");
            }
            if (input != null) input.close();
            if (output != null) output.close();
            if (socket != null) socket.close();
            connected = false;
        } catch (IOException e) {
            System.err.println("Error disconnecting: " + e.getMessage());
        }
    }

    public boolean isConnected() {
        return connected;
    }
}

