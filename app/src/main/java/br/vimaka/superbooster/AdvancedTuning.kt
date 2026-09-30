package br.vimaka.superbooster

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.pm.PackageManager
import android.provider.Settings
import android.widget.Toast

object AdvancedTuning {
    private val keys = arrayOf(Settings.Global.WINDOW_ANIMATION_SCALE, Settings.Global.TRANSITION_ANIMATION_SCALE, Settings.Global.ANIMATOR_DURATION_SCALE)
    fun open(a: Activity) {
        if (!BuildConfig.DEVELOPER_EDITION) return
        if (a.checkSelfPermission(Manifest.permission.WRITE_SECURE_SETTINGS) != PackageManager.PERMISSION_GRANTED) {
            val grant = "adb shell pm grant ${a.packageName} android.permission.WRITE_SECURE_SETTINGS"
            val revoke = "adb shell pm revoke ${a.packageName} android.permission.WRITE_SECURE_SETTINGS"
            AlertDialog.Builder(a).setTitle("Developer: autorização ADB necessária")
                .setMessage("Este recurso muda somente as três escalas de animação. A permissão concede acesso amplo a configurações protegidas: autorize apenas se confiar neste app. Execute via ADB no computador:\n\n$grant\n\nPara revogar depois:\n$revoke\n\nNão fornece root nem controle da memória de outros apps.")
                .setPositiveButton("Copiar comando") { _, _ -> (a.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager).setPrimaryClip(ClipData.newPlainText("ADB", grant)) }
                .setNegativeButton("Cancelar", null).show()
            return
        }
        val prefs = a.getSharedPreferences("tuning-backup", 0)
        val current = keys.joinToString("\n") { "$it: ${Settings.Global.getFloat(a.contentResolver, it, 1f)}" }
        AlertDialog.Builder(a).setTitle("Animações: tuning reversível")
            .setMessage("$current\n\nAltera a duração das animações; não aumenta capacidade CPU/GPU/RAM. O perfil original é salvo antes da primeira alteração.")
            .setPositiveButton("0,5×") { _, _ -> apply(a, 0.5f) }
            .setNeutralButton("Sem animações") { _, _ -> apply(a, 0f) }
            .setNegativeButton("Restaurar original") { _, _ ->
                if (!prefs.contains("saved")) { Toast.makeText(a, "Nenhum ajuste salvo por este app", Toast.LENGTH_LONG).show(); return@setNegativeButton }
                try {
                    var ok = true
                    keys.forEach { key -> ok = Settings.Global.putFloat(a.contentResolver, key, prefs.getFloat(key, 1f)) && ok }
                    if (ok) prefs.edit().clear().apply()
                    Toast.makeText(a, if (ok) "Configuração original restaurada" else "Restauração parcial: confira configurações", Toast.LENGTH_LONG).show()
                } catch (e: Exception) { Toast.makeText(a, "Ajuste recusado: ${e.localizedMessage}", Toast.LENGTH_LONG).show() }
            }.show()
    }
    private fun apply(a: Activity, scale: Float) {
        val prefs = a.getSharedPreferences("tuning-backup", 0)
        if (!prefs.contains("saved")) {
            val editor = prefs.edit().putBoolean("saved", true)
            keys.forEach { editor.putFloat(it, Settings.Global.getFloat(a.contentResolver, it, 1f)) }
            if (!editor.commit()) { Toast.makeText(a, "Não foi possível salvar o original; nada alterado", Toast.LENGTH_LONG).show(); return }
        }
        try {
            var ok = true
            keys.forEach { ok = Settings.Global.putFloat(a.contentResolver, it, scale) && ok }
            Toast.makeText(a, if (ok) "Animações ajustadas; original preservado" else "Ajuste parcial: confira configurações", Toast.LENGTH_LONG).show()
        } catch (e: Exception) { Toast.makeText(a, "Ajuste recusado: ${e.localizedMessage}", Toast.LENGTH_LONG).show() }
    }
}
