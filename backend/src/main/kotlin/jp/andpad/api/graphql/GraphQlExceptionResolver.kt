package jp.andpad.api.graphql

import graphql.GraphQLError
import graphql.GraphqlErrorBuilder
import graphql.schema.DataFetchingEnvironment
import jp.andpad.api.security.UnauthorizedException
import org.springframework.graphql.execution.DataFetcherExceptionResolver
import org.springframework.graphql.execution.ErrorType
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

/**
 * GraphQL データフェッチャー例外のグローバルリゾルバー。
 *
 * Spring GraphQL の [DataFetcherExceptionResolver] 実装として、Query / Mutation 実行中に
 * サービス層から送出された例外を GraphQL 仕様に沿った [GraphQLError] へ変換する。
 * クライアントは HTTP 200 のまま `errors` 配列内の `extensions.classification` で
 * エラー種別（UNAUTHORIZED / BAD_REQUEST 等）を判別できる。
 *
 * マッピング対象外の例外は [Mono.empty] を返し、Spring GraphQL のデフォルト処理
 * （INTERNAL_ERROR 等）に委譲する。
 */
@Component
class GraphQlExceptionResolver : DataFetcherExceptionResolver {

    /**
     * 単一の例外を GraphQL エラーリストへ解決する。
     *
     * | 例外型 | GraphQL ErrorType | 動作 |
     * |--------|-------------------|------|
     * | [UnauthorizedException] | UNAUTHORIZED | JWT 未設定・無効など認証不足。例外メッセージをそのまま返却。 |
     * | [IllegalArgumentException] | BAD_REQUEST | 入力不正・リソース未存在・業務ルール違反等。例外メッセージをそのまま返却。 |
     * | 上記以外 | （委譲） | 空 Mono を返し、フレームワークのデフォルト例外ハンドラが処理。 |
     *
     * @param ex データフェッチャー実行中に捕捉された例外。
     * @param env 現在の GraphQL 実行コンテキスト（パス・フィールド情報をエラー位置へ付与）。
     * @return 変換済み GraphQL エラーの Mono。未対応例外の場合は空 Mono。
     */
    override fun resolveException(
        ex: Throwable,
        env: DataFetchingEnvironment,
    ): Mono<List<GraphQLError>> = when (ex) {
        is UnauthorizedException -> Mono.just(
            listOf(
                GraphqlErrorBuilder.newError(env)
                    .errorType(ErrorType.UNAUTHORIZED)
                    .message(ex.message)
                    .build(),
            ),
        )
        is IllegalArgumentException -> Mono.just(
            listOf(
                GraphqlErrorBuilder.newError(env)
                    .errorType(ErrorType.BAD_REQUEST)
                    .message(ex.message)
                    .build(),
            ),
        )
        else -> Mono.empty()
    }
}
