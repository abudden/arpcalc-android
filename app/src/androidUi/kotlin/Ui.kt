package uk.co.cgtk.karpcalc

import android.text.method.ScrollingMovementMethod
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivityUi {
	private fun mainStyle(v: View) {
		when (v) {
			is Button -> {
				v.layoutParams = LinearLayout.LayoutParams(0, LayoutParams.MATCH_PARENT, 0.16f)
				v.textSize = 12f
				v.setPadding(0, 0, 0, 0)
				v.isAllCaps = false
			}
			is TextView -> {
				if (v.text.isEmpty()) {
					v.setHorizontallyScrolling(true)
					v.isHorizontalScrollBarEnabled = true
					v.isScrollbarFadingEnabled = true
					v.movementMethod = ScrollingMovementMethod()
				}
			}
			is ScrollView -> {
				v.isFillViewport = true
			}
		}
	}

	private fun tabStyle(v: View) {
		if (v is ImageView) {
			val params = LinearLayout.LayoutParams(0, LayoutParams.MATCH_PARENT, 20f)
			params.gravity = Gravity.CENTER
			v.layoutParams = params
		}
	}

	private fun View.applyRecursively(style: (View) -> Unit) {
		style(this)
		if (this is ViewGroup) {
			for (i in 0 until childCount) {
				getChildAt(i).applyRecursively(style)
			}
		}
	}

	/* Views accessed by MainActivity to change button text, write to status
	 * TextViews etc.
	 */
	lateinit var b00: Button; lateinit var b01: Button; lateinit var b02: Button; lateinit var b03: Button; lateinit var b04: Button; lateinit var b05: Button;
	lateinit var b10: Button; lateinit var b11: Button; lateinit var b12: Button; lateinit var b13: Button; lateinit var b14: Button; lateinit var b15: Button;
	lateinit var b20: Button; lateinit var b21: Button; lateinit var b22: Button; lateinit var b23: Button; lateinit var b24: Button; lateinit var b25: Button;
	lateinit var b30: Button; lateinit var b31: Button; lateinit var b32: Button; lateinit var b33: Button; lateinit var b34: Button; lateinit var b35: Button;
	lateinit var b40: Button; lateinit var b41: Button; lateinit var b42: Button; lateinit var b43: Button; lateinit var b44: Button; lateinit var b45: Button;
	lateinit var b50: Button; lateinit var b51: Button; lateinit var b52: Button; lateinit var b53: Button; lateinit var b54: Button; lateinit var b55: Button;

	lateinit var txtEntry: TextView
	lateinit var txtBase: TextView
	lateinit var txtStack: TextView
	lateinit var stackScroll: ScrollView
	lateinit var txtStatusExponent: TextView
	lateinit var txtStatusBase: TextView
	lateinit var txtStatusAngular: TextView
	lateinit var lytButtons: LinearLayout
	lateinit var lytOptionsPage1: LinearLayout
	lateinit var lytOptionsPage2: LinearLayout

	lateinit var imNumPad: ImageView
	lateinit var imFuncPad: ImageView
	lateinit var imConvPad: ImageView
	lateinit var imConstPad: ImageView
	lateinit var imOptPad: ImageView

	private val MATCH = LayoutParams.MATCH_PARENT

	/* Layout params helpers: a weighted share of the parent's width or height */
	private fun widthWeight(weight: Float) = LinearLayout.LayoutParams(0, MATCH, weight)
	private fun heightWeight(weight: Float) = LinearLayout.LayoutParams(MATCH, 0, weight)

	fun setContentView(owner: MainActivity) {
		val ctx = owner

		fun linear(o: Int) = LinearLayout(ctx).apply { orientation = o }
		fun statusText(t: String) = TextView(ctx).apply {
			gravity = Gravity.CENTER
			text = t
		}
		fun tabImage(res: Int, tab: String) = ImageView(ctx).apply {
			setImageResource(res)
			setOnClickListener { owner.tabSelect(tab) }
		}
		fun buttonRow(): Pair<LinearLayout, List<Button>> {
			val row = linear(LinearLayout.HORIZONTAL)
			val rowButtons = List(6) { Button(ctx).apply { text = "" } }
			rowButtons.forEach { row.addView(it) }
			return Pair(row, rowButtons)
		}

		val root = linear(LinearLayout.VERTICAL)

		/* Status bar */
		val status = linear(LinearLayout.HORIZONTAL)
		txtStatusExponent = statusText("EXP:10")
		txtStatusAngular = statusText("DEG")
		txtStatusBase = statusText("DEC")
		status.addView(txtStatusExponent, widthWeight(20f))
		status.addView(View(ctx), widthWeight(20f))
		status.addView(txtStatusAngular, widthWeight(20f))
		status.addView(View(ctx), widthWeight(20f))
		status.addView(txtStatusBase, widthWeight(20f))
		root.addView(status, heightWeight(5f))

		/* Stack and base displays */
		val displays = linear(LinearLayout.HORIZONTAL)
		val stackColumn = linear(LinearLayout.VERTICAL)
		stackScroll = ScrollView(ctx)
		txtStack = TextView(ctx).apply { gravity = Gravity.BOTTOM or Gravity.LEFT }
		stackScroll.addView(txtStack, LayoutParams(MATCH, MATCH))
		stackColumn.addView(stackScroll, LinearLayout.LayoutParams(MATCH, MATCH))
		displays.addView(stackColumn, widthWeight(40f))
		displays.addView(linear(LinearLayout.VERTICAL), widthWeight(5f))
		txtBase = TextView(ctx).apply { gravity = Gravity.TOP or Gravity.LEFT }
		displays.addView(txtBase, widthWeight(55f))
		root.addView(displays, heightWeight(25f))

		/* X entry */
		txtEntry = TextView(ctx).apply {
			textSize = 25f
			gravity = Gravity.BOTTOM or Gravity.RIGHT
			text = ""
			isSingleLine = true
		}
		val entryHeight = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 40f,
				ctx.resources.displayMetrics).toInt()
		root.addView(txtEntry, LinearLayout.LayoutParams(MATCH, entryHeight))

		/* Tab icons */
		val tabs = linear(LinearLayout.HORIZONTAL)
		imNumPad   = tabImage(R.drawable.numpad, "numpad")
		imFuncPad  = tabImage(R.drawable.funcpad, "funcpad")
		imConvPad  = tabImage(R.drawable.convpad, "convpad")
		imConstPad = tabImage(R.drawable.constpad, "constpad")
		imOptPad   = tabImage(R.drawable.optpad, "optpad1")
		for (im in listOf(imNumPad, imFuncPad, imConvPad, imConstPad, imOptPad)) {
			tabs.addView(im)
		}
		tabs.applyRecursively(::tabStyle)
		root.addView(tabs, heightWeight(12f))

		/* Button grid */
		lytButtons = linear(LinearLayout.VERTICAL)
		val rows = List(6) { buttonRow() }
		rows.forEach { (row, _) -> lytButtons.addView(row, heightWeight(0.16f)) }
		rows[0].second.let { b00 = it[0]; b01 = it[1]; b02 = it[2]; b03 = it[3]; b04 = it[4]; b05 = it[5] }
		rows[1].second.let { b10 = it[0]; b11 = it[1]; b12 = it[2]; b13 = it[3]; b14 = it[4]; b15 = it[5] }
		rows[2].second.let { b20 = it[0]; b21 = it[1]; b22 = it[2]; b23 = it[3]; b24 = it[4]; b25 = it[5] }
		rows[3].second.let { b30 = it[0]; b31 = it[1]; b32 = it[2]; b33 = it[3]; b34 = it[4]; b35 = it[5] }
		rows[4].second.let { b40 = it[0]; b41 = it[1]; b42 = it[2]; b43 = it[3]; b44 = it[4]; b45 = it[5] }
		rows[5].second.let { b50 = it[0]; b51 = it[1]; b52 = it[2]; b53 = it[3]; b54 = it[4]; b55 = it[5] }
		root.addView(lytButtons, heightWeight(48f))

		/* Options pages (populated by MainActivity.createOptPad) */
		lytOptionsPage1 = linear(LinearLayout.VERTICAL).apply { visibility = View.GONE }
		root.addView(lytOptionsPage1, heightWeight(48f))
		lytOptionsPage2 = linear(LinearLayout.VERTICAL).apply { visibility = View.GONE }
		root.addView(lytOptionsPage2, heightWeight(48f))

		root.applyRecursively(::mainStyle)

		/* Android 15+ draws apps edge-to-edge: keep the content clear of the
		 * status and navigation bars.
		 */
		ViewCompat.setOnApplyWindowInsetsListener(root) { v, insets ->
			val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
			v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
			insets
		}

		owner.setContentView(root)
	}
}
