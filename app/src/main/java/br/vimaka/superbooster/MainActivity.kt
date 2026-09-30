package br.vimaka.superbooster

import android.app.Activity
import android.app.ActivityManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.StatFs
import android.provider.Settings
import android.widget.ImageView
import android.text.method.LinkMovementMethod
import android.text.SpannableString
import android.text.Spanned
import android.text.style.URLSpan
import android.content.ActivityNotFoundException
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView

class MainActivity : Activity() {
    private lateinit var out: TextView

    private val termuxCmd =
        "pkg update -y && pkg install -y proot-distro && proot-distro install ubuntu && proot-distro login ubuntu"
    private val adbCmds = """
adb shell settings put global window_animation_scale 0.5
adb shell settings put global transition_animation_scale 0.5
adb shell settings put global animator_duration_scale 0.5

""".trim()

    override fun onCreate(b: Bundle?) {
        super.onCreate(b)
        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(20), dp(20), dp(20), dp(20)) }
        root.addView(ImageView(this).apply {
            setImageResource(R.drawable.vimaka_logo)
            contentDescription = "Vimaka Sistemas Inteligentes"
            adjustViewBounds = true
            scaleType = ImageView.ScaleType.FIT_CENTER
        }, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(150)))
        root.addView(TextView(this).apply { text = "Vimaka Android SuperBooster v${BuildConfig.VERSION_NAME}"; textSize = 22f })
        btn(root, "1. Diagnóstico (RAM / armazenamento)") { show(diagnostic()) }
        btn(root, "2. Auditoria de segurança") { show(audit()) }
        btn(root, "3. Otimização: abrir Opções do desenvolvedor") {
            openSafely(Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS))
        }
        btn(root, "3b. Lista de apps (desativar bloatware)") {
            openSafely(Intent(Settings.ACTION_MANAGE_ALL_APPLICATIONS_SETTINGS))
        }
        if (BuildConfig.DEVELOPER_EDITION) btn(root, "3c. Copiar comandos ADB de animação") { copy(adbCmds); show("Comandos copiados:\n\n$adbCmds") }
        btn(root, "4. Terminal Ubuntu (Termux + proot)") { openTermux() }
        btn(root, "5. Atualizar o app") {
            if (BuildConfig.DEVELOPER_EDITION) Updater.check(this) { m -> runOnUiThread { show(m) } }
            else openSafely(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$packageName")))
        }
        btn(root, "6. Perfil de aplicativos e inicialização") { DeviceTools.profiles(this) }
        btn(root, "7. Tuning guiado") { DeviceTools.tuning(this) }
        btn(root, "8. RAM, zRAM e cartão microSD") { show(DeviceTools.memory(this)) }
        btn(root, "9. Android: dispositivo e atualizações") { DeviceTools.firmware(this) }
        out = TextView(this).apply { textSize = 14f; setTextIsSelectable(true); setPadding(0, 24, 0, 0) }
        root.addView(out)
        val credits = "Created by Douglas Cardoso | https://vimaka.com | WhatsApp +55 11 945546072"
        root.addView(TextView(this).apply {
            text = SpannableString(credits).apply {
                val site = "https://vimaka.com"
                setSpan(URLSpan(site), credits.indexOf(site), credits.indexOf(site) + site.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                val phone = "WhatsApp +55 11 945546072"
                setSpan(URLSpan("https://wa.me/5511945546072"), credits.indexOf(phone), credits.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
            textSize = 14f
            movementMethod = LinkMovementMethod.getInstance()
            setPadding(0, dp(24), 0, dp(16))
        })
        setContentView(ScrollView(this).apply { addView(root) })
    }

    private fun btn(p: LinearLayout, t: String, a: () -> Unit) =
        p.addView(Button(this).apply { text = t; setOnClickListener { try { a() } catch (e: Exception) { show("Não foi possível executar: ${e.localizedMessage}") } } })

    private fun dp(value: Int) = (value * resources.displayMetrics.density).toInt()

    private fun openSafely(intent: Intent) {
        try { startActivity(intent) }
        catch (e: ActivityNotFoundException) { show("Esta opção não está disponível neste dispositivo.") }
    }

    override fun onResume() {
        super.onResume()
        if (BuildConfig.DEVELOPER_EDITION) Updater.resumeInstall(this)
    }

    private fun show(s: String) { out.text = s }
    private fun copy(s: String) =
        (getSystemService(CLIPBOARD_SERVICE) as ClipboardManager).setPrimaryClip(ClipData.newPlainText("cmd", s))

    private fun diagnostic(): String {
        val mi = ActivityManager.MemoryInfo()
        (getSystemService(ACTIVITY_SERVICE) as ActivityManager).getMemoryInfo(mi)
        val st = StatFs(Environment.getDataDirectory().path)
        val free = st.availableBytes * 100 / st.totalBytes
        return """
RAM total: ${mi.totalMem / 1048576} MB
RAM livre agora: ${mi.availMem / 1048576} MB
Armazenamento livre: $free% (${st.availableBytes / 1073741824} GB)
${if (free < 20) "⚠ Pouco armazenamento livre pode afetar o desempenho. Revise arquivos e apps." else "✔ Espaço OK."}
Android ${android.os.Build.VERSION.RELEASE} (API ${android.os.Build.VERSION.SDK_INT})
""".trim()
    }

    private val risky = mapOf(
        "android.permission.SYSTEM_ALERT_WINDOW" to "sobrepor telas",
        "android.permission.READ_SMS" to "ler SMS",
        "android.permission.SEND_SMS" to "enviar SMS",
        "android.permission.REQUEST_INSTALL_PACKAGES" to "instalar apps",
        "android.permission.RECORD_AUDIO" to "microfone",
        "android.permission.READ_CONTACTS" to "contatos",
        "android.permission.BIND_ACCESSIBILITY_SERVICE" to "acessibilidade",
        "android.permission.BIND_DEVICE_ADMIN" to "administrador do dispositivo"
    )

    private fun audit(): String {
        val sb = StringBuilder()
        val adb = Settings.Global.getInt(contentResolver, Settings.Global.ADB_ENABLED, 0) == 1
        val dev = Settings.Global.getInt(contentResolver, Settings.Global.DEVELOPMENT_SETTINGS_ENABLED, 0) == 1
        sb.appendLine(if (adb) "⚠ Depuração USB LIGADA (desligue quando não usar)." else "✔ Depuração USB desligada.")
        sb.appendLine(if (dev) "ℹ Opções do desenvolvedor ativas." else "✔ Opções do desenvolvedor desativadas.")
        sb.appendLine("\nApps visíveis com origem diferente da Play Store ou permissões solicitadas sensíveis (não significa malware):\n")
        val pm = packageManager
        var n = 0
        for (p in pm.getInstalledPackages(PackageManager.GET_PERMISSIONS)) {
            val ai = p.applicationInfo ?: continue
            if (ai.flags and ApplicationInfo.FLAG_SYSTEM != 0 || p.packageName == packageName) continue
            val src = try {
                if (android.os.Build.VERSION.SDK_INT >= 30) pm.getInstallSourceInfo(p.packageName).installingPackageName
                else pm.getInstallerPackageName(p.packageName)
            } catch (e: Exception) { null }
            val perms = p.requestedPermissions?.mapNotNull { risky[it] }?.distinct().orEmpty()
            val outside = src != "com.android.vending"
            if (outside || perms.isNotEmpty()) {
                n++
                sb.appendLine("• ${pm.getApplicationLabel(ai)} (${p.packageName})")
                if (outside) sb.appendLine("   origem: ${src ?: "desconhecida / instalação manual"}")
                if (perms.isNotEmpty()) sb.appendLine("   solicita: ${perms.joinToString()}")
            }
        }
        if (n == 0) sb.appendLine("Nenhum item sinalizado nos aplicativos visíveis. Isto não garante ausência de ameaças.")
        sb.appendLine("\nDica: confira também Play Protect (Play Store > perfil > Play Protect).")
        return sb.toString()
    }

    private fun openTermux() {
        copy(termuxCmd)
        val i = packageManager.getLaunchIntentForPackage("com.termux")
        if (i != null) {
            show("Comando copiado. Cole no Termux:\n\n$termuxCmd\n\nDepois, para entrar de novo: proot-distro login ubuntu")
            openSafely(i)
        } else {
            show("Termux não instalado. Instale por um canal oficial do projeto, volte e toque de novo.")
            openSafely(Intent(Intent.ACTION_VIEW, Uri.parse("https://f-droid.org/packages/com.termux/")))
        }
    }
}
