/*
 * TODO: License goes here!
 */
package io.funkode.rest

import org.http4s.Uri
import org.http4s.implicits.http4sLiteralsSyntax
import org.specs2.Specification
import org.specs2.matcher.MatchResult
import org.specs2.specification.core.SpecStructure


trait LinksAndUris {

  import resource._

  val sampleUri: Uri = uri"/some" / "uri"
  val someRel: String = "someRel"
  val otherRel: String = "otherRel"
  val someLinkValue = ResourceLink(sampleUri, someRel)
  val linkWithOtherRel = ResourceLink(sampleUri, otherRel)
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

  import resource._
  import io.funkode.rest.syntax.resource._

  def linkFromUri: MatchResult[ResourceLink] =
    (sampleUri.link(someRel) must_=== someLinkValue) and (sampleUri.link(someRel) must_=== someLinkValue)

  def updateLinkRel: MatchResult[ResourceLink] = someLinkValue.withRel(otherRel) must_=== linkWithOtherRel

  def httpResourceSelfLink: MatchResult[ResourceLink] = someResource.selfLink must_=== ResourceLink(sampleUri, "self")
}
