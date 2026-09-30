# Vimaka Android SuperBooster 1.2.0

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

## Novos recursos 1.2.0
- Diagnóstico pontual: RAM disponível/pressão, bateria/temperatura, estado térmico, espaço e leitura/escrita de 1 MiB. Não é um benchmark CPU/GPU nem validação no tablet.
- DNS da rede ativa, interface e gateways, DNS privado e tempos DNS/HTTPS; teste example.com após confirmação. Não mede banda máxima nem muda roteador.
- Orientação para DNS privado filtrado externo (AdGuard); app não implementa VPN/firewall nem classificação jurídica de anúncios.
- Scanner QR ZXing com foco/lanterna e importação de imagem pelo seletor Android. Leitura local com binarização adaptativa/global e inversão de cores. Não altera a câmera de outros apps.
- Developer: aplicação/restauração das três escalas de animação após concessão manual WRITE_SECURE_SETTINGS via ADB. Backup persistido antes de aplicar. Não toca clocks, limites de processos, thermal throttling, swap ou ROM.
- Compositor SMS/WhatsApp/e-mail com mensagem editável, um destinatário por vez e confirmação de opt-in. Abre rascunho no aplicativo externo; não envia, não lê contatos/SMS e não faz campanha em massa. O texto SAIR não cria serviço automático de opt-out.
- Catálogo estático de 26 nomes/URLs observados na página oficial em 30/09/2026; recursos dos serviços não foram validados. Reconsultar catálogo ao atualizar.

## Teste inicial de CI
A primeira execução falhou antes da compilação: setup-android tentou instalar o pacote obsoleto tools. Corrigido para platform-tools, platforms;android-35 e build-tools;35.0.0. Verificar resultado da nova execução.

## Privacidade operacional
QR e perfis processados localmente. Diagnóstico não é enviado pela aplicação. Teste de rede revela conexão e consulta example.com aos respectivos serviços. WhatsApp/SMS/e-mail recebem destinatário e mensagem ao abrir o rascunho; o usuário confirma o envio nesses apps. Ao configurar DNS externo, o provedor recebe as consultas DNS do dispositivo.

## Testes QR e tuning
CI inclui testes de decodificação QR normal, invertido, baixo contraste, imagem vazia e dimensões inválidas. Os testes sintéticos não substituem validação da câmera do tablet. Ajustes de animação são o único tuning automático do sistema implementado; os demais ajustes são guiados. Perfis selecionam prioridades para revisão, sem manter apps presos na RAM.

## Hardware e controle de rede
Relatório de SoC (Android 12+), placa, ABI, CPUs, frequências/governors expostos pelo kernel, tamanho lógico zRAM, modos de tela e capacidades Camera2. Sem acesso ao tablet não há confirmação de hardware, benchmark real ou tuning aplicado. Valores podem ser omitidos pelo kernel/fabricante.
Monitor de rede registra mudanças de interface, gateway e DNS enquanto a tela do app está aberta; para automaticamente ao sair. Não é firewall nem interceptação HTTPS.

## Correção de compatibilidade Android 8
Compilação da versão anterior concluída, mas lint detectou uso direto de longVersionCode (API 28). Substituído por PackageInfoCompat para minSdk 26. Validar a nova execução antes de instalar.

## CPU Root: edição Developer apenas
Módulo opcional solicita autorização ao gerenciador root já instalado no aparelho. Inspeciona políticas cpufreq, aplica governor performance somente quando suportado, salva parâmetros originais antes de escrever e oferece restauração na mesma inicialização do kernel. Recusa aplicação com estado térmico moderado ou superior ou bateria ≥42°C. Tenta rollback em falha parcial. Não faz root, overclock, tuning GPU, swap, remoção de limites térmicos nem controle dos processos de terceiros. Código de execução root não é incluído na edição Play. Este módulo precisa teste em hardware compatível; não foi executado em um tablet real. O fabricante pode sobrescrever as configurações.

A execução ae49744 passou compilação, lint e testes QR de ambas as edições. Revalidar após inclusão do módulo Root.
