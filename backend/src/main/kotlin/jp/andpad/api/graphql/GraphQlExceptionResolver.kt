package jp.andpad.api.graphql

import graphql.GraphQLError
import graphql.GraphqlErrorBuilder
import graphql.schema.DataFetchingEnvironment
import jp.andpad.api.security.UnauthorizedException
import org.springframework.graphql.execution.DataFetcherExceptionResolver
import org.springframework.graphql.execution.ErrorType
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

/** GraphQL データフェッチャー例外をクライアント向けエラー種別へ変換する。 */
@Component
class GraphQlExceptionResolver : DataFetcherExceptionResolver {

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
