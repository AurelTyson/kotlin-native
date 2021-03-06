import kotlin.test.*

fun <T> compare(expected: T, actual: T, block: CompareContext<T>.() -> Unit) {
    CompareContext(expected, actual).block()
}

class CompareContext<out T>(public val expected: T, public val actual: T) {

    public fun equals(message: String = "") {
        assertEquals(expected, actual, message)
    }

    public fun <P> propertyEquals(message: String = "", getter: T.() -> P) {
        assertEquals(expected.getter(), actual.getter(), message)
    }

    public fun propertyFails(getter: T.() -> Unit) {
        assertFailEquals({ expected.getter() }, { actual.getter() })
    }

    public fun <P> compareProperty(getter: T.() -> P, block: CompareContext<P>.() -> Unit) {
        compare(expected.getter(), actual.getter(), block)
    }

    private fun assertFailEquals(expected: () -> Unit, actual: () -> Unit) {
        val expectedFail = assertFails(expected)
        val actualFail = assertFails(actual)
        //assertEquals(expectedFail != null, actualFail != null)
        assertTypeEquals(expectedFail, actualFail)
    }
}

fun <T> CompareContext<Iterator<T>>.iteratorBehavior() {
    propertyEquals { hasNext() }

    while (expected.hasNext()) {
        propertyEquals { next() }
        propertyEquals { hasNext() }
    }
    propertyFails { next() }
}

public fun <N : Any> doTest(
        sequence: Iterable<N>,
        expectedFirst: N,
        expectedLast: N,
        expectedIncrement: Number,
        expectedElements: List<N>
) {
    val first: Any
    val last: Any
    val increment: Number
    when (sequence) {
        is IntProgression -> {
            first = sequence.first
            last = sequence.last
            increment = sequence.step
        }
        is LongProgression -> {
            first = sequence.first
            last = sequence.last
            increment = sequence.step
        }
        is CharProgression -> {
            first = sequence.first
            last = sequence.last
            increment = sequence.step
        }
        else -> throw IllegalArgumentException("Unsupported sequence type: $sequence")
    }

    assertEquals(expectedFirst, first)
    assertEquals(expectedLast, last)
    assertEquals(expectedIncrement, increment)

    if (expectedElements.isEmpty())
        assertTrue(sequence.none())
    else
        assertEquals(expectedElements, sequence.toList())

    compare(expectedElements.iterator(), sequence.iterator()) {
        iteratorBehavior()
    }
}

fun box() {
    doTest(3..9, 3, 9, 1, listOf(3, 4, 5, 6, 7, 8, 9))
    doTest(3.toByte()..9.toByte(), 3, 9, 1, listOf(3, 4, 5, 6, 7, 8, 9))
    doTest(3.toShort()..9.toShort(), 3, 9, 1, listOf(3, 4, 5, 6, 7, 8, 9))
    doTest(3.toLong()..9.toLong(), 3.toLong(), 9.toLong(), 1.toLong(), listOf<Long>(3, 4, 5, 6, 7, 8, 9))

    doTest('c'..'g', 'c', 'g', 1, listOf('c', 'd', 'e', 'f', 'g'))
}


fun simpleRangeWithNonConstantEnds() {
    doTest((1 + 2)..(10 - 1), 3, 9, 1, listOf(3, 4, 5, 6, 7, 8, 9))
    doTest((1.toByte() + 2.toByte()).toByte()..(10.toByte() - 1.toByte()).toByte(), 3, 9, 1, listOf(3, 4, 5, 6, 7, 8, 9))
    doTest((1.toShort() + 2.toShort()).toShort()..(10.toShort() - 1.toShort()).toShort(), 3, 9, 1, listOf(3, 4, 5, 6, 7, 8, 9))
    doTest((1.toLong() + 2.toLong())..(10.toLong() - 1.toLong()), 3.toLong(), 9.toLong(), 1.toLong(), listOf<Long>(3, 4, 5, 6, 7, 8, 9))

    doTest(("ace"[1])..("age"[1]), 'c', 'g', 1, listOf('c', 'd', 'e', 'f', 'g'))
}
