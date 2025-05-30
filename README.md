# Distributed-Tic-Tac-Toe

This project implements a distributed Tic Tac Toe game using Java RMI (Remote Method Invocation). Players can connect to a central server, join game sessions, and play against opponents in real-time with automatic matchmaking.

## Features

- **Distributed Architecture:** Server handles game logic, clients manage UI
- **Dynamic Session Management:** Auto-scales game sessions as players connect
- **Rematch System:** Players can restart games after completion
- **Heartbeat Monitoring:** Detects disconnected players automatically
- **Modern GUI:** Clean interface with game board, status updates, and controls
- **Connection Security:** Custom security manager for RMI communication

## Project Structure

| Class                | Purpose                          | Key Features                                  |
|----------------------|----------------------------------|-----------------------------------------------|
| ServerMain           | Server entry point               | Starts RMI registry, registers primary service |
| TicTacToeServer      | Session manager                  | Creates/destroys game sessions, manages player allocation |
| TicTacToeServiceImpl | Game logic core                  | Move validation, win detection, player management |
| ClientMain           | Client entry point               | Launches game GUI                             |
| TicTacToeGUI         | Player interface                 | Game board, status display, input handling    |
| TicTacToeClient      | Client logic                     | Server communication, game state management   |
| PlayerCallbackImpl   | Server→client comms              | Real-time updates, heartbeat monitoring      |
| MySecurityManager    | Security config                  | Enables RMI connections without strict policy files |
| GameConstants        | Shared config                    | Board size, player symbols, game rules       |

## Prerequisites

- Java JDK 17+
- Network connectivity between machines
- RMI ports open (1099 + 1100-1106 by default)

## Setup & Execution

### 1. Compile Code
```bash
javac -d bin src/server/*.java src/client/*.java src/common/*.java src/security/*.java
```
### 2. Start Server
```bash
java -cp bin -Djava.rmi.server.codebase=file:bin/ security.MySecurityManager server.ServerMain 192.168.1.10
```
> [!NOTE]  
>Replace 192.168.1.10 with server's actual IP

### 3. Start Clients
```bash
java -cp bin -Djava.rmi.server.codebase=file:bin/ security.MySecurityManager client.ClientMain
```
> [!NOTE]  
>Run on player machines, enter server IP when prompted

### 4. Game Flow
  1. Clients connect via GUI
  2. Server matches players into sessions
  3. Players take turns making moves
  4. Win/draw triggers rematch prompt
  5. Players can quit anytime

## Configuration Options
| Parameter        	 | Location              | Description                          |
|--------------------|-----------------------|--------------------------------------|
| MAX_SESSIONS       |	TicTacToeServer.java |	Max concurrent game sessions        |
| portBase           |	TicTacToeServer.java |	Starting port for game sessions     |
| sun.rmi.transport.*|	TicTacToeClient.java |	Network timeout settings            |
| Color constants    |	TicTacToeGUI.java    |	UI color scheme                     |

### Key Design Patterns
- Observer Pattern: Callbacks for game state updates
- Singleton Pattern: Central server instance
- Factory Pattern: Dynamic session creation
- Asynchronous Processing: Non-blocking game operations

## Troubleshooting
- Connection Issues: Verify IPs match server's network interface
- Port Conflicts: Change portBase in TicTacToeServer.java
- Session Limits: Increase MAX_SESSIONS if needed
- Timeout Errors: Adjust sun.rmi.transport.* properties

## Security Notes
The MySecurityManager class:
  - Overrides default RMI security policies
  - Allows network connections without policy files
  - Should be replaced with proper security policies in production
    
```bash
java
// security/MySecurityManager.java
public void checkConnect(String host, int port) {
    // Allow all connections (development only)
}
```
