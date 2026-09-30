package br.vimaka.superbooster

import android.app.Activity
import android.graphics.BitmapFactory
import android.net.Uri
import com.google.zxing.BarcodeFormat
import com.google.zxing.BinaryBitmap
import com.google.zxing.DecodeHintType
import com.google.zxing.MultiFormatReader
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.common.GlobalHistogramBinarizer
import java.util.concurrent.Executors

object QrImageReader {
    private val worker = Executors.newSingleThreadExecutor()
    fun decode(a: Activity, uri: Uri, output: (String) -> Unit) {
        output("Lendo QR na imagem selecionada; processamento local, sem envio da imagem.")
        worker.execute {
            val result = try {
                val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                a.contentResolver.openInputStream(uri).use { BitmapFactory.decodeStream(it, null, bounds) }
                require(bounds.outWidth > 0 && bounds.outHeight > 0) { "Imagem inválida" }
                var sample = 1
                while (maxOf(bounds.outWidth, bounds.outHeight) / sample > 2048) sample *= 2
                val options = BitmapFactory.Options().apply { inSampleSize = sample }
                val bitmap = a.contentResolver.openInputStream(uri).use { BitmapFactory.decodeStream(it, null, options) }
                    ?: throw IllegalArgumentException("Não foi possível abrir a imagem")
                try {
                    val pixels = IntArray(bitmap.width * bitmap.height)
                    bitmap.getPixels(pixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
                    val source = RGBLuminanceSource(bitmap.width, bitmap.height, pixels)
                    val reader = MultiFormatReader()
                    val hints = mapOf(DecodeHintType.TRY_HARDER to true, DecodeHintType.POSSIBLE_FORMATS to listOf(BarcodeFormat.QR_CODE))
                    var content: String? = null
                    for (luminance in listOf(source, source.invert())) {
                        for (binarizer in listOf(HybridBinarizer(luminance), GlobalHistogramBinarizer(luminance))) {
                            try { content = reader.decode(BinaryBitmap(binarizer), hints).text; break }
                            catch (_: com.google.zxing.ReaderException) { reader.reset() }
                        }
                        if (content != null) break
                    }
                    content?.let { "QR lido:\n$it\n\nRevise o conteúdo antes de usar; nenhum link foi aberto." }
                        ?: "QR não encontrado. Use imagem nítida, com o código inteiro e margem visível. Não é possível recuperar detalhe que a câmera não capturou."
                } finally { bitmap.recycle() }
            } catch (e: Exception) { "Erro na imagem: ${e.localizedMessage}" }
            a.runOnUiThread { if (!a.isDestroyed && !a.isFinishing) output(result) }
        }
    }
}
