package uk.co.cgtk.karpcalc

import org.junit.Assert.*
import org.junit.Test

public class CommandsTest {
	@Test
	fun defaultCommandTests() {
		var ch = CommandHandler()

		/* TODO: How to test this?! */
		ch.keypress("1")
		ch.keypress("2")
		ch.keypress("backspace")
		ch.keypress("backspace")
		ch.keypress("backspace")
		ch.keypress("backspace")

		ch.keypress("1")
		ch.keypress("Convert_Distance_Kilometres_Inches")
		ch.st.checkTop(1.0*1000.0*1000.0/25.4)

		/* Test that all conversions in grid work without crash */
		var grid = ch.getGrid("convpad")
		for (row in grid) {
			for (bi in row) {
				if (bi.name.startsWith("Convert_")) {
					//println("Testing " + bi.name)
					var check: String? = null
					if (bi.name == "Convert_Date_Date in Year_Day of Year") {
						ch.st.push(A("12.021824"))
						check = "43.1824"
					}
					else if (bi.name == "Convert_Date_Day of Year_Date in Year") {
						ch.st.push(A("60.2020"))
						check = "29.022020"
					}
					ch.keypress(bi.name)
					if (check != null) {
						ch.st.checkTop(check)
					}
				}
			}
		}

		ch.setOption(DispOpt.EngNotation, true)
		ch.setOption(DispOpt.PowerExponentView, false)
		ch.setOption(DispOpt.TrimZeroes, true)
		ch.setOption(DispOpt.AlwaysShowDecimal, true)
		ch.setOption(DispOpt.BinaryPrefixes, false)
		ch.setOption(DispOpt.ThousandsSeparator, true)
		ch.setPlaces(10)

		ch.selectBase(DisplayBase.baseDecimal)
		ch.keypress("clear")
		ch.keypress("clear")
		ch.keypress("1")
		ch.keypress("2")
		ch.keypress(".")
		ch.keypress("3")
		ch.keypress("exponent")
		ch.keypress("6")
		ch.keypress("enter")
		assertEquals("12.3e6", ch.getXDisplay())
		ch.keypress("EngL")
		assertEquals("12,300.0e3", ch.getXDisplay())
		ch.keypress("EngR")
		ch.keypress("EngR")
		assertEquals("0.0123e9", ch.getXDisplay())
		ch.keypress("clear")
		ch.keypress("clear")
		ch.keypress("1")
		ch.keypress("2")
		ch.keypress("3")
		ch.keypress("4")
		ch.keypress("5")
		assertEquals("12,345", ch.getXDisplay())
		ch.keypress("enter")
		assertEquals("12,345.0", ch.getXDisplay())
		ch.selectBase(DisplayBase.baseHexadecimal)
		assertEquals("0x3039".replace(" ", "&thinsp;"), ch.getXDisplay())
		ch.selectBase(DisplayBase.baseBinary)
		assertEquals("0b0011 0000 0011 1001".replace(" ", "&thinsp;"), ch.getXDisplay())
		ch.selectBase(DisplayBase.baseOctal)
		assertEquals("0o3 0071".replace(" ", "&thinsp;"), ch.getXDisplay())
		ch.selectBase(DisplayBase.baseDecimal)

		ch.keypress("clear")
		ch.keypress("clear")
		ch.keypress("5")
		ch.keypress("Convert_Distance_Inches_Millimetres")
		assertEquals("127.0", ch.getXDisplay())
		ch.keypress(".")
		assertEquals("0.", ch.getXDisplay())
		ch.keypress("5")
		assertEquals("0.5", ch.getXDisplay())
		ch.keypress("times")
		assertEquals("63.5", ch.getXDisplay())
		ch.keypress("clear")
		ch.keypress("clear")

		ch.selectBase(DisplayBase.baseHexadecimal)
		println("Hex Input Mode")
		ch.keypress("E")
		println("Finished E press")
		ch.keypress("A")
		ch.keypress("B")
		ch.keypress("C")
		ch.keypress("D")
		ch.keypress("F")
		println("Hex input mode complete: " + ch.getXDisplay())
		ch.selectBase(DisplayBase.baseDecimal)
		assertEquals("15.383775e6", ch.getXDisplay())

		ch.keypress("clear")
		ch.keypress("clear")

		ch.setPlaces(5)
		ch.selectBase(DisplayBase.baseHexadecimal)
		ch.keypress("A")
		ch.keypress("F")
		ch.keypress("8")
		ch.keypress("B")
		ch.keypress("C")
		ch.keypress("1")
		ch.keypress("5")
		println("Large hex input mode complete: " + ch.getXDisplay())
		ch.selectBase(DisplayBase.baseDecimal)
		assertEquals("184.07324e6", ch.getXDisplay())
		println("Converted to decimal: " + ch.getXDisplay())
		ch.keypress("EngL")
		assertEquals("184,073.237e3", ch.getXDisplay())
		ch.keypress("EngL")
		assertEquals("184,073,237.0", ch.getXDisplay())
		println("Double EngL: " + ch.getXDisplay())
		ch.keypress("EngL")
		assertEquals("184,073,237,000.0e&ndash;3", ch.getXDisplay())
		println("Triple EngL: " + ch.getXDisplay())
	}

