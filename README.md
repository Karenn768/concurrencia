# Guía Completa de Programación Concurrente en Java

<div align="center">

[![Java](https://img.shields.io/badge/Java-100%25-orange.svg)](https://www.java.com/)
[![License](https://img.shields.io/badge/License-Educational-blue.svg)]()
[![Contributors](https://img.shields.io/badge/Contributors-3-green.svg)](https://github.com/Karenn768/concurrencia/graphs/contributors)

**Ejemplos prácticos y soluciones a los problemas clásicos de concurrencia**

[Características](#características) • [Instalación](#instalación) • [Uso](#uso) • [Problemas Explicados](#problemas-explicados) • [Contribuir](#contribuir)

</div>

---

## Descripción

Este repositorio contiene implementaciones educativas completas de los **tres problemas clásicos de la programación concurrente**: Deadlock (Interbloqueo), Race Condition (Condición de Carrera) y Starvation (Inanición). Cada problema se presenta en dos versiones: una que demuestra el problema y otra que implementa soluciones efectivas utilizando técnicas avanzadas de sincronización en Java.

### Propósito

Proporcionar un recurso educativo completo para estudiantes y desarrolladores que deseen:
- Comprender los desafíos fundamentales de la programación concurrente
- Visualizar cómo ocurren estos problemas en escenarios reales
- Aprender técnicas prácticas de prevención y solución
- Experimentar con código funcional y bien documentado

---

## Características

- **Simulaciones Realistas**: Cada ejemplo utiliza escenarios del mundo real (transferencias bancarias, gestión de inventarios, procesamiento de tareas)
- **Logging Detallado**: Timestamps precisos y mensajes descriptivos para seguir la ejecución
- **Comparación de Soluciones**: Múltiples enfoques para resolver cada problema
- **Código Comentado**: Explicaciones claras de cada técnica utilizada
- **Métricas de Rendimiento**: Comparación de tiempos de ejecución y eficiencia

---

## Estructura del Proyecto

```
concurrencia/
│
├── deadlock/
│   ├── deadlock_con_problema/
│   │   └── Deadlock_con_problema.java         # Demostración del deadlock
│   └── deadlock_con_solucion/
│       └── Deadlock_solucion.java              # Soluciones: Ordenamiento + Banquero
│
├── race/
│   ├── race_con_problema/
│   │   └── Race_condition_con_problema.java    # Demostración de race condition
│   └── race_con_solucion/
│       └── Race_condition_solucion.java        # Soluciones: Mutex + Semáforos
│
├── starvation/
│   ├── starvation_con_problema/
│   │   └── StarvationConProblema.java          # Demostración de starvation
│   └── starvation_con_solucion/
│       └── StarvationConSolucion.java          # Solución: Aging (Envejecimiento)
│
├── README.md
└── README_compilación.txt
```

---

## Instalación

### Requisitos Previos

- **Java Development Kit (JDK)** 8 o superior
- Terminal o línea de comandos
- Editor de texto o IDE (opcional: IntelliJ IDEA, Eclipse, VS Code)

### Verificar Instalación de Java

```bash
java -version
javac -version
```

### Clonar el Repositorio

```bash
git clone https://github.com/Karenn768/concurrencia.git
cd concurrencia
```

---

## Uso

Todos los comandos deben ejecutarse desde la **raíz del proyecto** (`/concurrencia/`).

### 1. Deadlock (Interbloqueo)

#### Problema: Demostración de Deadlock

**Descripción**: Simula transferencias bancarias concurrentes donde dos hilos intentan transferir dinero entre las mismas cuentas en orden inverso, causando un bloqueo circular.

**Compilar:**
```bash
javac deadlock/deadlock_con_problema/Deadlock_con_problema.java
```

**Ejecutar:**
```bash
java deadlock.deadlock_con_problema.Deadlock_con_problema
```

**Resultado Esperado:**
- El programa se bloqueará indefinidamente
- Verás mensajes de hilos esperando bloqueos
- Transferencias incompletas (menos de 30/30)
- Mensaje de "DEADLOCK DETECTADO"

#### Solución: Prevención y Evitación de Deadlock

**Descripción**: Implementa dos algoritmos diferentes:
1. **Prevención (Ordenamiento de Recursos)**: Adquiere los locks siempre en el mismo orden (número de cuenta menor → mayor) para romper la condición de espera circular
2. **Evitación (Algoritmo del Banquero)**: Verifica que cada operación mantenga el sistema en un estado seguro antes de otorgar recursos

**Compilar:**
```bash
javac deadlock/deadlock_con_solucion/Deadlock_solucion.java
```

**Ejecutar:**
```bash
java deadlock.deadlock_con_solucion.Deadlock_solucion
```

**Resultado Esperado:**
- Todas las 30 transferencias completadas exitosamente
- Sin bloqueos ni timeouts
- Total de saldos conservado ($15,000)
- Comparación entre ambos algoritmos

**Conceptos Clave:**
- `ReentrantLock` con condiciones
- Estados seguros e inseguros
- Ordenamiento total de recursos
- Variables de condición (`await`/`signal`)

---

### 2. Race Condition (Condición de Carrera)

#### Problema: Demostración de Race Condition

**Descripción**: Simula un sistema de inventario donde múltiples hilos realizan ventas y reabastecimientos simultáneos sin sincronización, causando inconsistencias en el stock final.

**Compilar:**
```bash
javac race/race_con_problema/Race_condition_con_problema.java
```

**Ejecutar:**
```bash
java race.race_con_problema.Race_condition_con_problema
```

**Resultado Esperado:**
- Stock final inconsistente (varía en cada ejecución)
- Valor esperado: 1000, pero obtendrás valores incorrectos
- Advertencia de "RACE CONDITION DETECTADA"

#### Solución: Mutex y Semáforos

**Descripción**: Implementa y compara dos mecanismos de sincronización:
1. **Mutex (ReentrantLock)**: Exclusión mutua completa para acceso a sección crítica
2. **Semáforo**: Control de acceso mediante contadores

**Compilar:**
```bash
javac race/race_con_solucion/Race_condition_solucion.java
```

**Ejecutar:**
```bash
java race.race_con_solucion.Race_condition_solucion
```

**Resultado Esperado:**
- Stock final consistente: 1000 unidades en ambas soluciones
- Comparación de rendimiento entre Mutex y Semáforos
- Tiempos de ejecución medidos

**Conceptos Clave:**
- `ReentrantLock` (`lock()`/`unlock()`)
- `Semaphore` (`acquire()`/`release()`)
- Sección crítica
- Atomicidad de operaciones

---

### 3. Starvation (Inanición)

#### Problema: Demostración de Starvation

**Descripción**: Simula un sistema de procesamiento de tareas con prioridades donde las tareas de alta prioridad acaparan los recursos, dejando las tareas de baja prioridad sin procesar indefinidamente.

**Compilar:**
```bash
javac starvation/starvation_con_problema/StarvationConProblema.java
```

**Ejecutar:**
```bash
java starvation.starvation_con_problema.StarvationConProblema
```

**Resultado Esperado:**
- Tareas de alta prioridad procesadas completamente
- Tareas de baja prioridad sin procesar
- Advertencia de "INANICIÓN DETECTADA"
- Estadísticas mostrando desbalance

#### Solución: Aging (Envejecimiento)

**Descripción**: Implementa la técnica de **Aging**, que incrementa gradualmente la prioridad de las tareas que llevan mucho tiempo esperando, garantizando que eventualmente todas sean procesadas.

**Compilar:**
```bash
javac starvation/starvation_con_solucion/StarvationConSolucion.java
```

**Ejecutar:**
```bash
java starvation.starvation_con_solucion.StarvationConSolucion
```

**Resultado Esperado:**
- No ocurre starvation
- Prioridades aumentando con el tiempo
- Distribución equitativa de recursos
- Sin tareas rechazadas

**Conceptos Clave:**
- `PriorityBlockingQueue`
- Incremento dinámico de prioridad
- `Comparable` para ordenamiento personalizado
- Equidad en planificación

---

## Problemas Explicados

### ¿Qué es un Deadlock?

El **deadlock** ocurre cuando dos o más hilos se bloquean mutuamente esperando recursos que el otro posee, creando un ciclo de espera infinito.

**Condiciones necesarias (Coffman):**
1. Exclusión mutua
2. Retención y espera
3. No apropiación
4. Espera circular

**Soluciones implementadas:**
- **Prevención**: Romper la espera circular ordenando recursos
- **Evitación**: Algoritmo del Banquero para mantener estados seguros

---

### ¿Qué es una Race Condition?

Una **race condition** ocurre cuando el resultado de la ejecución depende del timing relativo de múltiples hilos accediendo a recursos compartidos sin sincronización adecuada.

**Ejemplo típico:**
```java
// Sin sincronización - INCORRECTO
stock = stock - cantidad;  // Thread 1
stock = stock + cantidad;  // Thread 2
// Resultado impredecible
```

**Soluciones:**
- Mutex para exclusión mutua
- Semáforos para control de acceso
- Variables atómicas

---

### ¿Qué es Starvation?

**Starvation** ocurre cuando un hilo no puede obtener acceso regular a recursos compartidos porque otros hilos monopolizan dichos recursos.

**Causas comunes:**
- Prioridades estáticas desequilibradas
- Algoritmos de planificación injustos
- Bloqueos prolongados

**Solución:**
- Aging: incremento gradual de prioridad con el tiempo de espera

---

## Conceptos Técnicos

### Mecanismos de Sincronización Utilizados

| Mecanismo | Uso | Ventajas | Desventajas |
|-----------|-----|----------|-------------|
| `synchronized` | Bloqueo implícito de objetos | Simple, integrado en Java | Menos flexible |
| `ReentrantLock` | Control explícito de locks | Trylock, timeouts, fairness | Requiere unlock manual |
| `Semaphore` | Limitar acceso concurrente | Múltiples permisos | Overhead adicional |
| `PriorityBlockingQueue` | Cola ordenada thread-safe | Ordenamiento automático | Costo de ordenamiento |
| `AtomicInteger` | Operaciones atómicas | Lock-free, rápido | Solo para enteros |

### Patrones de Concurrencia

1. **Ordenamiento de Recursos**: Adquirir locks en orden consistente
2. **Verificación de Estado Seguro**: Validar antes de otorgar recursos
3. **Timeout con Detección**: Detectar deadlocks mediante timeouts
4. **Aging Dinámico**: Ajustar prioridades según tiempo de espera

---

## Ejemplos de Ejecución

### Deadlock - Versión con Problema

```
[150.25ms] Thread-1 INTENTA bloquear cuenta 0
[150.30ms] Thread-1 BLOQUEÓ cuenta 0 | Ahora intenta bloquear cuenta 1
[152.15ms] Thread-2 INTENTA bloquear cuenta 1
[152.20ms] Thread-2 BLOQUEÓ cuenta 1 | Ahora intenta bloquear cuenta 0

DEADLOCK DETECTADO:
• Transferencias completadas: 15/30
• Threads bloqueados:
    - Thread-1 (Estado: BLOCKED)
    - Thread-2 (Estado: BLOCKED)
```

### Race Condition - Versión con Solución

```
RESUMEN - SOLUCIÓN CON MUTEX:
  Stock inicial: 1000
  Stock final: 1000 ✓
  Tiempo ejecución: 245ms

RESUMEN - SOLUCIÓN CON SEMÁFORO:
  Stock inicial: 1000
  Stock final: 1000 ✓
  Tiempo ejecución: 268ms
```

---

## Debugging y Herramientas

### Detectar Deadlocks

Si el programa se bloquea, puedes generar un thread dump:

```bash
# 1. Encuentra el PID del proceso Java
jps

# 2. Genera el thread dump
jstack <PID> > thread_dump.txt

# 3. Busca "BLOCKED" en el archivo
grep -A 5 "BLOCKED" thread_dump.txt
```

### Analizar Rendimiento

Para ver estadísticas de threads:

```bash
jconsole  # Interfaz gráfica
# o
jvisualvm # Profiler completo
```

---

## Contribuir

¡Las contribuciones son bienvenidas! Para contribuir:

1. **Fork** el repositorio
2. **Crea una rama** para tu feature:
   ```bash
   git checkout -b feature/nueva-solucion
   ```
3. **Commit** tus cambios:
   ```bash
   git commit -m "Agrega nueva solución para X"
   ```
4. **Push** a tu rama:
   ```bash
   git push origin feature/nueva-solucion
   ```
5. **Abre un Pull Request** describiendo tus cambios

### Ideas para Contribuir

- Implementar otros problemas clásicos (Filósofos Comensales, Productor-Consumidor)
- Agregar visualizaciones gráficas
- Implementar soluciones en otros lenguajes
- Mejorar documentación con diagramas
- Agregar pruebas unitarias

---

## Autores y Colaboradores

- [**Karenn768**](https://github.com/Karenn768) - Autor principal
- [**WBOK-GM**](https://github.com/WBOK-GM) (Walter Alfonso) - Colaborador
- [**Luisen1**](https://github.com/Luisen1) (Luisen Hernandez) - Colaborador

---

## Recursos Adicionales

### Documentación de Java

- [Java Concurrency Tutorial](https://docs.oracle.com/javase/tutorial/essential/concurrency/)
- [ReentrantLock API](https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/locks/ReentrantLock.html)
- [Semaphore API](https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/Semaphore.html)

### Lecturas Recomendadas

- *Java Concurrency in Practice* - Brian Goetz
- *Operating System Concepts* - Silberschatz, Galvin, Gagne
- *The Art of Multiprocessor Programming* - Herlihy, Shavit

---

## Licencia

Este proyecto es de código abierto con fines **educativos**. Libre para usar, modificar y distribuir con atribución.

---

## FAQ

**P: ¿Por qué el deadlock no siempre ocurre en la versión con problema?**  
R: El deadlock depende del timing de los hilos. Los sleeps fuerzan condiciones más probables, pero en sistemas rápidos puede no manifestarse en cada ejecución.

**P: ¿Cuál solución de race condition es mejor: Mutex o Semáforo?**  
R: Para exclusión mutua simple, Mutex (`ReentrantLock`) es más eficiente. Los semáforos son mejores cuando necesitas controlar acceso de múltiples hilos simultáneos (ej: pool de conexiones).

**P: ¿El aging puede causar inversión de prioridad?**  
R: Sí, es intencional. El aging permite que tareas originalmente de baja prioridad eventualmente superen a las de alta prioridad si han esperado suficiente tiempo.

---

<div align="center">

**⭐ Si este proyecto te ayudó, considera darle una estrella ⭐**

[Reportar Bug](https://github.com/Karenn768/concurrencia/issues) • [Solicitar Feature](https://github.com/Karenn768/concurrencia/issues) • [Hacer Pregunta](https://github.com/Karenn768/concurrencia/discussions)

</div>
