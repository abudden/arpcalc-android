package uk.co.cgtk.karpcalc

import android.content.Context
import android.text.method.ScrollingMovementMethod
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.view.ViewGroup.LayoutParams

class MainActivityUi {
	private val mainStyle = { v: View ->
		when (v) {
			is Button -> {
				var params = LinearLayout.LayoutParams(0, LayoutParams.MATCH_PARENT, 0.16f)
				params.topMargin = 0
				params.bottomMargin = 0
				params.leftMargin = 0
				params.rightMargin = 0
				v.setLayoutParams(params)
				v.textSize = 12f
				v.setPadding(0, 0, 0, 0)
				v.setAllCaps(false)
			}
			is TextView -> {
				if (v.getText().isEmpty()) {
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
	private val tabStyle = { v: View ->
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

	private val matchParent = LayoutParams.MATCH_PARENT

	/* Small helpers to keep the layout code below readable */
	private fun <T : View> ViewGroup.add(view: T, params: LayoutParams? = null): T {
		if (params == null) {
			addView(view)
		}
		else {
			addView(view, params)
		}
		return view
	}

	private fun weighted(width: Int, height: Int, weight: Float) =
		LinearLayout.LayoutParams(width, height, weight)

	private fun linearLayout(ctx: Context, orient: Int) = LinearLayout(ctx).apply {
		orientation = orient
	}

	private fun statusText(ctx: Context, initial: String) = TextView(ctx).apply {
		gravity = Gravity.CENTER
		text = initial
	}

	private fun tabImage(ctx: Context, resource: Int, onClick: () -> Unit) = ImageView(ctx).apply {
		setImageResource(resource)
		setOnClickListener { onClick() }
	}

	private fun buttonRow(ctx: Context): Pair<LinearLayout, List<Button>> {
		var row = linearLayout(ctx, LinearLayout.HORIZONTAL)
		var rowButtons = List(6) { row.add(Button(ctx)) }
		return Pair(row, rowButtons)
	}

	private fun applyRecursively(v: View, style: (View) -> Unit) {
		style(v)
		if (v is ViewGroup) {
			for (i in 0 until v.childCount) {
				applyRecursively(v.getChildAt(i), style)
			}
		}
	}

	fun createView(owner: MainActivity): View {
		val ctx: Context = owner
		var root = linearLayout(ctx, LinearLayout.VERTICAL)

		var statusLine = root.add(linearLayout(ctx, LinearLayout.HORIZONTAL),
				weighted(matchParent, 0, 5f))
		txtStatusExponent = statusLine.add(statusText(ctx, "EXP:10"), weighted(0, matchParent, 20f))
		statusLine.add(View(ctx), weighted(0, matchParent, 20f))
		txtStatusAngular = statusLine.add(statusText(ctx, "DEG"), weighted(0, matchParent, 20f))
		statusLine.add(View(ctx), weighted(0, matchParent, 20f))
		txtStatusBase = statusLine.add(statusText(ctx, "DEC"), weighted(0, matchParent, 20f))

		var displayArea = root.add(linearLayout(ctx, LinearLayout.HORIZONTAL),
				weighted(matchParent, 0, 25f))
		var stackHolder = displayArea.add(linearLayout(ctx, LinearLayout.VERTICAL),
				weighted(0, matchParent, 40f))
		stackScroll = stackHolder.add(ScrollView(ctx), LinearLayout.LayoutParams(matchParent, matchParent))
		txtStack = stackScroll.add(TextView(ctx).apply {
			gravity = Gravity.BOTTOM or Gravity.LEFT
		}, FrameLayout.LayoutParams(matchParent, matchParent))
		displayArea.add(linearLayout(ctx, LinearLayout.VERTICAL), weighted(0, matchParent, 5f))
		txtBase = displayArea.add(TextView(ctx).apply {
			gravity = Gravity.TOP or Gravity.LEFT
		}, weighted(0, matchParent, 55f))

		var entryHeight = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 40f,
				ctx.resources.displayMetrics).toInt()
		txtEntry = root.add(TextView(ctx).apply {
			textSize = 25f
			gravity = Gravity.BOTTOM or Gravity.RIGHT
			text = ""
			setSingleLine(true)
		}, LinearLayout.LayoutParams(matchParent, entryHeight))

		var tabs = root.add(linearLayout(ctx, LinearLayout.HORIZONTAL),
				weighted(matchParent, 0, 12f))
		imNumPad   = tabs.add(tabImage(ctx, R.drawable.numpad)   { owner.tabSelect("numpad") })
		imFuncPad  = tabs.add(tabImage(ctx, R.drawable.funcpad)  { owner.tabSelect("funcpad") })
		imConvPad  = tabs.add(tabImage(ctx, R.drawable.convpad)  { owner.tabSelect("convpad") })
		imConstPad = tabs.add(tabImage(ctx, R.drawable.constpad) { owner.tabSelect("constpad") })
		imOptPad   = tabs.add(tabImage(ctx, R.drawable.optpad)   { owner.tabSelect("optpad1") })
		applyRecursively(tabs, tabStyle)

		lytButtons = root.add(linearLayout(ctx, LinearLayout.VERTICAL),
				weighted(matchParent, 0, 48f))
		var buttonRows = List(6) {
			var (row, rowButtons) = buttonRow(ctx)
			lytButtons.add(row, weighted(matchParent, 0, 0.16f))
			rowButtons
		}
		b00 = buttonRows[0][0]; b01 = buttonRows[0][1]; b02 = buttonRows[0][2]; b03 = buttonRows[0][3]; b04 = buttonRows[0][4]; b05 = buttonRows[0][5]
		b10 = buttonRows[1][0]; b11 = buttonRows[1][1]; b12 = buttonRows[1][2]; b13 = buttonRows[1][3]; b14 = buttonRows[1][4]; b15 = buttonRows[1][5]
		b20 = buttonRows[2][0]; b21 = buttonRows[2][1]; b22 = buttonRows[2][2]; b23 = buttonRows[2][3]; b24 = buttonRows[2][4]; b25 = buttonRows[2][5]
		b30 = buttonRows[3][0]; b31 = buttonRows[3][1]; b32 = buttonRows[3][2]; b33 = buttonRows[3][3]; b34 = buttonRows[3][4]; b35 = buttonRows[3][5]
		b40 = buttonRows[4][0]; b41 = buttonRows[4][1]; b42 = buttonRows[4][2]; b43 = buttonRows[4][3]; b44 = buttonRows[4][4]; b45 = buttonRows[4][5]
		b50 = buttonRows[5][0]; b51 = buttonRows[5][1]; b52 = buttonRows[5][2]; b53 = buttonRows[5][3]; b54 = buttonRows[5][4]; b55 = buttonRows[5][5]

		lytOptionsPage1 = root.add(linearLayout(ctx, LinearLayout.VERTICAL).apply {
			visibility = View.GONE
		}, weighted(matchParent, 0, 48f))

		lytOptionsPage2 = root.add(linearLayout(ctx, LinearLayout.VERTICAL).apply {
			visibility = View.GONE
		}, weighted(matchParent, 0, 48f))

		applyRecursively(root, mainStyle)
		return root
	}
}
