/*
 * TODO: License goes here!
 */
package io.funkode.rest

import cats.syntax.option._
import io.funkode.rest.resource.HttpResource
import org.http4s.Uri
import org.http4s.headers.LinkValue
import org.http4s.implicits.http4sLiteralsSyntax
import org.specs2.Specification
import org.specs2.matcher.{MatchResult, RestMatchers}
import org.specs2.specification.core.SpecStructure

trait LinksAndUris {

  import resource._

  val sampleUri: Uri = uri"/some" / "uri"
  val someRel: String = "someRel"
  val otherRel: String = "otherRel"
  val someLinkValue = LinkValue(sampleUri, someRel.some)
  val linkWithOtherRel = LinkValue(sampleUri, otherRel.some)
  val someResource = HttpResource(sampleUri, "some random resource")
}

class SyntaxSpec
    extends Specification
    with LinksAndUris {
  def is: SpecStructure =
    s2"""
      Funkode Syntax should: <br/>
      Create LinkValue from uri                 $linkFromUri
      update link rel                           $updateLinkRel
      generate self link for HttpResource       $httpResourceSelfLink
      """

  import syntax.all._

  def linkFromUri: MatchResult[LinkValue] =
    (sampleUri.link(someRel) must_=== someLinkValue) and (sampleUri.link(someRel.some) must_=== someLinkValue)

  def updateLinkRel: MatchResult[LinkValue] = someLinkValue.withRel(otherRel) must_=== linkWithOtherRel

  def httpResourceSelfLink: MatchResult[LinkValue] = someResource.selfLink must_=== LinkValue(sampleUri, "self".some)
}
