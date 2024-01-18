package uk.co.cgtk.karpcalc

import org.junit.Assert.*
import org.junit.Test

public class SITest {
	@Test
	fun defaultSITests() {
		// Run tests with default options
		var calc = CommandHandler()
		/* Assumes checkTop is available */

		calc.st.push(5.2)
		calc.SI("k")
		calc.st.checkTop(5200.0)

		calc.st.push(1.0)
		calc.SI("Mi")
		calc.st.checkTop(1048576.0)

		assertEquals(listOf("Ki", "Mi", "Gi", "Ti", "Pi", "Ei"),
			calc.getBinaryPrefixSymbols())
		assertEquals(listOf("y", "z", "a", "f", "p", "n", "&mu;", "m", "k", "M", "G", "T", "P", "E", "Z", "Y"),
			calc.getDecimalPrefixSymbols())
		assertEquals(listOf("Kibi", "Mebi", "Gibi", "Tebi", "Pebi", "Exbi"),
			calc.getBinaryPrefixNames())
		assertEquals(listOf("Yocto", "Zepto", "Atto", "Femto", "Pico", "Nano", "Micro", "Milli", "Kilo", "Mega", "Giga", "Tera", "Peta", "Exa", "Zetta", "Yotta"),
			calc.getDecimalPrefixNames())
	}
}
