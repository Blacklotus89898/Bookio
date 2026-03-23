# COMP421 — Project 3 JDBC

## Environment Setup

Set credentials once per terminal session before compiling or running:

```powershell
$env:SOCSUSER="cs421g06"
$env:SOCSPASSWD="Bookio421"
```

---

## Tutorial (`simpleJDBC`)

Basic template to understand how JDBC connects to DB2.

```powershell
javac -cp ".;db2jcc4.jar" simpleJDBC.java
java  -cp ".;db2jcc4.jar" simpleJDBC
```

---

## Project (`Main`)

Runs all queries and modifications against the Bookio database.

```powershell
javac -cp ".;db2jcc4.jar" DBConnection.java Queries.java Modifications.java Main.java
java  -cp ".;db2jcc4.jar" Main
```

### File Overview

| File | Role |
|---|---|
| `DBConnection.java` | DB2 driver registration and connection |
| `Queries.java` | Q1–Q5 SELECT queries |
| `Modifications.java` | M1–M5 updates/inserts/deletes (self-restoring) |
| `Main.java` | Entry point — calls all queries and modifications |

---

## Cleanup Before Commit

Remove compiled `.class` files and any logs:

```powershell
# Windows — remove all .class files
Remove-Item -Force *.class

# Also remove any leftover logs if present
Remove-Item -Force *.log
```

Or if you prefer one-liner:

```powershell
Remove-Item -Force *.class, *.log
```

### Pre-commit checklist

- [ ] No credentials hardcoded in any `.java` file
- [ ] All `.class` files deleted
- [ ] `simpleJDBC.java` credentials removed if still present
- [ ] `.log` files removed