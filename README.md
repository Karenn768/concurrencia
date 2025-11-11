# Proyecto de Concurrencia - Sistemas Operativos

## 📋 Descripción del Proyecto

Este es un proyecto educativo desarrollado para el curso de **Sistemas Operativos** (8vo semestre) que demuestra los principales problemas de concurrencia en programación multihilo y sus soluciones. El proyecto está implementado en Java y cubre tres problemas clásicos de concurrencia:

- **Deadlock (Interbloqueo)**
- **Race Condition (Condición de Carrera)**
- **Starvation (Inanición)**

Cada problema incluye dos implementaciones:
1. **Con Problema**: Código que demuestra el problema de concurrencia
2. **Con Solución**: Código que implementa la solución correcta al problema

## 🎯 Objetivos de Aprendizaje

- Comprender los problemas comunes de concurrencia en sistemas operativos
- Identificar situaciones que pueden causar deadlock, race conditions y starvation
- Aprender técnicas y patrones para resolver estos problemas
- Aplicar conceptos de sincronización y gestión de recursos compartidos

## 📁 Estructura del Proyecto

```
concurrencia/
│
├── README.md                          # Este archivo
│
├── deadlock/                          # Ejemplos de Interbloqueo
│   ├── deadlock_con_problema/         # Código que genera deadlock
│   │   └── App.java
│   └── deadlock_con_solucion/         # Solución al deadlock
│
├── race/                              # Ejemplos de Condición de Carrera
│   ├── race_con_problema/             # Código con race condition
│   └── race_con_solucion/             # Solución a race condition
│
└── starvation/                        # Ejemplos de Inanición
    ├── starvation_con_problema/       # Código con starvation
    │   └── StarvationConProblema.java
    ├── starvation_con_solucion/       # Solución a starvation (Aging)
    │   └── StarvationConSolucion.java
    ├── docs/                          # Documentación completa
    │   ├── README_STARVATION.md       # Documentación detallada
    │   ├── GUIA_EJECUCION.md          # Guía de ejecución
    │   └── GUIA_RAPIDA.md             # Guía rápida
    ├── ejecutar_demo.ps1              # Script de demostración
    └── README.md                      # Índice del módulo
```

## 🔍 Problemas de Concurrencia Cubiertos

### 1. Deadlock (Interbloqueo)

**¿Qué es?**  
Situación donde dos o más hilos quedan bloqueados permanentemente esperando recursos que están siendo retenidos por otros hilos.

**Condiciones para Deadlock:**
- Exclusión mutua
- Retención y espera
- No apropiación
- Espera circular

**Ubicación:**
- Problema: `deadlock/deadlock_con_problema/`
- Solución: `deadlock/deadlock_con_solucion/`

### 2. Race Condition (Condición de Carrera)

**¿Qué es?**  
Situación donde el resultado del programa depende del orden de ejecución de los hilos, causando resultados impredecibles cuando múltiples hilos acceden a recursos compartidos sin sincronización adecuada.

**Problemas comunes:**
- Lecturas y escrituras no atómicas
- Inconsistencia de datos
- Resultados no determinísticos

**Ubicación:**
- Problema: `race/race_con_problema/`
- Solución: `race/race_con_solucion/`

### 3. Starvation (Inanición)

**¿Qué es?**  
Situación donde un hilo o tarea espera indefinidamente para obtener acceso a los recursos que necesita porque otros hilos con mayor prioridad constantemente tienen preferencia. A diferencia del deadlock, en starvation el sistema hace progreso, pero algunas tareas nunca obtienen los recursos necesarios.

**Características:**
- El hilo está activo pero no progresa
- Acceso injusto a recursos compartidos
- Problemas de priorización inadecuada
- Algunas tareas nunca se ejecutan

**Implementación:**
- **Sistema de procesamiento de tareas** con cola compartida
- **3 tipos de tareas**: Alta (A), Media (M), Baja (B)
- **5 threads productores**: Generan 30 tareas con distribución 60% B, 30% M, 10% A
- **3 threads consumidores**: Procesan tareas de la cola
- **Monitoreo en tiempo real**: Muestra estado del sistema cada 2 segundos

