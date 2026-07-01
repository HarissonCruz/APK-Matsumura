package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

data class PasswordRule(
    val id: String,
    val text: String,
    val isValid: (password: String, email: String) -> Boolean
)

val passwordRulesList = listOf(
    PasswordRule("min_length", "Deve ter pelo menos 8 caracteres") { p, _ -> p.length >= 8 },
    PasswordRule("max_length", "Deve ter no máximo 30 caracteres") { p, _ -> p.length <= 30 },
    PasswordRule("uppercase", "Deve incluir uma letra maiúscula") { p, _ -> p.any { it.isUpperCase() } },
    PasswordRule("lowercase", "Deve incluir uma letra minúscula") { p, _ -> p.any { it.isLowerCase() } },
    PasswordRule("number", "Deve incluir números") { p, _ -> p.any { it.isDigit() } },
    PasswordRule("special", "Deve incluir um caractere especial (ex: @, #, $, %)") { p, _ -> p.any { !it.isLetterOrDigit() && !it.isWhitespace() } },
    PasswordRule("no_email", "Não pode ser igual ao e-mail") { p, email -> p.trim().lowercase() != email.trim().lowercase() },
    PasswordRule("no_username", "Não pode ser igual ao nome de usuário do e-mail (antes do @)") { p, email ->
        val username = email.substringBefore("@").trim().lowercase()
        username.isNotBlank() && p.trim().lowercase() != username
    }
)

fun validateAllRules(password: String, email: String): Boolean {
    return passwordRulesList.all { it.isValid(password, email) }
}

@Composable
fun PasswordRequirementsList(
    password: String,
    email: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Requisitos de Senha",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            passwordRulesList.forEach { rule ->
                val isValid = rule.isValid(password, email)
                val color = if (isValid) Color(0xFF10B981) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                val icon = if (isValid) Icons.Default.CheckCircle else Icons.Outlined.Circle

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = if (isValid) "Válido" else "Pendente",
                        tint = color,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = rule.text,
                        fontSize = 11.sp,
                        color = color,
                        fontWeight = if (isValid) FontWeight.SemiBold else FontWeight.Normal
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LegalDocumentDialog(
    title: String,
    content: String,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = {
                            Text(
                                text = title,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        },
                        actions = {
                            IconButton(onClick = onDismiss) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Fechar"
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    )
                },
                modifier = Modifier.fillMaxSize()
            ) { paddingValues ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(horizontal = 20.dp, vertical = 8.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = content,
                        fontSize = 13.sp,
                        lineHeight = 20.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(bottom = 24.dp)
                    )
                }
            }
        }
    }
}

