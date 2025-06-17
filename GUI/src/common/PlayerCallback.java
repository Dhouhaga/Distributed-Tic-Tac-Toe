package common;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface PlayerCallback extends Remote {
    void updateBoard(char[][] board) throws RemoteException;
    void notifyTurn() throws RemoteException;
    void gameOver(String message) throws RemoteException;
    void displayMessage(String message) throws RemoteException;
    void notifyNewGame() throws RemoteException; 
}
