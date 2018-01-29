package app.util

import java.util.regex.Pattern
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

object ColorUtil {
    private val hexColorRegex = Pattern.compile("^#?([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})\$")

    fun clampDegrees(num: Number) = (num.toDouble() % 360).toInt().let { if (it < 0) it + 360 else it }

    fun hexToRgb(hex: String): IntArray {
        val matcher = hexColorRegex.matcher(hex)

        if (!matcher.find())
            return intArrayOf(0, 0, 0)

        val code = matcher.group(matcher.groupCount())
                .let { if (it.length == 3) it.flatMap { arrayOf(it, it).asIterable() }.joinToString("") else it }

        return IntArray(3) { code.slice(it * 2..it * 2 + 1).toInt(16) }
    }

    /*
     * https://en.wikipedia.org/wiki/HSL_and_HSV#From_HSL
     */
    fun hslToRgb(hsl: Array<Number>): IntArray {
        val h = hsl[0].toDouble() / 60
        val s = hsl[1].toDouble()
        val l = hsl[2].toDouble()

        val chroma = (1 - abs(2 * l - 1)) * s
        val x = chroma * (1 - abs(h % 2 - 1))
        val m = l - chroma / 2

        var (r, g, b) = when (h) {
            in 0.0..1.0 -> doubleArrayOf(chroma, x, 0.0)
            in 1.0..2.0 -> doubleArrayOf(x, chroma, 0.0)
            in 2.0..3.0 -> doubleArrayOf(0.0, chroma, x)
            in 3.0..4.0 -> doubleArrayOf(0.0, x, chroma)
            in 4.0..5.0 -> doubleArrayOf(x, 0.0, chroma)
            in 5.0..6.0 -> doubleArrayOf(chroma, 0.0, x)
            else -> doubleArrayOf(0.0, 0.0, 0.0)
        }

        r += m
        g += m
        b += m

        r *= 255
        g *= 255
        b *= 255

        return intArrayOf(r.toInt(), g.toInt(), b.toInt())
    }

    fun rgbToHex(rgb: IntArray, hash: Boolean = true): String = rgb.map { it.toString(16) }
            .joinToString("", if (hash) "#" else "") { if (it.length == 1) it + it else it }

    /*
     * http://www.niwa.nu/2013/05/math-behind-colorspace-conversions-rgb-hsl/
     */
    fun rgbToHsl(rgb: IntArray): Array<Number> {
        var min = 1.0
        var max = 0.0

        val (r, g, b) = rgb.map { it / 255.0 }.map { min = min(min, it); max = max(max, it); it }

        val hue: Int
        val saturation: Double
        val lightness: Double

        if (min == max) {
            hue = 0
            saturation = 0.0
            lightness = min
        } else {
            val sum = min + max
            val range = max - min

            lightness = sum / 2
            saturation = range / if (lightness < 0.5) sum else 2 - sum
            hue = clampDegrees(60 * when (max) {
                r -> (g - b) / range
                g -> 2 + (b - r) / range
                else -> 4 + (r - g) / range
            })
        }

        return arrayOf(hue, saturation, lightness)
    }

    fun customizeHsl(hsl: Array<Number>): Array<Number> = arrayOf(clampDegrees(hsl[0]), .85, .6)
    fun customizeHex(hex: String): String = rgbToHex(hslToRgb(customizeHsl(rgbToHsl(hexToRgb(hex)))), hex.startsWith('#'))

}
