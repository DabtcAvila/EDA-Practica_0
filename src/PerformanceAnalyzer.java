import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

public class PerformanceAnalyzer {
    private static final int TOTAL_PATIENTS = 1000;
    private static final long AVG_ARRIVAL_INTERVAL = 60000; // 1 minuto en ms
    private static final Random random = new Random(42); // Seed fija para reproducibilidad
    
    public static void main(String[] args) {
        System.out.println("=== Análisis de Rendimiento del Sistema de Triage ===\n");
        
        try {
            FileWriter csvWriter = new FileWriter("performance_results.csv");
            csvWriter.append("operation,structure,time_ms,patient_count,avg_time_per_op_ns\n");
            
            // Escenario 1: Solo inserciones
            System.out.println("--- Escenario 1: Solo Inserciones (1000 pacientes) ---");
            runInsertOnlyScenario(csvWriter);
            
            // Escenario 2: Patrón realista
            System.out.println("\n--- Escenario 2: Patrón Realista (70% insert, 20% extract, 10% search) ---");
            runRealisticScenario(csvWriter);
            
            csvWriter.close();
            System.out.println("\nResultados guardados en performance_results.csv");
            
        } catch (IOException e) {
            System.err.println("Error escribiendo archivo CSV: " + e.getMessage());
        }
    }
    
