package client;

import javax.swing.*;
import common.TicTacToeService;
import java.awt.*;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import common.GameConstants;

public class TicTacToeGUI {
    private TicTacToeClient client;
    private JFrame gameFrame;
    private JFrame connectionFrame;
    private JButton[][] buttons = new JButton[3][3];
    private JLabel statusLabel;
    private JTextField serverIpField;
    private JTextField clientIpField;

private JButton playAgainButton;
private JButton quitButton;

    // Simple color scheme
    private final Color PRIMARY_COLOR = new Color(70, 130, 180);  // Steel blue
    private final Color SECONDARY_COLOR = new Color(255, 255, 255);  // White
    private final Color ACCENT_COLOR = new Color(50, 205, 50);  // Lime green
    private final Color ERROR_COLOR = new Color(220, 20, 60);  // Crimson
    private final Color BOARD_COLOR = new Color(240, 240, 240);  // Light gray

    public TicTacToeGUI() {
        createConnectionPanel();
    }

    // Add these public methods to TicTacToeGUI
public void setStatusMessage(String message, Color color) {
    SwingUtilities.invokeLater(() -> {
        statusLabel.setText(message);
        statusLabel.setForeground(color);
    });
}

public void disableBoard() {
    enableBoard(false);
}

public void enableBoard(boolean enable) {
    SwingUtilities.invokeLater(() -> {
        for (JButton[] row : buttons) {
            for (JButton button : row) {
                button.setEnabled(enable);
            }
        }
    });
}

