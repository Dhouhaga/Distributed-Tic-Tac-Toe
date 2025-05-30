package server;

import common.*;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class TicTacToeServiceImpl extends UnicastRemoteObject implements TicTacToeService {
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final AtomicReference<Runnable> cleanupCallback = new AtomicReference<>();
    private volatile char[][] board;
    private volatile int currentPlayer;
    private volatile boolean gameActive;
    private final Map<Integer, PlayerCallback> players;
    private final Map<Integer, Boolean> playAgainResponses;
    private final Random random;
    private final Object gameLock = new Object();

    public TicTacToeServiceImpl(int port) throws RemoteException {
        super(port); // Now using a fixed port
        this.players = new ConcurrentHashMap<>();
        this.playAgainResponses = new ConcurrentHashMap<>();
        this.random = new Random();
        this.board = new char[GameConstants.BOARD_SIZE][GameConstants.BOARD_SIZE];
        initializeGame();
    }
    
    @Override
    public String connectToAvailableSession() throws RemoteException {
        return TicTacToeServer.connectToAvailableSession();
    }

    public void setCleanupCallback(Runnable callback) {
        this.cleanupCallback.set(callback);
    }

    public int getPlayerCount() {
        return players.size();
    }

    private void initializeGame() {
        synchronized (gameLock) {
            if (players.size() == 1) {
                // Reset board but keep the existing player
                for (int i = 0; i < GameConstants.BOARD_SIZE; i++) {
                    for (int j = 0; j < GameConstants.BOARD_SIZE; j++) {
                        board[i][j] = GameConstants.EMPTY;
                    }
                }
                gameActive = false; // Wait until another player joins
            } else {
                // Full reset if no players are left
                board = new char[GameConstants.BOARD_SIZE][GameConstants.BOARD_SIZE];

                // Initialize board to EMPTY
                for (int i = 0; i < GameConstants.BOARD_SIZE; i++) {
                    for (int j = 0; j < GameConstants.BOARD_SIZE; j++) {
                        board[i][j] = GameConstants.EMPTY;
                    }
                }

                gameActive = true;
                currentPlayer = random.nextInt(2) + 1;
            }
        }
    }

    @Override
    public int joinGame(PlayerCallback callback) throws RemoteException {
        synchronized (gameLock) {
            // Clean up disconnected players
            Iterator<Map.Entry<Integer, PlayerCallback>> it = players.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<Integer, PlayerCallback> entry = it.next();
                try {
                    entry.getValue().displayMessage("ping");
                } catch (RemoteException e) {
                    it.remove();
                }
            }
    
            if (players.size() >= 2) {
                try {
                    callback.displayMessage("Game is full. Please try again later.");  
                } catch (RemoteException e) { } 
                return -1;
            }
    
            // Assign Player ID (1 or 2)
            int playerId = players.isEmpty() ? 1 : 2;
            players.put(playerId, callback);
            try {
                callback.displayMessage("You joined as Player " + playerId + (playerId == 1 ? " (X)" : " (O)"));
            } catch (RemoteException e) { }
    
            // Start game if 2 players are connected
            if (players.size() == 2) {
                initializeGame();
                playAgainResponses.clear();
                new Thread(() -> {
                    try {
                        notifyPlayers("Both players connected. Game starts now!");
                        players.get(currentPlayer).notifyTurn();
                    } catch (RemoteException e) {
                        System.out.println("Couldn't Reach Player [Player disconnected]");
                        try {
                            quitGame(playerId);
                        } catch (RemoteException exp) { }
                    }
                }).start();
            }
    
            return playerId;
        }
    }

    @Override
    public boolean makeMove(int playerId, int row, int col) throws RemoteException {
        System.out.println("Received move from player " + playerId + ": " + row + "," + col);
        synchronized (gameLock) {
            PlayerCallback playerCallback = players.get(playerId);
            if (playerCallback == null) {
                throw new RemoteException("Player connection lost. Please reconnect.");
            }

            if (!gameActive || playerId != currentPlayer) {
                try {
                    playerCallback.displayMessage("It's not your turn!");
                } catch (RemoteException e) {
                    // TODO: handle exception
                }
                return false;
            }

            if (row < 0 || row >= GameConstants.BOARD_SIZE ||
                    col < 0 || col >= GameConstants.BOARD_SIZE ||
                    board[row][col] != GameConstants.EMPTY) {
                        try {
                            playerCallback.displayMessage("Invalid move! Try again.");
                        } catch (RemoteException e) {
                            // TODO: handle exception
                        }
                return false;
            }

            board[row][col] = (playerId == 1) ? GameConstants.PLAYER_X : GameConstants.PLAYER_O;

            new Thread(() -> {
                try {
                    for (PlayerCallback callback : players.values()) {
                        if (callback != null) {
                            callback.updateBoard(board);
                        }
                    }
                } catch (RemoteException e) {
                     System.out.println("Couldn't Reach Player [Player disconnected]");
                     try {
                        int otherPlayerId = (playerId == 1) ? 2 : 1;
                        System.out.println("Player ID OUT4 -> " + otherPlayerId);
                        quitGame(otherPlayerId);
                     } catch (RemoteException exp) {
                        // TODO: handle exception
                     }
                }
            }).start();

            if (checkWin()) {
                gameActive = false;
                String winMessage = "Player " + playerId + " (" +
                        (playerId == 1 ? "X" : "O") + ") wins!";
                new Thread(() -> {
                    try {
                        notifyPlayers(winMessage);
                        askPlayAgain();
                    } catch (RemoteException e) {
                         System.out.println("Couldn't Reach Player [Player disconnected]");
                         try {
                            System.out.println("Player ID OUT1 -> " + playerId);
                            quitGame(playerId);
                        } catch (RemoteException exp) {
                           // TODO: handle exception
                        }
                    }
                }).start();
                return true;
            }

            if (isBoardFull()) {
                gameActive = false;
                new Thread(() -> {
                    try {
                        notifyPlayers("It's a draw!");
                        askPlayAgain();
                    } catch (RemoteException e) {
                         System.out.println("Couldn't Reach Player [Player disconnected]");
                         try {
                            System.out.println("Player ID OUT2 -> " + playerId);
                            quitGame(playerId);
                        } catch (RemoteException exp) {
                           // TODO: handle exception
                        }
                    }
                }).start();
                return true;
            }

            currentPlayer = (currentPlayer == 1) ? 2 : 1;
            PlayerCallback nextPlayer = players.get(currentPlayer);
            if (nextPlayer != null) {
                new Thread(() -> {
                    try {
                        nextPlayer.notifyTurn();
                    } catch (RemoteException e) {
                        System.out.println("Player " + currentPlayer + " disconnected. Removing from game.");
                        players.remove(currentPlayer);
                    }
                }).start();
            }

            return true;
        }
    }

    @Override
    public void quitGame(int playerId) throws RemoteException {
        synchronized (gameLock) {
            try {
                // Get the other player ID before removing anyone
                int otherPlayerId = (playerId == 1) ? 2 : 1;
                PlayerCallback otherPlayer = players.get(otherPlayerId);
                
                // Immediately clear both players
                players.clear();
                playAgainResponses.clear();
                initializeGame();
                
                // Attempt to notify the other player
                if (otherPlayer != null) {
                    try {
                        otherPlayer.displayMessage("GAME_OVER|SESSION_END|Your opponent left the game.");
                    } catch (RemoteException ignored) {
                        // Intentionally empty - suppress connection error messages
                    }
                }
                
                // Trigger cleanup if callback exists
                Runnable callback = cleanupCallback.get();
                if (callback != null) {
                    callback.run();
                }
            } catch (Exception e) {
                // Suppress all quit-related errors
            }
        }
    }

    @Override
    public boolean playAgain(int playerId, boolean response) throws RemoteException {
        synchronized (gameLock) {
            System.out.println("[SERVER] Received response from Player " + playerId + ": " + response);
            playAgainResponses.put(playerId, response);
    
            // Immediately acknowledge receipt
            try {
                PlayerCallback callback = players.get(playerId);
                if (callback != null) {
                    callback.displayMessage("Your response (" + (response ? "YES" : "NO") + ") was received");
                }
            } catch (RemoteException e) {
                System.err.println("Player " + playerId + " disconnected during response");
                playAgainResponses.remove(playerId);
                players.remove(playerId);
            }
    
            // If any player responded NO, end the session immediately
            if (response == false) {
                System.out.println("[SERVER] Player " + playerId + " declined rematch - ending session");
                new Thread(() -> {
                    try {
                        // Notify both players the session is ending
                        notifyPlayers("GAME_OVER|SESSION_END|Game session ended because a player declined rematch.");
                        players.clear();
                        initializeGame();
                        
                        // Trigger cleanup
                        Runnable callback = cleanupCallback.get();
                        if (callback != null) {
                            callback.run();
                        }
                    } catch (RemoteException e) {
                        System.err.println("Error ending session: " + e.getMessage());
                    }
                }).start();
                return false;
            }

            // If we have both YES responses
            if (playAgainResponses.size() == 2) {
                System.out.println("[SERVER] Starting rematch");
                playAgainResponses.clear();
                initializeGame(); // This already sets currentPlayer and gameActive

                new Thread(() -> {
                    try {
                        // Update both players' boards
                        for (PlayerCallback callback : players.values()) {
                            callback.notifyNewGame();
                            callback.updateBoard(board);
                        }

                        // DIRECTLY USE THE currentPlayer SET BY initializeGame()
                        if (gameActive && players.containsKey(currentPlayer)) {
                            players.get(currentPlayer).notifyTurn();
                        }
                    } catch (RemoteException e) {
                        System.err.println("Error starting rematch: " + e.getMessage());
                    }
                }).start();
                return true;
            }

            // If only one player responded YES, wait for the other
            if (playAgainResponses.size() == 1) {
                // Set timeout only if we're waiting for another response
                scheduler.schedule(() -> {
                    synchronized (gameLock) {
                        if (playAgainResponses.size() == 1) { // Still only one response
                            try {
                                notifyPlayers(
                                        "GAME_OVER|SESSION_END|Game session ended - opponent didn't respond in time.");
                                players.clear();
                                initializeGame();
                            } catch (RemoteException e) {
                                System.err.println("Timeout notification failed");
                            }
                        }
                    }
                }, 30, TimeUnit.SECONDS);
            }

            return true;
        }
    }

    private boolean checkWin() {
        // Check rows
        for (int i = 0; i < GameConstants.BOARD_SIZE; i++) {
            if (board[i][0] != GameConstants.EMPTY &&
                    board[i][0] == board[i][1] &&
                    board[i][1] == board[i][2]) {
                return true;
            }
        }

        // Check columns
        for (int j = 0; j < GameConstants.BOARD_SIZE; j++) {
            if (board[0][j] != GameConstants.EMPTY &&
                    board[0][j] == board[1][j] &&
                    board[1][j] == board[2][j]) {
                return true;
            }
        }

        // Check diagonals
        if (board[0][0] != GameConstants.EMPTY &&
                board[0][0] == board[1][1] &&
                board[1][1] == board[2][2]) {
            return true;
        }

        if (board[0][2] != GameConstants.EMPTY &&
                board[0][2] == board[1][1] &&
                board[1][1] == board[2][0]) {
            return true;
        }

        return false;
    }

    private boolean isBoardFull() {
        for (int i = 0; i < GameConstants.BOARD_SIZE; i++) {
            for (int j = 0; j < GameConstants.BOARD_SIZE; j++) {
                if (board[i][j] == GameConstants.EMPTY) {
                    return false;
                }
            }
        }
        return true;
    }

    private void notifyPlayers(String message) throws RemoteException {
        Iterator<Map.Entry<Integer, PlayerCallback>> iterator = players.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Integer, PlayerCallback> entry = iterator.next();
            try {
                entry.getValue().displayMessage(message);
            } catch (RemoteException e) {
                iterator.remove(); // Remove disconnected players safely
            }
        }

    }

    private void askPlayAgain() throws RemoteException {
        playAgainResponses.clear(); // Clear any previous responses

        for (PlayerCallback callback : players.values()) {
            if (callback != null) {
                try {
                    // Send a structured prompt that the client can parse
                    callback.displayMessage("GAME_OVER|Do you want to play again? (yes/no/quit)");
                } catch (RemoteException e) {
                    System.err.println("Failed to ask player for rematch");
                    players.values().remove(callback);
                }
            }
        }
    }

    @Override
public void ping() throws RemoteException {
    // Empty implementation just for heartbeat checking
}
}
