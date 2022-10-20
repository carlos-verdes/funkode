/*
 * TODO: License goes here!
 */
package io.funkode.arangodb.models

import io.funkode.velocypack.*

case class Query(
    query: String,
    bindVars: VPack,
    batchSize: Option[Long] = None,
    cache: Option[Boolean] = None,
    count: Option[Boolean] = None,
    memoryLimit: Option[Long] = None,
    options: Option[Query.Options] = None,
    ttl: Option[Long] = None
)

object Query:

  def apply(query: String): Query = Query(query, VObject.empty)

  final case class Options(
      failOnWarning: Option[Boolean],
      fullCount: Option[Boolean],
      intermediateCommitCount: Option[Long],
      intermediateCommitSize: Option[Long],
      maxPlans: Option[Long],
      maxTransactionSize: Option[Long],
      maxWarningCount: Option[Long],
      optimizerRules: Option[List[String]],
      profile: Option[Int],
      satelliteSyncWait: Option[Boolean],
      skipInaccessibleCollections: Option[Boolean],
      stream: Option[Boolean]
  )
