package uk.co.cgtk.karpcalc

// java.time doesn't seem to be available in android < 26

import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun CommandHandler.checkCurrencyDate(currency_date: String) : String {
	val MAX_CURRENCY_AGE_DAYS = 5
	/* The ECB's dates have English month names, whatever the phone's language */
	var formatter = SimpleDateFormat("d MMMM yyyy", Locale.ENGLISH)
	var today = Date().getTime()
	var result = ""

	/* parse() throws rather than returning null if it can't parse the date
	 * (e.g. if the download wasn't the ECB's file at all)
	 */
	var dt: Date? = try { formatter.parse(currency_date) } catch (e: ParseException) { null }
	if (dt == null) {
		result = "Cannot parse date: ${currency_date}"
	}
	else {
		var elapsed_ms = today - dt.getTime()
		var elapsed = elapsed_ms / (86400*1000.0)

		if (elapsed > MAX_CURRENCY_AGE_DAYS) {
			result = "Currency data is out of date (last downloaded ${currency_date})"
			//result += " - calculated as ${elapsed_ms} milliseconds ago based on ${today} and ${dt.getTime()} - parsed as ${dt.toString()}"
		}
	}
	return result
}

