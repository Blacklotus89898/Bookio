# COMP421 — Project 3 JDBC

## Environment Setup

Set credentials once per terminal session before compiling or running:

```powershell
$env:SOCSUSER="cs421g06"
$env:SOCSPASSWD="Bookio421"
```

---

# Project Structure

```
Bookio
├── lib
│   └── db2jcc4.jar
└── src
    └── bookio
        ├── DBConnection.java
        ├── MainCLI.java
        └── MainServer.java
```

| File | Role |
|---|---|
| DBConnection.java | DB2 driver registration and connection |
| MainCLI.java | CLI interface for running queries |
| MainServer.java | HTTP server exposing API endpoints |
| db2jcc4.jar | DB2 JDBC driver |

---

# Compilation

Run from the **Bookio root directory**:

```powershell
javac -cp ".;lib\db2jcc4.jar" src\bookio\*.java
```

---

# Run CLI

```powershell
java -cp ".;src;lib\db2jcc4.jar" bookio.MainCLI
```

---

# Run HTTP Server

```powershell
java -cp ".;src;lib\db2jcc4.jar" bookio.MainServer
```

Server runs at:

```
http://localhost:8080
```

Example endpoint:

```
http://localhost:8080/api/books
```

---

# Cleanup Before Commit

Remove compiled `.class` files:

```powershell
Remove-Item -Force src\bookio\*.class
```

Remove logs if present:

```powershell
Remove-Item -Force *.log
```

---

# Pre-commit checklist

- [ ] No credentials hardcoded in any `.java` file
- [ ] All `.class` files deleted
- [ ] `.log` files removed