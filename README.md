# Stressful Kafka

Ambiente de estresse para testar Kafka com uma API que processa mensagens lentamente.

## Arquitetura

- **frontend-web** — React + TypeScript; botões para enviar 1/10/100 requests e count atualizado a cada 5s
- **backend-api** — Spring Boot; recebe HTTP e publica mensagens no Kafka
- **stress-api** — Spring Boot; consome Kafka, simula processamento pesado (1s) e persiste em H2 in-memory
- **kafka** — Apache Kafka oficial (KRaft, sem Zookeeper)

Mensagens de **count** usam tópico e consumer group separados, então o refresh do frontend não trava mesmo com fila grande de processamento.

## Subir na VM

```bash
docker compose up --build
```

Acesse no browser: `http://<IP-DA-VM>:8080/`

Portas expostas:
- `8080` — frontend (nginx + proxy para backend-api)
- `9092` — Kafka (opcional, debug)

## Cenário de teste

1. Clique em **100 requests** — 100 mensagens entram na fila `process-request` instantaneamente
2. A stress-api processa ~1 msg/s (delay de 1s, concurrency=1)
3. O count sobe gradualmente (~100s para esvaziar a fila)
4. O refresh a cada 5s continua responsivo (fila de count separada)
5. Clique 100 novamente antes de terminar — a fila acumula, demonstrando backpressure

## Variáveis de ambiente

| Variável | Serviço | Default |
|---|---|---|
| `SPRING_KAFKA_BOOTSTRAP_SERVERS` | backend-api, stress-api | `kafka:9092` |
| `STRESS_PROCESSING_DELAY_MS` | stress-api | `1000` (via `stress.processing-delay-ms`) |
