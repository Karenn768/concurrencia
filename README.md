# Guía Completa de Ejemplos de Concurrencia

Este repositorio contiene ejemplos prácticos y detallados sobre problemas comunes en programación concurrente, enfocados en ilustrar y resolver tres problemas clásicos: Deadlock (Interbloqueo), Race Condition (Condición de Carrera) y Starvation (Inanición).

## Propósito del Proyecto

El objetivo principal es ofrecer un recurso educativo para estudiantes y desarrolladores que deseen comprender mejor los desafíos y soluciones prácticas en la concurrencia mediante simulaciones claras y código funcional en Java.

## Estructura del Repositorio

El proyecto está organizado en tres carpetas principales, cada una destinada a un problema concurrente específico:

```
/
├── deadlock/
│   ├── deadlock_con_problema/  # Implementación que demuestra un Deadlock
│   └── deadlock_con_solucion/  # Soluciones para evitar y prevenir Deadlock (Ordenamiento y Algoritmo del Banquero)
├── race/
│   ├── race_con_problema/      # Ejemplo que presenta una Race Condition
│   └── race_con_solucion/      # Soluciones con Mutex y Semáforos
└── starvation/
    ├── starvation_con_problema/ # Simulación de Starvation
    └── starvation_con_solucion/ # Solución usando técnica de Aging (Envejecimiento)
```

## Detalle de Problemas y Soluciones

### Deadlock (Interbloqueo)

- **Problema:** Simula transferencias bancarias simultáneas que pueden quedar en espera circular y provocar bloqueo total del programa.
- **Solución:** Se usan dos mecanismos:
  - Ordenamiento de recursos para prevenir ciclos en adquisición de locks.
  - Algoritmo del Banquero para evitar estados inseguros durante las transferencias.

### Race Condition (Condición de Carrera)

- **Problema:** Venta y reabastecimiento concurrente de inventario sin sincronización, lo que causa inconsistencias en el stock final.
- **Solución:** Uso de mecanismos de sincronización:
  - Mutex (ReentrantLock) para acceso exclusivo a recursos.
  - Semáforos para controlar acceso a sección crítica.

### Starvation (Inanición)

- **Problema:** Procesamiento de tareas con prioridades donde las de baja prioridad pueden nunca ser atendidas.
- **Solución:** Técnica de Aging que incrementa la prioridad con el tiempo para evitar inanición.

## Tecnologías Utilizadas

- Lenguaje: Java 100%
- Herramientas: compilación y ejecución con JDK estándar

## Instrucciones de Compilación y Ejecución

Todos los comandos se deben ejecutar desde la raíz del proyecto (`/concurrencia/`).

---

### 1. Deadlock (Interbloqueo)

#### a) Deadlock con Problema

**Compilar:**
```bash
javac deadlock/deadlock_con_problema/Deadlock_con_problema.java
```

**Ejecutar:**
```bash
java deadlock.deadlock_con_problema.Deadlock_con_problema
```

**Resultado esperado:** El programa se bloqueará y no completará las transferencias.

---
#### b) Deadlock con Solución

**Compilar:**
```bash
javac deadlock/deadlock_con_solucion/Deadlock_solucion.java
```

**Ejecutar:**
```bash
java deadlock.deadlock_con_solucion.Deadlock_solucion
```

**Resultado esperado:** Ambas soluciones completan exitosamente las transferencias sin bloqueo.

---

### 2. Race Condition (Condición de Carrera)

#### a) Race Condition con Problema

**Compilar:**
```bash
javac race/race_con_problema/Race_condition_con_problema.java
```

**Ejecutar:**
```bash
java race.race_con_problema.Race_condition_con_problema
```

**Resultado esperado:** Stock final inconsistente y variable en cada ejecución.

---
#### b) Race Condition con Solución

**Compilar:**
```bash
javac race/race_con_solucion/Race_condition_solucion.java
```

**Ejecutar:**
```bash
java race.race_con_solucion.Race_condition_solucion
```

**Resultado esperado:** Stock consistente y correcto, con comparación de rendimiento entre Mutex y Semáforos.

---

### 3. Starvation (Inanición)

#### a) Starvation con Problema

**Compilar:**
```bash
javac starvation/starvation_con_problema/StarvationConProblema.java
```

**Ejecutar:**
```bash
java starvation.starvation_con_problema.StarvationConProblema
```

**Resultado esperado:** Tareas de baja prioridad no procesadas y advertencia de inanición.

---
#### b) Starvation con Solución

**Compilar:**
```bash
javac starvation/starvation_con_solucion/StarvationConSolucion.java
```

**Ejecutar:**
```bash
java starvation.starvation_con_solucion.StarvationConSolucion
```

**Resultado esperado:** Todas las tareas procesadas, con prioridades aumentando en tiempo.

---

## Cómo Contribuir

1. Clona el proyecto.
2. Crea una rama para tus mejoras.
3. Realiza cambios y pruebas.
4. Envía un pull request describiendo tus propuestas.

## Créditos

Autores originales y colaboradores:
- Karenn768
- WBOK-GM (Walter Alfonso)
- Luisen1 (Luisen Hernandez)

---

Este proyecto tiene fines educativos para entender y resolver problemas de concurrencia en Java.
