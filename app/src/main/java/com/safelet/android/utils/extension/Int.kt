package utils.extension

import java.nio.ByteBuffer

fun Int.toByteArray(): ByteArray {
    return ByteBuffer.allocate(4).putInt(this).array()
}
