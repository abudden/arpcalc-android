package uk.co.cgtk.karpcalc

data class BI(val name: String,
	val display: String,
	val helpText: String = "",
	val scale: Float = 1.0f,
	val doubleWidth: Boolean = false,
	val hidden: Boolean = false
)

data class OptLoc(val page: Int = 0, val row: Int = UndefinedRow, val col: Int = UndefinedColumn)

data class Option(val helpText: String,
	val clickAction: () -> Unit,
	val iconAction: () -> Int,
	val location: OptLoc,
	val hideOnPhone: Boolean = false,
	val hideOnPC: Boolean = false,
	val requiresRestart: Boolean = false
)

fun CommandHandler.getCheckIcon(v: Boolean): Int {
	if (v) {
		return R.drawable.checked
	}
	else {
		return R.drawable.unchecked
	}
}

fun CommandHandler.getPlacesIcon(): Int {
	var v: Int = getPlaces()
	return when (v) {
		0 -> R.drawable.num_0
		1 -> R.drawable.num_1
		2 -> R.drawable.num_2
		3 -> R.drawable.num_3
		4 -> R.drawable.num_4
		5 -> R.drawable.num_5
		6 -> R.drawable.num_6
		7 -> R.drawable.num_7
		8 -> R.drawable.num_8
		9 -> R.drawable.num_9
		else -> R.drawable.num_0
	}
}

fun CommandHandler.getBitCountIcon(): Int {
	var bc = getBitCount()
	return when (bc) {
		BitCount.bc8  -> R.drawable.num_8
		BitCount.bc16 -> R.drawable.num_16
		BitCount.bc32 -> R.drawable.num_32
		BitCount.bc64 -> R.drawable.num_64
	}
}

fun CommandHandler.getBaseIcon(): Int {
	return when (dspBase) {
		DisplayBase.baseDecimal  -> R.drawable.decimal
		DisplayBase.baseHexadecimal -> R.drawable.hexadecimal
		DisplayBase.baseBinary -> R.drawable.binary
		DisplayBase.baseOctal -> R.drawable.octal
	}
}

fun CommandHandler.getWindowSizeIcon(): Int {
	return when (sizeName) {
		"Large"  -> R.drawable.large
		"Medium" -> R.drawable.medium
		"Small"  -> R.drawable.small
		else     -> R.drawable.large
	}
}

