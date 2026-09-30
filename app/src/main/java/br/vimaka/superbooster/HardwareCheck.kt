package br.vimaka.superbooster

import android.app.Activity
import android.app.admin.DevicePolicyManager
import android.content.Context
import android.hardware.SensorManager
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.hardware.display.DisplayManager
import android.os.Build
import java.io.File
import java.util.concurrent.Executors

object HardwareCheck {
    private val worker = Executors.newSingleThreadExecutor()
    private fun read(path: String): String = try { File(path).readText().trim().take(500) } catch (_: Exception) { "não exposto pelo kernel" }
    fun run(a: Activity, output: (String) -> Unit) {
        output("Identificando hardware e parâmetros expostos. Nenhum clock ou limite térmico será alterado.")
        worker.execute {
            val report = try {
                buildString {
                    appendLine("HARDWARE / ANDROID — ${Build.MANUFACTURER} ${Build.MODEL}")
                    appendLine("Produto: ${Build.PRODUCT}; device: ${Build.DEVICE}; placa: ${Build.BOARD}")
                    if (Build.VERSION.SDK_INT >= 31) appendLine("SoC informado pelo fabricante: ${Build.SOC_MANUFACTURER} / ${Build.SOC_MODEL}")
                    appendLine("ABIs: ${Build.SUPPORTED_ABIS.joinToString()}; CPUs disponíveis ao processo: ${Runtime.getRuntime().availableProcessors()}")
                    appendLine("Kernel: ${System.getProperty("os.version")}")
                    appendLine("Tipo de build: ${Build.TYPE}; patch: ${Build.VERSION.SECURITY_PATCH}")
                    val policy = a.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
                    appendLine("Este app é device owner: ${policy.isDeviceOwnerApp(a.packageName)}")
                    appendLine("Root: não solicitado nem testado")
                    appendLine("\nCPU / governor — valores informados pelo kernel")
                    val policies = File("/sys/devices/system/cpu/cpufreq").listFiles()?.filter { it.name.startsWith("policy") }.orEmpty()
                    if (policies.isEmpty()) appendLine("Políticas CPU indisponíveis ao app; usar relatório ADB.")
                    policies.sortedBy { it.name }.forEach { p ->
                        appendLine("${p.name}: governor=${read("${p.path}/scaling_governor")}")
                        appendLine("Frequências mín/máx/atual (kHz): ${read("${p.path}/scaling_min_freq")} / ${read("${p.path}/scaling_max_freq")} / ${read("${p.path}/scaling_cur_freq")}")
                        appendLine("Governors disponíveis: ${read("${p.path}/scaling_available_governors")}")
                    }
                    appendLine("\nzRAM: tamanho lógico informado: ${read("/sys/block/zram0/disksize")} bytes; não representa RAM física adicional.")
                    appendLine("\nTELA — modos suportados informados pelo Android")
                    val displays = a.getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
                    displays.displays.forEach { display ->
                        appendLine("${display.name}: ${display.supportedModes.joinToString { "${it.physicalWidth}×${it.physicalHeight}@${it.refreshRate}Hz" }}")
                    }
                    appendLine("\nCÂMERAS — capacidades informadas; não medem qualidade real")
                    val cameras = a.getSystemService(Context.CAMERA_SERVICE) as CameraManager
                    cameras.cameraIdList.forEach { id ->
                        try {
                            val c = cameras.getCameraCharacteristics(id)
                            appendLine("Câmera $id: nível Camera2=${c.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL)}, flash=${c.get(CameraCharacteristics.FLASH_INFO_AVAILABLE)}")
                            appendLine("Foco: ${c.get(CameraCharacteristics.CONTROL_AF_AVAILABLE_MODES)?.joinToString()}; zoom digital máx=${c.get(CameraCharacteristics.SCALER_AVAILABLE_MAX_DIGITAL_ZOOM)}")
                        } catch (_: Exception) { appendLine("Câmera $id: características indisponíveis") }
                    }
                    val sensors = a.getSystemService(Context.SENSOR_SERVICE) as SensorManager
                    appendLine("\nSensores expostos: ${sensors.getSensorList(android.hardware.Sensor.TYPE_ALL).size}")
                    appendLine("\nTUNING POSSÍVEL NESTA VERSÃO")
                    appendLine("Animações reversíveis via Developer/ADB; demais ajustes via configurações do Android. Não altera governor, CPU/GPU, zRAM, processos, firmware ou tela por APIs privilegiadas.")
                    appendLine("Para planejar tuning de hardware: conferir este relatório, suporte root/ADB, temperaturas e parâmetros originais. Frequência máxima constante pode aquecer e causar throttling, reduzindo desempenho sustentado.")
                }
            } catch (e: Exception) { "Falha na leitura de hardware: ${e.localizedMessage}" }
            a.runOnUiThread { if (!a.isDestroyed && !a.isFinishing) output(report) }
        }
    }
}
