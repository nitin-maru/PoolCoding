package org.example.org.example

import kotlinx.coroutines.*
import org.slf4j.LoggerFactory

object BlockSyncer {
    private val logger = LoggerFactory.getLogger(BlockSyncer::class.java)

    // Start syncing blocks in the background
    fun startBlockSyncScheduler() {
        GlobalScope.launch {
            while (true) {
                try {
                    syncNewBlocks()
                    delay(60000)  // Sync every 60 seconds
                } catch (e: Exception) {
                    logger.error("Error during block sync", e)
                }
            }
        }
    }

    // Sync new blocks
    suspend fun syncNewBlocks() {
        try {
            val latestIndexedBlock = DatabaseHelper.getLatestIndexedBlockHeight()
            val currentBlockHeight = BitcoinRpcClient.getCurrentBlockHeight()

            for (blockHeight in (latestIndexedBlock + 1)..currentBlockHeight) {
                val blockHash = BitcoinRpcClient.getBlockHash(blockHeight)
                val block = BitcoinRpcClient.getBlockByHash(blockHash)
                val opReturnDataList = BitcoinRpcClient.extractOpReturnData(block)

                DatabaseHelper.storeOpReturnDataInDb(opReturnDataList)
                DatabaseHelper.updateLatestIndexedBlockHeight(blockHeight)
            }
            logger.info("Successfully synced up to block height $currentBlockHeight")
        } catch (e: Exception) {
            logger.error("Error syncing new blocks", e)
        }
    }
}