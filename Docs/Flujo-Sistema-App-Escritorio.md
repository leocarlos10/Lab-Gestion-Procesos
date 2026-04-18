# Flujo del Sistema — Aplicación de Escritorio
## Guía de Entendimiento para el Equipo de Desarrollo
**Universidad de Córdoba — Ingeniería de Sistemas**
**Curso:** Sistemas Operativos | Laboratorio 2026-I

---

## Introducción

Este documento explica de forma detallada cómo funciona la Aplicación de Escritorio (App 1) del laboratorio de Gestión de Procesos. Está escrito para que cualquier integrante del equipo pueda leerlo y entender qué hay que construir, cómo se conectan las partes y por qué se hace de esa manera.

El sistema consta de **dos aplicaciones separadas** que se comunican entre sí:
- **App 1 (Escritorio):** captura procesos reales del SO, los guarda y los expone vía REST XML
- **App 2 (Web):** consume el REST de la App 1 y ejecuta la simulación Round Robin

Este documento cubre únicamente la **App 1**.

---

## Paso 1 — Vista principal con controles integrados (RF01 + RF02)

La aplicación tiene **una sola ventana principal** que integra tanto los controles de captura como la tabla de resultados. No se usan ventanas o formularios separados.

**¿Por qué una sola vista?**

Si los controles de captura estuvieran en una ventana separada (formulario inicial), el usuario tendría que cerrar y reabrir la aplicación cada vez que quisiera capturar un nuevo conjunto de procesos. Integrando todo en la vista principal, el usuario puede repetir la captura cuantas veces quiera sin interrupciones.

**Distribución sugerida de la vista principal:**

```
┌─────────────────────────────────────────────────────┐
│  GESTIÓN DE PROCESOS — App Escritorio               │
├─────────────────────────────────────────────────────┤
│  Número de procesos: [ 10 ]                         │
│  Criterio:           ( ) CPU   ( ) Memoria          │
│                      [ Capturar Procesos ]          │
├─────────────────────────────────────────────────────┤
│  PID │ Nombre │ Usuario │ Descripción │ Prioridad   │
│ ─────┼────────┼─────────┼─────────────┼───────────  │
│ 1234 │firefox │estudiant│Navegador web│ Expulsivo   │
│ 5678 │systemd │ root    │Sistema init │No Expulsivo │
│  ... │  ...   │   ...   │     ...     │    ...      │
├─────────────────────────────────────────────────────┤
│  Nro. Catálogo: [ 001 ]  Nombre: [ Mi catálogo ]   │
│                          [ Guardar Catálogo ]       │
└─────────────────────────────────────────────────────┘
```

**Flujo de uso:**
1. El usuario llena los campos **N procesos** y **criterio** en la parte superior
2. Presiona **"Capturar Procesos"** → la tabla se actualiza con los resultados
3. Si quiere repetir con diferentes parámetros, cambia los campos y vuelve a presionar el botón — la tabla se refresca automáticamente
4. Cuando esté satisfecho con los resultados, guarda el catálogo desde la parte inferior

---

## Paso 2 — Captura de procesos con OSHI (RF01 + RF02 + RF04)

Para obtener los procesos del sistema operativo se utiliza la librería **OSHI (Operating System and Hardware Information)**, que provee una API Java uniforme que funciona en Linux, Windows y macOS sin ningún cambio en el código. No es necesario detectar el SO manualmente ni ejecutar comandos de terminal — OSHI lo abstrae todo internamente.

### ¿Por qué OSHI?

- Funciona igual en Linux, Windows y macOS — mismo código para todos
- No requiere parsear la salida de comandos de terminal
- API limpia y orientada a objetos
- Mantenida activamente y ampliamente usada en proyectos Java profesionales

### Ejemplo de captura con OSHI

```java
import oshi.SystemInfo;
import oshi.software.os.OSProcess;
import oshi.software.os.OperatingSystem;

public List<Proceso> capturarProcesos(int cantidad, String criterio) {
    SystemInfo si = new SystemInfo();
    OperatingSystem os = si.getOperatingSystem();

    // Seleccionar criterio de ordenamiento
    OperatingSystem.ProcessSorting sort = criterio.equals("CPU")
        ? OperatingSystem.ProcessSorting.CPU_DESC
        : OperatingSystem.ProcessSorting.RSS_DESC;

    List<OSProcess> procesos = os.getProcesses(cantidad, sort);

    List<Proceso> resultado = new ArrayList<>();
    for (OSProcess p : procesos) {
        Proceso proceso = new Proceso();
        proceso.setPid(p.getProcessID());
        proceso.setNombre(p.getName());
        proceso.setUsuario(p.getUser());
        proceso.setDescripcion(p.getName());

        // Asignar prioridad según origen del proceso
        // Usuarios de sistema: root (Linux/Mac), SYSTEM (Windows)
        String usuario = p.getUser() != null ? p.getUser().toLowerCase() : "";
        boolean esSistema = usuario.equals("root") || usuario.equals("system")
                         || usuario.equals("daemon") || usuario.isEmpty();
        proceso.setPrioridad(esSistema ? 1 : 0);

        resultado.add(proceso);
    }
    return resultado;
}
```

