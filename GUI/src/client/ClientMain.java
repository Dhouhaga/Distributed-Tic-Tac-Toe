package client;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import common.TicTacToeService;
import java.rmi.Naming;

public class ClientMain {
    public static void main(String[] args) {
        new TicTacToeGUI();
    }
}