sealed class RecursiveList<A> {
    abstract fun isEmpty(): Boolean

    fun setHead(head: A): RecursiveList<A> {
        return when (this) {
            is Nil -> throw IllegalStateException("setHead called on an empty list")
            is Cons -> tail.cons(head)
        }
    }

    fun cons(head: A): RecursiveList<A> {
        return Cons(head, this)
    }

    fun dropAtMost(n: Int): RecursiveList<A> {
        tailrec fun dropAtMost(n: Int, list: RecursiveList<A>): RecursiveList<A> {
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

    fun dropWhile(p: (A) -> Boolean): RecursiveList<A> {
        tailrec fun dropWhile(p: (A) -> Boolean, list: RecursiveList<A>): RecursiveList<A> {
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

    fun <B> map(f: (A) -> B): RecursiveList<B> {
        return foldLeft(Nil as RecursiveList<B>) { acc: RecursiveList<B> ->
            { h: A -> Cons(f(h), acc) }
        }.reverse()
    }

    fun filter(p: (A) -> Boolean): RecursiveList<A> {
        return foldLeft(Nil as RecursiveList<A>) { acc -> { a -> if (p(a)) acc.cons(a) else acc } }.reverse()
    }

    fun <B> flatMap(f: (A) -> RecursiveList<B>): RecursiveList<B> {
        return flatten(map(f))
    }

    fun concat(list: RecursiveList<A>): RecursiveList<A> {
        return concat(this, list)
    }

    abstract fun dropLast(): RecursiveList<A>

    fun reverse(): RecursiveList<A> {
        return reverse(this)
    }

    fun <B> foldRight(identity: B, f: (A) -> (B) -> B): B {
        return foldRight(this, identity, f)
    }

    fun <B> foldLeft(identity: B, f: (B) -> (A) -> B): B {
        return foldLeft(this, identity, f)
    }

    fun len(): Int {
        return foldLeft(0) { { _ -> it + 1 } }
    }

    companion object {

        fun <A> flatten(table: RecursiveList<RecursiveList<A>>): RecursiveList<A> {
            return table.foldLeft(Cons()) { acc -> { head -> acc.concat(head) } }
        }

        fun <A> concat(list1: RecursiveList<A>, list2: RecursiveList<A>): RecursiveList<A> {
            return when (list1) {
                is Nil -> list2
                is Cons -> concat(list1.dropAtMost(1), list2).cons(list1.head)
            }
        }

        fun <A, B> foldRight(list: RecursiveList<A>, acc: B, f: (A) -> (B) -> B): B {
            return when (val reversed = list.reverse()) {
                is Nil -> acc
                is Cons -> foldLeft(
                    reversed.tail,
                    f(reversed.head)(acc)
                ) { b -> { a -> f(a)(b) } } // f(list.head)(foldRight(list.tail, acc, f))
            }
        }

        tailrec fun <A, B> foldLeft(list: RecursiveList<A>, acc: B, f: (B) -> (A) -> B): B {
            return when (list) {
                is Nil -> acc
                is Cons -> foldLeft(list.tail, f(acc)(list.head), f)
            }
        }

        fun <A> reverse(list: RecursiveList<A>): RecursiveList<A> {
            return foldLeft(list, Cons.invoke()) { tail -> { head -> tail.cons(head) } }
        }

        operator fun <A> invoke(vararg a: A): RecursiveList<A> {
            return if (a.isEmpty()) {
                Nil as RecursiveList<A>
            } else {
                Cons(*a)
            }
        }
    }
}

internal class Cons<A>(internal val head: A, internal val tail: RecursiveList<A>) : RecursiveList<A>() {
    override fun isEmpty(): Boolean = false

    override fun toString(): String {
        return "[${toString("", this)}NIL]"
    }

    private tailrec fun toString(acc: String, list: RecursiveList<A>): String {
        return when (list) {
            is Nil -> acc
            is Cons -> toString("$acc${list.head}, ", list.tail)
        }
    }

    override fun dropLast(): RecursiveList<A> {
        return reverse().dropAtMost(1).reverse()
    }

    companion object {
        operator fun <A> invoke(vararg az: A): RecursiveList<A> {
            return az.foldRight(Nil as RecursiveList<A>) { a, list -> Cons(a, list) }
        }

        tailrec fun <A> reverse(acc: RecursiveList<A>, list: RecursiveList<A>): RecursiveList<A> {
            return when (list) {
                is Nil -> acc
                is Cons -> reverse(acc.cons(list.head), list.tail)
            }
        }
    }
}

private data object Nil : RecursiveList<Nothing>() {

    override fun dropLast(): RecursiveList<Nothing> = throw IllegalStateException()

    override fun isEmpty(): Boolean = true

    override fun toString(): String = "[NIL]"
}

fun RecursiveList<Int>.sum(): Long {
    return RecursiveList.foldLeft(this, 0) { acc -> { head -> acc + head } }
}

fun RecursiveList<Double>.product(): Double {
    return RecursiveList.foldLeft(this, 1.0) { acc -> { head -> acc * head } }
}