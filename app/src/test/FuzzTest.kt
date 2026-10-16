package uk.co.cgtk.karpcalc

import org.junit.Assert.*
import org.junit.Test
import java.util.Random
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

/*
 * Random key-press tester for the calculator core, ported from the desktop
 * version's tests/fuzz_core.cpp.
 *
 * Presses random buttons (weighted towards clear and backspace), changes
 * options and renders every display after each press, as the activity does.
 * Any exception, or a press that takes too long, fails the test with the
 * seed and the presses that led up to it.
 *
 * Set the FUZZ_SEEDS and FUZZ_STEPS environment variables to run more.
 */
public class FuzzTest {
	val grids = listOf("numpad", "funcpad", "altfuncpad", "convpad", "constpad", "sipad",
			"romanupperpad", "romanlowerpad", "greekupperpad", "greeklowerpad")

	fun allCommands(): List<String> {
		val ch = CommandHandler()
		val commands = sortedSetOf<String>()
		for (g in grids) {
			for (row in ch.getGrid(g)) {
				for (bi in row) {
					if (bi.name.isNotEmpty()) {
						commands.add(bi.name)
					}
				}
			}
		}
		return commands.toList()
	}

	fun run(seed: Long, steps: Int, commands: List<String>, history: MutableList<String>) {
		val rng = Random(seed)
		val ch = CommandHandler()
		val dispOpts = DispOpt.values()
		val calcOpts = CalcOpt.values()

		for (i in 0 until steps) {
			val r = rng.nextInt(100)
			if (r < 20) {
				history.add("clear")
				ch.keypress("clear")
			}
			else if (r < 30) {
				history.add("backspace")
				ch.keypress("backspace")
			}
			else if (r < 85) {
				val c = commands[rng.nextInt(commands.size)]
				if (c.startsWith("Store")) {
					if (rng.nextBoolean()) {
						history.add("store " + c)
						ch.store(c)
					}
					else {
						history.add("recall " + c)
						ch.recall(c)
					}
				}
				else {
					history.add(c)
					ch.keypress(c)
				}
			}
			else if (r < 92) {
				val o = dispOpts[rng.nextInt(dispOpts.size)]
				history.add("option " + o)
				ch.toggleOption(o)
			}
			else if (r < 94) {
				val o = calcOpts[rng.nextInt(calcOpts.size)]
				history.add("option " + o)
				ch.toggleOption(o)
			}
			else if (r < 96) {
				history.add("next places")
				ch.nextPlaces()
			}
			else if (r < 98) {
				val b = BitCount.values()[rng.nextInt(BitCount.values().size)]
				history.add("bits " + b)
				ch.setBitCount(b)
			}
			else {
				history.add("base")
				ch.nextBase()
			}

			ch.getXDisplay()
			ch.getStackDisplay()
			ch.getBaseDisplay()
			ch.getStatusBase()
			ch.getStatusExponent()
			ch.getStatusAngularUnits()
			ch.getXValue().toString()
		}
	}

	@Test
	fun randomKeyPresses() {
		val seeds = System.getenv("FUZZ_SEEDS")?.toIntOrNull() ?: 20
		val steps = System.getenv("FUZZ_STEPS")?.toIntOrNull() ?: 2000
		val commands = allCommands()
		var worker: Thread? = null
		val executor = Executors.newSingleThreadExecutor { r ->
			Thread(r).apply { isDaemon = true ; worker = this }
		}
		try {
			for (seed in 1..seeds) {
				val history = java.util.Collections.synchronizedList(mutableListOf<String>())
				val future = executor.submit { run(seed.toLong(), steps, commands, history) }
				var failure: String? = null
				try {
					future.get(60, TimeUnit.SECONDS)
				}
				catch (e: TimeoutException) {
					failure = "timed out\n" +
						worker?.stackTrace?.filter { "karpcalc" in it.className || "obermuhlner" in it.className }?.take(12)?.joinToString("\n") { "    at " + it }
				}
				catch (e: java.util.concurrent.ExecutionException) {
					failure = e.cause.toString() + "\n" +
						e.cause?.stackTrace?.take(8)?.joinToString("\n") { "    at " + it }
				}
				if (failure != null) {
					val last = history.toList().takeLast(25)
					fail("Seed $seed, step ${history.size}: $failure\nLast presses:\n" +
							last.joinToString("\n") { "  " + it })
				}
			}
		}
		finally {
			executor.shutdownNow()
		}
	}
}
