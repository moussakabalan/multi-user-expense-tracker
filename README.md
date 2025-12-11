# Multi-User Expense Tracker

This project delivers a **networked expense tracking system** using **Java** with a **JavaFX client** and a **Java server** that handles shared budget/account data.
The server uses **file-based JSON storage** and supports **multiple concurrent clients** through multithreading.

> 📝 **Note:** This was built as a collaborative group project following a structured 10-day development plan. The codebase is organized with clear separation of concerns between client, server, and common modules.

---

## 🛠️ Technologies Used
- **Language:** Java 21
- **Libraries:** JavaFX 21, Gson 2.10.1
- **Datastore:** JSON files (file-based storage)
- **Environment:** Cross-platform (Windows, Linux, macOS)
- **Networking:** Java Sockets, Multithreading

---

## 📂 Project Structure
| Directory/File | Description |
|----------------|-------------|
| `src/client/` | JavaFX client application with GUI |
| `src/server/` | Java server with socket handling and multithreading |
| `src/common/` | Shared models and utilities (Expense, JSON, Protocol) |
| `src/client/Client.java` | Main JavaFX application entry point |
| `src/client/ClientConnection.java` | Handles socket communication with server |
| `src/client/styles.css` | CSS styling for JavaFX UI |
| `src/server/Server.java` | Main server entry point, accepts client connections |
| `src/server/ClientHandler.java` | Handles individual client requests (thread per client) |
| `src/server/ExpenseStorage.java` | Manages file-based JSON storage with thread safety |
| `src/common/Expense.java` | Data model for expenses |
| `src/common/ExpenseJson.java` | JSON serialization/deserialization |
| `src/common/ExpenseProtocol.java` | Server protocol message parsing |
| `data/` | Directory where user expense JSON files are stored |
| `pom.xml` | Maven configuration and dependencies |

---

## 📦 Prerequisites
- **Java 21+** (JDK)
- **IntelliJ IDEA** (recommended) or any Java IDE
- **JavaFX SDK** (included via IntelliJ IDEA; *may be required to download!*)

---

## 🚀 How to Run

### 1. Build the Project
First, compile the project with your IDE of choice:
*In this example, we'll assume your using IntelliJ IDEA*

### 2. Start the Server
Run the server first:
```bash
# In IntelliJ: Run src/server/Server.java
```

You should see:
```
========================================
   Expense Tracker Server v1.1
========================================
[INIT] Initializing storage system...
[SERVER] ✓ Server started successfully on port 5000
[SERVER] Waiting for client connections...
```

### 3. Start the Client
In a separate terminal/run configuration:
```bash
# In IntelliJ: Run src/client/Client.java
```

The JavaFX application window should open with a login screen.

### 4. Using the Application
1. **Login:** Enter a username (no password required)
2. **Add Expense:** Click "Add Expense" → Fill form → Submit
3. **View Expenses:** Click "View Expenses" → See table and pie chart
4. **Multiple Clients:** Open multiple client windows to test concurrent users

---

## 🔌 Communication Protocol

The client and server communicate using a **pipe-delimited protocol** over TCP sockets (port 5000).

### Client → Server Commands

| Command | Format | Example |
|---------|--------|---------|
| `ADD_EXPENSE` | `ADD_EXPENSE\|username\|amount\|category\|date\|note` | `ADD_EXPENSE\|alice\|25.50\|Food\|2024-01-15\|Lunch` |
| `GET_EXPENSES` | `GET_EXPENSES\|username` | `GET_EXPENSES\|alice` |
| `QUIT` | `QUIT` | `QUIT` |

### Server → Client Responses

| Response | Format | Description |
|----------|--------|-------------|
| `SUCCESS` | `SUCCESS\|message` or `SUCCESS\|count\njson1\njson2...` | Operation succeeded |
| `ERROR` | `ERROR\|message` | Operation failed with error message |
| `CONNECTION SUCCESSFUL` | `CONNECTION SUCCESSFUL\|message` | Sent on initial connection |

### Example Flow
```
Client: ADD_EXPENSE|alice|25.50|Food|2024-01-15|Lunch
Server: SUCCESS|Expense added successfully

Client: GET_EXPENSES|alice
Server: SUCCESS|2
Server: {"amount":25.5,"category":"Food","date":"2024-01-15","note":"Lunch"}
Server: {"amount":10.0,"category":"Transport","date":"2024-01-16","note":"Bus"}
```

---

## 🎨 Features

### Client Features
- ✅ **Login Screen:** Simple username-based authentication
- ✅ **Add Expense Form:** Amount, category dropdown, date picker, notes
- ✅ **Expense Table:** View all expenses in a sortable table
- ✅ **Pie Chart:** Visual breakdown of spending by category
- ✅ **Error Handling:** Alert dialogs for network errors and validation
- ✅ **Modern UI:** CSS-styled JavaFX interface

### Server Features
- ✅ **Multithreading:** Handles multiple clients concurrently
- ✅ **Persistent Storage:** JSON file-based storage per user
- ✅ **Thread Safety:** Synchronized operations for data integrity
- ✅ **Comprehensive Logging:** Detailed console logs for debugging
- ✅ **Error Recovery:** Graceful handling of client disconnections

---

## 📊 Development Timeline

This project was developed over 10 days following a structured plan:

- **Day 1:** Project setup, folder structure, protocol definition
- **Day 2:** Data model (Expense class) with JSON serialization
- **Day 3:** Server base with multithreading
- **Day 4:** Server storage system (file-based JSON)
- **Day 5:** Client networking layer
- **Day 6:** JavaFX UI (login, dashboard)
- **Day 7:** Add expense form, TableView, charts
- **Day 8:** Charts, UI polish, error handling
- **Day 9:** Full integration testing, bug fixes
- **Day 10:** Documentation (this README)

***Will update with proper screenshots and details!***

---

## 📝 License

This project was created for educational purposes as part of a group collaboration project.

---

## 👥 Contributors

Built collaboratively by a team following a structured development plan with clear role assignments and daily deliverables.

