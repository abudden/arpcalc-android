package uk.co.cgtk.karpcalc

import android.graphics.Rect
import android.os.Looper
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/*
 * The options pages laid out as on a phone (with Android's own text
 * measurement), at the font sizes a phone's settings allow.  With a larger
 * font size, the options pages used to lose everything but the first
 * column of icons (the GridLayout they were in sized its columns to the
 * labels, which then didn't fit).
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w411dp-h914dp-port-xxhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class OptionsLayoutTest {
	fun textViews(v: View): List<TextView> {
		val result = mutableListOf<TextView>()
		if ((v is TextView) && (v.text.isNotEmpty())) {
			result.add(v)
		}
		if (v is ViewGroup) {
			for (i in 0 until v.childCount) {
				result.addAll(textViews(v.getChildAt(i)))
			}
		}
		return result
	}

	fun checkPage(activity: MainActivity, page: ViewGroup, what: String, expected: Int) {
		shadowOf(Looper.getMainLooper()).idle()
		val decor = activity.window.decorView
		val width = activity.resources.displayMetrics.widthPixels
		val height = activity.resources.displayMetrics.heightPixels
		decor.measure(View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY),
				View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY))
		decor.layout(0, 0, width, height)

		val labels = textViews(page)
		/* Every option's label and the More/Back button */
		assertEquals(what, expected, labels.size)
		for (t in labels) {
			val name = "$what: \"${t.text}\""
			assertTrue("$name isn't shown", t.isShown)
			val r = Rect()
			assertTrue("$name is off the screen", t.getGlobalVisibleRect(r))
			assertTrue("$name goes off the side ($r)", (r.left >= 0) && (r.right <= width))
			assertEquals("$name is cut off", t.width, r.width())
			/* All of its text fits */
			val layout = t.layout
			assertNotNull("$name has no layout", layout)
			assertTrue("$name doesn't fit (${layout.height} > ${t.height})",
					layout.height <= (t.height - t.totalPaddingTop - t.totalPaddingBottom))
			assertTrue("$name is too narrow", layout.width > 0)
		}
	}

	@Test
	fun optionsAtEachFontSize() {
		val visible = CommandHandler().getOptions().values.filter { ! it.hideOnPhone }
		for (scale in listOf(0.85f, 1.0f, 1.15f, 1.3f, 1.5f, 1.8f, 2.0f)) {
			RuntimeEnvironment.setFontScale(scale)
			val controller = Robolectric.buildActivity(MainActivity::class.java).setup()
			val activity = controller.get()
			for (page in 0..1) {
				activity.tabSelect("optpad1")
				if (page == 1) {
					activity.tabSelect("optpad2")
				}
				val layout = if (page == 0) activity.mainUI.lytOptionsPage1 else activity.mainUI.lytOptionsPage2
				checkPage(activity, layout, "font scale $scale, page ${page + 1}",
						visible.count { it.location.page == page } + 1)
			}
			controller.pause().stop().destroy()
		}
		RuntimeEnvironment.setFontScale(1.0f)
	}
}
