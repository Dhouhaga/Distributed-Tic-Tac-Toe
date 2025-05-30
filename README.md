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

## Game Flow

### 1. Client Connection
Players connect via the GUI by entering server details:
- Server IP (e.g., `localhost` for local testing)
- Client IP (auto-detected or manually entered)

![Screenshot 2025-05-30 111544](https://github.com/user-attachments/assets/891e3701-ef60-4f2b-b09a-42c3ba988c4f)
<br>
<br>
**Error Handling:**
- Unreachable servers show connection timeout messages
  ![serverdown](https://github.com/user-attachments/assets/eebbf300-a569-443d-86da-334d59c206c9)
<br>

- Invalid IP formats trigger validation warnings
- Duplicate player names are rejected

### 2. Session Matching
- Server automatically pairs available players
- New game sessions are created on-demand
- Players see "Waiting for opponent" status until matched

### 3. Gameplay Loop
Players take turns making moves on the board:
![Screenshot 2025-04-26 151818](https://github.com/user-attachments/assets/63e97e7a-7ac8-40d2-9b9f-e52a53f6d56d)
<br>
<br>
**Features:**
- Turn indication ("YOUR TURN" vs "Opponent's turn")
- Visual board updates after each move
- Real-time move validation (prevents invalid placements)

### 4. Win/Draw Detection
Game automatically detects end conditions:
- Win: 3-in-a-row patterns
- Draw: Full board with no winner
<table>
  <tr>
    <td>Game Board</td>
     <td>Game Over</td>
  </tr>
  <tr>
    <td>![draw](https://github.com/user-attachments/assets/fef778a8-b9f8-4e7c-9072-848d675a89c1)</td>
    <td>![draw](https://github.com/user-attachments/assets/fef778a8-b9f8-4e7c-9072-848d675a89c1)</td>
  </tr>
 </table>
**Error Recovery:**
- Network drops trigger automatic reconnection attempts
- Missing heartbeats mark players as "disconnected"
- Session timeouts after 30 seconds of inactivity

### 5. Rematch System
After game completion:
- Both players get rematch prompts
- New game starts if both choose "Play Again"
- Session dissolves if either player quits

### 6. Graceful Exit
Players can quit anytime via:
- "Quit Game" button during gameplay
- Window close (X) button (NOT ON WINDOWS)
- Console interrupt (Ctrl+C) for CLI clients

**Termination Sequence:**
1. Client sends disconnect notification
2. Server frees game resources
3. Opponent receives "Player left" notification
4. Session automatically closes after 60 seconds

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

