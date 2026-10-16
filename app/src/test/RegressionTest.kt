package uk.co.cgtk.karpcalc

import org.junit.Assert.*
import org.junit.Test
import java.io.ByteArrayOutputStream
import java.util.Locale
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/*
 * Things that used to crash or hang (found by random key-press testing,
 * ExtremeValuesTest or reviewing the desktop version).
 */
public class RegressionTest {
	fun keys(ch: CommandHandler, vararg keys: String): ErrorCode {
		var ec = ErrorCode.NoError
		for (k in keys) {
			ec = ch.keypress(k)
		}
		return ec
	}

	@Test(timeout = 10000)
	fun backspaceToMinusSign() {
		val ch = CommandHandler()
		keys(ch, "clear", "1", "plusMinus", "backspace", "backspace")
		ch.getXDisplay()
		keys(ch, "5", "enter")
		ch.st.checkTop(5.0)
	}

	@Test(timeout = 10000)
	fun backspaceEverything() {
		val ch = CommandHandler()
		for (k in listOf("clear", "backspace", "backspace", "1", "exponent", "backspace", "backspace", "backspace")) {
			ch.keypress(k)
			ch.getXDisplay()
		}
	}

	@Test(timeout = 10000)
	fun longEntryInOtherBases() {
		for ((base, digit) in listOf(Pair(DisplayBase.baseHexadecimal, "F"),
				Pair(DisplayBase.baseOctal, "7"), Pair(DisplayBase.baseBinary, "1"))) {
			val ch = CommandHandler()
			ch.selectBase(base)
			for (i in 0 until 70) {
				ch.keypress(digit)
				ch.getXDisplay()
				ch.getBaseDisplay()
			}
			ch.keypress("enter")
			ch.getStackDisplay()
		}
		val ch = CommandHandler()
		ch.selectBase(DisplayBase.baseHexadecimal)
		keys(ch, "1", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "enter")
		ch.st.checkTop("18446744073709551616")
	}

	@Test(timeout = 10000)
	fun longExponentEntry() {
		val ch = CommandHandler()
		keys(ch, "1", "exponent")
		for (i in 0 until 15) {
			ch.keypress("9")
			ch.getXDisplay()
		}
		ch.keypress("enter")
		ch.getStackDisplay()
	}

	@Test(timeout = 10000)
	fun percentChangeFromZero() {
		val ch = CommandHandler()
		ch.st.push(0.0)
		ch.st.push(5.0)
		assertEquals(ErrorCode.DivideByZero, ch.keypress("percentchange"))
		ch.st.checkTop(5.0)
		ch.st.checkTop(0.0)
	}

	@Test(timeout = 10000)
	fun dayOfYearRange() {
		val ch = CommandHandler()
		val toDate = "Convert_Date_Day of Year_Date in Year"
		ch.st.push(A("0.2024"))
		assertEquals(ErrorCode.InvalidConversion, ch.keypress(toDate))
		ch.st.push(A("366.2023"))
		assertEquals(ErrorCode.InvalidConversion, ch.keypress(toDate))
		ch.st.push(A("366.2024"))
		assertEquals(ErrorCode.NoError, ch.keypress(toDate))
		ch.st.checkTop("31.122024")

		val toDay = "Convert_Date_Date in Year_Day of Year"
		for (v in listOf("4294967301.012024", "1e100", "0.5", "-3")) {
			ch.st.push(A(v))
			assertEquals(ErrorCode.InvalidConversion, ch.keypress(toDay))
			ch.st.checkTop(v)
		}
	}

	@Test(timeout = 30000)
	fun hugeArguments() {
		val ch = CommandHandler()
		ch.st.push(A("10000"))
		assertEquals(ErrorCode.NoError, ch.keypress("cosh"))
		assertEquals(4342L, ch.st.pop().exponent10())
		ch.st.push(A("-10000"))
		ch.keypress("sinh")
		assertTrue(ch.st.pop() < A(0))
		ch.st.push(A("1e10"))
		ch.keypress("tanh")
		ch.st.checkTop(1.0)

		ch.st.push(A("1e10000000"))
		assertEquals(ErrorCode.NoError, ch.keypress("squareroot"))
		ch.st.checkTop("1e5000000")
		ch.st.push(A("8e-30000000"))
		ch.keypress("cuberoot")
		ch.st.checkTop("2e-10000000")
		ch.st.push(A("1e1000000000"))
		ch.keypress("floor")
		ch.st.checkTop("1e1000000000")
	}

	@Test(timeout = 10000)
	fun outOfRangeLeavesStackAlone() {
		val ch = CommandHandler()
		ch.st.push(A("7"))
		ch.st.push(A("1e100"))
		assertEquals(ErrorCode.OutOfRange, ch.keypress("sin"))
		ch.st.checkTop("1e100")
		ch.st.checkTop(7.0)

		ch.st.push(A("1.5e20"))
		assertEquals(ErrorCode.OutOfRange, ch.keypress("twotox"))
		ch.st.checkTop("1.5e20")

		/* Squaring 1e2000000000 overflows BigDecimal's exponent */
		ch.st.push(A("1e2000000000"))
		assertEquals(ErrorCode.OutOfRange, ch.keypress("square"))
		ch.st.checkTop("1e2000000000")
	}

	fun zip(contents: String): ByteArray {
		val bytes = ByteArrayOutputStream()
		ZipOutputStream(bytes).use {
			it.putNextEntry(ZipEntry("eurofxref.csv"))
			it.write(contents.toByteArray())
			it.closeEntry()
		}
		return bytes.toByteArray()
	}

	@Test(timeout = 10000)
	fun currencyDownloadIsNotTheFile() {
		val ch = CommandHandler()
		/* e.g. a Wi-Fi login page */
		val message = ch.processCurrencyData("<html><body>Please log in</body></html>".toByteArray())
		assertTrue(message.length > 0)
		assertTrue(ch.processCurrencyData(ByteArray(0)).length > 0)
		/* Truncated */
		val good = zip("Date, USD, JPY, \n16 October 2026, 1.1, 160.5, \n")
		ch.processCurrencyData(good.copyOf(good.size / 2))
	}

	@Test(timeout = 10000)
	fun currencyZeroRate() {
		val ch = CommandHandler()
		ch.processCurrencyData(zip("Date, USD, JPY, \n16 October 2026, 0, 160.5, \n"))
		assertFalse(ch.getRawCurrencyData().containsKey("USD"))
		assertEquals(160.5, ch.getRawCurrencyData()["JPY"]!!, 0.0)
	}

	@Test(timeout = 10000)
	fun currencyDateInOtherLanguages() {
		val saved = Locale.getDefault()
		try {
			Locale.setDefault(Locale.GERMANY)
			val ch = CommandHandler()
			val message = ch.processCurrencyData(zip("Date, USD, \n1 October 2001, 1.1, \n"))
			assertTrue(message, message.startsWith("Currency data is out of date"))
		}
		finally {
			Locale.setDefault(saved)
		}
	}
}
