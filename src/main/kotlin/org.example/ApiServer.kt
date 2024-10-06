package org.example.org.example

import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.plugins.contentnegotiation.*
import kotlinx.serialization.json.Json
import java.sql.Connection
import java.sql.DriverManager

fun main() {
    // Start the block sync scheduler to periodically sync new blocks
    BlockSyncer.startBlockSyncScheduler()

    embeddedServer(Netty, port = 8080) {
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
            })
        }

        routing {
            get("/opreturn/{opReturnData}") {
                val opReturnData = call.parameters["opReturnData"]

                if (opReturnData != null) {
                    val transactions = findTransactionsByOpReturn(opReturnData)
                    if (transactions.isNotEmpty()) {
                        call.respond(transactions)
                    } else {
                        call.respond(HttpStatusCode.NotFound, "No transactions found for OP_RETURN data")
                    }
                } else {
                    call.respond(HttpStatusCode.BadRequest, "OP_RETURN data is missing")
                }
            }
        }
    }.start(wait = true)
}

fun findTransactionsByOpReturn(opReturnData: String): List<Map<String, String>> {

    val dbUrl = System.getenv("DB_URL") ?: "jdbc:postgresql://localhost:5432/bitcoin_opreturn"
    val dbUser = System.getenv("DB_USER") ?: "nitin"
    val dbPassword = System.getenv("DB_PASSWORD") ?: "nitin"
    val connection: Connection = DriverManager.getConnection(dbUrl, dbUser, dbPassword)
    val preparedStatement = connection.prepareStatement(
        "SELECT transaction_hash, block_hash FROM op_return_index WHERE op_return_data = ?"
    )
    preparedStatement.setString(1, opReturnData)

    val resultSet = preparedStatement.executeQuery()
    val transactions = mutableListOf<Map<String, String>>()

    while (resultSet.next()) {
        transactions.add(
            mapOf(
                "transactionHash" to resultSet.getString("transaction_hash"),
                "blockHash" to resultSet.getString("block_hash")
            )
        )
    }

    connection.close()
    return transactions
}
