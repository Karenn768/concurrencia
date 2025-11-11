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
    └── starvation_con_solucion/       # Solución a starvation
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
Situación donde un hilo nunca obtiene acceso a los recursos que necesita porque otros hilos con mayor prioridad o mejor timing los acaparan constantemente.

**Características:**
- El hilo está activo pero no progresa
- Acceso injusto a recursos
- Problemas de prioridad

**Ubicación:**
- Problema: `starvation/starvation_con_problema/`
- Solución: `starvation/starvation_con_solucion/`

## 🚀 Cómo Ejecutar el Proyecto

### Requisitos Previos
- Java Development Kit (JDK) 8 o superior
- Editor de código o IDE (Eclipse, IntelliJ IDEA, VS Code)

### Compilación y Ejecución

Para cada ejemplo, navega al directorio correspondiente y ejecuta:

```bash
# Compilar
javac App.java

# Ejecutar
java App
```

**Ejemplo para Deadlock:**
```bash
cd deadlock/deadlock_con_problema
javac App.java
java App
```

## 📚 Conceptos Técnicos Utilizados

### Mecanismos de Sincronización
- `synchronized` - Bloques y métodos sincronizados
- `Lock` y `ReentrantLock` - Control explícito de bloqueos
- `Semaphore` - Control de acceso a recursos limitados
- `wait()` y `notify()` - Coordinación entre hilos
- `volatile` - Visibilidad de variables entre hilos

### Patrones de Solución
- Ordenamiento de recursos
- Timeout en adquisición de locks
- Detección y recuperación de deadlocks
- Uso de estructuras thread-safe
- Políticas de scheduling justas

## 👥 Equipo de Desarrollo

Proyecto desarrollado por estudiantes de 8vo semestre - Sistemas Operativos

## 📝 Notas Importantes

- Cada carpeta con problema demuestra el error de forma intencional
- Las carpetas con solución muestran las mejores prácticas
- Se recomienda ejecutar primero los ejemplos con problema para observar el comportamiento
- Los ejemplos están simplificados con fines educativos

## 🔗 Referencias

- "Operating System Concepts" - Silberschatz, Galvin, Gagne
- Java Concurrency in Practice - Brian Goetz
- Documentación oficial de Java sobre Concurrency

## 📄 Licencia

Este proyecto es material educativo desarrollado para fines académicos.

---

**Universidad:** Universidad Pedagogica y Tecnologica de Colombia 
**Curso:** Sistemas Operativos  
**Semestre:** 8vo  
**Año:** 2025