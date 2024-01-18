package uk.co.cgtk.karpcalc

/* Specific tests for Conversions */

import org.junit.Assert.*
import org.junit.Test

import java.io.File
import java.lang.ProcessBuilder
import java.lang.ProcessBuilder.Redirect
import java.util.concurrent.TimeUnit

fun String.runCommand(workingDir: File) {
	ProcessBuilder(*split(" ").toTypedArray())
				.directory(workingDir)
				.redirectOutput(Redirect.INHERIT)
				.redirectError(Redirect.INHERIT)
				.start()
				.waitFor(60, TimeUnit.MINUTES)
}

public class ConversionTests {

	private val ALLOWED_DELTA = 1e-5 // proportion of expected value

	@Test
	fun defaultConversionTests() {
		var st = Stack()

		var workingDir = File(".")

		// Export list of all supported conversions
		var unitListFileName = "./unit_export.txt"
		// Python script for test generation
		var pythonScriptFileName = "../src/test/generate_conversion_tests.py"
		// Output of python script containing list of tests
		var conversionTestFileName = "./conversion_tests.txt"

		File(unitListFileName).printWriter().use() { out ->
			var cats = st.getConversionCategories()
			for (cat in cats) {
				if (cat == "Currency") {
					continue
				}
				var units = st.getAvailableUnits(cat)
				for (unit in units) {
					if (unit.startsWith("Degrees.") || unit.startsWith("Hours.")) { // Degrees.Minutes-Seconds or Degrees.Minutes
						continue
					}
					if ((unit == "Date in Year") || (unit == "Day of Year")) {
						continue
					}
					out.println("${cat}_${unit}")
				}
			}
		}

		// Now run the python script
		"python ${pythonScriptFileName} --unit-list ${unitListFileName} --out-file ${conversionTestFileName}".runCommand(workingDir)

		// Now read the python script result back in
		// (Should be Category_InputValue_FromUnit_ToUnit_ExpectedValue one per line)
		File(conversionTestFileName).forEachLine {
			var parts = it.trim().split("_")
			var cat = parts[0]
			var inputValue = parts[1].toDouble()
			var fromUnit = parts[2]
			var toUnit = parts[3]
			var expectedValue = parts[4].toDouble()

			// Now test all entries from the python script result
			st.push(A(inputValue))
			assertEquals("Conversion of ${inputValue} failed: ${fromUnit} to ${toUnit}",
				ErrorCode.NoError,
				st.convert(cat, fromUnit, toUnit))
			var result = st.pop().toDouble()
			// Uses assertEquals from StackTest.kt
			var DELTA = Math.abs(ALLOWED_DELTA * expectedValue)
			assertEquals("Conversion of ${inputValue} from ${fromUnit} to ${toUnit} incorrect",
				expectedValue, result, DELTA)
		}

		File(unitListFileName).delete()
		File(conversionTestFileName).delete()
	}

	@Test
	fun symbolPresenceTest() {
		var st = Stack()
		for (tbl in st.conversionTable.values) {
			for (conv in tbl) {
				var fromUnit = conv.from
				assertTrue("Unit symbol missing for " + fromUnit,
					st.unitSymbols.containsKey(fromUnit))
			}
		}
	}

	@Test
	fun dateTests() {
		var st = Stack()
		st.push(A("11.102022"))

		assertEquals("Conversion to day of year failed",
			ErrorCode.NoError,
			st.convert("Date", "Date in Year", "Day of Year"))
		st.checkTop("284.2022", true)

		assertEquals("Conversion to date of year failed",
			ErrorCode.NoError,
			st.convert("Date", "Day of Year", "Date in Year"))

		st.checkTop("11.102022")
	}

	@Test
	fun conversionTableCompleteTest() {
		var st = Stack()
		var cats = st.getConversionCategories()
		for (cat in cats) {
			var units = st.getAvailableUnits(cat)
			for (fromUnit in units) {
				for (toUnit in units) {
					if ((fromUnit == "Date in Year")) {
						st.push(28.072022)
					}
					else {
						st.push(A(1.0))
					}
					assertEquals("Conversion failed: ${fromUnit} to ${toUnit}",
						ErrorCode.NoError,
						st.convert(cat, fromUnit, toUnit))
					st.pop()
				}
			}
		}
	}
}
