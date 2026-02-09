# Guia de Testes - SUS Queue Manager WhatsApp Integration (W-API Real)

## 📋 Pré-requisitos

- ✅ PostgreSQL rodando (porta 5432)
- ✅ RabbitMQ rodando (porta 5672)
- ✅ Aplicação Spring Boot rodando (http://localhost:8080)
- ✅ Conta W-API configurada: https://www.w-api.app
- ✅ Número de celular para testes

---

## 🚀 Setup Completo (Fluxo via W-API)

### 1. Configurar Webhook na W-API

Acesse: https://www.w-api.app/dashboard

1. Vá para **Instâncias** → Sua instância (LITE-LLJQV6-NBMXCF)
2. Clique em **Configurar webhooks**
3. Preencha os campos conforme abaixo:

**Para ambiente local (com ngrok):**
```powershell
# 1. Instale ngrok: https://ngrok.com/download
# 2. Execute em um terminal:
ngrok http 8080

# 3. Você receberá uma URL como: https://abcd1234.ngrok.io
# Use essa URL nos webhooks abaixo
```

**Configure os 4 webhooks na W-API:**

| Campo | URL |
|-------|-----|
| **Ao conectar o whatsapp na instância** | `https://seu-ngrok.ngrok.io/webhook/whatsapp/connect` |
| **Ao desconectar da instância** | `https://seu-ngrok.ngrok.io/webhook/whatsapp/disconnect` |
| **Ao enviar uma mensagem** | `https://seu-ngrok.ngrok.io/webhook/whatsapp/send` |
| **Ao receber uma mensagem** | `https://seu-ngrok.ngrok.io/webhook/whatsapp` ✅ (Principal) |

**Para ambiente de produção:**
```
Substitua "seu-ngrok.ngrok.io" pelo seu domínio/IP público:
- https://seu-dominio.com/webhook/whatsapp
- https://seu-ip-publico:8080/webhook/whatsapp
```

⚠️ **Importante:** O webhook mais importante é o **"Ao receber uma mensagem"** que processa as respostas do usuário (1 ou 2).

---

### 2. Inserir Dados de Teste no Banco

Conecte ao PostgreSQL e execute:

```sql
-- 1. Insira um Interest com SEU NÚMERO
INSERT INTO interest (pacient_name, phone_number, exam_name, pacient_cns, exam_hash_code, is_notified, notification_status)
VALUES ('Seu Nome', '55SEU_NUMERO_AQUI', 'Eletrocardiograma', '12345678901234', 'exam_hash_eco_001', false, NULL);
-- Exemplo: '5511987654321' (com país e DDD, sem +)

-- 2. Insira um Endereço
INSERT INTO address (city, state, zip_code)
VALUES ('São Paulo', 'SP', '01310100');

-- 3. Obtenha o ID do endereço
SELECT id FROM address ORDER BY id DESC LIMIT 1;

-- 4. Insira uma Availability
INSERT INTO availability (prestador_name, address_id, exam_hash_code, data_hora_disponivel, is_available)
VALUES ('Dr. Pedro Cardiology', {ADDRESS_ID}, 'exam_hash_eco_001', NOW() + INTERVAL '2 hours', true);

-- 5. Verificar o Interest criado
SELECT id_interest, pacient_name, phone_number, notification_status 
FROM interest 
WHERE exam_hash_code = 'exam_hash_eco_001';
```

---

## 📱 Fluxo Completo via WhatsApp

### **Passo 1: Disparar Notificação**

O scheduler roda automaticamente a cada **5 minutos** (`0 */5 * * * *`).

**Opção A: Aguardar o scheduler**
- Espere até 5 minutos
- Você receberá a mensagem no WhatsApp

**Opção B: Disparar manualmente**
```bash
POST http://localhost:8080/test/notify-interests
```

**Você receberá no WhatsApp:**
```
Olá João Silva, o exame Eletrocardiograma foi disponibilizado por Dr. Pedro Cardiology em São Paulo. Data: 09/02/2026 19:45

Responda:
*1* para confirmar a consulta
*2* para rejeitar a consulta
```

---

### **Passo 2: Responder no WhatsApp**

**Cenário 1: Responder com "1" (SIM - Confirma)**

- Abra a conversa no WhatsApp
- Digite: `1`
- Envie

**Esperado:**
- ✅ Você recebe: "✅ Sua consulta foi *confirmada* para 09/02/2026 19:45. Aguardamos você!"
- ✅ Banco: `notification_status` = `ACCEPTED`, `is_notified` = `true`
- ✅ RabbitMQ: Mensagem em `answer_confirmed`
- 📅 Consulta antecipada para aquele horário!

---

**Cenário 2: Responder com "2" (NÃO - Rejeita)**

- Abra a conversa no WhatsApp
- Digite: `2`
- Envie

**Esperado:**
- ✅ Você recebe: "📅 Entendido! Você continuará na fila. Enviaremos outra oportunidade em breve."
- ✅ Banco: `notification_status` = `REJECTED`
- ✅ RabbitMQ: Mensagem em `answer_rejected`
- 📋 Você continua na fila para outras oportunidades!

---

### **Passo 3: Próxima Notificação (se houver próximo paciente)**

Se você respondeu "2" e houver outro `Interest` na fila:
- O sistema automaticamente envia notificação para o próximo paciente
- O próximo recebe a mesma mensagem com as opções 1 e 2

---

## ⏱️ Teste de Timeout (2 horas)

Após responder ou não responder por **2 horas**:

O scheduler `processPendingTimeouts()` roda a cada **10 minutos** e verifica:
- Se há notificações PENDING há mais de 2 horas
- Se sim: Envia timeout automático e avança para próximo

**Você receberá:**
```
⏰ O tempo para responder expirou. Você continuará na fila. 
Enviaremos outra oportunidade em breve.
```

---

## 🔍 Monitoramento Real

### **Ver Status no Banco**
```sql
-- Ver todos os Interests com status
SELECT id_interest, pacient_name, phone_number, notification_status, notification_sent_at, is_notified, notification_correlation_id
FROM interest 
ORDER BY updated_at DESC;

-- Ver Interest específico
SELECT * FROM interest WHERE phone_number = '55SEU_NUMERO';
```

### **Ver Mensagens no RabbitMQ**
Acesse: http://localhost:15672
- User: `guest` | Pass: `guest`

Verifique as queues:
- `answer_confirmed` - Respostas confirmadas
- `answer_rejected` - Respostas rejeitadas

---

## 🐛 Troubleshooting

| Problema | Solução |
|----------|---------|
| "Não recebo mensagem no WhatsApp" | 1. Verificar se webhook está configurado na W-API<br>2. Verificar se ngrok está rodando (se local)<br>3. Verificar logs da aplicação |
| "Webhook não é chamado" | 1. Testar webhook na W-API: https://api.w-api.app/docs<br>2. Verificar URL do webhook em Settings<br>3. Usar ngrok para expor aplicação local |
| "correlationId null" | Aguarde o scheduler rodar (5 min) ou dispare manualmente |
| "W-API diz acesso negado" | Verificar token em `application.properties` |

---

## 📝 Testar Webhook Manualmente

Se quiser testar se o webhook está funcionando:

```bash
# Terminal PowerShell
$body = @{
    instanceId = "LITE-LLJQV6-NBMXCF"
    status = "DELIVERED"
    body = "sua mensagem"
    message = @{
        id = @{
            fromMe = $false
            remote = "55SEU_NUMERO"
            id = "UUID_GERADO"
        }
    }
    messageType = "chat"
    timestamp = (Get-Date -AsUTC).Ticks
    senderName = "João Silva"
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8080/webhook/whatsapp" `
    -Method POST `
    -Body $body `
    -ContentType "application/json"
```

---

## ✨ Resumo do Fluxo

```
┌─────────────────────────────────────────────────────────────┐
│ 1. Scheduler envia mensagem via W-API a cada 5 minutos     │
├─────────────────────────────────────────────────────────────┤
│ 2. Você recebe no WhatsApp com opções 1 (SIM) ou 2 (NÃO)   │
├─────────────────────────────────────────────────────────────┤
│ 3. Você responde com 1 ou 2                                 │
├─────────────────────────────────────────────────────────────┤
│ 4. W-API envia webhook para /webhook/whatsapp               │
├─────────────────────────────────────────────────────────────┤
│ 5. Sistema processa:                                        │
│    ├─ Se 1: Confirma consulta → Mensagem de sucesso        │
│    └─ Se 2: Rejeita → Próximo paciente é notificado        │
├─────────────────────────────────────────────────────────────┤
│ 6. Banco atualizado + RabbitMQ publica resultado            │
└─────────────────────────────────────────────────────────────┘
```

Tudo pronto! Vamos testar? 🚀
