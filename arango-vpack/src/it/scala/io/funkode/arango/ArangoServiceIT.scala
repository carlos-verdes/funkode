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
  import rest.syntax.all._

  case class Mock(id: String, user: String, age: Int)
  case class Person(name: String)

  implicit val mockEncoder: VPackEncoder[Mock] = VPackEncoder.gen
  implicit val mockDecoder: VPackDecoder[Mock] = VPackDecoder.gen
  implicit val personEncoder: VPackEncoder[Person] = VPackEncoder.gen
  implicit val personDecoder: VPackDecoder[Person] = VPackDecoder.gen

  val mocksUri = uri"/mocks"
  val mocksBatch1Col = "mocksBatch1"
  val mocksBatch2Col = "mocksBatch2"
  val mocksBatch1Uri = uri"/" / mocksBatch1Col
  val mocksBatch2Uri = uri"/" / mocksBatch2Col

  val mock1 = HttpResource(mocksUri / "aaa", Mock("aaa123", "Pizza", 21))
  val mock2 = HttpResource(mocksUri / "bbb", Mock("bbb", "Pasta", 10))
  val mock3 = HttpResource(mocksUri / "ccc", Mock("ccc", "Gazpacho", 10))
  val mock4 = HttpResource(mocksUri / "ddd", Mock("ddd", "Salad", 10))
  val updated3Mock = Mock("ccc", "Tortilla", 36)
  val person1 = HttpResource(uri"/person/roger" , Person("Roger"))
  val person2 = HttpResource(uri"/person/that", Person("That"))

  val likes = "likes"
  val knows = "knows"
  val person1WithLinks = person1.withLinks(Vector(person2.selfLink.withRel(knows), mock4.selfLink.withRel(likes)))

  val mock1Batch1 = HttpResource(mocksBatch1Uri / "batch1.1", Mock("batch1.1", "Pizza", 21))
  val mock2Batch1 = HttpResource(mocksBatch1Uri / "batch1.2", Mock("batch1.2", "Pasta", 10))
  val mock3Batch1 = HttpResource(mocksBatch1Uri / "batch1.3", Mock("batch1.3", "Gazpacho", 10))
  val mock4Batch1 = HttpResource(mocksBatch1Uri / "batch1.4", Mock("batch1.4", "Salad", 10))
  val mock1Batch2 = HttpResource(mocksBatch2Uri / "batch2.1", Mock("batch2.1", "Pizza", 21))
  val mock2Batch2 = HttpResource(mocksBatch2Uri / "batch2.2", Mock("batch2.2", "Pasta", 10))
  val mock3Batch2 = HttpResource(mocksBatch2Uri / "batch2.3", Mock("batch2.3", "Gazpacho", 10))
  val mock4Batch2 = HttpResource(mocksBatch2Uri / "batch2.4", Mock("batch2.4", "Salad", 10))

  val mocksBatch1Res = Vector(mock1Batch1, mock2Batch1, mock3Batch1, mock4Batch1)
  val mocksBatch2Res = Vector(mock1Batch2, mock2Batch2, mock3Batch2, mock4Batch2)
  val mocksBatch1 = mocksBatch1Res.map(_.body)
  val mocksBatch2 = mocksBatch2Res.map(_.body)

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
      _ <- linkResources(left, right, linkRel, Map(("random", "attribute")))
    } yield ()
  }

  def storeResources[R: VPackEncoder : VPackDecoder](
      resources: Vector[HttpResource[R]])(
      implicit dsl: VPackStoreDsl[IO]): IO[Unit] =
    resources.traverse(r => dsl.store[R](r)).map(_ => ())


  def queryCol[R: VPackDecoder](
      collection: String,
      batchSize: Option[Long] = Some(2))(
      implicit dsl: VPackStoreDsl[IO]): IO[QueryResult[R]] =
    dsl.query[R](s"FOR m in $collection RETURN m", batchSize)

  def queryColStream[R: VPackDecoder](
      collection: String)(
      implicit dsl: VPackStoreDsl[IO]): fs2.Stream[IO, R] =
    dsl.queryStream[R](s"FOR m in $collection RETURN m")

  def fetchFromArango[R : VPackDecoder](
      uri: Uri)(
      implicit dsl: VPackStoreDsl[IO]): IO[R] =
    dsl.fetch[R](uri).map(_.body)
}

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
      Store and query stream                                  $storeAndQueryStreamTest
      //Store a resource using HTTP API                         storeResource

  """

  import rest.error._
  import storeDsl._


  def arangoIsReady: MatchResult[Future[Boolean]] = isContainerReady(arangoContainer) must beTrue.await

  def storeNewResource: MatchResult[Any] =
    storeDsl.store[Mock](mocksUri, mock1.body).map(_.body) must returnValue(mock1.body)

  def testStoreAndFetch: MatchResult[Any] = storeAndFetch[Mock](mock2) must returnValue(mock2.body)

  def storeAndUpdate: MatchResult[Any] = storeAndUpdate[Mock](mock3, updated3Mock) must returnValue(updated3Mock)

  def storeAndLinkResources: MatchResult[Any] =
    (store(person1) *>
        store(person2) *>
        store(mock4) *>
        linkResources(person1, person2, knows) *>
        linkResources(person1, mock4, likes) *>
        fetch[Person](person1.uri) must returnValue(person1WithLinks)) and (
        getRelated[Person, Person](person1, knows).compile.toVector must returnValue(Vector(person2.body))) and (
        getRelated[Person, Mock](person1, likes).compile.toVector must returnValue(Vector(mock4.body)))

  def returnNotFound: MatchResult[Any] =
    fetchFromArango[Mock](uri"/emptyCollection/123") must returnError[Mock, NotFoundError]

  def storeAndQueryTest: MatchResult[Any] =
    (storeResources[Mock](mocksBatch1Res) must returnOk[Unit]) and
        (queryCol[Mock](mocksBatch1Col) must returnValue { (queryResult: QueryResult[Mock]) =>

          (queryResult.results must haveSize(2)) and
              (queryResult.results must containAnyOf(mocksBatch1)) and
              (queryResult.next mustNotEqual(None)) and {

            storeDsl.next[Mock](queryResult.next.get) must returnValue {
              (secondQueryResult: QueryResult[Mock]) =>

                (secondQueryResult.results must haveSize(2)) and
                    (secondQueryResult.results must containAnyOf(mocksBatch1)) and
                    (secondQueryResult.next mustEqual(None))
            }
          }
        })

  def storeAndQueryStreamTest: MatchResult[Any] =
    (storeResources[Mock](mocksBatch2Res) must returnOk[Unit]) and
        (queryColStream[Mock](mocksBatch2Col).compile.toVector must returnValue(mocksBatch2))
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
