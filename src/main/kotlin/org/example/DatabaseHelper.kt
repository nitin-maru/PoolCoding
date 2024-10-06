package org.example.org.example

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import java.sql.Connection
import java.sql.DriverManager
import javax.sql.DataSource

object DatabaseHelper {
    private val logger = LoggerFactory.getLogger(DatabaseHelper::class.java)
//    val config = environment.config

//    // Create HikariCP DataSource
//    // Initialize DataSource using HikariCP
//    private val dataSource: DataSource = HikariDataSource(HikariConfig().apply {
//        val dbUrl = System.getenv("DB_URL") ?: "jdbc:postgresql://localhost:5432/bitcoin_opreturn"
//        val dbUser = System.getenv("DB_USER") ?: "nitin"
//        val dbPassword = System.getenv("DB_PASSWORD") ?: "nitin"
//
//        // Log database connection details (not password for security reasons)
//        logger.info("Initializing database connection with URL: $dbUrl and user: $dbUser")
//
//        jdbcUrl = dbUrl
//        username = dbUser
//        password = dbPassword
//        maximumPoolSize = 10
//
//        // Optional logging to troubleshoot issues with connection pool configuration
//        logger.info("HikariCP configuration: Max Pool Size = $maximumPoolSize")
//    })

    // Lazy initialization for HikariCP DataSource
    private val dataSource: DataSource by lazy {
        try {
            val dbUrl = System.getenv("DB_URL") ?: "jdbc:postgresql://localhost:5432/bitcoin_opreturn"
            val dbUser = System.getenv("DB_USER") ?: "nitin"
            val dbPassword = System.getenv("DB_PASSWORD") ?: "nitin"

            // Log the database connection information (but not the password)
            logger.info("Initializing HikariCP DataSource with URL: $dbUrl and user: $dbUser")

            val config = HikariConfig().apply {
                jdbcUrl = dbUrl
                username = dbUser
                password = dbPassword
                maximumPoolSize = 10
                isAutoCommit = false // Example setting, tune this as needed
            }
            logger.info("HikariCP configuration created successfully with pool size: ${config.maximumPoolSize}")
            val ds = HikariDataSource(config)
            logger.info("HikariCP DataSource initialized successfully")
            ds

        } catch (e: Exception) {
            logger.error("Error initializing DataSource", e)
            throw RuntimeException("Failed to initialize DataSource", e)
        }
    }

    // Store OP_RETURN Data
    fun storeOpReturnDataInDb(opReturnDataList: List<Map<String, String>>) {
        try {
            dataSource.connection.use { connection ->
                val preparedStatement = connection.prepareStatement(
                    "INSERT INTO op_return_index (op_return_data, transaction_hash, block_hash) VALUES (?, ?, ?)"
                )
                for (data in opReturnDataList) {
                    preparedStatement.setString(1, data["opReturnData"])
                    preparedStatement.setString(2, data["txid"])
                    preparedStatement.setString(3, data["blockhash"])
                    preparedStatement.addBatch()
                }
                preparedStatement.executeBatch()
            }
        } catch (e: Exception) {
            logger.error("Error storing OP_RETURN data in DB", e)
        }
    }

    // Get latest indexed block height
    suspend fun getLatestIndexedBlockHeight(): Int = withContext(Dispatchers.IO) {
        var latestBlockHeight = 0
        try {
            dataSource.connection.use { connection ->
                val statement = connection.createStatement()
                val resultSet = statement.executeQuery("SELECT latest_block_height FROM block_sync_status LIMIT 1")
                if (resultSet.next()) {
                    latestBlockHeight = resultSet.getInt("latest_block_height")
                }
            }
        } catch (e: Exception) {
            logger.error("Error fetching latest indexed block height", e)
        }
        latestBlockHeight
    }

    // Update latest indexed block height
    fun updateLatestIndexedBlockHeight(newBlockHeight: Int) {
        try {
            dataSource.connection.use { connection ->
                val preparedStatement =
                    connection.prepareStatement("UPDATE block_sync_status SET latest_block_height = ?")
                preparedStatement.setInt(1, newBlockHeight)
                preparedStatement.executeUpdate()
            }
        } catch (e: Exception) {
            logger.error("Error updating latest indexed block height", e)
        }
    }
}