# Uso de Docker

Este proyecto utiliza Docker para empaquetar y ejecutar la aplicación Spring Boot de forma aislada, garantizando que funcione igual en cualquier entorno.

## Conceptos principales

### Docker Engine
Es el motor de Docker, el servicio que construye imágenes, ejecuta contenedores y gestiona recursos (redes, volúmenes, etc.). Es el "runtime" que hace que todo funcione.

### Imagen (`image`)
Plantilla inmutable que contiene el sistema base, el entorno de ejecución (JDK/JRE) y la aplicación (`.jar`). Se crea a partir de un `Dockerfile`.

### Contenedor (`container`)
Instancia en ejecución de una imagen. Es aislado del sistema, ligero (comparte el kernel del host) y efímero (se puede borrar y recrear fácilmente).

### Dockerfile
Un [Dockerfile](./Dockerfile) es un fichero de texto que contiene las instrucciones para construir una imagen Docker.

- Define el entorno de ejecución de la aplicación: sistema base, dependencias y cómo arrancar la app.
- Es reproducible y versionable, lo que garantiza que la imagen se construya igual en cualquier máquina.
- En proyectos Spring Boot, suele usar multi-stage build para separar la fase de compilación de la de ejecución.

#### Dockerfile (multi-stage)

- **Etapa 1 (construcción):** compila la aplicación con Maven
- **Etapa 2 (ejecución):** ejecuta solo el `.jar` con un entorno ligero

```dockerfile
# Etapa 1: construcción
FROM maven:3.9-eclipse-temurin-23 AS imagen_construccion

WORKDIR /app
COPY pom.xml .
COPY src ./src

RUN mvn clean package

# Etapa 2: ejecución
FROM eclipse-temurin:23-jre AS imagen_ejecucion

WORKDIR /app

COPY --from=imagen_construccion /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
```

**Qué está pasando:**

1. **Build:** se compila el proyecto y se genera el `.jar`
2. **Separación de responsabilidades:** Maven y el código fuente quedan solo en la etapa de build; el runtime solo contiene JRE y el `.jar`
3. **Optimización:** la imagen final es más ligera, con menor superficie de error y mayor seguridad

## Ejecución y conceptos prácticos

**Puertos** — permiten acceder a la aplicación desde fuera del contenedor:
```bash
-p 8080:8080
```

**Variables de entorno** — configuran la aplicación sin modificar el código:
```bash
-e SPRING_DATASOURCE_URL=...
```

**Volúmenes** — persisten datos fuera del contenedor:
```bash
-v /ruta/local:/ruta/contenedor
```

## Comandos básicos

```bash
# Construir la imagen Docker a partir del Dockerfile
# -t mapamundi-app  → asigna un nombre (tag) a la imagen
# .                 → indica el contexto de construcción (directorio actual, donde está el Dockerfile)
docker build -t mapamundi-app .

# Ejecutar un contenedor a partir de la imagen creada
# -p 8080:8080      → mapea el puerto 8080 del host al 8080 del contenedor
# --name mapamundi_sb → asigna un nombre al contenedor para poder gestionarlo fácilmente
# mapamundi-app     → nombre de la imagen que se va a ejecutar
docker run -p 8080:8080 --name mapamundi_sb mapamundi-app

# Ver contenedores (en ejecución / todos)
docker ps
docker ps -a

# Ver logs
docker logs -f mapamundi_sb

# Detener y eliminar contenedores
docker stop mapamundi_sb
docker rm mapamundi_sb

# Eliminar imagen
docker rmi mapamundi-app
```

## Resumen rápido

| Concepto | Descripción |
|---|---|
| Docker Engine | Ejecuta y gestiona todo |
| Imagen | Plantilla inmutable de la aplicación |
| Contenedor | Instancia en ejecución de una imagen |
| Dockerfile | Instrucciones para construir la imagen |
| Multi-stage | Imágenes más ligeras y eficientes |

---
Fuentes: [ChatGPT](https://chat.openai.com) + [Claude](https://claude.ai)