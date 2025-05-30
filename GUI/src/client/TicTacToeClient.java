package client;

import common.*;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;

public class TicTacToeClient {
    public TicTacToeGUI gui;
    private TicTacToeService gameService;
    private PlayerCallback callback;
    private int playerId;
    private Scanner scanner;
    private String serverIP;
    private String clientIP;

    public TicTacToeClient(TicTacToeService service, String serverIP, String clientIP) {
        this.gameService = service;
        this.serverIP = serverIP;
        this.clientIP = clientIP;
        this.scanner = new Scanner(System.in);
        this.gameService = service;
        System.setProperty("sun.rmi.transport.tcp.responseTimeout", "30000");
        System.setProperty("sun.rmi.transport.proxy.connectTimeout", "30000");
        System.setProperty("sun.rmi.transport.tcp.handshakeTimeout", "30000");
    }
    
    // Add this to the TicTacToeClient class
public void setGameService(TicTacToeService gameService) {
    this.gameService = gameService;
}
    public int getPlayerId() {
        return this.playerId;
    }
    

    public TicTacToeService getGameService() {
        return this.gameService;
    }

public void start() throws RemoteException {
    try {
        Registry registry = LocateRegistry.getRegistry(serverIP, 1099);
        TicTacToeService lookupService = (TicTacToeService) registry.lookup("TicTacToeService1");
        String sessionName = lookupService.connectToAvailableSession();
        gameService = (TicTacToeService) registry.lookup(sessionName);

        callback = new PlayerCallbackImpl(this, clientIP);
        playerId = gameService.joinGame(callback);

        if (playerId == -1) {
            throw new RemoteException("Server is full. Please try again later.");
        }
    } catch (Exception e) {
        throw new RemoteException("Connection error: Sorry, all sessions are full. Try again Later");
    }
}
    public void displayBoard(char[][] board) {
        System.out.println("\nCurrent Board:");
        for (int i = 0; i < GameConstants.BOARD_SIZE; i++) {
            for (int j = 0; j < GameConstants.BOARD_SIZE; j++) {
                System.out.print(" " + board[i][j] + " ");
                if (j < GameConstants.BOARD_SIZE - 1) {
                    System.out.print("|");
                }
            }
            System.out.println();
            if (i < GameConstants.BOARD_SIZE - 1) {
                System.out.println("-----------");
            }
        }
        System.out.println();
    }

    public void notifyYourTurn() {
        System.out.println("\nIt's your turn! Enter row and column (0-2) separated by space:");
        try {
            String input = scanner.nextLine().trim();

            if (input.equalsIgnoreCase("quit")) {
                quit(); // Use the proper quit method
                return;
            }

            String[] parts = input.split(" ");
            if (parts.length != 2) {
                System.out.println("Invalid input. Please enter row and column separated by space.");
                notifyYourTurn();
                return;
            }

            int row = Integer.parseInt(parts[0]);
            int col = Integer.parseInt(parts[1]);

            boolean validMove = gameService.makeMove(playerId, row, col);
            if (!validMove) {
                notifyYourTurn();
            }
        } catch (NumberFormatException e) {
            System.out.println("Please enter numbers only.");
            notifyYourTurn();
        } catch (RemoteException e) {
            System.err.println("Error: Could not reach the server. The game might be over or the server is down.");
            quit();
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            quit(); // Ensure clean exit on any error
        }
    }

    public void gameOver(String message) {
        System.out.println("\n" + message);
        System.out.println("Enter your choice (yes/no/quit):");

        new Thread(() -> {
            try {
                String response = scanner.nextLine().trim().toLowerCase();
                switch (response) {
                    case "quit":
                        quit();
                        break;
                    case "yes":
                    case "no":
                        System.out.println("Sending response: " + response);
                        try {
                            gameService.playAgain(playerId, response.equals("yes"));
                            if (response.equals("no")) {
                                System.out.println("Thank you for playing! Goodbye.");
                                Thread.sleep(1000); // Give time for message to display
                                quit();
                            }
                        } catch (RemoteException e) {
                            System.err.println("Error: Could not reach the server. The game session might have ended.");
                            quit();
                        }
                        break;
                    default:
                        System.out.println("Invalid input. Please try again.");
                        gameOver(message); // Restart the prompt
                }
            } catch (Exception e) {
                System.err.println("Error handling input: " + e.getMessage());
                quit();
            }
        }).start();
    }

    public void handleNewGame() {
        // Empty implementation - GUI handles this
    }

    public void displayMessage(String message) {
    if (message.startsWith("GAME_OVER|")) {
        String content = message.substring("GAME_OVER|".length());
        if (content.startsWith("SESSION_END|")) {
            // This will now show the timed message
            gui.showErrorMessage("Opponent left: " + content.substring("SESSION_END|".length()) + 
                               " Closing in 5 seconds...");
            new Thread(() -> {
                try {
                    Thread.sleep(5000);
                    System.exit(0);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }).start();
        } else {
            gui.gameOver(content);
        }
    } else {
        gui.displayMessage(message);
    }
}
    public void quit() {
    try {
        if (gameService != null && playerId != -1) {
            gameService.quitGame(playerId);  // Explicitly notify server
            System.out.println("Notified server about leaving game");
        }
    } catch (RemoteException e) {
        System.err.println("Error while notifying server: " + e.getMessage());
    } finally {
        System.exit(0);  // Ensure complete shutdown
    }
}
}
