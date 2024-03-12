sealed class RecList<A> {

    abstract val length: Int
    abstract fun isEmpty(): Boolean

    fun setHead(head: A): RecList<A> {
        return when (this) {
            is Nil -> throw IllegalStateException("setHead called on an empty list")
            is Cons -> tail.cons(head)
        }
    }

    fun cons(head: A): RecList<A> {
        return Cons(head, this)
    }

    fun dropAtMost(n: Int): RecList<A> {
        tailrec fun dropAtMost(n: Int, list: RecList<A>): RecList<A> {
            if (n <= 0) {
                return list
            }
            return when (list) {
                is Nil -> list
                is Cons -> dropAtMost(n - 1, list.tail)
            }
        }
        return dropAtMost(n, this)
    }

    fun dropWhile(p: (A) -> Boolean): RecList<A> {
        tailrec fun dropWhile(p: (A) -> Boolean, list: RecList<A>): RecList<A> {
            return when (list) {
                is Nil -> list
                is Cons -> {
                    if (p(list.head).not()) {
                        list
                    } else {
                        dropWhile(p, list.tail)
                    }
                }
            }
        }
        return dropWhile(p, this)
    }

    fun <B> map(f: (A) -> B): RecList<B> {
        return foldLeft(Nil as RecList<B>) { acc: RecList<B> ->
            { h: A -> Cons(f(h), acc) }
        }.reverse()
    }

    fun filter(p: (A) -> Boolean): RecList<A> {
        return foldLeft(Nil as RecList<A>) { acc -> { a -> if (p(a)) acc.cons(a) else acc } }.reverse()
    }

    fun <B> flatMap(f: (A) -> RecList<B>): RecList<B> {
        return flatten(map(f))
    }

    fun concat(list: RecList<A>): RecList<A> {
        return concat(this, list)
    }

    abstract fun dropLast(): RecList<A>

    fun reverse(): RecList<A> {
        return reverse(this)
    }

    fun <B> foldRight(identity: B, f: (A) -> (B) -> B): B {
        return foldRight(this, identity, f)
    }

    fun <B> foldLeft(identity: B, f: (B) -> (A) -> B): B {
        return foldLeft(this, identity, f)
    }

    fun length(): Int {
        return foldLeft(0) { { _ -> it + 1 } }
    }

    fun headSafe(): Result<A> {
        return when (this) {
            is Nil -> Result()
            is Cons -> Result(head)
        }
    }

    fun lastSafe(): Result<A> {
        return when (this) {
            is Cons -> reverse().headSafe()
            is Nil -> headSafe()
        }
    }

    fun <A1, A2> unzip(f: (A) -> Pair<A1, A2>): Pair<RecList<A1>, RecList<A2>> {
        return when (this) {
            is Nil -> Pair(Cons(), Cons())
            is Cons -> foldLeft(Pair(Cons(), Cons())) { acc ->
                { next ->
                    val pair = f(next)
                    acc.first.cons(pair.first)
                    acc.second.cons(pair.second)
                    acc
                }
            }
        }
    }

    fun getAt(index: Int): Result<A> {
        tailrec fun getAt(index: Int, list: RecList<A>): Result<A> {
            return when {
                list is Nil -> Result.failure("")
                index == 0 -> list.headSafe()
                else -> getAt(index - 1, list.dropAtMost(1))
            }
        }
        return getAt(index, this)
    }

    fun splitAt(index: Int): Pair<RecList<A>, RecList<A>> {
        val ii = if (index < 0) 0
        else if (index >= length()) length() else index
        val identity = Triple(Nil as RecList<A>, Nil as RecList<A>, ii)
        val rt = foldLeft(identity) { ta: Triple<RecList<A>, RecList<A>, Int> ->
            { a: A ->
                if (ta.third == 0)
                    Triple(ta.first, ta.second.cons(a), ta.third)
                else
                    Triple(ta.first.cons(a), ta.second, ta.third - 1)
            }
        }
        return Pair(rt.first.reverse(), rt.second.reverse())
    }

    fun startsWith(sub: RecList<@UnsafeVariance A>): Boolean {
        tailrec fun startsWith(list: RecList<A>, sub: RecList<A>): Boolean =
            when (sub) {
                Nil -> true
                is Cons -> when (list) {
                    Nil -> false
                    is Cons -> if (list.head == sub.head)
                        startsWith(list.tail, sub.tail)
                    else
                        false
                }
            }
        return startsWith(this, sub)
    }

    fun hasSubList(sub: RecList<@UnsafeVariance A>): Boolean {
        tailrec
        fun <A> hasSubList(list: RecList<A>, sub: RecList<A>): Boolean =
            when (list) {
                Nil -> sub.isEmpty()
                is Cons ->
                    if (list.startsWith(sub))
                        true
                    else
                        hasSubList(list.tail, sub)
            }
        return hasSubList(this, sub)
    }

    companion object {

        fun <A> flatten(table: RecList<RecList<A>>): RecList<A> {
            return table.foldLeft(Cons()) { acc -> { head -> acc.concat(head) } }
        }

        fun <A> concat(list1: RecList<A>, list2: RecList<A>): RecList<A> {
            return when (list1) {
                is Nil -> list2
                is Cons -> concat(list1.dropAtMost(1), list2).cons(list1.head)
            }
        }

        fun <A, B> foldRight(list: RecList<A>, acc: B, f: (A) -> (B) -> B): B {
            return when (val reversed = list.reverse()) {
                is Nil -> acc
                is Cons -> foldLeft(
                    reversed.tail,
                    f(reversed.head)(acc)
                ) { b -> { a -> f(a)(b) } } // f(list.head)(foldRight(list.tail, acc, f))
            }
        }

        tailrec fun <A, B> foldLeft(list: RecList<A>, acc: B, f: (B) -> (A) -> B): B {
            return when (list) {
                is Nil -> acc
                is Cons -> foldLeft(list.tail, f(acc)(list.head), f)
            }
        }

        fun <A> reverse(list: RecList<A>): RecList<A> {
            return foldLeft(list, Cons.invoke()) { tail -> { head -> tail.cons(head) } }
        }

        operator fun <A> invoke(vararg a: A): RecList<A> {
            return if (a.isEmpty()) {
                Nil as RecList<A>
            } else {
                Cons(*a)
            }
        }
    }
}