**Problema Demostrado:**
- Política de priorización estricta (siempre A > M > B)
- Tareas de baja prioridad (B) sufren inanición
- Tiempo de espera superior a 10 segundos para tareas B
- Muchas tareas B quedan sin procesar

**Solución Implementada: AGING (Envejecimiento)**
```
prioridad_efectiva = prioridad_base + (tiempo_espera / 1000) * 0.5
```
- La prioridad aumenta gradualmente con el tiempo de espera
- Garantiza que todas las tareas eventualmente se procesen
- Overhead del 10-15% en tiempo total
- Sistema justo y equitativo

**Métricas de Comparación:**

| Métrica | CON Starvation | SIN Starvation (Aging) |
|---------|----------------|------------------------|
| Tareas B procesadas | ~40% | ~60% (todas) |
| Tareas B en espera máx | 15+ tareas | 2-3 tareas |
| Tiempo espera máximo | >10,000ms | ~4,000ms |
| Tareas sin procesar | Sí | No |

**Ubicación:**
- Problema: `starvation/starvation_con_problema/StarvationConProblema.java`
- Solución: `starvation/starvation_con_solucion/StarvationConSolucion.java`
- Documentación: `starvation/docs/`
- Script demo: `starvation/ejecutar_demo.ps1`

**Ejecución Rápida:**
```powershell
# Desde la raíz del proyecto
cd concurrencia

# Versión CON Starvation
javac starvation/starvation_con_problema/StarvationConProblema.java
java concurrencia.starvation.starvation_con_problema.StarvationConProblema

# Versión SIN Starvation (Aging)
javac starvation/starvation_con_solucion/StarvationConSolucion.java
java concurrencia.starvation.starvation_con_solucion.StarvationConSolucion
```

📖 **Ver documentación completa**: [`starvation/docs/README_STARVATION.md`](starvation/docs/README_STARVATION.md)

## 🚀 Cómo Ejecutar el Proyecto

### Requisitos Previos
- Java Development Kit (JDK) 8 o superior
- Editor de código o IDE (Eclipse, IntelliJ IDEA, VS Code)
- PowerShell (para scripts de demostración)

### Compilación y Ejecución

#### Opción 1: Ejecución Manual

Para cada ejemplo, navega al directorio raíz del proyecto y ejecuta:

```bash
# Desde la raíz del proyecto concurrencia/
cd "c:\Users\luise\OneDrive\Documentos\8vo semestre\SistemasOperativos\EntregablePrueba\Concurrencia\concurrencia"

# Compilar
javac <ruta_al_modulo>/<archivo>.java

# Ejecutar
java <paquete>.<clase>
```

**Ejemplo para Starvation:**
```powershell
# Navegar a la raíz
cd concurrencia

# Versión CON problema
javac starvation/starvation_con_problema/StarvationConProblema.java
java concurrencia.starvation.starvation_con_problema.StarvationConProblema

# Versión CON solución
javac starvation/starvation_con_solucion/StarvationConSolucion.java
java concurrencia.starvation.starvation_con_solucion.StarvationConSolucion
```

#### Opción 2: Script Automatizado (Solo Starvation)

```powershell
cd starvation
.\ejecutar_demo.ps1
```

El script presenta un menú interactivo para:
- Ejecutar versión CON starvation
- Ejecutar versión SIN starvation
- Ejecutar ambas versiones para comparación
- Solo compilar ambas versiones

## 📚 Conceptos Técnicos Utilizados

### Mecanismos de Sincronización
- `synchronized` - Bloques y métodos sincronizados
- `Lock` y `ReentrantLock` - Control explícito de bloqueos
- `Semaphore` - Control de acceso a recursos limitados
- `wait()` y `notify()/notifyAll()` - Coordinación entre hilos
- `volatile` - Visibilidad de variables entre hilos

### Patrones de Solución
- **Ordenamiento de recursos** - Prevención de deadlock
- **Timeout en adquisición de locks** - Detección de deadlock
- **Detección y recuperación de deadlocks** - Grafos de asignación
- **Uso de estructuras thread-safe** - Prevención de race conditions
- **Políticas de scheduling justas** - Prevención de starvation
- **AGING (Envejecimiento)** - Incremento gradual de prioridad
- **Prioridad dinámica** - Ajuste según tiempo de espera

