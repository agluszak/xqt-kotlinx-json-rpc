// Copyright (C) 2023 Reece H. Dunn. SPDX-License-Identifier: Apache-2.0
package xqt.kotlinx.rpc.json.io

/**
 * A binary data channel to write data to.
 */
expect interface BinaryOutputChannel {
    /**
     * Write a single byte to the channel.
     */
    fun writeByte(byte: Byte)

    /**
     * Write a sequence of bytes to the channel.
     */
    fun writeBytes(bytes: ByteArray)

    /**
     * Flush any pending data on the channel.
     */
    fun flush()

    /**
     * Close the output channel.
     */
    fun close()
}
