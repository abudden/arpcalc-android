package uk.co.cgtk.karpcalc

import java.util.Random

data class Constant(val category: String, val symbol: String, val name: String, val value: AF, val unit: String)
data class Density(val name: String, val value: AF, val category: String)

fun Stack.random(): ErrorCode {
	val r = Random() // Ideally this should be seeded in the init function
	var num = r.nextDouble() * 32767.0
	this.push(num.toAF())
	return ErrorCode.NoError
}

fun Stack.plus(): ErrorCode {
	this.push(this.pop()+this.pop())
	return ErrorCode.NoError
}

fun Stack.minus(): ErrorCode {
	var x = this.pop()
	var y = this.pop()
	this.push(y-x)
	return ErrorCode.NoError
}

fun Stack.times(): ErrorCode {
	this.push(this.pop()*this.pop())
	return ErrorCode.NoError
}

fun Stack.divide(): ErrorCode {
	var x = this.pop()
	if (x == A(0.0)) {
		this.push(x)
		return ErrorCode.DivideByZero
	}
	else {
		var y = this.pop()
		this.push(y/x)
	}
	return ErrorCode.NoError
}

fun Stack.xrooty(): ErrorCode {
	var x = this.pop()
	var y = this.pop()
	if ((x == A(0.0)) or (y < 0.0)) {
		this.push(y)
		this.push(x)
		return ErrorCode.InvalidRoot
	}
	else {
		this.push(pow(y, 1.0/x))
	}
	return ErrorCode.NoError
}

fun Stack.invert(): ErrorCode {
	this.push(0.0-this.pop())
	return ErrorCode.NoError
}

fun Stack.etox(): ErrorCode {
	this.push(pow(e(), this.pop()))
	return ErrorCode.NoError
}

fun Stack.tentox(): ErrorCode {
	this.push(pow(10.0, this.pop()))
	return ErrorCode.NoError
}

fun Stack.twotox(): ErrorCode {
	this.push(pow(2.0, this.pop()))
	return ErrorCode.NoError
}

fun Stack.percent(): ErrorCode {
	/* This implementation matches the HP calculator one:
	 * calculate x %of y, so if y is 70 and x is 35.  The result
	 * is (35/100)*70.  By default PercentLeavesY is set and
	 * hence y stays as 70 and x becomes 24.5.
	 */
	var x = this.pop()
	var y = this.pop()
	if (options.contains(CalcOpt.PercentLeavesY)) {
		this.push(y)
	}
	this.push((x/100.0)*y)
	return ErrorCode.NoError
}

fun Stack.percentchange(): ErrorCode {
	/* This implementation matches the HP calculator one:
	 * calculate the percentage change of Y that would give X.
	 * This is calculated as ((x-y)/y)*100
	 * By default PercentLeavesY is set.
	 */
	var x = this.pop()
	var y = this.pop()
	if (options.contains(CalcOpt.PercentLeavesY)) {
		this.push(y)
	}
	this.push(((x-y)/y)*100.0)
	return ErrorCode.NoError
}