    private void createConnectionPanel() {
        connectionFrame = new JFrame("Connect to Server");
        connectionFrame.setSize(350, 200);
        connectionFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        connectionFrame.setLayout(new BorderLayout(10, 10));
        connectionFrame.getContentPane().setBackground(SECONDARY_COLOR);

        JPanel inputPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        inputPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));
        inputPanel.setBackground(SECONDARY_COLOR);

        // Server IP Field
        JLabel serverLabel = createBoldLabel("Server IP:");
        inputPanel.add(serverLabel);

        serverIpField = new JTextField("localhost");
        styleTextField(serverIpField);
        inputPanel.add(serverIpField);

        // Client IP Field
        JLabel clientLabel = createBoldLabel("Client IP:");
        inputPanel.add(clientLabel);

        clientIpField = new JTextField("localhost");
        styleTextField(clientIpField);
        inputPanel.add(clientIpField);

        // Connect Button
        JButton connectButton = new JButton("CONNECT");
        styleButton(connectButton, ACCENT_COLOR);
        connectButton.addActionListener(e -> connectToServer());

        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(SECONDARY_COLOR);
        buttonPanel.add(connectButton);

        connectionFrame.add(inputPanel, BorderLayout.CENTER);
        connectionFrame.add(buttonPanel, BorderLayout.SOUTH);
        connectionFrame.setVisible(true);
    }

    private JLabel createBoldLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Arial", Font.BOLD, 14));
        label.setForeground(PRIMARY_COLOR);
        return label;
    }

    private void styleTextField(JTextField field) {
        field.setFont(new Font("Arial", Font.PLAIN, 14));
        field.setBackground(Color.WHITE);
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(PRIMARY_COLOR, 1),
            BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));
    }

    private void styleButton(JButton button, Color bgColor) {
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 25, 10, 25));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(PRIMARY_COLOR, 1),
            BorderFactory.createEmptyBorder(8, 20, 8, 20)
        ));
    }

    private void connectToServer() {
    try {
        String serverIP = serverIpField.getText();
        String clientIP = clientIpField.getText();

        this.client = new TicTacToeClient(null, serverIP, clientIP);
        client.gui = this;

        Registry registry = LocateRegistry.getRegistry(serverIP, 1099);
        TicTacToeService service = (TicTacToeService) registry.lookup("TicTacToeService1");

        client.setGameService(service);

        // Attempt to start the client (now propagates errors)
        client.start(); 

        // Close connection window only if successful
        connectionFrame.dispose();
        initializeGameGUI();
        statusLabel.setText("Waiting for opponent...");
    } catch (RemoteException e) {
        // Show error in connection window
        JOptionPane.showMessageDialog(
            connectionFrame,
            e.getMessage(),
            "Connection Error",
            JOptionPane.ERROR_MESSAGE
        );
    } catch (Exception ex) {
        JOptionPane.showMessageDialog(
            connectionFrame,
            "Connection failed: " + ex.getMessage(),
            "Error",
            JOptionPane.ERROR_MESSAGE
        );
    }
}
   private void initializeGameGUI() {
    gameFrame = new JFrame("Tic Tac Toe");
    gameFrame.setSize(400, 500); // Increased height to fit button
    gameFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    gameFrame.setLayout(new BorderLayout(10, 10));
    gameFrame.getContentPane().setBackground(SECONDARY_COLOR);

    // Game Board (unchanged)
    JPanel boardPanel = new JPanel(new GridLayout(3, 3, 5, 5));
    boardPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20)); // Reduced bottom padding
    boardPanel.setBackground(BOARD_COLOR);
    
    for (int i = 0; i < 3; i++) {
        for (int j = 0; j < 3; j++) {
            buttons[i][j] = new JButton("");
            buttons[i][j].setFont(new Font("Arial", Font.BOLD, 48));
            buttons[i][j].setBackground(Color.WHITE);
            buttons[i][j].setForeground(PRIMARY_COLOR);
            buttons[i][j].setFocusPainted(false);
            buttons[i][j].setBorder(BorderFactory.createLineBorder(PRIMARY_COLOR, 2));
            
            final int row = i;
            final int col = j;
            buttons[i][j].addActionListener(e -> makeMove(row, col));
            boardPanel.add(buttons[i][j]);
        }
    }

    // Status Bar (unchanged)
    statusLabel = new JLabel("", SwingConstants.CENTER);
    statusLabel.setFont(new Font("Arial", Font.BOLD, 14));
    statusLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
    statusLabel.setOpaque(true);
    statusLabel.setBackground(SECONDARY_COLOR);

    // Add Leave Button Panel (at the bottom)
    JPanel leavePanel = new JPanel();
    leavePanel.setBackground(SECONDARY_COLOR);
    
    JButton leaveButton = new JButton("Leave Game");
    leaveButton.setFont(new Font("Arial", Font.BOLD, 14));
    leaveButton.setBackground(ERROR_COLOR);
    leaveButton.setForeground(Color.WHITE);
    leaveButton.addActionListener(e -> {
        if (client != null) {
            client.quit(); // This now properly notifies server
        }
        gameFrame.dispose();
        System.exit(0); // Force exit
    });
    leavePanel.add(leaveButton);

    // Add components to frame
    gameFrame.add(boardPanel, BorderLayout.CENTER);
    gameFrame.add(statusLabel, BorderLayout.SOUTH);
    gameFrame.add(leavePanel, BorderLayout.NORTH); // Button at top

    gameFrame.setVisible(true);
}

    private void makeMove(int row, int col) {
        try {
            boolean validMove = client.getGameService().makeMove(client.getPlayerId(), row, col);
            if(validMove) {
                showOpponentTurn();
            }
        } catch (RemoteException ex) {
            if(ex.getMessage().contains("not your turn")) {
                showErrorMessage("Wait for your turn!");
            } else {
                showErrorMessage("Error making move: " + ex.getMessage());
            }
        }
    }

    public void updateBoard(char[][] board) {
        SwingUtilities.invokeLater(() -> {
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    buttons[i][j].setText(String.valueOf(board[i][j]));
                    buttons[i][j].setEnabled(board[i][j] == GameConstants.EMPTY);
                    buttons[i][j].setForeground(
                        board[i][j] == 'X' ? ACCENT_COLOR : PRIMARY_COLOR
                    );
                }
            }
        });
    }

    public void notifyYourTurn() {
        SwingUtilities.invokeLater(() -> {
            statusLabel.setText("YOUR TURN - Make your move!");
            statusLabel.setForeground(ACCENT_COLOR);
            enableBoard(true);
        });
    }

