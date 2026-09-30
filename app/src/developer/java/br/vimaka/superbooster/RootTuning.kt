package br.vimaka.superbooster

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import android.os.PowerManager
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

/** Adaptador opcional: apenas governor já suportado pelo kernel. Sem overclock nem alteração térmica. */
object RootTuning {
    private val worker = Executors.newSingleThreadExecutor()
    @Volatile private var busy = false
    private const val inspect = """
set -e
printf 'BOOT|'; cat /proc/sys/kernel/random/boot_id
for p in /sys/devices/system/cpu/cpufreq/policy*; do
  [ -r "${'$'}p/scaling_governor" ] || continue
  printf 'CPU|%s|' "${'$'}p"; tr -d '\n' < "${'$'}p/scaling_governor"; printf '|'; cat "${'$'}p/scaling_available_governors"
done
"""
    fun open(a: Activity) {
        AlertDialog.Builder(a).setTitle("Root: governor de CPU do kernel")
            .setMessage("Funciona somente se o aparelho já estiver com root e seu gerenciador autorizar este app. Perfil Performance usa apenas o governor performance suportado pelo kernel, sem overclock. Pode aumentar consumo e calor; não garante ganho sustentado. Limites térmicos permanecem ativos. Não altera RAM, GPU, swap, câmera ou apps. Backup feito antes de aplicar; restauração válida na mesma inicialização do kernel.")
            .setPositiveButton("Aplicar Performance") { _, _ -> execute(a, false) }
            .setNeutralButton("Restaurar CPU") { _, _ -> execute(a, true) }
            .setNegativeButton("Cancelar", null).show()
    }
    private fun command(script: String): String {
        val p = ProcessBuilder("su", "-c", script).redirectErrorStream(true).start()
        val output = StringBuilder()
        val drain = Thread {
            p.inputStream.bufferedReader().useLines { lines -> lines.forEach { line -> synchronized(output) { if (output.length < 65536) output.appendLine(line.take(4096)) } } }
        }.apply { isDaemon = true; start() }
        if (!p.waitFor(20, TimeUnit.SECONDS)) { p.destroyForcibly(); drain.join(1000); throw IllegalStateException("Root não autorizado ou comando expirou; confira o gerenciador root") }
        drain.join(1000)
        val text = synchronized(output) { output.toString() }
        check(p.exitValue() == 0) { "Comando root recusado: ${text.takeLast(1000)}" }
        return text
    }
    private fun tooHot(a: Activity): Boolean {
        val pm = a.getSystemService(Context.POWER_SERVICE) as PowerManager
        if (Build.VERSION.SDK_INT >= 29 && pm.currentThermalStatus >= PowerManager.THERMAL_STATUS_MODERATE) return true
        val battery = a.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        return (battery?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1) ?: -1) >= 420
    }
    private fun execute(a: Activity, restore: Boolean) {
        if (busy) return
        if (!restore && tooHot(a)) { AlertDialog.Builder(a).setMessage("Aparelho aquecido ou limitado termicamente. Perfil não aplicado.").setPositiveButton("OK", null).show(); return }
        busy = true
        worker.execute {
            val prefs = a.getSharedPreferences("root-cpu-backup", 0)
            val report = try {
                val lines = command(inspect).lines()
                val boot = lines.firstOrNull { it.startsWith("BOOT|") }?.substringAfter('|') ?: error("Identificação do kernel indisponível")
                val rows = lines.filter { it.startsWith("CPU|") }.map { it.split('|') }
                require(rows.isNotEmpty()) { "Kernel não expõe governors CPU compatíveis" }
                if (restore) {
                    val snapshot = JSONObject(prefs.getString("snapshot", null) ?: error("Não há backup de CPU deste app"))
                    require(snapshot.getString("boot") == boot) { "Kernel reiniciado: backup antigo não será reaplicado. Governors normalmente são redefinidos pelo sistema no boot." }
                    val entries = snapshot.getJSONArray("policies")
                    val script = buildString {
                        appendLine("set -e")
                        for (i in 0 until entries.length()) {
                            val entry = entries.getJSONObject(i)
                            val path = entry.getString("path"); val governor = entry.getString("governor")
                            validate(path, governor)
                            appendLine("printf '%s' '$governor' > '$path/scaling_governor'")
                            appendLine("test \"${'$'}(cat '$path/scaling_governor')\" = '$governor'")
                        }
                    }
                    command(script)
                    prefs.edit().clear().commit()
                    "Governors originais restaurados."
                } else {
                    require(!prefs.contains("snapshot")) { "Existe backup anterior. Restaure primeiro para preservar os parâmetros originais." }
                    val eligible = rows.filter { it.size == 4 && it[3].split(Regex("\\s+")).contains("performance") }
                    require(eligible.isNotEmpty()) { "Nenhuma política suporta governor performance; nenhum parâmetro alterado" }
                    val entries = JSONArray()
                    eligible.forEach { row -> validate(row[1], row[2]); entries.put(JSONObject().put("path", row[1]).put("governor", row[2])) }
                    val snapshot = JSONObject().put("boot", boot).put("policies", entries)
                    require(prefs.edit().putString("snapshot", snapshot.toString()).commit()) { "Não foi possível salvar o backup; nada alterado" }
                    val apply = buildString {
                        appendLine("set -e")
                        eligible.forEach { row ->
                            appendLine("printf '%s' 'performance' > '${row[1]}/scaling_governor'")
                            appendLine("test \"${'$'}(cat '${row[1]}/scaling_governor')\" = 'performance'")
                        }
                    }
                    try { command(apply) }
                    catch (e: Exception) {
                        val rollback = buildString { appendLine("set -e"); eligible.forEach { appendLine("printf '%s' '${it[2]}' > '${it[1]}/scaling_governor'") } }
                        try { command(rollback); prefs.edit().clear().commit() } catch (_: Exception) { }
                        throw e
                    }
                    "Perfil Performance aplicado em ${eligible.size} políticas CPU compatíveis. Backup salvo para restaurar. Frequências máximas, GPU e controles térmicos não foram alterados. Se aquecer ou consumir demais, toque em Restaurar CPU. O Android/fabricante pode sobrescrever o governor."
                }
            } catch (e: Exception) { "Tuning não concluído: ${e.localizedMessage}. Confira/restaure o backup se houve alteração parcial." }
            busy = false
            a.runOnUiThread { if (!a.isDestroyed && !a.isFinishing) AlertDialog.Builder(a).setTitle("Resultado CPU Root").setMessage(report).setPositiveButton("OK", null).show() }
        }
    }
    private fun validate(path: String, governor: String) {
        require(path.matches(Regex("/sys/devices/system/cpu/cpufreq/policy[0-9]+")))
        require(governor.matches(Regex("[a-zA-Z0-9_-]+")))
    }
}
