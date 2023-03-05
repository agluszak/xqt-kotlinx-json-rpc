// Copyright (C) 2023 Reece H. Dunn. SPDX-License-Identifier: Apache-2.0
package xqt.kotlinx.rpc.json.test.protocol

import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive
import xqt.kotlinx.rpc.json.protocol.*
import xqt.kotlinx.rpc.json.serialization.jsonArrayOf
import xqt.kotlinx.rpc.json.serialization.jsonObjectOf
import xqt.kotlinx.rpc.json.serialization.types.JsonIntOrString
import xqt.kotlinx.test.DisplayName
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@DisplayName("The response DSL")
class TheResponseDSL {
    @Test
    @DisplayName("reports ErrorCode.ParseError on invalid JSON")
    fun reports_parse_error_on_invalid_json() = testJsonRpc {
        client.send("{\"jsonrpc\":\"2.0\",result:\"test\",\"id\":null}")

        var called = 0
        server.jsonRpc {
            ++called
        }

        assertEquals(0, called, "The jsonRpc DSL should not have been called.")

        assertEquals(
            jsonObjectOf(
                "jsonrpc" to JsonPrimitive("2.0"),
                "id" to JsonNull,
                "error" to jsonObjectOf(
                    "code" to JsonPrimitive(ErrorCode.ParseError.code),
                    "message" to JsonPrimitive("Unexpected JSON token at offset 22: Expected quotation mark '\"', but had 't' instead at path: \$")
                )
            ),
            client.receive()
        )
    }

    @Test
    @DisplayName("reports ErrorCode.InvalidRequest on an invalid RequestObject")
    fun reports_invalid_request_on_an_invalid_request_object() = testJsonRpc {
        client.send(
            jsonObjectOf(
                "jsonrpc" to JsonPrimitive("2.0"),
                "id" to JsonPrimitive(true),
                "result" to JsonPrimitive(1)
            )
        )

        var called = 0
        server.jsonRpc {
            ++called
        }

        assertEquals(0, called, "The jsonRpc DSL should not have been called.")

        assertEquals(
            jsonObjectOf(
                "jsonrpc" to JsonPrimitive("2.0"),
                "id" to JsonNull,
                "error" to jsonObjectOf(
                    "code" to JsonPrimitive(ErrorCode.InvalidRequest.code),
                    "message" to JsonPrimitive("Unsupported kind type 'boolean'")
                )
            ),
            client.receive()
        )
    }

    @Test
    @DisplayName("reports ErrorCode.InternalError for generic exceptions")
    fun reports_internal_error_for_generic_exceptions() = testJsonRpc {
        client.send(
            jsonObjectOf(
                "jsonrpc" to JsonPrimitive("2.0"),
                "id" to JsonPrimitive(1),
                "result" to JsonPrimitive("test")
            )
        )

        server.jsonRpc {
            response {
                throw RuntimeException("Lorem ipsum")
            }
        }

        assertEquals(
            jsonObjectOf(
                "jsonrpc" to JsonPrimitive("2.0"),
                "id" to JsonPrimitive(1),
                "error" to jsonObjectOf(
                    "code" to JsonPrimitive(ErrorCode.InternalError.code),
                    "message" to JsonPrimitive("Lorem ipsum")
                )
            ),
            client.receive()
        )
    }

    @Test
    @DisplayName("supports responses with results")
    fun supports_responses_with_results() = testJsonRpc {
        client.send(
            jsonObjectOf(
                "jsonrpc" to JsonPrimitive("2.0"),
                "id" to JsonPrimitive(1),
                "result" to JsonPrimitive("test")
            )
        )

        var called = 0
        server.jsonRpc {
            ++called
            response {
                assertEquals(1, called)

                assertEquals("2.0", jsonrpc)
                assertEquals(JsonIntOrString.IntegerValue(1), id)
                assertEquals(JsonPrimitive("test"), result)
                assertEquals(null, error)
            }
        }

        assertEquals(1, called, "The response DSL should have been called.")
    }

    @Test
    @DisplayName("supports responses with errors")
    fun supports_responses_with_errors() = testJsonRpc {
        client.send(
            jsonObjectOf(
                "jsonrpc" to JsonPrimitive("2.0"),
                "id" to JsonPrimitive(1),
                "error" to jsonObjectOf(
                    "code" to JsonPrimitive(-32601),
                    "message" to JsonPrimitive("Method 'foo' not found.")
                )
            )
        )

        var called = 0
        server.jsonRpc {
            ++called
            response {
                assertEquals(1, called)

                assertEquals("2.0", jsonrpc)
                assertEquals(JsonIntOrString.IntegerValue(1), id)
                assertEquals(null, result)

                assertEquals(ErrorCode.MethodNotFound, error?.code)
                assertEquals("Method 'foo' not found.", error?.message)
                assertEquals(null, error?.data)
            }
        }

        assertEquals(1, called, "The response DSL should have been called.")
    }

