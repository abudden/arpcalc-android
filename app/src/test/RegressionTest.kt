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

	/* The identities used for large and tiny arguments agree with big-math
	 * where it works, and the extremes are quick and don't overflow
	 */
	fun close(expected: AF, actual: AF) {
		val diff = (expected - actual).abs()
		val scale = expected.abs()
		assertTrue("expected $expected, got $actual",
				(diff == A(0)) || (diff <= scale * A("1e-30")))
	}

	@Test(timeout = 20000)
	fun inverseTrigIdentities() {
		for (v in listOf("1.5", "2", "-3", "10", "12345.678", "-1e30")) {
			close(ch.obermuhlner.math.big.BigDecimalMath.atan(A(v).value, mc).toAF(), A(v).atan())
		}
		for (v in listOf("1e21", "3.5e25", "1e40")) {
			close(ch.obermuhlner.math.big.BigDecimalMath.asinh(A(v).value, mc).toAF(), A(v).asinh())
			close(ch.obermuhlner.math.big.BigDecimalMath.acosh(A(v).value, mc).toAF(), A(v).acosh())
		}
		/* big-math loses digits for negative arguments (x + sqrt(x² + 1)
		 * nearly cancels), so compare with -asinh(|x|)
		 */
		for (v in listOf("-0.5", "-2", "-1e10")) {
			close(-ch.obermuhlner.math.big.BigDecimalMath.asinh(A(v).abs().value, mc).toAF(), A(v).asinh())
		}
		/* (big-math gives 0 for some of these: it works to a number of
		 * decimal places rather than significant figures)
		 */
		for (v in listOf("1e-41", "-3e-50")) {
			close(A(v), A(v).atan())
			close(A(v), A(v).asin())
			close(A(v), A(v).atanh())
			close(A(v), A(v).asinh())
			close((pi() / 2.0) - A(v), A(v).acos())
		}

		val halfPi = pi() / 2.0
		close(halfPi, A("1e1000000").atan())
		close(-halfPi, A("-1e999999999").atan())
		close(A("1e-999999999"), A("1e-999999999").atan())
		close(A("1e-999999999"), A("1e-999999999").asin())
		close(halfPi, A("1e-999999999").acos())
		/* ln(2 * 10^1000000) = 1000000 ln(10) + ln(2) */
		val ln2x = A(1000000) * ch.obermuhlner.math.big.BigDecimalMath.log(java.math.BigDecimal.TEN, mc).toAF() +
				ch.obermuhlner.math.big.BigDecimalMath.log(java.math.BigDecimal(2), mc).toAF()
		close(ln2x, A("1e1000000").asinh())
		close(-ln2x, A("-1e1000000").asinh())
		close(ln2x, A("1e1000000").acosh())
		val ln2xMax = A(999999999) * ch.obermuhlner.math.big.BigDecimalMath.log(java.math.BigDecimal.TEN, mc).toAF() +
				ch.obermuhlner.math.big.BigDecimalMath.log(java.math.BigDecimal(2), mc).toAF()
		close(ln2xMax, A("1e999999999").asinh())

		for ((v, r) in listOf(Pair("1e-1000000", listOf(0, 0, 1)), Pair("-1e-1000000", listOf(0, -1, 0)),
				Pair("0.004", listOf(0, 0, 1)), Pair("-0.004", listOf(0, -1, 0)),
				Pair("0.4", listOf(0, 0, 1)), Pair("-0.6", listOf(-1, -1, 0)))) {
			assertEquals(A(r[0]), A(v).round())
			assertEquals(A(r[1]), A(v).floor())
			assertEquals(A(r[2]), A(v).ceil())
		}

		val ch = CommandHandler()
		ch.st.push(A("2"))
		ch.st.push(A("1e-1000000"))
		assertEquals(ErrorCode.NoError, ch.keypress("inversetan2"))
		close(A(90), ch.st.pop())
	}
}
