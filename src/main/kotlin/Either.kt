

sealed class Either<E, out A> {

    abstract fun <B> map(f: (A) -> B): Either<E, B>

    abstract fun <B> flatMap(f: (A) -> Either<E, B>): Either<E, B>

    abstract fun getOrElse(defaultValue: () -> @UnsafeVariance A): A

    fun orElse(defaultValue: () -> Either<E, @UnsafeVariance A>): Either<E, A> {
        return map { this }.getOrElse(defaultValue)
    }

    internal class Left<E, out A>(private val value: E): Either<E, A>() {

        override fun <B> map(f: (A) -> B): Either<E, B> = Left(value)

        override fun <B> flatMap(f: (A) -> Either<E, B>): Either<E, B> {
            return Left(value)
        }

        override fun getOrElse(defaultValue: () -> @UnsafeVariance A): A {
            return defaultValue()
        }

        override fun toString(): String = "Left($value)"
    }

    internal class Right<E, out A>(private val value: A) : Either<E, A>() {

        override fun <B> map(f: (A) -> B): Either<E, B> = Right(f(value))

        override fun <B> flatMap(f: (A) -> Either<E, B>): Either<E, B> {
            return f(value)
        }

        override fun getOrElse(defaultValue: () -> @UnsafeVariance A): A {
            return value
        }

        override fun toString(): String = "Right($value)"
    }

    companion object {

        fun <E, A> left(value: E): Either<E, A> = Left(value)

        fun <E, B> right(value: B): Either<E, B> = Right(value)
    }
}