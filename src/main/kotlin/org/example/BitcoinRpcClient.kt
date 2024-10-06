package org.example.org.example

import org.json.JSONObject
import org.slf4j.LoggerFactory
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.util.*

object BitcoinRpcClient {

    private val logger = LoggerFactory.getLogger(BitcoinRpcClient::class.java)

    private fun rpcRequest(method: String, params: List<Any>): String {
        val rpcUrl = "http://localhost:38332/"
        val rpcUser = System.getenv("RPC_USER") ?: "nitin"
        val rpcPassword = System.getenv("RPC_PASSWORD") ?: "nitin"

        return try {
            val url = URL(rpcUrl)
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.doOutput = true

            // Debug: Print the method and params being sent
            // println("Sending RPC request - Method: $method, Params: $params")

            val auth = Base64.getEncoder().encodeToString("$rpcUser:$rpcPassword".toByteArray())
            // println("Authorization header: Basic $auth")
            conn.setRequestProperty("Authorization", "Basic $auth")

            val jsonRequest = JSONObject()
            jsonRequest.put("jsonrpc", "1.0")
            jsonRequest.put("id", "kotlinrpc")
            jsonRequest.put("method", method)
            jsonRequest.put("params", params)

            // Log the request for debugging
            // println("Sending RPC Request: $jsonRequest")

            val writer = OutputStreamWriter(conn.outputStream)
            writer.write(jsonRequest.toString())
            writer.flush()

            // Debug: Print the raw response from Bitcoin Core
            val responseCode = conn.responseCode
            if (responseCode != 200) {
                val errorResponse = conn.errorStream.bufferedReader().use { it.readText() }
                throw RuntimeException("RPC request failed with HTTP code: $responseCode, Error Response: $errorResponse")
            }
            val response = conn.inputStream.bufferedReader().use { it.readText() }
            //    println("Raw RPC Response: $response")
            response
        } catch (e: Exception) {
            logger.error("Error making RPC request", e)
            throw e
        }
    }

    fun getBlockHash(blockHeight: Int): String {
        val response = rpcRequest("getblockhash", listOf(blockHeight))
        return parseBlockHash(response)
    }

    fun getBlockByHash(blockHash: String): JSONObject {
        val response = rpcRequest("getblock", listOf(blockHash, 2))
        return JSONObject(response)
    }

    private fun parseBlockHash(response: String): String {
        val jsonObject = JSONObject(response)
        return jsonObject.getString("result")
    }

    fun getCurrentBlockHeight(): Int {
        val response = rpcRequest("getblockcount", emptyList())
        val jsonObject = JSONObject(response)
        return jsonObject.getInt("result")
    }

    fun extractOpReturnData(blockResponse: JSONObject): List<Map<String, String>> {
        val opReturnDataList = mutableListOf<Map<String, String>>()
        // Extract the "result" field from the block response
        if (!blockResponse.has("result")) {
            println("No 'result' field found in the block response")
            return opReturnDataList  // Return empty if no "result" field
        }

        val block = blockResponse.getJSONObject("result")
        // Check if the "tx" field exists and is a non-empty array
        if (!block.has("tx")) {
            println("Block does not contain 'tx' field")
            return opReturnDataList  // Return empty if "tx" field is missing
        }

        val transactions = block.getJSONArray("tx")
        if (transactions.length() == 0) {
            println("Block contains 'tx' field but it is empty")
            return opReturnDataList  // Return empty if "tx" array is empty
        }
        println("Transactions found: ${transactions.length()}")

        // Loop through each transaction
        for (i in 0 until transactions.length()) {
            val tx = transactions.getJSONObject(i)
            val vout = tx.getJSONArray("vout")

            // Loop through each output (vout)
            for (j in 0 until vout.length()) {
                val output = vout.getJSONObject(j)
                val scriptPubKey = output.getJSONObject("scriptPubKey")

                // Check if it's OP_RETURN (type is nulldata)
                if (scriptPubKey.getString("type") == "nulldata") {
                    val asm = scriptPubKey.getString("asm")
                    val opReturnData = asm.split(" ")[1]  // Extract OP_RETURN data in hex

                    // Store the transaction hash, block hash, and OP_RETURN data
                    opReturnDataList.add(
                        mapOf(
                            "txid" to tx.getString("txid"),
                            "blockhash" to block.getString("hash"),
                            "opReturnData" to opReturnData
                        )
                    )
                    println("OP_RETURN found in transaction ${tx.getString("txid")}: $opReturnData")
                }
            }
        }
        return opReturnDataList
    }
}