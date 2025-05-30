package client;

import common.PlayerCallback;
import common.TicTacToeService;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.SwingUtilities;

public class PlayerCallbackImpl implements PlayerCallback {
    private final TicTacToeClient client;
    private final AtomicBoolean serverAlive = new AtomicBoolean(true);
    private final String clientIP;

    public PlayerCallbackImpl(TicTacToeClient client, String clientIP) throws RemoteException {
	
        this.client = client;
        this.clientIP = clientIP;
        System.setProperty("java.rmi.server.hostname", clientIP);
        
        // Port assignment logic remains the same
        int port = 5002;
        PlayerCallback stub = null;

	
        while (stub == null) {
            try {
                stub = (PlayerCallback) UnicastRemoteObject.exportObject(this, port);
                System.out.println("Callback object exported on port: " + port);
            } catch (RemoteException e) {
                if (e.getMessage().contains("Port already in use")) {
                    port++;
                    if (port > 5100) {
                        throw new RemoteException("No available ports in range 5002-5100");
                    }
                } else {
                    throw e;
                }
            }
        }

        startHeartbeatChecker();
    }

    private void startHeartbeatChecker() {
        new Thread(() -> {
            while (serverAlive.get()) {
                try {
                    Thread.sleep(5000);
                    if (!serverAlive.get()) break;

                    TicTacToeService service = client.getGameService();
                    if (service != null) {
                        service.ping();
                    }
                } catch (RemoteException e) {
                    serverAlive.set(false);
                    SwingUtilities.invokeLater(() -> {
                        client.gui.showErrorMessage("Error: Server connection lost! Closing in 5 seconds...");
                    });
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                    System.exit(1);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }).start();
    }

@Override
public void displayMessage(String message) throws RemoteException {
    if (message.contains("SESSION_END|")) {
        String content = message.substring(message.indexOf("SESSION_END|") + "SESSION_END|".length());
        client.gui.showTimedErrorMessage("Opponent left: " + content, 5000);
    } else if (message.contains("declined rematch")) {
        client.gui.showTimedErrorMessage("Opponent declined to play again", 5000);
    } else {
        client.displayMessage(message);
    }
}

/*
@Override
public void displayMessage(String message) throws RemoteException {
    if (message.contains("SESSION_END|")) {
        String content = message.substring(message.indexOf("SESSION_END|") + "SESSION_END|".length());
        client.gui.showTimedErrorMessage("Opponent left: " + content + " Closing in 5 seconds...", 5000);
    } else if (message.contains("declined rematch")) {
        client.gui.showTimedErrorMessage("Opponent declined to play again. Closing in 5 seconds...", 5000);
    } else {
        client.displayMessage(message);
    }
}
*/
    @Override
    public void updateBoard(char[][] board) throws RemoteException {
        client.gui.updateBoard(board);
    }


    
    @Override
    public void notifyNewGame() throws RemoteException {
        client.gui.resetGameBoard();
        client.gui.setStatusMessage("New game starting...", client.gui.getPrimaryColor());
    }

@Override 
public void notifyTurn() throws RemoteException {
    client.gui.setStatusMessage("YOUR TURN - Make your move!", client.gui.getAccentColor());
    client.gui.enableBoard(true);
}

@Override
public void gameOver(String message) throws RemoteException {
    client.gui.gameOver(message); // Already handles GUI updates
}
}
