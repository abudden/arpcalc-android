package uk.co.cgtk.karpcalc

import java.math.BigDecimal
import java.math.MathContext
import ch.obermuhlner.math.big.BigDecimalMath

// TODO: Consider changing this to UNLIMITED
var mc = MathContext.DECIMAL128
class AF(value: BigDecimal) {
	var value: BigDecimal
	var context = mc

	init {
		this.value = value
	}

	override operator fun equals(other: Any?) : Boolean =
		when (other) {
			is Int -> this == other.toAF()
			is Long -> this == other.toAF()
			is Double -> this == other.toAF()
			is String -> this == other.toAF()
			is AF -> this.value.compareTo(other.value) == 0
			else -> false
		}
	
	operator fun compareTo(other: Any?) : Int =
		when (other) {
			is Int -> this.compareTo(other.toAF())
			is Long -> this.compareTo(other.toAF())
			is Double -> this.compareTo(other.toAF())
			is String -> this.compareTo(other.toAF())
			is AF -> this.value.compareTo(other.value)
			else -> -1
		}

	operator fun unaryMinus() : AF {
		return this.value.unaryMinus().toAF()
	}
	/* Rounded to the working precision (as divide always was): exact
	 * results can need any number of digits (e.g. 1e500000 + 1), which
	 * makes everything after them slow.
	 */
	operator fun minus(b: AF) : AF {
		return this.value.subtract(b.value, mc).toAF()
	}
	operator fun minus(b: Double) : AF {
		return this.minus(b.toAF())
	}
	operator fun times(b: AF) : AF {
		return this.value.multiply(b.value, mc).toAF()
	}
	operator fun times(b: Double) : AF {
		return this.times(b.toAF())
	}
	operator fun plus(b: AF) : AF {
		return this.value.add(b.value, mc).toAF()
	}
	operator fun plus(b: Double) : AF {
		return this.plus(b.toAF())
	}
	operator fun div(b: AF) : AF {
		return this.value.divide(b.value, mc).toAF()
	}
	operator fun div(b: Double) : AF {
		return this.div(b.toAF())
	}
	operator fun rem(b: AF) : AF {
		/* The remainder is exact, so needs the whole of the quotient: with
		 * a huge quotient that takes practically forever.
		 */
		if ((this.value.signum() != 0) && (b.value.signum() != 0)
				&& ((this.exponent10() - b.exponent10()) > 10000)) {
			throw ArithmeticException("Remainder out of range")
		}
		return this.value.remainder(b.value).toAF()
	}
	operator fun rem(b: Double) : AF {
		return this.rem(b.toAF())
	}

	fun remainder(b: AF) = this.rem(b)
	fun remainder(b: Double) = this.rem(b)

	/* Rounding a huge number would write out all of its digits (which can
	 * take practically forever), but it's already an integer.
	 */
	fun isInteger() = (this.value.scale() <= 0)

	/* Rounding a tiny number (e.g. 1e-1000000) would also write out all of
	 * its digits; anything below 0.01 rounds to 0, or for floor and ceil
	 * to -1 or 1 depending on its sign.
	 */
	private fun isSmall() = (this.value.signum() != 0) && (this.exponent10() < -2)

	fun round() : AF {
		if (this.isInteger()) {
			return this
		}
		if (this.isSmall()) {
			return A(0)
		}
		return this.value.setScale(0, BigDecimal.ROUND_HALF_UP).toAF()
	}
	fun floor() : AF {
		if (this.isInteger()) {
			return this
		}
		if (this.isSmall()) {
			return A(if (this.value.signum() > 0) 0 else -1)
		}
		return this.value.setScale(0, BigDecimal.ROUND_FLOOR).toAF()
	}
	fun ceil(): AF {
		if (this.isInteger()) {
			return this
		}
		if (this.isSmall()) {
			return A(if (this.value.signum() > 0) 1 else 0)
		}
		return this.value.setScale(0, BigDecimal.ROUND_CEILING).toAF()
	}
	fun pow(x: AF) : AF {
		/* The result would be beyond BigDecimal's range (10^±2,147,483,647)
		 * and big-math can take practically forever to find that out.
		 */
		if ((this.value.signum() != 0) && (x.abs() > A(1000))
				&& ((this.abs().log10() * x).abs() > A(2.1e9))) {
			throw ArithmeticException("Power out of range")
		}
		return BigDecimalMath.pow(this.value, x.value, this.context).toAF()
	}

	/* Rounded to a number of decimal places, e.g. so that minutes worked
	 * out as 55.999... (from rounding in an earlier division) are 56 before
	 * taking the floor.  A huge number has no digits that far down.
	 */
	fun roundToPlaces(places: Int) : AF {
		if ((this.value.scale() <= places) || (this.exponent10() > 100)) {
			return this
		}
		return this.value.setScale(places, BigDecimal.ROUND_HALF_UP).toAF()
	}

