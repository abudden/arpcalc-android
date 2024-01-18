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
	operator fun minus(b: AF) : AF {
		return this.value.subtract(b.value).toAF()
	}
	operator fun minus(b: Double) : AF {
		return this.minus(b.toAF())
	}
	operator fun times(b: AF) : AF {
		return this.value.multiply(b.value).toAF()
	}
	operator fun times(b: Double) : AF {
		return this.times(b.toAF())
	}
	operator fun plus(b: AF) : AF {
		return this.value.add(b.value).toAF()
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
		return this.value.remainder(b.value).toAF()
	}
	operator fun rem(b: Double) : AF {
		return this.rem(b.toAF())
	}

	fun remainder(b: AF) = this.rem(b)
	fun remainder(b: Double) = this.rem(b)

	fun round() : AF {
		return this.value.setScale(0, BigDecimal.ROUND_HALF_UP).toAF()
	}
	fun floor() : AF {
		return this.value.setScale(0, BigDecimal.ROUND_FLOOR).toAF()
	}
	fun ceil(): AF {
		return this.value.setScale(0, BigDecimal.ROUND_CEILING).toAF()
	}
	fun pow(x: AF) : AF {
		return BigDecimalMath.pow(this.value, x.value, this.context).toAF()
	}

	fun sqrt() : AF {
		return BigDecimalMath.sqrt(this.value, this.context).toAF()
	}
	fun cbrt() : AF {
		return this.root(3.0)
	}
	fun root(p: AF) : AF {
		return BigDecimalMath.root(this.value, p.value, this.context).toAF()
	}
	fun root(p: Double) : AF {
		return BigDecimalMath.root(this.value, p.toAF().value, this.context).toAF()
	}

	fun cos() = BigDecimalMath.cos(this.value, this.context).toAF()
	fun sin() = BigDecimalMath.sin(this.value, this.context).toAF()
	fun tan() = BigDecimalMath.tan(this.value, this.context).toAF()
	fun acos() = BigDecimalMath.acos(this.value, this.context).toAF()
	fun asin() = BigDecimalMath.asin(this.value, this.context).toAF()
	fun atan() = BigDecimalMath.atan(this.value, this.context).toAF()

	fun cosh() = BigDecimalMath.cosh(this.value, this.context).toAF()
	fun sinh() = BigDecimalMath.sinh(this.value, this.context).toAF()
	fun tanh() = BigDecimalMath.tanh(this.value, this.context).toAF()
	fun acosh() = BigDecimalMath.acosh(this.value, this.context).toAF()
	fun asinh() = BigDecimalMath.asinh(this.value, this.context).toAF()
	fun atanh() = BigDecimalMath.atanh(this.value, this.context).toAF()

	fun abs() : AF {
		return this.value.abs().toAF()
	}
	fun log(): AF {
		return BigDecimalMath.log(this.value, mc).toAF()
	}
	fun log10() : AF {
		return BigDecimalMath.log10(this.value, mc).toAF()
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
