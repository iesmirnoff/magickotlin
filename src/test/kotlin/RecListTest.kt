import org.testng.Assert.assertEquals
import org.testng.annotations.Test


class RecListTest {

    @Test
    fun test_toString() {
        val list = Cons(1, 2, 4)
        assertEquals("[1, 2, 4, NIL]", list.toString())
    }

    @Test
    fun test_setHead() {
        val list = Cons(1, 2, 4)
        assertEquals("[5, 2, 4, NIL]", list.setHead(5).toString())
    }

    @Test
    fun test_dropAtMost() {
        val list = Cons(1, 2, 4)
        assertEquals("[2, 4, NIL]", list.dropAtMost(1).toString())
    }

    @Test
    fun test_dropWhile() {
        val list = Cons(1, 2, 3, 4)
        assertEquals("[4, NIL]", list.dropWhile { it < 4 }.toString())
    }

    @Test
    fun test_concat() {
        val list1 = Cons(1, 2)
        val list2 = Cons(3, 4)
        assertEquals("[1, 2, 3, 4, NIL]", list1.concat(list2).toString())
    }

    @Test
    fun test_dropLast() {
        val list = Cons(1, 2, 3, 4)
        assertEquals("[1, 2, 3, NIL]", list.dropLast().toString())
    }

    @Test
    fun test_reverse() {
        val list = Cons(1, 2, 3)
        assertEquals("[3, 2, 1, NIL]", list.reverse().toString())
    }

    @Test
    fun test_concat_by_foldRight() {
        val list1 = Cons(1, 2)
        val list2 = Cons(3, 4)
        val list = list1.foldRight(list2) { elem -> { list -> list.cons(elem) } }
        assertEquals("[1, 2, 3, 4, NIL]", list.toString())
    }

    @Test
    fun test_len() {
        val list = Cons(1, 2, 3)
        assertEquals(3, list.length())
    }

    @Test
    fun test_sum() {
        val list = Cons(1, 2, 3)
        assertEquals(6, list.sum())
    }

    @Test
    fun test_product() {
        val list = Cons(1.0, 2.0, 3.0)
        assertEquals(6.0, list.product())
    }

    @Test
    fun test_flatten() {
        val row1 = Cons(1, 2, 3)
        val row2 = Cons(4, 5)
        val row3 = Cons(6, 7, 8)
        val column = Cons(row1, row2, row3)
        assertEquals("[1, 2, 3, 4, 5, 6, 7, 8, NIL]", RecList.flatten(column).toString())
    }

    @Test
    fun test_map() {
        val list = Cons(1, 2)
        assertEquals("[+1, +2, NIL]", list.map { "+$it" }.toString())
    }

    @Test
    fun test_filter() {
        val list = Cons(4, 2, 5)
        assertEquals("[4, 5, NIL]", list.filter { it > 3 }.toString())
    }

    @Test
    fun test_flatMap() {
        val list = Cons(1, 2, 3)
        assertEquals("[1, -1, 2, -2, 3, -3, NIL]", list.flatMap { Cons(it, -it) }.toString())
    }

    @Test
    fun test_lenMemoized() {
        val list = Cons(1, 2, 3)
        assertEquals(list.length, list.length())
        assertEquals(list.length(), 3)
    }
}