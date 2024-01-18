package uk.co.cgtk.karpcalc

fun CommandHandler.initialiseSIPrefixes() {
	this.SIBinaryPrefixes = linkedMapOf(
		"Kibi" to 10,
		"Mebi" to 20,
		"Gibi" to 30,
		"Tebi" to 40,
		"Pebi" to 50,
		"Exbi" to 60
	)

	this.SIDecimalPrefixes = linkedMapOf(
		"Yocto" to -24,
		"Zepto" to -21,
		"Atto" to -18,
		"Femto" to -15,
		"Pico" to -12,
		"Nano" to -9,
		"Micro" to -6,
		"Milli" to -3,
		"Kilo" to 3,
		"Mega" to 6,
		"Giga" to 9,
		"Tera" to 12,
		"Peta" to 15,
		"Exa" to 18,
		"Zetta" to 21,
		"Yotta" to 24
	)

	this.SISymbols = linkedMapOf(
		"Kibi" to "Ki",
		"Mebi" to "Mi",
		"Gibi" to "Gi",
		"Tebi" to "Ti",
		"Pebi" to "Pi",
		"Exbi" to "Ei",
		"Yocto" to "y",
		"Zepto" to "z",
		"Atto" to "a",
		"Femto" to "f",
		"Pico" to "p",
		"Nano" to "n",
		"Micro" to "&mu;",
		"Milli" to "m",
		"Kilo" to "k",
		"Mega" to "M",
		"Giga" to "G",
		"Tera" to "T",
		"Peta" to "P",
		"Exa" to "E",
		"Zetta" to "Z",
		"Yotta" to "Y"
	)
}

fun CommandHandler.SI(name: String) : ErrorCode {
	var mult: AF? = null
	var power: Int?  = this.SIDecimalPrefixes[name]
	if (power != null) {
		mult = pow(10.0, power)
	}
	if (mult == null) {
		power = this.SIBinaryPrefixes[name]
		if (power != null) {
			mult = pow(2.0, power)
		}
	}
	if (mult == null) {
		for ((long, short) in this.SISymbols) {
			if (short == name) {
				power = this.SIDecimalPrefixes[long]
				if (power != null) {
					mult = pow(10.0, power)
				}
				if (mult == null) {
					power = this.SIBinaryPrefixes[long]
					if (power != null) {
						mult = pow(2.0, power)
					}
				}
				break;
			}
		}
	}
	if (mult == null) {
		return ErrorCode.UnknownSI
	}
	var multiplied: AF = st.pop() * mult
	st.push(multiplied)
	return ErrorCode.NoError
}

fun CommandHandler.getBinaryPrefixSymbols(): List<String> {
	var result: MutableList<String> = mutableListOf()

	for ((long, short) in this.SISymbols) {
		if (this.SIBinaryPrefixes.containsKey(long)) {
			result.add(short)
		}
	}
	return result.toList()
}

fun CommandHandler.getDecimalPrefixSymbols(): List<String> {
	var result: MutableList<String> = mutableListOf()

	for ((long, short) in this.SISymbols) {
		if (this.SIDecimalPrefixes.containsKey(long)) {
			result.add(short)
		}
	}
	return result.toList()
}

fun CommandHandler.getBinaryPrefixNames(): List<String> {
	var result: MutableList<String> = mutableListOf()

	for (p in this.SISymbols.keys) {
		if (this.SIBinaryPrefixes.containsKey(p)) {
			result.add(p)
		}
	}
	return result.toList()
}

fun CommandHandler.getDecimalPrefixNames(): List<String> {
	var result: MutableList<String> = mutableListOf()

	for (p in this.SISymbols.keys) {
		if (this.SIDecimalPrefixes.containsKey(p)) {
			result.add(p)
		}
	}
	return result.toList()
}
