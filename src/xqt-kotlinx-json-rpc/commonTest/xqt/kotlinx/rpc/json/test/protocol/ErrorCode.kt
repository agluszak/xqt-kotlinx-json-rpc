// Copyright (C) 2023 Reece H. Dunn. SPDX-License-Identifier: Apache-2.0
package xqt.kotlinx.rpc.json.test.protocol

import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive
import xqt.kotlinx.rpc.json.protocol.ErrorCode
import xqt.kotlinx.rpc.json.serialization.jsonArrayOf
import xqt.kotlinx.rpc.json.serialization.jsonObjectOf
import xqt.kotlinx.test.DisplayName
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails

@DisplayName("The ErrorCode type")
class TheErrorCodeType {
    @Test
    @DisplayName("can serialize to JSON")
    fun can_serialize_to_json() {
        assertEquals("-32700", ErrorCode.serializeToJson(ErrorCode.ParseError).toString())

        assertEquals("-32603", ErrorCode.serializeToJson(ErrorCode.InternalError).toString())
        assertEquals("-32602", ErrorCode.serializeToJson(ErrorCode.InvalidParams).toString())
        assertEquals("-32601", ErrorCode.serializeToJson(ErrorCode.MethodNotFound).toString())
        assertEquals("-32600", ErrorCode.serializeToJson(ErrorCode.InvalidRequest).toString())

        assertEquals("-32099", ErrorCode.serializeToJson(ErrorCode.ServerErrorRangeStart).toString())
        assertEquals("-32000", ErrorCode.serializeToJson(ErrorCode.ServerErrorRangeEnd).toString())
    }

    @Test
    @DisplayName("can deserialize from JSON")
    fun can_deserialize_from_json() {
        assertEquals(ErrorCode.ParseError, ErrorCode.deserialize(JsonPrimitive(-32700)))

        assertEquals(ErrorCode.InternalError, ErrorCode.deserialize(JsonPrimitive(-32603)))
        assertEquals(ErrorCode.InvalidParams, ErrorCode.deserialize(JsonPrimitive(-32602)))
        assertEquals(ErrorCode.MethodNotFound, ErrorCode.deserialize(JsonPrimitive(-32601)))
        assertEquals(ErrorCode.InvalidRequest, ErrorCode.deserialize(JsonPrimitive(-32600)))

        assertEquals(ErrorCode.ServerErrorRangeStart, ErrorCode.deserialize(JsonPrimitive(-32099)))
        assertEquals(ErrorCode.ServerErrorRangeEnd, ErrorCode.deserialize(JsonPrimitive(-32000)))
    }

    @Test
    @DisplayName("throws an error if the kind type is not supported")
    fun throws_an_error_if_the_kind_type_is_not_supported() {
        val e1 = assertFails { ErrorCode.deserialize(jsonObjectOf()) }
        assertEquals(IllegalArgumentException::class, e1::class)
        assertEquals("Unsupported kind type 'object'", e1.message)

        val e2 = assertFails { ErrorCode.deserialize(jsonArrayOf()) }
        assertEquals(IllegalArgumentException::class, e2::class)
        assertEquals("Unsupported kind type 'array'", e2.message)

        val e3 = assertFails { ErrorCode.deserialize(JsonNull) }
        assertEquals(IllegalArgumentException::class, e3::class)
        assertEquals("Unsupported kind type 'null'", e3.message)

        val e4 = assertFails { ErrorCode.deserialize(JsonPrimitive("test")) }
        assertEquals(IllegalArgumentException::class, e4::class)
        assertEquals("Unsupported kind type 'string'", e4.message)

        val e5 = assertFails { ErrorCode.deserialize(JsonPrimitive(true)) }
        assertEquals(IllegalArgumentException::class, e5::class)
        assertEquals("Unsupported kind type 'boolean'", e5.message)

        val e6 = assertFails { ErrorCode.deserialize(JsonPrimitive(1.2)) }
        assertEquals(IllegalArgumentException::class, e6::class)
        assertEquals("Unsupported kind type 'decimal'", e6.message)
    }
}