    private static void runInsertOnlyScenario(FileWriter csvWriter) throws IOException {
        Patient[] patients = generateRealisticPatients(TOTAL_PATIENTS);
        
        // Probar MinHeap
        testInsertOnly("MinHeap", new MinHeapTriage(), patients, csvWriter);
        
        // Probar SortedLinkedList
        testInsertOnly("SortedLinkedList", new SortedLinkedListTriage(), patients, csvWriter);
        
        System.gc(); // Forzar garbage collection entre pruebas
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    private static void testInsertOnly(String structureName, TriageSystem system, 
                                     Patient[] patients, FileWriter csvWriter) throws IOException {
        System.out.println("Probando " + structureName + "...");
        
        Runtime runtime = Runtime.getRuntime();
        long memoryBefore = runtime.totalMemory() - runtime.freeMemory();
        
        long startTime = System.nanoTime();
        
        for (Patient patient : patients) {
            system.insert(patient);
        }
        
        long endTime = System.nanoTime();
        long totalTime = endTime - startTime;
        long avgTimePerOp = totalTime / patients.length;
        
        long memoryAfter = runtime.totalMemory() - runtime.freeMemory();
        long memoryUsed = memoryAfter - memoryBefore;
        
        double totalTimeMs = totalTime / 1_000_000.0;
        
        System.out.printf("  %s - Inserción total: %.2f ms (%.2f ns/op), Memoria: %d KB%n", 
                         structureName, totalTimeMs, (double)avgTimePerOp, memoryUsed / 1024);
        
        // Escribir al CSV
        csvWriter.append(String.format("insert,%s,%.6f,%d,%d\n", 
                        structureName, totalTimeMs, patients.length, avgTimePerOp));
    }
    
    private static void runRealisticScenario(FileWriter csvWriter) throws IOException {
        Patient[] patients = generateRealisticPatients(TOTAL_PATIENTS);
        
        // Probar MinHeap
        testRealisticPattern("MinHeap", new MinHeapTriage(), patients, csvWriter);
        
        // Probar SortedLinkedList
        testRealisticPattern("SortedLinkedList", new SortedLinkedListTriage(), patients, csvWriter);
    }
    
    private static void testRealisticPattern(String structureName, TriageSystem system, 
                                           Patient[] patients, FileWriter csvWriter) throws IOException {
        System.out.println("Probando " + structureName + " con patrón realista...");
        
        long insertTime = 0, extractTime = 0, searchTime = 0;
        int insertCount = 0, extractCount = 0, searchCount = 0;
        
        // Pre-llenar con algunos pacientes para poder hacer extract y search
        for (int i = 0; i < 100; i++) {
            system.insert(patients[i]);
        }
        
        Runtime runtime = Runtime.getRuntime();
        long memoryBefore = runtime.totalMemory() - runtime.freeMemory();
        
        for (int i = 100; i < patients.length; i++) {
            double operation = random.nextDouble();
            
            if (operation < 0.7) { // 70% insert
                long start = System.nanoTime();
                system.insert(patients[i]);
                insertTime += System.nanoTime() - start;
                insertCount++;
                
            } else if (operation < 0.9 && !system.isEmpty()) { // 20% extract
                long start = System.nanoTime();
                system.extractMin();
                extractTime += System.nanoTime() - start;
                extractCount++;
                
            } else { // 10% search
                int searchId = random.nextInt(i) + 1;
                long start = System.nanoTime();
                system.search(searchId);
                searchTime += System.nanoTime() - start;
                searchCount++;
            }
        }
        
        long memoryAfter = runtime.totalMemory() - runtime.freeMemory();
        long memoryUsed = memoryAfter - memoryBefore;
        
        // Mostrar resultados
        System.out.printf("  %s - Operaciones realizadas:%n", structureName);
        if (insertCount > 0) {
            double avgInsert = insertTime / (double) insertCount;
            System.out.printf("    Insert: %d ops, %.2f ms total, %.2f ns/op%n", 
                             insertCount, insertTime / 1_000_000.0, avgInsert);
            csvWriter.append(String.format("insert,%s,%.6f,%d,%.2f\n", 
                            structureName, insertTime / 1_000_000.0, insertCount, avgInsert));
        }
        
        if (extractCount > 0) {
            double avgExtract = extractTime / (double) extractCount;
            System.out.printf("    Extract: %d ops, %.2f ms total, %.2f ns/op%n", 
                             extractCount, extractTime / 1_000_000.0, avgExtract);
            csvWriter.append(String.format("extract,%s,%.6f,%d,%.2f\n", 
                            structureName, extractTime / 1_000_000.0, extractCount, avgExtract));
        }
        
        if (searchCount > 0) {
            double avgSearch = searchTime / (double) searchCount;
            System.out.printf("    Search: %d ops, %.2f ms total, %.2f ns/op%n", 
                             searchCount, searchTime / 1_000_000.0, avgSearch);
            csvWriter.append(String.format("search,%s,%.6f,%d,%.2f\n", 
                            structureName, searchTime / 1_000_000.0, searchCount, avgSearch));
        }
        
        System.out.printf("    Memoria utilizada: %d KB%n", memoryUsed / 1024);
    }
    
    private static Patient[] generateRealisticPatients(int count) {
        Patient[] patients = new Patient[count];
        long baseTime = System.currentTimeMillis();
        
        for (int i = 0; i < count; i++) {
            // Distribución realista de prioridades (más casos leves)
            int priority = generateRealisticPriority();
            
            // Tiempo entre arribos exponencial con media de 1 minuto
            long arrivalTime = baseTime + (long)(generateExponentialInterval(AVG_ARRIVAL_INTERVAL) * i);
            
            String name = "Paciente" + (i + 1);
            patients[i] = new Patient(i + 1, priority, arrivalTime, name);
        }
        
        return patients;
    }
    
    private static int generateRealisticPriority() {
        // Distribución realista: más casos menos urgentes
        double rand = random.nextDouble();
        
        if (rand < 0.05) return 1; // 5% crítico
        if (rand < 0.15) return 2; // 10% muy urgente
        if (rand < 0.25) return 3; // 10% urgente
        if (rand < 0.35) return 4; // 10% semi-urgente
        if (rand < 0.50) return 5; // 15% normal
        if (rand < 0.65) return 6; // 15% menos urgente
        if (rand < 0.75) return 7; // 10% leve
        if (rand < 0.85) return 8; // 10% muy leve
        if (rand < 0.95) return 9; // 10% mínimo
        return 10; // 5% no urgente
    }
    
    private static double generateExponentialInterval(long mean) {
        // Distribución exponencial para intervalos entre arribos
        return -mean * Math.log(1 - random.nextDouble());
    }
}