	/* The power of ten in this number (as in scientific notation) */
	fun exponent10(): Long = this.value.precision().toLong() - this.value.scale().toLong() - 1

	fun sqrt() : AF {
		return this.root(2)
	}
	fun cbrt() : AF {
		return this.root(3)
	}
	fun root(p: AF) : AF {
		if ((p.isInteger()) && (p.abs() <= A(1000))) {
			return this.root(p.toInt())
		}
		return BigDecimalMath.root(this.value, p.value, this.context).toAF()
	}
	fun root(p: Double) : AF {
		return this.root(p.toAF())
	}
	/* big-math's root takes practically forever for very large or very
	 * small numbers, so take out a power of ten that's a multiple of p and
	 * do that part exactly.
	 */
	fun root(p: Int) : AF {
		if ((p <= 0) || (this.value.signum() == 0)) {
			return BigDecimalMath.root(this.value, BigDecimal(p), this.context).toAF()
		}
		val shift = Math.floorDiv(this.exponent10(), p.toLong()) * p
		val scaled = this.value.scaleByPowerOfTen(-shift.toInt())
		val r = BigDecimalMath.root(scaled, BigDecimal(p), this.context)
		return r.scaleByPowerOfTen((shift / p).toInt()).round(this.context).toAF()
	}

	fun cos() = BigDecimalMath.cos(this.value, this.context).toAF()
	fun sin() = BigDecimalMath.sin(this.value, this.context).toAF()
	fun tan() = BigDecimalMath.tan(this.value, this.context).toAF()
	/* big-math's inverse functions take time in proportion to the size of
	 * the argument's exponent (seconds for 1e1000000) and overflow or
	 * underflow near BigDecimal's limits, so use identities that are exact
	 * at this precision: for |x| > 1, atan(x) = ±π/2 - atan(1/x); and below
	 * 1e-40, atan(x), asin(x) and the inverse hyperbolic sine and tangent are
	 * x (the next term is x³/3, 80 digits further down).
	 */
	private fun isTiny() = (this.value.signum() != 0) && (this.exponent10() < -40)

	fun acos() : AF {
		if (this.isTiny()) {
			return (pi() / 2.0) - this
		}
		return BigDecimalMath.acos(this.value, this.context).toAF()
	}
	fun asin() : AF {
		if (this.isTiny()) {
			return this
		}
		return BigDecimalMath.asin(this.value, this.context).toAF()
	}
	fun atan() : AF {
		if (this.isTiny()) {
			return this
		}
		if (this.abs() > A(1)) {
			val halfPi = pi() / 2.0
			val sign = if (this.value.signum() > 0) halfPi else -halfPi
			return sign - (A(1) / this).atan()
		}
		return BigDecimalMath.atan(this.value, this.context).toAF()
	}

	/* big-math's hyperbolic functions take practically forever for large
	 * arguments (seconds for 1000, much longer for 10,000).  Beyond 100, e^-x
	 * is too small to make a difference at this precision, so use e^x / 2
	 * (and tanh is just +/-1).
	 */
	fun cosh() : AF {
		if (this.abs() > A(100)) {
			return (e().pow(this.abs()) / 2.0)
		}
		return BigDecimalMath.cosh(this.value, this.context).toAF()
	}
	fun sinh() : AF {
		if (this > A(100)) {
			return (e().pow(this) / 2.0)
		}
		else if (this < A(-100)) {
			return -(e().pow(-this) / 2.0)
		}
		return BigDecimalMath.sinh(this.value, this.context).toAF()
	}
	fun tanh() : AF {
		if (this > A(100)) {
			return A(1)
		}
		else if (this < A(-100)) {
			return A(-1)
		}
		return BigDecimalMath.tanh(this.value, this.context).toAF()
	}
	/* For x > 1e20, acosh(x) and asinh(x) are ln(2x) (the next term is
	 * 1/(4x²), 40 digits further down); asinh(-x) is -asinh(x).
	 */
	fun acosh() : AF {
		if (this > A("1e20")) {
			return (this * 2.0).log()
		}
		return BigDecimalMath.acosh(this.value, this.context).toAF()
	}
	fun asinh() : AF {
		if (this.value.signum() < 0) {
			return -((-this).asinh())
		}
		if (this.isTiny()) {
			return this
		}
		if (this > A("1e20")) {
			return (this * 2.0).log()
		}
		return BigDecimalMath.asinh(this.value, this.context).toAF()
	}
	fun atanh() : AF {
		if (this.isTiny()) {
			return this
		}
		return BigDecimalMath.atanh(this.value, this.context).toAF()
	}

