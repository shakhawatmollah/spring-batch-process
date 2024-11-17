# Spring Boot Batch Process (Multithreading)

## Features!
* Data Batch Process with Multi-threading
* Kafka to real-time data feeds
* MySQL to update product data

### Technology
* JDK 17 or later
* Spring Boot 3.3.5
* Kafka
* Maven 3.0
* Lombok

### Bulk Data Script

Path: docs/product.sql

## Quick Start

1. Clone the repository
```bash
git clone https://github.com/shakhawatmollah/spring-batch-process.git
cd spring-batch-process
```

```bash
mvn spring-boot:run
```

2. Open Postman

### Postman Collection

Import Postman collection to quickly get started.

Collection:

[![Collection](https://run.pstmn.io/button.svg)](https://github.com/shakhawatmollah/spring-batch-process/blob/main/docs/spring-batch-process.postman_collection.json)

### Setup Kafka
🟢 Kafka Start at Windows
1. Start Zookeeper Server

   ```zookeeper-server-start.bat ..\..\config\zookeeper.properties```

2. Start Kafka Server / Broker

   ```kafka-server-start.bat ..\..\config\server.properties```

### Audit Kafka Topic

```
kafka-console-consumer.bat --bootstrap-server localhost:9092 --topic PRODUCT_DISCOUNT_UPDATE --from-beginning
```