package uk.co.cgtk.karpcalc

import org.junit.Assert.*
import org.junit.Test
import java.util.Random

/*
 * Arithmetic in hours/degrees, minutes and seconds (H.MMSS / D.MMSS) and
 * degrees and minutes (D.MM), as it's done on the calculator: type a value,
 * convert it to hours (or degrees), type another and convert that, add or
 * subtract, then convert back.
 *
 * The minutes and seconds typed in can be 60 or more (12.8622 is 12 hours,
 * 86 minutes and 22 seconds, i.e. 13:26:22), and the seconds (or for D.MM
 * the minutes) can have two decimal places.  The expected answer is worked
 * out separately, in whole hundredths of a second (or of a minute), and
 * both the result's value and how it's displayed are checked against it.
 *
 * The same tests are in the desktop version's tests/hms_tests.cpp.  Set
 * HMS_CASES to run more random cases (default 2000 for each kind).
 */
public class HmsTest {
	/* One of the formats: the value is a whole number of units (hundredths
	 * of a second or of a minute), written as whole.MMSScc or whole.MMcc
	 */
	class Kind(val name: String, val toDecimal: String, val fromDecimal: String,
			val fields: List<Int>) {
		/* fields: how many of the previous field make one of this one, from
		 * the whole number downwards, e.g. [60, 60, 100] for H.MMSScc
		 */
		val perWhole = fields.fold(1L) { a, b -> a * b }

		/* What would be typed in for these fields (whole, then each field
		 * as two digits, with trailing zeros left off as a person would)
		 */
		fun typed(whole: Long, parts: List<Int>): String {
			val fraction = parts.joinToString("") { "%02d".format(it) }.trimEnd('0')
			return if (fraction.isEmpty()) "$whole" else "$whole.$fraction"
		}

		fun units(whole: Long, parts: List<Int>): Long {
			var result = whole
			for ((i, p) in parts.withIndex()) {
				result = result * fields[i] + p
			}
			return result
		}

		/* The expected result: the units written out with each field in
		 * range (minutes and seconds below 60)
		 */
		fun expected(units: Long): String {
			val sign = if (units < 0) "-" else ""
			var rest = Math.abs(units)
			val parts = mutableListOf<Int>()
			for (f in fields.reversed()) {
				parts.add(0, (rest % f).toInt())
				rest /= f
			}
			return sign + typed(rest, parts)
		}
	}

	val kinds = listOf(
		Kind("H.MS", "Convert_Time_Hours.Minutes-Seconds_Hours",
			"Convert_Time_Hours_Hours.Minutes-Seconds", listOf(60, 60, 100)),
		Kind("D.MS", "Convert_Angle_Degrees.Minutes-Seconds_Degrees",
			"Convert_Angle_Degrees_Degrees.Minutes-Seconds", listOf(60, 60, 100)),
		Kind("D.M", "Convert_Angle_Degrees.Minutes_Degrees",
			"Convert_Angle_Degrees_Degrees.Minutes", listOf(60, 100)))

	fun type(ch: CommandHandler, text: String) {
		for (c in text) {
			assertEquals(ErrorCode.NoError, ch.keypress(c.toString()))
		}
	}

	/* Returns a description of what went wrong, or null if it worked */
	fun check(kind: Kind, a: String, op: String?, b: String?, expected: String): String? {
		val ch = CommandHandler()
		val keys = mutableListOf<String>()
		fun press(key: String) {
			keys.add(key)
			val ec = ch.keypress(key)
			if (ec != ErrorCode.NoError) {
				throw AssertionError("$key gave $ec")
			}
		}
		try {
			type(ch, a)
			press(kind.toDecimal)
			if (op != null) {
				type(ch, b!!)
				press(kind.toDecimal)
				press(op)
			}
			press(kind.fromDecimal)
		}
		catch (e: AssertionError) {
			return "$a $op $b: ${e.message}"
		}

		val actual = ch.st.peek()
		val display = ch.getXDisplay()

		/* How the expected value is displayed (with the same options) */
		val reference = CommandHandler()
		reference.st.push(A(expected))
		val expectedDisplay = reference.getXDisplay()

		val what = if (op == null) a else "$a $op $b"
		if ((actual - A(expected)).abs() > A("1e-9")) {
			return "${kind.name} $what: expected $expected, got $actual"
		}
		if (display != expectedDisplay) {
			return "${kind.name} $what: expected display $expectedDisplay, got $display (value $actual)"
		}
		return null
	}

