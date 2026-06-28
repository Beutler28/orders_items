# 🗺️ Divisão do Projeto — Partes, Módulos e Fases

Este documento quebra o **Order Saga** em partes gerenciáveis. A ideia é entregar valor de
forma incremental: cada fase resulta em algo que **roda e pode ser demonstrado**, e cada uma
adiciona uma competência técnica que vale ponto numa entrevista de pleno/sênior.

> Visão geral do projeto está no [README.md](README.md).

---

## 🧩 Parte 1 — Os componentes (o "o quê")

O sistema é dividido em **3 microsserviços** + **infraestrutura compartilhada**.

### A. `order-service` — Orquestrador da Saga
O coração do sistema. Recebe os pedidos via API REST e coordena toda a saga.
- API REST de pedidos (criar, consultar, listar)
- Máquina de estados do pedido (`CREATED → PAID → CONFIRMED → ...`)
- Locking otimista para evitar updates concorrentes no mesmo pedido
- Publica e consome eventos para conduzir a saga e suas compensações

### B. `payment-service` — Pagamento
Simula o processamento de pagamento (aprovado/recusado).
- Consome `payment.requested`
- Idempotência: a mesma cobrança nunca é processada 2x
- Publica `payment.completed` ou `payment.failed`
- Suporta compensação: `payment.refund` (estorno)

### C. `inventory-service` — Estoque
Reserva itens do estoque de forma segura sob concorrência.
- Consome `inventory.reserve`
- Locking (otimista) para o caso de dois pedidos disputarem o último item
- Publica `inventory.reserved` ou `inventory.rejected`
- Suporta compensação: `inventory.release` (devolve ao estoque)

### D. Infraestrutura (`docker-compose.yml` + `infra/`)
- **Kafka** — barramento de eventos entre os serviços
- **PostgreSQL** — um banco por serviço (`order_db`, `payment_db`, `inventory_db`)
- **Kafka UI** — inspeção visual dos tópicos e mensagens
- **Prometheus + Grafana** — métricas e dashboards

---

## 📅 Parte 2 — As fases (o "quando")

Cada fase é uma branch/PR independente e tem um critério de "pronto" claro.

### Fase 0 — Fundação ✅ (em andamento)
> **Entrega:** infra sobe com `docker compose up` e o esqueleto dos serviços compila.
- [x] `docker-compose.yml` com Kafka, Postgres e observabilidade
- [x] READMEs (explicação + divisão do projeto)
- [ ] Estrutura Maven dos 3 serviços (com Maven Wrapper)
- [ ] Health check (`/actuator/health`) respondendo em cada serviço

### Fase 1 — Domínio de Pedidos
> **Entrega:** criar e consultar pedidos via API, persistindo no banco.
- [ ] Entidades `Order` e `OrderItem` (JPA)
- [ ] Migrations Flyway (`V1__init.sql`)
- [ ] Máquina de estados `OrderStatus` com transições válidas
- [ ] Locking otimista (`@Version`)
- [ ] Endpoints `POST /api/orders`, `GET /api/orders/{id}`, `GET /api/orders`
- [ ] Tratamento global de erros + validação (Bean Validation)
- **Competência demonstrada:** modelagem de domínio, concorrência, REST bem feito

### Fase 2 — Mensageria (eventos assíncronos)
> **Entrega:** pedido criado dispara evento; pagamento e estoque reagem.
- [ ] Configuração do Spring Kafka (producer + consumer)
- [ ] Definição dos tópicos e contratos de evento (DTOs versionados)
- [ ] `order-service` publica `order.created`
- [ ] `payment-service` e `inventory-service` consomem e respondem
- **Competência demonstrada:** comunicação assíncrona, desacoplamento

### Fase 3 — Saga + Compensação
> **Entrega:** falha de pagamento ou estoque cancela o pedido de forma consistente.
- [ ] Orquestração da saga no `order-service`
- [ ] Caminho feliz: `created → paid → reserved → confirmed`
- [ ] Compensações: estorno de pagamento, liberação de estoque
- [ ] Estados de falha refletidos no pedido (`CANCELLED`, `PAYMENT_FAILED`)
- **Competência demonstrada:** transações distribuídas, consistência eventual

### Fase 4 — Resiliência & Idempotência
> **Entrega:** sistema sobrevive a mensagens duplicadas e falhas transitórias.
- [ ] Chave de idempotência nos consumidores
- [ ] Retry com backoff (Spring Kafka / Resilience4j)
- [ ] Dead-Letter Queue para mensagens "envenenadas"
- [ ] Circuit breaker em chamadas externas simuladas
- **Competência demonstrada:** sistemas confiáveis em produção

### Fase 5 — Qualidade & Testes
> **Entrega:** suíte de testes roda no CI e dá confiança para refatorar.
- [ ] Testes unitários do domínio (máquina de estados, regras)
- [ ] Testes de integração com **Testcontainers** (Postgres + Kafka reais)
- [ ] Cobertura de teste medida (JaCoCo)
- **Competência demonstrada:** disciplina de testes, automação

### Fase 6 — Operação & Observabilidade
> **Entrega:** dá para ver o que o sistema está fazendo em tempo real.
- [ ] Métricas via Actuator + Micrometer → Prometheus
- [ ] Dashboard Grafana (taxa de pedidos, falhas, latência)
- [ ] Logs estruturados com correlação (trace id por pedido)
- [ ] `Dockerfile` de cada serviço + perfil de produção
- **Competência demonstrada:** maturidade operacional

### Fase 7 — CI/CD
> **Entrega:** todo push roda build + testes automaticamente.
- [ ] GitHub Actions: build, test, lint
- [ ] Build das imagens Docker
- [ ] Badge de status no README
- **Competência demonstrada:** automação de entrega

### Fase 8 — Extras (diferencial)
> **Entrega:** detalhes que mostram visão de engenharia avançada.
- [ ] **Outbox pattern** (atomicidade entre banco e evento)
- [ ] API Gateway + roteamento
- [ ] Versionamento de eventos / schema registry
- [ ] Deploy real (Railway / Render / fly.io)

---

## 🎯 Parte 3 — Sugestão de ordem de ataque

```
Fase 0  ──►  Fase 1  ──►  Fase 2  ──►  Fase 3  ──►  Fase 4
(infra)     (domínio)    (eventos)     (saga)      (resiliência)
                                                        │
                              Fase 7  ◄──  Fase 6  ◄──  Fase 5
                              (CI/CD)    (observ.)     (testes)
```

**Regra de ouro:** termine uma fase, faça commit/PR com um bom texto explicando a decisão,
e só então comece a próxima. O histórico do Git vira parte do seu portfólio — ele conta a
história de como você pensa.

---

## 📌 Mapa rápido de pastas por parte

| Parte | Pasta | Status |
|-------|-------|--------|
| Infra | `docker-compose.yml`, `infra/` | 🟡 em andamento |
| Order Service | `order-service/` | ⚪ a fazer |
| Payment Service | `payment-service/` | ⚪ a fazer |
| Inventory Service | `inventory-service/` | ⚪ a fazer |
