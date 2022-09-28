package io.funkode.velocystream

import scala.concurrent.duration.*

trait VStreamConfiguration:
  def host: String
  def port: Int
  def connectTimeout: Duration
  def chunkLength: Long
  def readBufferSize: Int
  def replyTimeout: Duration

object VStreamConfiguration:
  val CHUNK_LENGTH_DEFAULT: Long = 30000L
  val READ_BUFFER_SIZE_DEFAULT: Int = 256 * 1024
  val CONNECT_TIMEOUT_DEFAULT: Duration = 10.seconds
  val REPLY_TIMEOUT_DEFAULT: Duration = 30.seconds