**Dependencia Maven:**

```xml
<dependency>
    <groupId>com.github.oshi</groupId>
    <artifactId>oshi-core</artifactId>
    <version>6.4.10</version>
</dependency>
```

---

## Paso 3 — El usuario guarda el catálogo (RF03)

Una vez que la tabla muestra los procesos capturados, el usuario puede decidir **guardar ese conjunto** como un catálogo. Para esto:

1. Ingresa un **número de catálogo** (ej: `001`) — puede ser automático o manual
2. Ingresa un **nombre descriptivo** (ej: `"Procesos del viernes 18 de abril"`)
3. Presiona el botón **"Guardar Catálogo"**

El sistema guarda estos datos en una base de datos **SQLite local**. SQLite es ideal para aplicaciones de escritorio porque no requiere instalar ningún servidor — genera un único archivo `.db` dentro del proyecto.

**Estructura de la base de datos:**

```sql
-- Tabla catálogo: agrupa un conjunto de procesos
CREATE TABLE catalogo (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    numero      INTEGER NOT NULL,
    nombre      TEXT NOT NULL,
    fecha       TEXT NOT NULL
);

-- Tabla proceso: cada proceso pertenece a un catálogo
CREATE TABLE proceso (
    id           INTEGER PRIMARY KEY AUTOINCREMENT,
    catalogo_id  INTEGER,
    pid          INTEGER,
    nombre       TEXT,
    usuario      TEXT,
    descripcion  TEXT,
    prioridad    INTEGER,  -- 0 = Expulsivo, 1 = No Expulsivo
    FOREIGN KEY (catalogo_id) REFERENCES catalogo(id)
);
```

> **Nota:** El catálogo es una acción **voluntaria** del usuario. Si no lo guarda, el Web Service no tendrá datos para exponer y la App 2 no podrá simular nada. Es un paso obligatorio para el funcionamiento completo del sistema, pero lo decide el usuario.

---

## Paso 4 — El Web Service REST expone los datos (RF07)

Una vez que hay catálogos guardados en la base de datos, la aplicación debe tener activo un **servidor REST** que responda peticiones HTTP de la App 2. Este servicio debe estar disponible antes de que la App 2 pueda hacer cualquier cosa.

**¿Cómo funciona?**

- La App 1 levanta un servidor HTTP (usando Jersey o similar)
- Cuando la App 2 hace una petición GET a la URL del servicio, la App 1 consulta la base de datos y devuelve los procesos del catálogo activo en formato **XML**

**Ejemplo de petición desde App 2:**
```
GET http://localhost:8080/api/procesos/catalogo/1
```

**Ejemplo de respuesta en XML:**
```xml
<?xml version="1.0" encoding="UTF-8"?>
<catalogo>
    <numero>001</numero>
    <nombre>Procesos del viernes 18 de abril</nombre>
    <procesos>
        <proceso>
            <pid>1234</pid>
            <nombre>firefox</nombre>
            <usuario>estudiante</usuario>
            <descripcion>Navegador web</descripcion>
            <prioridad>0</prioridad>
            <tiempoRafaga>7000</tiempoRafaga>
        </proceso>
        <proceso>
            <pid>5678</pid>
            <nombre>systemd</nombre>
            <usuario>root</usuario>
            <descripcion>Sistema de inicio</descripcion>
            <prioridad>1</prioridad>
            <tiempoRafaga>8500</tiempoRafaga>
        </proceso>
    </procesos>
</catalogo>
```

> **¿Por qué XML y no JSON?** El laboratorio lo especifica explícitamente. XML es el formato obligatorio para el Web Service.

---

## Paso 5 — La actividad de cada proceso (RF05)

> ⚠️ **Aclaración importante:** La actividad del proceso NO es iniciada por la App 1 ni se dispara cuando el usuario presiona "Capturar Procesos". Esta actividad es **iniciada y controlada completamente por la App 2 (Web)**. La App 1 únicamente captura los datos del proceso y los expone vía REST. Es la App 2 quien, una vez que el usuario define el valor de TH e inicia la simulación, crea los hilos y ejecuta la actividad de escritura de cada proceso.

**¿En qué consiste la actividad?**

