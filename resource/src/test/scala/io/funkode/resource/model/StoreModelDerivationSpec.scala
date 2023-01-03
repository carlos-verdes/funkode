package io.funkode.resource
package model

import scala.compiletime.*

import zio.json.*
import zio.schema.*
import zio.test.*

trait PortfolioSampleModel:

  val expectedModel =
    ResourceModel(
      "portfolio",
      Map(
        "networks" -> CollectionModel(
          "io.funkode.resource.model.Network",
          Map("transactions" -> RelModel("io.funkode.resource.model.Transaction", RelArity.OneToMany))
        ),
        "tx" -> CollectionModel(
          "io.funkode.resource.model.Transaction",
          Map("network" -> RelModel("io.funkode.resource.model.Network", RelArity.OneToOne))
        )
      )
    )

object StoreModelDerivationSpec extends ZIOSpecDefault with PortfolioSampleModel:

  override def spec: Spec[TestEnvironment, Any] =
    suite("Arango ResourceStore should")(test("Create graph from model") {

      // given portfolioSchema: Schema[Portfolio] = DeriveSchema.gen[Portfolio]
      val graphModel: ResourceModel = ResourceModelDerivation.gen[Portfolio]
      // val graphModelEnum: ResourceModel = ResourceModelDerivation.gen[PortfolioEnum]
      assertTrue(graphModel == expectedModel) // && assertTrue(graphModelEnum == expectedModel)
    })
