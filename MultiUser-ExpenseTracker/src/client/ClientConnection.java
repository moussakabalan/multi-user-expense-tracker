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
            input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            output = new PrintWriter(socket.getOutputStream(), true);
            
            String welcome = input.readLine();
            if (welcome != null && welcome.startsWith("CONNECTION SUCCESSFUL")) {
                connected = true;
                return true;
            }
            return false;
        } catch (IOException e) {
            System.err.println("Connection failed! :( Error: " + e.getMessage());
            return false;
        }
    }

    public String sendCommand(String command) {
        if (!connected) {
            return "ERROR|Not connected to server";
        }

        try {
            output.println(command);
            return input.readLine();
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

