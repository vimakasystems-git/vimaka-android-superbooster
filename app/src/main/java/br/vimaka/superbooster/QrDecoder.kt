package br.vimaka.superbooster

import com.google.zxing.BarcodeFormat
import com.google.zxing.BinaryBitmap
import com.google.zxing.DecodeHintType
import com.google.zxing.MultiFormatReader
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.common.GlobalHistogramBinarizer

object QrDecoder {
    fun decode(width: Int, height: Int, pixels: IntArray): String? {
        require(width in 1..2048 && height in 1..2048 && pixels.size == width * height)
        val source = RGBLuminanceSource(width, height, pixels)
        val reader = MultiFormatReader()
        val hints = mapOf(DecodeHintType.TRY_HARDER to true, DecodeHintType.POSSIBLE_FORMATS to listOf(BarcodeFormat.QR_CODE))
        for (luminance in listOf(source, source.invert())) {
            for (binarizer in listOf(HybridBinarizer(luminance), GlobalHistogramBinarizer(luminance))) {
                try { return reader.decode(BinaryBitmap(binarizer), hints).text }
                catch (_: com.google.zxing.ReaderException) { reader.reset() }
            }
        }
        return null
    }
}
