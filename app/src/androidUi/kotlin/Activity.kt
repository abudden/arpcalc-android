package uk.co.cgtk.karpcalc

import android.os.Build
import android.os.Bundle
import android.content.ClipboardManager
import android.content.ClipData
import android.content.Context
import android.content.SharedPreferences
import android.support.v7.app.AppCompatActivity
import android.graphics.drawable.Drawable
import android.text.Html
import android.text.Spanned
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import org.jetbrains.anko.*
import android.view.ViewGroup.LayoutParams
import com.github.kittinunf.fuel.*
import com.github.kittinunf.fuel.core.FuelManager

val UndefinedRow = GridLayout.UNDEFINED
val UndefinedColumn = GridLayout.UNDEFINED

var sizeName = "default" // Ignored in Android

class MainActivity : AppCompatActivity() {

	var buttons: List<List<Button>> = listOf()
	var helpStrings: Array<Array<String>> = arrayOf(
		Array(6, {""}),
		Array(6, {""}),
		Array(6, {""}),
		Array(6, {""}),
		Array(6, {""}),
		Array(6, {""})
	)
	lateinit var mainUI : MainActivityUi
	var calc = CommandHandler()

	var TAG: String = "KARPCALC-UI"

	var returnToNumPad: Boolean = true
	var altFunctionsMode: Boolean = false
	var lastTab: String = "numpad"
	var storeMode: String = "None"
	var lastStoreTab: String = "romanupperpad"
	var optIcons: MutableMap<String, ImageView> = mutableMapOf()

	@Suppress("DEPRECATION")
	fun fromHtml(h: String): Spanned {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
			return Html.fromHtml(h, Html.FROM_HTML_MODE_LEGACY);
		} else {
			return Html.fromHtml(h);
		}
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		mainUI = MainActivityUi()
		mainUI.setContentView(this)

		buttons = listOf(
			listOf(mainUI.b00, mainUI.b01, mainUI.b02, mainUI.b03, mainUI.b04, mainUI.b05),
			listOf(mainUI.b10, mainUI.b11, mainUI.b12, mainUI.b13, mainUI.b14, mainUI.b15),
			listOf(mainUI.b20, mainUI.b21, mainUI.b22, mainUI.b23, mainUI.b24, mainUI.b25),
			listOf(mainUI.b30, mainUI.b31, mainUI.b32, mainUI.b33, mainUI.b34, mainUI.b35),
			listOf(mainUI.b40, mainUI.b41, mainUI.b42, mainUI.b43, mainUI.b44, mainUI.b45),
			listOf(mainUI.b50, mainUI.b51, mainUI.b52, mainUI.b53, mainUI.b54, mainUI.b55)
		)

		for ((rowIndex, row) in buttons.withIndex()) {
			for ((colIndex, button) in row.withIndex()) {
				button.setOnLongClickListener( { longClick(rowIndex, colIndex) } )
				button.setOnClickListener( { buttonPress(rowIndex, colIndex) } )
			}
		}
		mainUI.txtEntry.setOnLongClickListener( { copyXValue() } )
		mainUI.txtStatusBase.setOnLongClickListener( { showRevisionInfo() } )
		mainUI.txtStatusExponent.setOnLongClickListener( { showBuildDate() } )

		restoreSettings()
		restoreStack()

		createOptPad()

		cancelPressed()
		updateDisplays()

