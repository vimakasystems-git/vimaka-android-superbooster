package br.vimaka.superbooster

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.SystemClock
import android.provider.Settings
import java.net.URL
import javax.net.ssl.HttpsURLConnection
import java.util.concurrent.Executors

object NetworkCheck {
    private val worker = Executors.newSingleThreadExecutor()
    @Volatile private var busy = false
    fun run(a: Activity, output: (String) -> Unit) {
        if (busy) { output("Teste de conexão em andamento."); return }
        val cm = a.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = cm.activeNetwork ?: run { output("Sem rede ativa."); return }
        val lp = cm.getLinkProperties(network)
        val caps = cm.getNetworkCapabilities(network)
        busy = true
        output("Testando DNS e conexão HTTPS. O teste conecta a example.com e usa o DNS da rede ativa; não altera sua conexão.")
        worker.execute {
            val text = buildString {
                appendLine("REDE ATIVA")
                appendLine("Interface: ${lp?.interfaceName ?: "indisponível"}")
                appendLine("DNS configurado: ${lp?.dnsServers?.joinToString { it.hostAddress.orEmpty() } ?: "indisponível"}")
                appendLine("Gateways: ${lp?.routes?.mapNotNull { it.gateway?.hostAddress }?.distinct()?.joinToString() ?: "indisponível"}")
                appendLine("Rede medida: ${caps?.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED) == false}")
                if (android.os.Build.VERSION.SDK_INT >= 28) appendLine("DNS privado ativo: ${lp?.isPrivateDnsActive}; host: ${lp?.privateDnsServerName ?: "não configurado"}")
                appendLine("\nDNS — example.com")
                try {
                    val begin = SystemClock.elapsedRealtimeNanos()
                    val ips = network.getAllByName("example.com")
                    appendLine("Resolução: %.1f ms; ${ips.size} endereços (cache pode influenciar)".format((SystemClock.elapsedRealtimeNanos()-begin)/1_000_000.0))
                } catch (e: Exception) { appendLine("Falha DNS: ${e.localizedMessage}") }
                appendLine("\nHTTPS — example.com")
                try {
                    val c = network.openConnection(URL("https://example.com")) as HttpsURLConnection
                    c.connectTimeout = 5000; c.readTimeout = 5000; c.instanceFollowRedirects = false
                    try {
                        val begin = SystemClock.elapsedRealtimeNanos()
                        val status = c.responseCode
                        val firstByte = (SystemClock.elapsedRealtimeNanos()-begin)/1_000_000.0
                        val size = c.inputStream.use { input ->
                            val buffer = ByteArray(4096); var total = 0
                            while (total < 65536) { val n = input.read(buffer, 0, minOf(buffer.size, 65536-total)); if (n < 0) break; total += n }
                            total
                        }
                        appendLine("HTTP $status; resposta em %.1f ms; $size bytes lidos".format(firstByte))
                    } finally { c.disconnect() }
                } catch (e: Exception) { appendLine("Falha HTTPS: ${e.localizedMessage}") }
                appendLine("\nNão é teste de velocidade máxima. DNS rápido não aumenta a banda. Compare Wi-Fi, sinal e distância do roteador. DNS privado pode bloquear domínios conhecidos, mas não determina se um anúncio é ilegal nem bloqueia todo anúncio HTTPS.")
            }
            busy = false
            a.runOnUiThread { if (!a.isDestroyed && !a.isFinishing) output(text) }
        }
    }
    fun settings(a: Activity) {
        AlertDialog.Builder(a).setTitle("DNS privado e proteção por domínio")
            .setMessage("Você pode configurar DNS privado com filtro de anúncios nas configurações do Android. Exemplo de provedor externo: dns.adguard-dns.com (AdGuard DNS público). Digite esse hostname no campo Nome do host. O provedor recebe consultas DNS; veja sua política em https://adguard-dns.io/privacy.html. Este app não inspeciona tráfego, não altera o gateway e não garante bloqueio de anúncios ilegais. Um DNS filtrado pode afetar serviços; é possível voltar para Automático.")
            .setPositiveButton("Abrir DNS privado") { _, _ ->
                try { a.startActivity(Intent(if (android.os.Build.VERSION.SDK_INT >= 28) "android.settings.PRIVATE_DNS_SETTINGS" else Settings.ACTION_WIRELESS_SETTINGS)) }
                catch (_: Exception) { a.startActivity(Intent(Settings.ACTION_SETTINGS)) }
            }.setNegativeButton("Cancelar", null).show()
    }
}
