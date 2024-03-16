import java.io.IOException

fun main() {

    (1..10).toList()
        .map {
            println(it)
            it
        }
        .map { it * 2 }
        .map {
            println(it)
            it
        }
        .toList()
    println()
    Stream.from(1)
        .takeAtMost(10)
        .map {
            println(it)
            it
        }
        .map { it * 2 }
        .map {
            println(it)
            it
        }
        .toList()
}

fun main1(args: Array<String>) {
    var stream = Stream.from(0).takeWhile { it < 5 }.append(Lazy { Stream.from(7) })
    var h = stream.headSafe()
    while (h.exists { it < 10 }) {
        println(h.getOrElse(-1))
        stream = stream.tail().getOrElse(Stream.invoke())
        h = stream.headSafe()
    }
}

fun or(a: Lazy<Boolean>, b: Lazy<Boolean>): Boolean =
    if (a()) true else b()


fun getName(): Result<String> {
    return try {
        validate(readlnOrNull())
    } catch (e: IOException) {
        Result.failure(e)
    }
}

fun validate(name: String?): Result<String> {
    return when {
        name?.isNotEmpty() ?: false -> Result(name)
        else -> Result.failure(IOException())
    }
}

fun <K, V> Map<K, V>.getResult(key: K): Result<V & Any> {
    return when {
        this.containsKey(key) -> Result(this[key])
        else -> Result.Empty
    }
}

data class Toon internal constructor(
    val firstName: String,
    val lastName: String,
    val email: Result<String>
) {
    companion object {
        operator fun invoke(
            firstName: String,
            lastName: String
        ): Toon {
            return Toon(
                firstName, lastName, Result.Empty
            )
        }

        operator fun invoke(
            firstName: String,
            lastName: String,
            email: String
        ): Toon {
            return Toon(firstName, lastName, Result(email))
        }
    }
}