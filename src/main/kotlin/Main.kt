import java.io.IOException

fun main() {

    val toons: Map<String, Toon> = mapOf(
        "Mickey" to Toon("Mickey", "Mouse", "mickey@disney.com"),
        "Minnie" to Toon("Minnie", "Mouse"),
        "Donald" to Toon("Donald", "Duck", "donald@disney.com")
    )

    val toon = getName()
        .flatMap(toons::getResult)
        .flatMap(Toon::email)
    println(toon)
}

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
                firstName, lastName, Result.Empty)
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