    @Test
    @DisplayName("supports multiple response messages")
    fun supports_multiple_response_messages() = testJsonRpc {
        client.send(
            jsonObjectOf(
                "jsonrpc" to JsonPrimitive("2.0"),
                "id" to JsonPrimitive(1),
                "result" to JsonPrimitive("test")
            )
        )
        client.send(
            jsonObjectOf(
                "jsonrpc" to JsonPrimitive("2.0"),
                "id" to JsonPrimitive(2),
                "error" to jsonObjectOf(
                    "code" to JsonPrimitive(-32601),
                    "message" to JsonPrimitive("Method 'foo' not found.")
                )
            )
        )

        var called = 0
        server.jsonRpc {
            ++called
            response {
                when (id) {
                    JsonIntOrString.IntegerValue(1) -> {
                        assertEquals(1, called)

                        assertEquals("2.0", jsonrpc)
                        assertEquals(JsonPrimitive("test"), result)
                        assertEquals(null, error)
                    }

                    JsonIntOrString.IntegerValue(2) -> {
                        assertEquals(2, called)

                        assertEquals(JsonIntOrString.IntegerValue(2), id)
                        assertEquals("2.0", jsonrpc)
                        assertEquals(null, result)

                        assertEquals(ErrorCode.MethodNotFound, error?.code)
                        assertEquals("Method 'foo' not found.", error?.message)
                        assertEquals(null, error?.data)
                    }

                    else -> assertTrue(false, "Unknown response id: $id")
                }
            }
        }

        assertEquals(2, called, "The response DSL should have been called.")
    }

    @Test
    @DisplayName("supports batched response messages")
    fun supports_batched_response_messages() = testJsonRpc {
        client.send(
            jsonArrayOf(
                jsonObjectOf(
                    "jsonrpc" to JsonPrimitive("2.0"),
                    "id" to JsonPrimitive(1),
                    "result" to JsonPrimitive("test")
                ),
                jsonObjectOf(
                    "jsonrpc" to JsonPrimitive("2.0"),
                    "id" to JsonPrimitive(2),
                    "error" to jsonObjectOf(
                        "code" to JsonPrimitive(-32601),
                        "message" to JsonPrimitive("Method 'foo' not found.")
                    )
                )
            )
        )

        var called = 0
        server.jsonRpc {
            ++called
            response {
                when (id) {
                    JsonIntOrString.IntegerValue(1) -> {
                        assertEquals(1, called)

                        assertEquals("2.0", jsonrpc)
                        assertEquals(JsonPrimitive("test"), result)
                        assertEquals(null, error)
                    }

                    JsonIntOrString.IntegerValue(2) -> {
                        assertEquals(2, called)

                        assertEquals(JsonIntOrString.IntegerValue(2), id)
                        assertEquals("2.0", jsonrpc)
                        assertEquals(null, result)

                        assertEquals(ErrorCode.MethodNotFound, error?.code)
                        assertEquals("Method 'foo' not found.", error?.message)
                        assertEquals(null, error?.data)
                    }

                    else -> assertTrue(false, "Unknown response id: $id")
                }
            }
        }

        assertEquals(2, called, "The response DSL should have been called.")
    }

    @Test
    @DisplayName("supports sending notifications without parameters")
    fun supports_sending_notifications_without_parameters() = testJsonRpc {
        client.send(
            jsonObjectOf(
                "jsonrpc" to JsonPrimitive("2.0"),
                "id" to JsonPrimitive(1),
                "result" to JsonPrimitive("test")
            )
        )

        server.jsonRpc {
            response {
                sendNotification(Notification(method = "lorem/ipsum"))
                sendNotification(method = "notify/test")
            }
        }

        assertEquals(
            jsonObjectOf(
                "jsonrpc" to JsonPrimitive("2.0"),
                "method" to JsonPrimitive("lorem/ipsum")
            ),
            client.receive()
        )

        assertEquals(
            jsonObjectOf(
                "jsonrpc" to JsonPrimitive("2.0"),
                "method" to JsonPrimitive("notify/test")
            ),
            client.receive()
        )
    }

