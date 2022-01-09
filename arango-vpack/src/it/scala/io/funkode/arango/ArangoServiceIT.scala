/*
 * TODO: License goes here!
 */

package io.funkode
package arango

import scala.concurrent.Future

import avokka.arangodb.ArangoConfiguration
import avokka.arangodb.fs2.Arango
import avokka.velocypack.{VPackDecoder, VPackEncoder}
import cats.effect.IO
import cats.implicits.toTraverseOps
import com.whisk.docker.impl.spotify._
import com.whisk.docker.specs2.DockerTestKit
import io.funkode.rest.query.QueryResult
import org.http4s._
import org.http4s.implicits._
import org.specs2.Specification
import org.specs2.matcher.{IOMatchers, MatchResult, RestMatchers}
import org.specs2.specification.core.{Env, SpecStructure}
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger


trait IOMatchersWithLogger extends IOMatchers {

  implicit def unsafeLogger: Logger[IO] = Slf4jLogger.getLogger[IO]
}

trait InterpretersAndDsls extends IOMatchersWithLogger {

  import rest.store._

  val arangoConfig = ArangoConfiguration.load()
  val arangoResource = Arango(arangoConfig)

  implicit val storeDsl: HttpStoreWithQueryDsl[IO, VPackEncoder, VPackDecoder] = new ArangoStore(arangoResource)
  //implicit val securityDsl: SecurityAlgebra[IO] = ioJwtSecurityInterpreter
}

trait MockServiceWithArango extends InterpretersAndDsls {

  import rest.query._
  import rest.resource._

  case class Mock(id: String, user: String, age: Int)
  case class Person(name: String)

  implicit val mockEncoder: VPackEncoder[Mock] = VPackEncoder.gen
  implicit val mockDecoder: VPackDecoder[Mock] = VPackDecoder.gen
  implicit val personEncoder: VPackEncoder[Person] = VPackEncoder.gen
  implicit val personDecoder: VPackDecoder[Person] = VPackDecoder.gen

  val mocksUri = uri"/mocks"
  val mocksBatchCol = "mocksBatch"
  val mocksBatchUri = uri"/" / mocksBatchCol

  val mock1 = HttpResource(mocksUri / "aaa", Mock("aaa123", "Pizza", 21))
  val mock2 = HttpResource(mocksUri / "bbb", Mock("bbb", "Pasta", 10))
  val mock3 = HttpResource(mocksUri / "ccc", Mock("ccc", "Gazpacho", 10))
  val mock4 = HttpResource(mocksUri / "ddd", Mock("ddd", "Salad", 10))
  val updated3Mock = Mock("ccc", "Tortilla", 36)
  val person1 = HttpResource(uri"/person/roget" , Person("Roger"))
  val person2 = HttpResource(uri"/person/that", Person("That"))
  val likes = "likes"


  val mockBatch1 = HttpResource(mocksBatchUri / "batch1", Mock("batch1", "Pizza", 21))
  val mockBatch2 = HttpResource(mocksBatchUri / "batch2", Mock("batch2", "Pasta", 10))
  val mockBatch3 = HttpResource(mocksBatchUri / "batch3", Mock("batch3", "Gazpacho", 10))
  val mockBatch4 = HttpResource(mocksBatchUri / "batch4", Mock("batch4", "Salad", 10))

  val mocksBatchRes = Vector(mockBatch1, mockBatch2, mockBatch3, mockBatch4)
  val mocksBatch = mocksBatchRes.map(_.body)

  def storeAndFetch[R: VPackEncoder : VPackDecoder](
      mockResource: HttpResource[R])(
      implicit dsl: VPackStoreDsl[IO]): IO[R] = {

    import dsl._

    for {
      savedResource <- store[R](mockResource)
      fetchedResource <- fetch[R](savedResource.uri)
    } yield fetchedResource.body
  }

  def storeAndUpdate[R: VPackEncoder : VPackDecoder](
      mockResource: HttpResource[R],
      newMock: R)(
      implicit dsl: VPackStoreDsl[IO]): IO[R] = {

    import dsl._

    for {
      savedResource <- store[R](mockResource)
      updatedResource <- store[R](savedResource.uri, newMock)
    } yield updatedResource.body
  }

  def storeAndLink[L : VPackEncoder : VPackDecoder, R : VPackEncoder : VPackDecoder](
      left: HttpResource[L],
      right: HttpResource[R],
      linkRel: String)(
      implicit dsl: VPackStoreDsl[IO]): IO[Unit] = {

    import dsl._

    for {
      _ <- store[L](left)
      _ <- store[R](right)
      _ <- linkResources(left, right, linkRel)
    } yield ()
  }

