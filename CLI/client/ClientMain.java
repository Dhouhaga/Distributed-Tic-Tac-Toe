package client;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import common.TicTacToeService;
import java.rmi.Naming;

public class ClientMain {
    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println("Usage: java ClientMain <server_ip> <client_ip>");
            System.exit(1);
        }
        
        try {
            String serverIP = args[0];
            String clientIP = args[1];
            
            // Set client IP property before any RMI operations
            System.setProperty("java.rmi.server.hostname", clientIP);
            
            TicTacToeService service = (TicTacToeService) Naming.lookup("rmi://" + serverIP + ":1099/TicTacToeService1");
            TicTacToeClient client = new TicTacToeClient(service, serverIP, clientIP);
            client.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}