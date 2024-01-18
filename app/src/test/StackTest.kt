package uk.co.cgtk.karpcalc

import org.junit.Assert.*
import org.junit.Test

fun assertEquals(v1: AF, v2: Double, DELTA: Double) {
	assertEquals(v1.toDouble(), v2, DELTA)
}
fun assertEquals(v1: Double, v2: AF, DELTA: Double) {
	assertEquals(v1, v2.toDouble(), DELTA)
}

public class StackTest {

	private val ALLOWED_DELTA = 1e-9

	fun check(a: AF, b: Double) : Boolean {
		var diff: AF = a-b
		diff = diff.abs()
		if (diff < 1e-9) {
			return true
		}
		else {
			return false
		}
	}

	fun check(a: MutableList<AF>, b: MutableList<Double>): Boolean {
		var result: Boolean = true
		if (a.size != b.size) {
			result = false
		}
		else {
			for (index in a.indices) {
				if ( ! check(a[index], b[index])) {
					result = false
				}
			}
		}
		if ( ! result) {
			print("Lists differ: (${a.toString()}) vs (${b.toString()})")
		}
		return result
	}

	@Test
	fun stackTests() {
		var nrset = Stack(mutableSetOf(CalcOpt.SaveHistory))

		assertTrue(nrset.stack.size == 0)
		assertTrue( ! nrset.options.contains(CalcOpt.ReplicateStack))
		assertTrue(nrset.options.contains(CalcOpt.SaveHistory))
		nrset.push(1.0)
		nrset.push(2.0)
		nrset.push(3.0)
		nrset.push(4.0)
		nrset.push(5.0)
		assertEquals(nrset.peek(), 5.0, ALLOWED_DELTA)
		assertEquals(nrset.peekAt(0), 5.0, ALLOWED_DELTA)
		assertEquals(nrset.peekAt(1), 4.0, ALLOWED_DELTA)
		assertEquals(nrset.peekAt(2), 3.0, ALLOWED_DELTA)
		assertEquals(nrset.peekAt(3), 2.0, ALLOWED_DELTA)
		assertEquals(nrset.peekAt(4), 1.0, ALLOWED_DELTA)
		assertTrue(nrset.stack.size == 5)
		assertEquals(nrset.pop(), 5.0, ALLOWED_DELTA)
		assertTrue(nrset.stack.size == 4)
		assertEquals(nrset.peekAt(0), 4.0, ALLOWED_DELTA)
		assertEquals(nrset.peekAt(1), 3.0, ALLOWED_DELTA)
		assertEquals(nrset.peekAt(2), 2.0, ALLOWED_DELTA)
		assertEquals(nrset.peekAt(3), 1.0, ALLOWED_DELTA)
		assertEquals(nrset.peekAt(4), 0.0, ALLOWED_DELTA)
		assertEquals(nrset.pop(), 4.0, ALLOWED_DELTA)
		assertTrue(nrset.stack.size == 3)
		assertEquals(nrset.pop(), 3.0, ALLOWED_DELTA)
		assertTrue(nrset.stack.size == 2)
		assertEquals(nrset.pop(), 2.0, ALLOWED_DELTA)
		assertTrue(nrset.stack.size == 1)
		assertEquals(nrset.pop(), 1.0, ALLOWED_DELTA)
		assertTrue(nrset.stack.size == 0)
		assertEquals(nrset.pop(), 0.0, ALLOWED_DELTA)

		var ps = mutableListOf<Double>(1.0, 2.0, 3.0, 4.0, 5.0)
		nrset.push(ps)
		assertTrue(nrset.stack.size == 5)
		assertEquals(nrset.peek(), 5.0, ALLOWED_DELTA)
		assertEquals(nrset.peekAt(4), 1.0, ALLOWED_DELTA)
		nrset.rollUp()
		assertTrue(check(nrset.stack, mutableListOf<Double>(5.0, 1.0, 2.0, 3.0, 4.0)))
		nrset.rollDown()
		assertTrue(check(nrset.stack, mutableListOf<Double>(1.0, 2.0, 3.0, 4.0, 5.0)))
		/* The following tests no longer work as the functionality of deciding when to 
		 * save history has been moved into the Command layer.
		nrset.undo()
		assertTrue(check(nrset.stack, mutableListOf<Double>(5.0, 1.0, 2.0, 3.0, 4.0)))
		nrset.undo()
		assertTrue(check(nrset.stack, mutableListOf<Double>(1.0, 2.0, 3.0, 4.0, 5.0)))
		nrset.undo()
		assertTrue(check(nrset.stack, mutableListOf<Double>()))
		nrset.undo()
		nrset.undo()
		assertTrue(check(nrset.stack, mutableListOf<Double>(1.0)))
		*/

		var rst = Stack(mutableSetOf(CalcOpt.SaveHistory, CalcOpt.ReplicateStack))

		assertTrue(rst.stack.size == 0)
		assertTrue(rst.options.contains(CalcOpt.ReplicateStack))
		rst.push(1.0)
		rst.push(2.0)
		rst.push(3.0)
		rst.push(4.0)
		rst.push(5.0)
		assertEquals(rst.peek(), 5.0, ALLOWED_DELTA)
		assertEquals(rst.peekAt(0), 5.0, ALLOWED_DELTA)
		assertEquals(rst.peekAt(1), 4.0, ALLOWED_DELTA)
		assertEquals(rst.peekAt(2), 3.0, ALLOWED_DELTA)
		assertEquals(rst.peekAt(3), 2.0, ALLOWED_DELTA)
		assertEquals(rst.peekAt(4), 0.0, ALLOWED_DELTA)
		assertTrue(rst.stack.size == 4)
		assertEquals(rst.pop(), 5.0, ALLOWED_DELTA)
		assertTrue(rst.stack.size == 4)
		assertEquals(rst.peekAt(0), 4.0, ALLOWED_DELTA)
		assertEquals(rst.peekAt(1), 3.0, ALLOWED_DELTA)
		assertEquals(rst.peekAt(2), 2.0, ALLOWED_DELTA)
		assertEquals(rst.peekAt(3), 2.0, ALLOWED_DELTA)
		assertEquals(rst.peekAt(4), 0.0, ALLOWED_DELTA)
		assertEquals(rst.pop(), 4.0, ALLOWED_DELTA)
		assertTrue(rst.stack.size == 4)
		assertEquals(rst.pop(), 3.0, ALLOWED_DELTA)
		assertTrue(rst.stack.size == 4)
		assertEquals(rst.pop(), 2.0, ALLOWED_DELTA)
		assertTrue(rst.stack.size == 4)
		assertEquals(rst.pop(), 2.0, ALLOWED_DELTA)
		assertTrue(rst.stack.size == 4)
		assertEquals(rst.pop(), 2.0, ALLOWED_DELTA)

		rst.clear()
		assertTrue(rst.stack.size == 0)

		var rps = mutableListOf<Double>(1.0, 2.0, 3.0, 4.0, 5.0)
		rst.push(rps)
		assertTrue(rst.stack.size == 4)
		assertEquals(rst.peek(), 5.0, ALLOWED_DELTA)
		assertEquals(rst.peekAt(3), 2.0, ALLOWED_DELTA)
		assertTrue(check(rst.stack, mutableListOf<Double>(2.0, 3.0, 4.0, 5.0)))
		rst.rollUp()
		assertTrue(check(rst.stack, mutableListOf<Double>(5.0, 2.0, 3.0, 4.0)))
		rst.rollDown()
		assertTrue(check(rst.stack, mutableListOf<Double>(2.0, 3.0, 4.0, 5.0)))
		/* The following tests no longer work as the functionality of deciding when to 
		 * save history has been moved into the Command layer.
		rst.undo()
		assertTrue(check(rst.stack, mutableListOf<Double>(5.0, 2.0, 3.0, 4.0)))
		rst.undo()
		assertTrue(check(rst.stack, mutableListOf<Double>(2.0, 3.0, 4.0, 5.0)))
		rst.undo()
		assertTrue(check(rst.stack, mutableListOf<Double>()))
		rst.undo()
		assertTrue(check(rst.stack, mutableListOf<Double>(2.0, 2.0, 2.0, 2.0)))
		rst.undo()
		assertTrue(check(rst.stack, mutableListOf<Double>(2.0, 2.0, 2.0, 2.0)))
		rst.undo()
		assertTrue(check(rst.stack, mutableListOf<Double>(2.0, 2.0, 2.0, 2.0)))
		rst.undo()
		assertTrue(check(rst.stack, mutableListOf<Double>(2.0, 2.0, 2.0, 2.0)))
		rst.undo()
		assertTrue(check(rst.stack, mutableListOf<Double>(2.0, 2.0, 2.0, 3.0)))
		*/
	}
}