	fun run(cases: List<Triple<Kind, List<Pair<Long, List<Int>>>, String?>>) {
		val failures = mutableListOf<String>()
		for ((kind, unpadded, op) in cases) {
			/* Any fields left off (e.g. the hundredths) are zero */
			val values = unpadded.map { (w, p) -> Pair(w, p + List(kind.fields.size - p.size) { 0 }) }
			val (wa, pa) = values[0]
			val ua = kind.units(wa, pa)
			val a = kind.typed(wa, pa)
			val failure = if (op == null) {
				check(kind, a, null, null, kind.expected(ua))
			}
			else {
				val (wb, pb) = values[1]
				val ub = kind.units(wb, pb)
				val result = if (op == "plus") ua + ub else ua - ub
				check(kind, a, op, kind.typed(wb, pb), kind.expected(result))
			}
			if (failure != null) {
				failures.add(failure)
			}
		}
		if (failures.isNotEmpty()) {
			fail("${failures.size} of ${cases.size} failed, e.g.\n" +
					failures.take(30).joinToString("\n"))
		}
	}

	fun v(whole: Long, vararg parts: Int) = Pair(whole, parts.toList())

	@Test
	fun handPicked() {
		val hms = kinds[0]
		val dms = kinds[1]
		val dm = kinds[2]
		val cases = mutableListOf<Triple<Kind, List<Pair<Long, List<Int>>>, String?>>()
		for (k in listOf(hms, dms)) {
			/* Out of range minutes and seconds, just converted and back */
			for (x in listOf(v(12, 86, 22), v(0, 0, 60), v(0, 60, 0), v(0, 59, 60),
					v(23, 59, 59), v(0, 99, 99), v(1, 15), v(19, 15), v(2, 15),
					v(0, 0, 1), v(0, 0, 0, 1), v(10, 10, 10, 10), v(0, 59, 59, 99),
					v(359, 59, 59, 99), v(999, 99, 99, 99))) {
				cases.add(Triple(k, listOf(x), null))
			}
			for (op in listOf("plus", "minus")) {
				for ((x, y) in listOf(
						Pair(v(12, 86, 22), v(1, 33, 38)),
						Pair(v(23, 59, 59), v(0, 0, 1)),
						Pair(v(0, 0, 1), v(0, 0, 2)),
						Pair(v(0, 30), v(1, 0)),
						Pair(v(10, 0), v(9, 59, 59, 99)),
						Pair(v(1, 15), v(2, 15)),
						Pair(v(19, 15), v(8, 30, 56)),
						Pair(v(0, 0, 59, 99), v(0, 0, 0, 1)),
						Pair(v(5), v(5)),
						Pair(v(12, 34, 56, 78), v(98, 76, 54, 32)))) {
					cases.add(Triple(k, listOf(x, y), op))
				}
			}
		}
		for (x in listOf(v(51, 30, 45), v(0, 99, 99), v(12, 60), v(0, 59, 99), v(1, 15))) {
			cases.add(Triple(dm, listOf(x), null))
		}
		for (op in listOf("plus", "minus")) {
			for ((x, y) in listOf(Pair(v(51, 30, 45), v(0, 29, 55)), Pair(v(0, 0, 1), v(0, 0, 2)),
					Pair(v(10, 0), v(9, 59, 99)))) {
				cases.add(Triple(dm, listOf(x, y), op))
			}
		}
		run(cases)
	}

	@Test
	fun random() {
		val count = System.getenv("HMS_CASES")?.toIntOrNull() ?: 2000
		val rng = Random(1)
		val cases = mutableListOf<Triple<Kind, List<Pair<Long, List<Int>>>, String?>>()
		/* Mostly small numbers of hours, with some large ones */
		fun whole() = if (rng.nextInt(4) == 0) rng.nextInt(10000).toLong() else rng.nextInt(48).toLong()
		/* Mostly in range, sometimes up to 99; often no hundredths */
		fun part(i: Int, kind: Kind): Int {
			val last = (i == kind.fields.size - 1)
			if (last && rng.nextBoolean()) {
				return 0
			}
			return if (last || rng.nextInt(5) == 0) rng.nextInt(100) else rng.nextInt(60)
		}
		fun value(kind: Kind) = Pair(whole(), kind.fields.indices.map { part(it, kind) })
		for (kind in kinds) {
			for (i in 0 until count) {
				val op = listOf(null, "plus", "minus")[i % 3]
				val values = if (op == null) listOf(value(kind)) else listOf(value(kind), value(kind))
				cases.add(Triple(kind, values, op))
			}
		}
		run(cases)
	}
}
