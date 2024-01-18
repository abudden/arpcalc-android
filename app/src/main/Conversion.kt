package uk.co.cgtk.karpcalc

import java.util.Calendar
import java.time.Year
import java.time.LocalDate

data class Conversion(var from: String, var to: String, var func: () -> ErrorCode)

fun Stack.convertKelvinToCelsius(): ErrorCode {
	this.push(this.pop() - 273.15)
	return ErrorCode.NoError
}
fun Stack.convertCelsiusToKelvin(): ErrorCode {
	this.push(this.pop() + 273.15)
	return ErrorCode.NoError
}
fun Stack.convertFahrenheitToCelsius(): ErrorCode {
	this.push((this.pop()-32.0)*5.0/9.0)
	return ErrorCode.NoError
}
fun Stack.convertCelsiusToFahrenheit(): ErrorCode {
	this.push((this.pop()*9.0/5.0)+32.0)
	return ErrorCode.NoError
}
fun Stack.convertPintsToLitres(reverse: Boolean = false, american: Boolean = false): ErrorCode {
	var multiplier: Double
	if (american) {
		multiplier = 0.47317648
	}
	else {
		multiplier = 0.568261485
	}

	if (reverse) {
		multiplier = 1.0/multiplier
	}
	this.push(this.pop()*multiplier)
	return ErrorCode.NoError
}
fun Stack.convertGallonsToPints(): ErrorCode {
	return this.convertMultiplier(8.0)
}
fun Stack.convertLitresToPints(american: Boolean = false): ErrorCode {
	return this.convertPintsToLitres(true, american=american)
}
fun Stack.convertGallonsToLitres(): ErrorCode {
	this.convertGallonsToPints()
	return this.convertPintsToLitres()
}
fun Stack.convertPintsToGallons(): ErrorCode {
	return this.convertMultiplier(1.0/8.0)
}
fun Stack.convertLitresToGallons(): ErrorCode {
	this.convertPintsToLitres(reverse=true)
	return this.convertPintsToGallons()
}
fun Stack.convertInchToMM(): ErrorCode {
	return this.convertMultiplier(25.4)
}
fun Stack.convertMMToInch(): ErrorCode {
	return this.convertMultiplier(1.0/25.4)
}
fun Stack.convertRadiansToDegrees(): ErrorCode {
	return this.convertMultiplier(180.0/pi())
}
fun Stack.convertDegreesToRadians(): ErrorCode {
	return this.convertMultiplier(pi()/180.0)
}
fun Stack.convertMilesToKilometres(): ErrorCode {
	return this.convertMultiplier(1.609344)
}
fun Stack.convertKilometresToMiles(): ErrorCode {
	return this.convertMultiplier(1.0/1.609344)
}
fun Stack.convertRPMToHertz(): ErrorCode {
	return this.convertMultiplier(1.0/60.0)
}
fun Stack.convertHertzToRPM(): ErrorCode {
	return this.convertMultiplier(60.0)
}
fun Stack.convertOuncesToGrams(): ErrorCode {
	return this.convertMultiplier(28.3495231)
}
fun Stack.convertGramsToOunces(): ErrorCode {
	return this.convertMultiplier(1.0/28.3495231)
}
fun Stack.convertNewtonMetresToPoundsFeet(): ErrorCode {
	return this.convertMultiplier(0.737562149277)
}
fun Stack.convertPoundsFeetToNewtonMetres(): ErrorCode {
	return this.convertMultiplier(1.0/0.737562149277)
}
fun Stack.convertKilogramsToPounds(): ErrorCode {
	return this.convertMultiplier(1/0.45359237)
}
fun Stack.convertPoundsToKilograms(): ErrorCode {
	return this.convertMultiplier(0.45359237)
}
fun Stack.convertKilogramsToStone(): ErrorCode {
	return this.convertMultiplier(2.2046228/14.0)
}
fun Stack.convertStoneToKilograms(): ErrorCode {
	return this.convertMultiplier(14.0/2.2046228)
}
fun Stack.convertGramsToKilograms(): ErrorCode {
	return this.convertMultiplier(1.0/1000.0)
}
fun Stack.convertKilogramsToGrams(): ErrorCode {
	return this.convertMultiplier(1000.0)
}
fun Stack.convertRadPerSecToHertz(): ErrorCode {
	return this.convertMultiplier(1.0/(2.0*pi()))
}
fun Stack.convertHertzToRadPerSec(): ErrorCode {
	return this.convertMultiplier(2.0*pi())
}

fun Stack.convertKilometresPerLitreToLitresPer100KM(): ErrorCode {
	var kmpl = this.pop()
	if (kmpl == A(0.0)) {
		this.push(kmpl)
		return ErrorCode.DivideByZero
	}
	var lpkm = 1.0 / kmpl
	var lp100km = 100.0 * lpkm
	this.push(lp100km)
	return ErrorCode.NoError
}

fun Stack.convertLitresPer100KMToKilometresPerLitre(): ErrorCode {
	var lp100km = this.pop()
	if (lp100km == A(0.0)) {
		this.push(lp100km)
		return ErrorCode.DivideByZero
	}
	var lpkm = lp100km / 100.0
	var kmpl = 1.0 / lpkm
	this.push(kmpl)
	return ErrorCode.NoError
}

fun Stack.convertDayOfYearToDateInCurrentYear(): ErrorCode {
	var dayofyear = this.pop()
	if ((dayofyear < 0) || (dayofyear > 365)) {
		this.push(dayofyear)
		return ErrorCode.InvalidConversion
	}

	var dayofyear_i = dayofyear.floor().toInt()
	var fp = dayofyear - dayofyear.floor()
	fp *= A("10000")
	var year: Int = fp.round().toInt()

	print("Day of year input ${dayofyear.toString()} became ${dayofyear_i.toString()} and ${year.toString()}\n")

	if (year < 1600) {
		var c = Calendar.getInstance()
		year = c.get(Calendar.YEAR)
	}

	var yearObj = Year.of(year)
	var ld: LocalDate = yearObj.atDay(dayofyear_i)

	var dom: Int = ld.getDayOfMonth()
	var m: Int = ld.getMonthValue()

	var result = A(year.toDouble())
	result /= A("10000")
	result += m.toDouble()
	result /= A("100")
	result += dom.toDouble()

	this.push(result)

	return ErrorCode.NoError
}

