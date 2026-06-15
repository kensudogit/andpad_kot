package jp.andpad.api.util

import java.util.HexFormat
import java.util.concurrent.ThreadLocalRandom

object Ids {

    fun random(prefix: String): String {
        val nano = System.nanoTime()
        val rand = ThreadLocalRandom.current().nextInt()
        val hex = HexFormat.of().toHexDigits(nano) + HexFormat.of().toHexDigits(rand)
        return prefix + hex
    }
}
