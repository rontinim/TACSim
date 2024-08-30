public class MobileApplication {
    public static void main(String[] args) {
        String citta = "Imola";
        CityMapDownloader city = new CityMapDownloader();
        city.alternativeMain(citta);
        city.setTask(20);
        city.createTxt();
        System.out.println("Txt creato per applicazione");
        Experiment exp = new Experiment();
        exp.mobileApplicationMain();
        //faccio tentativo per accedere direttamente a dijkstra, perhè alla fine il file che passo al client app gli serve la distanza... stimando il tempo con una formula
        //calcolutaTest();
        //una velocità media di un auto in città è di 35km/h aumentiamo il tempo di un 20%
        //in caso che sia a piedi, cerchiamo di avere tre vie.... persona che cammina 5km/h aumentiamo il tempo di un 20%
        //creo tre metodi separati, volendo anche bici...  15km/h con aumento di tempo del 20%
    }

    public static void calcolutaTest(){
        Dijkstra dij = new Dijkstra();

        GPSCoordinate source;
        GPSCoordinate destination;

        source = new GPSCoordinate(44.3137522, 11.6311092);
        destination = new GPSCoordinate(44.3210736, 11.7475367);
        System.out.println(dij.calculateDistance(source, destination));

        source = new GPSCoordinate(44.3137522, 11.6311092);
        destination = new GPSCoordinate(44.3528834, 11.6930715);
        System.out.println(dij.calculateDistance(source, destination));

        source = new GPSCoordinate(44.3528834, 11.6930715);
        destination = new GPSCoordinate(44.3210736, 11.7475367);
        System.out.println(dij.calculateDistance(source, destination));

        source = new GPSCoordinate(44.3546937, 11.7130719);
        destination = new GPSCoordinate(44.3137522, 11.6311092);
        System.out.println(dij.calculateDistance(source, destination));

        source = new GPSCoordinate(44.3947573, 11.7012034);
        destination = new GPSCoordinate(44.3137522, 11.6311092);
        System.out.println(dij.calculateDistance(source, destination));

        source = new GPSCoordinate(44.3210736, 11.7475367);
        destination = new GPSCoordinate(44.3300191, 11.750367);
        System.out.println(dij.calculateDistance(source, destination));

        source = new GPSCoordinate(44.3546937, 11.7130719);
        destination = new GPSCoordinate(44.3300191, 11.750367);
        System.out.println(dij.calculateDistance(source, destination));

        source = new GPSCoordinate(44.4645374, 11.7451589);
        destination = new GPSCoordinate(44.3591627, 11.6786187);
        System.out.println(dij.calculateDistance(source, destination));

    }
}