### Estructuras de Datos Concurrentes
- `Queue<T>` con sincronización manual
- `LinkedList<T>` protegida con locks
- `ArrayList<T>` con acceso sincronizado
- Colas compartidas con capacidad limitada

## 🎓 Características Implementadas por Módulo

### ✅ Starvation (Completamente Implementado)
- ✅ Sistema de cola compartida (capacidad 20 tareas)
- ✅ 3 tipos de tareas con diferentes prioridades
- ✅ 5 threads productores y 3 consumidores
- ✅ Monitoreo en tiempo real cada 2 segundos
- ✅ Implementación de algoritmo AGING
- ✅ Tablas de monitoreo temporal
- ✅ Estadísticas y métricas detalladas
- ✅ Documentación completa en carpeta `docs/`
- ✅ Script PowerShell de demostración
- ✅ Código bien documentado con comentarios explicativos

### ⏳ Deadlock (Pendiente de Implementación)
- Estado: Por implementar

### ⏳ Race Condition (Pendiente de Implementación)
- Estado: Por implementar

## 👥 Equipo de Desarrollo

Proyecto desarrollado por estudiantes de 8vo semestre - Sistemas Operativos

### Estado del Proyecto

| Módulo | Estado | Completitud |
|--------|--------|-------------|
| **Starvation** | ✅ Completado | 100% |
| **Deadlock** | ⏳ Pendiente | 0% |
| **Race Condition** | ⏳ Pendiente | 0% |

### Progreso General: 33% (1/3 módulos completados)

## 📝 Notas Importantes

- Cada carpeta con problema demuestra el error de forma intencional
- Las carpetas con solución muestran las mejores prácticas
- Se recomienda ejecutar primero los ejemplos con problema para observar el comportamiento
- Los ejemplos están simplificados con fines educativos

### Notas Específicas de Starvation

- **Ejecución desde raíz**: Los archivos de starvation usan paquetes, por lo que deben ejecutarse desde la raíz del proyecto
- **Archivos independientes**: Cada versión (con/sin starvation) es completamente independiente
- **Salida por consola**: Toda la información se muestra en terminal, no se generan archivos externos
- **Tiempo de ejecución**: Cada programa ejecuta aproximadamente 10-15 segundos
- **Monitoreo**: Observa los mensajes del monitor que aparecen cada 2 segundos
- **Documentación**: Consulta `starvation/docs/` para información detallada

### Respuestas a Preguntas Frecuentes (Starvation)

1. **¿Tiempo de espera máximo para tareas B en versión CON starvation?**
   - Respuesta: Más de 10 segundos (>10,000ms). Muchas tareas quedan sin procesar.

2. **¿Cómo funciona el mecanismo anti-starvation?**
   - Respuesta: AGING - La prioridad aumenta +0.5 cada 1000ms de espera, garantizando que todas las tareas se procesen.

3. **¿Qué overhead introduce la solución?**
   - Respuesta: ~10-15% en tiempo total, justificado por garantizar fairness del sistema.

## 🔗 Referencias

- "Operating System Concepts" - Silberschatz, Galvin, Gagne (Capítulos 5-7: Process Synchronization, Deadlocks, Scheduling)
- "Java Concurrency in Practice" - Brian Goetz
- "Modern Operating Systems" - Andrew S. Tanenbaum (Capítulo 2: Processes and Threads)
- Documentación oficial de Java sobre Concurrency (`java.util.concurrent`)
- "The Art of Multiprocessor Programming" - Maurice Herlihy, Nir Shavit

## 📖 Documentación Adicional

### Starvation
- [Documentación Completa de Starvation](starvation/docs/README_STARVATION.md)
- [Guía de Ejecución Starvation](starvation/docs/GUIA_EJECUCION.md)
- [Guía Rápida Starvation](starvation/docs/GUIA_RAPIDA.md)

### Deadlock y Race Condition
- Documentación pendiente (por implementar)

## 📄 Licencia

Este proyecto es material educativo desarrollado para fines académicos.

---

**Universidad:** Universidad Pedagogica y Tecnologica de Colombia 
**Curso:** Sistemas Operativos  
**Semestre:** 8vo  
**Año:** 2025