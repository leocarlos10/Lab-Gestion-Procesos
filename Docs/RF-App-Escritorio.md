# Requisitos Funcionales — Aplicación de Escritorio
## Sistema de Gestión de Procesos | Laboratorio SO 2026-I
**Universidad de Córdoba — Ingeniería de Sistemas**
**Curso:** Sistemas Operativos
**Fecha de entrega:** 26/04/2026

---

## RF01 — Definir cantidad de procesos

**Descripción:** El sistema debe permitir al usuario ingresar el número de procesos que desea obtener del sistema operativo en ejecución.

**Entrada:** Número entero positivo ingresado por el usuario desde la interfaz gráfica.

**Salida:** Lista de N procesos capturados del SO mostrados en la tabla de la interfaz.

**Reglas:**
- El número debe ser mayor que 0
- El sistema captura exactamente N procesos según el criterio definido en RF02

---

## RF02 — Filtrar procesos por recurso

**Descripción:** El sistema debe permitir al usuario seleccionar el criterio de ordenamiento para obtener los procesos más consumidores del sistema.

**Opciones disponibles:**
- Mayor uso de **CPU**
- Mayor uso de **Memoria**

**Reglas:**
- El sistema consulta los procesos del SO ordenados por el criterio seleccionado
- Se toman los primeros N procesos según el número definido en RF01
- En Ubuntu/Linux se usan los comandos: `ps aux --sort=-%cpu` o `ps aux --sort=-%mem`

---

## RF03 — Registrar catálogo de procesos

**Descripción:** El sistema debe permitir al usuario guardar el conjunto de procesos obtenidos bajo un catálogo identificado, para su posterior uso en el simulador.

**Datos del catálogo:**
| Campo | Tipo | Descripción |
|---|---|---|
| Número de catálogo | Entero | Consecutivo automático o digitado manualmente por el usuario |
| Nombre del catálogo | Texto | Descripción ingresada por el usuario para identificar el conjunto |

**Reglas:**
- El registro del catálogo es una acción voluntaria ejecutada por el usuario
- El catálogo se persiste en base de datos SQLite local
- Sin un catálogo guardado, el Web Service (RF07) no tiene datos para exponer

---

## RF04 — Capturar datos de cada proceso

**Descripción:** Por cada proceso obtenido del sistema operativo, la aplicación debe registrar automáticamente la siguiente información:

| Campo | Tipo | Descripción |
|---|---|---|
| PID | Entero | Identificador único del proceso en el SO |
| Nombre | Texto | Nombre del ejecutable del proceso |
| Usuario | Texto | Usuario propietario que generó el proceso |
| Descripción | Texto | Descripción del proceso |
| Prioridad | Entero (0 ó 1) | Tipo de proceso según su origen |

**Regla de asignación de prioridad:**
- `1` = No Expulsivo → el proceso fue generado por el **sistema operativo**
- `0` = Expulsivo → el proceso fue generado por un **usuario**

---

## RF05 — Ejecutar actividad del proceso

**Descripción:** Cada proceso registrado debe tener asociada una actividad que consiste en crear un archivo de texto y escribir su descripción carácter por carácter, simulando el uso de la CPU.

**Reglas de ejecución:**
- Cada proceso corre en su propio **hilo (Thread)** independiente
- El hilo crea un archivo con el **nombre del proceso** como título
- Escribe la **descripción** del proceso dentro del archivo, un carácter a la vez
- Por cada carácter escrito, el proceso consume **1 quantum**
- El hilo espera **TH milisegundos** entre cada carácter escrito (TH definido por el usuario en la App 2)

**Fórmula del tiempo de ráfaga:**

TR = TH × (cantidad de caracteres de la descripción)

**Ejemplo:**
- Proceso: `firefox` | Descripción: `"Navegador web"` (13 caracteres) | TH: 500ms
- TR = 500 × 13 = 6500ms

---

## RF06 — Listar procesos capturados

**Descripción:** El sistema debe mostrar en la interfaz gráfica el conjunto completo de procesos del catálogo activo con todos sus datos.

**Datos visibles en la tabla:**
- PID
- Nombre
- Usuario
- Descripción
- Prioridad (Expulsivo / No Expulsivo)

**Reglas:**
- La tabla se actualiza cada vez que se obtienen nuevos procesos del SO
- Debe permitir visualizar todos los procesos del catálogo seleccionado

---

## RF07 — Exponer Web Service REST

**Descripción:** El sistema debe exponer un servicio web de tipo REST en formato **XML** que permita a la Aplicación Web (App 2) consumir los datos necesarios para ejecutar la simulación.

**Datos expuestos por el servicio:**
- Información completa de todos los procesos del catálogo activo
- Datos del algoritmo de planificación asociados a cada proceso

**Formato de respuesta:** XML obligatorio según especificación del laboratorio.

**Ejemplo de respuesta:**
```xml
<?xml version="1.0" encoding="UTF-8"?>
<catalogo>
    <numero>001</numero>
    <nombre>Procesos pesados abril</nombre>
    <procesos>
        <proceso>
            <pid>1234</pid>
            <nombre>firefox</nombre>
            <usuario>estudiante</usuario>
            <descripcion>Navegador web</descripcion>
            <prioridad>0</prioridad>
            <tiempoRafaga>6500</tiempoRafaga>
        </proceso>
    </procesos>
</catalogo>
```

---

## Resumen de requisitos

| ID | Nombre | Iniciado por |
|---|---|---|
| RF01 | Definir cantidad de procesos | Usuario |
| RF02 | Filtrar procesos por recurso | Usuario |
| RF03 | Registrar catálogo de procesos | Usuario |
| RF04 | Capturar datos de cada proceso | Sistema (automático) |
| RF05 | Ejecutar actividad del proceso | Sistema (automático tras iniciar simulación) |
| RF06 | Listar procesos capturados | Sistema (automático) |
| RF07 | Exponer Web Service REST | Sistema (servicio activo) |