    @Test
    @DisplayName("supports sending notifications with parameters")
    fun supports_sending_notifications_with_parameters() = testJsonRpc {
        client.send(
            jsonObjectOf(
                "jsonrpc" to JsonPrimitive("2.0"),
                "id" to JsonPrimitive(1),
                "result" to JsonPrimitive("test")
            )
        )

        server.jsonRpc {
            response {
                sendNotification(Notification(method = "lorem/ipsum", params = jsonArrayOf(JsonPrimitive(5))))
                sendNotification(method = "notify/test", params = jsonArrayOf(JsonPrimitive(123)))
            }
        }

        assertEquals(
            jsonObjectOf(
                "jsonrpc" to JsonPrimitive("2.0"),
                "method" to JsonPrimitive("lorem/ipsum"),
                "params" to jsonArrayOf(JsonPrimitive(5))
            ),
            client.receive()
        )

        assertEquals(
            jsonObjectOf(
                "jsonrpc" to JsonPrimitive("2.0"),
                "method" to JsonPrimitive("notify/test"),
                "params" to jsonArrayOf(JsonPrimitive(123))
            ),
            client.receive()
        )
    }

    @Test
    @DisplayName("supports sending requests without parameters for integer|string ids")
    fun supports_sending_requests_without_parameters_for_integer_or_string_ids() = testJsonRpc {
        client.send(
            jsonObjectOf(
                "jsonrpc" to JsonPrimitive("2.0"),
                "id" to JsonPrimitive(1),
                "result" to JsonPrimitive("test")
            )
        )

        server.jsonRpc {
            response {
                sendRequest(
                    RequestObject(
                        method = "lorem/ipsum",
                        id = JsonIntOrString.IntegerValue(1)
                    )
                )
                sendRequest(
                    method = "notify/test",
                    id = JsonIntOrString.IntegerValue(2)
                )
            }
        }

        assertEquals(
            jsonObjectOf(
                "jsonrpc" to JsonPrimitive("2.0"),
                "id" to JsonPrimitive(1),
                "method" to JsonPrimitive("lorem/ipsum")
            ),
            client.receive()
        )

        assertEquals(
            jsonObjectOf(
                "jsonrpc" to JsonPrimitive("2.0"),
                "id" to JsonPrimitive(2),
                "method" to JsonPrimitive("notify/test")
            ),
            client.receive()
        )
    }

    @Test
    @DisplayName("supports sending requests without parameters for integer ids")
    fun supports_sending_requests_without_parameters_for_integer_ids() = testJsonRpc {
        client.send(
            jsonObjectOf(
                "jsonrpc" to JsonPrimitive("2.0"),
                "id" to JsonPrimitive(1),
                "result" to JsonPrimitive("test")
            )
        )

        server.jsonRpc {
            response {
                sendRequest(
                    RequestObject(
                        method = "lorem/ipsum",
                        id = JsonIntOrString.IntegerValue(1)
                    )
                )
                sendRequest(
                    method = "notify/test",
                    id = JsonIntOrString.IntegerValue(2)
                )
            }
        }

        assertEquals(
            jsonObjectOf(
                "jsonrpc" to JsonPrimitive("2.0"),
                "id" to JsonPrimitive(1),
                "method" to JsonPrimitive("lorem/ipsum")
            ),
            client.receive()
        )

        assertEquals(
            jsonObjectOf(
                "jsonrpc" to JsonPrimitive("2.0"),
                "id" to JsonPrimitive(2),
                "method" to JsonPrimitive("notify/test")
            ),
            client.receive()
        )
    }

    @Test
    @DisplayName("supports sending requests without parameters for string ids")
    fun supports_sending_requests_without_parameters_for_string_ids() = testJsonRpc {
        client.send(
            jsonObjectOf(
                "jsonrpc" to JsonPrimitive("2.0"),
                "id" to JsonPrimitive(1),
                "result" to JsonPrimitive("test")
            )
        )

        server.jsonRpc {
            response {
                sendRequest(
                    RequestObject(
                        method = "lorem/ipsum",
                        id = "one"
                    )
                )
                sendRequest(
                    method = "notify/test",
                    id = "two"
                )
            }
        }

        assertEquals(
            jsonObjectOf(
                "jsonrpc" to JsonPrimitive("2.0"),
                "id" to JsonPrimitive("one"),
                "method" to JsonPrimitive("lorem/ipsum")
            ),
            client.receive()
        )

        assertEquals(
            jsonObjectOf(
                "jsonrpc" to JsonPrimitive("2.0"),
                "id" to JsonPrimitive("two"),
                "method" to JsonPrimitive("notify/test")
            ),
            client.receive()
        )
    }

