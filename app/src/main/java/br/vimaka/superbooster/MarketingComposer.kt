package br.vimaka.superbooster

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.text.InputType
import android.widget.*

object MarketingComposer {
    // Snapshot do catálogo público consultado em 30/09/2026; não implica validação dos serviços.
    private val products = linkedMapOf(
        "Vimaka Sistemas Inteligentes" to "https://vimakasistemas.com.br/",
        "Vimaka Odontologic" to "https://vimakaodontologic.com.br/",
        "Vimaka Health Suite" to "https://vimaka-heath-suite.base44.app/",
        "Vimaka Mental Care" to "https://vimaka-mental-care.base44.app/",
        "Aqui Resolve" to "https://aquiresolveagora.com.br/",
        "Vimaka Car Commerce Solutions" to "https://jv-car-sales.base44.app/",
        "ParkinGold" to "https://vimakaparkingold.com.br/",
        "Vimaka Social Manager" to "https://vimaka-social-manager.base44.app/",
        "Vimaka Social Intelligence" to "https://vimaka-social-intelligence.base44.app/",
        "Talentia Jobs" to "https://talentiajobs.com/",
        "Bydodoo" to "https://bydodoo.com/",
        "Meu Trabalho Escolar" to "https://meutrabalhoescolar.base44.app/",
        "Vimaka AI Academy" to "https://vimaka-ai-academy.base44.app/",
        "ConsultaFácil IA" to "https://consultafacil-ia.base44.app/",
        "ProdPipes" to "https://prodpipes.com/",
        "Vimaka Nexus Core" to "https://vimaka-nexus-core.base44.app/",
        "Vimaka MyFinanceGenius" to "https://myfinancegenius.base44.app/",
        "DevPulse" to "https://devpulse-app.base44.app/",
        "Vimaka AI Agents" to "https://vimaka-ai-agents.base44.app/",
        "Super Kate" to "https://super-kate.com/",
        "CerebroBrasil" to "https://cerebrobrasil.com.br/",
        "exploraSAMPA" to "https://exploresampa.com/",
        "Vimaka Live Flow" to "https://vimaka-live-flow.base44.app/",
        "Vimaka People Finance" to "https://vimaka-people-finance.base44.app/",
        "CNPJ Check" to "https://cnpjcheck.com/",
        "Angel DevToolbox" to "https://angel-devtoolbox.base44.app/"
    )
    fun open(a: Activity) {
        val root = LinearLayout(a).apply { orientation = LinearLayout.VERTICAL; setPadding(24, 12, 24, 12) }
        val choices = products.keys.toList()
        val product = Spinner(a).apply { adapter = ArrayAdapter(a, android.R.layout.simple_spinner_dropdown_item, choices) }
        val recipient = EditText(a).apply { hint = "Destinatário: telefone com DDI ou e-mail"; inputType = InputType.TYPE_CLASS_TEXT }
        val message = EditText(a).apply { hint = "Mensagem editável"; minLines = 4; inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE }
        val consent = CheckBox(a).apply { text = "Confirmo que este destinatário autorizou receber esta divulgação." }
        root.addView(TextView(a).apply { text = "Um destinatário por vez. Revise no app de envio. SMS pode ter custo. Catálogo consultado em 30/09/2026." })
        root.addView(product); root.addView(recipient); root.addView(message); root.addView(consent)
        fun template(i: Int) = "Olá! Conheça ${choices[i]}, uma solução da Vimaka Sistemas Inteligentes: ${products[choices[i]]}\n\nMais soluções: https://vimakasistemas.com.br/ecosistema\nSe não desejar receber novas mensagens, responda SAIR."
        message.setText(template(0))
        product.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long) { message.setText(template(position)) }
            override fun onNothingSelected(parent: AdapterView<*>?) { }
        }
        val dialog = AlertDialog.Builder(a).setTitle("Divulgação Vimaka — revisar e enviar")
            .setView(ScrollView(a).apply { addView(root) })
            .setPositiveButton("Escolher canal", null).setNegativeButton("Fechar", null).create()
        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val destination = recipient.text.toString().trim()
                val body = message.text.toString().trim()
                if (!consent.isChecked || destination.isEmpty() || body.isEmpty()) {
                    Toast.makeText(a, "Informe destinatário, mensagem e autorização", Toast.LENGTH_LONG).show(); return@setOnClickListener
                }
                AlertDialog.Builder(a).setTitle("Abrir rascunho; envio será confirmado por você")
                    .setItems(arrayOf("SMS", "WhatsApp", "E-mail")) { _, index ->
                        try {
                            val phone = destination.removePrefix("+")
                            if (index != 2 && !phone.matches(Regex("[0-9]{8,15}"))) { Toast.makeText(a, "Use telefone com DDI, somente números", Toast.LENGTH_LONG).show(); return@setItems }
                            if (index == 2 && !android.util.Patterns.EMAIL_ADDRESS.matcher(destination).matches()) { Toast.makeText(a, "E-mail inválido", Toast.LENGTH_LONG).show(); return@setItems }
                            val intent = when (index) {
                                0 -> Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:$phone")).putExtra("sms_body", body)
                                1 -> Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/$phone?text=${Uri.encode(body)}"))
                                else -> Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:${Uri.encode(destination)}?subject=${Uri.encode("Conheça as soluções Vimaka")}&body=${Uri.encode(body)}"))
                            }
                            a.startActivity(intent)
                        } catch (_: Exception) { Toast.makeText(a, "Nenhum aplicativo disponível para este canal", Toast.LENGTH_LONG).show() }
                    }.setNegativeButton("Cancelar", null).show()
            }
        }
        dialog.show()
    }
}