public void gameOver(String message) {
    SwingUtilities.invokeLater(() -> {
        // Clear the game frame completely
        gameFrame.getContentPane().removeAll();
        
        // Create main panel with vertical box layout
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(SECONDARY_COLOR);

        // Game over message - now at top with bigger font
        JLabel messageLabel = new JLabel("<html><div style='text-align:center;font-size:20px;'>" + 
                                       message + "</div></html>", SwingConstants.CENTER);
        messageLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 30, 0));
        messageLabel.setFont(new Font("Arial", Font.BOLD, 20));
        mainPanel.add(messageLabel, BorderLayout.NORTH);

        // Status message label (for waiting/error messages) - also at top but below main message
        statusLabel = new JLabel(" ", SwingConstants.CENTER);
        statusLabel.setFont(new Font("Arial", Font.BOLD, 16));
        statusLabel.setForeground(PRIMARY_COLOR);
        statusLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        mainPanel.add(statusLabel, BorderLayout.CENTER);

        // Button panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(SECONDARY_COLOR);
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 0));

        // Play Again button
        playAgainButton = new JButton("Play Again");
        styleButton(playAgainButton, ACCENT_COLOR);
        playAgainButton.addActionListener(e -> {
            try {
                client.getGameService().playAgain(client.getPlayerId(), true);
                statusLabel.setText("Waiting for opponent's response...");
                statusLabel.setForeground(PRIMARY_COLOR);
                playAgainButton.setEnabled(false);
                quitButton.setEnabled(false);
            } catch (RemoteException ex) {
                statusLabel.setText("Error sending response");
                statusLabel.setForeground(ERROR_COLOR);
            }
        });

        // Quit button
        quitButton = new JButton("Quit Game");
        styleButton(quitButton, ERROR_COLOR);
        quitButton.addActionListener(e -> {
            try {
                client.getGameService().playAgain(client.getPlayerId(), false);
                System.exit(0);
            } catch (RemoteException ex) {
                statusLabel.setText("Error quitting game");
                statusLabel.setForeground(ERROR_COLOR);
            }
        });

        buttonPanel.add(playAgainButton);
        buttonPanel.add(quitButton);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        // Add to frame and refresh
        gameFrame.add(mainPanel);
        gameFrame.revalidate();
        gameFrame.repaint();
    });
}

/*
  public void gameOver(String message) {
    SwingUtilities.invokeLater(() -> {
        // Clear the game frame completely
        gameFrame.getContentPane().removeAll();
        
        // Create main panel with vertical box layout
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(SECONDARY_COLOR);

        // Game over message
        JLabel messageLabel = new JLabel("<html><div style='text-align:center;font-size:16px;'>" + 
                                       message + "</div></html>");
        messageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        messageLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 30, 0));
        mainPanel.add(messageLabel);

        // Error message label (for timed messages)
        JLabel errorLabel = new JLabel(" ");
        errorLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        errorLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        errorLabel.setForeground(ERROR_COLOR);
        errorLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        mainPanel.add(errorLabel);

        // Button panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(SECONDARY_COLOR);
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 0));

        // Play Again button
        playAgainButton = new JButton("Play Again");
        styleButton(playAgainButton, ACCENT_COLOR);
        playAgainButton.addActionListener(e -> {
            try {
                client.getGameService().playAgain(client.getPlayerId(), true);
                errorLabel.setText("Waiting for opponent's response...");
                errorLabel.setForeground(PRIMARY_COLOR);
                playAgainButton.setEnabled(false);
                quitButton.setEnabled(false);
            } catch (RemoteException ex) {
                errorLabel.setText("Error sending response");
                errorLabel.setForeground(ERROR_COLOR);
            }
        });

        // Quit button
        quitButton = new JButton("Quit Game");
        styleButton(quitButton, ERROR_COLOR);
        quitButton.addActionListener(e -> {
            try {
                client.getGameService().playAgain(client.getPlayerId(), false);
                System.exit(0);
            } catch (RemoteException ex) {
                errorLabel.setText("Error quitting game");
                errorLabel.setForeground(ERROR_COLOR);
            }
        });

        buttonPanel.add(playAgainButton);
        buttonPanel.add(quitButton);
        mainPanel.add(buttonPanel);

        // Add to frame and refresh
        gameFrame.add(mainPanel, BorderLayout.CENTER);
        gameFrame.revalidate();
        gameFrame.repaint();
    });
}
*/
public void resetGameBoard() {
    SwingUtilities.invokeLater(() -> {
        initializeGameBoard();
        statusLabel.setText("Waiting for opponent...");
        statusLabel.setForeground(PRIMARY_COLOR);
    });
}
    
