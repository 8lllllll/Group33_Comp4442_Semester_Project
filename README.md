# A simple login system

## Setup

### 1. Start the Server

Run the following commands in the project root:

```bash
.\mvnw.cmd compile
.\mvnw.cmd spring-boot:run
```

The server will start at:
http://localhost:9001

---

## Web UI

Access the login page at:
http://localhost:9001/api/index.html

---

## H2 Database Console

Access the H2 database console for testing and debugging:
http://localhost:9001/api/h2-console

### Database Configuration:

* **JDBC URL:** `jdbc:h2:file:./data/usersdb`
* **Username:** `Admin`
* **Password:** `Admin`

---
