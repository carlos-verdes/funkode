package io.funkode.arangodb

import zio.*

import models.*
import protocol.*

trait ArangoApi[Encoder[_], Decoder[_]]:

  def current: DatabaseName

  def changeTo(newDatabaseName: DatabaseName): ArangoApi[Encoder, Decoder]

  def db: ArangoDatabase[Encoder, Decoder]

  def collection(name: CollectionName): ArangoCollection[Encoder, Decoder] = db.collection(name)

  def graph(name: GraphName): ArangoGraph[Encoder, Decoder] = db.graph(name)

  def server: ArangoServer[Decoder]

object ArangoApi:

  private def withApi[Enc[_]: TagK, Dec[_]: TagK, O](f: ArangoApi[Enc, Dec] => O) =
    ZIO.service[ArangoApi[Enc, Dec]].map(f)

  def current[Enc[_]: TagK, Dec[_]: TagK]: WithApi[Enc, Dec, DatabaseName] =
    withApi(_.current)

  def changeTo[Enc[_]: TagK, Dec[_]: TagK](
      newDatabaseName: DatabaseName
  ): WithApi[Enc, Dec, ArangoApi[Enc, Dec]] =
    withApi(_.changeTo(newDatabaseName))

  def db[Enc[_]: TagK, Dec[_]: TagK]: WithApi[Enc, Dec, ArangoDatabase[Enc, Dec]] =
    withApi(_.db)

  def collection[Enc[_]: TagK, Dec[_]: TagK](
      name: CollectionName
  ): WithApi[Enc, Dec, ArangoCollection[Enc, Dec]] =
    withApi(_.collection(name))

  def server[Enc[_]: TagK, Dec[_]: TagK]: WithApi[Enc, Dec, ArangoServer[Dec]] =
    withApi(_.server)

  class Impl[Encoder[_], Decoder[_]](databaseName: DatabaseName)(using
      arangoClient: ArangoClient[Encoder, Decoder]
  ) extends ArangoApi[Encoder, Decoder]:

    val current: DatabaseName = databaseName

    def changeTo(newDatabaseName: DatabaseName): ArangoApi[Encoder, Decoder] =
      new Impl(newDatabaseName)

    val db: ArangoDatabase[Encoder, Decoder] = new ArangoDatabase.Impl[Encoder, Decoder](databaseName)

    val server: ArangoServer[Decoder] = new ArangoServer.Impl[Encoder, Decoder](using arangoClient)

  extension [R, Enc[_], Dec[_]](api: ZIO[R, ArangoError, ArangoApi[Enc, Dec]])
    def db: WithResource[R, ArangoDatabase[Enc, Dec]] = api.map(_.db)

    def collection(name: CollectionName): WithResource[R, ArangoCollection[Enc, Dec]] =
      api.map(_.collection(name))

    def graph(name: GraphName): WithResource[R, ArangoGraph[Enc, Dec]] = api.map(_.graph(name))