Cada proceso debe:
1. Crear un archivo de texto con su **nombre** como nombre del archivo (ej: `firefox.txt`)
2. Escribir su **descripción** dentro del archivo, **un carácter a la vez**
3. Esperar **TH milisegundos** entre cada carácter escrito

**¿Por qué un carácter a la vez?**

Porque cada carácter escrito representa el consumo de **1 quantum de CPU**. Así se simula que el proceso está "trabajando" y consumiendo tiempo de procesador, igual que en un sistema operativo real.

**¿Quién define TH?**

El valor de TH (tiempo del quantum en milisegundos) lo define el usuario desde la **App 2 (Web)**. La App 1 no lo controla directamente.

**¿Por qué un hilo por proceso?**

Porque en un sistema operativo real, múltiples procesos corren **al mismo tiempo** (concurrentemente). Si no se usaran hilos, los procesos se ejecutarían uno por uno en secuencia, lo cual no refleja la realidad. Con un Thread por proceso, todos los procesos pueden estar "escribiendo su archivo" simultáneamente.

**Fórmula del tiempo de ráfaga (TR):**

TR = TH × (cantidad de caracteres de la descripción del proceso)

**Ejemplo concreto con 3 procesos y TH = 500ms:**

| Proceso | Descripción | Caracteres | TR |
|---|---|---|---|
| firefox | "Navegador web" | 14 | 500 × 14 = 7000ms |
| chrome | "Web browser" | 11 | 500 × 11 = 5500ms |
| code | "Editor de código" | 17 | 500 × 17 = 8500ms |

Los tres hilos corren al mismo tiempo, cada uno escribiendo su archivo letra por letra, esperando 500ms entre cada una.

---

## Flujo completo resumido

```
APP 1 — ESCRITORIO
══════════════════════════════════════════════════
[Usuario abre la App 1 — Vista Principal Única]
        |
        v
[Ingresa N procesos + criterio CPU/Memoria]
[Presiona "Capturar Procesos"]
        |
        v
[OSHI obtiene los N procesos del SO]
[Funciona en Linux, Windows y macOS sin cambios]
[Asigna prioridades automáticamente]
[Tabla se actualiza con los resultados]
        |
        ↕  (repetible sin cerrar la app)
        |
        v
[Usuario asigna número y nombre al catálogo]
[Presiona "Guardar Catálogo"]
[App guarda en SQLite]
        |
        v
[Web Service REST queda activo y escuchando]
[Expone los procesos en XML]

APP 2 — WEB
══════════════════════════════════════════════════
        |
        v
[Consume el REST XML de la App 1]
[Usuario define TH en milisegundos]
[Usuario presiona "Iniciar Simulación"]
        |
        v
[Se crean los Threads — uno por proceso]
[Cada Thread escribe su archivo carácter por carácter]
[Round Robin gestiona qué proceso usa la CPU]
[Simulación puede pausarse y reanudarse]
```

---

## Stack tecnológico recomendado

| Componente | Tecnología |
|---|---|
| Interfaz gráfica | JavaFX + Scene Builder |
| Build system | Maven |
| Base de datos | SQLite (sqlite-jdbc) |
| Captura de procesos multiplataforma | OSHI |
| Web Service REST | Jersey (JAX-RS) |
| Serialización XML | JAXB |
| Hilos | Java Thread / ExecutorService |
| JDK | Java 21 LTS |
| IDE | IntelliJ IDEA |

---

## Dependencias Maven necesarias

```xml
<!-- JavaFX -->
<dependency>
    <groupId>org.openjfx</groupId>
    <artifactId>javafx-controls</artifactId>
    <version>21</version>
</dependency>
<dependency>
    <groupId>org.openjfx</groupId>
    <artifactId>javafx-fxml</artifactId>
    <version>21</version>
</dependency>

<!-- SQLite -->
<dependency>
    <groupId>org.xerial</groupId>
    <artifactId>sqlite-jdbc</artifactId>
    <version>3.45.1.0</version>
</dependency>

<!-- OSHI — captura de procesos multiplataforma -->
<dependency>
    <groupId>com.github.oshi</groupId>
    <artifactId>oshi-core</artifactId>
    <version>6.4.10</version>
</dependency>

<!-- Jersey REST -->
<dependency>
    <groupId>org.glassfish.jersey.containers</groupId>
    <artifactId>jersey-container-grizzly2-http</artifactId>
    <version>3.1.3</version>
</dependency>

<!-- JAXB para XML -->
<dependency>
    <groupId>jakarta.xml.bind</groupId>
    <artifactId>jakarta.xml.bind-api</artifactId>
    <version>4.0.0</version>
</dependency>
```
