# Guía Rápida de Uso - Starvation

## 🚀 Inicio Rápido

### Opción 1: Usar el Script Automatizado (Recomendado)

```powershell
cd "c:\Users\luise\OneDrive\Documentos\8vo semestre\SistemasOperativos\EntregablePrueba\Concurrencia\concurrencia\starvation"
.\ejecutar_demo.ps1
```

El script presenta un menú interactivo con opciones para ejecutar cualquiera de las versiones.

### Opción 2: Compilar y Ejecutar Manualmente

#### Versión CON Starvation (Problema)

```powershell
# Navegar a la carpeta
cd "c:\Users\luise\OneDrive\Documentos\8vo semestre\SistemasOperativos\EntregablePrueba\Concurrencia\concurrencia\starvation\starvation_con_problema"

# Compilar
javac StarvationConProblema.java

# Ejecutar
java StarvationConProblema
```

#### Versión SIN Starvation (Solución con Aging)

```powershell
# Navegar a la carpeta
cd "c:\Users\luise\OneDrive\Documentos\8vo semestre\SistemasOperativos\EntregablePrueba\Concurrencia\concurrencia\starvation\starvation_con_solucion"

# Compilar
javac StarvationConSolucion.java

# Ejecutar
java StarvationConSolucion
```

---

## 📊 Qué Observar en Cada Versión

### En la Versión CON Problema:

1. **Acumulación de Tareas B**: Verás mensajes como:
   ```
   *** MONITOR: Tareas tipo B en espera: 12 ***
   *** ¡ADVERTENCIA! POSIBLE STARVATION DETECTADA ***
   ```

2. **Tiempos de Espera Largos**: Las tareas B esperarán mucho tiempo:
   ```
   Consumidor-2 procesando Tarea#25[Baja] (esperó 8540 ms)
   ```

3. **Tareas Sin Procesar**: Al final verás tareas B que nunca se procesaron:
   ```
   Tareas Baja (B): 13  <-- Quedaron en la cola
   ```

### En la Versión CON Solución:

1. **Procesamiento Equitativo**: Todas las tareas se procesan:
   ```
   Total de tareas procesadas: 30
   - Tareas B: 18 (60%)  <-- Todas procesadas
   ```

2. **Prioridad Dinámica**: Verás cómo aumenta la prioridad con el tiempo:
   ```
   Consumidor-1 procesando Tarea#15[Baja,Prior:2.50] 
   (esperó 3000 ms, prioridad efectiva: 2.50)
   ```

3. **Tabla de Monitoreo**: Se muestra cada 2 segundos:
   ```
   ╔════════════════════════════════════════════════╗
   ║  TABLA DE MONITOREO TEMPORAL - 4 segundos      ║
   ║  Tareas A Procesadas: 3                        ║
   ║  Tareas M Procesadas: 7                        ║
   ║  Tareas B Procesadas: 12                       ║
   ╚════════════════════════════════════════════════╝
   ```

---

## 🎓 Análisis Comparativo

### Pregunta 1: ¿Tiempo de espera máximo para tareas B en versión CON starvation?

**Respuesta**: Más de 10 segundos (>10,000 ms). Algunas tareas nunca se procesan.

### Pregunta 2: ¿Cómo funciona el mecanismo anti-starvation?

**Algoritmo AGING**:
```
prioridad_efectiva = prioridad_base + (tiempo_espera / 1000) * 0.5

Ejemplo para una tarea B:
- En t=0s:  prioridad = 1.0
- En t=2s:  prioridad = 2.0
- En t=4s:  prioridad = 3.0 (iguala a tareas A)
- En t=6s:  prioridad = 4.0 (supera a tareas A)
```

### Pregunta 3: ¿Qué overhead introduce la solución?

**Overhead**: ~10-15% en tiempo total

- **CON problema**: ~10-11 segundos
- **SIN problema**: ~11-12 segundos

El overhead viene de:
- Calcular prioridad efectiva para cada tarea
- Buscar la tarea con mayor prioridad (O(n) donde n ≤ 20)

**Conclusión**: El overhead es mínimo y justificable porque garantiza que todas las tareas se procesen.

---

## 📝 Notas Importantes

1. **Ejecución Independiente**: Cada archivo es completamente independiente y se puede ejecutar por separado.

2. **Salida por Consola**: Toda la salida es por consola, no se generan archivos externos.

3. **Tiempo de Ejecución**: Cada programa ejecuta por aproximadamente 10-15 segundos.

4. **Buenas Prácticas**:
   - Código bien documentado con comentarios explicativos
   - Uso correcto de sincronización (`synchronized`, `wait()`, `notifyAll()`)
   - Manejo de interrupciones con `volatile` flags
   - Thread-safe con locks apropiados
   - Monitoreo en tiempo real del estado del sistema

5. **Requisitos**:
   - Java JDK 8 o superior
   - PowerShell (para el script de demostración)

---

## 🔍 Para Profundizar

Consulta el archivo `README_STARVATION.md` para:
- Explicación detallada de los conceptos
- Pseudocódigo completo
- Experimentos sugeridos
- Referencias bibliográficas

---

## 💡 Tips para la Demostración

1. **Ejecuta primero la versión CON problema** para ver claramente el starvation
2. **Observa los mensajes del monitor** que indican cuántas tareas B están esperando
3. **Luego ejecuta la versión CON solución** para ver cómo Aging resuelve el problema
4. **Compara las estadísticas finales** de ambas ejecuciones

---

**¡Listo para ejecutar!** 🎯
