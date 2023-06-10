package earth.levi.app.extensions

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

inline fun <reified T> String.toObjectFromJsonString(): T = Json.decodeFromString(this)
inline fun <reified T> T.toJsonString(): String = Json.encodeToString(this)