fun Stack.square(): ErrorCode {
	var x = this.pop()
	this.push(x*x)
	return ErrorCode.NoError
}
fun Stack.cube(): ErrorCode {
	var x = this.pop()
	this.push(x*x*x)
	return ErrorCode.NoError
}
fun Stack.reciprocal(): ErrorCode {
	var x = this.pop()
	if (x == A(0.0)) {
		this.push(x)
		return ErrorCode.DivideByZero
	}
	else {
		this.push(1.0/x)
	}
	return ErrorCode.NoError
}
fun Stack.integerpart(): ErrorCode {
	var x = this.pop()
	if (x > 0.0) {
		this.push(x.floor())
	}
	else {
		this.push(x.ceil())
	}
	return ErrorCode.NoError
}
fun Stack.floatingpart(): ErrorCode {
	var x = this.pop()
	if (x > 0.0) {
		this.push(x - x.floor())
	}
	else {
		this.push(x - x.ceil())
	}
	return ErrorCode.NoError
}
fun Stack.integerdivide(): ErrorCode {
	var x = this.pop()
	if (x == A(0.0)) {
		this.push(x)
		return ErrorCode.DivideByZero
	}
	else {
		var y = this.pop()
		var v = y / x
		if (v >= 0.0) {
			this.push(v.floor())
		}
		else {
			this.push(v.ceil())
		}
	}
	return ErrorCode.NoError
}
fun Stack.bitwiseand(): ErrorCode {
	var xl = round(this.pop()).toLong()
	var yl = round(this.pop()).toLong()
	var mask = this.getBitMask()
	xl = xl and mask
	yl = yl and mask
	var v = xl and yl
	v = v and mask
	this.push(v.toAF())
	return ErrorCode.NoError
}
fun Stack.bitwiseor(): ErrorCode {
	var xl = round(this.pop()).toLong()
	var yl = round(this.pop()).toLong()
	var mask = this.getBitMask()
	xl = xl and mask
	yl = yl and mask
	var v = xl or yl
	v = v and mask
	this.push(v.toAF())
	return ErrorCode.NoError
}
fun Stack.bitwisexor(): ErrorCode {
	var xl = round(this.pop()).toLong()
	var yl = round(this.pop()).toLong()
	var mask = this.getBitMask()
	xl = xl and mask
	yl = yl and mask
	var v = xl xor yl
	v = v and mask
	this.push(v.toAF())
	return ErrorCode.NoError
}
fun Stack.bitwisenot(): ErrorCode {
	var xl = round(this.pop()).toLong()
	var mask = this.getBitMask()
	xl = xl and mask
	xl = xl.inv()
	xl = xl and mask
	this.push(xl.toAF())
	return ErrorCode.NoError
}
fun Stack.twoscomplement(): ErrorCode {
	var xl = round(this.pop()).toLong()
	var mask = this.getBitMask()
	xl = xl and mask
	xl = xl.inv()
	xl += 1
	xl = xl and mask
	this.push(xl.toAF())
	return ErrorCode.NoError
}
fun Stack.absolute(): ErrorCode {
	this.push(abs(this.pop()))
	return ErrorCode.NoError
}
fun Stack.log10(): ErrorCode {
	var x = this.pop()
	if (x <= 0.0) {
		this.push(x)
		return ErrorCode.InvalidLog
	}
	else {
		this.push(log10(x))
	}
	return ErrorCode.NoError
}
fun Stack.loge(): ErrorCode {
	var x = this.pop()
	if (x <= 0.0) {
		this.push(x)
		return ErrorCode.InvalidLog
	}
	else {
		this.push(AF.log(x))
	}
	return ErrorCode.NoError
}
fun Stack.log2(): ErrorCode {
	var x = this.pop()
	if (x <= 0.0) {
		this.push(x)
		return ErrorCode.InvalidLog
	}
	else {
		this.push(AF.log10(x)/AF.log10(2.0))
	}
	return ErrorCode.NoError
}
fun Stack.ceiling(): ErrorCode {
	this.push(this.pop().ceil())
	return ErrorCode.NoError
}
fun Stack.floor(): ErrorCode {
	this.push(this.pop().floor())
	return ErrorCode.NoError
}
fun Stack.cos(): ErrorCode {
	var x = this.pop()
	if ( ! this.options.contains(CalcOpt.Radians)) {
		x *= pi() / 180.0
	}
	this.push(AF.cos(x))
	return ErrorCode.NoError
}
fun Stack.sin(): ErrorCode {
	var x = this.pop()
	if ( ! this.options.contains(CalcOpt.Radians)) {
		x *= pi() / 180.0
	}
	this.push(AF.sin(x))
	return ErrorCode.NoError
}
fun Stack.tan(): ErrorCode {
	var x = this.pop()
	var xr = x;
	if ( ! this.options.contains(CalcOpt.Radians)) {
		xr *= pi() / 180.0
	}
	if ((xr.remainder(pi()/2.0) == A(0.0)) && ((xr/(pi()/2.0)).remainder(2.0) == A(1.0))) {
		this.push(x)
		return ErrorCode.InvalidTan
	}
	else {
		this.push(AF.tan(xr))
	}
	return ErrorCode.NoError
}
fun Stack.inversecos(): ErrorCode {
	var x = this.pop()
	if ((x < -1.0) || (x > 1.0)) {
		this.push(x)
		return ErrorCode.InvalidInverseTrig
	}
	else {
		var v = AF.acos(x)
		if ( ! this.options.contains(CalcOpt.Radians)) {
			v *= 180.0/pi()
		}
		this.push(v)
	}
	return ErrorCode.NoError
}
fun Stack.inversesin(): ErrorCode {
	var x = this.pop()
	if ((x < -1.0) || (x > 1.0)) {
		this.push(x)
		return ErrorCode.InvalidInverseTrig
	}
	else {
		var v = AF.asin(x)
		if ( ! this.options.contains(CalcOpt.Radians)) {
			v *= 180.0/pi()
		}
		this.push(v)
	}
	return ErrorCode.NoError
}
fun Stack.inversetan(): ErrorCode {
	var x = this.pop()
	var v = AF.atan(x)
	if ( ! this.options.contains(CalcOpt.Radians)) {
		v *= 180.0/pi()
	}
	this.push(v)
	return ErrorCode.NoError
}
fun Stack.inversetan2(): ErrorCode {
	var x = this.pop()
	var y = this.pop()
	if ((x == A(0.0)) && (y == A(0.0))) {
		this.push(y)
		this.push(x)
		return ErrorCode.DivideByZero
	}
	else {
		var v: AF
		if (x > 0.0) {
			v = y / x
			v = v.atan()
		}
		else if ((x < 0.0) && (y >= 0.0)) {
			v = y / x
			v = v.atan() + pi()
		}
		else if ((x < 0.0) && (y < 0.0)) {
			v = y / x
			v = v.atan() - pi()
		}
		else if ((x == A(0.0)) && (y > 0.0)) {
			v = pi()/2.0
		}
		else if ((x == A(0.0)) && (y < 0.0)) {
			v = -pi()/2.0
		}
		else {
			// Shouldn't get here, but just in case:
			this.push(y)
			this.push(x)
			return ErrorCode.DivideByZero
		}

		if ( ! this.options.contains(CalcOpt.Radians)) {
			v *= 180.0/pi()
		}
		this.push(v)
	}
	return ErrorCode.NoError
}
fun Stack.cosh(): ErrorCode {
	var x = this.pop()
	this.push(AF.cosh(x))
	return ErrorCode.NoError
}
fun Stack.sinh(): ErrorCode {
	var x = this.pop()
	this.push(AF.sinh(x))
	return ErrorCode.NoError
}
fun Stack.tanh(): ErrorCode {
	var x = this.pop()
	this.push(AF.tanh(x))
	return ErrorCode.NoError
}
fun Stack.inversecosh(): ErrorCode {
	var x = this.pop()
	var v1 = (x*x) - 1.0
	if (v1 < 0.0) {
		this.push(x)
		return ErrorCode.InvalidInverseHypTrig
	}
	v1 = x + (sqrt(v1))
	if (v1 <= 0.0) {
		this.push(x)
		return ErrorCode.InvalidInverseHypTrig
	}
	this.push(log(v1))
	return ErrorCode.NoError
}
fun Stack.inversesinh(): ErrorCode {
	var x = this.pop()
	var v1 = x + sqrt(1.0+(x*x))
	if (v1 <= 0.0) {
		this.push(x)
		return ErrorCode.InvalidInverseHypTrig
	}
	this.push(log(v1))
	return ErrorCode.NoError
}
fun Stack.inversetanh(): ErrorCode {
	var x = this.pop()
	if (x == A(1.0)) {
		this.push(x)
		return ErrorCode.DivideByZero
	}
	var v1 = (1.0+x)/(1.0-x)
	if (v1 <= 0.0) {
		this.push(x)
		return ErrorCode.InvalidInverseHypTrig
	}
	this.push(0.5*log(v1))
	return ErrorCode.NoError
}
fun Stack.inversetanh2(): ErrorCode {
	var x = this.pop()
	if (x == A(0.0)) {
		this.push(x)
		return ErrorCode.DivideByZero
	}
	var y = this.pop()
	var v1 = y / x
	if (v1 == A(1.0)) {
		this.push(y)
		this.push(x)
		return ErrorCode.DivideByZero
	}
	v1 = (1.0 + v1) / (1.0 - v1)
	if (v1 <= 0.0) {
		this.push(y)
		this.push(x)
		return ErrorCode.InvalidInverseHypTrig
	}
	this.push(0.5*log(v1))
	return ErrorCode.NoError
}
fun Stack.round(): ErrorCode {
	var v = this.pop().round()
	this.push(v)
	return ErrorCode.NoError
}
fun Stack.squareroot(): ErrorCode {
	var x = this.pop()
	if (x < 0.0) {
		this.push(x)
		return ErrorCode.InvalidRoot
	}
	else {
		this.push(sqrt(x))
	}
	return ErrorCode.NoError
}
fun Stack.cuberoot(): ErrorCode {
	var x = this.pop()
	if (x < 0.0) {
		this.push(x)
		return ErrorCode.InvalidRoot
	}
	else {
		this.push(cbrt(x))
	}
	return ErrorCode.NoError
}
fun Stack.power(): ErrorCode {
	var x = this.pop()
	var y = this.pop()
	this.push(pow(y, x))
	return ErrorCode.NoError
}
fun Stack.swap(): ErrorCode {
	var x = this.pop()
	var y = this.pop()
	this.push(x)
	this.push(y)
	return ErrorCode.NoError
}
fun Stack.duplicate(): ErrorCode {
	/* In practice, more likely to use ENTER,
	 * which is implemented separately.
	 */
	this.push(this.peek())
	return ErrorCode.NoError
}
fun Stack.remainder(): ErrorCode {
	var x = this.pop()
	if (x == A(0.0)) {
		this.push(x)
		return ErrorCode.DivideByZero
	}
	else {
		var y = this.pop()
		this.push(y.remainder(x))
	}
	return ErrorCode.NoError
}

