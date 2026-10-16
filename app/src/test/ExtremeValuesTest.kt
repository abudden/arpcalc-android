package uk.co.cgtk.karpcalc

import org.junit.Assert.*
import org.junit.Test
import java.util.concurrent.Callable
import java.util.concurrent.ExecutionException
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

/*
 * Every button and every display with very large and very small numbers.
 *
 * BigDecimal has no infinity, so a result too large to represent throws
 * (and some big-math functions take practically forever) where the desktop
 * version would just show infinity.  None of this should crash or hang the
 * app: out-of-range results are reported as errors instead.
 */
public class ExtremeValuesTest {
	val values = listOf("0", "1e-1000000", "-1e-1000000", "1e1000000", "-1e1000000",
			"1e100", "-1e-100", "123456789e300", "1e999999999", "-1e999999999",
			"1e-999999999", "18446744073709551616", "-9223372036854775809")

	val grids = listOf("numpad", "funcpad", "altfuncpad", "convpad", "constpad", "sipad")

	fun commands(): List<String> {
		val ch = CommandHandler()
		val result = sortedSetOf<String>()
		for (g in grids) {
			for (row in ch.getGrid(g)) {
				for (bi in row) {
					if (bi.name.isNotEmpty()) {
						result.add(bi.name)
					}
				}
			}
		}
		return result.toList()
	}

	fun displayAll(ch: CommandHandler) {
		ch.getXDisplay()
		ch.getStackDisplay()
		ch.getBaseDisplay()
		ch.getXValue().toString()
	}

	/* Run each check with a time limit and collect the failures, so that one
	 * run shows every problem.
	 */
	fun check(checks: List<Pair<String, () -> Unit>>) {
		val failures = mutableListOf<String>()
		var executor = Executors.newSingleThreadExecutor { r -> Thread(r).apply { isDaemon = true } }
		try {
			for ((name, f) in checks) {
				val future = executor.submit(Callable { f() })
				try {
					future.get(10, TimeUnit.SECONDS)
				}
				catch (e: TimeoutException) {
					failures.add("$name: timed out")
					/* The stuck thread can't be stopped and keeps the CPU
					 * busy, so don't let too many of them build up.
					 */
					if (failures.count { it.endsWith("timed out") } >= 5) {
						failures.add("(stopped after 5 time-outs)")
						break
					}
					executor.shutdownNow()
					executor = Executors.newSingleThreadExecutor { r -> Thread(r).apply { isDaemon = true } }
				}
				catch (e: ExecutionException) {
					val at = e.cause?.stackTrace?.firstOrNull { "karpcalc" in it.className }
					failures.add("$name: ${e.cause} at $at")
				}
			}
		}
		finally {
			executor.shutdownNow()
		}
		if (failures.isNotEmpty()) {
			fail("${failures.size} failures:\n" + failures.joinToString("\n"))
		}
	}

	@Test
	fun displays() {
		val checks = mutableListOf<Pair<String, () -> Unit>>()
		for (v in values) {
			for (opt in listOf<DispOpt?>(null) + DispOpt.values()) {
				for (base in DisplayBase.values()) {
					checks.add(Pair("display $v $opt $base", {
						val ch = CommandHandler()
						if (opt != null) {
							ch.toggleOption(opt)
						}
						ch.selectBase(base)
						ch.st.push(A(v))
						displayAll(ch)
						ch.keypress("EngL")
						displayAll(ch)
						ch.keypress("EngR")
						ch.keypress("EngR")
						displayAll(ch)
					}))
				}
			}
		}
		check(checks)
	}

	@Test
	fun buttons() {
		val checks = mutableListOf<Pair<String, () -> Unit>>()
		for (c in commands()) {
			for (x in values) {
				for (y in listOf("2", x)) {
					checks.add(Pair("$y $x $c", {
						val ch = CommandHandler()
						ch.st.push(A(y))
						ch.st.push(A(x))
						ch.keypress(c)
						displayAll(ch)
					}))
				}
			}
		}
		check(checks)
	}
}
