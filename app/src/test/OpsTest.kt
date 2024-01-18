package uk.co.cgtk.karpcalc

/* Also tests some conversions (which used to be in Ops.kt */

import org.junit.Assert.*
import org.junit.Test

fun Stack.checkTop(v: Double, just_peek: Boolean = false) {
	val ALLOWED_DELTA = 1e-9
	var x: AF
	if (just_peek) {
		x = this.peek()
	}
	else {
		x = this.pop()
	}
	assertEquals(v, x.toDouble(), ALLOWED_DELTA)
}

fun Stack.checkTop(v: String, just_peek: Boolean = false) {
	var vf = A(v)
	var x: AF
	if (just_peek) {
		x = this.peek()
	}
	else {
		x = this.pop()
	}
	assertEquals(vf, x)
}

fun Stack.push(v: Double) {
	this.push(A(v))
}
fun Stack.push(v: List<Double>) {
	var va = v.map { A(it) }
	this.push(va)
}

public class OpsTest {
	private val ALLOWED_DELTA = 1e-9

	@Test
	fun defaultOpTests() {
		// Run tests with default options
		var st = Stack()
		
		// Most of these tests just check basic normal operation
		// rather than looking for faults with edge cases
		// Result values taken from my HP 33s calculator

		st.push(1.0)
		st.push(2.0)
		st.plus()
		st.checkTop(3.0)

		st.push(5.0)
		st.push(3.0)
		st.minus()
		st.checkTop(2.0)

		st.push(3.3)
		st.push(7.9)
		st.times()
		st.checkTop(26.07)

		st.push(7.0)
		st.push(3.5)
		st.divide()
		st.checkTop(2.0)

		st.push(7.0)
		st.push(0.0)
		assert(st.divide() == ErrorCode.DivideByZero)
		st.checkTop(0.0)
		st.checkTop(7.0)

		st.push(3.0)
		st.push(2.65)
		st.xrooty()
		st.checkTop(1.51372072297)

		st.push(3.0)
		st.invert()
		st.checkTop(-3.0)

		st.push(2.0)
		st.etox()
		st.checkTop(7.38905609893, true)
		st.loge()
		st.checkTop(2.0)

		st.push(2.0)
		st.tentox()
		st.checkTop(100.0)

		st.push(70.0)
		st.push(35.0)
		st.percent()
		st.checkTop(24.5)

		st.push(7.2)
		st.square()
		st.checkTop(51.84)

		st.push(7.2)
		st.cube()
		st.checkTop(373.248)

		st.push(8.0)
		st.reciprocal()
		st.checkTop(0.125)

		st.push(8.875)
		st.integerpart()
		st.checkTop(8.0)
		st.push(-8.875)
		st.integerpart()
		st.checkTop(-8.0)

		st.push(8.875)
		st.floatingpart()
		st.checkTop(0.875)
		st.push(-8.875)
		st.floatingpart()
		st.checkTop(-0.875)

		st.push(7.5)
		st.push(3.2)
		st.integerdivide()
		st.checkTop(2.0)

		st.bitCount = BitCount.bc32
		st.push(0x002378FF.toDouble())
		st.push(0x00727655.toDouble())
		st.bitwiseand()
		st.checkTop(0x00227055.toDouble())

		st.bitCount = BitCount.bc32
		st.push(0x002378FF.toDouble())
		st.push(0x00727655.toDouble())
		st.bitwiseor()
		st.checkTop(0x00737EFF.toDouble())

		st.bitCount = BitCount.bc32
		st.push(0x002378FF.toDouble())
		st.push(0x00727655.toDouble())
		st.bitwisexor()
		st.checkTop(0x00510EAA.toDouble())

		st.bitCount = BitCount.bc32
		st.push(0x002378FF.toDouble())
		st.bitwisenot()
		st.checkTop(0xFFDC8700.toDouble())

		st.bitCount = BitCount.bc16
		st.push(0x002378FF.toDouble())
		st.push(0x00727655.toDouble())
		st.bitwiseand()
		st.checkTop(0x00007055.toDouble())

		st.bitCount = BitCount.bc16
		st.push(0x002378FF.toDouble())
		st.push(0x00727655.toDouble())
		st.bitwiseor()
		st.checkTop(0x00007EFF.toDouble())

		st.bitCount = BitCount.bc16
		st.push(0x002378FF.toDouble())
		st.push(0x00727655.toDouble())
		st.bitwisexor()
		st.checkTop(0x00000EAA.toDouble())

		st.bitCount = BitCount.bc16
		st.push(0x002378FF.toDouble())
		st.bitwisenot()
		st.checkTop(0x00008700.toDouble())

		st.push(-8.0)
		st.absolute()
		st.checkTop(8.0, true)
		st.absolute()
		st.checkTop(8.0)

		st.push(1000.0)
		st.log10()
		st.checkTop(3.0)

		/* loge tested with etox above */

		st.push(256.0)
		st.log2()
		st.checkTop(8.0)

		st.push(3.5)
		st.ceiling()
		st.checkTop(4.0)
		st.push(-3.5)
		st.ceiling()
		st.checkTop(-3.0)

		st.push(3.5)
		st.floor()
		st.checkTop(3.0)
		st.push(-3.5)
		st.floor()
		st.checkTop(-4.0)

		st.push(60.0)
		st.sin()
		st.square()
		st.checkTop(0.75, true)
		st.squareroot()
		st.inversesin()
		st.checkTop(60.0)

		st.push(60.0)
		st.cos()
		st.checkTop(0.5, true)
		st.inversecos()
		st.checkTop(60.0)

		st.push(45.0)
		st.tan()
		st.checkTop(1.0, true)
		st.inversetan()
		st.checkTop(45.0)

		st.push(150.0)
		st.sin()
		st.push(5.0)
		st.times()
		st.push(150.0)
		st.cos()
		st.push(5.0)
		st.times()
		st.inversetan2()
		st.checkTop(150.0)

		st.push(3.3)
		st.round()
		st.checkTop(3.0)
		st.push(3.7)
		st.round()
		st.checkTop(4.0)
		st.push(5.5)
		st.round()
		st.checkTop(6.0)
		st.push(-3.3)
		st.round()
		st.checkTop(-3.0)
		st.push(-3.7)
		st.round()
		st.checkTop(-4.0)
		st.push(-5.5)
		st.round()
		st.checkTop(-6.0)

		st.push(49.0)
		st.squareroot()
		st.checkTop(7.0)

		st.push(8.0)
		st.cuberoot()
		st.checkTop(2.0)

		st.push(2.0)
		st.push(5.0)
		st.power()
		st.checkTop(32.0)

		st.push(3.0)
		st.push(5.0)
		st.swap()
		st.checkTop(3.0)
		st.checkTop(5.0)

		st.push(7.3)
		st.push(5.4)
		st.remainder()
		st.checkTop(1.9)

		st.push(15.3455222)
		st.convertHmsToHours()
		st.checkTop(15.5820061111, true)
		st.convertHoursToHms()
		st.checkTop(15.3455222)

		// TODO: Add test with HP for reference

		st.push(8.30)
		st.convertHmsToHours()
		st.checkTop(8.5, true)
		st.convertHoursToHms()
		st.checkTop(8.3)

		st.push(-15.3455222)
		st.convertHmsToHours()
		st.checkTop(-15.5820061111, true)
		st.convertHoursToHms()
		st.checkTop(-15.3455222)

		st.push(-8.30)
		st.convertHmsToHours()
		st.checkTop(-8.5, true)
		st.convertHoursToHms()
		st.checkTop(-8.3)

		st.constant("Pi")
		st.checkTop(Math.PI)
		
		st.constant("Euler's Number")
		st.checkTop(Math.E)

		st.push(5.0)
		st.convertGallonsToPints()
		st.checkTop(40.0, true)
		st.convertPintsToLitres()
		st.checkTop(22.7304594, true)
		st.convertLitresToPints()
		st.checkTop(40.0, true)
		st.convertPintsToGallons()
		st.checkTop(5.0, true)
		st.convertGallonsToPints()
		st.checkTop(40.0, true)
		st.convert("Volume", "Pints", "Fluid Ounces")
		st.checkTop(800.0, true)
		st.convert("Volume", "Fluid Ounces", "Pints")
		st.checkTop(40.0)

		st.push(5.0)
		st.convert("Volume", "Gallons", "Fluid Ounces")
		st.checkTop(800.0)
	}

