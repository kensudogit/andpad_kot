package jp.andpad.api.graphql

import graphql.schema.DataFetchingEnvironment
import jp.andpad.api.security.UnauthorizedException
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.Answers
import org.mockito.Mockito.mock
import org.springframework.graphql.execution.ErrorType

class GraphQlExceptionResolverTest {

    private val resolver = GraphQlExceptionResolver()
    private val env: DataFetchingEnvironment =
        mock(DataFetchingEnvironment::class.java, Answers.RETURNS_DEEP_STUBS)

    @Test
    fun resolvesUnauthorizedException() {
        val errors = resolver.resolveException(UnauthorizedException("login required"), env).block()

        assertThat(errors).hasSize(1)
        assertThat(errors!![0].errorType).isEqualTo(ErrorType.UNAUTHORIZED)
        assertThat(errors[0].message).isEqualTo("login required")
    }

    @Test
    fun resolvesIllegalArgumentException() {
        val errors = resolver.resolveException(IllegalArgumentException("invalid input"), env).block()

        assertThat(errors).hasSize(1)
        assertThat(errors!![0].errorType).isEqualTo(ErrorType.BAD_REQUEST)
        assertThat(errors[0].message).isEqualTo("invalid input")
    }

    @Test
    fun delegatesUnknownExceptions() {
        val errors = resolver.resolveException(RuntimeException("boom"), env).block()

        assertThat(errors).isNull()
    }
}