  def storeResources[R: VPackEncoder : VPackDecoder](
      resources: Vector[HttpResource[R]])(
      implicit dsl: VPackStoreDsl[IO]): IO[Unit] =
    resources.traverse(r => dsl.store[R](r)).map(_ => ())


  def queryCol[R: VPackDecoder](
      collection: String,
      batchSize: Option[Long] = Some(2))(
      implicit dsl: VPackStoreDsl[IO]): IO[QueryResult[R]] = {

    dsl.query[R](s"FOR m in $collection RETURN m", batchSize)
  }

  def fetchFromArango[R : VPackDecoder](
      uri: Uri)(
      implicit dsl: VPackStoreDsl[IO]): IO[R] =
    dsl.fetch[R](uri).map(_.body)
}

/*
trait AuthCases extends InterpretersAndDsls {

  import error._
  import resource._
  import tagless.interpreters.arangoStore._
  //import tagless.security._
  //import tsec.jwt.JWTClaims


  case class UserRequest(address: String)
  case class User(address: String, nonce: String, username: Option[String])
  case class UserUpdate(username: String)

  implicit val userEncoder: VPackEncoder[User] = VPackEncoder.gen
  implicit val userDecoder: VPackDecoder[User] = VPackDecoder.gen


  val userAddress = "address1"
  val userNonce = "nonce1"
  val username = "rogerthat"
  val userRequest = UserRequest(userAddress)
  val expectedCreatedUser = User(userAddress, userNonce, None)
  val expectedUpdatedUser = User(userAddress, userNonce, username.some)

  val claim = JWTClaims(subject = "address1".some, jwtId = None)
  val jwtToken = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9." +
      "eyJzdWIiOiJhZGRyZXNzMSJ9." +
      "hruJMUPgmxZwCYYqZJXB9l5x_shhGk5nYbvE_ryfECw"

  def userUri(user: User): Uri = userUri(user.address)
  def userUri(userAddress: String): Uri = uri"/users" / userAddress

  def retrieveUser: Kleisli[IO, String, User] = Kleisli(id => storeDsl.fetch[User](userUri(id)).map(_.body))

  def onFailure: AuthedRoutes[Throwable, IO] = Kleisli(req => OptionT.liftF(Forbidden(req.context.toString)))

  def jwtTokenFromAuthHeader(request: Request[IO]): IO[String] =
    request.headers.get[Authorization] match {
      case Some(credentials) => credentials match {
        case Authorization(Credentials.Token(_, jwtToken)) => jwtToken.pure[IO]
        case _ => IO.raiseError(NonAuthorizedError("Invalid Authorization header".some))
      }
      case None => IO.raiseError(NonAuthorizedError("Couldn't find an Authorization header".some))
    }


  def log(text: String): IO[Unit] = println(text).pure[IO]

  def authUser: Kleisli[IO, Request[IO], Either[Throwable, User]] =
    Kleisli({ request =>
      val message = for {
        jwtToken <- httpFreeDsl.getJwtTokenFromHeader(request)
        _ <- log(s"jwt token: $jwtToken")
        claim <- securityDsl.validateToken(Token(jwtToken))
        _ <- log(s"claim: $claim")
        user <- storeDsl.fetch[User](userUri(claim.subject.value))
        _ <- log(s"user: $user")
      } yield user.body

      message.attempt
    })

  def authMiddleware: AuthMiddleware[IO, User] =
    AuthMiddleware[IO, Throwable, User](authUser, onFailure)

  def publicRoutes(
      implicit httpFreeDsl: HttpAlgebra[IO],
      httpStoreDsl: ArangoStoreAlgebra[IO]): HttpRoutes[IO] = {

    import httpFreeDsl._
    import httpStoreDsl._

    HttpRoutes.of[IO] {
      case r @ POST -> Root / "users" =>
        for {
          userRequest <- parseRequest[UserRequest](r)
          userAddress = userRequest.address
          user = User(userAddress, userNonce, None)
          storedUser <- store[User](userUri(user), user)
        } yield storedUser.created[IO]
    }
  }

  def privateRoutes(
      implicit httpFreeDsl: HttpAlgebra[IO],
      httpStoreDsl: ArangoStoreAlgebra[IO]): AuthedRoutes[User, IO] = {

    import httpFreeDsl._
    import httpStoreDsl._

    val wrongAddressError = NonAuthorizedError("wrong address".some)

    AuthedRoutes.of[User, IO] {
      case GET -> Root / "profile" as user => Ok(user)

      case r @ PUT -> Root / "users" / address as user =>
        for {
          UserUpdate(newUsername) <- parseRequest[UserUpdate](r.req)
          _ <-
              if (address == user.address)
                IO.pure[Unit](())
              else {
                IO.raiseError[Unit](wrongAddressError)
              }
          updatedUser <- store[User](r.req.uri, user.copy(username = newUsername.some))
          response <- Ok(updatedUser.body)
        } yield response
    }
  }



  val authMiddlewareInstance = authMiddleware
  val userService = publicRoutes <+> authMiddlewareInstance(privateRoutes)

  val validAuthHeader = Headers(Authorization(Credentials.Token(AuthScheme.Bearer, jwtToken)))
  val createUserRequest: Request[IO] = Request[IO](Method.POST, uri"/users").withEntity(userRequest)
  val getProfileRequest: Request[IO] = Request[IO](Method.GET,uri"/profile").withHeaders(validAuthHeader)
  val updateUserRequest: Request[IO] =
    Request[IO](Method.PUT,uri"/users/address1")
        .withEntity(UserUpdate(username))
        .withHeaders(validAuthHeader)

  // Windows testing hack
  private def tsecWindowsFix(): Unit =
    try {
      SecureRandom.getInstance("NativePRNGNonBlocking")
      ()
    } catch {
      case _: NoSuchAlgorithmException =>
        val secureRandom = new SecureRandom()
        val defaultSecureRandomProvider = secureRandom.getProvider.get(s"SecureRandom.${secureRandom.getAlgorithm}")
        secureRandom.getProvider.put("SecureRandom.NativePRNGNonBlocking", defaultSecureRandomProvider)
        Security.addProvider(secureRandom.getProvider)
        ()
    }

  tsecWindowsFix()
}
 */