fun Stack.convertDateInCurrentYearToDayOfYear(): ErrorCode {
	var xval = this.pop()

	var date_form = xval;

	var dom = date_form.floor().toInt()
	date_form -= date_form.floor()
	date_form *= A("100")
	var m = date_form.floor().toInt()
	date_form -= date_form.floor()
	date_form *= A("10000")
	var year = date_form.floor().toInt()
	if (year < 1600) {
		var c = Calendar.getInstance()
		year = c.get(Calendar.YEAR)
	}

	print("Date input ${xval.toString()} became ${dom.toString()}/${m.toString()}/${year.toString()}\n")

	try {
		var ld: LocalDate = LocalDate.of(year, m, dom)

		var result = A(ld.getDayOfYear())
		result += (A(year) / A("10000"))

		this.push(result)

		return ErrorCode.NoError
	} catch (e: java.time.DateTimeException) {
		this.push(xval)
		return ErrorCode.InvalidConversion
	}
}

fun Stack.convertMultiplier(mult: Double): ErrorCode {
	this.push(this.pop()*mult)
	return ErrorCode.NoError
}

fun Stack.convertMultiplier(mult: AF): ErrorCode {
	this.push(this.pop()*mult)
	return ErrorCode.NoError
}

fun Stack.populateConversionTable() {
	this.unitSymbols = mapOf(
		"Acres" to "ac",
		"Angstroms" to "&Aring;",
		"Atmosphere" to "atm",
		"Bar" to "bar",
		"Bytes" to "B",
		"Calories" to "cal",
		"Celsius" to "&deg;C",
		"Centimetres" to "cm",
		"Chains" to "ch",
		"Cubic Centimetres" to "cc",
		"Cubic Decimetres" to "dm<sup><small>3</small></sup>",
		"Cubic Feet" to "cu ft",
		"Cubic Inches" to "cu in",
		"Cubic Metres" to "m<sup><small>3</small></sup>",
		"Cubic Millimetres" to "mm<sup><small>3</small></sup>",
		"Cubic Yards" to "cu yd",
		"Days" to "days",
		"Day of Year" to "N.(Y)",
		"Date in Year" to "D.M(Y)",
		"Degrees" to "deg",
		"Degrees.Minutes" to "D.M",
		"Degrees.Minutes-Seconds" to "DMS",
		"Fahrenheit" to "&deg;F",
		"Fathoms" to "fm",
		"Feet Per Second" to "ft/s",
		"Feet" to "ft",
		"Fluid Ounces" to "fl.oz",
		"Furlongs" to "furlongs",
		"Gallons" to "gal",
		"Gibibytes" to "GiB",
		"Gigabytes" to "GB",
		"Grams" to "g",
		"Gram-Force" to "gf",
		"Gram-Force Centimetres" to "gf cm",
		"Gram-Force Millimetres" to "gf mm",
		"Gram-Force Metres" to "gf m",
		"Hectares" to "ha",
		"Hectopascal" to "hPa",
		"Hertz" to "Hz",
		"Horsepower (Mech)" to "hp<sub><small>mech</small></sub>",
		"Horsepower (Metric)" to "hp<sub><small>met</small></sub>",
		"Hours" to "hr",
		"Hours.Minutes-Seconds" to "HMS",
		"Hundredweight" to "cwt",
		"Inches of Mercury" to "in Hg",
		"Inches" to "in",
		"Joules" to "J",
		"Kelvin" to "K",
		"Kibibytes" to "KiB",
		"Kilobytes" to "kB",
		"Kilocalories" to "kcal",
		"Kilogram-Force" to "kgf",
		"Kilograms Per Sq. cm" to "kg/cm<sup><small>2</small></sup>",
		"Kilograms" to "kg",
		"Kilogram-Force Centimetres" to "kgf cm",
		"Kilogram-Force Millimetres" to "kgf mm",
		"Kilogram-Force Metres" to "kgf m",
		"Kilojoules" to "kJ",
		"Kilometres Per Hour" to "km/h",
		"Kilometres Per Litre" to "km/l",
		"Kilometres" to "km",
		"Kilonewtons" to "kN",
		"Kilopascal" to "kPa",
		"Kilowatt-Hours" to "kWh",
		"Kilowatts" to "kW",
		"Knots" to "kt",
		"Light Years" to "ly",
		"Litres Per 100 Kilometres" to "l/100 km",
		"Litres" to "l",
		"Mebibytes" to "MiB",
		"Megabytes" to "MB",
		"Megajoules" to "MJ",
		"Megapascal" to "MPa",
		"Metres Per Second" to "m/s",
		"Metres Per Hour" to "m/hr",
		"Metres" to "m",
		"Microgram" to "&mu;g",
		"Micrometres" to "&mu;m",
		"Micronewtons" to "&mu;N",
		"Microns" to "&mu;m",
		"Microseconds" to "&mu;s",
		"Miles Per Gallon" to "mpg",
		"Miles Per US Gallon" to "mpg<sub><small>US</small></sub>",
		"Miles Per Litre" to "mpl",
		"Miles Per Hour" to "mph",
		"Miles" to "miles",
		"Millibar" to "mbar",
		"Milligrams" to "mg",
		"Millilitres" to "ml",
		"Millimetres" to "mm",
		"Millinewtons" to "mN",
		"Milliseconds" to "ms",
		"Mils" to "mil",
		"Minutes" to "min",
		"Nanometres" to "nm",
		"Nanoseconds" to "ns",
		"Nautical Miles" to "nm",
		"Newton Centimetres" to "N cm",
		"Newton Millimetres" to "N mm",
		"Newton Metres" to "Nm",
		"Newtons" to "N",
		"Ounces" to "oz",
		"Ounce-Force" to "ozf",
		"Ounce-Force Feet" to "ozf-ft",
		"Ounce-Force Inches" to "ozf-in",
		"Pascal" to "Pa",
		"Pints" to "pt",
		"Points" to "pt",
		"Pound-Force" to "lb<sub><small>F</small></sub>",
		"Pound-Force Feet" to "lbf ft",
		"Pound-Force Inches" to "lbf in",
		"Pounds Per Sq. Inch" to "psi",
		"Pounds" to "lb",
		"RPM" to "RPM",
		"Radians Per Second" to "rad/s",
		"Radians" to "rad",
		"Seconds" to "s",
		"Sq. Centimetres" to "cm<sup><small>2</small></sup>",
		"Sq. Feet" to "sq ft",
		"Sq. Inches" to "sq in",
		"Sq. Kilometres" to "km<sup><small>2</small></sup>",
		"Sq. Metres" to "m<sup><small>2</small></sup>",
		"Sq. Miles" to "sq mi",
		"Sq. Millimetres" to "mm<sup><small>2</small></sup>",
		"Sq. Yards" to "sq yd",
		"Stone" to "st",
		"Tebibytes" to "TiB",
		"Terabytes" to "TB",
		"Thou" to "th",
		"Tonnes" to "t",
		"Tons" to "ton",
		"Torr" to "Torr",
		"US Fluid Ounces" to "fl.oz<sub><small>US</small></sub>",
		"US Gallons" to "gal<sub><small>US</small></sub>",
		"US Hundredweight" to "cwt<sub><small>US</small></sub>",
		"US Pints" to "pt<sub><small>US</small></sub>",
		"US Tons" to "ton<sub><small>US</small></sub>",
		"Watts" to "W",
		"Weeks" to "wk",
		"Yards" to "yd",
		// "British Thermal Units" to "BTU",
		// "Years (Gregorian)" to "a<sub><small>g</small></sub>",
		// "Years (Julian)" to "a<sub><small>j</small></sub>",
		"Calories Per Second" to "cal/s",
		"Megawatts" to "MW",
		// Currencies
		"Australian Dollar" to "AUD",
		"Bulgarian Lev" to "BGN",
		"Brazilian Real" to "BRL",
		"Canadian Dollar" to "CAD",
		"Swiss Franc" to "CHF",
		"Chinese Yuan" to "CNY",
		"Czech Koruna" to "CZK",
		"Danish Krone" to "DKK",
		"Euro" to "EUR / &euro;",
		"United Kingdom Pound" to "GBP / &pound;",
		"Hong Kong Dollar" to "HKD",
		"Croatian Kuna" to "HRK",
		"Hungarian Forint" to "HUF",
		"Indonesian Rupiah" to "IDR",
		"Israeli New Shekel" to "ILS",
		"Indian Rupee" to "INR",
		"Icelandic Krona" to "ISK",
		"Japanese Yen" to "JPY",
		"South Korean Won" to "KRW",
		"Mexican Peso" to "MXN",
		"Malaysian Ringgit" to "MYR",
		"Norwegian Krone" to "NOK",
		"New Zealand Dollar" to "NZD",
		"Philippine Peso" to "PHP",
		"Polish Z&#0322;oty" to "PLN",
		"Romanian Leu" to "RON",
		"Russian Ruble" to "RUB",
		"Swedish Krona" to "SEK",
		"Singapore Dollar" to "SGD",
		"Thai Baht - Baht" to "THB",
		"Turkish Lira" to "TRY",
		"US Dollar" to "USD / $",
		"South African Rand" to "ZAR"
	)
	this.currencyMap = mapOf(
		"AUD" to "Australian Dollars",
		"BGN" to "Bulgarian Levs",
		"BRL" to "Brazilian Real",
		"CAD" to "Canadian Dollars",
		"CHF" to "Swiss Francs",
		"CNY" to "Chinese Yuan",
		"CZK" to "Czech Koruna",
		"DKK" to "Danish Krone",
		"EUR" to "Euros",
		"GBP" to "GB Pounds",
		"HKD" to "Hong Kong Dollars",
		"HRK" to "Croatian Kuna",
		"HUF" to "Hungarian Forint",
		"IDR" to "Indonesian Rupiah",
		"ILS" to "Israeli New Shekels",
		"INR" to "Indian Rupees",
		"ISK" to "Icelandic Krona",
		"JPY" to "Japanese Yen",
		"KRW" to "South Korean Won",
		"MXN" to "Mexican Pesos",
		"MYR" to "Malaysian Ringgit",
		"NOK" to "Norwegian Krone",
		"NZD" to "New Zealand Dollars",
		"PHP" to "Philippine Pesos",
		"PLN" to "Polish Z&#0322;oty",
		"RON" to "Romanian Leu",
		"RUB" to "Russian Rubles",
		"SEK" to "Swedish Krona",
		"SGD" to "Singapore Dollars",
		"THB" to "Thai Baht",
		"TRY" to "Turkish Lira",
		"USD" to "US Dollars",
		"ZAR" to "South African Rand"
	)
	this.conversionTable = mutableMapOf(
		/* Fluid volume conversions */
		"Volume" to listOf(
			Conversion("Pints", "Fluid Ounces", { this.convertMultiplier(20.0) }),
			Conversion("Fluid Ounces", "Pints", { this.convertMultiplier(1.0/20.0) }),
			Conversion("US Fluid Ounces", "Cubic Inches", { this.convertMultiplier(1.8046875) } ),
			Conversion("Cubic Inches", "US Fluid Ounces", { this.convertMultiplier(1.0/1.8046875) } ),
			Conversion("US Pints", "US Fluid Ounces", { this.convertMultiplier(16.0) } ),
			Conversion("US Fluid Ounces", "US Pints", { this.convertMultiplier(1.0/16.0) } ),
			Conversion("Pints", "Gallons", { this.convertPintsToGallons() }),
			Conversion("Gallons", "Pints", { this.convertGallonsToPints() }),
			Conversion("US Pints", "US Gallons", { this.convertPintsToGallons() }),
			Conversion("US Gallons", "US Pints", { this.convertGallonsToPints() }),
			Conversion("Pints", "Litres", { this.convertPintsToLitres() }),
			Conversion("Litres", "Pints", { this.convertLitresToPints() }),
			Conversion("US Pints", "Litres", { this.convertPintsToLitres(american=true) } ),
			Conversion("Litres", "US Pints", { this.convertLitresToPints(american=true) } ),
			Conversion("Millilitres", "Litres", { this.convertMultiplier(1.0/1000.0) } ),
			Conversion("Litres", "Millilitres", { this.convertMultiplier(1000.0) } ),
			Conversion("Millilitres", "Cubic Centimetres", { ErrorCode.NoError } ),
			Conversion("Cubic Centimetres", "Millilitres", { ErrorCode.NoError } ),
			Conversion("Litres", "Cubic Metres", { this.convertMultiplier(1.0/1000.0) } ),
			Conversion("Cubic Metres", "Litres", { this.convertMultiplier(1000.0) } ),
			Conversion("Cubic Millimetres", "Cubic Metres", { this.convertMultiplier(1.0e-9) } ),
			Conversion("Cubic Metres", "Cubic Millimetres", { this.convertMultiplier(1.0e9) } ),
			Conversion("Cubic Decimetres", "Cubic Metres", { this.convertMultiplier(1.0/1000.0) } ),
			Conversion("Cubic Metres", "Cubic Decimetres", { this.convertMultiplier(1000.0) } ),
			Conversion("Millilitres", "Cubic Inches", { this.convertMultiplier(1.0/(2.54*2.54*2.54)) } ),
			Conversion("Cubic Inches", "Millilitres", { this.convertMultiplier(2.54*2.54*2.54) } ),
			Conversion("Cubic Inches", "Cubic Feet", { this.convertMultiplier(1.0/1728.0) } ),
			Conversion("Cubic Feet", "Cubic Inches", { this.convertMultiplier(1728.0) } ),
			Conversion("Cubic Feet", "Cubic Yards", { this.convertMultiplier(1.0/27.0) } ),
			Conversion("Cubic Yards", "Cubic Feet", { this.convertMultiplier(27.0) } )
		),
		/* Weight conversions */
		"Mass" to listOf(
			Conversion("Ounces", "Grams", { this.convertOuncesToGrams() }),
			Conversion("Grams", "Ounces", { this.convertGramsToOunces() }),
			Conversion("Grams", "Kilograms", { this.convertGramsToKilograms() }),
			Conversion("Kilograms", "Grams", { this.convertKilogramsToGrams() }),
			Conversion("Kilograms", "Pounds", { this.convertKilogramsToPounds() }),
			Conversion("Pounds", "Kilograms", { this.convertPoundsToKilograms() }),
			Conversion("Kilograms", "Stone", { this.convertKilogramsToStone() }),
			Conversion("Stone", "Kilograms", { this.convertStoneToKilograms() }),
			Conversion("Grams", "Microgram", { this.convertMultiplier(1e6) } ),
			Conversion("Microgram", "Grams", { this.convertMultiplier(1.0/(1e6)) } ),
			Conversion("Grams", "Milligrams", { this.convertMultiplier(1000.0) } ),
			Conversion("Milligrams", "Grams", { this.convertMultiplier(1.0/1000.0) } ),
			Conversion("Tonnes", "Kilograms", { this.convertMultiplier(1000.0) } ),
			Conversion("Kilograms", "Tonnes", { this.convertMultiplier(1.0/1000.0) } ),
			Conversion("Stone", "Hundredweight", { this.convertMultiplier(1.0/8.0) } ),
			Conversion("Hundredweight", "Stone", { this.convertMultiplier(8.0) } ),
			Conversion("Pounds", "US Hundredweight", { this.convertMultiplier(1.0/100.0) } ),
			Conversion("US Hundredweight", "Pounds", { this.convertMultiplier(100.0) } ),
			Conversion("Hundredweight", "Tons", { this.convertMultiplier(1.0/20.0) } ),
			Conversion("Tons", "Hundredweight", { this.convertMultiplier(20.0) } ),
			Conversion("Pounds", "US Tons", { this.convertMultiplier(1.0/2000.0) } ),
			Conversion("US Tons", "Pounds", { this.convertMultiplier(2000.0) } )
		),
		/* Torque conversions */
		"Torque" to listOf(
			Conversion("Pound-Force Feet", "Newton Metres", { this.convertPoundsFeetToNewtonMetres() }),
			Conversion("Newton Metres", "Pound-Force Feet", { this.convertNewtonMetresToPoundsFeet() }),
			Conversion("Newton Metres", "Newton Centimetres", { this.convertMultiplier(100.0) }),
			Conversion("Newton Centimetres", "Newton Metres", { this.convertMultiplier(1.0/100.0) }),
			Conversion("Newton Metres", "Newton Millimetres", { this.convertMultiplier(1000.0) }),
			Conversion("Newton Millimetres", "Newton Metres", { this.convertMultiplier(1.0/1000.0) }),
			Conversion("Newton Metres", "Kilogram-Force Metres", { this.convertMultiplier(1.0/9.80665) }),
			Conversion("Kilogram-Force Metres", "Newton Metres", { this.convertMultiplier(9.80665) }),
			Conversion("Kilogram-Force Metres", "Kilogram-Force Centimetres", { this.convertMultiplier(100.0) }),
			Conversion("Kilogram-Force Centimetres", "Kilogram-Force Metres", { this.convertMultiplier(1.0/100.0) }),
			Conversion("Kilogram-Force Metres", "Kilogram-Force Millimetres", { this.convertMultiplier(1000.0) }),
			Conversion("Kilogram-Force Millimetres", "Kilogram-Force Metres", { this.convertMultiplier(1.0/1000.0) }),
			Conversion("Kilogram-Force Metres", "Gram-Force Metres", { this.convertMultiplier(1000.0) }),
			Conversion("Gram-Force Metres", "Kilogram-Force Metres", { this.convertMultiplier(1.0/1000.0) }),
			Conversion("Gram-Force Metres", "Gram-Force Millimetres", { this.convertMultiplier(1000.0) }),
			Conversion("Gram-Force Millimetres", "Gram-Force Metres", { this.convertMultiplier(1.0/1000.0) }),
			Conversion("Gram-Force Metres", "Gram-Force Centimetres", { this.convertMultiplier(100.0) }),
			Conversion("Gram-Force Centimetres", "Gram-Force Metres", { this.convertMultiplier(1.0/100.0) }),
			Conversion("Pound-Force Feet", "Pound-Force Inches", { this.convertMultiplier(12.0) }),
			Conversion("Pound-Force Inches", "Pound-Force Feet", { this.convertMultiplier(1.0/12.0) }),
			Conversion("Pound-Force Feet", "Ounce-Force Feet", { this.convertMultiplier(16.0) }),
			Conversion("Ounce-Force Feet", "Pound-Force Feet", { this.convertMultiplier(1.0/16.0) }),
			Conversion("Pound-Force Inches", "Ounce-Force Inches", { this.convertMultiplier(16.0) }),
			Conversion("Ounce-Force Inches", "Pound-Force Inches", { this.convertMultiplier(1.0/16.0) })
		),
		"Speed" to listOf(
			Conversion("Metres Per Second", "Kilometres Per Hour", { this.convertMultiplier(3.6) } ),
			Conversion("Kilometres Per Hour", "Metres Per Second", { this.convertMultiplier(1.0/3.6) } ),
			Conversion("Kilometres Per Hour", "Metres Per Hour", { this.convertMultiplier(1000.0) } ),
			Conversion("Metres Per Hour", "Kilometres Per Hour", { this.convertMultiplier(1.0/1000.0) } ),
			Conversion("Miles Per Hour", "Feet Per Second", { this.convertMultiplier(1760.0 * 3.0 / (3600.0)) } ),
			Conversion("Feet Per Second", "Miles Per Hour", { this.convertMultiplier(1.0/(1760.0 * 3.0 / (3600.0))) } ),
			Conversion("Miles Per Hour", "Kilometres Per Hour", { this.convert("Distance", "Miles", "Kilometres") } ),
			Conversion("Kilometres Per Hour", "Miles Per Hour", { this.convert("Distance", "Kilometres", "Miles") } ),
			Conversion("Knots", "Metres Per Hour", { this.convertMultiplier(1852.0) } ),
			Conversion("Metres Per Hour", "Knots", { this.convertMultiplier(1.0/1852.0) } )
		),
		"Time" to listOf(
			Conversion("Seconds", "Nanoseconds", { this.convertMultiplier(1e9) } ),
			Conversion("Nanoseconds", "Seconds", { this.convertMultiplier(1.0/(1e9)) } ),
			Conversion("Seconds", "Microseconds", { this.convertMultiplier(1e6) } ),
			Conversion("Microseconds", "Seconds", { this.convertMultiplier(1.0/(1e6)) } ),
			Conversion("Seconds", "Milliseconds", { this.convertMultiplier(1e3) } ),
			Conversion("Milliseconds", "Seconds", { this.convertMultiplier(1.0/(1e3)) } ),
			Conversion("Minutes", "Seconds", { this.convertMultiplier(60.0) } ),
			Conversion("Seconds", "Minutes", { this.convertMultiplier(1.0/60.0) } ),
			Conversion("Hours", "Minutes", { this.convertMultiplier(60.0) } ),
			Conversion("Minutes", "Hours", { this.convertMultiplier(1.0/60.0) } ),
			Conversion("Days", "Hours", { this.convertMultiplier(24.0) } ),
			Conversion("Hours", "Days", { this.convertMultiplier(1.0/24.0) } ),
			Conversion("Weeks", "Days", { this.convertMultiplier(7.0) } ),
			Conversion("Days", "Weeks", { this.convertMultiplier(1.0/7.0) } ),
			Conversion("Hours", "Hours.Minutes-Seconds", { this.convertHoursToHms() } ),
			Conversion("Hours.Minutes-Seconds", "Hours", { this.convertHmsToHours() } )
		),
		"Date" to listOf(
			Conversion("Day of Year", "Date in Year", { this.convertDayOfYearToDateInCurrentYear() } ),
			Conversion("Date in Year", "Day of Year", { this.convertDateInCurrentYearToDayOfYear() } )
			// "Years (Julian)":Days / 365.25
			// "Years (Gregorian)", "a<sub><small>g</small></sub>"
		),
		"Force" to listOf(
			Conversion("Newtons", "Micronewtons", { this.convertMultiplier(1e6) } ),
			Conversion("Micronewtons", "Newtons", { this.convertMultiplier(1.0/1e6) } ),
			Conversion("Newtons", "Millinewtons", { this.convertMultiplier(1e3) } ),
			Conversion("Millinewtons", "Newtons", { this.convertMultiplier(1.0/1e3) } ),
			Conversion("Kilonewtons", "Newtons", { this.convertMultiplier(1e3) } ),
			Conversion("Newtons", "Kilonewtons", { this.convertMultiplier(1.0/1e3) } ),
			Conversion("Kilogram-Force", "Newtons", { this.convertMultiplier(9.80665) } ),
			Conversion("Newtons", "Kilogram-Force", { this.convertMultiplier(1.0/9.80665) } ),
			Conversion("Kilogram-Force", "Gram-Force", { this.convertMultiplier(1000.0) } ),
			Conversion("Gram-Force", "Kilogram-Force", { this.convertMultiplier(1.0/1000.0) } ),
			Conversion("Pound-Force", "Newtons", { this.convertMultiplier(4.4482216152605) } ),
			Conversion("Newtons", "Pound-Force", { this.convertMultiplier(1.0/4.4482216152605) } ),
			Conversion("Pound-Force", "Ounce-Force", { this.convertMultiplier(16.0) } ),
			Conversion("Ounce-Force", "Pound-Force", { this.convertMultiplier(1.0/16.0) } )
		),
		"Pressure" to listOf(
			Conversion("Pascal", "Hectopascal", { this.convertMultiplier(1.0/100.0) } ),
			Conversion("Hectopascal", "Pascal", { this.convertMultiplier(100.0) } ),
			Conversion("Pascal", "Kilopascal", { this.convertMultiplier(1.0/1e3) } ),
			Conversion("Kilopascal", "Pascal", { this.convertMultiplier(1e3) } ),
			Conversion("Pascal", "Megapascal", { this.convertMultiplier(1.0/1e6) } ),
			Conversion("Megapascal", "Pascal", { this.convertMultiplier(1e6) } ),
			Conversion("Millibar", "Pascal", { this.convertMultiplier(100.0) } ),
			Conversion("Pascal", "Millibar", { this.convertMultiplier(1.0/100.0) } ),
			Conversion("Millibar", "Bar", { this.convertMultiplier(1.0/1000.0) } ),
			Conversion("Bar", "Millibar", { this.convertMultiplier(1000.0) } ),
			Conversion("Pascal", "Atmosphere", { this.convertMultiplier(1.0/101325.0) } ),
			Conversion("Atmosphere", "Pascal", { this.convertMultiplier(101325.0) } ),
			Conversion("Kilopascal", "Kilograms Per Sq. cm", { this.convertMultiplier(1.0/98.0665) } ),
			Conversion("Kilograms Per Sq. cm", "Kilopascal", { this.convertMultiplier(98.0665) } ),
			Conversion("Pascal", "Pounds Per Sq. Inch", { this.convertMultiplier(1.0/6894.780176784) } ),
			Conversion("Pounds Per Sq. Inch", "Pascal", { this.convertMultiplier(6894.780176784) } ),
			Conversion("Pascal", "Inches of Mercury", { this.convertMultiplier(1.0/3386.389) } ),
			Conversion("Inches of Mercury", "Pascal", { this.convertMultiplier(3386.389) } ),
			Conversion("Torr", "Atmosphere", { this.convertMultiplier(1.0/760.0) } ),
			Conversion("Atmosphere", "Torr", { this.convertMultiplier(760.0) } )
		),
		// TODO Review got here
		"Energy" to listOf(
			Conversion("Kilojoules", "Joules", { this.convertMultiplier(1000.0) } ),
			Conversion("Joules", "Kilojoules", { this.convertMultiplier(1.0/1000.0) } ),
			Conversion("Megajoules", "Kilojoules", { this.convertMultiplier(1000.0) } ),
			Conversion("Kilojoules", "Megajoules", { this.convertMultiplier(1.0/1000.0) } ),
			Conversion("Joules", "Kilowatt-Hours", { this.convertMultiplier(1.0/3.6e6) } ),
			Conversion("Kilowatt-Hours", "Joules", { this.convertMultiplier(3.6e6) } ),
			Conversion("Joules", "Kilocalories", { this.convertMultiplier(1.0/4184.0) } ),
			Conversion("Kilocalories", "Joules", { this.convertMultiplier(4184.0) } ),
			Conversion("Kilocalories", "Calories", { this.convertMultiplier(1000.0) } ),
			Conversion("Calories", "Kilocalories", { this.convertMultiplier(1.0/1000.0) } )
			// "British Thermal Units", "BTU"
		),
		/* Temperature conversions */
		"Temperature" to listOf(
			Conversion("Kelvin", "Celsius", { this.convertKelvinToCelsius() }),
			Conversion("Celsius", "Kelvin", { this.convertCelsiusToKelvin() }),
			Conversion("Celsius", "Fahrenheit", { this.convertCelsiusToFahrenheit() }),
			Conversion("Fahrenheit", "Celsius", { this.convertFahrenheitToCelsius() })
		),
		"Area" to listOf(
			Conversion("Sq. Millimetres", "Sq. Metres", { this.convertMultiplier(1.0/1e6) } ),
			Conversion("Sq. Metres", "Sq. Millimetres", { this.convertMultiplier(1e6) } ),
			Conversion("Sq. Centimetres", "Sq. Metres", { this.convertMultiplier(1.0/10000.0) } ),
			Conversion("Sq. Metres", "Sq. Centimetres", { this.convertMultiplier(10000.0) } ),
			Conversion("Sq. Metres", "Sq. Kilometres", { this.convertMultiplier(1.0/1e6) } ),
			Conversion("Sq. Kilometres", "Sq. Metres", { this.convertMultiplier(1e6) } ),
			Conversion("Sq. Metres", "Hectares", { this.convertMultiplier(1.0/10000.0) } ),
			Conversion("Hectares", "Sq. Metres", { this.convertMultiplier(10000.0) } ),
			Conversion("Sq. Millimetres", "Sq. Inches", { this.convertMultiplier(1.0/(25.4*25.4)) } ),
			Conversion("Sq. Inches", "Sq. Millimetres", { this.convertMultiplier(25.4*25.4) } ),
			Conversion("Sq. Inches", "Sq. Feet", { this.convertMultiplier(1.0/(12.0*12.0)) } ),
			Conversion("Sq. Feet", "Sq. Inches", { this.convertMultiplier(12.0*12.0) } ),
			Conversion("Sq. Feet", "Sq. Yards", { this.convertMultiplier(1.0/9.0) } ),
			Conversion("Sq. Yards", "Sq. Feet", { this.convertMultiplier(9.0) } ),
			Conversion("Sq. Yards", "Acres", { this.convertMultiplier(1.0/4840.0) } ),
			Conversion("Acres", "Sq. Yards", { this.convertMultiplier(4840.0) } ),
			Conversion("Sq. Yards", "Sq. Miles", { this.convertMultiplier(1.0/(1760.0*1760.0)) } ),
			Conversion("Sq. Miles", "Sq. Yards", { this.convertMultiplier(1760.0*1760.0) } )
		),
		"Data Size" to listOf(
			Conversion("Kibibytes", "Bytes", { this.convertMultiplier(1024.0) } ),
			Conversion("Bytes", "Kibibytes", { this.convertMultiplier(1.0/1024.0) } ),
			Conversion("Mebibytes", "Kibibytes", { this.convertMultiplier(1024.0) } ),
			Conversion("Kibibytes", "Mebibytes", { this.convertMultiplier(1.0/1024.0) } ),
			Conversion("Gibibytes", "Mebibytes", { this.convertMultiplier(1024.0) } ),
			Conversion("Mebibytes", "Gibibytes", { this.convertMultiplier(1.0/1024.0) } ),
			Conversion("Tebibytes", "Gibibytes", { this.convertMultiplier(1024.0) } ),
			Conversion("Gibibytes", "Tebibytes", { this.convertMultiplier(1.0/1024.0) } ),
			Conversion("Kilobytes", "Bytes", { this.convertMultiplier(1000.0) } ),
			Conversion("Bytes", "Kilobytes", { this.convertMultiplier(1.0/1000.0) } ),
			Conversion("Megabytes", "Kilobytes", { this.convertMultiplier(1000.0) } ),
			Conversion("Kilobytes", "Megabytes", { this.convertMultiplier(1.0/1000.0) } ),
			Conversion("Gigabytes", "Megabytes", { this.convertMultiplier(1000.0) } ),
			Conversion("Megabytes", "Gigabytes", { this.convertMultiplier(1.0/1000.0) } ),
			Conversion("Terabytes", "Gigabytes", { this.convertMultiplier(1000.0) } ),
			Conversion("Gigabytes", "Terabytes", { this.convertMultiplier(1.0/1000.0) } )
		),
		/* Distance conversions */
		"Distance" to listOf(
			Conversion("Inches", "Millimetres", { this.convertInchToMM() }),
			Conversion("Millimetres", "Inches", { this.convertMMToInch() }),
			Conversion("Metres", "Millimetres", { this.convertMultiplier(1000.0) }),
			Conversion("Millimetres", "Metres", { this.convertMultiplier(1.0/1000.0) }),
			Conversion("Millimetres", "Microns", { this.convertMultiplier(1000.0) }),
			Conversion("Microns", "Millimetres", { this.convertMultiplier(1.0/1000.0) }),
			Conversion("Nanometres", "Microns", { this.convertMultiplier(1.0/1000.0) }),
			Conversion("Microns", "Nanometres", { this.convertMultiplier(1000.0) }),
			Conversion("Micrometres", "Microns", { ErrorCode.NoError}),
			Conversion("Microns", "Micrometres", { ErrorCode.NoError}),
			Conversion("Nanometres", "Angstroms", { this.convertMultiplier(10.0) }),
			Conversion("Angstroms", "Nanometres", { this.convertMultiplier(0.1) }),
			Conversion("Metres", "Centimetres", { this.convertMultiplier(100.0) }),
			Conversion("Centimetres", "Metres", { this.convertMultiplier(1.0/100.0) }),
			Conversion("Kilometres", "Metres",  { this.convertMultiplier(1000.0) }),
			Conversion("Metres", "Kilometres",  { this.convertMultiplier(1.0/1000.0) }),
			Conversion("Inches", "Thou",        { this.convertMultiplier(1000.0) }),
			Conversion("Thou", "Inches",        { this.convertMultiplier(1.0/1000.0) }),
			Conversion("Inches", "Points",        { this.convertMultiplier(72.0) }),
			Conversion("Points", "Inches",        { this.convertMultiplier(1.0/72.0) }),
			Conversion("Inches", "Feet",        { this.convertMultiplier(1.0/12.0) }),
			Conversion("Feet", "Inches",        { this.convertMultiplier(12.0) }),
			Conversion("Yards", "Feet",         { this.convertMultiplier(3.0) }),
			Conversion("Feet", "Yards",         { this.convertMultiplier(1.0/3.0) }),
			Conversion("Yards", "Miles",        { this.convertMultiplier(1.0/1760.0) }),
			Conversion("Miles", "Yards",        { this.convertMultiplier(1760.0) }),
			Conversion("Yards", "Furlongs",     { this.convertMultiplier(1.0/220.0) }),
			Conversion("Furlongs", "Yards",     { this.convertMultiplier(220.0) }),
			Conversion("Metres", "Microns", { this.convertMultiplier(1e6) } ),
			Conversion("Microns", "Metres", { this.convertMultiplier(1.0/1e6) } ),
			Conversion("Mils", "Thou", { ErrorCode.NoError }),
			Conversion("Thou", "Mils", { ErrorCode.NoError }),
			Conversion("Nautical Miles", "Metres", { this.convertMultiplier(1852.0) } ),
			Conversion("Metres", "Nautical Miles", { this.convertMultiplier(1.0/1852.0) } ),
			Conversion("Fathoms", "Feet", { this.convertMultiplier(6.0) } ),
			Conversion("Feet", "Fathoms", { this.convertMultiplier(1.0/6.0) } ),
			Conversion("Chains", "Yards", { this.convertMultiplier(22.0) } ),
			Conversion("Yards", "Chains", { this.convertMultiplier(1.0/22.0) } ),
			Conversion("Light Years", "Metres", { this.convertMultiplier(9460730472580800.0) } ),
			Conversion("Metres", "Light Years", { this.convertMultiplier(1.0/9460730472580800.0) } )
		),
		/* Angular conversions */
		"Angle" to listOf(
			Conversion("Radians", "Degrees", { this.convertRadiansToDegrees() }),
			Conversion("Degrees", "Radians", { this.convertDegreesToRadians() }),
			Conversion("Degrees", "Degrees.Minutes-Seconds", { this.convertHoursToHms() } ),
			Conversion("Degrees.Minutes-Seconds", "Degrees", { this.convertHmsToHours() } ),
			Conversion("Degrees.Minutes", "Degrees", { this.convertHmToHours() } ),
			Conversion("Degrees", "Degrees.Minutes", { this.convertHoursToHm() } )

		),
		/* Power conversions */
		"Power" to listOf(
			Conversion("Watts", "Kilowatts", { this.convertMultiplier(1.0/1000.0) }),
			Conversion("Kilowatts", "Watts", { this.convertMultiplier(1000.0) }),
			Conversion("Watts", "Horsepower (Mech)", { this.convertMultiplier(1.0/745.69987158227022) }),
			Conversion("Horsepower (Mech)", "Watts", { this.convertMultiplier(745.69987158227022) }),
			Conversion("Horsepower (Metric)", "Watts", { this.convertMultiplier(735.49875) } ),
			Conversion("Watts", "Horsepower (Metric)", { this.convertMultiplier(1.0/735.49875) } ),
			Conversion("Megawatts", "Watts", { this.convertMultiplier(1e6) } ),
			Conversion("Watts", "Megawatts", { this.convertMultiplier(1.0/1e6) } ),
			Conversion("Calories Per Second", "Watts", { this.convertMultiplier(4.184) } ),
			Conversion("Watts", "Calories Per Second", { this.convertMultiplier(1.0/4.184) } )
		   // "BTUs Per Hour", "BTU/h"
		),
		/* Frequency conversions */
		"Frequency" to listOf(
			Conversion("RPM", "Hertz", { this.convertRPMToHertz() }),
			Conversion("Hertz", "RPM", { this.convertHertzToRPM() }),
			Conversion("Radians Per Second", "Hertz", { this.convertRadPerSecToHertz() }),
			Conversion("Hertz", "Radians Per Second", { this.convertHertzToRadPerSec() })
		),
		/* Fuel economy conversions */
		"Fuel Economy" to listOf(
			Conversion("Miles Per Gallon", "Miles Per Litre", { this.convert("Volume", "Litres", "Gallons") }),
			Conversion("Miles Per Litre", "Miles Per Gallon", { this.convert("Volume", "Gallons", "Litres") }),
			Conversion("Miles Per Gallon", "Miles Per US Gallon", { this.convert("Volume", "US Gallons", "Gallons") }),
			Conversion("Miles Per US Gallon", "Miles Per Gallon", { this.convert("Volume", "Gallons", "US Gallons") }),
			Conversion("Kilometres Per Litre", "Miles Per Litre", { this.convert("Distance", "Kilometres", "Miles") }),
			Conversion("Miles Per Litre", "Kilometres Per Litre", { this.convert("Distance", "Miles", "Kilometres") }),
			Conversion("Kilometres Per Litre", "Litres Per 100 Kilometres", { this.convertKilometresPerLitreToLitresPer100KM() }),
			Conversion("Litres Per 100 Kilometres", "Kilometres Per Litre", { this.convertLitresPer100KMToKilometresPerLitre() })
		)
	)
}

