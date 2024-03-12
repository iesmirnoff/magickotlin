import kotlin.math.pow

sealed class Option<out A> {
    abstract fun isEmpty(): Boolean

    fun getOrElse(default: @UnsafeVariance A): A {
        return when (this) {
            is None -> default
            is Some -> value
        }
    }

    fun orElse(default: () -> Option<@UnsafeVariance A>): Option<A> {
        return map { this }.getOrElse(default)
    }

    fun getOrElse(default: () -> @UnsafeVariance A): A {
        return when (this) {
            is None -> default()
            is Some -> value
        }
    }

    fun filter(p: (A) -> Boolean): Option<A> {
        return flatMap { if (p(it)) this else None }
    }

    fun <B> map(f: (A) -> B): Option<B> {
        return when (this) {
            is None -> None
            is Some -> Some(f(value))
        }
    }

    fun <B> flatMap(f: (A) -> Option<B>): Option<B> {
        return map(f).getOrElse(None)
    }

    internal object None : Option<Nothing>() {
        override fun isEmpty() = true

        override fun toString(): String = "None"
        override fun equals(other: Any?): Boolean =
            other === None

        override fun hashCode(): Int = 0
    }

    internal data class Some<out A>(internal val value: A) : Option<A>() {
        override fun isEmpty() = false
    }

    companion object {
        operator fun <A> invoke(a: A? = null): Option<A> = when (a) {
            null -> None
            else -> Some(a)
        }
    }
}

fun <A, B, C> map2(oa: Option<A>, ob: Option<B>, f: (A) -> (B) -> C): Option<C> {
    return oa.flatMap { a -> ob.map { b -> f(a)(b) } }
}

val mean: (List<Double>) -> Option<Double> = { list ->
    when {
        list.isEmpty() -> Option()
        else -> Option(list.sum() / list.size)
    }
}

val variance: (List<Double>) -> Option<Double> = { list ->
    mean(list).flatMap { m ->
        mean(list.map { x -> (x - m).pow(2.0) })
    }
}
