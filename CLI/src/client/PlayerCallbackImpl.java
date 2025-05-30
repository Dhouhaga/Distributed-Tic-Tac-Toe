package client;

import common.PlayerCallback;
import common.TicTacToeService;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.atomic.AtomicBoolean;


public class PlayerCallbackImpl implements PlayerCallback {
    private final TicTacToeClient client;
    private final AtomicBoolean serverAlive = new AtomicBoolean(true);
    private final String clientIP;
    
    public PlayerCallbackImpl(TicTacToeClient client, String clientIP) throws RemoteException {
        this.client = client;
        this.clientIP = clientIP;
        System.setProperty("java.rmi.server.hostname", clientIP); 
        PlayerCallback stub = (PlayerCallback) UnicastRemoteObject.exportObject(this, 5002); // Using 0 for random port
        
        startHeartbeatChecker();
    }
    private void startHeartbeatChecker() {
        new Thread(() -> {
            while (serverAlive.get()) {
                try {
                    Thread.sleep(5000); // Check every 5 seconds
                    if (!serverAlive.get()) break;
                    
                    // Test server connection using the getter
                    TicTacToeService service = client.getGameService();
                    if (service != null) {
                        service.ping(); // This method needs to exist in your TicTacToeService interface
                    }
                } catch (RemoteException e) {
                    serverAlive.set(false);
                    System.err.println("\nError: Server connection lost!");
                    System.exit(1);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }).start();
    }
    
    @Override
    public void updateBoard(char[][] board) throws RemoteException {
        serverAlive.set(true); // Reset flag on successful communication
        client.displayBoard(board);
    }
    
    @Override
    public void notifyTurn() throws RemoteException {
        serverAlive.set(true); // Reset flag on successful communication
        client.notifyYourTurn();
    }
    
    @Override
    public void gameOver(String message) throws RemoteException {
        client.gameOver(message);
    }
    
    @Override
    public void displayMessage(String message) throws RemoteException {
        client.displayMessage(message);
    }
    
    @Override
    public void notifyNewGame() throws RemoteException {
        client.handleNewGame();
    }
}
