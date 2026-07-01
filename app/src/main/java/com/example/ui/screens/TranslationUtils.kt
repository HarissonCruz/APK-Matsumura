package com.example.ui.screens

object TranslationUtils {
    private val translations = mapOf(
        "pt" to mapOf(
            "home" to "Início",
            "athletes" to "Atletas",
            "belts" to "Graduação",
            "wallet" to "Financeiro",
            "settings" to "Configurações",
            "training_hours" to "Horas de Treino",
            "daily_streak" to "Frequência Seguida",
            "days" to "Dias",
            "next_graduation" to "Próxima Graduação",
            "progress" to "Progresso",
            "todays_class" to "TREINO DE HOJE",
            "class_theme" to "Esquema de Aula do Dia",
            "main_theme" to "Tema Principal / Técnica",
            "warmup" to "Aquecimento",
            "technical_exercises" to "Parte Técnica / Exercícios",
            "sparring" to "Rolas / Combates",
            "no_plan_registered" to "Nenhum planejamento cadastrado pelo professor para o horário de %s hoje.",
            "your_wallet" to "Sua Carteira",
            "monthly_payment" to "Mensalidade",
            "status" to "Status",
            "value" to "Valor",
            "due_date" to "Vencimento",
            "paid" to "Pago",
            "pending" to "Pendente",
            "overdue" to "Atrasado",
            "request_plan_change" to "Solicitar Alteração de Plano",
            "current_plan" to "Plano Atual",
            "request_new_plan" to "Solicitar Novo Plano",
            "attendance_history" to "Registro de Presença",
            "language" to "Idioma",
            "save_changes" to "Salvar Alterações",
            "nickname" to "Apelido",
            "weight" to "Peso (kg)",
            "height" to "Altura (m)",
            "access_password" to "Senha de Acesso",
            "select_language" to "Escolha o Idioma do Aplicativo",
            "portuguese" to "Português",
            "english" to "English",
            "academy" to "Academia",
            "active" to "Ativo",
            "inactive" to "Inativo",
            "search_athlete" to "Pesquisar atleta...",
            "all_belts" to "Todas as Faixas",
            "all_status" to "Todos os Status",
            "graduation_journey" to "Jornada de Graduação",
            "current_belt" to "Faixa Atual: %s",
            "current_belt_label" to "Faixa Atual",
            "completed" to "%d%% Concluído",
            "hours_completed" to "%d horas concluídas",
            "classes_attended" to "Aulas assistidas",
            "last_class" to "Última aula",
            "classes" to "aulas",
            "edit_profile" to "Editar Perfil",
            "personal_info" to "Informações Pessoais",
            "change_password_title" to "Alterar Senha",
            "password_saved_success" to "Alterações salvas com sucesso!",
            "saving" to "Salvando...",
            "select_athlete_to_configure" to "Selecionar Atleta para Configurar",
            "athlete_details" to "Detalhes do Atleta",
            "academic_details" to "Dados Acadêmicos",
            "payment_history" to "Histórico de Pagamentos",
            "attendance_title" to "Frequência",
            "frequency" to "Frequência",
            "contracts" to "Contratos",
            "classes_history" to "Histórico de Aulas",
            "summer_challenge" to "DESAFIO DE VERÃO",
            "summer_challenge_desc" to "Participe de 20 treinos neste mês para ganhar o patch exclusivo da equipe!",
            "how_it_works" to "Como funciona:",
            "challenge_rule" to "Treine com consistência e complete o desafio para garantir sua recompensa.",
            "remaining_classes" to "Faltam apenas %d aulas para o objetivo!",
            "challenge_completed" to "Parabéns! Desafio concluído! Procure o professor para resgatar seu patch.",
            "logout" to "Sair",
            "select_plan_subtitle" to "Escolha um plano de assinatura para solicitar alteração",
            "price_per_month" to "R$ %.2f / mês",
            "confirm_request" to "Confirmar Solicitação",
            "cancel" to "Cancelar",
            "request_sent_success" to "Solicitação enviada com sucesso para o professor!",
            "password_min_length" to "Mínimo 6 caracteres"
        ),
        "en" to mapOf(
            "home" to "Home",
            "athletes" to "Athletes",
            "belts" to "Belts",
            "wallet" to "Wallet",
            "settings" to "Settings",
            "training_hours" to "Training Hours",
            "daily_streak" to "Daily Streak",
            "days" to "Days",
            "next_graduation" to "Next Graduation",
            "progress" to "Progress",
            "todays_class" to "TODAY'S CLASS",
            "class_theme" to "Today's Class Outline",
            "main_theme" to "Main Theme / Technique",
            "warmup" to "Warm-up",
            "technical_exercises" to "Technical Exercises",
            "sparring" to "Sparring / Combats",
            "no_plan_registered" to "No class plan registered by the professor for %s today.",
            "your_wallet" to "Your Wallet",
            "monthly_payment" to "Monthly Payment",
            "status" to "Status",
            "value" to "Value",
            "due_date" to "Due Date",
            "paid" to "Paid",
            "pending" to "Pending",
            "overdue" to "Overdue",
            "request_plan_change" to "Request Plan Change",
            "current_plan" to "Current Plan",
            "request_new_plan" to "Request New Plan",
            "attendance_history" to "Attendance Log",
            "language" to "Language",
            "save_changes" to "Save Changes",
            "nickname" to "Nickname",
            "weight" to "Weight (kg)",
            "height" to "Height (m)",
            "access_password" to "Access Password",
            "select_language" to "Choose App Language",
            "portuguese" to "Português",
            "english" to "English",
            "academy" to "Academy",
            "active" to "Active",
            "inactive" to "Inactive",
            "search_athlete" to "Search athlete...",
            "all_belts" to "All Belts",
            "all_status" to "All Status",
            "graduation_journey" to "Graduation Journey",
            "current_belt" to "Current Belt: %s",
            "current_belt_label" to "Current Belt",
            "completed" to "%d%% Completed",
            "hours_completed" to "%d hours completed",
            "classes_attended" to "Classes attended",
            "last_class" to "Last class",
            "classes" to "classes",
            "edit_profile" to "Edit Profile",
            "personal_info" to "Personal Info",
            "change_password_title" to "Change Password",
            "password_saved_success" to "Changes saved successfully!",
            "saving" to "Saving...",
            "select_athlete_to_configure" to "Select Athlete to Configure",
            "athlete_details" to "Athlete Details",
            "academic_details" to "Academic Details",
            "payment_history" to "Payment History",
            "attendance_title" to "Attendance",
            "frequency" to "Frequency",
            "contracts" to "Contracts",
            "classes_history" to "Classes History",
            "summer_challenge" to "SUMMER CHALLENGE",
            "summer_challenge_desc" to "Attend 20 training sessions this month to win the exclusive team patch!",
            "how_it_works" to "How it works:",
            "challenge_rule" to "Train consistently and complete the challenge to guarantee your reward.",
            "remaining_classes" to "Only %d classes left to reach the goal!",
            "challenge_completed" to "Congratulations! Challenge completed! Ask your professor for your patch.",
            "logout" to "Log Out",
            "select_plan_subtitle" to "Choose a subscription plan to request changes",
            "price_per_month" to "$ %.2f / month",
            "confirm_request" to "Confirm Request",
            "cancel" to "Cancel",
            "request_sent_success" to "Request successfully sent to your professor!",
            "password_min_length" to "Minimum 6 characters"
        )
    )

