package br.vimaka.superbooster

import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import android.os.PowerManager
import android.os.StatFs
import android.os.SystemClock
import java.io.File
import java.util.concurrent.Executors

object PerformanceCheck {
    private val executor = Executors.newSingleThreadExecutor()
    @Volatile private var running = false
    fun run(a: Activity, output: (String) -> Unit) {
        if (running) { output("Diagnóstico em andamento."); return }
        running = true
        output("Medindo RAM, armazenamento, bateria e estado térmico. Teste local limitado a 1 MiB; nenhum parâmetro é alterado.")
        executor.execute {
            val report = try {
                val mi = ActivityManager.MemoryInfo()
                (a.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager).getMemoryInfo(mi)
                val storage = StatFs(a.filesDir.path)
                val battery = a.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
                val power = a.getSystemService(Context.POWER_SERVICE) as PowerManager
                val level = battery?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
                val scale = battery?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
                val temperature = battery?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1) ?: -1
                val file = File.createTempFile("performance-", ".tmp", a.cacheDir)
                val elapsed = try {
                    val start = SystemClock.elapsedRealtimeNanos()
                    file.outputStream().use { it.write(ByteArray(1024 * 1024) { index -> (index % 251).toByte() }) }
                    file.inputStream().use { input -> val buffer = ByteArray(8192); while (input.read(buffer) >= 0) { } }
                    (SystemClock.elapsedRealtimeNanos() - start) / 1_000_000.0
                } finally { file.delete() }
                buildString {
                    appendLine("CHECK DE DESEMPENHO — ${Build.MODEL}")
                    appendLine("Android ${Build.VERSION.RELEASE}; ABI ${Build.SUPPORTED_ABIS.joinToString()}")
                    appendLine("RAM física: ${mi.totalMem / 1048576} MiB; disponível: ${mi.availMem / 1048576} MiB")
                    appendLine("Pressão de memória: ${if (mi.lowMemory) "alta" else "não sinalizada pelo Android"}")
                    appendLine("Armazenamento: ${storage.availableBytes / 1048576} MiB livres de ${storage.totalBytes / 1048576} MiB")
                    appendLine("Teste leitura/escrita 1 MiB: %.1f ms (inclui cache; não é benchmark de disco)".format(elapsed))
                    appendLine("Bateria: ${if (level >= 0 && scale > 0) "${level * 100 / scale}%" else "indisponível"}")
                    appendLine("Temperatura da bateria: ${if (temperature >= 0) "${temperature / 10.0} °C" else "indisponível"}")
                    appendLine("Economia de energia: ${power.isPowerSaveMode}")
                    if (Build.VERSION.SDK_INT >= 29) appendLine("Estado térmico Android: ${power.currentThermalStatus} (0 sem restrição; valores maiores indicam restrição)")
                    appendLine("\nRECOMENDAÇÕES")
                    if (mi.lowMemory) appendLine("• Reduza apps ativos e revise segundo plano em Perfis. Fechar repetidamente pode aumentar recargas.")
                    if (storage.availableBytes < storage.totalBytes / 10) appendLine("• Libere armazenamento: menos de 10% disponível neste volume.")
                    if (temperature >= 420) appendLine("• Bateria aquecida: interrompa carga pesada e deixe esfriar. Não elevar clocks.")
                    if (power.isPowerSaveMode) appendLine("• Economia de energia limita desempenho; revise conforme autonomia desejada.")
                    appendLine("• Reduzir animações muda fluidez percebida; não aumenta CPU ou RAM.")
                    appendLine("• Não usar limite global de processos como rotina: pode prejudicar notificações e multitarefa.")
                    appendLine("\nDiagnóstico pontual: não mede desempenho de todos os apps, CPU/GPU ou câmera e não garante ganho.")
                }
            } catch (e: Exception) { "Diagnóstico indisponível: ${e.localizedMessage}" }
            running = false
            a.runOnUiThread { if (!a.isDestroyed && !a.isFinishing) output(report) }
        }
    }
}