fun Stack.registerCurrencies(wrtEuro: Map<String, Double>) {
	var conversions: MutableList<Conversion> = mutableListOf()
	rawCurrencyData = wrtEuro.toMap() // Copy
	for ((name, euros) in wrtEuro.entries) {
		var currencyName = currencyMap[name]
		if (currencyName != null) {
			var convFrom = Conversion("Euros", currencyName, { this.convertMultiplier(euros) })
			var convTo = Conversion(currencyName, "Euros", { this.convertMultiplier(1.0/euros) })
			conversions.add(convFrom)
			conversions.add(convTo)
		}
		else {
			println("Unknown currency: ${name}")
		}
	}
	if (conversions.size > 0) {
		this.conversionTable.put("Currency", conversions.toList())
	}
}

fun Stack.seenConv(path: List<Conversion>, to: String): Boolean {
	for (p in path) {
		if ((p.to == to) || (p.from == to)) {
			return true
		}
	}
	return false
}

fun Stack.getConvPathsFrom(type: String, from: String, pathSoFar: List<Conversion>): List<Conversion> {
	/* Assume type and from are valid */
	var conversionList = this.conversionTable[type]!!

	var result: MutableList<Conversion> = mutableListOf()

	for (conversion in conversionList) {
		if ((conversion.from == from) && ( ! seenConv(pathSoFar, conversion.to))) {
			result.add(conversion)
		}
	}
	return result.toList()
}

