Práctica 0: Revisión de Algoritmos y Estructuras de Datos
Marco Morales Aguirre
Departamento Académico de Computación, Instituto Tecnológico Autónomo de México
1. Objetivos
Que las y los estudiantes:

Utilicen estructuras de datos para la solución de un problema práctico
Comparen el desempeño de estructuras de datos alternativas para el mismo problema
Practiquen los procesos de entrega de las prácticas de laboratorio

2. Problema
En el servicio de urgencias de un hospital público llegan, con alta frequencia, pacientes con distintos tipos de padecimientos. El administrador del hospital te pide desarrollar un sistema de Triage que decida quién es el siguiente paciente a atender basado en la prioridad asignada después de una revisión inicial.
Implementa el sistema de Triage de manera que:

La prioridad se establezca de manera numérica con 1 siendo el más urgente y 10 el menos urgente
Implementa el sistema con al menos dos de las siguientes estructuras de datos:

Heap (priority queue).
Lista ligada ordenada.
Árbol binario de búsqueda.


Compara el desempeño de tus implementaciones en las siguientes acciones simulando llegadas en tiempo real de 1000 pacientes con un tiempo entre arribos de 1 minuto (aleatorio con distribución uniforme)
• Insert
• Search
• Delete

3. Entrega
Deposita los siguientes entregables en la carpeta PR0 en tu repositorio de GitHub del curso.

Código fuente junto con una descripción de cada archivo, e instrucciones para compilarlo y ejecutarlo.
Gráficas del tiempo de ejecución de cada estructura de datos usada.
archivo README.md que contenga la descripción de cada archivo, instrucciones para compilarlo y ejecutarlo y una discusión que compare el análisis teórico del desempeño de las estructuras de datos con los resultados obtenidos en tus gráficas.