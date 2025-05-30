package server;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class TicTacToeServer {
    private static final int MAX_SESSIONS = 6;
    private static final ConcurrentHashMap<String, TicTacToeServiceImpl> activeSessions = new ConcurrentHashMap<>();
    private static final AtomicInteger sessionCounter = new AtomicInteger(2);
    private static TicTacToeServiceImpl permanentService;
    private static int portBase = 1100; // Default base port

    public static void setPortConfiguration(int basePort) {
        portBase = basePort;
    }
    public static void registerPermanentService(String serviceName, TicTacToeServiceImpl service) {
        permanentService = service;
        activeSessions.put(serviceName, service);
    }

    public static void main(String[] args) {
        try {
            // Create registry if it doesn't exist
            Registry registry;
            try {
                registry = LocateRegistry.createRegistry(1099);
                System.out.println("RMI registry created on port 1099");
            } catch (Exception e) {
                registry = LocateRegistry.getRegistry(1099);
                System.out.println("Using existing RMI registry on port 1099");
            }

            System.out.println("TicTacToeServer ready with dynamic session management...");
        } catch (Exception e) {
            System.err.println("Server exception: " + e.toString());
            e.printStackTrace();
        }
    }

    public static synchronized String connectToAvailableSession() throws RemoteException {
        // First try to find a session with only one player
        for (Map.Entry<String, TicTacToeServiceImpl> entry : activeSessions.entrySet()) {
            if (!entry.getKey().equals("TicTacToeService1") && 
                entry.getValue().getPlayerCount() == 1) {
                return entry.getKey();
            }
        }
    
        // Create new session if we have capacity
        if (activeSessions.size() < MAX_SESSIONS) {
            try {
                Registry registry = LocateRegistry.getRegistry(1099);
                return createNewSession(registry);
            } catch (Exception e) {
                throw new RemoteException("Failed to create new session", e);
            }
        }
    
        throw new RemoteException("All sessions are full");
    }

    private static synchronized String createNewSession(Registry registry) throws RemoteException {
        if (activeSessions.size() >= MAX_SESSIONS) {
            throw new RemoteException("Maximum number of sessions reached");
        }

        int sessionId = sessionCounter.getAndIncrement();
        String serviceName = "TicTacToeService" + sessionId;
        
        // Calculate port for this service (base + sessionId)
        int servicePort = portBase + sessionId;
        TicTacToeServiceImpl gameService = new TicTacToeServiceImpl(servicePort);
        
        gameService.setCleanupCallback(() -> {
            try {
                if (!serviceName.equals("TicTacToeService1")) {
                    registry.unbind(serviceName);
                }
                activeSessions.remove(serviceName);
                System.out.println("Session " + serviceName + " cleaned up");
            } catch (Exception e) {
                System.err.println("Error cleaning up session: " + e.getMessage());
            }
        });

        registry.rebind(serviceName, gameService);
        activeSessions.put(serviceName, gameService);
        System.out.println("Created new session: " + serviceName + " on port " + servicePort);
        return serviceName;
    }
}