val DIST_POLICY_TEXT = """
## Contrato de Distribuição do Desenvolvedor Certificado Matsumura Connect
Última atualização: 10/01/2026

O Matsumura Connect é um software desenvolvido pela Matsumura S.A. ("Matsumura"), uma empresa com ID 509987184 e sede principal na Rua Soeiro Pereira Gomes, Lote 1, 3D, 1649-031 Lisboa, e sede social na Rua Fernanda Seno n. 6, 7005-484, Évora, Portugal, que permite a distribuição de produtos na Matsumura App Store, Matsumura App Store iOS e outros canais de distribuição de Apps. O Matsumura Connect é um produto desenvolvido e de propriedade da Matsumura.

Ao fazer o upload ou disponibilizar de outra forma aplicativos ou quaisquer outros materiais sob este Contrato, você (em seu próprio nome ou em nome da empresa que representa) concorda em cumprir os termos deste Contrato. Conforme usado neste Contrato, "nós" significa a Matsumura ou qualquer uma de suas afiliadas, e "você" e "Desenvolvedor Certificado" significam o solicitante (se estiver se registrando como pessoa física) ou a empresa que emprega o solicitante (se estiver se registrando como pessoa jurídica). Os termos em letras maiúsculas têm os significados listados nas Definições abaixo.

## Definições

* Apps: Softwares, conteúdos e materiais digitais desenvolvidos ou distribuídos pelo Desenvolvedor Certificado distribuídos via Matsumura Connect, incluindo os respectivos metadados e os conteúdos digitais ou bens digitais que os Usuários podem comprar dentro de tais Apps.
* Matsumura App Store: Software desenvolvido e fornecido pela Matsumura que pode ser acessado por Dispositivos e dá acesso aos Apps, seja através do site da Matsumura ou do App Matsumura.
* Matsumura App Store iOS: A Matsumura App Store desenvolvida e fornecida pela Matsumura para a iOS Store.
* Parceiro Matsumura: Qualquer pessoa ou empresa registrada e aprovada pela Matsumura para ter acesso ao conteúdo do Matsumura Connect e distribuir Apps na App Store do Parceiro.
* Matsumura Wallet: Sistema de pagamento próprio da Matsumura usado para processar pagamentos de usuários finais na Matsumura App Store e Lojas Parceiras.
* Créditos AppCoins: Uma moeda virtual usada para fazer compras em aplicativos nos seus Apps.
* Matsumura Connect: O software desenvolvido pela Matsumura que permite aos Desenvolvedores Certificados distribuir e monetizar seus Apps na Matsumura App Store e Lojas Parceiras.
* Desenvolvedor Certificado: O desenvolvedor ou distribuidor de Apps que aderiu ao Programa de Desenvolvedores Matsumura, através do qual seus Apps são certificados pela Matsumura, tendo acesso a ferramentas avançadas para a distribuição de Apps e faturamento no aplicativo.
* Conta de Desenvolvedor Certificado: Uma conta emitida para Desenvolvedores Certificados que permite a distribuição de Apps no Matsumura Connect.
* Dispositivo: Qualquer dispositivo que possa acessar a Matsumura App Store, a Matsumura App Store iOS ou Lojas Parceiras, conforme definido aqui.
* Produtos In-App: Itens e/ou serviços que os Usuários podem comprar nos Apps.
* Territórios MoR: Os países listados no Anexo I deste Contrato. A Matsumura reserva-se o direito de adicionar unilateralmente países adicionais ao Anexo I deste Contrato sem a necessidade de uma alteração formal a este Contrato. A lista atualizada dos Territórios MoR é disponibilizada ao Desenvolvedor na Conta de Desenvolvedor Certificado.
* Lojas Parceiras: App Store do Parceiro Matsumura para a distribuição de Apps.
* Serviços: Os serviços prestados pela Matsumura em relação à distribuição de Apps via Matsumura Connect.
* Territórios de Impostos sobre Transações: Os países listados no Anexo II deste Contrato. A Matsumura reserva-se o direito de adicionar unilateralmente países adicionais ao Anexo II deste Contrato sem a necessidade de uma alteração formal a este Contrato. A lista atualizada dos Territórios de Impostos sobre Transações é disponibilizada ao Desenvolvedor na Conta de Desenvolvedor Certificado.
* Impostos sobre Transações: Impostos que incidem sobre o preço de compra de mercadorias, tais como impostos sobre o valor acrescentado (IVA) e impostos retidos na fonte.
* Usuários: Os proprietários de dispositivos que acessam a Matsumura App Store ou as Lojas Parceiras Matsumura em seus dispositivos, que baixam e usam os Apps.
* Conta Wallet: Uma carteira digital que suporta o saldo do Desenvolvedor Certificado. Os Desenvolvedores Certificados devem manter sua Conta Wallet operacional.

## 1. Introdução

1. A Matsumura desenvolveu o Programa de Desenvolvedores Certificados, que consiste na certificação dos Apps do Desenvolvedor. Apenas Apps certificados podem ser distribuídos via Matsumura Connect.
2. O acesso ao Matsumura Connect é limitado a usuários profissionais que pretendem disponibilizar seus Apps não apenas na Matsumura App Store, mas também nas Lojas Parceiras Matsumura.
3. O Matsumura Connect é um SaaS (Software as a Service) desenvolvido para profissionais que permite a distribuição de Apps. Ao registar-se no Matsumura Connect, ou ao iniciar sessão pela primeira vez no Matsumura Connect com as suas credenciais, uma Conta Wallet será associada à sua Conta de Desenvolvedor Certificado. Todas as suas transações feitas através do Matsumura Connect serão gerenciadas por tal Conta Wallet.
4. Se os seus Apps forem pagos ou incluírem Produtos In-App pagos, você deve integrar o faturamento do Matsumura Connect em seus Apps. Para tanto, você deverá fornecer à Matsumura dados específicos sobre seus Apps através do Console Matsumura Connect (tais como, mas não limitados a, nome do pacote do App fornecido ao Google Play, o preço dos Apps ou compras no aplicativo). Você se compromete a disponibilizar à Matsumura, ou a fornecer à Matsumura as autorizações necessárias para obter, todas as informações necessárias para esse efeito e reconhece que a Matsumura não pode ser responsabilizada por quaisquer atrasos, danos ou perdas relacionados com a falta de disponibilidade de tais informações.

## 2. Aceitação deste Contrato

1. Este contrato ("Contrato") constitui um contrato juridicamente vinculativo entre você e a Matsumura em relação à distribuição de Apps via Matsumura Connect. Para distribuir Apps via Matsumura Connect, você deve primeiro concordar com este Contrato aceitando-o online (através do botão "Eu aceito"). Você não pode distribuir Apps no Matsumura Connect se não aceitar este Contrato.
2. O uso do Matsumura Connect e dos Serviços é limitado a partes que legalmente possam celebrar e formar contratos sob a lei aplicável.
3. Você declara e garante que: (a) se você for uma empresa, você está devidamente organizada, validamente existente e em boa situação sob as leis do país em que sua empresa está registrada; e (b) você tem todos os direitos, poderes e autoridade necessários para celebrar este Contrato e cumprir suas obrigações aqui estabelecidas.

## 3. Distribuição de Apps

1. Você pode distribuir Apps através do Matsumura Connect, enviando-os para sua Conta de Desenvolvedor Certificado.
2. Ao assinar este Contrato, você concorda que, ao enviar Apps para o Matsumura Connect, eles estarão automaticamente disponíveis na Matsumura App Store e, se você escolher, na Matsumura App Store iOS e em todas as Lojas Parceiras Matsumura que têm acesso ao Matsumura Connect. Você pode escolher em quais Lojas Parceiras Matsumura específicas seus Apps serão distribuídos, alterando as configurações de distribuição em sua Conta de Desenvolvedor Certificado.
3. O Desenvolvedor Certificado entende e concorda que as Lojas Parceiras Matsumura podem decidir parar de distribuir os Apps em suas App Stores a qualquer momento, a seu exclusivo critério. A suspensão ou término da distribuição dos Apps por uma determinada Loja Parceira Matsumura não afetará a validade e eficácia deste Contrato, sob o qual o Desenvolvedor Certificado poderá continuar a distribuir seus Apps em outras App Stores.
4. Através de sua Conta de Desenvolvedor Certificado, você pode distribuir Apps de sua propriedade e/ou Apps em relação aos quais recebeu uma licença de distribuição pelo respectivo proprietário. Você declara e garante que possui todos os direitos de propriedade intelectual, incluindo todas as patentes, marcas comerciais, segredos comerciais, direitos autorais ou outros direitos de propriedade necessários nos e para os Apps. Se você usar materiais de terceiros, você declara e garante que tem o direito de distribuir o material de terceiros no App. Você concorda que não enviará material para o Matsumura Connect que seja protegido por direitos autorais, protegido por segredo comercial ou de outra forma sujeito a direitos de propriedade de terceiros, incluindo direitos de patente, privacidade e publicidade, a menos que você seja o proprietário de tais direitos ou tenha permissão de seu legítimo proprietário.
""".trimIndent()

