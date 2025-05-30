package server;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import common.TicTacToeService;
import java.rmi.server.RemoteServer;
import java.rmi.server.ServerNotActiveException;


public class ServerMain {
    private static final int FIXED_PORT = 1099;
    private static final int SERVICE_PORT_BASE = 1100;

    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Usage: java ServerMain <server_ip>");
            System.exit(1);
        }
        
        try {
            String serverIP = args[0];
            System.setProperty("java.rmi.server.hostname", serverIP);
            
            Registry registry = LocateRegistry.createRegistry(FIXED_PORT);
            TicTacToeServiceImpl service1 = new TicTacToeServiceImpl(SERVICE_PORT_BASE);
            registry.rebind("TicTacToeService1", service1);
            
            TicTacToeServer.registerPermanentService("TicTacToeService1", service1);
            TicTacToeServer.setPortConfiguration(SERVICE_PORT_BASE);
            
            System.out.println("Server ready at " + serverIP);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}