    @Test
    @DisplayName("supports sending requests with parameters for integer|string ids")
    fun supports_sending_requests_with_parameters_for_integer_or_string_ids() = testJsonRpc {
        client.send(
            jsonObjectOf(
                "jsonrpc" to JsonPrimitive("2.0"),
                "id" to JsonPrimitive(1),
                "result" to JsonPrimitive("test")
            )
        )

        server.jsonRpc {
            response {
                sendRequest(
                    RequestObject(
                        method = "lorem/ipsum",
                        id = JsonIntOrString.IntegerValue(1),
                        params = jsonArrayOf(JsonPrimitive(5))
                    )
                )
                sendRequest(
                    method = "notify/test",
                    id = JsonIntOrString.IntegerValue(2),
                    params = jsonArrayOf(JsonPrimitive(123))
                )
            }
        }

        assertEquals(
            jsonObjectOf(
                "jsonrpc" to JsonPrimitive("2.0"),
                "id" to JsonPrimitive(1),
                "method" to JsonPrimitive("lorem/ipsum"),
                "params" to jsonArrayOf(JsonPrimitive(5))
            ),
            client.receive()
        )

        assertEquals(
            jsonObjectOf(
                "jsonrpc" to JsonPrimitive("2.0"),
                "id" to JsonPrimitive(2),
                "method" to JsonPrimitive("notify/test"),
                "params" to jsonArrayOf(JsonPrimitive(123))
            ),
            client.receive()
        )
    }

    @Test
    @DisplayName("supports sending requests with parameters for integer ids")
    fun supports_sending_requests_with_parameters_for_integer_ids() = testJsonRpc {
        client.send(
            jsonObjectOf(
                "jsonrpc" to JsonPrimitive("2.0"),
                "id" to JsonPrimitive(1),
                "result" to JsonPrimitive("test")
            )
        )

        server.jsonRpc {
            response {
                sendRequest(
                    RequestObject(
                        method = "lorem/ipsum",
                        id = 1,
                        params = jsonArrayOf(JsonPrimitive(5))
                    )
                )
                sendRequest(
                    method = "notify/test",
                    id = 2,
                    params = jsonArrayOf(JsonPrimitive(123))
                )
            }
        }

        assertEquals(
            jsonObjectOf(
                "jsonrpc" to JsonPrimitive("2.0"),
                "id" to JsonPrimitive(1),
                "method" to JsonPrimitive("lorem/ipsum"),
                "params" to jsonArrayOf(JsonPrimitive(5))
            ),
            client.receive()
        )

        assertEquals(
            jsonObjectOf(
                "jsonrpc" to JsonPrimitive("2.0"),
                "id" to JsonPrimitive(2),
                "method" to JsonPrimitive("notify/test"),
                "params" to jsonArrayOf(JsonPrimitive(123))
            ),
            client.receive()
        )
    }

    @Test
    @DisplayName("supports sending requests with parameters for string ids")
    fun supports_sending_requests_with_parameters_for_string_ids() = testJsonRpc {
        client.send(
            jsonObjectOf(
                "jsonrpc" to JsonPrimitive("2.0"),
                "id" to JsonPrimitive(1),
                "result" to JsonPrimitive("test")
            )
        )

        server.jsonRpc {
            response {
                sendRequest(
                    RequestObject(
                        method = "lorem/ipsum",
                        id = "one",
                        params = jsonArrayOf(JsonPrimitive(5))
                    )
                )
                sendRequest(
                    method = "notify/test",
                    id = "two",
                    params = jsonArrayOf(JsonPrimitive(123))
                )
            }
        }

        assertEquals(
            jsonObjectOf(
                "jsonrpc" to JsonPrimitive("2.0"),
                "id" to JsonPrimitive("one"),
                "method" to JsonPrimitive("lorem/ipsum"),
                "params" to jsonArrayOf(JsonPrimitive(5))
            ),
            client.receive()
        )

        assertEquals(
            jsonObjectOf(
                "jsonrpc" to JsonPrimitive("2.0"),
                "id" to JsonPrimitive("two"),
                "method" to JsonPrimitive("notify/test"),
                "params" to jsonArrayOf(JsonPrimitive(123))
            ),
            client.receive()
        )
    }
}
