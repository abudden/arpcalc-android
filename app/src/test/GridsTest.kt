package uk.co.cgtk.karpcalc

import org.junit.Assert.*
import org.junit.Test

public class GridsTest {
	@Test
	fun simpleGridTest() {
		var ch = CommandHandler()
		// All grids except SI Pad
		var gridList = listOf("numpad",
			"funcpad", "altfuncpad",
			"convpad", "constpad",
			"romanupperpad", "romanlowerpad",
			"greekupperpad", "greeklowerpad"
		)

		for (gridname in gridList) {
			println("Checking ${gridname}")
			var grid = ch.getGrid(gridname)
			// Just check the first entry is not NOP
			assertFalse(grid[0][0].name == "NOP")
		}
			println("Checking sipad")
		var sigrid = ch.getGrid("sipad")
		// Check second row for sipad
		assertFalse(sigrid[1][0].name == "NOP")
	}
}