    fun t(key: String, lang: String): String {
        return translations[lang]?.get(key) ?: translations["en"]?.get(key) ?: key
    }

    fun getLocalizedBelt(beltRank: String, lang: String): String {
        val clean = beltRank.trim().lowercase()
        val suffix = if (beltRank.contains("—")) {
            " — " + beltRank.substringAfter("—").trim()
        } else ""

        val baseBelt = if (beltRank.contains("—")) {
            beltRank.substringBefore("—").trim()
        } else {
            beltRank
        }
        val baseClean = baseBelt.lowercase()

        if (lang == "pt") {
            if (baseClean.contains("white") || baseClean.contains("branca")) return "Faixa Branca$suffix"
            if (baseClean.contains("blue") || baseClean.contains("azul")) return "Faixa Azul$suffix"
            if (baseClean.contains("purple") || baseClean.contains("roxa")) return "Faixa Roxa$suffix"
            if (baseClean.contains("brown") || baseClean.contains("marrom")) return "Faixa Marrom$suffix"
            if (baseClean.contains("black") || baseClean.contains("preta")) return "Faixa Preta$suffix"
            return beltRank
        } else {
            if (baseClean.contains("white") || baseClean.contains("branca")) return "White Belt$suffix"
            if (baseClean.contains("blue") || baseClean.contains("azul")) return "Blue Belt$suffix"
            if (baseClean.contains("purple") || baseClean.contains("roxa")) return "Purple Belt$suffix"
            if (baseClean.contains("brown") || baseClean.contains("marrom")) return "Brown Belt$suffix"
            if (baseClean.contains("black") || baseClean.contains("preta")) return "Black Belt$suffix"
            return beltRank
        }
    }
}
