package __APP_PACKAGE__

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import androidx.core.content.ContextCompat
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.File
import java.io.FileOutputStream

/**
 * Renders the launcher icon layers into shots/ (`./gradlew snap`), so you can look at them:
 *  - icon-512.png     background and foreground layers, full bleed. Upload this to Google Play.
 *  - icon-48.png      the same icon at launcher size. Is the symbol still clear?
 *  - icon-themed.png  the monochrome layer on white, the shape Android 13 themed icons tint.
 */
@RunWith(RobolectricTestRunner::class)
class IconTest {
    private val context: Context = ApplicationProvider.getApplicationContext()

    private fun save(bitmap: Bitmap, name: String) {
        val dir = File(System.getProperty("shots.dir") ?: "build/shots")
        dir.mkdirs()
        FileOutputStream(File(dir, name)).use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
    }

    private fun draw(canvas: Canvas, resId: Int, size: Int) {
        val drawable = checkNotNull(ContextCompat.getDrawable(context, resId)) { "missing drawable $resId" }
        drawable.setBounds(0, 0, size, size)
        drawable.draw(canvas)
    }

    private fun Bitmap.hasInk(): Boolean {
        for (x in 0 until width step 4) for (y in 0 until height step 4) {
            if (Color.alpha(getPixel(x, y)) > 0 && getPixel(x, y) != Color.WHITE) return true
        }
        return false
    }

    @Test
    fun launcher_icon_renders_at_store_and_launcher_size() {
        val size = 512
        val icon = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(icon)
        canvas.drawColor(ContextCompat.getColor(context, R.color.ic_launcher_background))
        draw(canvas, R.drawable.ic_launcher_foreground, size)
        save(icon, "icon-512.png")
        save(Bitmap.createScaledBitmap(icon, 48, 48, true), "icon-48.png")
        assertTrue("the foreground drew nothing over the background", icon.hasInk())
    }

    @Test
    fun themed_icon_layer_renders() {
        val size = 432
        val layer = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(layer)
        canvas.drawColor(Color.WHITE)
        draw(canvas, R.drawable.ic_launcher_monochrome, size)
        save(layer, "icon-themed.png")
        assertTrue("the monochrome layer drew nothing", layer.hasInk())
    }
}