fun CommandHandler.getOptions(): Map<String, Option> {
	return mapOf(
		"Use Engineering Exponents" to Option(
			"""When showing exponents, always use multiples of 3 to correspond with SI prefixes (milli-, kilo- etc).
			When using binary prefixes, always use multiples of 10 to correspond with SI binary prefixes (Kibi-, Mebi- etc).""",
			{ this.toggleOption(DispOpt.EngNotation) },
			{ this.getCheckIcon(this.getOption(DispOpt.EngNotation)) },
			OptLoc(0, 0, 0)
		),
		"Use SI Exponents" to Option(
			"""When showing exponents, use the SI prefixes (k, M, G etc) instead of showing powers of 10 (for powers that match SI prefixes.""",
			{ this.toggleOption(DispOpt.SINotation) },
			{ this.getCheckIcon(this.getOption(DispOpt.SINotation)) },
			OptLoc(0, 1, 0)
		),
		"Show Exponents as Powers" to Option(
			"""When showing exponents, show them as powers (e.g. 4.0&times;10<sup><small>3</small></sup>) rather than exponents
			(e.g. 4.0e3).  This option is set automatically if using binary prefixes to avoid confusion as to the meaning of 'e'.""",
			{ this.toggleOption(DispOpt.PowerExponentView) },
			{ this.getCheckIcon(this.getOption(DispOpt.PowerExponentView)) },
			OptLoc(0, 2, 0)
		),
		"Use Binary Exponents" to Option(
			"""When showing exponents, use powers of 2 instead of powers of 10.""",
			{ this.toggleOption(DispOpt.BinaryPrefixes) },
			{ this.getCheckIcon(this.getOption(DispOpt.BinaryPrefixes)) },
			OptLoc(0, 3, 0)
		),
		"Degrees or Radians" to Option(
			"""Angle type to use when performing trigonometric functions.""",
			{ this.toggleOption(CalcOpt.Radians) },
			{ if (this.getOption(CalcOpt.Radians)) R.drawable.radians else R.drawable.degrees },
			OptLoc(0, 4, 0)
		),
		"Show Help on Start" to Option(
			"""Show a "toast" notification on start-up explaining that a long-press on any key gives help.""",
			{ this.toggleOption(DispOpt.ShowHelpOnStart) },
			{ this.getCheckIcon(this.getOption(DispOpt.ShowHelpOnStart)) },
			OptLoc(0, 5, 0),
			hideOnPC = true
		),
		"Window Size" to Option(
			"""Switch the size of the PC UI - note that this option requires a restart to take effect.""",
			{ this.nextWindowSize() },
			{ this.getWindowSizeIcon() },
			OptLoc(0, 5, 0),
			hideOnPhone = true,
			requiresRestart = true
		),
		"Decimal Places to Show" to Option(
			"""Number of decimal places to show.  See also "Trim Zeroes after Decimal" and "Always Show Decimal" options""",
			{ this.nextPlaces() },
			{ this.getPlacesIcon() },
			OptLoc(0, 0, 3)
		),
		"Always Show Decimal" to Option(
			"""Always show the decimal point (and following zero) even if there are no decimal places to show (this option is
			only relevant if the "Trim Zeroes after Decimal" option is set.""",
			{ this.toggleOption(DispOpt.AlwaysShowDecimal) },
			{ this.getCheckIcon(this.getOption(DispOpt.AlwaysShowDecimal)) },
			OptLoc(0, 1, 3)
		),
		"Trim Zeroes after Decimal" to Option(
			"""Do not show trailing zeroes (after the decimal point), regardless of the setting of "Decimal Places to Show".
			See also the "Always Show Decimal" option.""",
			{ this.toggleOption(DispOpt.TrimZeroes) },
			{ this.getCheckIcon(this.getOption(DispOpt.TrimZeroes)) },
			OptLoc(0, 2, 3)
		),
		"Save Stack on Exit" to Option(
			"""When quitting ARPCalc, save the current stack state to allow
			continuing when you restart.""",
			{ this.toggleOption(DispOpt.SaveStackOnExit) },
			{ this.getCheckIcon(this.getOption(DispOpt.SaveStackOnExit)) },
			OptLoc(0, 3, 3)
		),
		"Use Replicating Stack" to Option(
			"""Use a four-entry stack that replicates the bottom entry (T) rather than an infinite stack.""",
			{ this.toggleOption(CalcOpt.ReplicateStack) },
			{ this.getCheckIcon(this.getOption(CalcOpt.ReplicateStack)) },
			OptLoc(0, 4, 3)
		),
		"Select Base" to Option(
			"""Toggle between decimal, hexadecimal, binary and octal.""",
			{ this.nextBase() },
			{ this.getBaseIcon() },
			OptLoc(1, 0, 0)
		),
		"Save Base on Exit" to Option(
			"""Save base between sessions (so if you exit in hexadecimal mode,
			ARPCalc will start next time in hexadecimal mode).""",
			{ this.toggleOption(DispOpt.SaveBaseOnExit) },
			{ this.getCheckIcon(this.getOption(DispOpt.SaveBaseOnExit)) },
			OptLoc(1, 1, 0)
		),
		"Show Thousands Separator" to Option(
			"""For values of 1000 or greater, show thousands separators for readability (e.g. 1,234,567.0).""",
			{ this.toggleOption(DispOpt.ThousandsSeparator) },
			{ this.getCheckIcon(this.getOption(DispOpt.ThousandsSeparator)) },
			OptLoc(1, 2, 0)
		),
		"Space As Thousands Separator" to Option(
			"""If show thousands separator option is set, use spaces as the separator (e.g. 1&nbsp;234&nbsp;567.0).""",
			{ this.toggleOption(DispOpt.SpaceAsThousandsSeparator) },
			{ this.getCheckIcon(this.getOption(DispOpt.SpaceAsThousandsSeparator)) },
			OptLoc(1, 3, 0)
		),
		"Bit Count for Bitwise Ops" to Option(
			"""When performing bitwise operations, assume that the data size is set to this value.
			This is also used for the top-right base display.""",
			{ this.nextBitCount() },
			{ this.getBitCountIcon() },
			OptLoc(1, 4, 0)
		),
		"Percent Leaves Y Unchanged" to Option(
			"""When using % or &Delta;%, leave Y in place.""",
			{ this.toggleOption(CalcOpt.PercentLeavesY) },
			{ this.getCheckIcon(this.getOption(CalcOpt.PercentLeavesY)) },
			OptLoc(1, 0, 3)
		),
		"American Units on Keypad" to Option(
			"""When set, the main conversion screen shows more American units.""",
			{ this.toggleOption(DispOpt.AmericanUnits) },
			{ this.getCheckIcon(this.getOption(DispOpt.AmericanUnits)) },
			OptLoc(1, 1, 3)
		),
		"European-Style Decimal" to Option(
			"""When set, decimal places are shown with a comma.""",
			{ this.toggleOption(DispOpt.EuropeanDecimal) },
			{ this.getCheckIcon(this.getOption(DispOpt.EuropeanDecimal)) },
			OptLoc(1, 2, 3)
		),
		"Decimal Base Bias" to Option(
			"""When set, the first press of "BASE" will change to decimal when in any other base.""",
			{ this.toggleOption(DispOpt.DecimalBaseBias) },
			{ this.getCheckIcon(this.getOption(DispOpt.DecimalBaseBias)) },
			OptLoc(1, 3, 3)
		)
	)
}

fun CommandHandler.getStoreHelpText(register: String) : String {
	val currentValue = varStore[register]
	var storeHelpText = "Store/Recall X in the selected register"
	if (currentValue != null) {
		val formattedValue = formatDecimal(currentValue, isX=false)
		storeHelpText += " (current value is ${formattedValue})"
	}
	storeHelpText += "."
	return storeHelpText
}

