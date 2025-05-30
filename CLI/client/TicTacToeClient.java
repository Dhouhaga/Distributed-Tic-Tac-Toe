package client;

import common.*;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;

public class TicTacToeClient {
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
        System.setProperty("sun.rmi.transport.tcp.responseTimeout", "30000");
        System.setProperty("sun.rmi.transport.proxy.connectTimeout", "30000");
        System.setProperty("sun.rmi.transport.tcp.handshakeTimeout", "30000");
    }


    public TicTacToeService getGameService() {
        return this.gameService;
    }

    public void handleNewGame() {
        System.out.println("\n--- NEW GAME STARTED ---");
    }

    public void start() {
        try {
            Registry registry;
        try {
            registry = LocateRegistry.getRegistry(serverIP, 1099);
            // Test if registry is actually available
            registry.list(); // This will throw RemoteException if unreachable
        } catch (RemoteException e) {
            System.err.println("\nError: Could not connect to server (server unreachable)");
            System.exit(1);
            return;
        }

            // First lookup the main service to find an available session
            TicTacToeService lookupService = (TicTacToeService) registry.lookup("TicTacToeService1");
            String sessionName="";
            // Get an available session name
            try {
                sessionName = lookupService.connectToAvailableSession();

            } catch (Exception e) {
                System.out.println("Sorry no available sessions.. Please Try Later. \tEXITING.");
                System.exit(0);
            }
            System.out.println("Connecting to session: " + sessionName);
            // Now connect to the actual game session
            gameService = (TicTacToeService) registry.lookup(sessionName);

            // Create and register the callback
            callback = new PlayerCallbackImpl(this, clientIP);
            playerId = gameService.joinGame(callback);

            if (playerId == -1) {
                System.out.println("Could not join game. Exiting...");
                return;
            }

            System.out.println("Type 'quit' at any time to exit the game.");

        } catch (RemoteException e) {
            System.err.println("Error: Could not connect to the server. Please check if the server is running.");
            System.exit(1);
        } catch (Exception e) {
            System.err.println("Error: " + e.toString());
            System.exit(1);
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

    public void displayMessage(String message) {
        if (message.startsWith("GAME_OVER|")) {
            String content = message.substring("GAME_OVER|".length());
            if (content.startsWith("SESSION_END|")) {
                // Handle session termination
                String endMessage = content.substring("SESSION_END|".length());
                System.out.println("\n" + endMessage);
                System.exit(0);
            } else {
                // Handle normal game over prompt
                gameOver(content);
            }
        } else {
            System.out.println(message); // Normal message
        }
    }

    public void quit() {
        try {
            if (gameService != null) {
                gameService.quitGame(playerId);
                System.out.println("Exiting..");
                System.exit(0);
            }
        } catch (RemoteException ignored) {
            System.err.println("Error: Could not notify server about quitting. The server might be down.");
        } finally {
            System.out.println("You have left the game. Goodbye!");
            System.exit(0);
        }
    }
}