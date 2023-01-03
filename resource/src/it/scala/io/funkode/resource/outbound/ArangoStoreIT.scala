package io.funkode.resource.outbound

import io.funkode.arangodb.*
import io.funkode.arangodb.http.*
import io.funkode.arangodb.model.*
import io.funkode.arangodb.http.JsonCodecs.given
import io.lemonlabs.uri.{Url, Urn}
import zio.*
import zio.json.*
import zio.http.*
import zio.test.*

import io.funkode.resource.model.*
import io.funkode.resource.model.given
import io.funkode.resource.model.Resource.*
import adapter.ArangoResourceStore

trait TransactionsExamples:

  case class Network(id: String, chainId: String, name: String, currency: String) derives JsonCodec

  case class Transaction(
      networkId: Urn,
      hash: String,
      timestamp: Long
  ) derives JsonCodec

  given Identifiable[Network] with
    extension (r: Network)
      def urn: Urn = Urn.parse("urn:network:" + r.id)
      def withId(urn: Urn): Network = r.copy(id = urn.nss)

  given Identifiable[Transaction] with
    extension (transaction: Transaction)
      def urn: Urn = Urn.parse(s"urn:tx:${transaction.hash}@${transaction.networkId}")
      def withId(urn: Urn): Transaction =
        val Array(newNetworkId, newHash) = urn.nss.split("@")
        transaction.copy(networkId = Urn.parse(newNetworkId), hash = newHash)

  val storeModel =
    ResourceModel(
      "portfolio",
      Map(
        "network" -> CollectionModel(
          "io.funkode.resource.model.Network",
          Map("transactions" -> RelModel("io.funkode.resource.model.Transaction", RelArity.OneToMany))
        ),
        "tx" -> CollectionModel(
          "io.funkode.resource.model.Transaction",
          Map("network" -> RelModel("io.funkode.resource.model.Network", RelArity.OneToOne))
        )
      )
    )

  val hash1 = "0x888333"
  val timestamp1 = 1L

  val ethNetworkUrn = Urn.parse("urn:network:eth")
  val tx1Urn = Urn.parse("urn:tx:" + hash1 + "@" + ethNetworkUrn.toString)

  val ethNetwork = Network("eth", "1", "Ethereum Mainnet", "ETH")

  val tx1 = Transaction(ethNetwork.urn, hash1, timestamp1)

object ArangoStoreIT extends ZIOSpecDefault with TransactionsExamples:

  override def spec: Spec[TestEnvironment, Any] =
    suite("Arango ResourceStore should")(test("Store transaction") {
      for
        _ <- JsonStore.initStore(storeModel)
        storedNetwork <- JsonStore.store(ethNetwork).deserialize[Network]
        fetchedNetwork <- JsonStore.fetch(ethNetworkUrn).deserialize[Network]
        storedTx <- JsonStore.store(tx1).deserialize[Transaction]
        fetchedTx <- JsonStore.fetch(tx1Urn).deserialize[Transaction]
      yield assertTrue(storedTx == tx1) &&
        assertTrue(fetchedTx == tx1) &&
        assertTrue(storedNetwork == ethNetwork) &&
        assertTrue(fetchedNetwork == ethNetwork)
    }).provideShared(
      Scope.default,
      ArangoConfiguration.default,
      Client.default,
      ArangoClientJson.testContainers,
      ArangoResourceStore.live
    )
