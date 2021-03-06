package de.lightwave.shockwave.io.protocol

import java.io.ByteArrayOutputStream

import akka.util.ByteString
import de.lightwave.io.tcp.protocol.MessageWriter

/**
  * Helper class for writing outgoing Shockwave messages that is mainly
  * used by message composers
  */
class ShockwaveMessageWriter(opCode: Short) extends MessageWriter(opCode) {
  private val messageStream = new ByteArrayOutputStream

  // Set operation code
  push(opCode)

  /**
    * Write simple strings such as nicknames
    * Format: STR+CHR(2)
    */
  def push(s: String): MessageWriter = {
    messageStream.write(ShockwaveMessageWriter.filterString(s).getBytes())
    push(2.toByte)
    this
  }

  def push(bs: ByteString): MessageWriter = {
    messageStream.write(bs.toArray)
    this
  }

  /**
    * Write byte
    */
  def push(b: Byte): MessageWriter = {
    messageStream.write(b)
    this
  }

  /**
    * Write large numbers such as player ids
    * Format: VL64(NUMBER)
    */
  def push(i: Int): MessageWriter = {
    messageStream.write(NumberEncoding.encodeInt(i).toArray)
    this
  }

  /**
    * Write boolean value
    * Format: VL64(BOOLEAN ? 1 : 0)
    */
  def push(b: Boolean): MessageWriter = {
    messageStream.write(if (b) 'I'.toByte else 'H'.toByte)
    this
  }

  /**
    * Write small numbers such as operation codes
    * Format: B64(NUMBER)
    */
  def push(s: Short): MessageWriter = {
    messageStream.write(NumberEncoding.encodeShort(if (s < 0) 0 else s).toArray)
    this
  }

  /**
    * @return Byte string of message including an end-byte (+ CHR(1))
    */
  def toByteString: ByteString = {
    val finalOutput = new ByteArrayOutputStream
    messageStream.writeTo(finalOutput)

    // Indicates end of message
    finalOutput.write(1)

    val result = ByteString.fromArray(finalOutput.toByteArray)
    finalOutput.close()

    result
  }
}

/**
  * Utility functions regarding message writing
  */
object ShockwaveMessageWriter {
  /**
    * Remove characters from string that could disturb
    * the client message reader
    */
  def filterString(str: String): String = str.filter(_.toInt >= 32)
}