	fun abs() : AF {
		return this.value.abs().toAF()
	}
	/* big-math's logs go via a Double, so fail for numbers too large or
	 * too small for one (beyond about 1e±308).  Take out the power of ten
	 * first: log(m * 10^e) = log(m) + e * log(10).
	 */
	fun log(): AF {
		return this.logWithBase(false)
	}
	fun log10() : AF {
		return this.logWithBase(true)
	}
	private fun logWithBase(base10: Boolean): AF {
		val e10 = this.exponent10()
		if ((this.value.signum() <= 0) || (Math.abs(e10) < 300)) {
			if (base10) {
				return BigDecimalMath.log10(this.value, mc).toAF()
			}
			return BigDecimalMath.log(this.value, mc).toAF()
		}
		val mc2 = MathContext(mc.precision + 20)
		val mantissa = this.value.scaleByPowerOfTen(-e10.toInt())
		val result = if (base10) {
			BigDecimalMath.log10(mantissa, mc2).add(BigDecimal(e10))
		}
		else {
			BigDecimalMath.log(mantissa, mc2).add(
					BigDecimal(e10).multiply(BigDecimalMath.log(BigDecimal.TEN, mc2)))
		}
		return result.round(mc).toAF()
	}

	fun toDouble(): Double {
		return this.value.toDouble()
	}
	fun toLong(): Long {
		return this.value.toLong()
	}
	fun toInt(): Int {
		return this.value.toInt()
	}
	override fun toString(): String {
		return this.value.toString()
	}

	companion object {
		fun from(v: Long) : AF {
			return AF(BigDecimal(v, mc))
		}
		fun from(v: Int) : AF {
			return AF(BigDecimal(v, mc))
		}
		fun from(v: String) : AF {
			return AF(BigDecimal(v, mc))
		}
		fun from(v: Double) : AF {
			return AF(BigDecimal(v, mc))
		}
		fun from(v: BigDecimal) : AF {
			return AF(v)
		}

		// Repeated functions due to duplicate names
		// in Stack class
		fun floor(v: AF) = v.floor()
		fun round(v: AF) = v.round()
		fun log(v: AF) = v.log()
		fun log10(v: AF) = v.log10()
		fun log10(v: Double) = v.toAF().log10()

		fun sqrt(v: AF) = v.sqrt()
		fun cbrt(v: AF) = v.cbrt()
		fun root(v: AF, p: AF) = v.root(p)
		fun root(v: AF, p: Double) = v.root(p)

		fun cos(v: AF) = v.cos()
		fun sin(v: AF) = v.sin()
		fun tan(v: AF) = v.tan()
		fun acos(v: AF) = v.acos()
		fun asin(v: AF) = v.asin()
		fun atan(v: AF) = v.atan()
		fun cosh(v: AF) = v.cosh()
		fun sinh(v: AF) = v.sinh()
		fun tanh(v: AF) = v.tanh()
		fun acosh(v: AF) = v.acosh()
		fun asinh(v: AF) = v.asinh()
		fun atanh(v: AF) = v.atanh()
	}
}

fun Long.toAF() = AF.from(this)
fun Int.toAF() = AF.from(this)
fun String.toAF() = AF.from(this)
fun Double.toAF() = AF.from(this)
fun BigDecimal.toAF() = AF.from(this)

operator fun Double.div(other: AF) = this.toAF().div(other)
operator fun Double.times(other: AF) = this.toAF().times(other)
operator fun Double.minus(other: AF) = this.toAF().minus(other)
operator fun Double.plus(other: AF) = this.toAF().plus(other)

// Short-hand to allow == A(2.0)
fun A(v: Double) = AF.from(v)
fun A(v: Int) = AF.from(v)
fun A(v: String) = AF.from(v)

fun pi() = BigDecimalMath.pi(mc).toAF()
fun e() = BigDecimalMath.e(mc).toAF()

fun pow(y: AF, x: AF) = y.pow(x)
fun pow(y: Double, x: AF) = y.toAF().pow(x)
fun pow(y: Double, x: Double) = y.toAF().pow(x.toAF())
fun pow(y: Double, x: Int) = y.toAF().pow(x.toAF())
fun round(v: AF) = v.round()
fun log(v: AF) = v.log()
fun log10(v: AF) = v.log10()
fun log10(v: Double) = v.toAF().log10()
fun abs(v: AF) = v.abs()
fun sqrt(v: AF) = v.sqrt()
fun cbrt(v: AF) = v.cbrt()
fun root(v: AF, p: AF) = v.root(p)
fun root(v: AF, p: Double) = v.root(p)