val TERMS_CONDITIONS_TEXT = """
Termos e Condições do Matsumura Connect
Última atualização: 10/01/2026

O Matsumura Connect é um software desenvolvido pela Matsumura S.A. ("Matsumura"), uma empresa com ID 509987184 e sede principal na Rua Soeiro Pereira Gomes, Lote 1, 3D, 1649-031 Lisboa, e sede social na Rua Fernanda Seno n. 6, 7005-484, Évora, Portugal, que permite a distribuição de produtos na Matsumura App Store e outros canais de distribuição de Apps.

Conforme usado nestes Termos e Condições, "nós" significa a Matsumura, e "você" e "usuário" significam a pessoa que acessa o Matsumura Connect e usa os serviços prestados pela Matsumura para distribuir Apps.

Antes de usar o Matsumura Connect, leia estes Termos e Condições, juntamente com quaisquer outras políticas ou avisos disponibilizados pelo Matsumura Connect e nossa Política de Privacidade (coletivamente referidos como os "Termos").

Se você não concordar com os Termos, não deverá usar, ou deverá interromper o uso do Matsumura Connect.

1. Geral
O Matsumura Connect é um SaaS (Software as a Service) desenvolvido para profissionais que permite a distribuição de Apps.

Para ter acesso aos serviços do Matsumura Connect, você deve primeiro celebrar um contrato de distribuição ou parceria com a Matsumura, o qual detalhará os termos e condições aplicáveis aos serviços do Matsumura Connect que serão prestados a você (o "Contrato").

Ao usar o Matsumura Connect, você reconhece que leu, compreendeu e concordou com os Termos.

Ao concordar com estes Termos, o usuário declara que, ao acessar os Serviços, possui a idade legal e/ou capacidade jurídica para, de acordo com a lei, fornecer consentimento e estar contratualmente vinculado na execução e prestação os Serviços.

Você concorda em usar o Matsumura Connect apenas para os fins permitidos pelos Termos e pelo Contrato.

Você concorda que não violará nenhuma lei ao usar o Matsumura Connect. Isso inclui quaisquer leis locais, provinciais, estaduais, federais, nacionais ou internacionais que possam se aplicar a você.

Você concorda que não se envolverá em nenhuma atividade que interfere ou interrompa o Matsumura Connect (ou os servidores e redes que estão conectados ao protocolo).

Você concorda que é o único responsável (e que a Matsumura não tem nenhuma responsabilidade com você ou com terceiros) por qualquer violação de suas obrigações sob os Termos e pelas consequências (incluindo any perda ou dano) de tal violação.

2. Serviços
O Matsumura Connect é um software desenvolvido para profissionais para permitir a distribuição de Apps em diferentes canais de distribuição.

Ao se registrar no Matsumura Connect, ou ao fazer o login pela primeira vez no Matsumura Connect com suas credenciais, uma Wallet (Carteira) será automaticamente adicionada à sua conta (Conta Wallet). Todas as transações feitas através do Matsumura Connect serão gerenciadas por tal Conta Wallet.

O Matsumura Connect permitirá que você:
* Distribua seus Apps na Matsumura App Store e em outras Lojas de Aplicativos Parceiras.
* Venda Produtos In-App para usuários finais e receba uma participação na receita.

3. Taxa do Matsumura Connect
Em contrapartida pelo uso os Serviços, você se compromete a pagar à Matsumura uma taxa anual. O valor da taxa anual é determinado pela Matsumura e está disponível em sua conta. A taxa do Matsumura Connect é reembolsável nos primeiros 30 dias e antes que qualquer um de seus Apps comece a ser distribuído no Matsumura Connect.

A taxa anual é devida quando você assina os Serviços do Matsumura Connect pela primeira vez e será devida em cada ano subsequente na mesma data. O pagamento da taxa anual pode ser realizado através de sua Conta de Desenvolvedor Certificado usando seu cartão de crédito, conta do PayPal ou outros métodos de pagamento disponíveis.

Ao pagar a taxa anual, você terá acesso imediato aos Serviços.

4. Conteúdo Restrito
Os Apps e outros materiais disponibilizados durante o uso dos Serviços ("Conteúdo") devem cumprir o seguinte:

* Conteúdo Sexual: O Conteúdo não pode conter ou promover conteúdo sexual ou profanidade, incluindo pornografia, ou quaisquer conteúdos ou serviços destinados à gratificação sexual. Conteúdos que contenham nudez podem ser permitidos se o objetivo principal for educacional, documental, científico ou artístico, e não for gratuito.
* Exploração Infantil: Não é permitido Conteúdo que sexualize menores, incluindo, mas não se limitando a, Conteúdo que promova a pedofilia ou interação inadequada direcionada a um menor (ex: apalpar ou acariciar).
* Discurso de Ódio: O Conteúdo não pode promover a violência ou incitar o ódio contra indivíduos ou grupos com base em raça ou origem étnica, religião, deficiência, idade, nacionalidade, status de veterano, orientação sexual, gênero, identidade de gênero ou qualquer outra característica associada à discriminação sistêmica ou marginalização.
* Violência: O Conteúdo não pode retratar ou facilitar a violência gratuita ou outras atividades perigosas, nem estar relacionado ao terrorismo, como conteúdos que promovam atos terroristas, incitem a violência ou celebrem ataques terroristas. Conteúdos que retratam violência fictícia no contexto de um jogo, como desenhos animados, caça ou pesca, são geralmente permitidos.
* Produtos Perigosos: O Conteúdo não pode facilitar a venda de explosivos, armas de fogo, munições ou certos acessórios para armas de fogo, nem instruções para a fabricação de explosivos, armas de fogo, munições, acessórios restritos para armas de fogo ou outras armas.
* Álcool, tabaco e drogas: O Conteúdo não pode facilitar a venda de álcool, tabaco ou drogas, nem incentivar o uso ilegal ou inadequado de álcool ou tabaco.
* Serviços Financeiros: O Conteúdo não pode expor os usuários a produtos e serviços financeiros enganosos ou prejudiciais.
* Jogos de Azar: Conteúdos e serviços que facilitam jogos de azar online podem ter a distribuição permitida em determinados locais e se cumprirem certos requisitos, que são analisados caso a caso e sujeitos às leis aplicáveis e outros critérios.
* Atividades Ilícitas: O Conteúdo não pode promover ou facilitar atividades ilegais.
* Propriedade Intelectual: O Conteúdo não pode violar direitos de propriedade intelectual, incluindo patente, direitos autorais, marca registrada, segredo comercial ou outro direito de propriedade de qualquer parte, nem encorajar ou induzir a violação de direitos de propriedade intelectual.
* Privacidade: O Conteúdo deve cumprir todas as leis locais aplicáveis, incluindo o Regulamento Geral de Proteção de Dados da União Europeia (GDPR).

Você também se compromete a não fazer o upload ou disponibilizar de outra forma Apps ou quaisquer outros materiais que:
* Contenham vírus, trojans, spyware, adware, ransomware, rootkits, back doors, worms e qualquer outro malware ou mecanismo ou dispositivo semelhante, ou qualquer outro código projetado ou destinado a ter, ou destinado a ser capaz de executar, qualquer uma das seguintes funções: (a) interromper, desativar, danificar ou impedir de qualquer forma o acesso ou a operação de, ou fornecer acesso não autorizado a, um system de computador ou rede ou outro dispositivo no qual tal código esteja armazenado ou instalado; ou (b) acessar, copiar, bloquear, criptografar, danificar ou destruir quaisquer dados ou arquivos, em cada caso, sem o consentimento do usuário.
* Danifiquem os dispositivos dos usuários ou dados pessoais.
* Crie um uso de rede imprevisível que cause um impacto adverso nas tarifas de serviço do usuário ou na rede de uma Operadora Autorizada.
* Violem conscientemente os termos de serviço de uma Operadora Autorizada para o uso permitido.
* Crie uma experiência de usuário de spam, seja postando conteúdo repetitivo ou informações enganosas sobre a finalidade de um App.

Caso a Matsumura detecte um Conteúdo que não esteja em conformidade com estes Termos, a Matsumura poderá, sem prejuízo dos direitos da Matsumura de rescindir qualquer contrato com você, remover ou desativar o acesso a tal Conteúdo.

Podemos relatar qualquer atividade que suspeitemos violar qualquer lei ou regulamento às autoridades policiais apropriadas, reguladores ou outros terceiros apropriados. Nosso relato pode incluir a divulgação de informações apropriadas do usuário. Também podemos cooperar com agências de aplicação da lei apropriadas, reguladores ou outros terceiros apropriados para ajudar na investigação e repressão de condutas ilegais relacionadas a supostas violações destes Termos.

5. Serviços de Terceiros
Ao usar o Matsumura Connect, você poderá visualizar conteúdos ou utilizar serviços prestados por terceiros, incluindo hiperlinks para outros sites ou conteúdos ou recursos ("Conteúdo de Terceiros").
Às vezes, quando você usa o Conteúdo de Terceiros, os termos de uso e políticas de privacidade de tais terceiros serão aplicáveis.
""".trimIndent()
