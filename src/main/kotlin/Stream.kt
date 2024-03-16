sealed class Stream<out A> {

    abstract fun isEmpty(): Boolean

    abstract fun head(): Result<A>

    abstract fun tail(): Result<Stream<A>>

    abstract fun takeAtMost(n: Int): Stream<A>

    abstract fun dropAtMost(n: Int): Stream<A>

    abstract fun takeWhile(p: (A) -> Boolean): Stream<A>

    abstract fun dropWhile(p: (A) -> Boolean): Stream<A>

    abstract fun exists(p: (A) -> Boolean): Boolean

    abstract fun <B> foldRight(z: Lazy<B>, f: (A) -> (Lazy<B>) -> B): B

    abstract fun takeWhileViaFoldRight(p: (A) -> Boolean): Stream<A>

    fun toList(): RecList<@UnsafeVariance A> {
        return toList(this)
    }

    fun headSafe(): Result<A> {
        return foldRight(Lazy { Result() }) { a -> { Result(a) } }
    }

    fun <B> map(f: (A) -> B): Stream<B> {
        return foldRight(Lazy { invoke() }) { a ->
            { acc ->
                cons(Lazy { f(a) }, acc)
            }
        }
    }

    fun filter(p: (A) -> Boolean): Stream<A> {
        return foldRight(Lazy { invoke() }) { a ->
            { foldedTail ->
                if (p(a)) {
                    cons(Lazy { a }, foldedTail)
                } else {
                    foldedTail()
                }
            }
        }
    }

    fun append(stream2: Lazy<Stream<@UnsafeVariance A>>): Stream<A> {
        return foldRight(stream2) { a: A ->
            { b: Lazy<Stream<A>> ->
                cons(Lazy { a }, b)
            }
        }
    }

    fun <B> flatMap(f: (A) -> Stream<B>): Stream<B> {
        return foldRight(Lazy { invoke() }) { head ->
            {
                f(head).append(it)
            }
        }
    }

    fun find(p: (A) -> Boolean): Result<A> {
        return filter(p).headSafe()
    }

    private data object Empty : Stream<Nothing>() {
        override fun isEmpty(): Boolean {
            return true
        }

        override fun head(): Result<Nothing> {
            return Result()
        }

        override fun tail(): Result<Stream<Nothing>> {
            return Result()
        }

        override fun takeAtMost(n: Int): Stream<Nothing> {
            return this
        }

        override fun dropAtMost(n: Int): Stream<Nothing> {
            return this
        }

        override fun takeWhileViaFoldRight(p: (Nothing) -> Boolean): Stream<Nothing> {
            return this
        }

        override fun <B> foldRight(z: Lazy<B>, f: (Nothing) -> (Lazy<B>) -> B): B {
            return z()
        }

        override fun exists(p: (Nothing) -> Boolean): Boolean {
            return false
        }

        override fun dropWhile(p: (Nothing) -> Boolean): Stream<Nothing> {
            return this
        }

        override fun takeWhile(p: (Nothing) -> Boolean): Stream<Nothing> {
            return this
        }
    }

    private class Cons<out A>(
        val hd: Lazy<A>,
        val tl: Lazy<Stream<A>>
    ) : Stream<A>() {
        override fun isEmpty(): Boolean {
            return false
        }

        override fun head(): Result<A> {
            return Result(hd())
        }

        override fun tail(): Result<Stream<A>> {
            return Result(tl())
        }

        override fun takeAtMost(n: Int): Stream<A> {
            if (n == 0) {
                return Empty
            }
            return cons(hd, Lazy { tl().takeAtMost(n - 1) })
        }

        override fun dropAtMost(n: Int): Stream<A> {
            return dropAtMost(n, this)
        }

        override fun takeWhileViaFoldRight(p: (A) -> Boolean): Stream<A> {
            return foldRight(Lazy { invoke() }) { head ->
                { acc ->
                    if (p(head)) {
                        cons(Lazy { head }, acc)
                    } else {
                        Empty
                    }
                }
            }
        }

        override fun <B> foldRight(z: Lazy<B>, f: (A) -> (Lazy<B>) -> B): B {
            return f(hd())(Lazy { tl().foldRight(z, f) })
        }

        override fun exists(p: (A) -> Boolean): Boolean {
            return exists(p, this)
        }

        override fun dropWhile(p: (A) -> Boolean): Stream<A> {
            return dropWhile(p, this)
        }

        override fun takeWhile(p: (A) -> Boolean): Stream<A> {
            if (p(hd())) {
                return cons(hd, Lazy { tl().takeWhile(p) })
            }
            return Empty
        }
    }

    companion object {

        fun <A> foldRight(
            acc: Lazy<@UnsafeVariance A>,
            stream: Stream<@UnsafeVariance A>,
            p: (A) -> Boolean,
            f: (Lazy<@UnsafeVariance A>) -> (Lazy<@UnsafeVariance A>) -> Lazy<@UnsafeVariance A>
        ): Lazy<A> {
            return when (stream) {
                is Empty -> acc
                is Cons -> if (p(stream.hd())) {
                    acc
                } else {
                    f(stream.hd)(foldRight(acc, stream.tl(), p, f))
                }
            }
        }

        fun <A> cons(hd: Lazy<A>, tl: Lazy<Stream<A>>): Stream<A> {
            return Cons(hd, tl)
        }

        operator fun <A> invoke(): Stream<A> = Empty

        fun from(i: Int): Stream<Int> = iterate(i) { it + 1 }

        fun <A> repeat(f: () -> A): Stream<A> {
            return cons(Lazy { f() }, Lazy { repeat(f) })
        }

        tailrec fun <A> dropAtMost(n: Int, stream: Stream<A>): Stream<A> =
            when {
                n > 0 -> when (stream) {
                    is Empty -> stream
                    is Cons -> dropAtMost(n - 1, stream.tl())
                }

                else -> stream
            }

        fun <A> toList(stream: Stream<A>): RecList<A> {
            tailrec fun <A> toList(list: RecList<A>, stream: Stream<A>): RecList<A> =
                when (stream) {
                    Empty -> list
                    is Cons -> toList(list.cons(stream.hd()), stream.tl())
                }
            return toList(RecList(), stream).reverse()
        }

        fun <A> iterate(seed: A, f: (A) -> A): Stream<A> {
            return cons(Lazy { seed }, Lazy { iterate(f(seed), f) })
        }

        tailrec fun <A> dropWhile(p: (A) -> Boolean, stream: Stream<A>): Stream<A> {
            return when (stream) {
                is Empty -> stream
                is Cons -> if (p(stream.hd())) {
                    dropWhile(p, stream.tl())
                } else {
                    stream
                }
            }
        }

        tailrec fun <A> exists(p: (A) -> Boolean, stream: Stream<A>): Boolean {
            return when (stream) {
                is Empty -> false
                is Cons -> if (p(stream.hd())) {
                    return true
                } else {
                    return exists(p, stream.tl())
                }
            }
        }

        tailrec fun <A> find(p: (A) -> Boolean, stream: Stream<A>): Result<A> {
            return when (stream) {
                is Empty -> Result()
                is Cons -> if (p(stream.hd())) {
                    return Result(stream.hd())
                } else {
                    return find(p, stream.tl())
                }
            }
        }
    }
}