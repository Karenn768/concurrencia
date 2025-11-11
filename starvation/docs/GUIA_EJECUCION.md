# 🚀 Guía de Ejecución - Starvation

## Ejecución Rápida

### ✅ Paso 1: Navegar a la raíz del proyecto

```powershell
cd "c:\Users\luise\OneDrive\Documentos\8vo semestre\SistemasOperativos\EntregablePrueba\Concurrencia\concurrencia"
```

### ✅ Paso 2: Ejecutar versión CON Starvation (Problema)

```powershell
# Compilar
javac starvation/starvation_con_problema/StarvationConProblema.java

# Ejecutar
java concurrencia.starvation.starvation_con_problema.StarvationConProblema
```

**Qué observarás:**
- ⚠️ Advertencias de starvation detectado
- 📊 Tareas tipo B acumulándose (>10 en espera)
- ⏱️ Tiempos de espera muy largos para tareas B (>10 segundos)
- ❌ Muchas tareas B sin procesar al final

**Salida esperada:**
```
*** MONITOR: Tareas tipo B en espera: 12 ***
*** ¡ADVERTENCIA! POSIBLE STARVATION DETECTADA ***

=== ESTADO DE LA COLA ===
Tareas Baja (B): 13  <-- Quedaron sin procesar
Máximo de tareas tipo B en espera: 15
*** PROBLEMA OBSERVADO: Las tareas tipo B sufren STARVATION ***
```

---

### ✅ Paso 3: Ejecutar versión SIN Starvation (Solución con Aging)

```powershell
# Compilar
javac starvation/starvation_con_solucion/StarvationConSolucion.java

# Ejecutar
java concurrencia.starvation.starvation_con_solucion.StarvationConSolucion
```

**Qué observarás:**
- ✅ Prioridad efectiva aumentando con el tiempo
- 📈 Todas las tareas se procesan eventualmente
- 📊 Tablas de monitoreo temporal cada 2 segundos
- ✅ Distribución equitativa de procesamiento

**Salida esperada:**
```
Consumidor-2 procesando Tarea#15[Baja,Prior:2.50] 
(esperó 3000 ms, prioridad efectiva: 2.50)

╔════════════════════════════════════════════════╗
║  TABLA DE MONITOREO TEMPORAL - 4 segundos      ║
║  Tareas B Procesadas: 12                       ║
╚════════════════════════════════════════════════╝

Total de tareas procesadas: 30
- Tareas B: 18 (60%)  <-- ¡Todas procesadas!
*** SOLUCIÓN EXITOSA: El mecanismo de AGING garantizó ***
*** que TODAS las tareas eventualmente se procesaran ***
```

---

## 📊 Comparación de Resultados

| Métrica | CON Starvation | SIN Starvation (Aging) |
|---------|----------------|------------------------|
| **Tareas B procesadas** | ~40% | ~60% (todas) |
| **Tareas B en espera máx** | 15+ | 2-3 |
| **Tiempo espera máximo** | >10,000ms | ~4,000ms |
| **Advertencias** | Múltiples | Ninguna |
| **Tareas sin procesar** | Sí (muchas) | No (todas procesadas) |

---

## 🎯 Conceptos Clave

### ¿Qué es Starvation?
**Starvation (Inanición)** ocurre cuando una tarea espera indefinidamente porque otras de mayor prioridad siempre tienen preferencia.

### Problema en versión 1:
```
Algoritmo que causa STARVATION:
1. Buscar tareas tipo A (alta prioridad)
2. Si no hay A, buscar tipo M (media prioridad)
3. Solo si no hay A ni M, tomar tipo B (baja prioridad)

Resultado: Las tareas B nunca se procesan si siempre llegan A o M
```

### Solución en versión 2:
```
Algoritmo AGING (Envejecimiento):
prioridad_efectiva = prioridad_base + (tiempo_espera / 1000) * 0.5

Ejemplo tarea B:
- t=0s:  prioridad = 1.0
- t=2s:  prioridad = 2.0
- t=4s:  prioridad = 3.0 (iguala a tareas A)
- t=6s:  prioridad = 4.0 (supera a tareas A)

Resultado: Las tareas antiguas aumentan su prioridad y 
eventualmente se procesan
```

---

## ⚙️ Características del Sistema

- **Cola compartida**: Capacidad 20 tareas
- **Tipos de tareas**:
  - Alta (A): Prioridad 3, Tiempo: 50ms, Producción: 10%
  - Media (M): Prioridad 2, Tiempo: 100ms, Producción: 30%
  - Baja (B): Prioridad 1, Tiempo: 150ms, Producción: 60%
- **5 threads productores**: Generan 30 tareas total
- **3 threads consumidores**: Procesan tareas de la cola
- **Monitoreo**: Cada 2 segundos muestra estadísticas

---

## 📝 Respuestas a Preguntas del Documento

### 1. ¿Tiempo de espera máximo para tareas B en versión CON starvation?
**Respuesta**: Más de 10 segundos (>10,000ms). Muchas tareas quedan sin procesar indefinidamente.

### 2. ¿Cómo funciona el mecanismo anti-starvation?
**Algoritmo AGING**:
- Cada tarea tiene prioridad base (A=3, M=2, B=1)
- La prioridad aumenta +0.5 cada 1000ms de espera
- Se selecciona siempre la tarea con mayor prioridad efectiva
- Garantiza que tareas antiguas eventualmente se procesen

### 3. ¿Qué overhead introduce la solución?
**Overhead**: ~10-15% en tiempo total
- **CON starvation**: ~10-11 segundos
- **SIN starvation**: ~11-12 segundos
- **Justificación**: El pequeño overhead es aceptable porque garantiza fairness

---

## 🔍 Archivos del Proyecto

```
starvation/
├── starvation_con_problema/
│   └── StarvationConProblema.java    (Versión CON starvation)
├── starvation_con_solucion/
│   └── StarvationConSolucion.java    (Versión SIN starvation - Aging)
├── GUIA_EJECUCION.md                 (Este archivo)
└── README_STARVATION.md              (Documentación completa)
```

---

## 💡 Requisitos

- Java JDK 8 o superior
- PowerShell (Windows)

---

**¡Listo para ejecutar!** 🎯
