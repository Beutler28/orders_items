# Order Saga — Sistema de Pedidos Event-Driven

Plataforma de pedidos construída com **microsserviços orientados a eventos**, demonstrando
como manter consistência entre serviços independentes (pedido, pagamento e estoque) sem
transações distribuídas tradicionais.

> Projeto de portfólio focado em demonstrar maturidade de engenharia backend:
> consistência eventual, concorrência, idempotência e resiliência.

## 🎯 O problema que este projeto resolve

Quando um cliente faz um pedido, três coisas precisam acontecer de forma confiável:
1. O **pedido** é registrado.
2. O **pagamento** é processado.
3. O **estoque** é reservado.

Esses passos vivem em serviços diferentes. Se o pagamento falhar *depois* do estoque ser
reservado, como reverter? Esse é o problema central de **transações distribuídas**, resolvido
aqui com o **padrão Saga (orquestração)** e operações de **compensação**.

## 🏗️ Arquitetura

```
                          ┌───────────────────────────┐
                          │      order-service        │
   POST /api/orders  ───► │  (Saga Orchestrator)      │
                          │  - máquina de estados      │
                          │  - locking otimista        │
                          └──────────┬────────────────┘
                                     │ Kafka
                 ┌───────────────────┼────────────────────┐
                 ▼                                          ▼
        ┌─────────────────┐                       ┌──────────────────┐
        │ payment-service │                       │ inventory-service│
        │  - idempotência │                       │  - reserva estoque│
        │  - retry/DLQ    │                       │  - locking        │
        └─────────────────┘                       └──────────────────┘
```

### Fluxo da Saga (caminho feliz)
```
order.created ─► payment.requested ─► payment.completed ─► inventory.reserved ─► order.confirmed
```

### Fluxo de compensação (pagamento falha)
```
order.created ─► payment.requested ─► payment.failed ─► order.cancelled
```

### Fluxo de compensação (estoque insuficiente após pagamento)
```
... payment.completed ─► inventory.rejected ─► payment.refunded (compensação) ─► order.cancelled
```

## 🧠 Decisões técnicas (e o porquê)

| Decisão | Motivo |
|---------|--------|
| **Saga por orquestração** (não coreografia) | Fluxo fica explícito e fácil de debugar; um lugar único decide as compensações. |
| **Locking otimista** (`@Version`) no estoque | Conflitos de estoque são raros; otimista evita o custo de locks pessimistas e escala melhor. |
| **Chave de idempotência** em cada consumidor | Kafka entrega *at-least-once*: a mesma mensagem pode chegar 2x. A chave evita cobrar/reservar em duplicidade. |
| **Dead-Letter Queue + retry** (Resilience4j) | Mensagens que falham repetidamente não travam a fila; vão para a DLQ para inspeção. |
| **Banco por serviço** | Cada microsserviço é dono dos seus dados; ninguém acessa a tabela do outro direto. |
| **Outbox pattern** (roadmap) | Garante que publicar evento e gravar no banco sejam atômicos. |

## 🛠️ Stack

`Java 21` · `Spring Boot 3.3` · `Spring Kafka` · `PostgreSQL` · `Flyway` ·
`Resilience4j` · `Testcontainers` · `Docker Compose` · `Prometheus` + `Grafana` · `GitHub Actions`

## 🚀 Como rodar

Pré-requisitos: **Docker** + **Docker Compose**. (Não precisa de Maven nem JDK instalados para subir a infra.)

```bash
# 1. Sobe a infraestrutura (Kafka, Postgres, Prometheus, Grafana, Kafka UI)
docker compose up -d

# 2. Builda e roda cada serviço (em terminais separados)
cd order-service     && ./mvnw spring-boot:run
cd payment-service   && ./mvnw spring-boot:run
cd inventory-service && ./mvnw spring-boot:run
```

| Serviço | URL |
|---------|-----|
| order-service (API) | http://localhost:8081/swagger-ui.html |
| payment-service | http://localhost:8082 |
| inventory-service | http://localhost:8083 |
| Kafka UI | http://localhost:8080 |
| Prometheus | http://localhost:9090 |
| Grafana | http://localhost:3000 (admin/admin) |

### Exemplo de requisição

```bash
curl -X POST http://localhost:8081/api/orders \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": "11111111-1111-1111-1111-111111111111",
    "items": [
      { "productId": "p-1001", "quantity": 2, "unitPrice": 49.90 }
    ]
  }'
```

## 🗺️ Roadmap

- [x] Domínio de pedidos + máquina de estados + locking otimista
- [ ] Mensageria Kafka entre os serviços
- [ ] Saga com compensação completa
- [ ] Idempotência nos consumidores
- [ ] DLQ + retry + circuit breaker
- [ ] Testes de integração com Testcontainers
- [ ] CI no GitHub Actions
- [ ] Outbox pattern
- [ ] Métricas e dashboards Grafana

## 📂 Estrutura

```
orders_items/
├── docker-compose.yml          # Kafka, Postgres, observabilidade
├── infra/                      # Configs de Postgres, Prometheus
├── order-service/              # Saga orchestrator + API de pedidos
├── payment-service/            # Processamento de pagamento
└── inventory-service/          # Reserva de estoque
```