fun Stack.getConvPathsFromTo(type: String, from: String, to: String, pathSoFar: List<Conversion> = listOf()): List<Conversion> {
	var result: MutableList<Conversion> = mutableListOf()

	var pathList = getConvPathsFrom(type, from, pathSoFar)
	for (p in pathList) {
		if (p.to == to) {
			return listOf(p)
		}
	}

	/* Didn't find, so keep going */

	for (p in pathList) {
		var psF: MutableList<Conversion> = pathSoFar.toMutableList()
		psF.add(p)
		var r = getConvPathsFromTo(type, p.to, to, psF)
		if (r.size > 0) {
			/* Must have found it */
			result.add(p)
			result.addAll(r)
			return result
		}
	}

	return result.toList()
}

fun Stack.convert(type: String, from: String, to: String): ErrorCode {
	/* This is fairly inefficient code to find a path through the conversion table
	 * and work out how to convert from one unit to another.
	 *
	 * It assumes the table contains a path for any conversion within that table
	 */
	var foundFrom: Boolean = false
	var foundTo: Boolean = false

	if (from == to) {
		return ErrorCode.NoError
	}
	var conversionList = this.conversionTable[type]
	if (conversionList == null) {
		return ErrorCode.UnknownConversion
	}

	for (conversion in conversionList) {
		if (conversion.from == from) {
			foundFrom = true
		}
		if (conversion.to == to) {
			foundTo = true
		}
		if (foundFrom && foundTo) {
			break
		}
	}
	if ( ( ! foundFrom) || ( ! foundTo) ) {
		return ErrorCode.UnknownConversion
	}

	var conversionPath = getConvPathsFromTo(type, from, to)
	if (conversionPath.size == 0) {
		return ErrorCode.UnknownConversion
	}


	var stackCopy = this.stack.toList()
	for (conv in conversionPath) {
		var result = conv.func()
		if (result != ErrorCode.NoError) {
			this.stack = stackCopy.toMutableList()
			return result
		}
	}
	return ErrorCode.NoError
}

fun Stack.getAvailableConversions(from: String) : Set<String> {
	var build_list: MutableList<String>

	var this_table = false
	for (conversionList in this.conversionTable.values) {
		build_list = mutableListOf()
		for (conversion in conversionList) {
			var convFrom = conversion.from
			build_list.add(convFrom)
			if (convFrom == from) {
				this_table = true
			}
		}
		if (this_table) {
			return build_list.toSet()
		}
	}
	return setOf()
}

fun Stack.getConversionCategories(): List<String> {
	return this.conversionTable.keys.sorted()
}

fun Stack.getAvailableUnits(category: String): Set<String> {
	var build_list: MutableList<String> = mutableListOf()
	if (this.conversionTable.containsKey(category)) {
		for (conversion in this.conversionTable[category]!!) {
			build_list.add(conversion.from)
		}
	}
	return build_list.toSet()
}
