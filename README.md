# Bitcoin OP_RETURN Data Indexing Project

This project indexes OP_RETURN data from Bitcoin's signet blockchain and exposes it through a REST API. The project uses Kotlin and Ktor to create the API and PostgreSQL to store the OP_RETURN data.The app also ensures that new blocks are synced and indexed automatically.


##  Key Features:

	1.	OP_RETURN Data Indexing: Extract OP_RETURN data from Bitcoin Signet blockchain transactions and store them in PostgreSQL.
	2.	REST API: Serve the indexed data via an HTTP endpoint /opreturn/{opReturnData}, which returns the associated transaction and block hashes.
	3.	Automatic Block Syncing: Sync and index new blocks automatically as they are mined on the Signet network.

## Architecture Overview

High-Level Architecture Diagram

+-------------------+          +----------------+         +-----------------+
|                   |          |                |         |                 |
|   Bitcoin Core    |  <-----> |   REST API      |  <----->|  PostgreSQL DB  |
|                   |          |   (Ktor + Kotlin)|         |                 |
+-------------------+          +----------------+         +-----------------+
   |                                                       |
   v                                                       v
+-----------------------------------+          +------------------------------------+
| OP_RETURN data extracted from     |          |   Data stored in `op_return_index` |
| Signet blockchain transactions    |          |   table with transaction, block,  |
| and served via REST API           |          |   and OP_RETURN data              |
+-----------------------------------+          +------------------------------------+


## Components:

	1.	Bitcoin Core: Connects to the Bitcoin Signet network, providing blockchain data via RPC.
	2.	Ktor API Server: A Kotlin-based REST API built with Ktor, serving OP_RETURN data.
	3.	PostgreSQL: Stores the indexed OP_RETURN data and keeps track of the latest synced block.
	4.	HikariCP Connection Pool: Manages database connections efficiently.


## Project Structure

- **BitcoinRpcClient.kt**: Handles Bitcoin Core RPC requests to fetch blocks and transactions.
- **DatabaseHelper.kt**: Manages PostgreSQL database connections, inserts OP_RETURN data, and keeps track of the latest indexed block.
- **ApiServer.kt**: Defines the REST API endpoint `/opreturn/{opReturnData}` to retrieve transactions with OP_RETURN data.
- **BlockSyncer.kt**: Periodically syncs new blocks and indexes OP_RETURN data.

```
.
├── src
│   ├── main
│   │   ├── kotlin
│   │   │   ├── org
│   │   │   │   └── example
│   │   │   │       ├── BitcoinRpcClient.kt
│   │   │   │       ├── DatabaseHelper.kt
│   │   │   │       ├── ApiServer.kt
│   │   │   │       ├── BlockSyncer.kt
│   ├── resources
│   │   └── application.conf
├── build.gradle
└── README.md
```

##  Project Flow:

1. **Block Sync Scheduler**: The `startBlockSyncScheduler()` will run in the background, periodically fetching new blocks and indexing their OP_RETURN data.
2. **Ktor API Server**: The Ktor server will start at `localhost:8080` and handle requests to the `/opreturn/{opReturnData}` endpoint.


##  Environment Variables
To properly configure the environment variables for your application, you should create a .env file or export the environment variables directly depending on your environment. Here’s a breakdown of the sensitive and configurable data that should be placed in the environment variables:

```bash
# Bitcoin Core RPC Config
RPC_USER=my_rpc_user
RPC_PASSWORD=my_secure_rpc_password

# PostgreSQL Database Config
DB_URL=jdbc:postgresql://localhost:5432/bitcoin_opreturn
DB_USER=my_db_user
DB_PASSWORD=my_secure_db_password

# Block sync interval (in milliseconds, optional)
SYNC_INTERVAL_MS=60000  # Default to 60 seconds
```

## to use env variables in local developement
You can export these environment variables in your terminal session before running your application.

```bash
export RPC_USER="my_rpc_user"
export RPC_PASSWORD="my_secure_rpc_password"
export DB_URL="jdbc:postgresql://localhost:5432/bitcoin_opreturn"
export DB_USER="my_db_user"
export DB_PASSWORD="my_secure_db_password"
export SYNC_INTERVAL_MS="60000"
```

## Best Practises used
1.	Error Handling: Wrap critical sections in try-catch blocks with meaningful logging.
2.	Logging: Use SLF4J with Logback for proper logging.
3.	Environment Variables: Use environment variables to secure sensitive data.
4.	Connection Pooling: Leverage HikariCP for efficient database connection handling.
5.	Resiliency: Ensure that background processes (like block syncing) handle errors gracefully without crashing the application.

## Prerequisites

### 1. Bitcoin Core Configuration

Make sure you have **Bitcoin Core** running in **signet mode** with RPC enabled. You need to configure the `bitcoin.conf` file, which is typically located in:

- On macOS: `~/Library/Application Support/Bitcoin/bitcoin.conf`
- On Linux: `~/.bitcoin/bitcoin.conf`
- On Windows: `C:\Users\YourUsername\AppData\Roaming\Bitcoin\bitcoin.conf`

Here's an example `bitcoin.conf` file configuration:

```bash
server=1
signet=1
rpcuser=your_rpc_user
rpcpassword=your_rpc_password
rpcallowip=127.0.0.1
rpcbind=127.0.0.1
```
Restart Bitcoin Core after making these changes:
```bash
./bitcoin-qt -signet
```

### 2. PostgreSQL Setup
Create a PostgreSQL database named **bitcoin_opreturn** and set up the required tables. You can do this by connecting to your PostgreSQL instance and running the following SQL queries:

```sql
CREATE DATABASE bitcoin_opreturn;
\c bitcoin_opreturn  -- Switch to the database
CREATE TABLE op_return_index (
    id SERIAL PRIMARY KEY,
    op_return_data TEXT,
    transaction_hash TEXT,
    block_hash TEXT
);

CREATE TABLE block_sync_status (
    id SERIAL PRIMARY KEY,
    latest_block_height INT
);
```

### 3. Kotlin and Dependencies
Ensure you have **Kotlin** installed and the project uses Gradle for dependency management. You need to include the following dependencies in your build.gradle.kts file:

```kotlin
dependencies {
    implementation("io.ktor:ktor-server-core:2.0.0")
    implementation("io.ktor:ktor-server-netty:2.0.0")
    implementation("io.ktor:ktor-serialization-kotlinx-json:2.0.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.2")
    implementation("org.postgresql:postgresql:42.3.1")
}
```

## Running the Project
### 1. Start the Ktor API
To start the Ktor API, navigate to the project directory and run:
```bash
./gradlew run
```
### 2. Access the API
You can query for transactions by OP_RETURN data using Postman or curl:
```bash
GET http://localhost:8080/opreturn/{opReturnData}
```
### 3. Sync New Blocks
The block syncer runs automatically every 60 seconds, indexing new blocks and their OP_RETURN data.

## Example API Request
Here’s an example API request using curl:
```bash
curl -X GET http://localhost:8080/opreturn/6a6f686e646f65
```
This will return the transactions and block hashes containing the OP_RETURN data matching the provided value (6a6f686e646f65).

## Dependencies
- 	Ktor: For building the REST API.
- 	PostgreSQL: For storing OP_RETURN data.
- 	Bitcoin Core: To fetch blocks and transactions via RPC.