fun CommandHandler.getGrid(grid: String) : List<List<BI>> {
	var nop = BI("NOP", "", hidden=true)
	var cancelButton = BI("CANCEL", "Cancel", "Cancel and return to main buttons.", doubleWidth=true)
	var result: List<MutableList<BI>> = mutableListOf(
		mutableListOf(nop, nop, nop, nop, nop, nop),
		mutableListOf(nop, nop, nop, nop, nop, nop),
		mutableListOf(nop, nop, nop, nop, nop, nop),
		mutableListOf(nop, nop, nop, nop, nop, nop),
		mutableListOf(nop, nop, nop, nop, nop, nop),
		mutableListOf(nop, nop, nop, nop, cancelButton)
	)
	if (grid == "numpad") {
		result = listOf(
			mutableListOf(
				BI("ShowAll", "SHOW", "Ignore decimal places setting temporarily and show all non-zero digits."),
				BI("SI", "SI", "Multiply X by a standard SI prefix."),
				BI("EngL", "ENG&larr;", "Adjust the exponent used to display the current value of X."),
				BI("EngR", "ENG&rarr;", "Adjust the exponent used to display the current value of X."),
				BI("pi", "&pi;", "&pi; (3.141...): push onto stack."),
				BI("backspace", "DEL", "Backspace: correct entered values.")
			),
			mutableListOf(
				BI("base", "BASE", "Switch between decimal, hexadecimal, binary and octal."),
				BI("random", "RAND", "Push a random number onto the stack."),
				BI("exponent", "EXP", "Add an exponent to the entered value."),
				BI("squareroot", "&radic;x", "Calculate the square-root of X."), // Available if needed as duplicated on funcpad
				BI("reciprocal", "1/x", "Calculate the reciprocal of X."),
				BI("divide", "&divide;", "Divide Y by X.")
			),
			mutableListOf(
				BI("store", "STO", "Store the current value of X for later use."),
				BI("drop", "DROP", "Pop X off the stack and discard it."),
				BI("7", "7"),
				BI("8", "8"),
				BI("9", "9"),
				BI("times", "&times;", "Multiply Y by X.")
			),
			mutableListOf(
				BI("recall", "RCL", "Recall a previously stored value and push it onto the stack."),
				BI("swap", "x &hArr; y", "Swap the values in X and Y."),
				BI("4", "4"),
				BI("5", "5"),
				BI("6", "6"),
				BI("minus", "&ndash;", "Subtract X from Y.")
			),
			mutableListOf(
				BI("undo", "UNDO", "Undo the last operation that modified the stack."),
				BI("invert", "&plusmn;", "Swap the sign of the current value. If currently entering an exponent, switch sign of the exponent."),
				BI("1", "1"),
				BI("2", "2"),
				BI("3", "3"),
				BI("plus", "+", "Add Y to X.")
			),
			mutableListOf(
				BI("clear", "CLEAR", "Press once to clear X, press again to clear the whole stack.", doubleWidth = true),
				BI("0", "0"),
				BI("dot", "&middot;"),
				BI("enter", "ENTER", "Push the entered value onto the stack.", doubleWidth = true)
			)
		)
		if ( ! getOption(CalcOpt.SaveHistory)) {
			result[4][0] = nop // remove undo if save history not set
		}
		if (dspBase == DisplayBase.baseHexadecimal) {
			result[0][2] = BI("D", "D")
			result[0][3] = BI("E", "E")
			result[0][4] = BI("F", "F")
			result[1][2] = BI("A", "A")
			result[1][3] = BI("B", "B")
			result[1][4] = BI("C", "C")
			result[5][2] = nop
		}
		else if (dspBase == DisplayBase.baseOctal) {
			for (row in 0..1) {
				for (col in 2..4) {
					result[row][col] = nop
				}
			}
			result[2][3] = nop
			result[2][4] = nop
			result[5][2] = nop
		}
		else if (dspBase == DisplayBase.baseBinary) {
			for (row in 0..3) {
				for (col in 2..4) {
					result[row][col] = nop
				}
			}
			result[4][3] = nop
			result[4][4] = nop
			result[5][2] = nop
		}
		else {
			// stet
		}
	}
	else if ((grid == "funcpad") || (grid == "altfuncpad")) {
		result = listOf(
			mutableListOf(
				BI("rollUp", "ROLL&uarr;", "Roll the stack up."),
				BI("tentox", "10<sup><small>x</small></sup>", "Raise 10 to the power of X."),
				BI("etox", "e<sup><small>x</small></sup>", "Raise e (Euler's number) to the power of X."),
				BI("twotox", "2<sup><small>x</small></sup>", "Raise 2 to the power of X."),
				BI("round", "ROUND", "Round X to the nearest integer."),
				BI("absolute", "ABS", "If X is negative, invert it, otherwise leave as is.")
			),
			mutableListOf(
				BI("rollDown", "ROLL&darr;", "Roll the stack down."),
				BI("log10", "log10", "Calculate the log (base 10) of X."),
				BI("loge", "ln", "Calculate the log (base e) of X."),
				BI("log2", "log2", "Calculate the log (base 2) of X."),
				BI("floor", "FLOOR", "Round down to the nearest integer."),
				BI("integerpart", "INT", "Remove the floating point part of X, leaving only the integer part - equivalent to FLOOR for X &gt; 0 or CEIL for X &lt; 0.")
			),
			mutableListOf(
				BI("squareroot", "&radic;x", "Calculate the square-root of X."),
				BI("cuberoot", "3&radic;x", "Calculate the cube-root of X."),
				BI("xrooty", "x&radic;y", "Calculate the X-root of Y."),
				BI("percent", "%", "Convert X to a percentage of Y."), // TODO: Check operation
				BI("ceiling", "CEIL", "Round up to the nearest integer."),
				BI("floatingpart", "FP", "Remove the integer part of X, leaving only the floating point part.")
			),
			mutableListOf(
				BI("square", "x<sup><small>2</small></sup>", "Calculate X&times;X."),
				BI("cube", "x<sup><small>3</small></sup>", "Calculate X&times;X&times;X"),
				BI("power", "y<sup><small>x</small></sup>", "Raise Y to the power of X."),
				BI("twoscomp", "s2c", "Calculate the signed two's complement of X."),
				BI("remainder", "REM", "Calculate the remainder after an integer division of Y / X."),
				BI("integerdivide", "INTDIV", "Perform an integer division Y / X.")
			),
			mutableListOf(
				BI("bitwiseand", "x & y", "Perform a bitwise AND on the integer part of X and Y."),
				BI("bitwiseor", "x | y", "Perform a bitwise OR on the integer part of X and Y."),
				BI("inversesin", "sin<sup><small>-1</small></sup>(x)", "Calculate the inverse sine of X."),
				BI("inversecos", "cos<sup><small>-1</small></sup>(x)", "Calculate the inverse cosine of X."),
				BI("inversetan", "tan<sup><small>-1</small></sup>(x)", "Calculate the inverse tangent of X."),
				BI("inversetan2", "tan<sup><small>-1</small></sup>(y/x)", "Calculate the inverse tangent of Y/X.")
			),
			mutableListOf(
				BI("bitwisenot", "~ x", "Perform a bitwise NOT on the integer part of X."),
				BI("bitwisexor", "x ^ y", "Perform a bitwise XOR on the integer part of X."),
				BI("sin", "sin(x)", "Calculate the sine of X."),
				BI("cos", "cos(x)", "Calculate the cosine of X."),
				BI("tan", "tan(x)", "Calculate the tangent of X."),
				BI("altmode", "ALT", "Show alternative functions (including hyperbolic trigonometry).")
			)
		)

		if (grid == "altfuncpad") {
			result[4][2] = BI("inversesinh", "sinh<sup><small>-1</small></sup>(x)", "Calculate the inverse hyperbolic sine of X.")
			result[4][3] = BI("inversecosh", "cosh<sup><small>-1</small></sup>(x)", "Calculate the inverse hyperbolic cosine of X.")
			result[4][4] = BI("inversetanh", "tanh<sup><small>-1</small></sup>(x)", "Calculate the inverse hyperbolic tangent of X.")
			result[4][5] = BI("inversetanh2", "tanh<sup><small>-1</small></sup>(y/x)", "Calculate the inverse hyperbolic tangent of Y/X.", scale=0.8f)
			result[5][2] = BI("sinh", "sinh(x)", "Calculate the hyperbolic sine of X.")
			result[5][3] = BI("cosh", "cosh(x)", "Calculate the hyperbolic cosine of X.")
			result[5][4] = BI("tanh", "tanh(x)", "Calculate the hyperbolic tangent of X.")

			// TODO: Check operation
			result[2][3] = BI("percentchange", "&Delta;%", "Calculate the percentage change of X vs Y.")
		}
	}
	else if (grid == "convpad") {
		result = listOf(
			mutableListOf(
				BI("Convert_Distance_Inches_Millimetres",                              st.unitSymbols["Millimetres"]+"&larr;",                "Convert Inches to Millimetres."),
				BI("Convert_Distance_Millimetres_Inches",                              "&rarr;"+st.unitSymbols["Inches"],                     "Convert Millimetres to Inches."),
				BI("Convert_Mass_Ounces_Grams",                                        st.unitSymbols["Grams"]+"&larr;",                      "Convert Ounces to Grams."),
				BI("Convert_Mass_Grams_Ounces",                                        "&rarr;"+st.unitSymbols["Ounces"],                     "Convert Grams to Ounces."),
				BI("Convert_Angle_Radians_Degrees",                                    st.unitSymbols["Degrees"]+"&larr;",                    "Convert Radians to Degrees."),
				BI("Convert_Angle_Degrees_Radians",                                    "&rarr;"+st.unitSymbols["Radians"],                    "Convert Degrees to Radians.")
			),
			mutableListOf(
				BI("Convert_Distance_Miles_Kilometres",                                st.unitSymbols["Kilometres"]+"&larr;",                 "Convert Miles to Kilometres."),
				BI("Convert_Distance_Kilometres_Miles",                                "&rarr;"+st.unitSymbols["Miles"],                      "Convert Kilometres to Miles."),
				BI("Convert_Mass_Pounds_Kilograms",                                    st.unitSymbols["Kilograms"]+"&larr;",                  "Convert Pounds to Kilograms."),
				BI("Convert_Mass_Kilograms_Pounds",                                    "&rarr;"+st.unitSymbols["Pounds"],                     "Convert Kilograms to Pounds."),
				BI("Convert_Angle_Degrees.Minutes-Seconds_Degrees",                    st.unitSymbols["Degrees"]+"&larr;",                    "Convert Hours.Minutes-Seconds (or Degrees.Minutes-Seconds) to Hours (or Degrees)."),
				BI("Convert_Angle_Degrees_Degrees.Minutes-Seconds",                    "&rarr;"+st.unitSymbols["Degrees.Minutes-Seconds"],    "Convert Hours (or Degrees) to Hours.Minutes-Seconds (or Degrees.Minutes-Seconds).")
			),
			mutableListOf(
				BI("Convert_Volume_Gallons_Litres",                                    st.unitSymbols["Litres"]+"&larr;",                     "Convert Gallons to Litres."),
				BI("Convert_Volume_Litres_Gallons",                                    "&rarr;"+st.unitSymbols["Gallons"],                    "Convert Litres to Gallons."),
				BI("Convert_Temperature_Fahrenheit_Celsius",                           st.unitSymbols["Celsius"]+"&larr;",                    "Convert Fahrenheit to Celsius."),
				BI("Convert_Temperature_Celsius_Fahrenheit",                           "&rarr;"+st.unitSymbols["Fahrenheit"],                 "Convert Celsius to Fahrenheit."),
				BI("Convert_Volume_Cubic Millimetres_Cubic Metres",                    st.unitSymbols["Cubic Metres"]+"&larr;",               "Convert Cubic Millimetres to Cubic Metres."),
				BI("Convert_Volume_Cubic Metres_Cubic Millimetres",                    "&rarr;"+st.unitSymbols["Cubic Millimetres"],          "Convert Cubic Metres to Cubic Millimetres.")
			),
			mutableListOf(
				BI("Convert_Volume_Pints_Litres",                                      st.unitSymbols["Litres"]+"&larr;",                     "Convert Pints to Litres."),
				BI("Convert_Volume_Litres_Pints",                                      "&rarr;"+st.unitSymbols["Pints"],                      "Convert Litres to Pints."),
				BI("Convert_Fuel Economy_Miles Per Litre_Miles Per Gallon",            st.unitSymbols["Miles Per Gallon"]+"&larr;",           "Convert Miles Per Litre to Miles Per Gallon."),
				BI("Convert_Fuel Economy_Miles Per Gallon_Miles Per Litre",            "&rarr;"+st.unitSymbols["Miles Per Litre"],            "Convert Miles Per Gallon to Miles Per Litre."),
				BI("Convert_Frequency_RPM_Hertz",                                      st.unitSymbols["Hertz"]+"&larr;",                      "Convert RPM to Hertz."),
				BI("Convert_Frequency_Hertz_RPM",                                      "&rarr;"+st.unitSymbols["RPM"],                        "Convert Hertz to RPM.")
			),
			mutableListOf(
				BI("Convert_Volume_Fluid Ounces_Litres",                               st.unitSymbols["Litres"]+"&larr;",                     "Convert Fluid Ounces to Litres."),
				BI("Convert_Volume_Litres_Fluid Ounces",                               "&rarr;"+st.unitSymbols["Fluid Ounces"],               "Convert Litres to Fluid Ounces."),
				BI("Convert_Fuel Economy_Litres Per 100 Kilometres_Miles Per Gallon",  st.unitSymbols["Miles Per Gallon"]+"&larr;",           "Convert Litres Per 100 Kilometres to Miles Per Gallon."),
				BI("Convert_Fuel Economy_Miles Per Gallon_Litres Per 100 Kilometres",  "&rarr;"+st.unitSymbols["Litres Per 100 Kilometres"],  "Convert Miles Per Gallon to Litres Per 100 Kilometres.", scale=0.8f),
				BI("Convert_Torque_Pounds Feet_Newton Metres",                         st.unitSymbols["Newton Metres"]+"&larr;",              "Convert Pound-Force Feet to Newton Metres."),
				BI("Convert_Torque_Newton Metres_Pounds Feet",                         "&rarr;"+st.unitSymbols["Pound-Force Feet"],           "Convert Newton Metres to Pound-Force Feet.")
			),
			mutableListOf(
				BI("Convert_Volume_US Pints_Pints",                                    st.unitSymbols["Pints"]+"&larr;",                      "Convert US Pints to Pints."),
				BI("Convert_Volume_Pints_US Pints",                                    "&rarr;"+st.unitSymbols["US Pints"],                   "Convert Pints to US Pints."),
				BI("Convert_Date_Day of Year_Date in Year",                            st.unitSymbols["Date in Year"]+"&larr;",               "Convert Day Number as NN.(YYYY) to Date as DD.MM(YYYY) - current year is assumed if not specified."),
				BI("Convert_Date_Date in Year_Day of Year",                            "&rarr;"+st.unitSymbols["Day of Year"],                "Convert Date as DD.MM(YYYY) to Day Number as NN.(YYYY) - current year is assumed if not specified."),
				BI("MoreConversions",                                                  "MORE",                                                doubleWidth = true)
			)
		)

		if (this.getOption(DispOpt.AmericanUnits)) {
			var us_change = mapOf(
				"Gallons" to "US Gallons",
				"Pints" to "US Pints",
				"Fluid Ounces" to "US Fluid Ounces",
				"Miles Per Gallon" to "Miles Per US Gallon"
			)
			for ((i, ml) in result.withIndex()) {
				for ((j, bi) in ml.withIndex()) {
					if (bi.name.contains("US ")) {
						// Already a US conversion
						continue
					}

					var parts = bi.name.split("_")
					if (parts.size < 4) {
						continue
					}
					var cat = parts[1]
					var from = parts[2]
					var to = parts[3]

					if (us_change.containsKey(from)) {
						from = us_change[from]!!
					}
					if (us_change.containsKey(to)) {
						to = us_change[to]!!
					}
					var disp: String
					if (bi.display.contains("&larr")) {
						disp = st.unitSymbols[to] + "&larr;"
					}
					else {
						disp = "&rarr;" + st.unitSymbols[to]
					}
					result[i][j] = BI("Convert_${cat}_${from}_${to}",
						disp,
						"Convert ${from} to ${to}",
						bi.scale)
				}
			}
		}
	}
	else if (grid == "constpad") {
		var vres: MutableList<MutableList<BI> > = mutableListOf()
		vres.add(mutableListOf())
		var colCount: Int = 0
		var totalCount: Int = 0
		for (k in st.constants) {
			if (colCount >= 6) {
				vres.add(mutableListOf())
				colCount = 0
			}
			var helptext = """${k.name} - <span style="white-space: nowrap;">"""
			helptext += formatDecimal(k.value, isX=false, constHelpMode=true)
			helptext += "&nbsp;${k.unit}</span>"

			vres.last().add(BI("Const-"+k.name, k.symbol, helptext))
			colCount += 1
			totalCount += 1
			if (totalCount >= 36) {
				break
			}
		}
		while (totalCount < 32) {
			if (colCount >= 6) {
				vres.add(mutableListOf())
				colCount = 0
			}
			vres.last().add(nop)
			colCount += 1
			totalCount += 1
		}
		vres.last().add(BI("DensityByName", "Material Density", doubleWidth=true))
		vres.last().add(BI("ConstByName", "Add By Name", doubleWidth=true))
		result = vres.toList()
	}
	else if (grid == "sipad") {
		result = listOf(
			mutableListOf(
				BI("NOP", "Prefixes > 1:", doubleWidth = true),
				BI("NOP", "Prefixes < 1:", doubleWidth = true),
				BI("NOP", "Binary Prefixes:", doubleWidth = true)
			),
			mutableListOf(
				BI("SI-Yotta", "Y", "SI prefix Yotta: multiply X by 1&times;10<sup><small>24</small></sup>."),
				BI("SI-Tera", "T", "SI prefix Tera: multiply X by 1&times;10<sup><small>12</small></sup>."),
				BI("SI-Milli", "m", "SI prefix Milli: multiply X by 1&times;10<sup><small>-3</small></sup>."),
				BI("SI-Femto", "f", "SI prefix Femto: multiply X by 1&times;10<sup><small>-15</small></sup>."),
				BI("SI-Exbi", "Ei", "SI prefix Exbi: multiply X by 2<sup><small>60</small></sup>."),
				BI("SI-Gibi", "Gi")
			),
			mutableListOf(
				BI("SI-Zetta", "Z", "SI prefix Zetta: multiply X by 1&times;10<sup><small>21</small></sup>."),
				BI("SI-Giga", "G", "SI prefix Giga: multiply X by 1&times;10<sup><small>9</small></sup>."),
				BI("SI-Micro", "&mu;", "SI prefix Micro: multiply X by 1&times;10<sup><small>-6</small></sup>."),
				BI("SI-Atto", "a", "SI prefix Atto: multiply X by 1&times;10<sup><small>-18</small></sup>."),
				BI("SI-Pebi", "Pi", "SI prefix Pebi: multiply X by 2<sup><small>50</small></sup>."),
				BI("SI-Mebi", "Mi")
			),
			mutableListOf(
				BI("SI-Exa", "E", "SI prefix Exa: multiply X by 1&times;10<sup><small>18</small></sup>."),
				BI("SI-Mega", "M", "SI prefix Mega: multiply X by 1&times;10<sup><small>6</small></sup>."),
				BI("SI-Nano", "n", "SI prefix Nano: multiply X by 1&times;10<sup><small>-9</small></sup>."),
				BI("SI-Zepto", "z", "SI prefix Zepto: multiply X by 1&times;10<sup><small>-21</small></sup>."),
				BI("SI-Tebi", "Ti", "SI prefix Tebi: multiply X by 2<sup><small>40</small></sup>."),
				BI("SI-Kibi", "Ki")
			),
			mutableListOf(
				BI("SI-Peta", "P", "SI prefix Peta: multiply X by 1&times;10<sup><small>15</small></sup>."),
				BI("SI-Kilo", "k", "SI prefix Kilo: multiply X by 1&times;10<sup><small>3</small></sup>."),
				BI("SI-Pico", "p", "SI prefix Pico: multiply X by 1&times;10<sup><small>-12</small></sup>."),
				BI("SI-Yocto", "y", "SI prefix Yocto: multiply X by 1&times;10<sup><small>-24</small></sup>."),
				nop,
				nop
			),
			mutableListOf(
				nop,
				nop,
				nop,
				nop,
				BI("CANCEL", "Cancel", doubleWidth = true)
			)
		)
	}
	else if (grid == "romanupperpad") {
		result = listOf(
			mutableListOf(
				BI("StoreRomanUpperA", "A", getStoreHelpText("StoreRomanUpperA")),
				BI("StoreRomanUpperB", "B", getStoreHelpText("StoreRomanUpperB")),
				BI("StoreRomanUpperC", "C", getStoreHelpText("StoreRomanUpperC")),
				BI("StoreRomanUpperD", "D", getStoreHelpText("StoreRomanUpperD")),
				BI("StoreRomanUpperE", "E", getStoreHelpText("StoreRomanUpperE")),
				BI("StoreRomanUpperF", "F", getStoreHelpText("StoreRomanUpperF"))
			),
			mutableListOf(
				BI("StoreRomanUpperG", "G", getStoreHelpText("StoreRomanUpperG")),
				BI("StoreRomanUpperH", "H", getStoreHelpText("StoreRomanUpperH")),
				BI("StoreRomanUpperI", "I", getStoreHelpText("StoreRomanUpperI")),
				BI("StoreRomanUpperJ", "J", getStoreHelpText("StoreRomanUpperJ")),
				BI("StoreRomanUpperK", "K", getStoreHelpText("StoreRomanUpperK")),
				BI("StoreRomanUpperL", "L", getStoreHelpText("StoreRomanUpperL"))
			),
			mutableListOf(
				BI("StoreRomanUpperM", "M", getStoreHelpText("StoreRomanUpperM")),
				BI("StoreRomanUpperN", "N", getStoreHelpText("StoreRomanUpperN")),
				BI("StoreRomanUpperO", "O", getStoreHelpText("StoreRomanUpperO")),
				BI("StoreRomanUpperP", "P", getStoreHelpText("StoreRomanUpperP")),
				BI("StoreRomanUpperQ", "Q", getStoreHelpText("StoreRomanUpperQ")),
				BI("StoreRomanUpperR", "R", getStoreHelpText("StoreRomanUpperR"))
			),
			mutableListOf(
				BI("StoreRomanUpperS", "S", getStoreHelpText("StoreRomanUpperS")),
				BI("StoreRomanUpperT", "T", getStoreHelpText("StoreRomanUpperT")),
				BI("StoreRomanUpperU", "U", getStoreHelpText("StoreRomanUpperU")),
				BI("StoreRomanUpperV", "V", getStoreHelpText("StoreRomanUpperV")),
				BI("StoreRomanUpperW", "W", getStoreHelpText("StoreRomanUpperW")),
				BI("StoreRomanUpperX", "X", getStoreHelpText("StoreRomanUpperX"))
			),
			mutableListOf(
				BI("StoreRomanUpperY", "Y", getStoreHelpText("StoreRomanUpperY")),
				BI("StoreRomanUpperZ", "Z", getStoreHelpText("StoreRomanUpperZ")),
				nop,
				nop,
				nop,
				nop
			),
			mutableListOf(
				nop,
				nop,
				nop,
				nop,
				cancelButton
			)
		)
	}
	else if (grid == "romanlowerpad") {
		result = listOf(
			mutableListOf(
				BI("StoreRomanLowerA", "a", getStoreHelpText("StoreRomanLowerA")),
				BI("StoreRomanLowerB", "b", getStoreHelpText("StoreRomanLowerB")),
				BI("StoreRomanLowerC", "c", getStoreHelpText("StoreRomanLowerC")),
				BI("StoreRomanLowerD", "d", getStoreHelpText("StoreRomanLowerD")),
				BI("StoreRomanLowerE", "e", getStoreHelpText("StoreRomanLowerE")),
				BI("StoreRomanLowerF", "f", getStoreHelpText("StoreRomanLowerF"))
			),
			mutableListOf(
				BI("StoreRomanLowerG", "g", getStoreHelpText("StoreRomanLowerG")),
				BI("StoreRomanLowerH", "h", getStoreHelpText("StoreRomanLowerH")),
				BI("StoreRomanLowerI", "i", getStoreHelpText("StoreRomanLowerI")),
				BI("StoreRomanLowerJ", "j", getStoreHelpText("StoreRomanLowerJ")),
				BI("StoreRomanLowerK", "k", getStoreHelpText("StoreRomanLowerK")),
				BI("StoreRomanLowerL", "l", getStoreHelpText("StoreRomanLowerL"))
			),
			mutableListOf(
				BI("StoreRomanLowerM", "m", getStoreHelpText("StoreRomanLowerM")),
				BI("StoreRomanLowerN", "n", getStoreHelpText("StoreRomanLowerN")),
				BI("StoreRomanLowerO", "o", getStoreHelpText("StoreRomanLowerO")),
				BI("StoreRomanLowerP", "p", getStoreHelpText("StoreRomanLowerP")),
				BI("StoreRomanLowerQ", "q", getStoreHelpText("StoreRomanLowerQ")),
				BI("StoreRomanLowerR", "r", getStoreHelpText("StoreRomanLowerR"))
			),
			mutableListOf(
				BI("StoreRomanLowerS", "s", getStoreHelpText("StoreRomanLowerS")),
				BI("StoreRomanLowerT", "t", getStoreHelpText("StoreRomanLowerT")),
				BI("StoreRomanLowerU", "u", getStoreHelpText("StoreRomanLowerU")),
				BI("StoreRomanLowerV", "v", getStoreHelpText("StoreRomanLowerV")),
				BI("StoreRomanLowerW", "w", getStoreHelpText("StoreRomanLowerW")),
				BI("StoreRomanLowerX", "x", getStoreHelpText("StoreRomanLowerX"))
			),
			mutableListOf(
				BI("StoreRomanLowerY", "y", getStoreHelpText("StoreRomanLowerY")),
				BI("StoreRomanLowerZ", "z", getStoreHelpText("StoreRomanLowerZ")),
				nop,
				nop,
				nop,
				nop
			),
			mutableListOf(
				nop,
				nop,
				nop,
				nop,
				cancelButton
			)
		)
	}
	else if (grid == "greekupperpad") {
		result = listOf(
			mutableListOf(
				BI("StoreGreekUpperAlpha",    "&Alpha;", getStoreHelpText("StoreGreekUpperAlpha")),
				BI("StoreGreekUpperBeta",     "&Beta;", getStoreHelpText("StoreGreekUpperBeta")),
				BI("StoreGreekUpperGamma",    "&Gamma;", getStoreHelpText("StoreGreekUpperGamma")),
				BI("StoreGreekUpperDelta",    "&Delta;", getStoreHelpText("StoreGreekUpperDelta")),
				BI("StoreGreekUpperEpsilon",  "&Epsilon;", getStoreHelpText("StoreGreekUpperEpsilon")),
				BI("StoreGreekUpperZeta",     "&Zeta;", getStoreHelpText("StoreGreekUpperZeta"))
				),
			mutableListOf(
				BI("StoreGreekUpperEta",      "&Eta;", getStoreHelpText("StoreGreekUpperEta")),
				BI("StoreGreekUpperTheta",    "&Theta;", getStoreHelpText("StoreGreekUpperTheta")),
				BI("StoreGreekUpperIota",     "&Iota;", getStoreHelpText("StoreGreekUpperIota")),
				BI("StoreGreekUpperKappa",    "&Kappa;", getStoreHelpText("StoreGreekUpperKappa")),
				BI("StoreGreekUpperLambda",   "&Lambda;", getStoreHelpText("StoreGreekUpperLambda")),
				BI("StoreGreekUpperMu",       "&Mu;", getStoreHelpText("StoreGreekUpperMu"))
				),
			mutableListOf(
				BI("StoreGreekUpperNu",       "&Nu;", getStoreHelpText("StoreGreekUpperNu")),
				BI("StoreGreekUpperXi",       "&Xi;", getStoreHelpText("StoreGreekUpperXi")),
				BI("StoreGreekUpperOmicron",  "&Omicron;", getStoreHelpText("StoreGreekUpperOmicron")),
				BI("StoreGreekUpperPi",       "&Pi;", getStoreHelpText("StoreGreekUpperPi")),
				BI("StoreGreekUpperRho",      "&Rho;", getStoreHelpText("StoreGreekUpperRho")),
				BI("StoreGreekUpperSigma",    "&Sigma;", getStoreHelpText("StoreGreekUpperSigma"))
				),
			mutableListOf(
				BI("StoreGreekUpperTau",      "&Tau;", getStoreHelpText("StoreGreekUpperTau")),
				BI("StoreGreekUpperUpsilon",  "&Upsilon;", getStoreHelpText("StoreGreekUpperUpsilon")),
				BI("StoreGreekUpperPhi",      "&Phi;", getStoreHelpText("StoreGreekUpperPhi")),
				BI("StoreGreekUpperChi",      "&Chi;", getStoreHelpText("StoreGreekUpperChi")),
				BI("StoreGreekUpperPsi",      "&Psi;", getStoreHelpText("StoreGreekUpperPsi")),
				BI("StoreGreekUpperOmega",    "&Omega;", getStoreHelpText("StoreGreekUpperOmega"))
			),
			mutableListOf(
				nop,
				nop,
				nop,
				nop,
				nop,
				nop
			),
			mutableListOf(
				nop,
				nop,
				nop,
				nop,
				cancelButton
			)
		)
	}
	else if (grid == "greeklowerpad") {
		result = listOf(
			mutableListOf(
				BI("StoreGreekLowerAlpha",    "&alpha;", getStoreHelpText("StoreGreekLowerAlpha")),
				BI("StoreGreekLowerBeta",     "&beta;", getStoreHelpText("StoreGreekLowerBeta")),
				BI("StoreGreekLowerGamma",    "&gamma;", getStoreHelpText("StoreGreekLowerGamma")),
				BI("StoreGreekLowerDelta",    "&delta;", getStoreHelpText("StoreGreekLowerDelta")),
				BI("StoreGreekLowerEpsilon",  "&epsilon;", getStoreHelpText("StoreGreekLowerEpsilon")),
				BI("StoreGreekLowerZeta",     "&zeta;", getStoreHelpText("StoreGreekLowerZeta"))
				),
			mutableListOf(
				BI("StoreGreekLowerEta",      "&eta;", getStoreHelpText("StoreGreekLowerEta")),
				BI("StoreGreekLowerTheta",    "&theta;", getStoreHelpText("StoreGreekLowerTheta")),
				BI("StoreGreekLowerIota",     "&iota;", getStoreHelpText("StoreGreekLowerIota")),
				BI("StoreGreekLowerKappa",    "&kappa;", getStoreHelpText("StoreGreekLowerKappa")),
				BI("StoreGreekLowerLambda",   "&lambda;", getStoreHelpText("StoreGreekLowerLambda")),
				BI("StoreGreekLowerMu",       "&mu;", getStoreHelpText("StoreGreekLowerMu"))
				),
			mutableListOf(
				BI("StoreGreekLowerNu",       "&nu;", getStoreHelpText("StoreGreekLowerNu")),
				BI("StoreGreekLowerXi",       "&xi;", getStoreHelpText("StoreGreekLowerXi")),
				BI("StoreGreekLowerOmicron",  "&omicron;", getStoreHelpText("StoreGreekLowerOmicron")),
				BI("StoreGreekLowerPi",       "&pi;", getStoreHelpText("StoreGreekLowerPi")),
				BI("StoreGreekLowerRho",      "&rho;", getStoreHelpText("StoreGreekLowerRho")),
				BI("StoreGreekLowerSigma",    "&sigma;", getStoreHelpText("StoreGreekLowerSigma"))
				),
			mutableListOf(
				BI("StoreGreekLowerTau",      "&tau;", getStoreHelpText("StoreGreekLowerTau")),
				BI("StoreGreekLowerUpsilon",  "&upsilon;", getStoreHelpText("StoreGreekLowerUpsilon")),
				BI("StoreGreekLowerPhi",      "&phi;", getStoreHelpText("StoreGreekLowerPhi")),
				BI("StoreGreekLowerChi",      "&chi;", getStoreHelpText("StoreGreekLowerChi")),
				BI("StoreGreekLowerPsi",      "&psi;", getStoreHelpText("StoreGreekLowerPsi")),
				BI("StoreGreekLowerOmega",    "&omega;", getStoreHelpText("StoreGreekLowerOmega"))
			),
			mutableListOf(
				nop,
				nop,
				nop,
				nop,
				nop,
				nop
			),
			mutableListOf(
				nop,
				nop,
				nop,
				nop,
				cancelButton
			)
		)
	}
	return result
}