		if (calc.getOption(DispOpt.ShowHelpOnStart)) {
			showToast("Long-press any key for help")
		}
		getCurrencyData()
	}

	fun getCurrencyData() {
		FuelManager.instance.basePath = "http://www.ecb.int"

		// responseString instead of response for String? data
		// first two parameters are request and response, but
		// renamed as _ as not used
		"/stats/eurofxref/eurofxref.zip".httpGet().response { _, _, result ->
			val (data, error) = result
			if ((error == null) && (data != null)) {
				var processing_error = calc.processCurrencyData(data);
				if (processing_error.length > 0) {
					showToast(processing_error)
				}
			} else {
				//error handling
			}
		}
	}

	fun showRevisionInfo(): Boolean {
		showToast("ARPCalc Revision: ${RevisionInfo.changeset}")
		return true
	}

	fun showBuildDate(): Boolean {
		showToast("Built: ${RevisionInfo.build_datetime}")
		return true
	}

	override fun onBackPressed() {
		if (lastTab == "numpad") {
			saveSettings()
			saveStack()
			java.lang.System.exit(0)
		}
		else {
			cancelPressed()
		}
	}

	override fun onPause() {
		super.onPause()
		saveSettings()
		saveStack()
	}

	fun cancelPressed() {
		storeMode = "None" // Not really necessary as done on every button press
		tabSelect("numpad")
	}
	fun storePressed() {
		storeMode = "store"
		tabSelect(lastStoreTab)
		showToast("Please select location")
	}

	fun recallPressed() {
		storeMode = "recall"
		tabSelect(lastStoreTab)
		showToast("Please select location")
	}

	fun restoreSettings() {
		var prefs = getPreferences(0)
		calc.setOption(DispOpt.EngNotation, prefs.getBoolean("EngNotation", true))
		calc.setOption(DispOpt.BinaryPrefixes, prefs.getBoolean("BinaryPrefixes", false))
		calc.setOption(DispOpt.PowerExponentView, prefs.getBoolean("PowerExponentView", true))
		calc.setOption(DispOpt.SINotation, prefs.getBoolean("SINotation", false))
		calc.setOption(DispOpt.TrimZeroes, prefs.getBoolean("TrimZeroes", true))
		calc.setOption(DispOpt.AlwaysShowDecimal, prefs.getBoolean("AlwaysShowDecimal", true))
		calc.setOption(DispOpt.ThousandsSeparator, prefs.getBoolean("ThousandsSeparator", true))
		calc.setOption(DispOpt.SpaceAsThousandsSeparator, prefs.getBoolean("SpaceAsThousandsSeparator", false))
		calc.setOption(DispOpt.SaveStackOnExit, prefs.getBoolean("SaveStackOnExit", false))
		calc.setOption(DispOpt.SaveBaseOnExit, prefs.getBoolean("SaveBaseOnExit", false))
		calc.setOption(DispOpt.DecimalBaseBias, prefs.getBoolean("DecimalBaseBias", true))
		calc.setOption(DispOpt.AmericanUnits, prefs.getBoolean("AmericanUnits", false))
		calc.setOption(DispOpt.EuropeanDecimal, prefs.getBoolean("EuropeanDecimal", false))
		calc.setOption(DispOpt.ShowHelpOnStart, prefs.getBoolean("ShowHelpOnStart", true))

		calc.setPlaces(prefs.getInt("DecimalPlaces", 7))

		calc.setOption(CalcOpt.ReplicateStack, prefs.getBoolean("ReplicateStack", false))
		calc.setOption(CalcOpt.Radians, prefs.getBoolean("Radians", false))
		calc.setOption(CalcOpt.SaveHistory, prefs.getBoolean("SaveHistory", true))
		calc.setOption(CalcOpt.PercentLeavesY, prefs.getBoolean("PercentLeavesY", true))

		if (calc.getOption(DispOpt.SaveBaseOnExit)) {
			calc.setStatusBase(prefs.getString("SavedBase", "DEC"))
		}
		else {
			calc.setStatusBase("DEC")
		}

		var vs = prefs.getAll()
		var vMap: MutableMap<String, AF> = mutableMapOf()
		var cMap: MutableMap<String, Double> = mutableMapOf()
		for (key in vs.keys) {
			if (key.startsWith("VARSTORE-")) {
				var value = prefs.getString(key, "0.0")
				vMap[key.drop(9)] = A(value)
			}
			else if (key.startsWith("CURRENCY-")) {
				var lv = prefs.getLong(key, 0)
				var value = java.lang.Double.longBitsToDouble(lv)
				cMap[key.drop(9)] = value
			}
			else if (key == "CURRENCYDATE") {
				calc.last_currency_date = prefs.getString(key, "")
			}
		}
		calc.loadVarStore(vMap.toMap())
		calc.st.registerCurrencies(cMap.toMap())

		var bc = prefs.getInt("BitCount", 32)
		when (bc) {
			8    -> calc.setBitCount(BitCount.bc8)
			16   -> calc.setBitCount(BitCount.bc16)
			32   -> calc.setBitCount(BitCount.bc32)
			64   -> calc.setBitCount(BitCount.bc64)
			else -> calc.setBitCount(BitCount.bc32)
		}
	}

	fun saveSettings() {
		var prefs = getPreferences(0)
		var edit = prefs.edit()
		edit.putBoolean("EngNotation", calc.getOption(DispOpt.EngNotation))
		edit.putBoolean("BinaryPrefixes", calc.getOption(DispOpt.BinaryPrefixes))
		edit.putBoolean("PowerExponentView", calc.getOption(DispOpt.PowerExponentView))
		edit.putBoolean("SINotation", calc.getOption(DispOpt.SINotation))
		edit.putBoolean("TrimZeroes", calc.getOption(DispOpt.TrimZeroes))
		edit.putBoolean("AlwaysShowDecimal", calc.getOption(DispOpt.AlwaysShowDecimal))
		edit.putBoolean("ThousandsSeparator", calc.getOption(DispOpt.ThousandsSeparator))
		edit.putBoolean("SpaceAsThousandsSeparator", calc.getOption(DispOpt.SpaceAsThousandsSeparator))
		edit.putBoolean("SaveStackOnExit", calc.getOption(DispOpt.SaveStackOnExit))
		edit.putBoolean("SaveBaseOnExit", calc.getOption(DispOpt.SaveBaseOnExit))
		edit.putBoolean("DecimalBaseBias", calc.getOption(DispOpt.DecimalBaseBias))
		edit.putBoolean("AmericanUnits", calc.getOption(DispOpt.AmericanUnits))
		edit.putBoolean("EuropeanDecimal", calc.getOption(DispOpt.EuropeanDecimal))
		edit.putBoolean("ShowHelpOnStart", calc.getOption(DispOpt.ShowHelpOnStart))

		edit.putInt("DecimalPlaces", calc.getPlaces())

		edit.putBoolean("ReplicateStack", calc.getOption(CalcOpt.ReplicateStack))
		edit.putBoolean("Radians", calc.getOption(CalcOpt.Radians))
		edit.putBoolean("SaveHistory", calc.getOption(CalcOpt.SaveHistory))
		edit.putBoolean("PercentLeavesY", calc.getOption(CalcOpt.PercentLeavesY))

		if (calc.getOption(DispOpt.SaveBaseOnExit)) {
			edit.putString("SavedBase", calc.getStatusBase())
		}
		else {
			edit.putString("SavedBase", "DEC")
		}

		if (calc.varStoreHasChanged()) {
			var vs = calc.varStore
			for ((key, value) in vs.entries) {
				var lv = value.toString()
				edit.putString("VARSTORE-"+key, lv)
			}
		}

		for ((key, value) in calc.getRawCurrencyData().entries) {
			var lv: Long = java.lang.Double.doubleToLongBits(value)
			edit.putLong("CURRENCY-"+key, lv)
		}
		edit.putString("CURRENCYDATE", calc.last_currency_date)

		when (calc.getBitCount()) {
			BitCount.bc8  -> edit.putInt("BitCount", 8)
			BitCount.bc16 -> edit.putInt("BitCount", 16)
			BitCount.bc32 -> edit.putInt("BitCount", 32)
			BitCount.bc64 -> edit.putInt("BitCount", 64)
			else -> edit.putInt("BitCount", 32)
		}

		edit.commit()
	}

	fun saveStack() {
		var storeStack: List<AF> = listOf()
		if (calc.getOption(DispOpt.SaveStackOnExit)) {
			storeStack = calc.st.stack.toList()
		}

		var prefs = getPreferences(0)
		var edit = prefs.edit()
		edit.putInt("SavedStackLength", storeStack.size)
		for ((index, value) in storeStack.withIndex()) {
			edit.putString("SavedStack-%03d".format(index), value.toString())
		}
	}

	fun restoreStack() {
		var prefs = getPreferences(0)
		var stackSize = prefs.getInt("SavedStackLength", 0)

		for (index in 0..(stackSize-1)) {
			/* Bypass history stuff */
			var lv = prefs.getString("SavedStack-%03d".format(index), "0.0")
			var v: AF = A(lv)
			calc.st.push(v)
		}
	}

	fun copyXValue(): Boolean {
		var clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
		var clip = ClipData.newPlainText("Value from ARPCalc", calc.getParseableX())
		clipboard.setPrimaryClip(clip)
		showToast("Value copied to clipboard")
		return true
	}

	fun longClick(row: Int, col: Int): Boolean {
		var help = helpStrings[row][col].replace("\t+".toRegex(), " ")
		if (help.length == 0) {
			return true
		}
		showToast(help, isHelp=true)
		return true
	}

	fun showToast(html: String, isHelp: Boolean = false) {
		var len = Toast.LENGTH_SHORT
		if (isHelp) {
			/* TODO: consider adjusting based on length of text (although HTML makes this hard) */
			len = Toast.LENGTH_LONG
		}
		var t = Toast.makeText(this, fromHtml(html), len)
		t.setGravity(Gravity.TOP or Gravity.CENTER_HORIZONTAL, 0, 0)
		t.show()
	}

	fun selectToUnit(category: String, from: String) {
		var unitList = calc.st.getAvailableUnits(category).toMutableList()
		unitList.sort()
		unitList.add("Cancel")
		selector("Convert to?", unitList, {
			_, i ->
				if (unitList[i] != "Cancel") {
					var to = unitList[i]
					var command = "Convert_" + category + "_" + from + "_" + to
					buttonPress(command)
				}
			}
		)
	}

	fun selectFromUnit(category: String) {
		var unitList = calc.st.getAvailableUnits(category).toMutableList()
		unitList.sort()
		unitList.add("Cancel")
		selector("Convert from?", unitList, {
			_, i ->
				if (unitList[i] != "Cancel") {
					var from = unitList[i]
					selectToUnit(category, from)
				}
			}
		)
	}

	fun moreConversions() {
		var categories = calc.st.getConversionCategories().toMutableList()
		categories.add("Cancel")
		selector("Category?", categories, {
			_, i ->
				if (categories[i] != "Cancel") {
					selectFromUnit(categories[i])
				}
			}
		)
	}

	fun selectConstant(cat: String) {
		var constantNames: MutableList<String> = mutableListOf()
		for (c in calc.st.constants) {
			if (c.category == cat) {
				constantNames.add(c.name /*+ "(" + c.symbol + ")" */)
			}
		}
		constantNames.add("Cancel")
		selector("Constant?", constantNames, {
			_, i ->
				if (constantNames[i] != "Cancel") {
					var cn = constantNames[i]
					buttonPress("Const-" + cn)
				}
			}
		)
	}

	fun constByName() {
		var categories: MutableList<String> = mutableListOf()
		for (c in calc.st.constants) {
			if ( ! categories.contains(c.category)) {
				categories.add(c.category)
			}
		}
		categories.add("Cancel")
		selector("Category?", categories, {
			_, i ->
				if (categories[i] != "Cancel") {
					selectConstant(categories[i])
				}
			}
		)
	}

	fun selectDensity(cat: String) {
		var densityNames: MutableList<String> = mutableListOf()
		for (c in calc.st.densities) {
			if (c.category == cat) {
				var value = c.value.toString()
				densityNames.add(c.name + " (" + value + "\u00A0kg/m\u00B3)")
			}
		}
		densityNames.add("Cancel")
		selector("Material?", densityNames, {
			_, i ->
				if (densityNames[i] != "Cancel") {
					var cn = densityNames[i].split(" (")[0]
					buttonPress("Density-" + cn)
				}
			}
		)
	}

	fun densityByName() {
		var categories: MutableList<String> = mutableListOf()
		for (c in calc.st.densities) {
			if ( ! categories.contains(c.category)) {
				categories.add(c.category)
			}
		}
		categories.add("Cancel")
		selector("Category?", categories, {
			_, i ->
				if (categories[i] != "Cancel") {
					selectDensity(categories[i])
				}
			}
		)
	}

	fun toggleAltFunctions() {
		altFunctionsMode = ! altFunctionsMode
		tabSelect("funcpad")
	}

	fun buttonPress(row: Int, col: Int): Boolean {
		var command = buttons[row][col].getTag() as String
		buttonPress(command)
		return true
	}
	fun buttonPress(command: String) {
		var handled = true

		when (command) {
			"SI"                         -> tabSelect("sipad")
			"altmode"                    -> toggleAltFunctions()
			"store"                      -> storePressed()
			"recall"                     -> recallPressed()
			"NOP"                        -> {}
			"CANCEL"                     -> cancelPressed()
			"MoreConversions"            -> moreConversions()
			"ConstByName"                -> constByName()
			"DensityByName"              -> densityByName()
			else                         -> handled = false
		}
		if (( ! handled) && command.startsWith("Store")) {
			handled = true
			if (storeMode == "store") {
				calc.store(command)
				saveSettings()
			}
			else if (storeMode == "recall") {
				calc.recall(command)
			}
			else {
				cancelPressed()
			}
			tabSelect("numpad")
		}

		if ((command != "store") && (command != "recall")) {
			storeMode = "None"
		}

		if ( ! handled) {
			altFunctionsMode = false
			var result = calc.keypress(command)
			when (result) {
				ErrorCode.NoFunction         -> showToast("Unknown function: " + command)
				ErrorCode.NotImplemented     -> showToast("Not implemented yet: " + command)
				ErrorCode.DivideByZero       -> showToast("Divide by zero error")
				ErrorCode.InvalidRoot        -> showToast("Invalid root error")
				ErrorCode.InvalidLog         -> showToast("Invalid log error")
				ErrorCode.InvalidTan         -> showToast("Invalid tangent error")
				ErrorCode.InvalidInverseTrig -> showToast("Invalid inverse trigonometry error")
				ErrorCode.InvalidInverseHypTrig -> showToast("Invalid inverse hyperbolic trigonometry error")
				ErrorCode.UnknownConstant    -> showToast("Unknown constant error")
				ErrorCode.UnknownConversion  -> showToast("Unknown conversion error")
				ErrorCode.InvalidConversion  -> showToast("Conversion error")
				ErrorCode.UnknownSI          -> showToast("Unknown SI unit")
				else                         -> {}
			}

			if (returnToNumPad) {
				tabSelect("numpad")
			}
			else {
				tabSelect(lastTab)
			}
		}
		
		updateDisplays()
	}

	fun updateDisplays() {
		var xText = calc.getXDisplay()
		var stackDisplay = calc.getStackDisplay()
		var baseDisplay = calc.getBaseDisplay()
		mainUI.txtEntry.setText(fromHtml(xText), TextView.BufferType.SPANNABLE)
		mainUI.txtStack.setText(fromHtml(stackDisplay), TextView.BufferType.SPANNABLE)
		mainUI.txtBase.setText(fromHtml(baseDisplay), TextView.BufferType.SPANNABLE)

		var stExp = calc.getStatusExponent()
		var stAng = calc.getStatusAngularUnits()
		var stBase = calc.getStatusBase()

		mainUI.txtStatusExponent.setText(stExp)
		mainUI.txtStatusBase.setText(stBase)
		mainUI.txtStatusAngular.setText(stAng)

		mainUI.stackScroll.scrollTo(0, mainUI.stackScroll.getHeight())
		mainUI.stackScroll.post(Runnable {
			mainUI.stackScroll.fullScroll(ScrollView.FOCUS_DOWN);
		})

		//mainUI.txtEntry.scrollBy(Int.MIN_VALUE, 0)
		//mainUI.txtEntry.post(Runnable {
		//	mainUI.txtEntry.scrollBy(Int.MIN_VALUE, 0)
		//})

		mainUI.txtBase.scrollTo(0, 0)
		mainUI.txtBase.post(Runnable {
			mainUI.txtBase.scrollTo(0, 0)
		})

		mainUI.txtStack.scrollTo(0, 0)
		mainUI.txtStack.post(Runnable {
			mainUI.txtStack.scrollTo(0, 0)
		})
	}

	fun tabSelect(tab: String) {
		var local_tab = tab
		val store_tabs = listOf(
			"romanupperpad",
			"romanlowerpad",
			"greekupperpad",
			"greeklowerpad"
		)

		if ((local_tab == lastTab) && (local_tab != "numpad")) {
			returnToNumPad = false
		}
		else {
			returnToNumPad = true
		}

		if (store_tabs.contains(local_tab) || ((storeMode != "None") && (local_tab != "numpad"))) {
			returnToNumPad = true
			mainUI.imFuncPad.setImageDrawable(getIcon(R.drawable.romanupper))
			mainUI.imConvPad.setImageDrawable(getIcon(R.drawable.romanlower))
			mainUI.imConstPad.setImageDrawable(getIcon(R.drawable.greekupper))
			mainUI.imOptPad.setImageDrawable(getIcon(R.drawable.greeklower))
			local_tab = when (local_tab) {
				"funcpad"  -> "romanupperpad"
				"convpad"  -> "romanlowerpad"
				"constpad" -> "greekupperpad"
				"optpad1"  -> "greeklowerpad"
				else       -> local_tab
			}

			lastStoreTab = local_tab
		}
		else {
			storeMode = "None"
			mainUI.imFuncPad.setImageDrawable(getIcon(R.drawable.funcpad))
			mainUI.imConvPad.setImageDrawable(getIcon(R.drawable.convpad))
			mainUI.imConstPad.setImageDrawable(getIcon(R.drawable.constpad))
			mainUI.imOptPad.setImageDrawable(getIcon(R.drawable.optpad))
		}

		if (local_tab == "optpad1") {
			showOptPad1()
			lastTab = local_tab
			return
		}
		if (local_tab == "optpad2") {
			showOptPad2()
			lastTab = local_tab
			return
		}

		if (altFunctionsMode && (local_tab == "funcpad")) {
			local_tab = "altfuncpad"
		}

		var tabGrid = calc.getGrid(local_tab)
		for ((rowIndex, row) in tabGrid.withIndex()) {
			var buttonIncrement = 0
			for ((colIndex, bi) in row.withIndex()) {
				buttons[rowIndex][colIndex+buttonIncrement].setText(fromHtml(bi.display), TextView.BufferType.SPANNABLE)
				buttons[rowIndex][colIndex+buttonIncrement].setTextScaleX(bi.scale)
				buttons[rowIndex][colIndex+buttonIncrement].setTag(bi.name)
				helpStrings[rowIndex][colIndex+buttonIncrement] = bi.helpText
				if (bi.hidden) {
					buttons[rowIndex][colIndex+buttonIncrement].setVisibility(View.INVISIBLE)
				}
				else {
					buttons[rowIndex][colIndex+buttonIncrement].setVisibility(View.VISIBLE)
				}
				if (bi.doubleWidth) {
					var params = LinearLayout.LayoutParams(0, LayoutParams.MATCH_PARENT, 0.32f)
					buttons[rowIndex][colIndex+buttonIncrement].setLayoutParams(params)
					buttonIncrement += 1
					buttons[rowIndex][colIndex+buttonIncrement].setVisibility(View.GONE)
				}
				else {
					var params = LinearLayout.LayoutParams(0, LayoutParams.MATCH_PARENT, 0.16f)
					buttons[rowIndex][colIndex+buttonIncrement].setLayoutParams(params)
				}
			}
		}
		mainUI.lytOptionsPage1.setVisibility(View.GONE)
		mainUI.lytOptionsPage2.setVisibility(View.GONE)
		mainUI.lytButtons.setVisibility(View.VISIBLE)
		lastTab = local_tab
	}

	fun showOptPad1() {
		lastTab = "numpad"
		returnToNumPad = true
		updateOptionIcons()
		mainUI.lytButtons.setVisibility(View.GONE)
		mainUI.lytOptionsPage1.setVisibility(View.VISIBLE)
		mainUI.lytOptionsPage2.setVisibility(View.GONE)
	}

	fun showOptPad2() {
		lastTab = "numpad"
		returnToNumPad = true
		updateOptionIcons()
		mainUI.lytButtons.setVisibility(View.GONE)
		mainUI.lytOptionsPage1.setVisibility(View.GONE)
		mainUI.lytOptionsPage2.setVisibility(View.VISIBLE)
	}

	@Suppress("DEPRECATION")
	fun getIcon(v: Int): Drawable {
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
			return getApplicationContext().getDrawable(v)
		}
		else {
			return getResources().getDrawable(v)
		}
	}

	fun optionClicked(name: String): Boolean {
		var optList = calc.getOptions()
		if ( ! optList.containsKey(name)) {
			return true
		}
		optList[name]!!.clickAction()
		updateOptionIcons()
		updateDisplays()
		return true
	}

	fun optionHelp(name: String): Boolean {
		var optList = calc.getOptions()
		if ( ! optList.containsKey(name)) {
			return true
		}
		showToast(optList[name]!!.helpText.replace("\t+".toRegex(), " "), isHelp=true)
		return true
	}

	fun updateOptionIcons() {
		var optList = calc.getOptions()
		for ((name, opt) in optList.entries) {
			if (opt.hideOnPhone) {
				continue
			}
			var newIcon = opt.iconAction()
			optIcons[name]!!.setImageDrawable(getIcon(newIcon))
		}
		saveSettings()
	}

	fun createOptPad() {
		var optGrids: MutableList<GridLayout> = mutableListOf()
		optGrids.add(GridLayout(this))
		optGrids.add(GridLayout(this))

		for (grid in optGrids) {
			grid.setRowCount(6)
			grid.setColumnCount(6)
			grid.setLayoutParams(LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
		}

		var optList = calc.getOptions()
		for ((name, detail) in optList.entries) {
			if (detail.hideOnPhone) {
				continue
			}
			var txtCol = detail.location.col
			var imCol = detail.location.col
			if (detail.location.col != GridLayout.UNDEFINED) {
				txtCol += 1
			}

			var txtColSpec = GridLayout.spec(txtCol, 2, GridLayout.LEFT, 0.16f)
			var txtRowSpec = GridLayout.spec(detail.location.row, 1, GridLayout.CENTER, 0.16f)
			var imColSpec = GridLayout.spec(imCol, 1, GridLayout.CENTER, 0.16f)
			var imRowSpec = GridLayout.spec(detail.location.row, 1, GridLayout.CENTER, 0.16f)

			var imParams = GridLayout.LayoutParams(imRowSpec, imColSpec)
			var im = ImageView(this)
			im.setLayoutParams(imParams)
			if (name != "BLANK") {
				im.setOnClickListener( { optionClicked(name) } )
				im.setOnLongClickListener( { optionHelp(name) } )
			}
			optIcons[name] = im
			optGrids[detail.location.page].addView(im)

			var txtParams = GridLayout.LayoutParams(txtRowSpec, txtColSpec)
			var txt = TextView(this)
			if (name != "BLANK") {
				txt.setText(name)
				txt.setOnClickListener( { optionClicked(name) } )
				txt.setOnLongClickListener( { optionHelp(name) } )
			}
			txt.setTextSize(11f)
			txt.setLayoutParams(txtParams)
			optGrids[detail.location.page].addView(txt)
		}

		var moreColSpec = GridLayout.spec(4, 2, GridLayout.LEFT, 0.16f)
		var moreRowSpec = GridLayout.spec(5, 1, GridLayout.CENTER, 0.16f)
		var btnToOpt2 = Button(this)
		btnToOpt2.setText("More...")
		btnToOpt2.setTextSize(12f)
		btnToOpt2.setAllCaps(false)
		btnToOpt2.setPadding(0, 0, 0, 0)
		var b2p = GridLayout.LayoutParams(moreRowSpec, moreColSpec)
		b2p.setMargins(0,0,0,0)
		b2p.setGravity(Gravity.FILL)
		btnToOpt2.setLayoutParams(b2p)
		btnToOpt2.setOnClickListener( { tabSelect("optpad2") } )
		optGrids[0].addView(btnToOpt2)

		var btnToOpt1 = Button(this)
		btnToOpt1.setText("Back...")
		btnToOpt1.setTextSize(12f)
		btnToOpt1.setAllCaps(false)
		btnToOpt1.setPadding(0, 0, 0, 0)
		var b1p = GridLayout.LayoutParams(moreRowSpec, moreColSpec)
		b1p.setMargins(0,0,0,0)
		b1p.setGravity(Gravity.FILL)
		btnToOpt1.setLayoutParams(b1p)
		btnToOpt1.setOnClickListener( { tabSelect("optpad1") } )
		optGrids[1].addView(btnToOpt1)

		mainUI.lytOptionsPage1.addView(optGrids[0])
		mainUI.lytOptionsPage2.addView(optGrids[1])

		updateOptionIcons()
	}
}
