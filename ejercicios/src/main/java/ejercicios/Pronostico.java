package ejercicio1;

public class Pronostico {
    public String localidad;
    public String fecha;
    public double temperatura;
    public int humedad;
    public int nubes;
    public double viento;
    public String tiempo;

    public Pronostico(String localidad, String fecha, double temperatura, int humedad, int nubes, double viento,
            String tiempo) {
        this.localidad = localidad;
        this.fecha = fecha;
        this.temperatura = temperatura - 273;
        this.humedad = humedad;
        this.nubes = nubes;
        this.viento = viento;
        this.tiempo = tiempo;
    }

    @Override
    public String toString() {
        return String.format("%s %s, %.2fºC, humedad: %d, nubes: %d, viento: %.2fm/s, clima: %s", this.localidad,
                this.fecha, this.temperatura, this.humedad, this.nubes, this.viento, this.tiempo);
    }

}
