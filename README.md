# Vimaka Android SuperBooster 1.2.0

**Created by Douglas Cardoso | [vimaka.com](https://vimaka.com) | [WhatsApp +55 11 945546072](https://wa.me/5511945546072)**

[Português](#português) · [English](#english) · [Español](#español)

## Português

### Instalação no celular ou tablet
O instalador Android é **`vimaka-android-superbooster.apk`**, correspondente à edição Developer de teste. Android 8.0+ (API 26) é necessário.

1. Baixe o APK e abra-o no dispositivo.
2. Se o Android solicitar, permita a instalação para o aplicativo usado para abrir o arquivo e confirme a instalação.
3. Abra o SuperBooster e execute o diagnóstico de desempenho e de hardware.
4. Use o tuning Root somente em aparelho que já tenha root, após autorização no gerenciador root.

Os APKs originais também estão na [execução validada do GitHub Actions](https://github.com/vimakasystems-git/vimaka-android-superbooster/actions/runs/36671929255): baixe o artefato **android-validation**, extraia o ZIP e encontre **app-developer-debug.apk** ou **app-play-debug.apk**. É necessário entrar no GitHub para baixar artefatos. O Developer pode ser renomeado para `vimaka-android-superbooster.apk` sem alterar sua assinatura. São APKs de teste assinados com chave debug; não são um lançamento de produção.

### Recursos
- Diagnóstico pontual de RAM, pressão de memória, armazenamento, bateria, temperatura e estado térmico; teste limitado de leitura/escrita de 1 MiB.
- Perfis locais Equilibrado, Trabalho, Jogos e Economia: seleção de apps prioritários e revisão manual das configurações dos demais.
- Relatório de CPU, governors/frequências expostos, placa, ABI, SoC quando disponível, zRAM, tela e capacidades Camera2.
- Ajustes guiados do Android. Na edição Developer, aplicação/restauração das três escalas de animação após concessão manual de WRITE_SECURE_SETTINGS por ADB.
- CPU Root opcional: aplica o governor `performance` somente onde suportado pelo kernel; salva valores originais e permite restaurar na mesma inicialização do kernel. Recusa aplicação com estado térmico moderado ou superior, ou bateria a partir de 42 °C. Tenta reverter alterações em falha parcial.
- Terminal Linux externo via Termux/PRoot e comandos para Ubuntu; não há terminal embutido.
- QR Code por câmera e imagem, com leitura local adaptativa, tentativa de contraste global e cores invertidas. Links lidos não são abertos automaticamente.
- Testes DNS/HTTPS com example.com e monitor de mudanças de interface/gateway/DNS enquanto o app está aberto.
- Orientação para DNS privado filtrado externo; não há firewall/VPN próprio ou classificação de anúncios ilegais.
- Rascunhos SMS, WhatsApp e e-mail, com mensagem editável, um destinatário por vez e confirmação de autorização. Catálogo de 26 nomes/URLs observado em 30/09/2026.

### Limites, privacidade e validação
O app não faz root, overclock, tuning GPU, criação de swap, flash de ROM ou remoção de proteção térmica. Não força outros apps a permanecerem na RAM, não limita seus processos e não altera sua inicialização. microSD é armazenamento, não RAM física. Nenhum ganho é garantido; hardware e desempenho do tablet A8 ainda não foram validados em aparelho real.

Perfis, diagnóstico e leitura QR são locais. Testes de rede consultam serviços externos; o DNS configurado recebe consultas. SMS/WhatsApp/e-mail recebem os dados do rascunho e o usuário confirma o envio no app externo. Não há leitura de contatos/SMS ou disparo automático em massa. O texto SAIR não implementa serviço de descadastro.

Compilação, lint e testes QR passaram nas duas edições no commit `5808c74`. Os testes cobrem QR normal, invertido, pouco contraste, imagem vazia e dimensões inválidas. Não substituem teste de câmera, tuning Root ou desempenho em hardware real.

A edição Play não inclui execução Root, QUERY_ALL_PACKAGES ou REQUEST_INSTALL_PACKAGES; atualiza pela loja. A edição Developer inclui o módulo Root e atualização por GitHub. Aprovação da Google Play não foi obtida: ainda é necessário revisar políticas, API alvo vigente, privacidade e Data Safety.

## English

### Installation on a phone or tablet
The Android installer is **`vimaka-android-superbooster.apk`**, the Developer test edition. Android 8.0+ (API 26) is required.

1. Download and open the APK on your device.
2. If Android asks, allow installation for the app opening the file and confirm installation.
3. Open SuperBooster and run the performance and hardware diagnostics.
4. Use Root tuning only on an already rooted device, after approval in its root manager.

The original APKs are also available in the [validated GitHub Actions run](https://github.com/vimakasystems-git/vimaka-android-superbooster/actions/runs/36671929255): download **android-validation**, extract the ZIP and locate **app-developer-debug.apk** or **app-play-debug.apk**. GitHub sign-in is required to download artifacts. Renaming the Developer APK to `vimaka-android-superbooster.apk` does not change its signature. These are debug-signed test builds, not a production release.

### Features
- Point-in-time RAM, memory pressure, storage, battery, temperature and thermal diagnostics; a bounded 1 MiB read/write check.
- Local Balanced, Work, Gaming and Economy profiles: select priority apps and manually review settings for other apps.
- Hardware report: exposed CPU governors/frequencies, board, ABI, SoC when available, zRAM, display and Camera2 capabilities.
- Guided Android settings. Developer can apply/restore three animation scales after manual ADB authorization for WRITE_SECURE_SETTINGS.
- Optional Root CPU tuning: applies the `performance` governor only when supported, saves original values and supports restoration within the same kernel boot. It refuses application at moderate or higher thermal status or battery temperature of 42 °C or higher. Attempts rollback after partial failure.
- External Linux terminal through Termux/PRoot with Ubuntu commands; no embedded terminal.
- Camera and image QR decoding, including adaptive/global binarization and inverted colors. Decoded links never open automatically.
- DNS/HTTPS tests using example.com and interface/gateway/DNS change monitoring while the app is visible.
- Guidance for an external filtered private DNS provider; no built-in firewall/VPN or legal classification of advertisements.
- Editable SMS, WhatsApp and email drafts for one recipient at a time, with opt-in confirmation. A snapshot of 26 product names/URLs was observed on September 30, 2026.

### Limits, privacy and validation
The app does not root devices, overclock, tune GPUs, create swap, flash ROMs or disable thermal protection. It cannot pin other apps in RAM, limit their processes or change their boot behavior. microSD is storage, not physical RAM. Performance gains are not guaranteed; the A8 tablet has not been tested on real hardware.

Profiles, diagnostics and QR processing stay local. Network tests contact external services; the configured DNS provider receives queries. Messaging apps receive draft data, and the user confirms sending in the external app. There is no contact/SMS reading or automatic bulk sending. The SAIR text does not implement an unsubscribe service.

Both editions passed compilation, lint and QR tests at commit `5808c74`. Tests cover standard, inverted and reduced-contrast QR codes, blank images and invalid dimensions. They do not replace camera, Root tuning or real-device performance testing.

Play excludes Root execution, QUERY_ALL_PACKAGES and REQUEST_INSTALL_PACKAGES and uses store updates. Developer includes Root tuning and GitHub updates. Google Play approval has not been obtained; current target API, policies, privacy and Data Safety still require review.

## Español

### Instalación en teléfono o tablet
El instalador Android es **`vimaka-android-superbooster.apk`**, la edición Developer de prueba. Requiere Android 8.0+ (API 26).

1. Descargue y abra el APK en el dispositivo.
2. Si Android lo solicita, permita instalar desde la aplicación que abre el archivo y confirme la instalación.
3. Abra SuperBooster y ejecute el diagnóstico de rendimiento y hardware.
4. Use el ajuste Root únicamente si el dispositivo ya tiene root y su gestor lo autoriza.

Los APK originales también están en la [ejecución validada de GitHub Actions](https://github.com/vimakasystems-git/vimaka-android-superbooster/actions/runs/36671929255): descargue **android-validation**, extraiga el ZIP y busque **app-developer-debug.apk** o **app-play-debug.apk**. Es necesario iniciar sesión en GitHub para descargar los artefactos. Puede renombrar el APK Developer a `vimaka-android-superbooster.apk` sin cambiar su firma. Son compilaciones de prueba firmadas con clave debug, no una versión de producción.

### Funciones
- Diagnóstico puntual de RAM, presión de memoria, almacenamiento, batería, temperatura y estado térmico; prueba limitada de lectura/escritura de 1 MiB.
- Perfiles locales Equilibrado, Trabajo, Juegos y Economía: selección de aplicaciones prioritarias y revisión manual de las demás.
- Informe de CPU, governors/frecuencias expuestos, placa, ABI, SoC cuando esté disponible, zRAM, pantalla y capacidades Camera2.
- Ajustes guiados de Android. Developer permite aplicar/restaurar tres escalas de animación después de autorizar WRITE_SECURE_SETTINGS manualmente mediante ADB.
- Ajuste opcional de CPU con Root: aplica el governor `performance` solo si el kernel lo admite, guarda valores originales y permite restaurarlos durante el mismo arranque del kernel. Rechaza la aplicación con estado térmico moderado o superior, o batería a partir de 42 °C. Intenta revertir cambios tras un fallo parcial.
- Terminal Linux externo mediante Termux/PRoot y comandos para Ubuntu; no incluye terminal integrado.
- Lectura QR por cámara e imagen, con binarización adaptativa/global y colores invertidos. Los enlaces leídos no se abren automáticamente.
- Pruebas DNS/HTTPS con example.com y monitor de cambios de interfaz/gateway/DNS mientras la aplicación está visible.
- Orientación para DNS privado externo con filtrado; no incluye firewall/VPN propio ni clasificación legal de anuncios.
- Borradores SMS, WhatsApp y correo editables, un destinatario a la vez y confirmación de autorización. Catálogo de 26 nombres/URLs observado el 30/09/2026.

### Límites, privacidad y validación
La aplicación no instala root, hace overclock, ajusta GPU, crea swap, instala ROM ni desactiva protecciones térmicas. No mantiene otras aplicaciones forzadamente en RAM, limita sus procesos ni cambia su inicio. microSD es almacenamiento, no RAM física. No garantiza mejoras; la tablet A8 no ha sido validada en hardware real.

Perfiles, diagnóstico y lectura QR se procesan localmente. Las pruebas de red consultan servicios externos y el proveedor DNS recibe consultas. Las aplicaciones de mensajería reciben el borrador y el usuario confirma el envío. No se leen contactos/SMS ni se realizan envíos automáticos masivos. El texto SAIR no implementa un servicio de baja.

Ambas ediciones pasaron compilación, lint y pruebas QR en el commit `5808c74`. Las pruebas cubren QR normal, invertido, con bajo contraste, imagen vacía y dimensiones inválidas. No sustituyen pruebas reales de cámara, Root o rendimiento.

Play no incluye ejecución Root, QUERY_ALL_PACKAGES ni REQUEST_INSTALL_PACKAGES y usa actualizaciones de la tienda. Developer incluye Root y actualizaciones de GitHub. No se obtuvo aprobación de Google Play: faltan revisar API objetivo vigente, políticas, privacidad y Data Safety.

## Build / Compilação / Compilación

JDK 17 · Gradle 8.9 · Android SDK 35 · minSdk 26 · targetSdk 35

```sh
gradle assemblePlayDebug assembleDeveloperDebug lintPlayDebug lintDeveloperDebug testPlayDebugUnitTest testDeveloperDebugUnitTest
gradle bundlePlayRelease assembleDeveloperRelease
```

Release signing / Assinatura / Firma: `KEYSTORE_FILE`, `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD`. Without a keystore, release is unsigned / Sem keystore, release não é assinado / Sin keystore, release no está firmado. Tags `v*` generate a Developer APK and a Play AAB through the release workflow.

## References / Referências / Referencias

- [Android process restrictions](https://developer.android.com/about/versions/14/behavior-changes-all)
- [Package visibility policy](https://support.google.com/googleplay/android-developer/answer/10158779)
- [APK installation permission policy](https://support.google.com/googleplay/android-developer/answer/12085295)
- [Android memory management](https://source.android.com/docs/core/perf/mmd)
- [Termux / PRoot](https://github.com/termux/proot-distro)
- [Vimaka ecosystem](https://vimakasistemas.com.br/ecosistema)

MIT license: see [LICENSE](LICENSE).
