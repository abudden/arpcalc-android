package uk.co.cgtk.karpcalc

import java.util.Scanner
import java.util.zip.ZipInputStream

enum class DisplayBase {
	baseDecimal,
	baseHexadecimal,
	baseOctal,
	baseBinary,
}

data class DisplayState(
	var forcedEngDisplay: Boolean = false,
	var forcedEngFactor: Long = 0,
	var enteredText: String = "",
	var enteredValue: AF = A("0.0"),
	var entering: Boolean = false,
	var justPressedEnter: Boolean = true,
	var justPressedBase: Boolean = false,
	var showAll: Boolean = false
)

data class ValueAndExp(
	var value: AF,
	var exponent: Int
)

enum class DispOpt {
	EngNotation,
	BinaryPrefixes,
	PowerExponentView,
	SINotation,
	TrimZeroes,
	AlwaysShowDecimal,
	ThousandsSeparator,
	SpaceAsThousandsSeparator,
	SaveStackOnExit,
	SaveBaseOnExit,
	DecimalBaseBias,
	AmericanUnits,
	EuropeanDecimal,
	ShowHelpOnStart
	// If adding any here, make sure they're added to getDispOptionByName
	// and the various settings saving places.
}

data class DisplayOptions(
	var bOptions: MutableSet<DispOpt> = mutableSetOf<DispOpt>(),
	var decimalPlaces: Int = 7,
	var expNegMinDisplay: Int = -3,
	var expPosMaxDisplay: Int = 7
)