fun Stack.drop(): ErrorCode {
	this.pop()
	return ErrorCode.NoError
}

fun Stack.convertHmsToHours(): ErrorCode {
	var hours: AF
	var minutes: AF
	var seconds: AF
	var sign: Int
	var x = this.pop()
	if (x >= 0.0) {
		hours = x.floor()
		minutes = x - hours
		sign = 1
	}
	else {
		hours = x.ceil()
		minutes = hours - x
		sign = -1
	}
	minutes *= 100.0
	seconds = minutes - minutes.floor()
	minutes = minutes.floor()
	seconds *= 100.0
	this.push(hours + (sign.toAF()*((minutes+(seconds/60.0))/60.0)))
	return ErrorCode.NoError
}
fun Stack.convertHmToHours(): ErrorCode {
	var hours: AF
	var minutes: AF
	var sign: Int
	var x = this.pop()
	if (x >= 0.0) {
		hours = x.floor()
		minutes = x - hours
		sign = 1
	}
	else {
		hours = x.ceil()
		minutes = hours - x
		sign = -1
	}
	minutes *= 100.0
	this.push(hours + (sign.toAF()*(minutes/60.0)))
	return ErrorCode.NoError
}
fun Stack.convertHoursToHms(): ErrorCode {
	var parthours: AF
	var hours: AF
	var minutes: AF
	var seconds: AF
	var sign: Int
	var x = this.pop()
	if (x >= 0.0) {
		hours = x.floor()
		parthours = x - hours
		sign = 1
	}
	else {
		hours = x.ceil()
		parthours = hours - x
		sign = -1
	}
	minutes = parthours * A("60")
	seconds = minutes - minutes.floor()
	seconds *= A("60.0")
	minutes = minutes.floor()
	while (seconds >= A("60.0")) {
		seconds -= A("60.0")
		minutes += A("1.0")
	}
	while (minutes >= A("60.0")) {
		minutes -= A("60.0")
		hours += sign.toAF()
	}
	this.push(hours + (sign.toAF()*((minutes + (seconds / A("100.0")))/A("100.0"))))
	return ErrorCode.NoError
}
fun Stack.convertHoursToHm(): ErrorCode {
	var parthours: AF
	var hours: AF
	var minutes: AF
	var sign: Int
	var x = this.pop()
	if (x >= A("0.0")) {
		hours = x.floor()
		parthours = x - hours
		sign = 1
	}
	else {
		hours = x.ceil()
		parthours = hours - x
		sign = -1
	}
	minutes = parthours * A("60.0")
	while (minutes >= A("60.0")) {
		minutes -= A("60.0")
		hours += sign.toAF()
	}
	this.push(hours + (sign.toAF()*(minutes / A("100.0"))))
	return ErrorCode.NoError
}

