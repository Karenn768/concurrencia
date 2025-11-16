# Guía de Compilación y Ejecución de Ejemplos de Concurrencia

Este repositorio contiene ejemplos prácticos de problemas comunes en programación concurrente (Deadlock, Race Condition y Starvation), cada uno con una versión que demuestra el problema y otra que implementa una solución.

## Estructura del Repositorio

El proyecto está organizado en tres carpetas principales, una para cada problema de concurrencia:

```
/
├── deadlock/
│   ├── deadlock_con_problema/  # Demostración de un Deadlock
│   └── deadlock_con_solucion/  # Soluciones para el Deadlock (Ordenamiento y Algoritmo del Banquero)
├── race/
│   ├── race_con_problema/      # Demostración de una Race Condition
│   └── race_con_solucion/      # Soluciones para Race Condition (Mutex y Semáforos)
└── starvation/
    ├── starvation_con_problema/ # Demostración de Starvation
    └── starvation_con_solucion/ # Solución para Starvation (Aging)
```

## Instrucciones de Compilación y Ejecución

A continuación se detallan los comandos para compilar y ejecutar cada uno de los ejemplos. **Todos los comandos deben ejecutarse desde la raíz del proyecto (`/concurrencia/`)**.

---

### 1. Deadlock (Interbloqueo)

#### a) Deadlock con Problema

Este ejemplo simula transferencias bancarias concurrentes que llevan a un interbloqueo, haciendo que el programa se congele.

**Compilar:**
```bash
javac deadlock/deadlock_con_problema/Deadlock_con_problema.java
```

**Ejecutar:**
```bash
java deadlock.deadlock_con_problema.Deadlock_con_problema
```
**Resultado esperado:** El programa se bloqueará y no completará todas las transferencias.

---
#### b) Deadlock con Solución

Este archivo contiene dos algoritmos para resolver el deadlock:
1.  **Prevención (Ordenamiento de Recursos):** Los hilos adquieren los bloqueos de las cuentas en un orden numérico consistente, evitando el ciclo de espera.
2.  **Evitación (Algoritmo del Banquero):** Un "banquero" centralizado analiza si conceder un recurso (realizar una transferencia) dejará al sistema en un estado seguro.

**Compilar:**
```bash
javac deadlock/deadlock_con_solucion/Deadlock_solucion.java
```

**Ejecutar:**
```bash
java deadlock.deadlock_con_solucion.Deadlock_solucion
```
**Resultado esperado:** El programa ejecutará ambos algoritmos. En ambos casos, todas las transferencias se completarán exitosamente y el programa finalizará sin bloqueos.

---

### 2. Race Condition (Condición de Carrera)

#### a) Race Condition con Problema

Este ejemplo simula la venta y reabastecimiento concurrente de stock. Múltiples hilos leen y escriben en el mismo inventario sin sincronización, lo que resulta en un stock final incorrecto.

**Compilar:**
```bash
javac race/race_con_problema/Race_condition_con_problema.java
```

**Ejecutar:**
```bash
java race.race_con_problema.Race_condition_con_problema
```
**Resultado esperado:** El stock final de los productos será inconsistente y variará en cada ejecución. El valor esperado (120 para el producto 0) no se cumplirá.

---
#### b) Race Condition con Solución

Este archivo implementa y compara dos mecanismos de sincronización para resolver la condición de carrera:
1.  **Mutex (ReentrantLock):** Asegura que solo un hilo a la vez pueda modificar el stock de un producto específico.
2.  **Semáforos:** Cumplen una función similar al mutex en este caso, permitiendo el acceso exclusivo a la sección crítica.

**Compilar:**
```bash
javac race/race_con_solucion/Race_condition_solucion.java
```

**Ejecutar:**
```bash
java race.race_con_solucion.Race_condition_solucion
```
**Resultado esperado:** El programa ejecutará ambas versiones (Mutex y Semáforos). En ambos casos, el stock final será el correcto y consistente en todas las ejecuciones. Al final, se mostrará una comparación de rendimiento entre ambas soluciones.

---

### 3. Starvation (Inanición)

#### a) Starvation con Problema

Este ejemplo simula un sistema de procesamiento de tareas con tres prioridades (Alta, Media, Baja). Los consumidores siempre priorizan las tareas de tipo 'A' y 'M', lo que provoca que las tareas de tipo 'B' (Baja prioridad) nunca sean procesadas.

**Compilar:**
```bash
javac starvation/starvation_con_problema/StarvationConProblema.java
```

**Ejecutar:**
```bash
java starvation.starvation_con_problema.StarvationConProblema
```
**Resultado esperado:** El programa finalizará, pero quedarán muchas tareas de tipo 'B' en la cola sin procesar. Un monitor advertirá sobre la posible inanición.

---
#### b) Starvation con Solución

Para resolver la inanición, este ejemplo implementa la técnica de **Aging (Envejecimiento)**. La prioridad de las tareas aumenta a medida que envejecen en la cola, garantizando que incluso las tareas de baja prioridad eventualmente sean seleccionadas.

**Compilar:**
```bash
javac starvation/starvation_con_solucion/StarvationConSolucion.java
```

**Ejecutar:**
```bash
java starvation.starvation_con_solucion.StarvationConSolucion
```
**Resultado esperado:** Todas las tareas, incluidas las de tipo 'B', serán procesadas. El programa mostrará cómo la prioridad efectiva de las tareas 'B' aumenta con el tiempo, permitiéndoles competir con las de mayor prioridad inicial.
