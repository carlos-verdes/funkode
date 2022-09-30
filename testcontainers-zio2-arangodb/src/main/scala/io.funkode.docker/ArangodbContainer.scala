package io.funkode.docker

import scala.util.Random

import com.dimafeng.testcontainers.GenericContainer
import com.dimafeng.testcontainers.GenericContainer.FileSystemBind
import io.funkode.arangodb.*
import org.testcontainers.containers.BindMode
import org.testcontainers.containers.wait.strategy.HttpWaitStrategy
import zio.*

class ArangodbContainer(
    val password: String,
    val port: Int,
    val version: String,
    underlying: GenericContainer)
    extends GenericContainer(underlying) {

  import ArangodbContainer._

  def configuration: ArangoConfiguration = ArangoConfiguration(
    host = containerIpAddress,
    port = mappedPort(port),
    username = "root",
    password = password,
    database = DatabaseName("docker-initdb.d/dumps/test")
  )

  def endpoint: String = "http://%s:%d".format(containerIpAddress, mappedPort(port))
}

object ArangodbContainer {

  object Defaults {
    val port: Int = 8529
    val version: String = java.lang.System.getProperty("test.arangodb.version", "3.7.15")
    val password: String = Random.nextLong().toHexString
  }

  // In the container definition you need to describe, how your container will be constructed:
  case class Def(
      password: String = Defaults.password,
      port: Int = Defaults.port,
      version: String = Defaults.version)
    extends GenericContainer.Def[ArangodbContainer](
      new ArangodbContainer(
        password,
        port,
        version,
        GenericContainer(
          dockerImage = s"arangodb:$version",
          env = Map("ARANGO_ROOT_PASSWORD" -> password),
          exposedPorts = Seq(port),
          classpathResourceMapping = Seq(
            ("docker-initdb.d/", "/docker-entrypoint-initdb.d/", BindMode.READ_ONLY)
          ).map(FileSystemBind.apply),
          waitStrategy = (new HttpWaitStrategy)
            .forPath("/_db/test/_api/collection/countries")
            .withBasicCredentials("root", password)
        )
      )
    )

  def makeScopedContainer(config: ArangoConfiguration) =
    ZIO.acquireRelease(
      ZIO.attemptBlocking {
        val containerDef = ArangodbContainer.Def(
          port = config.port,
          password = config.password
        )
        println(s"Container def: $containerDef")
        val container: ArangodbContainer = containerDef.start()
        println(s"Container: $container")
        container
      }.orDie
    )(container =>
      ZIO
        .attemptBlocking(container.stop())
        .ignoreLogged
    )

  val life = {
    ZLayer.scopedEnvironment {
      for
        arangoConfig  <- ZIO.service[ArangoConfiguration]
        container <- makeScopedContainer(arangoConfig)
      yield
        ZEnvironment(container)
    }
  }
}