fun Stack.populateConstants() {
	this.constants = listOf(
		Constant(name="Pi",                         symbol="&pi;",                                  value=pi(),          unit="m&nbsp;s<sup><small>-1</small></sup>",                                                            category="Universal"),
		Constant(name="Euler's Number",             symbol="e<sup><small>1</small></sup>",          value=e(),           unit="m&nbsp;s<sup><small>-1</small></sup>",                                                            category="Universal"),
		Constant(name="Speed of Light in Vacuum",   symbol="c",                                     value=A(299792458.0),      unit="m&nbsp;s<sup><small>-1</small></sup>",                                                            category="Universal"),
		Constant(name="Gravitational Constant",     symbol="G",                                     value=A(6.67408e11),       unit="m<sup><small>3</small></sup>&nbsp;kg<sup><small>-1</small></sup>&nbsp;s<sup><small>-2</small></sup>",  category="Universal"),
		Constant(name="Planck Constant",            symbol="h",                                     value=A(6.62607004e-34),   unit="J&nbsp;s",                                                                                        category="Universal"),
		Constant(name="Reduced Planck Constant",    symbol="&#295;",                                value=A(1.0545718e-34),    unit="J&nbsp;s",                                                                                        category="Universal"),
		Constant(name="Permeability of Free Space", symbol="&mu;<sub><small>0</small></sub>",       value=A(1.256637061e-6),   unit="N&nbsp;A<sup><small>-2</small></sup>",                                                            category="Electromagnetic"),
		Constant(name="Permittivity of Free Space", symbol="&epsilon;<sub><small>0</small></sub>",  value=A(8.854187818e-12),  unit="F&nbsp;m<sup><small>-1</small></sup>",                                                            category="Electromagnetic"),
		Constant(name="Elementary Charge",          symbol="e",                                     value=A(1.602176621e-19),  unit="C",                                                                                          category="Electromagnetic"),
		Constant(name="Magnetic Flux Quantum",      symbol="&phi;<sub><small>0</small></sub>",      value=A(2.067833831e-15),  unit="Wb",                                                                                         category="Electromagnetic"),
		Constant(name="Conductance Quantum",        symbol="G<sub><small>0</small></sub>",          value=A(7.748091731e-5),   unit="S",                                                                                          category="Electromagnetic"),
		Constant(name="Electron Mass",              symbol="m<sub><small>e</small></sub>",          value=A(9.10938356e-31),   unit="kg",                                                                                         category="Atomic"),
		Constant(name="Proton Mass",                symbol="m<sub><small>p</small></sub>",          value=A(1.672621898e-27),  unit="kg",                                                                                         category="Atomic"),
		Constant(name="Fine Structure Constant",    symbol="&alpha;",                               value=A(0.007297353),      unit="",                                                                                           category="Atomic"),
		Constant(name="Rydberg Constant",           symbol="R<sub><small>&infin;</small></sub>",    value=A(10973731.57),      unit="m<sup><small>-1</small></sup>",                                                              category="Atomic"),
		Constant(name="Bohr Radius",                symbol="a<sub><small>0</small></sub>",          value=A(5.291772107e-11),  unit="m",                                                                                          category="Atomic"),
		Constant(name="Classical Electron Radius",  symbol="r<sub><small>e</small></sub>",          value=A(2.817940323e-15),  unit="m",                                                                                          category="Atomic"),
		Constant(name="Atomic Mass Unit",           symbol="u",                                     value=A(1.66053904e-27),   unit="kg",                                                                                         category="Physical"),
		Constant(name="Avogadro Constant",          symbol="N<sub><small>A</small></sub>",          value=A(6.022140857e23),   unit="mol<sup><small>-1</small></sup>",                                                            category="Physical"),
		Constant(name="Faraday Constant",           symbol="F",                                     value=A(96485.33289),      unit="C&nbsp;mol<sup><small>-1</small></sup>",                                                          category="Physical"),
		Constant(name="Molar Gas Constant",         symbol="R",                                     value=A(8.3144598),        unit="J&nbsp;mol<sup><small>-1</small></sup>&nbsp;K<sup><small>-1</small></sup>",                            category="Physical"),
		Constant(name="Boltzmann Constant",         symbol="k",                                     value=A(1.38064852e-23),   unit="J&nbsp;K<sup><small>-1</small></sup>",                                                            category="Physical"),
		Constant(name="Stefan-Boltzmann Constant",  symbol="&sigma;",                               value=A(5.670367e-8),      unit="W&nbsp;m<sup><small>-2</small></sup>&nbsp;K<sup><small>-4</small></sup>",                              category="Physical"),
		Constant(name="Electron Volt",              symbol="eV",                                    value=A(1.602176621e-19),  unit="J",                                                                                          category="Physical"),
		Constant(name="Standard Gravity",           symbol="g<sub><small>0</small></sub>",          value=A(9.80665),          unit="m&nbsp;s<sup><small>-2</small></sup>",                                                            category="Other")
	)
}