	fun mathValueTest(startValue: Double, mathFun: (Double) -> Double, st: Stack, stackFun: () -> ErrorCode) {
		var javaMathVal = mathFun(startValue)
		st.push(startValue.toAF())
		stackFun()
		var afValue = st.pop().toDouble()
		assertEquals(javaMathVal, afValue, ALLOWED_DELTA)
	}

	@Test
	fun mathValueTests() {
		var st = Stack()
		st.setOption(CalcOpt.Radians, true)
		mathValueTest(10.8, { Math.cos(it)   }, st, st::cos)
		mathValueTest(10.8, { Math.sin(it)   }, st, st::sin)
		mathValueTest(10.8, { Math.tan(it)   }, st, st::tan)
		mathValueTest(10.8, { Math.log(it)   }, st, st::loge)
		mathValueTest(10.8, { Math.log10(it) }, st, st::log10)
		mathValueTest(10.8, { Math.log(it)/Math.log(2.0) }, st, st::log2)
		mathValueTest(10.8, { it*it }, st, st::square)
		mathValueTest(10.8, { it*it*it }, st, st::cube)
		mathValueTest(10.8, { Math.sqrt(it) }, st, st::squareroot)
		mathValueTest(10.8, { Math.cbrt(it) }, st, st::cuberoot)
		mathValueTest(10.8, { 1.0/it }, st, st::reciprocal)
	}
}
