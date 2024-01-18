package uk.co.cgtk.karpcalc

enum class CalcOpt {
	ReplicateStack, // Use an X, Y, Z, T replicating stack
	Radians,        // Use radians instead of degrees
	SaveHistory,    // Save the stack history (memory intensive)
	PercentLeavesY, // Leave Y in place when calculating percentage
	// If adding any here, make sure they're added to getCalcOptionByName
}

enum class ErrorCode {
	NoError,
	DivideByZero,
	InvalidRoot,
	InvalidLog,
	InvalidTan,
	InvalidInverseTrig,
	InvalidInverseHypTrig,
	UnknownConstant,
	UnknownConversion,
	InvalidConversion,
	UnknownSI,
	NoFunction,
	NoHistorySaved,
	NotImplemented,
}

enum class BitCount {
	bc8,
	bc16,
	bc32,
	bc64,
}

class Stack(
	var options: MutableSet<CalcOpt> = mutableSetOf<CalcOpt>(),
	var bitCount: BitCount = BitCount.bc32
) {
	var stack = mutableListOf<AF>()
	var history: MutableList<List<AF > > = mutableListOf()
	var conversionTable: MutableMap<String, List< Conversion > > = mutableMapOf()
	var currencyMap: Map<String, String> = mapOf()
	var rawCurrencyData: Map<String, Double> = mapOf()
	var unitSymbols: Map<String, String> = mapOf()
	var constants: List<Constant> = listOf()
	var densities: List<Density> = listOf()
	val MAX_HIST: Int = 50

	init {
		this.populateConversionTable()
		this.populateConstants()
		this.populateDensities()
	}

	fun getStackForDisplay(): MutableList<AF> {
		var result: MutableList<AF> = mutableListOf()
		/* Use indices to get the right range, but use peek
		 * to ensure the correct order regardless of actual
		 * implementation.
		 */
		for (i in stack.indices) {
			result.add(peekAt(i))
		}
		return result
	}

	fun setOption(o: CalcOpt, v: Boolean = true) {
		if (v) {
			options.add(o)
		}
		else {
			options.remove(o)
		}
	}
	fun getOption(o: CalcOpt) : Boolean {
		return options.contains(o)
	}
	fun getBitMask() : Long {
		when (bitCount) {
			BitCount.bc8 ->  return 0xFFL
			BitCount.bc16 -> return 0xFFFFL
			BitCount.bc32 -> return 0xFFFFFFFFL
			else -> return 0x7FFFFFFFFFFFFFFFL
		}
	}
	fun saveHistory() {
		if (options.contains(CalcOpt.SaveHistory)) {
			history.add(stack.toList())
			if (history.size > MAX_HIST) {
				history.removeAt(0)
			}
		}
	}
	fun clear() {
		stack.clear()
	}
	fun push(v: AF) {
		stack.add(v)
		if (options.contains(CalcOpt.ReplicateStack)) {
			while (stack.size > 4) {
				stack.removeAt(0)
			}
		}
	}

	fun push(vs: List<AF>) {
		stack.addAll(vs)
		if (options.contains(CalcOpt.ReplicateStack)) {
			while (stack.size > 4) {
				stack.removeAt(0)
			}
		}
	}

	fun pop(): AF {
		var result: AF
		if (stack.size == 0) {
			result = A(0.0)
		}
		else {
			if (options.contains(CalcOpt.ReplicateStack)) {
				if (stack.size == 4) {
					stack.add(0, stack.first())
				}
			}
			result = stack.last()
			stack.removeAt(stack.lastIndex)
		}
		return result
	}

	fun peek(): AF {
		if (stack.size == 0) {
			return A(0)
		}
		else {
			return stack.last()
		}
	}

	fun peekAt(index: Int) : AF {
		if (index < stack.size) {
			return stack[stack.lastIndex-index]
		}
		else {
			return A(0)
		}
	}

	fun rollUp() : ErrorCode {
		if (stack.size > 0) {
			var v = stack.last()
			stack.removeAt(stack.lastIndex)
			stack.add(0, v)
		}
		return ErrorCode.NoError
	}

	fun rollDown() : ErrorCode {
		if (stack.size > 0) {
			stack.add(stack.first())
			stack.removeAt(0)
		}
		return ErrorCode.NoError
	}

	fun undo(): ErrorCode {
		if ( ! options.contains(CalcOpt.SaveHistory)) {
			return ErrorCode.NoHistorySaved
		}
		if (history.size > 0) {
			stack = history.removeAt(history.lastIndex).toMutableList()
		}
		else {
			stack.clear()
		}
		return ErrorCode.NoError
	}
}

/* ------------------------------------------------- */

