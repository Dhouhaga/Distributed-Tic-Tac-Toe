package common;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface TicTacToeService extends Remote {
    void ping() throws RemoteException;
    String connectToAvailableSession() throws RemoteException;
    int joinGame(PlayerCallback callback) throws RemoteException;
    boolean makeMove(int playerId, int row, int col) throws RemoteException;
    boolean playAgain(int playerId, boolean response) throws RemoteException;
    void quitGame(int playerId) throws RemoteException;
}