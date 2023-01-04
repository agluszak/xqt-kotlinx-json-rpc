// Copyright (C) 2022-2023 Reece H. Dunn. SPDX-License-Identifier: Apache-2.0
package xqt.kotlinx.rpc.json.serialization

import kotlinx.serialization.json.*
import xqt.kotlinx.rpc.json.serialization.types.JsonString

/**
 * An error during `JsonSerialization.deserialize`.
 */
open class JsonDeserializationException(message: String) :
    RuntimeException(message)

/**
 * A non-empty JSON array is empty.
 */
class EmptyArrayException :
    JsonDeserializationException("The array is empty")

/**
 * A missing key in a JSON object.
 */
class MissingKeyException(key: String) :
    JsonDeserializationException("Missing $key key")

/**
 * The `kind` key type is not supported.
 */
class UnsupportedKindValueException(kind: String) :
    JsonDeserializationException("Unsupported kind property value '$kind'")

/**
 * Throws a non-empty array is empty exception.
 */
fun emptyArray(): Nothing = throw EmptyArrayException()

/**
 * Throws a missing key in a JSON object exception.
 */
fun missingKey(vararg key: String): Nothing {
    val keyNames = key.withIndex().joinToString(", ") { (index, name) ->
        if (index == key.size - 1) "or '$name'" else "'$name'"
    }
    throw MissingKeyException(keyNames)
}

/**
 * Throws a missing key in a JSON object exception.
 */
fun missingKey(key: String): Nothing = throw MissingKeyException(key)

/**
 * A helper function for reporting unsupported JSON types during deserialization.
 */
fun unsupportedKindType(json: JsonElement): Nothing = unsupportedKindType(json.kindType)

/**
 * A helper function for reporting unsupported JSON types during deserialization.
 */
fun unsupportedKindType(kindType: KindType): Nothing {
    throw IllegalArgumentException("Unsupported kind type '${kindType.kindName}'")
}

/**
 * Throws a kind key type is not supported exception.
 */
fun unsupportedKindValue(kind: String): Nothing = throw UnsupportedKindValueException(kind)

/**
 * Throws a kind key type is not supported exception.
 */
fun unsupportedKindValue(json: JsonObject): Nothing {
    unsupportedKindValue(json.get("kind", JsonString))
}

/**
 * A helper function for reporting numeric values outside supported bounds.
 */
fun <T> valueOutOfRange(value: T): Nothing {
    throw IllegalArgumentException("The value '$value' is out of range")
}

/**
 * A helper function for reporting numeric values outside supported bounds.
 */
fun valueOutOfRange(json: JsonPrimitive): Nothing {
    throw IllegalArgumentException("The value '${json.content}' is out of range")
}
