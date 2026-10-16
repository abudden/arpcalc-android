package uk.co.cgtk.karpcalc

import org.junit.Assert.*
import org.junit.Test

/*
 * The keypad layout (and the classic layout option), and the bit shifts.
 * The same tests are in the desktop version's tests/unit_tests.cpp.
 */
public class LayoutTest {
	fun names(row: List<BI>) = row.map { it.name }

	@Test
	fun newLayout() {
		val ch = CommandHandler()
		assertEquals(listOf("base", "exponent", "power", "squareroot", "reciprocal", "divide"),
				names(ch.getGrid("numpad")[1]))
		ch.selectBase(DisplayBase.baseHexadecimal)
		assertEquals(listOf("base", "NOP", "A", "B", "C", "divide"), names(ch.getGrid("numpad")[1]))
		ch.selectBase(DisplayBase.baseDecimal)
		for (pad in listOf("funcpad", "altfuncpad")) {
			val grid = ch.getGrid(pad)
			assertEquals(listOf("cuberoot", "xrooty", "square"), names(grid[2]).take(3))
			assertEquals(listOf("shiftleft", "shiftright", "cube"), names(grid[3]).take(3))
		}
		/* RAND is at the end of the fifth row of constants, in both layouts */
		for (classic in listOf(false, true)) {
			ch.setOption(DispOpt.ClassicLayout, classic)
			val constants = ch.getGrid("constpad")
			assertEquals("random", constants[4][5].name)
			assertEquals(1, constants.flatten().count { it.name == "random" })
		}
	}

	@Test
	fun classicLayout() {
		val ch = CommandHandler()
		ch.setOption(DispOpt.ClassicLayout, true)
		assertEquals(listOf("base", "random", "exponent", "squareroot", "reciprocal", "divide"),
				names(ch.getGrid("numpad")[1]))
		for (pad in listOf("funcpad", "altfuncpad")) {
			val grid = ch.getGrid(pad)
			assertEquals(listOf("squareroot", "cuberoot", "xrooty"), names(grid[2]).take(3))
			assertEquals(listOf("square", "cube", "power"), names(grid[3]).take(3))
		}
		assertTrue(ch.getOptions().containsKey("Classic Keypad Layout"))
	}

	fun shift(command: String, x: Long, y: Long, bits: BitCount = BitCount.bc32): Long {
		val ch = CommandHandler()
		ch.setBitCount(bits)
		ch.st.push(A(y.toInt()))
		ch.st.push(x.toAF())
		assertEquals(ErrorCode.NoError, ch.keypress(command))
		return ch.st.pop().toLong()
	}

	@Test
	fun shifts() {
		assertEquals(0x84CAL, shift("shiftleft", 0x4265, 1))
		assertEquals(0x2132L, shift("shiftright", 0x4265, 1))
		assertEquals(0x42650L, shift("shiftleft", 0x4265, 4))
		assertEquals(0x4265L, shift("shiftleft", 0x4265, 0))
		/* Bits shifted beyond the bit count are lost */
		assertEquals(0x2L, shift("shiftleft", 0x80000001L, 1))
		assertEquals(0xCAL, shift("shiftleft", 0x4265, 1, BitCount.bc8))
		assertEquals(0L, shift("shiftleft", 0x4265, 32))
		assertEquals(0L, shift("shiftleft", 0x4265, 100))
		assertEquals(0L, shift("shiftright", 0x4265, 100))
		/* A logical shift: no sign extension */
		assertEquals(0x7FFFFFFFL, shift("shiftright", 0xFFFFFFFFL, 1))
		/* Negative shifts go the other way */
		assertEquals(0x2132L, shift("shiftleft", 0x4265, -1))
		assertEquals(0x84CAL, shift("shiftright", 0x4265, -1))

		/* As typed in hex */
		val ch = CommandHandler()
		ch.selectBase(DisplayBase.baseHexadecimal)
		for (k in listOf("1", "enter", "4", "2", "6", "5", "shiftleft")) {
			ch.keypress(k)
		}
		assertEquals("0x84CA", ch.getXDisplay())
	}
}
