package br.vimaka.superbooster

import android.app.Activity
import android.app.AlertDialog
import android.content.pm.PackageManager
import java.util.concurrent.atomic.AtomicBoolean
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.core.content.FileProvider
import androidx.core.content.pm.PackageInfoCompat
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

object Updater {
    private val busy = AtomicBoolean(false)
    private var pending: File? = null

    fun resumeInstall(act: Activity) {
        val file = pending ?: return
        if (act.packageManager.canRequestPackageInstalls()) {
            pending = null
            try { install(act, file) } catch (_: Exception) {
                AlertDialog.Builder(act).setMessage("Não foi possível abrir o instalador. Tente buscar a atualização novamente.").setPositiveButton("OK", null).show()
            }
        }
    }

    private fun get(url: String): HttpURLConnection =
        (URL(url).openConnection() as HttpURLConnection).apply {
            setRequestProperty("Accept", "application/vnd.github+json")
            connectTimeout = 15000; readTimeout = 30000
        }

    private fun newer(remote: String, local: String): Boolean {
        val r = remote.trim().removePrefix("v").split(".").map { it.toIntOrNull() ?: 0 }
        val l = local.split(".").map { it.toIntOrNull() ?: 0 }
        for (i in 0 until maxOf(r.size, l.size)) {
            val a = r.getOrElse(i) { 0 }; val b = l.getOrElse(i) { 0 }
            if (a != b) return a > b
        }
        return false
    }

    /** Roda em thread; chama log() com o progresso. */
    fun check(act: Activity, log: (String) -> Unit) {
      if (!busy.compareAndSet(false, true)) { log("Uma verificação já está em andamento."); return }
      Thread {
        try {
            val c = get("https://api.github.com/repos/${BuildConfig.GITHUB_REPO}/releases/latest")
            if (c.responseCode != 200) { val status = c.responseCode; c.disconnect(); log("Não foi possível consultar a release (HTTP $status)."); return@Thread }
            val j = try { JSONObject(c.inputStream.bufferedReader().use { it.readText() }) } finally { c.disconnect() }
            val tag = j.getString("tag_name")
            if (!newer(tag, BuildConfig.VERSION_NAME)) { log("Já está na última versão (${BuildConfig.VERSION_NAME})."); return@Thread }
            val assets = j.getJSONArray("assets")
            var apk: String? = null
            for (i in 0 until assets.length()) {
                val a = assets.getJSONObject(i)
                if (a.getString("name").endsWith(".apk")) apk = a.getString("browser_download_url")
            }
            if (apk == null) { log("Release $tag não tem .apk anexado."); return@Thread }
            log("Baixando $tag...")
            val downloadUrl = apk ?: return@Thread
            require(downloadUrl.startsWith("https://github.com/${BuildConfig.GITHUB_REPO}/releases/download/")) { "Origem do APK inválida" }
            val dir = File(act.filesDir, "updates").apply { mkdirs() }
            val f = File(dir, "update.apk")
            val download = get(downloadUrl)
            try {
                require(download.responseCode == 200) { "Falha no download: HTTP ${download.responseCode}" }
                download.inputStream.use { input -> f.outputStream().use { output -> input.copyTo(output) } }
            } finally { download.disconnect() }
            val info = act.packageManager.getPackageArchiveInfo(f.path, 0)
            require(info != null && info.packageName == act.packageName && PackageInfoCompat.getLongVersionCode(info) > BuildConfig.VERSION_CODE) { "APK incompatível ou versionCode não superior" }
            act.runOnUiThread {
                if (!act.isFinishing && !act.isDestroyed) {
                    AlertDialog.Builder(act).setTitle("Atualização $tag")
                        .setMessage("APK baixado. Deseja abrir o instalador Android? O Android valida a assinatura antes de atualizar.")
                        .setPositiveButton("Instalar") { _, _ ->
                            try { install(act, f) } catch (e: Exception) { log("Não foi possível instalar: ${e.localizedMessage}") }
                        }.setNegativeButton("Agora não", null).show()
                }
            }
            log("Download concluído. Confirme a instalação.")
        } catch (e: Exception) { log("Erro: ${e.message}") }
        finally { busy.set(false) }
      }.start()
    }

    private fun install(act: Activity, f: File) {
        if (!act.packageManager.canRequestPackageInstalls()) {
            pending = f
            act.startActivity(Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                Uri.parse("package:${act.packageName}")))
            return
        }
        val uri = FileProvider.getUriForFile(act, "${act.packageName}.fileprovider", f)
        act.startActivity(Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        })
    }
}
