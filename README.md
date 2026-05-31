# Store Intelligence System
### Purplle Tech Challenge 2026 — Round 2

An end-to-end AI-powered Store Intelligence System that processes CCTV footage to deliver real-time analytics, anomaly detection, and a live operations dashboard.

---

## Architecture

```
CCTV Cameras → Video Ingestion → CV Pipeline (AWS Rekognition / Simulator)
     → Kafka Event Stream → Analytics Engine → REST APIs + WebSocket
     → Live Dashboard
```

**Tech Stack:** Java 21 · Spring Boot 3 · Spring Kafka · H2 (local) / PostgreSQL (prod) · WebSocket (STOMP) · Docker

---

## Quick Start (No install needed except Java + Maven)

### Prerequisites
- Java 21+ (`java -version`)
- Maven 3.9+ (`mvn -version`)

### Run locally

```bash
# Clone the repo
git clone https://github.com/YOUR_USERNAME/store-intelligence.git
cd store-intelligence

# Build and run
mvn spring-boot:run

# Open dashboard
open http://localhost:8080
```

The system starts a CCTV simulator immediately — you will see live data flowing within seconds.

### Run with Docker (zero dependencies)

```bash
docker build -t store-intelligence .
docker run -p 8080:8080 store-intelligence
```

---

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/health` | Health check |
| GET | `/api/dashboard` | Summary stats |
| GET | `/api/zones/occupancy` | Real-time zone occupancy |
| GET | `/api/detections` | Last 50 detections |
| GET | `/api/alerts` | Active anomaly alerts |
| GET | `/api/alerts/all` | Last 20 alerts |
| POST | `/api/alerts/{id}/resolve` | Resolve an alert |
| WS | `/ws` → `/topic/events` | Live detection stream |
| WS | `/ws` → `/topic/alerts` | Live alert stream |
| WS | `/ws` → `/topic/occupancy` | Live zone occupancy |

**H2 Console** (local dev): http://localhost:8080/h2-console (JDBC URL: `jdbc:h2:mem:storedb`)

---

## Key Features

- **CV Pipeline**: Simulates AWS Rekognition detections (person tracking, bounding boxes, confidence scores). Swap `CctvSimulator` for real Rekognition SDK calls in production.
- **Event Streaming**: Apache Kafka (embedded locally, Amazon MSK in prod) with partitioned topics per camera group.
- **Anomaly Detection**: Rule-based detection for overcrowding (>8 persons), lingering (>120s dwell), and empty zones.
- **Real-time Dashboard**: WebSocket push with live heatmap, anomaly feed, footfall chart, and event log.
- **Production-ready**: Dockerized, structured logging, externalized config, H2→PostgreSQL swap via single property change.

---

## Production Deployment (AWS)

Replace these in `application.properties`:
```properties
# Swap H2 for RDS PostgreSQL
spring.datasource.url=jdbc:postgresql://your-rds-endpoint:5432/storedb

# Swap embedded Kafka for Amazon MSK
spring.kafka.bootstrap-servers=your-msk-bootstrap:9092

# Enable real CV pipeline
app.simulation.enabled=false
```

Deploy via:
```bash
docker build -t store-intelligence .
aws ecr push ... # push to ECR
aws ecs update-service ... # deploy on Fargate
```

---

## Event Schema

```json
{
  "cameraId": "CAM_01",
  "zone": "Electronics",
  "storeId": "STORE_001",
  "personId": "P1023",
  "confidence": 0.94,
  "personCount": 7,
  "eventType": "ANOMALY_OVERCROWD",
  "dwellTimeSeconds": 145,
  "overcrowded": true,
  "lingering": false,
  "timestamp": "2026-05-30T10:23:45Z"
}
```