class CommandHandler(
	var dspOptions: DisplayOptions = DisplayOptions()
) {
	var dspState = DisplayState()
	var dspBase: DisplayBase = DisplayBase.baseDecimal
	var st = Stack()
	var varStore: MutableMap<String, AF> = mutableMapOf()
	var varStoreChanged: Boolean = false
	var SIDecimalPrefixes: LinkedHashMap<String, Int> = linkedMapOf()
	var SIBinaryPrefixes: LinkedHashMap<String, Int> = linkedMapOf()
	var SISymbols: LinkedHashMap<String, String> = linkedMapOf()
	var last_currency_date: String = ""

	init {
		this.initialiseSIPrefixes()
	}

	fun varStoreHasChanged(): Boolean {
		return varStoreChanged
	}

	fun loadVarStore(stored: Map<String, AF>) {
		varStore = stored.toMutableMap()
	}

	fun setOption(o: CalcOpt, v: Boolean = true) {
		st.setOption(o, v)
	}
	fun getOption(o: CalcOpt) : Boolean {
		return st.getOption(o)
	}
	fun toggleOption(o: CalcOpt) {
		setOption(o, ! getOption(o))
	}

	fun setPlaces(v: Int) {
		dspOptions.decimalPlaces = v
	}
	fun getPlaces(): Int {
		return dspOptions.decimalPlaces
	}

	fun setOption(o: DispOpt, v: Boolean = true) {
		if (v) {
			dspOptions.bOptions.add(o)
		}
		else {
			dspOptions.bOptions.remove(o)
		}
	}

	fun getOption(o: DispOpt) : Boolean {
		return dspOptions.bOptions.contains(o)
	}
	fun toggleOption(o: DispOpt) {
		setOption(o, ! getOption(o))
	}

	fun startEntering() {
		if (dspState.justPressedEnter) {
			if ( ! dspState.entering ) {
				st.pop()
			}
			dspState.justPressedEnter = false
			dspState.entering = false
		}
		if (dspState.entering) {
			if (dspState.enteredText.length > 0) {
				return
			}
		}
		dspState.enteredText = ""
		dspState.entering = true
	}

	fun completeEntering(needValue: Boolean) {
		dspState.justPressedEnter = false
		if ( ! dspState.entering) {
			return
		}
		if (needValue || (dspState.enteredText.length > 0)) {
			st.push(dspState.enteredValue)
		}
		dspState.entering = false
		dspState.enteredText = ""
	}

	fun updateValueFromText() {
		if (dspState.enteredText.length == 0) {
			dspState.enteredValue = A("0.0")
		}
		else if (isDecimal()) {
			if (getOption(DispOpt.BinaryPrefixes) && dspState.enteredText.contains("e")) {
				var parts = dspState.enteredText.split("e")
				dspState.enteredValue = parts[0].toAF() * (pow(2.0, parts[1].toAF()))
			}
			else {
				dspState.enteredValue = dspState.enteredText.toAF()
			}
		}
		else if (dspBase == DisplayBase.baseHexadecimal) {
			dspState.enteredValue = dspState.enteredText.toLong(16).toAF()
		}
		else if (dspBase == DisplayBase.baseBinary) {
			dspState.enteredValue = dspState.enteredText.toLong(2).toAF()
		}
		else if (dspBase == DisplayBase.baseOctal) {
			dspState.enteredValue = dspState.enteredText.toLong(8).toAF()
		}
		else {
			/* ??? */
			assert(false)
		}
	}

	fun clearButton() {
		if (dspState.justPressedEnter) {
			dspState.enteredText = ""
			dspState.enteredValue = A("0.0")
			dspState.justPressedEnter = false
		}
		else if (dspState.entering) {
			if ((dspState.enteredText.length == 0) || (dspState.enteredValue == A(0.0))) {
				st.saveHistory()
				st.clear()
			}
		}
		else if (st.peek() == A(0.0)) {
			st.saveHistory()
			st.clear()
		}
		else {
			st.saveHistory()
			st.pop()
		}
		dspState.entering = true
		dspState.enteredText = ""
		updateValueFromText()
	}

	fun numInput(value: String) {
		startEntering()
		if ((dspBase == DisplayBase.baseBinary) && (value.toInt() > 1)) {
			return
		}
		if ((dspBase == DisplayBase.baseOctal) && (value.toInt() > 7)) {
			return
		}
		if (dspState.enteredText.endsWith("e0")) {
			dspState.enteredText = dspState.enteredText.dropLast(1)
		}
		if ((dspState.enteredText != "") || (value != "0")) {
			dspState.enteredText += value.toString()
		}
		updateValueFromText()
	}

	fun hexInput(value: String) {
		if (dspBase == DisplayBase.baseHexadecimal) {
			startEntering()
			if ((dspState.enteredText != "") || (value != "0")) {
				dspState.enteredText += value
			}
			updateValueFromText()
		}
	}

	fun dotInput() {
		if ( ! isDecimal()) {
			return
		}
		if (dspState.enteredText.contains("e")) {
			return
		}
		if (dspState.enteredText.contains(".")) {
			return
		}
		startEntering()
		if (dspState.enteredText.length == 0) {
			dspState.enteredText = "0"
		}
		dspState.enteredText += "."
		updateValueFromText()
	}

	fun enter() {
		st.saveHistory()
		if (dspState.entering) {
			st.push(dspState.enteredValue)
			dspState.enteredText = ""
		}
		else {
			st.push(st.peek())
		}
		dspState.justPressedEnter = true
	}

	fun exponent() {
		if (! isDecimal()) {
			return
		}
		if (dspState.enteredText.contains("e")) {
			return
		}
		startEntering()
		if (dspState.enteredText.length == 0) {
			dspState.enteredText = "1"
		}
		dspState.enteredText += "e0"
		updateValueFromText()
	}

	fun backspace() {
		if (dspState.entering && ( ! dspState.justPressedEnter)) {
			var l = dspState.enteredText.length
			if ( l == 1 ) {
				clearButton()
			}
			else {
				if ((l > 1) && (dspState.enteredText[l-2] == 'e')) {
					dspState.enteredText = dspState.enteredText.dropLast(2)
				}
				else if ((l > 2) && (dspState.enteredText[l-3] == 'e') && (dspState.enteredText[l-2] == '-')) {
					dspState.enteredText = dspState.enteredText.dropLast(3)
				}
				else {
					dspState.enteredText = dspState.enteredText.dropLast(1)
				}
				updateValueFromText()
			}
		}
		else {
			clearButton()
		}
	}

	fun plusMinus() {
		if (dspState.entering && ( ! dspState.justPressedEnter)) {
			if (dspState.enteredText.length == 0) {
				/* Do nothing */
			}
			else if (dspState.enteredText.contains("e")) {
				var parts = dspState.enteredText.split("e")
				if (parts[1].startsWith('-')) {
					dspState.enteredText = parts[0] + "e" + parts[1].drop(1)
				}
				else {
					dspState.enteredText = parts[0] + "e-" + parts[1]
				}
			}
			else {
				if (dspState.enteredText.startsWith('-')) {
					dspState.enteredText = dspState.enteredText.drop(1)
				}
				else {
					dspState.enteredText = "-" + dspState.enteredText
				}
			}
			updateValueFromText()
		}
		else {
			completeEntering(true)
			st.saveHistory()
			st.invert()
		}
	}

	fun getXValue(): AF {
		if (dspState.entering && ( ! dspState.justPressedEnter)) {
			return dspState.enteredValue
		}
		else {
			return st.peek()
		}
	}

	fun getXDisplay(): String {
		var xValue: AF = getXValue()
		var formattedX: String

		if ( ! isDecimal() ) {
			formattedX = formatBase(xValue, dspBase, isX = true)
			return formattedX
		}

		// Assume decimal (sets up xValue)
		if (dspState.entering && ( ! dspState.justPressedEnter)) {
			var t: String = dspState.enteredText
			if (t.length == 0) {
				formattedX = formatDecimal(A(0.0), isX = true)
			}
			else {
				formattedX = formatEnteredText(t)
			}
		}
		else {
			formattedX = formatDecimal(xValue, isX = true)
		}

		return formattedX
	}

	fun getStackDisplay(): String {
		var sd  = st.getStackForDisplay()
		if (dspState.entering) {
			sd.add(0, dspState.enteredValue)
		}
		var l: Int = sd.size
		var result: String = ""
		var prefixes: MutableList<String> = mutableListOf("X", "Y", "Z")
		if (st.getOption(CalcOpt.ReplicateStack)) {
			if (l > 4) {
				l = 4
			}
			prefixes.add("T")
		}
		for (i in 1 until l) { /* exclusive end range - i=1;i<l;i++ */
			var entry: String
			if (result != "") {
				result = "<br>" + result
			}
			if (i < prefixes.size) {
				entry = prefixes[i] + ": "
			}
			else {
				entry = "S(${i-prefixes.size}): "
			}
			if (isDecimal()) {
				entry += formatDecimal(sd[i], isX = false)
			}
			else {
				entry += formatBase(sd[i], dspBase)
			}
			result = entry + result
		}
		return result
	}

	fun getStatusAngularUnits() : String {
		if (st.getOption(CalcOpt.Radians)) {
			return "RAD"
		}
		else {
			return "DEG"
		}
	}

	fun getStatusBase() : String {
		return when (dspBase) {
			DisplayBase.baseDecimal -> "DEC"
			DisplayBase.baseHexadecimal -> "HEX"
			DisplayBase.baseBinary -> "BIN"
			DisplayBase.baseOctal -> "OCT"
		}
	}

	fun setStatusBase(b: String) {
		dspBase = when (b) {
			"DEC", "decimal" -> DisplayBase.baseDecimal
			"HEX", "hexadecimal" -> DisplayBase.baseHexadecimal
			"BIN", "binary" -> DisplayBase.baseBinary
			"OCT", "octal" -> DisplayBase.baseOctal
			else -> DisplayBase.baseDecimal
		}
	}

	fun getStatusExponent() : String {
		if (getOption(DispOpt.BinaryPrefixes)) {
			return "EXP:2"
		}
		else {
			return "EXP:10"
		}
	}

	fun getBaseDisplay() : String {
		var xValue = getXValue();

		var bc = when(getBitCount()) {
			BitCount.bc8  -> 8
			BitCount.bc16 -> 16
			BitCount.bc32 -> 32
			BitCount.bc64 -> 64
		}

		var result: String = ""
		result += "As ${bc}-bit integer:<br><br>"
		result += "Dec:${formatBase(xValue, DisplayBase.baseDecimal)}<br>"
		result += "Hex:${formatBase(xValue, DisplayBase.baseHexadecimal)}<br>"
		result += "Bin:${formatBase(xValue, DisplayBase.baseBinary)}<br>"
		result += "Oct:${formatBase(xValue, DisplayBase.baseOctal)}"
		return result
	}

	fun nextBase() {
		completeEntering(false)
		if (getOption(DispOpt.DecimalBaseBias) && ( ! dspState.justPressedBase) && (dspBase != DisplayBase.baseDecimal)) {
			dspBase = DisplayBase.baseDecimal
		}
		else {
			dspBase = when (dspBase) {
				DisplayBase.baseDecimal     -> DisplayBase.baseHexadecimal
				DisplayBase.baseHexadecimal -> DisplayBase.baseBinary
				DisplayBase.baseBinary      -> DisplayBase.baseOctal
				DisplayBase.baseOctal       -> DisplayBase.baseDecimal
			}
		}
		dspState.justPressedBase = true
	}

	fun selectBase(base: DisplayBase) {
		completeEntering(false)
		dspBase = base
		dspState.justPressedBase = true
	}

	fun nextWindowSize() {
		sizeName = when(sizeName) {
			"Large"  -> "Medium"
			"Medium" -> "Small"
			"Small"  -> "Large"
			else     -> "Large"
		}
	}

	fun nextPlaces() {
		var v: Int = getPlaces()
		v += 1
		if ((v > 9) || (v < 0)) {
			v = 0
		}
		setPlaces(v)
	}

	fun setBitCount(v: BitCount) {
		st.bitCount = v
	}

	fun getBitCount(): BitCount {
		return st.bitCount
	}

	fun nextBitCount() {
		st.bitCount = when (st.bitCount) {
			BitCount.bc8  -> BitCount.bc16
			BitCount.bc16 -> BitCount.bc32
			BitCount.bc32 -> BitCount.bc64
			BitCount.bc64 -> BitCount.bc8
		}
	}

	fun getDisplayContents(): Map<String, String> {
		var result: Map<String, String> = mapOf()
		return result
	}

	fun optionsChanged() {
		completeEntering(false)
	}

	fun getParseableX(): String {
		return getXValue().toString()
	}

	fun isDecimal() : Boolean {
		if (dspBase == DisplayBase.baseDecimal) {
			return true
		}
		else {
			return false
		}
	}

	fun isHexadecimal() : Boolean {
		if (dspBase == DisplayBase.baseHexadecimal) {
			return true
		}
		else {
			return false
		}
	}

	fun store(name: String) {
		completeEntering(false)
		dspState.showAll = false
		varStore[name] = getXValue()
		varStoreChanged = true
	}

	fun recall(name: String) {
		var v = A(0)
		dspState.showAll = false
		completeEntering(false)
		if (varStore.containsKey(name)) {
			// !! forces it to not be null as we've
			// checked the varStore has the key
			v = varStore[name]!!
		}
		st.push(v)
	}

	fun keypresses(charKeys: String) : ErrorCode {
		return keypresses(charKeys.toList().map { Character.toString(it) } )
	}

	fun keypresses(commands: List<String>): ErrorCode {
		var ec = ErrorCode.NoError
		for (command in commands) {
			// This is potentially flaky if new commands are
			// added that start with a digit, but in practice
			// this is only likely to be used in test harnesses
			// so not too much of a problem
			if (command.first().isDigit() && (command.length != 1)) {
				ec = keypresses(command)
			}
			else {
				ec = keypress(command)
			}
			if (ec != ErrorCode.NoError) {
				break
			}
		}
		return ec
	}

	fun keypress(key: String) : ErrorCode {
		var handled = true
		dspState.showAll = false
		if (key != "base") {
			dspState.justPressedBase = false
		}

		if ((key != "EngL") && (key != "EngR")) {
			dspState.forcedEngDisplay = false
		}

		if (key.length == 1) {
			when (key[0]) {
				in '0'..'9' -> numInput(key)
				in 'A'..'D', in 'a'..'d', 'f', 'F' -> hexInput(key.toUpperCase())
				'E', 'e' -> {
					//println("E pressed")
					if (isDecimal()) {
						//println("Exponent")
						exponent()
					}
					else if (dspBase == DisplayBase.baseHexadecimal) {
						//println("Hex input")
						hexInput("E")
					}
					else {
						/* Do nothing */
					}
				}
				'.' -> dotInput()
				else -> handled = false
			}
		}
		else {
			when (key) {
				"backspace"           -> backspace()
				"plusMinus", "invert" -> plusMinus()
				"clear"               -> clearButton()
				"enter"               -> enter()
				"exponent"            -> exponent()
				"display"             -> println(st.peek().toString())
				"dot"                 -> dotInput()
				"EngL"                -> engRotate(-1)
				"EngR"                -> engRotate(1)
				"ShowAll"             -> dspState.showAll = true
				"base"                -> nextBase()
				 else -> handled = false
			}
		}

		if (handled) {
			return ErrorCode.NoError
		}

		if (key.startsWith("Const-")) {
			completeEntering(false)
			st.saveHistory()
			return st.constant(key.substring(6))
		}
		if (key.startsWith("Density-")) {
			completeEntering(false)
			st.saveHistory()
			return st.density(key.substring(8))
		}
		if (key.startsWith("SI-")) {
			completeEntering(true)
			st.saveHistory()
			return SI(key.drop(3))
		}

		if (key.startsWith("Convert_")) {
			completeEntering(true)
			st.saveHistory()
			var convertParts = key.split("_")
			return st.convert(convertParts[1], convertParts[2], convertParts[3])
		}

		var takesValue = true
		val noValueKeys = setOf(
			"p", "pi", "random", "rUp", "rDown",
			"undo", "u"
		)
		if (noValueKeys.contains(key)) {
			takesValue = false
		}

		if (dspState.entering && dspState.enteredText.endsWith("e0")) {
			if ((key == "minus") || (key == "-")) {
				plusMinus();
				return ErrorCode.NoError;
			}
		}

		val UNSET_CALLBACK: () -> ErrorCode = { ErrorCode.NoFunction }
		var stackFunc: (() -> ErrorCode) = when (key) {
			"absolute"                   -> st::absolute
			"bitwiseand"                 -> st::bitwiseand
			"bitwisenot"                 -> st::bitwisenot
			"bitwiseor"                  -> st::bitwiseor
			"bitwisexor"                 -> st::bitwisexor
			"twoscomp"                   -> st::twoscomplement
			"ceiling"                    -> st::ceiling
			"pi"                         -> { completeEntering(false) ; return st.constant("Pi") }
			"cos"                        -> st::cos
			"cosh"                       -> st::cosh
			"cube"                       -> st::cube
			"cuberoot"                   -> st::cuberoot
			"/", "divide"                -> st::divide
			"drop"                       -> st::drop
			"etox"                       -> st::etox
			"floatingpart"               -> st::floatingpart
			"floor"                      -> st::floor
			"integerdivide"              -> st::integerdivide
			"integerpart"                -> st::integerpart
			"inversecos"                 -> st::inversecos
			"inversesin"                 -> st::inversesin
			"inversetan"                 -> st::inversetan
			"inversetan2"                -> st::inversetan2
			"inversecosh"                -> st::inversecosh
			"inversesinh"                -> st::inversesinh
			"inversetanh"                -> st::inversetanh
			"inversetanh2"               -> st::inversetanh2
			"log10"                      -> st::log10
			"log2"                       -> st::log2
			"loge", "ln"                 -> st::loge
			"-", "minus"                 -> st::minus
			"%", "percent"               -> st::percent
			"percentchange"              -> st::percentchange
			"+", "plus"                  -> st::plus
			"^", "power"                 -> st::power
			"random"                     -> st::random
			"reciprocal"                 -> st::reciprocal
			"remainder"                  -> st::remainder
			"rollDown"                   -> st::rollDown
			"rollUp"                     -> st::rollUp
			"round"                      -> st::round
			"sin"                        -> st::sin
			"sinh"                       -> st::sinh
			"square"                     -> st::square
			"r", "sqrt", "squareroot"    -> st::squareroot
			"w", "swap"                  -> st::swap
			"tan"                        -> st::tan
			"tanh"                       -> st::tanh
			"tentox"                     -> st::tentox
			"twotox"                     -> st::twotox
			"*", "times"                 -> st::times
			"u", "undo"                  -> st::undo
			"xrooty"                     -> st::xrooty
			else -> UNSET_CALLBACK
		}

		if (stackFunc !== UNSET_CALLBACK) {
			completeEntering(takesValue)
		}
		if ((key != "undo") && (key != "u")) {
			st.saveHistory()
		}
		var ec = stackFunc()
		
		return ec
	}

	fun addPeriodic(source: String, spacing: Int, insertion: String): String {
		var new = source.takeLast(spacing)
		var old = source
		old = old.dropLast(spacing)
		while (old.length > 0) {
			new = old.takeLast(spacing) + insertion + new
			old = old.dropLast(spacing)
		}
		return new
	}

	fun addThinSpaces(source: String, spacing: Int = 4): String {
		return addPeriodic(source, spacing, "&thinsp;")
	}

	fun addThousandsSeparator(source: String, spacing: Int = 3): String {
		var prefix = ""
		var suffix = ""
		var modify = source
		var separator = ","
		if (getOption(DispOpt.SpaceAsThousandsSeparator)) {
			separator = "&nbsp;"
		}
		if (source.startsWith("-")) {
			prefix = "-"
			modify = source.substring(1)
		}
		if (modify.contains(".")) {
			var parts = modify.split(".")
			suffix = "." + parts[1]
			modify = parts[0]
		}
		else if (modify.contains("e")) {
			var parts = modify.split("e")
			suffix = "e" + parts[1]
			modify = parts[0]
		}
		else {
		}

		var result = prefix
		result += addPeriodic(modify, spacing, separator)
		result += suffix
		return result
	}

	fun europeanDecimal(source: String): String {
		// Swap dot and comma
		var modified = source.replace(".", "__DECIMAL__")
		modified = modified.replace(",", ".")
		modified = modified.replace("__DECIMAL__", ",")
		return modified
	}

	fun formatBase(value: AF, base: DisplayBase, isX: Boolean = false): String {
		
		var bn: Int = 10
		var prefix: String

		when (base) {
			DisplayBase.baseDecimal     -> { bn = 10; prefix = "" }
			DisplayBase.baseHexadecimal -> { bn = 16; prefix = "0x" }
			DisplayBase.baseBinary      -> { bn =  2; prefix = "0b" }
			DisplayBase.baseOctal       -> { bn =  8; prefix = "0o" }
		}

		var formatted: String
		var asLong = value.toLong()
		var mask = st.getBitMask()
		if ((asLong >= 0) || (base == DisplayBase.baseDecimal)) {
			if ( ! isX) {
				asLong = asLong and mask
			}
			formatted = "${asLong.toString(bn)}".toUpperCase()
		}
		else {
			var absolute = Math.abs(asLong)
			var twoscomp = absolute.inv() + 1
			if ( ! isX) {
				twoscomp = twoscomp and mask
			}
			formatted = "${twoscomp.toString(bn)}".toUpperCase()
		}
		if (base == DisplayBase.baseBinary) {
			while ((formatted.length % 4) != 0) {
				formatted = "0" + formatted
			}
		}
		if (base != DisplayBase.baseDecimal) {
			formatted = addThinSpaces(formatted)
		}
		return prefix + formatted
	}

	fun getValueAndExponent(value: AF, isX: Boolean): ValueAndExp {
		var result = ValueAndExp(A(0.0), 0)
		var exponent: Long = 0
		var absolute: AF = value.abs()
		var realnumber: AF = value
		var places: Int = dspOptions.decimalPlaces

		if ((absolute != A(0.0)) && (getOption(DispOpt.BinaryPrefixes))) {
			var binExpNegMinDisplay =
				Math.floor(Math.log10(Math.pow(10.0, dspOptions.expNegMinDisplay.toDouble()))/Math.log10(2.0))
			var binExpPosMaxDisplay =
				Math.floor(Math.log10(Math.pow(10.0, dspOptions.expPosMaxDisplay.toDouble()))/Math.log10(2.0))
			if ((isX && dspState.forcedEngDisplay)
				|| ((absolute < pow(2.0, binExpNegMinDisplay))
					|| (absolute >= pow(2.0, binExpPosMaxDisplay)))) {
				if (isX && dspState.forcedEngDisplay) {
					exponent = dspState.forcedEngFactor
				}
				else {
					exponent = AF.round(AF.floor(AF.log10(absolute)/Math.log10(2.0))).toLong()
					exponent = 10 * AF.round(AF.floor(exponent.toAF() / 10.0)).toLong()
				}
				realnumber /= pow(2.0, exponent.toAF())

				// Check whether rounding bumps exponent
				if ( ! dspState.showAll) {
					realnumber = RoundToDecimalPlaces(realnumber, places)
				}
				absolute = abs(realnumber)

				if (getOption(DispOpt.EngNotation) && ( ! dspState.forcedEngDisplay)
						&& (absolute >= 1024.0)) {
					realnumber /= 1024.0
					exponent += 10
				}
				else if (( ! ( getOption(DispOpt.EngNotation) || dspState.forcedEngDisplay))
						&& (absolute >= 2.0)) {
					realnumber /= 2.0
					exponent += 1
				}
				else {
					/* No rounding needed */
				}
			}
		}
		else if (absolute != A(0.0)) { /* But not binary prefixes */
			if ((isX && dspState.forcedEngDisplay)
					|| ((absolute < pow(10.0, dspOptions.expNegMinDisplay.toAF()))
						|| ((absolute >= pow(10.0, dspOptions.expPosMaxDisplay.toAF()))))) {
				if (isX && dspState.forcedEngDisplay) {
					exponent = dspState.forcedEngFactor
				}
				else {
					exponent = round(AF.floor(log10(absolute))).toLong()
					if ((getOption(DispOpt.EngNotation)) || (dspState.forcedEngDisplay)) {
						exponent = 3 * round(AF.floor(exponent.toAF() / 3.0)).toLong()
					}
				}
				realnumber /= pow(10.0, exponent.toAF())

				// Check whether rounding bumps exponent
				if (( ! dspState.showAll) && ( ! dspState.forcedEngDisplay)) {
					realnumber = RoundToDecimalPlaces(realnumber, places)
				}
				absolute = abs(realnumber)

				if (getOption(DispOpt.EngNotation) && ( ! dspState.forcedEngDisplay)
						&& (absolute >= 1000.0)) {
					realnumber /= 1000.0
					exponent += 3
				}
				else if (( ! (getOption(DispOpt.EngNotation) || dspState.forcedEngDisplay))
						&& (absolute >= 10.0)) {
					realnumber /= 10.0
					exponent += 1
				}
				else {
					/* No rounding needed */
				}
			}
		}
		else {
		}
		result.value = realnumber
		result.exponent = exponent.toInt()
		return result
	}

	fun formatEnteredText(value: String): String {
		var formatted = value
		if (getOption(DispOpt.ThousandsSeparator)) {
			formatted = addThousandsSeparator(formatted)
		}
		if (getOption(DispOpt.EuropeanDecimal)) {
			formatted = europeanDecimal(formatted)
		}
		formatted = powerExponent(formatted, isX = true)
		formatted = formatted.replace("-", "&ndash;")
		return formatted
	}

	fun formatDecimal(value: AF, isX: Boolean, constHelpMode: Boolean = false): String {
		var v: ValueAndExp = getValueAndExponent(value, isX)
		var realnumber = v.value
		var exponent = v.exponent

		var places: Int = dspOptions.decimalPlaces

		var formatted: String
		if (dspState.showAll || constHelpMode) {
			formatted = "%.30f".format(realnumber.toDouble())
		}
		else if ((realnumber != A(0.0)) && dspState.forcedEngDisplay) {
			formatted = "%." + places.toString() + "f" // or e?
			formatted = formatted.format(realnumber.toDouble())
			if (formatted.contains("e")) {
				var parts = formatted.split("e")
				var numparts: List<String>
				if (parts[0].contains(".")) {
					numparts = parts[0].split(".")
				}
				else {
					numparts = listOf(parts[0], "0")
				}
				var sc_exponent: Int = parts[1].toInt()
				var newformatted: String
				if (sc_exponent < 0) {
					newformatted = "0."
					var major_length: Int = Math.abs(sc_exponent) - numparts[0].length
					if (major_length < numparts[0].length) {
						/* TODO: Problem! */
					}
					else if (major_length == numparts[0].length) {
						newformatted += numparts[0]
					}
					else {
						var padStr = ""
						while (padStr.length < (major_length-numparts[0].length)) {
							padStr += "0"
						}
						newformatted += padStr + numparts[0]
					}
					newformatted += numparts[1]
					formatted = newformatted
				}
				else {
					newformatted = numparts[0] + numparts[1]
					var total_length: Int = sc_exponent + numparts[0].length
					if (total_length < newformatted.length) {
						/* TODO: Problem */
					}
					else if (total_length > newformatted.length) {
						var padStr = ""
						while (padStr.length < (newformatted.length - total_length)) {
							padStr += "0"
						}
						newformatted += padStr
					}
					else {
						/* NOP */
					}
					formatted = newformatted
				}
			}

			if (formatted.contains(".")) {
				while (formatted.endsWith("0")) {
					formatted = formatted.dropLast(1)
				}
				if (formatted.endsWith(".")) {
					formatted += "0"
				}
			}
		}
		else {
			formatted = "%." + places.toString() + "f"
			// TODO: Check this is reasonable
			formatted = formatted.format(realnumber.toDouble())
		}

		if (getOption(DispOpt.ThousandsSeparator)) {
			formatted = addThousandsSeparator(formatted)
		}

		if (getOption(DispOpt.TrimZeroes) || dspState.showAll || constHelpMode) {
			if (formatted.contains(".")) {
				while (formatted.endsWith("0")) {
					formatted = formatted.dropLast(1)
				}
				if (formatted.endsWith(".")) {
					if (getOption(DispOpt.AlwaysShowDecimal) || constHelpMode) {
						formatted += "0"
					}
					else {
						formatted = formatted.dropLast(1)
					}
				}
			}
		}
		if (getOption(DispOpt.EuropeanDecimal)) {
			formatted = europeanDecimal(formatted)
		}

		var trim_exponents: Boolean = true
		if ((exponent != 0) || (isX && (dspState.enteredText.contains("e")))) {
			var exponent_digits: Int = 4
			if (trim_exponents) {
				exponent_digits = 1
			}
			var exponentStr = "%0" + exponent_digits.toString() + "d"
			exponentStr = exponentStr.format(exponent)
			while (exponentStr.startsWith("0") && (exponentStr.length > 1)) {
				exponentStr = exponentStr.drop(1)
			}
			formatted += "e"
			formatted += exponentStr
			formatted = powerExponent(formatted, isX)
		}

		formatted = formatted.replace("-", "&ndash;")

		return formatted
	}

	fun powerExponent(preformatted: String, isX: Boolean): String {
		var parts = preformatted.toLowerCase().split("e")
		if (parts.size > 1) {
			var exponentPart: String = parts[1]
			var coefficientPart: String = parts[0]
			var exponentSign: String = ""

			if (exponentPart.startsWith("+")) {
				exponentPart = exponentPart.substring(1)
			}
			else if (exponentPart.startsWith("-")) {
				exponentSign = "-"
				exponentPart = exponentPart.substring(1)
			}

			while (exponentPart.startsWith("0") && (exponentPart.length > 1)) {
				exponentPart = exponentPart.substring(1)
			}

			var expValue: Int = parts[1].toInt()
			if ((expValue == 0) && isX && (( ! dspState.entering) || (dspState.justPressedEnter))) {
				return coefficientPart
			}
			else if (getOption(DispOpt.BinaryPrefixes)) {
				if (getOption(DispOpt.SINotation)) {
					for ((prefix, eV) in SIBinaryPrefixes) {
						if (eV == expValue) {
							return coefficientPart + "&nbsp;" + SISymbols[prefix]
						}
					}
				}

				/* Enforce power exponent view if binary prefixes are used to avoid
				 * confusion as to what E means
				 */
				return coefficientPart + "&times;2<sup><small>" + exponentSign + exponentPart + "</small></sup>"
			}
			else if (getOption(DispOpt.SINotation)) {
				for ((prefix, eV) in SIDecimalPrefixes) {
					if (eV == expValue) {
						return coefficientPart + "&nbsp;" + SISymbols[prefix]
					}
				}
			}
			else {
				/* Handled below */
			}

			if (getOption(DispOpt.PowerExponentView)) {
				return coefficientPart + "&times;10<sup><small>" + exponentSign + exponentPart + "</small></sup>"
			}
		}
		return preformatted
	}

	fun engRotate(direction: Int) {
		var multiplier: Int = 3
		completeEntering(true)
		if (getOption(DispOpt.BinaryPrefixes)) {
			multiplier = 10
		}

		if (dspState.forcedEngDisplay) {
			dspState.forcedEngFactor += multiplier*direction
		}
		else {
			/* Temporarily set use eng notation */
			var before: Boolean = getOption(DispOpt.EngNotation)
			setOption(DispOpt.EngNotation, true)
			var v: ValueAndExp = getValueAndExponent(st.peek(), true)
			setOption(DispOpt.EngNotation, before)

			var exponent: Int = v.exponent
			// Ensure it's a multiple of multiplier
			exponent = multiplier * (AF.round(AF.floor(exponent.toAF() / multiplier.toAF()))).toInt()

			dspState.forcedEngDisplay = true
			dspState.forcedEngFactor = (exponent + (multiplier*direction)).toLong()
		}
	}

	fun RoundToDecimalPlaces(d: AF, c: Int): AF {
		var temp: AF = round(d*pow(10.0, c.toAF()))
		return (temp / pow(10.0, c.toAF()))
	}

	fun processCurrencyData(data: ByteArray) : String {
		var zipStream = ZipInputStream(data.inputStream())
		zipStream.getNextEntry()
		
		var sc = Scanner(zipStream);
		var headers: MutableMap<Int, String> = mutableMapOf()
		// Leave as double for easier parsing
		var values: MutableMap<String, Double> = mutableMapOf()
		// TODO: Do something with this:
		var date: String = "" // This needs to become a date object later
		while (sc.hasNextLine()) {
			var line = sc.nextLine().trim()
			if (headers.size == 0) {
				for ((index, entry) in line.split(",").withIndex()) {
					if (entry.trim().length > 0) {
						headers[index] = entry.trim()
					}
				}
			}
			else if (values.size == 0) {
				for ((index, entry) in line.split(",").withIndex()) {
					var currency = headers[index]
					if (currency == "Date") {
						date = entry
					}
					else if (currency != null) {
						var f = entry.trim().toDoubleOrNull()
						if (f != null) {
							values.put(currency, f)
						}
					}
				}
			}
			else {
				// Too many lines or just a blank one on the end
			}
		}
		st.registerCurrencies(values.toMap())
		last_currency_date = date
		return checkCurrencyDate(date)
	}

	fun getRawCurrencyData() : Map<String, Double> {
		return st.rawCurrencyData
	}
}
