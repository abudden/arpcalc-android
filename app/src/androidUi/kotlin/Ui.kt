package uk.co.cgtk.karpcalc

import android.graphics.Color
import android.text.method.ScrollingMovementMethod
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.GridLayout
import android.widget.ScrollView
import android.widget.TextView
import org.jetbrains.anko.*
import android.view.ViewGroup.LayoutParams
import org.jetbrains.anko.sdk25.coroutines.onClick

class MainActivityUi : AnkoComponent<MainActivity> {
	private val mainStyle = { v: Any ->
		when (v) {
			is Button -> {
				var params = LinearLayout.LayoutParams(0, LayoutParams.MATCH_PARENT, 0.16f)
				params.topMargin = 0
				params.bottomMargin = 0
				params.leftMargin = 0
				params.rightMargin = 0
				v.setLayoutParams(params)
				v.textSize = 12f
				v.padding = 0
				v.setAllCaps(false)
			}
			is TextView -> {
				if (v.getText() == "") {
					v.setHorizontallyScrolling(true)
					v.setHorizontalScrollBarEnabled(true)
					v.setScrollbarFadingEnabled(true)
					v.setMovementMethod(ScrollingMovementMethod())
				}
			}
			is ScrollView -> {
				v.setFillViewport(true)
			}
		}
	}
	private val tabStyle = { v: Any ->
		when (v) {
			is ImageView -> {
				var params = LinearLayout.LayoutParams(0, LayoutParams.MATCH_PARENT, 20f)
				params.gravity = Gravity.CENTER
				v.setLayoutParams(params)
			}
		}
	}

	/* Ick */
	/* This is a truly horrific method of making sure the items defined in createView
	 * can be accessed by the implementation class (to try to keep implementation
	 * separate from layout and to enable changes to button text, write to status
	 * TextViews etc.  This amount of boilerplate is awful, but I haven't found a better
	 * way (short of adding an id to every element and going back to findViewById).
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

	override fun createView(ui: AnkoContext<MainActivity>) = with(ui) {
		verticalLayout {
			linearLayout {
				orientation = LinearLayout.HORIZONTAL

				txtStatusExponent = textView {
					gravity = Gravity.CENTER
					text = "EXP:10"
				}.lparams(height = matchParent, width = 0) {
					weight = 20f
				}
				view {
				}.lparams(height = matchParent, width = 0) {
					weight = 20f
				}
				txtStatusAngular = textView {
					gravity = Gravity.CENTER
					text = "DEG"
				}.lparams(height = matchParent, width = 0) {
					weight = 20f
				}
				view {
				}.lparams(height = matchParent, width = 0) {
					weight = 20f
				}
				txtStatusBase = textView {
					gravity = Gravity.CENTER
					text = "DEC"
				}.lparams(height = matchParent, width = 0) {
					weight = 20f
				}
			}.lparams (width = matchParent, height = 0) {
				weight = 5f
			}
			linearLayout {
				orientation = LinearLayout.HORIZONTAL
				verticalLayout {
					stackScroll = scrollView {
						txtStack = textView {
							gravity = Gravity.BOTTOM or Gravity.LEFT
							//lineSpacingMultiplier = 1.2f
							//backgroundColor = Color.WHITE
						}.lparams (width = matchParent, height = matchParent)
					}.lparams (width = matchParent, height = matchParent)
				}.lparams (height = matchParent, width = 0) {
					weight = 40f
				}
				verticalLayout {
				}.lparams (height = matchParent, width = 0) {
					weight = 5f
				}
				txtBase = textView {
					gravity = Gravity.TOP or Gravity.LEFT
					//backgroundColor = Color.GREEN
				}.lparams (height = matchParent, width = 0) {
					weight = 55f
				}
			}.lparams (width = matchParent, height = 0) {
				weight = 25f
			}

			txtEntry = textView {
				textSize = 25f
				gravity = Gravity.BOTTOM or Gravity.RIGHT
				text = ""
				singleLine = true
			}.lparams(height = sp(40f), width = matchParent)

			linearLayout {
				orientation = LinearLayout.HORIZONTAL
				imNumPad   = imageView(R.drawable.numpad)   { onClick { ui.owner.tabSelect("numpad") } }
				imFuncPad  = imageView(R.drawable.funcpad)  { onClick { ui.owner.tabSelect("funcpad") } }
				imConvPad  = imageView(R.drawable.convpad)  { onClick { ui.owner.tabSelect("convpad") } }
				imConstPad = imageView(R.drawable.constpad) { onClick { ui.owner.tabSelect("constpad") } }
				imOptPad   = imageView(R.drawable.optpad)   { onClick { ui.owner.tabSelect("optpad1") } }
			}.lparams(width = matchParent, height = 0) {
				weight = 12f
			}.applyRecursively(tabStyle)
			
			lytButtons = verticalLayout {
				linearLayout {
					orientation = LinearLayout.HORIZONTAL
					b00 = button("")
					b01 = button("")
					b02 = button("")
					b03 = button("")
					b04 = button("")
					b05 = button("")
				}.lparams(width = matchParent, height = 0) { weight = 0.16f }
				linearLayout {
					orientation = LinearLayout.HORIZONTAL
					b10 = button("")
					b11 = button("")
					b12 = button("")
					b13 = button("")
					b14 = button("")
					b15 = button("")
				}.lparams(width = matchParent, height = 0) { weight = 0.16f }
				linearLayout {
					orientation = LinearLayout.HORIZONTAL
					b20 = button("")
					b21 = button("")
					b22 = button("")
					b23 = button("")
					b24 = button("")
					b25 = button("")
				}.lparams(width = matchParent, height = 0) { weight = 0.16f }
				linearLayout {
					orientation = LinearLayout.HORIZONTAL
					b30 = button("")
					b31 = button("")
					b32 = button("")
					b33 = button("")
					b34 = button("")
					b35 = button("")
				}.lparams(width = matchParent, height = 0) { weight = 0.16f }
				linearLayout {
					orientation = LinearLayout.HORIZONTAL
					b40 = button("")
					b41 = button("")
					b42 = button("")
					b43 = button("")
					b44 = button("")
					b45 = button("")
				}.lparams(width = matchParent, height = 0) { weight = 0.16f }
				linearLayout {
					orientation = LinearLayout.HORIZONTAL
					b50 = button("")
					b51 = button("")
					b52 = button("")
					b53 = button("")
					b54 = button("")
					b55 = button("")
				}.lparams(width = matchParent, height = 0) { weight = 0.16f }
			}.lparams(width = matchParent, height = 0) { weight = 48f }

			lytOptionsPage1 = verticalLayout {
				visibility = View.GONE
			}.lparams(width = matchParent, height = 0) { weight = 48f }

			lytOptionsPage2 = verticalLayout {
				visibility = View.GONE
			}.lparams(width = matchParent, height = 0) { weight = 48f }
		}.applyRecursively(mainStyle)
	}
}
