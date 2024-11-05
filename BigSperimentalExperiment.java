import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Scanner;
import java.time.Instant;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.io.IOException;

public class BigSperimentalExperiment {
//il min della ram la prendo da qua..
    public static void main(String[] args) throws Exception {
        MemoryUsageTracker memoryTracker = MemoryUsageTracker.getInstance();
        long lowerMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        Instant start = Instant.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy - HH:mm:ss");
        String startDate = formatter.format(ZonedDateTime.now());
        MemoryUsageTracker memo = MemoryUsageTracker.getInstance();

        Scanner scanner = new Scanner(System.in);
        String[] cities = new String[5];
        int rep = 1;
        int contCsv = 1;
        System.out.println("Da che numero vuoi inziare a salvare i csv? (default 1)");
        String inputNumber = scanner.nextLine();
        if (!inputNumber.isEmpty()) {
            contCsv = Integer.parseInt(inputNumber);
        }
        System.out.println("Quante simulazioni vuoi ripetere? (default 1)");
        String input = scanner.nextLine();
        if (!input.isEmpty()) {
            rep = Integer.parseInt(input);
            rep = rep + contCsv;
        }
        
        System.out.println("Inserisci i nomi di 5 città:");
        for (int i = 0; i < cities.length; i++) {
            System.out.print("Città " + (i + 1) + ": ");
            cities[i] = scanner.nextLine();
        }
        scanner.close();
        String lastCity = "";
        long tempoSimulazioneCache1 = 0;
        long tempoSimulazioneCache2 = 0;
        CityMapDownloader mapDownloader = new CityMapDownloader();
        for(int r=contCsv; r < rep; r++){
            for (String city : cities) {
                System.out.println("Elaborazione della città: " + city);
                Instant cityStart = Instant.now();
                boolean hasWrittenTasksForCity = false;

                if (!city.equals(lastCity)) {                
                    try (PrintWriter outSimEssence = new PrintWriter(new FileWriter("SimulationsEssence.txt", true))) {
                        outSimEssence.println("  ");
                        outSimEssence.println("Città corrente: " + city);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                
                    lastCity = city;
                }

                mapDownloader.alternativeMain(city);
                Experiment experiment = new Experiment();
                for(int i = 80; i < 110; i = i + 40){//20-110-20
                //for(int i = 20; i < 25; i = i + 1){
                    mapDownloader.setTask(i);
                    mapDownloader.createTxt();
                        hasWrittenTasksForCity = false;
                        int method = 1;
                            if (!hasWrittenTasksForCity) {
                                try (PrintWriter outSimEssence = new PrintWriter(new FileWriter("SimulationsEssence.txt", true))) {
                                    outSimEssence.println("Numero di Task: " + i);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                hasWrittenTasksForCity = true;
                            }
                            FileWriter fw = new FileWriter("Sperimentation.txt", true);
                            PrintWriter resultWriter = new PrintWriter(fw);
                            resultWriter.print("Città: " + city + ", Task eseguiti: " + i);
                            resultWriter.print(", --- Utilizzo Cache Con Cacolo Preciso Delle Distanze ---");                       
                            Graph_Dijkstra graph = GraphPersistanSingleton.getInstance();
                            int nodeCount = graph.getNodeCount();
                            int edgeCount = graph.getEdgeCount();

                            resultWriter.print(", Numero di Nodi: " + nodeCount + ", Numero di Archi: " + edgeCount);
                            resultWriter.println();
                            resultWriter.close();

                            
                            experiment.alternativeMain(i, city, method, r);
                            Instant endSimulazione = Instant.now();
                            Duration durataSimulazione = Duration.between(start, endSimulazione);
                            tempoSimulazioneCache1 = durataSimulazione.toMillis();     
                }                
                GraphPersistanSingleton.resetInstance();
                Instant cityEnd = Instant.now();
                Duration cityDuration = Duration.between(cityStart, cityEnd);
                long cityHours = cityDuration.toHours();
                long cityMinutes = cityDuration.toMinutes() % 60;
                long citySeconds = cityDuration.getSeconds() % 60;
                FileWriter fwCityTime = new FileWriter("Sperimentation.txt", true);
                PrintWriter timeWriter = new PrintWriter(fwCityTime);
                timeWriter.printf("Tempo impiegato per elaborare la città %s: %02d:%02d:%02d\n", city, cityHours, cityMinutes, citySeconds);
                timeWriter.close();
                Graph gp = new Graph();
                try (PrintWriter outSimEssence = new PrintWriter(new FileWriter("SimulationsEssence.txt", true))) {
                    outSimEssence.printf(" Minimo Ram utilizzata all'avvio del processo: " + (lowerMemory/(1024.0 * 1024.0)) + "Mb ");
                    outSimEssence.printf(" Max Ram Utilizzata per il calcolo delle distanze: " + (gp.getMaxRamForDistance()/ (1024.0 * 1024.0)) + "Mb ");
                    outSimEssence.printf(" Max Ram utilizzata per il download della mappa: " + (mapDownloader.getMaxMemoryUsedForDownload()/ (1024.0 * 1024.0)) + "Mb ");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                lowerMemory = 0;
                gp.clearMaxRam();
                mapDownloader.clearMaxRam();
            }
            
            Instant end = Instant.now();
            Duration duration = Duration.between(start, end);
            long hours = duration.toHours();
            long minutes = duration.toMinutes() % 60;
            long seconds = duration.getSeconds() % 60;

            FileWriter fw = new FileWriter("Sperimentation.txt", true);
            PrintWriter resultWriter = new PrintWriter(fw);
            resultWriter.println("\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\");
            resultWriter.printf("Tempo: %02d:%02d:%02d", hours, minutes, seconds);
            resultWriter.println("   Data: " + startDate);
            resultWriter.println("\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\");
            resultWriter.close();
        }
    }
}
