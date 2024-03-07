import java.math.BigInteger


fun main() {

}

fun fib(n: Int): String {
    val fibs = iterate(1 to 1, n) { it.second to it.first + it.second }
    return map(fibs) { it.first }.joinToString(", ")
}

fun <T> iterate(seed: T, n: Int, f: (T) -> T): List<T> {
    tailrec fun iterate(acc: List<T>, seed: T): List<T> {
        return if (acc.size < n) {
            iterate(acc + seed, f(seed))
        } else {
            acc
        }
    }
    return iterate(listOf(), seed)
}

fun <T, U> map(list: List<T>, f: (T) -> U): List<U> {
    tailrec fun map(list: List<T>, result: List<U> = listOf()): List<U> {
        if (list.isEmpty()) {
            return result
        }
        return map(list.drop(1), result + f(list.first()))
    }
    return map(list, listOf())
}

fun <T, U> foldLeft(list: List<T>, z: U, f: (U, T) -> U): U {
    tailrec fun foldLeft(list: List<T>, acc: U): U =
        if (list.isEmpty()) {
            acc
        } else {
            foldLeft(list.drop(1), f(acc, list.first()))
        }
    return foldLeft(list, z)
}

fun fibonacci(n: Int): String {
    tailrec fun fibonacci(
        n: Int,
        f1: BigInteger = BigInteger.ZERO,
        f2: BigInteger = BigInteger.ONE,
        acc: String = ""
    ): String {
        if (n <= 1) {
            return "$acc$f2"
        }
        return fibonacci(n - 1, f2, f1 + f2, "$acc$f2, ")
    }
    return fibonacci(n)
}

fun <T> unfold(seed: T, f: (T) -> T, p: (T) -> Boolean): List<T> {
    tailrec fun unfold(acc: List<T>, seed: T): List<T> =
        if (p(seed)) {
            unfold(acc + seed, f(seed))
        } else {
            acc
        }
    return unfold(listOf(), seed)
}
