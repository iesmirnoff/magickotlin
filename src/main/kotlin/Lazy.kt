class Lazy<out A>(func: () -> A) : () -> A {

    private val value: A by lazy(func)

    override fun invoke(): A {
        return value
    }

    fun <B> map(f: (A) -> B): Lazy<B> {
        return Lazy { f(value) }
    }

    fun <B> flatMap(f: (A) -> Lazy<B>): Lazy<B> {
        return Lazy { f(value)() }
    }
}

fun <A> sequence(lst: RecList<Lazy<A>>): Lazy<RecList<A>> {
    return Lazy { lst.map { it() } }
}

fun <A> sequenceResult(lst: RecList<Lazy<A>>): Lazy<Result<RecList<A>>> {
    return Lazy {
        Result(lst.map { it() })
    }
}

fun constructMessage(greetings: Lazy<String>, name: Lazy<String>): Lazy<String> {
    return Lazy { "${greetings()}, ${name()}!" }
}

val consMessage: (String) -> (String) -> String =
    { greetings ->
        { name ->
            "$greetings, $name!"
        }
    }

val liftLazy2: ((String) -> (String) -> String) -> (Lazy<String>) -> (Lazy<String>) -> Lazy<String> = { f ->
    { l1 ->
        { l2 ->
            Lazy {
                f(l1())(l2())
            }
        }
    }
}

fun <A, B, C> liftLazy2(f: (A) -> (B) -> C): (Lazy<A>) -> (Lazy<B>) -> Lazy<C> {
    return { l1 -> { l2 -> Lazy { f(l1())(l2()) } } }
}