internal class Cons<A>(
    internal val head: A,
    internal val tail: RecList<A>,
) : RecList<A>() {

    override val length: Int = length()

    override fun isEmpty(): Boolean = false

    override fun toString(): String {
        return "[${toString("", this)}NIL]"
    }

    private tailrec fun toString(acc: String, list: RecList<A>): String {
        return when (list) {
            is Nil -> acc
            is Cons -> toString("$acc${list.head}, ", list.tail)
        }
    }

    override fun dropLast(): RecList<A> {
        return reverse().dropAtMost(1).reverse()
    }

    companion object {
        operator fun <A> invoke(vararg az: A): RecList<A> {
            return az.foldRight(Nil as RecList<A>) { a, list -> Cons(a, list) }
        }

        tailrec fun <A> reverse(acc: RecList<A>, list: RecList<A>): RecList<A> {
            return when (list) {
                is Nil -> acc
                is Cons -> reverse(acc.cons(list.head), list.tail)
            }
        }
    }
}

private data object Nil : RecList<Nothing>() {

    override fun dropLast(): RecList<Nothing> = throw IllegalStateException()

    override val length: Int
        get() = 0

    override fun isEmpty(): Boolean = true

    override fun toString(): String = "[NIL]"
}

fun RecList<Int>.sum(): Long {
    return RecList.foldLeft(this, 0) { acc -> { head -> acc + head } }
}

fun RecList<Double>.product(): Double {
    return RecList.foldLeft(this, 1.0) { acc -> { head -> acc * head } }
}

fun <A> RecList<Result<A>>.flattenResult(): RecList<A> {
    return flatMap { ra -> ra.map { RecList(it) }.getOrElse(RecList()) }
}

fun <A> sequence(list: RecList<Result<A>>): Result<RecList<A>> {
    return traverse(list) { it }
}

fun <A, B> traverse(list: RecList<A>, f: (A) -> Result<B>): Result<RecList<B>> {
    return list.map(f).foldLeft(RecList<B>()) { acc ->
        { next ->
            when (next) {
                is Result.Success -> acc.cons(next.value)
                else -> RecList()
            }
        }
    }.run {
        if (isEmpty()) {
            Result.failure("")
        } else {
            Result(this)
        }
    }
}

fun <A, B, C> zipWith(
    list1: RecList<A>,
    list2: RecList<B>,
    f: (A) -> (B) -> C
): RecList<C> {
    tailrec fun <A, B, C> zipWith(
        list1: RecList<A>,
        list2: RecList<B>,
        acc: RecList<C>,
        f: (A) -> (B) -> C
    ): RecList<C> {
        return when {
            list1 is Cons && list2 is Cons -> zipWith(
                list1.dropAtMost(1),
                list2.dropAtMost(1),
                acc.cons(f(list1.head)(list2.head)),
                f
            )

            else -> acc
        }
    }
    return zipWith(list1, list2, RecList(), f)
}

fun <A, B, C> product(
    list1: RecList<A>,
    list2: RecList<B>,
    f: (A) -> (B) -> C
): RecList<C> {
    return list1.flatMap { a -> list2.map { b -> f(a)(b) } }
}

fun <A, B> unzip(list: RecList<Pair<A, B>>): Pair<RecList<A>, RecList<B>> {
    return list.unzip { it }
}
