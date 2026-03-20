# Gestión de secretos en distintos entornos (Java + Docker + CI/CD)

Este documento resume cómo manejar de manera segura credenciales y secretos en distintos entornos: desarrollo local, Docker, docker-compose y CI/CD (GitHub Actions).

---

## 1. Desarrollo local (Java)

Leer secretos usando `System.getenv()`:

```java
String user = System.getenv("DB_USER");
String password = System.getenv("DB_PASSWORD");

if (user == null || password == null) {
    throw new RuntimeException("Faltan variables de entorno DB_USER / DB_PASSWORD");
}
```

Definir las variables en la sesión activa:

**Windows (PowerShell):**
```powershell
$env:DB_USER="mi_usuario"
$env:DB_PASSWORD="mi_password"
```

**Linux / Mac / WSL:**
```bash
export DB_USER=mi_usuario
export DB_PASSWORD=mi_password
```

> Las variables solo existen en la sesión activa. Configura persistencia si es necesario (`.bashrc`, `.zshrc`, variables de entorno del sistema).

---

## 2. Dockerfile

Nunca incluir secretos directamente. Solo se declaran variables vacías como documentación:

```dockerfile
# Documentación de variables esperadas en runtime
ENV DB_USER=""
ENV DB_PASSWORD=""
```

Los valores reales se pasan al ejecutar el contenedor. La aplicación sigue usando `System.getenv()` para leerlos.

---

## 3. Docker (runtime)

**Opción A — En línea:**
```bash
docker run -e DB_USER=mi_usuario -e DB_PASSWORD=mi_password -p 8080:8080 mi-imagen
```

**Opción B — Archivo `.env` (recomendado):**

Archivo `.env`:
```
DB_USER=mi_usuario
DB_PASSWORD=mi_password
```

```bash
docker run --env-file .env -p 8080:8080 mi-imagen
```

> Añade `.env` a `.gitignore` para que nunca se suba al repositorio.

---

## 4. Docker Compose

**Opción A — `env_file`:**
```yaml
version: "3.8"

services:
  app:
    build: .
    ports:
      - "8080:8080"
    env_file:
      - .env
    depends_on:
      - db

  db:
    image: mysql:8
    environment:
      MYSQL_ROOT_PASSWORD: root
      MYSQL_DATABASE: mi_base_datos
    ports:
      - "3306:3306"
```

**Opción B — Interpolación de variables:**
```yaml
services:
  app:
    build: .
    ports:
      - "8080:8080"
    environment:
      - DB_USER=${DB_USER}
      - DB_PASSWORD=${DB_PASSWORD}
```

> `depends_on` solo espera a que el contenedor arranque, no a que MySQL esté listo para aceptar conexiones. Considera usar healthchecks o lógica de retry en la aplicación.

---

## 5. CI/CD — GitHub Actions

Definir los secretos en el repositorio: **Settings → Secrets and variables → Actions**.

```yaml
name: Build & Deploy

on:
  push:
    branches: [ "main" ]

jobs:
  build:
    runs-on: ubuntu-latest

    steps:
      - uses: actions/checkout@v3

      - name: Build Docker image
        run: docker build -t mi-app .

      - name: Run container with secrets
        run: |
          docker run \
            -e DB_USER=${{ secrets.DB_USER }} \
            -e DB_PASSWORD=${{ secrets.DB_PASSWORD }} \
            mi-app
```

> Nunca uses `echo ${{ secrets.DB_PASSWORD }}` en los logs: GitHub lo enmascara, pero es una mala práctica.

---

## 6. Buenas prácticas generales

- No subir `.env` al repositorio.
- No hardcodear credenciales en `config.properties` ni en el código.
- No incluir secretos en la imagen Docker.
- Documentar las variables necesarias sin poner valores reales (p. ej., en el `README`).
- Para producción avanzada: considerar **Secret Manager** (AWS Secrets Manager, HashiCorp Vault, Azure Key Vault).

---

## 7. Tabla comparativa por entorno

| Entorno | Mecanismo | Dónde se definen | ¿Se sube al repo? |
|---|---|---|---|
| Desarrollo local | `System.getenv()` | Variables de sesión / sistema | No |
| Dockerfile | `ENV` (vacío) | El propio Dockerfile | Sí (sin valores) |
| Docker runtime | `-e` / `--env-file` | Línea de comandos / `.env` | No (`.env` en `.gitignore`) |
| Docker Compose | `env_file` / `environment` | `.env` / `docker-compose.yml` | No (`.env` en `.gitignore`) |
| GitHub Actions | `secrets.*` | Settings del repositorio | No (gestionado por GitHub) |
---
Fuentes: [ChatGPT](https://chat.openai.com) + [Claude](https://claude.ai)