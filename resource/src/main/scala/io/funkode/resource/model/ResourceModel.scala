package io.funkode.resource.model

import scala.quoted.*

import zio.*
import zio.schema.{DeriveSchema, Schema, TypeId}
import zio.schema.meta.MetaSchema

import io.funkode.resource.model

case class ResourceModel(name: String, collections: Map[String, CollectionModel] = Map.empty)
case class CollectionModel(resourceType: String, rels: Map[String, RelModel] = Map.empty)
case class RelModel(targetType: String, arity: RelArity)

enum RelArity:
  case OneToOne
  case OneToMany

object ResourceModelDerivation:

  inline def decapitalize(inline str: String): String = str match
    case null | "" => str
    case nonEmpty  => s"${nonEmpty.head.toLower}${nonEmpty.tail}"

  inline def gen[R]: ResourceModel =
    ${ graphModelForTypeGen[R] }

  private def graphModelForTypeGen[R](using t: Type[R])(using Quotes): Expr[ResourceModel] =
    import quotes.reflect.*

    val typeRepr = TypeRepr.of[R]
    println(s"typeOf $typeRepr")

    val exp = '{

      val schema: Schema[R] = zio.schema.DeriveSchema.gen[R]
      val metaSchema: MetaSchema = schema.ast

      metaSchema match
        case MetaSchema.Product(id, _, fields, _) =>
          val graphName = decapitalize(id.name)
          val collections = fields.map((name, colSchema) => name -> collectionFromSchema(colSchema))

          ResourceModel(graphName, collections.toMap)
        case other =>
          throw RuntimeException("Graph model derivation not supported for current type: " + other)

    }
    println("exp looks like: \n" + exp.show)
    exp

  extension (typeId: TypeId)
    def fullName: String = typeId match
      case TypeId.Structural => TypeId.Structural.name
      case n: TypeId.Nominal => n.fullyQualified

  private def collectionFromSchema(colSchema: MetaSchema): CollectionModel =
    colSchema match
      case colMetaSchema: MetaSchema.Product =>
        val colType = colSchema.toSchema.schemaType

        val rels = colMetaSchema.fields
          .filter((_, schema) => schema.isList || schema.isProduct)
          .map((name, schema) =>
            schema match
              case _: MetaSchema.Product =>
                name -> RelModel(schema.toSchema.schemaType, RelArity.OneToOne)
              case MetaSchema.ListNode(item, _, _) =>
                name -> RelModel(item.toSchema.schemaType, RelArity.OneToMany)
              case other =>
                throw RuntimeException(
                  "Rel type model derivation not supported for current type: " + other
                )
          )

        CollectionModel(colType, rels.toMap)

      case other =>
        throw RuntimeException(
          "Collection model derivation not supported for current type: " + other
        )

  extension (metaSchema: MetaSchema)
    def isProduct: Boolean = metaSchema match
      case _: MetaSchema.Product => true
      case _                     => false
    def isList: Boolean = metaSchema match
      case _: MetaSchema.ListNode => true
      case _                      => false

  extension (schema: Schema[?])
    def schemaType: String = schema match
      case e: Schema.Enum[?]   => e.id.fullName
      case r: Schema.Record[?] => r.id.fullName
      case c: Schema.Collection[?, ?] =>
        c match
          case Schema.Sequence(elementSchema, _, _, _, _) =>
            s"Sequence(${elementSchema.schemaType})"
          case Schema.Map(keySchema, valueSchema, _) =>
            s"Map(${keySchema.schemaType}, ${valueSchema.schemaType}"
          case Schema.Set(elementSchema, _) =>
            s"Set(${elementSchema.schemaType})"

      case _: Schema.Transform[?, ?, ?] => "Transform"
      case p: Schema.Primitive[?]       => p.standardType.tag
      case o: Schema.Optional[?]        => s"Option(${o.schema})"
      case _: Schema.Fail[?]            => "Fail"
      case t: Schema.Tuple2[?, ?]       => s"Tuple2(${t.left.schemaType}, ${t.right.schemaType})"
      case e: Schema.Either[?, ?]       => s"Either(${e.left.schemaType}, ${e.right.schemaType})"
      case l: Schema.Lazy[?]            => s"Lazy${l.schema.schemaType}"
      case _: Schema.Dynamic            => "Dynamic"
