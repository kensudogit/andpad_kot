/**
 * 一意 ID 生成ユーティリティ。
 * ナノ秒タイムスタンプと乱数を 16 進連結し、
 * プレフィックス付きの衝突しにくい ID をリポジトリ層で生成する。
 */
package jp.andpad.api.util

import java.util.HexFormat
import java.util.concurrent.ThreadLocalRandom

/**
 * プレフィックス付きランダム ID を生成するオブジェクト。
 */
object Ids {

    /**
     * プレフィックス + 16 進（nanoTime + 乱数）で ID を生成する。
     * UUID より短く、デバッグ時にエンティティ種別がプレフィックスで判別できる。
     *
     * @param prefix エンティティ種別プレフィックス（例: "usr_", "org_"）
     * @return 一意性の高い ID 文字列
     */
    fun random(prefix: String): String {
        val nano = System.nanoTime()
        val rand = ThreadLocalRandom.current().nextInt()
        val hex = HexFormat.of().toHexDigits(nano) + HexFormat.of().toHexDigits(rand)
        return prefix + hex
    }
}
