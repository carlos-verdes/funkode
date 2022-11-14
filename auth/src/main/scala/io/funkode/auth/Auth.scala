package io.funkode.auth

import java.math.BigInteger

import org.web3j.crypto.{ECDSASignature, Hash, Keys}
import org.web3j.crypto.Keys.toChecksumAddress
import org.web3j.crypto.Sign.{recoverFromSignature, SignatureData}
import org.web3j.utils.Numeric.hexStringToByteArray
import pdi.jwt.*
import zio.*

type AuthIO[O] = IO[AuthError, O]
type WithAuth[O] = ZIO[Auth, AuthError, O]

case class Claim(subject: Option[Subject])

object Claim:

  given claimToJwt: Conversion[Claim, JwtClaim] = claim => JwtClaim(subject = claim.subject.map(_.unwrap))
  given jwtToClaim: Conversion[JwtClaim, Claim] = jwtClaim => Claim(jwtClaim.subject.map(Subject.apply))

opaque type Message = String
object Message:
  def apply(value: String): Message = value
  extension (x: Message) def unwrap: String = x

opaque type Nonce = String
object Nonce:
  def apply(value: String): Nonce = value
  extension (x: Nonce) def unwrap: String = x

opaque type Signature = String
object Signature:
  def apply(value: String): Signature = value
  extension (x: Signature) def unwrap: String = x

opaque type Subject = String
object Subject:
  def apply(value: String): Subject = value
  extension (x: Subject) def unwrap: String = x

opaque type Token = String
object Token:
  def apply(value: String): Token = value
  extension (x: Token) def unwrap: String = x

trait Auth:

  def createNonce(subject: Subject): AuthIO[Nonce]

  def validateSubject(subject: Subject): AuthIO[Subject]

  def validateMessage(msg: Message, signature: Signature, subject: Subject): AuthIO[Unit]

  def createToken(claim: Claim): AuthIO[Token]

  def validateToken(token: Token): AuthIO[Claim]

object Auth:

  import AuthError.*
  import Claim.given

  private def withAuth[O](f: Auth => AuthIO[O]): WithAuth[O] = ZIO.service[Auth].flatMap(f)

  def createNonce(subject: Subject): WithAuth[Nonce] = withAuth(_.createNonce(subject))

  def validateSubject(subject: Subject): WithAuth[Subject] = withAuth(_.validateSubject(subject))

  def validateMessage(msg: Message, signature: Signature, subject: Subject): WithAuth[Unit] =
    withAuth(_.validateMessage(msg, signature, subject))

  def createToken(claim: Claim): WithAuth[Token] =
    withAuth(_.createToken(claim))

  def validateToken(token: Token): WithAuth[Claim] =
    withAuth(_.validateToken(token))

  val InvalidAddress = "Invalid address"
  val EthAddressRegex = "^0x[0-9a-f]{40}$".r
  val PersonalMessagePrefix = "\u0019Ethereum Signed Message:\n"
  val SignatureError = Forbidden("Wrong signature")
  val MissingSubjectError = Forbidden("Invalid jwt token, subject is missing")

  val Index0 = 0
  val Number27 = 27
  val Index32 = 32
  val Index64 = 64

  private def wrongEthAddressError(subject: Subject) = WrongRequest(subject, InvalidAddress)

  private def canonical(address: Subject): Subject = Subject(toChecksumAddress(address.unwrap))

  class EvmJwtAuth(jwtConfig: JwtConfiguration) extends Auth:
    def createNonce(subject: Subject): AuthIO[Nonce] =
      Random.nextUUID.map(_.toString).map(Nonce.apply)

    def validateSubject(subject: Subject): AuthIO[Subject] =
      if EthAddressRegex.matches(subject.unwrap.toLowerCase)
      then ZIO.succeed(canonical(subject))
      else ZIO.fail(wrongEthAddressError(subject))

    def validateMessage(msg: Message, signature: Signature, subject: Subject): AuthIO[Unit] =
      val prefixedMessage = PersonalMessagePrefix + msg.unwrap.length + msg.unwrap
      val messageHash = Hash.sha3(prefixedMessage.getBytes)
      val canonicalAddress = canonical(subject)

      val signatureBytes = hexStringToByteArray(signature.unwrap)
      val aux = signatureBytes(Index64)

      val v: Byte = if aux < Number27.toByte then (aux + Number27.toByte).toByte else aux
      val r = java.util.Arrays.copyOfRange(signatureBytes, Index0, Index32)
      val s = java.util.Arrays.copyOfRange(signatureBytes, Index32, Index64)

      val sd = new SignatureData(v, r, s)
      val ecdaSignature = new ECDSASignature(new BigInteger(1, sd.getR), new BigInteger(1, sd.getS))

      var found = false
      var i = 0
      while !found && i < 4 do
        val candidate = recoverFromSignature(i, ecdaSignature, messageHash)

        if candidate != null && canonical(Subject("0x" + Keys.getAddress(candidate))) == canonicalAddress
        then found = true

        i = i + 1

      if found then ZIO.succeed(()) else ZIO.fail(SignatureError)

    def createToken(claim: Claim): AuthIO[Token] =
      ZIO.succeed(Token(Jwt.encode(claim, jwtConfig.signingKey, JwtAlgorithm.HS256)))

    def validateToken(token: Token): AuthIO[Claim] =
      for
        claim <- ZIO
          .fromTry[Claim](
            JwtZIOJson
              .decode(token.unwrap, jwtConfig.signingKey, Seq(JwtAlgorithm.HS256))
              .map(Claim.jwtToClaim.apply)
          )
          .catchAll { case e: Throwable =>
            ZIO.fail(Forbidden(s"Error validating jwt token", Some(e)))
          }
        _ <- ZIO.getOrFailWith(MissingSubjectError)(claim.subject)
      yield claim

  val evmJwtAuth: ZLayer[JwtConfiguration, AuthError, Auth] =
    ZLayer(
      for config <- ZIO.service[JwtConfiguration]
      yield new EvmJwtAuth(config)
    )
