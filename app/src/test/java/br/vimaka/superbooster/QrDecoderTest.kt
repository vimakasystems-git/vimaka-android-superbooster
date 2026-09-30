package br.vimaka.superbooster

import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import org.junit.Assert.*
import org.junit.Test

class QrDecoderTest {
    private fun pixels(inverted: Boolean, lowContrast: Boolean = false): IntArray {
        val matrix = QRCodeWriter().encode("https://vimaka.com/", BarcodeFormat.QR_CODE, 320, 320)
        return IntArray(320 * 320) { i ->
            val dark = matrix[i % 320, i / 320] xor inverted
            if (lowContrast) { if (dark) 0xff606060.toInt() else 0xffb0b0b0.toInt() }
            else if (dark) 0xff000000.toInt() else 0xffffffff.toInt()
        }
    }
    @Test fun readsStandardQr() { assertEquals("https://vimaka.com/", QrDecoder.decode(320, 320, pixels(false))) }
    @Test fun readsInvertedQr() { assertEquals("https://vimaka.com/", QrDecoder.decode(320, 320, pixels(true))) }
    @Test fun readsReducedContrastQr() { assertEquals("https://vimaka.com/", QrDecoder.decode(320, 320, pixels(false, true))) }
    @Test fun doesNotInventQrOnBlankImage() { assertNull(QrDecoder.decode(320, 320, IntArray(320 * 320) { -1 })) }
    @Test(expected = IllegalArgumentException::class) fun rejectsInvalidDimensions() { QrDecoder.decode(0, 1, intArrayOf()) }
}