	@Test
	fun dmsConversionTest() {
		var ch = CommandHandler()
		ch.setOption(DispOpt.EngNotation)
		ch.setOption(DispOpt.TrimZeroes)
		ch.setOption(DispOpt.AlwaysShowDecimal)
		ch.setOption(DispOpt.ThousandsSeparator)

		// Simple case
		ch.keypresses("10")
		ch.keypress("Convert_Angle_Degrees_Degrees.Minutes-Seconds")
		assertEquals("10.0", ch.getXDisplay())

		// This relies on correct rounding
		ch.keypresses("19.15")
		ch.keypress("Convert_Angle_Degrees.Minutes-Seconds_Degrees")
		assertEquals("19.25", ch.getXDisplay()) // Currently fails the test
		ch.keypresses("8.3056")
		ch.keypress("Convert_Angle_Degrees.Minutes-Seconds_Degrees")
		ch.keypresses(listOf("plus", "24", "remainder"))
		ch.keypress("Convert_Angle_Degrees_Degrees.Minutes-Seconds")
		assertEquals("3.4556", ch.getXDisplay())
	}

	@Test
	fun teneminus10test() {
		var ch = CommandHandler()
		ch.setOption(DispOpt.EngNotation)
		ch.setOption(DispOpt.TrimZeroes)
		ch.setOption(DispOpt.AlwaysShowDecimal)
		ch.setOption(DispOpt.ThousandsSeparator)

		ch.keypress("1")
		ch.keypress("0")
		ch.keypress("exponent")
		ch.keypress("3")
		ch.keypress("plusMinus")
		assertEquals("10e&ndash;3", ch.getXDisplay())
		ch.keypress("enter")
		assertEquals("0.01", ch.getXDisplay())
		ch.keypress("clear")
		ch.keypress("clear")

		ch.keypress("1")
		ch.keypress("0")
		ch.keypress("exponent")
		ch.keypress("1")
		ch.keypress("0")
		ch.keypress("plusMinus")

		assertEquals("10e&ndash;10", ch.getXDisplay())
		ch.keypress("enter")
		assertEquals("1.0e&ndash;9", ch.getXDisplay())
		ch.keypress("clear")
		ch.keypress("clear")
	}


	@Test
	fun leading_dot_test() {
		var ch = CommandHandler()
		ch.setOption(DispOpt.EngNotation)
		ch.setOption(DispOpt.TrimZeroes)
		ch.setOption(DispOpt.AlwaysShowDecimal)
		ch.setOption(DispOpt.ThousandsSeparator)

		ch.keypresses("15")
		ch.keypress("enter")
		ch.keypresses("0.5")
		ch.keypress("times")
		ch.keypresses("0.5")
		ch.keypress("times")
		assertEquals("3.75", ch.getXDisplay())
		ch.keypress("clear")
		ch.keypress("clear")

		ch.keypresses("15")
		ch.keypress("enter")
		ch.keypresses(".5")
		ch.keypress("times")
		ch.keypresses("0.5")
		ch.keypress("times")
		assertEquals("3.75", ch.getXDisplay())
		ch.keypress("clear")
		ch.keypress("clear")

		ch.keypresses("15")
		ch.keypress("enter")
		ch.keypresses(".5")
		ch.keypress("times")
		ch.keypresses(".5")
		ch.keypress("times")
		assertEquals("3.75", ch.getXDisplay())
		ch.keypress("clear")
		ch.keypress("clear")
	}
}

