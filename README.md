# Vimaka Android SuperBooster 1.1.0

Created by Douglas Cardoso | https://vimaka.com | WhatsApp +55 11 945546072

Diagnóstico e ajustes guiados para celulares/tablets Android 8+. Não promete aumento de RAM física, acesso root ou ganho garantido de desempenho.

## Recursos implementados
- RAM e armazenamento; inventário restrito aos apps visíveis.
- Perfis Equilibrado, Trabalho, Jogos e Economia salvos localmente. Seleção de apps prioritários e revisão manual dos demais via Configurações; não altera inicialização nem encerra processos.
- Atalhos para armazenamento, bateria, apps, acessibilidade e opções de desenvolvedor.
- Terminal Linux externo: abre Termux e copia comandos para Ubuntu via PRoot; não é terminal embutido.
- Informação de RAM física e volumes secundários; explicação de zRAM e limitações de swap microSD. Não cria swap.
- Identificação de fabricante, modelo, placa, produto, ABI, Android e patch. Atualização oficial e links para fontes abertas; não busca nem instala uma ROM automaticamente.
- Logos Vimaka e créditos com links.

## Distribuições
`play`: sem QUERY_ALL_PACKAGES, sem REQUEST_INSTALL_PACKAGES; atualização pela Play Store. Integração externa com terminal precisa revisão de política e jornadas antes da submissão.
`developer`: atualização por GitHub com confirmação e comandos ADB copiados; não inclui agente ADB, Shizuku ou root. Controle privilegiado não implementado.

## Compilar
JDK 17, Gradle 8.9, Android SDK 35.
```sh
gradle assemblePlayDebug assembleDeveloperDebug lintPlayDebug lintDeveloperDebug
gradle bundlePlayRelease assembleDeveloperRelease
```
Para release, configure KEYSTORE_FILE, KEYSTORE_PASSWORD, KEY_ALIAS e KEY_PASSWORD. Sem keystore, release não é assinado. Workflow de tags v* gera APK Developer e AAB Play.

## Google Play: pendências
- Confirmar target API exigido no Play Console na data de envio; base atual API 35.
- Compilação, lint, testes reais e revisão de SDKs/políticas ainda não executados. Não há certificação ou garantia de aprovação.
- Publicar política de privacidade e preencher Data Safety de acordo com o comportamento final. Perfis não são enviados a servidor; links externos usam navegador e GitHub Developer recebe consultas de atualização.
- Não usar acessibilidade para burlar permissões, matar apps ou modificar sistema.

## Tablet A8 / ASIN B0GVNKP3JF
Anúncio não pôde ser consultado. Modelo comercial não identifica firmware. Coletar a tela Android/dispositivo e conferir SoC, revisão de placa, Treble, bootloader e imagem de recuperação do fabricante antes de avaliar ROM. Firmware permanece bloqueado para instalação automática.

## Limites
Google Play e app comum não oferecem controle total do dispositivo. Swap requer kernel/filesystem/privilégios compatíveis e pode piorar latência/desgastar cartão. Boot e RAM são geridos pelo Android. ADB/root/device owner são projetos adicionais, não recursos ativos desta versão.

## Fontes
https://developer.android.com/about/versions/14/behavior-changes-all
https://support.google.com/googleplay/android-developer/answer/10158779
https://support.google.com/googleplay/android-developer/answer/12085295
https://source.android.com/docs/core/perf/mmd
https://github.com/termux/proot-distro
