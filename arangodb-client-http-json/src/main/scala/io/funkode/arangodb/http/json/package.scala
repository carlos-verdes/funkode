package io.funkode.arangodb
package http

import zio.json.*

package object json:

  type JRAIO[O] = RAIO[JsonEncoder, JsonDecoder, O]

  object codecs extends ModelCodecs