class ArangoServiceIT(env: Env)
    extends Specification
        with DockerKitSpotify
        with DockerArango
        with DockerTestKit
        with MockServiceWithArango
        //with AuthCases
        with RestMatchers[IO] {

  implicit val ee = env.executionEnv

  def is: SpecStructure = s2"""
      The ArangoDB container should be ready                  $arangoIsReady
      Store a new resource with specific id                   $storeNewResource
      Store and fetch from ArangoDB                           $testStoreAndFetch
      Store and update from ArangoDB                          $storeAndUpdate
      Store and link two resources                            $storeAndLinkResources
      Return not found error when document doesn't exist      $returnNotFound
      Store and query                                         $storeAndQueryTest
      //Store a resource using HTTP API                         storeResource

  """

  import rest.error._

  def arangoIsReady: MatchResult[Future[Boolean]] = isContainerReady(arangoContainer) must beTrue.await

  def storeNewResource: MatchResult[Any] =
    storeDsl.store[Mock](mocksUri, mock1.body).map(_.body) must returnValue(mock1.body)

  def testStoreAndFetch: MatchResult[Any] = storeAndFetch[Mock](mock2) must returnValue(mock2.body)

  def storeAndUpdate: MatchResult[Any] = storeAndUpdate[Mock](mock3, updated3Mock) must returnValue(updated3Mock)

  def storeAndLinkResources: MatchResult[Any] =
    (storeAndLink[Person, Person](person1, person2, likes)  must returnValue(())) and
        (storeAndLink[Person, Mock](person1, mock4, likes) must returnValue(()))

  def returnNotFound: MatchResult[Any] =
    fetchFromArango[Mock](uri"/emptyCollection/123") must returnError[Mock, NotFoundError]

  def storeAndQueryTest: MatchResult[Any] =
    (storeResources[Mock](mocksBatchRes) must returnOk[Unit]) and
        (queryCol[Mock](mocksBatchCol) must returnValue { (queryResult: QueryResult[Mock]) =>

          (queryResult.results must haveSize(2)) and
              (queryResult.results must containAnyOf(mocksBatch)) and
              (queryResult.next mustNotEqual(None)) and {

            storeDsl.next[Mock](queryResult.next.get) must returnValue {
              (secondQueryResult: QueryResult[Mock]) =>

                (secondQueryResult.results must haveSize(2)) and
                    (secondQueryResult.results must containAnyOf(mocksBatch)) and
                    (secondQueryResult.next mustEqual(None))
            }
          }
        })

  /*
  def storeResource: MatchResult[Any] =
    (userService.orNotFound(createUserRequest) must returnValue { (response: Response[IO]) =>
      response must haveStatus(Created) and
          (response must haveBody(expectedCreatedUser)) and
          (response must containHeader(Location(userUri(expectedCreatedUser))))
    }) and (
      userService.orNotFound(getProfileRequest) must returnValue { response: Response[IO] =>
        response must haveStatus(Ok) and (response must haveBody(expectedCreatedUser))
      }) and (
        userService.orNotFound(updateUserRequest) must returnValue { response: Response[IO] =>
          response must haveStatus(Ok) and (response must haveBody(expectedUpdatedUser))
      })

 */
}