fun Stack.constant(name: String): ErrorCode {
	var value: AF? = null

	for (constant in constants) {
		if ((constant.name == name) || (constant.name.replace(" ", "") == name)) {
			value = constant.value
			break
		}
	}

	if (value == null) {
		return ErrorCode.UnknownConstant
	}
	else {
		this.push(value)
	}
	return ErrorCode.NoError
}

fun Stack.populateDensities() {
	this.densities = listOf(
		Density(name="Aluminium Bronze",       value=A("7700"), category="Metal"),
		Density(name="Aluminium",              value=A("2700"), category="Metal"),
		Density(name="Antimony",               value=A("6700"), category="Metal"),
		Density(name="Beryllium",              value=A("1850"), category="Metal"),
		Density(name="Bismuth",                value=A("9800"), category="Metal"),
		Density(name="Brass",                  value=A("8610"), category="Metal"),
		Density(name="Bronze",                 value=A("8815"), category="Metal"),
		Density(name="Cadmium",                value=A("8640"), category="Metal"),
		Density(name="Cast Iron",              value=A("7200"), category="Metal"),
		Density(name="Chromium",               value=A("7100"), category="Metal"),
		Density(name="Cobalt",                 value=A("8800"), category="Metal"),
		Density(name="Copper",                 value=A("8790"), category="Metal"),
		Density(name="Gallium",                value=A("5900"), category="Metal"),
		Density(name="Gold",                   value=A("19290"), category="Metal"),
		Density(name="Lead",                   value=A("11350"), category="Metal"),
		Density(name="Lithium",                value=A("530"), category="Metal"),
		Density(name="Magnesium",              value=A("1740"), category="Metal"),
		Density(name="Manganese",              value=A("7430"), category="Metal"),
		Density(name="Molybdenum",             value=A("10200"), category="Metal"),
		Density(name="Nickel",                 value=A("8900"), category="Metal"),
		Density(name="Osmium",                 value=A("22480"), category="Metal"),
		Density(name="Palladium",              value=A("12000"), category="Metal"),
		Density(name="Phosphor Bronze",        value=A("8800"), category="Metal"),
		Density(name="Phosphorus",             value=A("1820"), category="Metal"),
		Density(name="Potassium",              value=A("860"), category="Metal"),
		Density(name="Silver",                 value=A("10500"), category="Metal"),
		Density(name="Sodium",                 value=A("980"), category="Metal"),
		Density(name="Steel",                  value=A("7820"), category="Metal"),
		Density(name="Tantalum",               value=A("16600"), category="Metal"),
		Density(name="Tin",                    value=A("7280"), category="Metal"),
		Density(name="Titanium",               value=A("4500"), category="Metal"),
		Density(name="Tungsten Carbide",       value=A("14500"), category="Metal"),
		Density(name="Tungsten",               value=A("19200"), category="Metal"),
		Density(name="Uranium",                value=A("19100"), category="Metal"),
		Density(name="Vanadium",               value=A("6100"), category="Metal"),
		Density(name="Zinc",                   value=A("7120"), category="Metal"),
		Density(name="Agate",                  value=A("2600"), category="Mineral"),
		Density(name="Amber",                  value=A("1080"), category="Mineral"),
		Density(name="Basalt",                 value=A("2750"), category="Mineral"),
		Density(name="Bauxite",                value=A("1280"), category="Mineral"),
		Density(name="Borax",                  value=A("850"), category="Mineral"),
		Density(name="Brick, Fire",            value=A("2300"), category="Mineral"),
		Density(name="Brick",                  value=A("1900"), category="Mineral"),
		Density(name="Calcium",                value=A("1550"), category="Mineral"),
		Density(name="Carbon",                 value=A("3510"), category="Mineral"),
		Density(name="Clay",                   value=A("2200"), category="Mineral"),
		Density(name="Diamond",                value=A("3250"), category="Mineral"),
		Density(name="Flint",                  value=A("2600"), category="Mineral"),
		Density(name="Glass",                  value=A("2600"), category="Mineral"),
		Density(name="Granite",                value=A("2700"), category="Mineral"),
		Density(name="Graphite",               value=A("2500"), category="Mineral"),
		Density(name="Marble",                 value=A("2700"), category="Mineral"),
		Density(name="Opal",                   value=A("2200"), category="Mineral"),
		Density(name="Pyrex",                  value=A("2250"), category="Mineral"),
		Density(name="Quartz",                 value=A("2650"), category="Mineral"),
		Density(name="Silicon",                value=A("2330"), category="Mineral"),
		Density(name="Slate",                  value=A("2950"), category="Mineral"),
		Density(name="Sulphur",                value=A("2000"), category="Mineral"),
		Density(name="Topaz",                  value=A("3550"), category="Mineral"),
		Density(name="Beeswax",                value=A("960"), category="Other"),
		Density(name="Cardboard",              value=A("700"), category="Other"),
		Density(name="Leather",                value=A("860"), category="Other"),
		Density(name="Paper",                  value=A("925"), category="Other"),
		Density(name="Paraffin",               value=A("900"), category="Other"),
		Density(name="ABS",                    value=A("1060"), category="Plastic"),
		Density(name="Acetal",                 value=A("1420"), category="Plastic"),
		Density(name="Acrylic",                value=A("1190"), category="Plastic"),
		Density(name="Bakelite",               value=A("1360"), category="Plastic"),
		Density(name="Epoxy Cast Resin",       value=A("1255"), category="Plastic"),
		Density(name="Expanded Polystyrene",   value=A("22"), category="Plastic"),
		Density(name="HDPE",                   value=A("960"), category="Plastic"),
		Density(name="LDPE",                   value=A("910"), category="Plastic"),
		Density(name="Nylon",                  value=A("1145"), category="Plastic"),
		Density(name="PBT",                    value=A("1350"), category="Plastic"),
		Density(name="PET",                    value=A("1350"), category="Plastic"),
		Density(name="PMMA",                   value=A("1200"), category="Plastic"),
		Density(name="POM",                    value=A("1400"), category="Plastic"),
		Density(name="PP",                     value=A("925"), category="Plastic"),
		Density(name="PS",                     value=A("1030"), category="Plastic"),
		Density(name="PTFE",                   value=A("2290"), category="Plastic"),
		Density(name="PU",                     value=A("30"), category="Plastic"),
		Density(name="PVC",                    value=A("1405"), category="Plastic"),
		Density(name="Poly Carbonate",         value=A("1200"), category="Plastic"),
		Density(name="Teflon",                 value=A("2200"), category="Plastic"),
		Density(name="Alder",                  value=A("550"), category="Wood"),
		Density(name="Apple",                  value=A("750"), category="Wood"),
		Density(name="Ash, European",          value=A("710"), category="Wood"),
		Density(name="Aspen",                  value=A("420"), category="Wood"),
		Density(name="Balsa",                  value=A("125"), category="Wood"),
		Density(name="Bamboo",                 value=A("355"), category="Wood"),
		Density(name="Beech",                  value=A("800"), category="Wood"),
		Density(name="Birch",                  value=A("640"), category="Wood"),
		Density(name="Box",                    value=A("1055"), category="Wood"),
		Density(name="Cedar of Lebanon",       value=A("580"), category="Wood"),
		Density(name="Cedar, Western Red",     value=A("380"), category="Wood"),
		Density(name="Cherry",                 value=A("630"), category="Wood"),
		Density(name="Chestnut, Sweet",        value=A("560"), category="Wood"),
		Density(name="Cypress",                value=A("510"), category="Wood"),
		Density(name="Douglas Fir",            value=A("530"), category="Wood"),
		Density(name="Ebony",                  value=A("1220"), category="Wood"),
		Density(name="Elm",                    value=A("570"), category="Wood"),
		Density(name="Greenheart",             value=A("1040"), category="Wood"),
		Density(name="Hemlock, Western",       value=A("500"), category="Wood"),
		Density(name="Hickory",                value=A("765"), category="Wood"),
		Density(name="Holly",                  value=A("760"), category="Wood"),
		Density(name="Iroko",                  value=A("660"), category="Wood"),
		Density(name="Juniper",                value=A("560"), category="Wood"),
		Density(name="Larch",                  value=A("530"), category="Wood"),
		Density(name="Lignum Vitae",           value=A("1250"), category="Wood"),
		Density(name="Lime, European",         value=A("560"), category="Wood"),
		Density(name="Magnolia",               value=A("570"), category="Wood"),
		Density(name="Mahogany",               value=A("675"), category="Wood"),
		Density(name="Maple",                  value=A("685"), category="Wood"),
		Density(name="Meranti",                value=A("710"), category="Wood"),
		Density(name="Myrtle",                 value=A("660"), category="Wood"),
		Density(name="Oak",                    value=A("750"), category="Wood"),
		Density(name="Pear",                   value=A("670"), category="Wood"),
		Density(name="Pecan",                  value=A("770"), category="Wood"),
		Density(name="Pine, Pitch",            value=A("840"), category="Wood"),
		Density(name="Pine, Scots",            value=A("510"), category="Wood"),
		Density(name="Pine, White",            value=A("425"), category="Wood"),
		Density(name="Pine, Yellow",           value=A("420"), category="Wood"),
		Density(name="Plane, European",        value=A("640"), category="Wood"),
		Density(name="Plum",                   value=A("720"), category="Wood"),
		Density(name="Plywood",                value=A("540"), category="Wood"),
		Density(name="Poplar",                 value=A("425"), category="Wood"),
		Density(name="Redwood, American",      value=A("450"), category="Wood"),
		Density(name="Redwood, European",      value=A("510"), category="Wood"),
		Density(name="Rosewood, Bolivian",     value=A("820"), category="Wood"),
		Density(name="Rosewood, East Indian",  value=A("900"), category="Wood"),
		Density(name="Sapele",                 value=A("640"), category="Wood"),
		Density(name="Spruce",                 value=A("450"), category="Wood"),
		Density(name="Sycamore",               value=A("500"), category="Wood"),
		Density(name="Teak, Indian",           value=A("820"), category="Wood"),
		Density(name="Teak, African",          value=A("980"), category="Wood"),
		Density(name="Teak, Burma",            value=A("740"), category="Wood"),
		Density(name="Utile",                  value=A("660"), category="Wood"),
		Density(name="Walnut",                 value=A("670"), category="Wood"),
		Density(name="Walnut, American Black", value=A("630"), category="Wood"),
		Density(name="Walnut, European",       value=A("570"), category="Wood"),
		Density(name="Willow",                 value=A("500"), category="Wood"),
		Density(name="Yew",                    value=A("670"), category="Wood"),
		Density(name="Zebrawood",              value=A("790"), category="Wood")
	)
}

fun Stack.density(name: String): ErrorCode {
	var value: AF? = null

	for (density in densities) {
		if ((density.name == name) || (density.name.replace(" ", "") == name)) {
			value = density.value
			break
		}
	}

	if (value == null) {
		return ErrorCode.UnknownConstant
	}
	else {
		this.push(value)
	}
	return ErrorCode.NoError
}

