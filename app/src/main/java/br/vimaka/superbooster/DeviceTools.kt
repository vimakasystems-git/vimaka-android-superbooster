package br.vimaka.superbooster

import android.app.Activity
import android.app.ActivityManager
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.StatFs
import android.provider.Settings
import android.widget.Toast

/** Somente APIs públicas; nenhuma alteração privilegiada silenciosa. */
object DeviceTools {
    private fun open(a: Activity, intent: Intent) {
        try { a.startActivity(intent) }
        catch (_: Exception) { Toast.makeText(a, "Opção indisponível neste dispositivo", Toast.LENGTH_LONG).show() }
    }
    private fun message(a: Activity, title: String, text: String) {
        AlertDialog.Builder(a).setTitle(title).setMessage(text).setPositiveButton("OK", null).show()
    }
    fun profiles(a: Activity) {
        val prefs = a.getSharedPreferences("profiles", 0)
        val names = arrayOf("Equilibrado", "Trabalho", "Jogos", "Economia")
        AlertDialog.Builder(a).setTitle("Perfis locais: não alteram o boot do Android")
            .setItems(names) { _, index ->
                val key = names[index]
                val apps = a.packageManager.queryIntentActivities(Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER), 0)
                    .filter { it.activityInfo.packageName != a.packageName }
                    .distinctBy { it.activityInfo.packageName }
                    .sortedBy { it.loadLabel(a.packageManager).toString() }
                val saved = prefs.getStringSet(key, emptySet()).orEmpty()
                val selected = BooleanArray(apps.size) { apps[it].activityInfo.packageName in saved }
                AlertDialog.Builder(a).setTitle("$key: apps prioritários")
                    .setMultiChoiceItems(apps.map { it.loadLabel(a.packageManager).toString() }.toTypedArray(), selected) { _, i, checked -> selected[i] = checked }
                    .setPositiveButton("Salvar e revisar") { _, _ ->
                        val chosen = apps.indices.filter { selected[it] }.map { apps[it].activityInfo.packageName }.toSet()
                        prefs.edit().putStringSet(key, chosen).apply()
                        val review = apps.filter { it.activityInfo.packageName !in chosen }
                        if (review.isEmpty()) message(a, key, "Perfil salvo. O Android controla memória e inicialização. Não há apps restantes para revisar.")
                        else AlertDialog.Builder(a).setTitle("Revise apps fora do perfil")
                            .setMessage("Perfil salvo. Ajuste bateria/segundo plano manualmente; notificações podem atrasar. Para escolher um app, toque em Continuar.")
                            .setPositiveButton("Continuar") { _, _ ->
                                AlertDialog.Builder(a).setTitle("Abrir configurações do app")
                                    .setItems(review.map { it.loadLabel(a.packageManager).toString() }.toTypedArray()) { _, i ->
                                        open(a, Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:${review[i].activityInfo.packageName}")))
                                    }.setNegativeButton("Fechar", null).show()
                            }.setNegativeButton("Fechar", null).show()
                    }.setNegativeButton("Cancelar", null).show()
            }.setNegativeButton("Fechar", null).show()
    }
    fun tuning(a: Activity) {
        val labels = arrayOf("Armazenamento: revisar espaço", "Bateria: revisar consumo", "Aplicativos: restringir segundo plano", "Acessibilidade: opções de animação", "Desenvolvedor: ajustes manuais")
        val actions = arrayOf(Settings.ACTION_INTERNAL_STORAGE_SETTINGS, Settings.ACTION_BATTERY_SAVER_SETTINGS, Settings.ACTION_MANAGE_ALL_APPLICATIONS_SETTINGS, Settings.ACTION_ACCESSIBILITY_SETTINGS, Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS)
        AlertDialog.Builder(a).setTitle("Tuning guiado pelo Android")
            .setItems(labels) { _, i -> open(a, Intent(actions[i])) }
            .setNegativeButton("Fechar", null).show()
    }
    fun memory(a: Activity): String {
        val info = ActivityManager.MemoryInfo()
        (a.getSystemService(Activity.ACTIVITY_SERVICE) as ActivityManager).getMemoryInfo(info)
        val cards = a.getExternalFilesDirs(null).filterNotNull().drop(1)
        val cardInfo = cards.joinToString("\n") { f ->
            val st = StatFs(f.path)
            "Volume secundário: ${st.availableBytes / 1048576} MiB livres na área acessível ao app"
        }.ifEmpty { "Nenhum volume secundário acessível detectado." }
        return "RAM física: ${info.totalMem / 1048576} MiB\nRAM disponível: ${info.availMem / 1048576} MiB\nPressão baixa de memória: ${info.lowMemory}\n\n$cardInfo\n\nCartão microSD é armazenamento, não RAM física. Swap exige suporte do kernel, filesystem compatível e privilégios de sistema/root. Pode causar latência e desgaste; não é habilitado por este app. zRAM usa compressão na RAM e é administrada pelo Android.\n\nNão fechar apps repetidamente: isso pode aumentar recargas e consumo. Perfis organizam a revisão manual; não limitam processos por conta própria."
    }
    fun firmware(a: Activity) {
        val report = "Fabricante: ${Build.MANUFACTURER}\nModelo: ${Build.MODEL}\nDispositivo: ${Build.DEVICE}\nProduto: ${Build.PRODUCT}\nPlaca: ${Build.BOARD}\nABIs: ${Build.SUPPORTED_ABIS.joinToString()}\nAndroid: ${Build.VERSION.RELEASE} / API ${Build.VERSION.SDK_INT}\nPatch: ${Build.VERSION.SECURITY_PATCH}\nBuild: ${Build.DISPLAY}\n\nROM precisa corresponder ao modelo e à placa exatos. GSI depende de Treble, bootloader e drivers; pode apagar dados ou inutilizar o aparelho. Este app não verifica compatibilidade nem instala firmware."
        AlertDialog.Builder(a).setTitle("Android e firmware")
            .setMessage(report)
            .setPositiveButton("Atualização oficial") { _, _ -> open(a, Intent("android.settings.SYSTEM_UPDATE_SETTINGS")) }
            .setNeutralButton("Fontes abertas") { _, _ ->
                val names = arrayOf("LineageOS: dispositivos suportados", "AOSP: GSI e requisitos", "Termux: terminal Linux")
                val urls = arrayOf("https://wiki.lineageos.org/devices/", "https://source.android.com/docs/setup/create/gsi", "https://github.com/termux/proot-distro")
                AlertDialog.Builder(a).setTitle("Consultar fontes oficiais; não há ROM validada")
                    .setItems(names) { _, i -> open(a, Intent(Intent.ACTION_VIEW, Uri.parse(urls[i]))) }.show()
            }.setNegativeButton("Fechar", null).show()
    }
}