private void initializeGameBoard() {
    // Clear the frame first
    gameFrame.getContentPane().removeAll();
    
    // Create board panel
    JPanel boardPanel = new JPanel(new GridLayout(3, 3, 5, 5));
    boardPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
    boardPanel.setBackground(BOARD_COLOR);

    // Reinitialize buttons
    buttons = new JButton[3][3];
    for (int i = 0; i < 3; i++) {
        for (int j = 0; j < 3; j++) {
            buttons[i][j] = new JButton("");
            buttons[i][j].setFont(new Font("Arial", Font.BOLD, 48));
            buttons[i][j].setBackground(Color.WHITE);
            buttons[i][j].setForeground(PRIMARY_COLOR);
            buttons[i][j].setFocusPainted(false);
            buttons[i][j].setBorder(BorderFactory.createLineBorder(PRIMARY_COLOR, 2));
            
            final int row = i;
            final int col = j;
            buttons[i][j].addActionListener(e -> makeMove(row, col));
            boardPanel.add(buttons[i][j]);
        }
    }

    // Recreate the leave button panel (same as in initializeGameGUI)
    JPanel leavePanel = new JPanel();
    leavePanel.setBackground(SECONDARY_COLOR);
    
    JButton leaveButton = new JButton("Leave Game");
    leaveButton.setFont(new Font("Arial", Font.BOLD, 14));
    leaveButton.setBackground(ERROR_COLOR);
    leaveButton.setForeground(Color.WHITE);
    leaveButton.addActionListener(e -> {
        if (client != null) {
            client.quit();
        }
        gameFrame.dispose();
        System.exit(0);
    });
    leavePanel.add(leaveButton);

    // Reinitialize status label if needed
    if (statusLabel == null) {
        statusLabel = new JLabel("", SwingConstants.CENTER);
        statusLabel.setFont(new Font("Arial", Font.BOLD, 14));
        statusLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        statusLabel.setOpaque(true);
        statusLabel.setBackground(SECONDARY_COLOR);
    }

    // Add components to frame (same layout as initializeGameGUI)
    gameFrame.add(boardPanel, BorderLayout.CENTER);
    gameFrame.add(statusLabel, BorderLayout.SOUTH);
    gameFrame.add(leavePanel, BorderLayout.NORTH);

    gameFrame.revalidate();
    gameFrame.repaint();
}
    

    // Change from private to public
public void showErrorMessage(String message) {
    statusLabel.setText(message);
    statusLabel.setForeground(ERROR_COLOR);
}

public void showTimedErrorMessage(String message, int delayMillis) {
    SwingUtilities.invokeLater(() -> {
        // Set larger font for the error message
        statusLabel.setFont(new Font("Arial", Font.BOLD, 16));
        statusLabel.setText(message);
        statusLabel.setForeground(ERROR_COLOR);
        
        // Create a timer to exit after delay
        new Timer(delayMillis, e -> {
            System.exit(0);
        }).start();
    });
}

/*
public void showTimedErrorMessage(String message, int delayMillis) {
    SwingUtilities.invokeLater(() -> {
        // Check if we're in the game over interface
        boolean inGameOver = gameFrame.getContentPane().getComponentCount() > 0 && 
                           gameFrame.getContentPane().getComponent(0) instanceof JPanel;
        
        if (inGameOver) {
            // Disable buttons if they exist
            if (playAgainButton != null) playAgainButton.setEnabled(false);
            if (quitButton != null) quitButton.setEnabled(false);
            
            // Find the error label in the game over interface
            JPanel mainPanel = (JPanel) gameFrame.getContentPane().getComponent(0);
            for (Component comp : mainPanel.getComponents()) {
                if (comp instanceof JLabel && comp != statusLabel) {
                    JLabel errorLabel = (JLabel) comp;
                    errorLabel.setText(message);
                    errorLabel.setForeground(ERROR_COLOR);
                    
                    // Create a timer to exit after delay
                    new Timer(delayMillis, e -> {
                        System.exit(0);
                    }).start();
                    return;
                }
            }
        }
        
        // Default behavior for regular game interface
        statusLabel.setText(message);
        statusLabel.setForeground(ERROR_COLOR);
        
        // Create a timer to exit after delay
        new Timer(delayMillis, e -> {
            System.exit(0);
        }).start();
    });
}*/

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new TicTacToeGUI());
    }

    public void showOpponentTurn() {
        SwingUtilities.invokeLater(() -> {
            statusLabel.setText("Opponent's turn - Waiting...");
            statusLabel.setForeground(PRIMARY_COLOR);
            enableBoard(false);
        });
    }

    public void displayMessage(String message) {
        SwingUtilities.invokeLater(() -> {
            if (message.equals("It's not your turn!")) {
                showErrorMessage("Wait for your turn!");
            } 
            // Don't show game over messages in status label - handled by gameOver()
            else if (!message.startsWith("GAME_OVER|")) {
                statusLabel.setText(message);
                statusLabel.setForeground(PRIMARY_COLOR);
            }
        });
    }

    // Add these getter methods to TicTacToeGUI
public Color getPrimaryColor() {
    return PRIMARY_COLOR;
}

public Color getAccentColor() {
    return ACCENT_COLOR;
}

public Color getErrorColor() {
    return ERROR_COLOR;
}
}
