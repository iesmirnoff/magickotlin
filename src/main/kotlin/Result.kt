import java.io.Serializable

sealed class Result<out A> : Serializable {

    abstract fun <B> map(f: (A) -> B): Result<B>
    abstract fun <B> flatMap(f: (A) -> Result<B>): Result<B>
    fun getOrElse(defaultValue: @UnsafeVariance A): A {
        return when (this) {
            is Success -> value
            else -> defaultValue
        }
    }

    fun getOrElse(defaultValue: () -> @UnsafeVariance A): A {
        return when (this) {
            is Success -> value
            else -> defaultValue()
        }
    }

    fun orElse(defaultValue: () -> Result<@UnsafeVariance A>): Result<A> {
        return when (this) {
            is Success -> this
            else -> try {
                defaultValue()
            } catch (e: RuntimeException) {
                failure(e)
            } catch (e: Exception) {
                failure(RuntimeException(e))
            }
        }
    }

    fun filter(p: (A) -> Boolean): Result<A> {
        return flatMap {
            if (p(it)) {
                this
            } else {
                Empty
            }
        }
    }

    fun filter(message: String, p: (A) -> Boolean): Result<A> {
        return flatMap {
            if (p(it)) {
                this
            } else {
                failure(message)
            }
        }
    }

    fun exists(p: (A) -> Boolean): Boolean {
        return map(p).getOrElse(false)
    }

    abstract fun mapFailure(message: String): Result<A>

    abstract fun forEach(
        onSuccess: (A) -> Unit = {},
        onFailure: (Exception) -> Unit = {},
        onEmpty: () -> Unit = {}
    )

    class Success<A> internal constructor(internal val value: A) : Result<A>() {
        override fun <B> map(f: (A) -> B): Result<B> {
            return try {
                Success(f(value))
            } catch (e: Exception) {
                failure(e)
            }
        }

        override fun <B> flatMap(f: (A) -> Result<B>): Result<B> {
            return f(value)
        }

        override fun mapFailure(message: String): Result<A> {
            return this
        }

        override fun forEach(
            onSuccess: (A) -> Unit,
            onFailure: (Exception) -> Unit,
            onEmpty: () -> Unit
        ) {
            onSuccess(value)
        }

        override fun toString(): String = "Success($value)"
    }

    class Failure<A> internal constructor(private val exception: Exception) : Result<A>() {
        override fun <B> map(f: (A) -> B): Result<B> {
            return Failure(exception)
        }

        override fun <B> flatMap(f: (A) -> Result<B>): Result<B> {
            return failure(exception)
        }

        override fun mapFailure(message: String): Result<A> {
            return failure(RuntimeException(message))
        }

        override fun forEach(
            onSuccess: (A) -> Unit,
            onFailure: (Exception) -> Unit,
            onEmpty: () -> Unit
        ) {
            onFailure(exception)
        }

        override fun toString(): String = "Failure(${exception.message})"
    }

    internal data object Empty : Result<Nothing>() {
        override fun <B> map(f: (Nothing) -> B): Result<B> = Empty
        override fun <B> flatMap(f: (Nothing) -> Result<B>): Result<B> = Empty
        override fun mapFailure(message: String): Result<Nothing> {
            return this
        }

        override fun forEach(
            onSuccess: (Nothing) -> Unit,
            onFailure: (Exception) -> Unit,
            onEmpty: () -> Unit
        ) {
            onEmpty()
        }
    }

    companion object {
        operator fun <A> invoke(value: A? = null): Result<A> {
            return when (value) {
                null -> Failure(NullPointerException())
                else -> Success<A>(value)
            }
        }

        operator fun <A> invoke(): Result<A> = Empty

        operator fun <A> invoke(a: A? = null, message: String): Result<A> {
            return Result(a).mapFailure(message)
        }

        operator fun <A> invoke(a: A? = null, p: (A) -> Boolean): Result<A> {
            return Result(a).filter(p)
        }

        operator fun <A> invoke(a: A? = null, message: String, p: (A) -> Boolean): Result<A> {
            return Result(a).filter(p).mapFailure(message)
        }

        fun <A> failure(message: String): Result<A> {
            return Failure(IllegalStateException(message))
        }

        fun <A> failure(exception: Exception): Result<A> {
            return Failure(exception)
        }

        fun <A, B> lift(f: (A) -> B): (Result<A>) -> Result<B> {
            return { it.map(f) }
        }

        fun <A, B, C> lift2(f: (A) -> (B) -> C): (Result<A>) -> (Result<B>) -> Result<C> {
            return { ra ->
                { rb ->
                    ra.flatMap { a ->
                        rb.map { b -> f(a)(b) }
                    }
                }
            }
        }

        fun <A, B, C, D> lift3(f: (A) -> (B) -> (C) -> D): (Result<A>) -> (Result<B>) -> (Result<C>) -> Result<D> {
            return { ra ->
                { rb ->
                    { rc ->
                        ra.flatMap { a ->
                            rb.flatMap { b ->
                                rc.map { c ->
                                    f(a)(b)(c)
                                }
                            }
                        }
                    }
                }
            }
        }

        fun <A, B, C> map2(
            a: Result<A>,
            b: Result<B>,
            f: (A) -> (B) -> C
        ): Result<C> {
            return lift2(f)(a)(b)
        }
    }
}