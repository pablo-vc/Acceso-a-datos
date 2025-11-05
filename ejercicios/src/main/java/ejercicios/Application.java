
package ejercicio1;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonString;
import javax.json.JsonValue;
import javax.net.ssl.HttpsURLConnection;

public class Application {

  public static JsonValue leeJSON(String ruta) {
    try {
      if (ruta.toLowerCase().startsWith("http://")) {
        return leerHttp(ruta);
      } else if (ruta.toLowerCase().startsWith("https://")) {
        return leerHttps(ruta);
      } else {
        return leerFichero(ruta);
      }
    } catch (IOException e) {
      System.out.println("Error procesando documento Json " +
          e.getLocalizedMessage());
      return null;
    }
  }

  public static JsonValue leerFichero(String ruta) throws FileNotFoundException {
    try (JsonReader reader = Json.createReader(new FileReader(ruta))) {
      return reader.read();
      /*
       * JsonStructure jsonSt = reader.read();
       * System.out.println(jsonSt.getValueType());
       * JsonObject jsonObj = reader.readObject();
       * System.out.println(jsonObj.getValueType());
       * JsonArray jsonArr = reader.readArray();
       * System.out.println(jsonArr.getValueType());
       */
    }
  }

  public static JsonValue leerHttp(String direccion) throws IOException {
    URL url = new URL(direccion);
    try (InputStream is = url.openStream();
        JsonReader reader = Json.createReader(is)) {
      return reader.read();
    }
  }

  public static JsonValue leerHttps(String direccion) throws IOException {
    URL url = new URL(direccion);
    HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
    try (InputStream is = conn.getInputStream();
        JsonReader reader = Json.createReader(is)) {
      return reader.read();
    } finally {
      conn.disconnect();
    }
  }

  public static JsonObject Ej1(String localidad) {
    String ruta = "https://api.openweathermap.org/data/2.5/weather?q=" +
        localidad
        + ",es&lang=es&APPID=8f8dccaf02657071004202f05c1fdce0";
    JsonValue jv = leeJSON(ruta);
    return jv.asJsonObject();
  }

  public static JsonObject Ej2(double latitud, double longitud) {
    String ruta = "https://api.openweathermap.org/data/2.5/weather?lat=" + latitud + "&lon=" + longitud
        + "&APPID=8f8dccaf02657071004202f05c1fdce0";
    JsonValue jv = leeJSON(ruta);
    return jv.asJsonObject();
  }

  public static JsonObject Ej3(double latitud, double longitud, int n) {
    String ruta = "https://api.openweathermap.org/data/2.5/find?lat=42.232819&lon=-8.72264&cnt=" + n
        + "&APPID=8f8dccaf02657071004202f05c1fdce0";
    JsonValue jv = leeJSON(ruta);
    return jv.asJsonObject();
  }

  public static int Ej4(String localidad) {
    JsonObject jo = Ej1(localidad);
    return jo.getInt("id");

  }

  public static String Ej5(String localidad) {
    JsonObject jo = Ej1(localidad);
    return jo.getString("name");

  }

  public static String Ej6(String localidad) {
    JsonObject jo = Ej1(localidad);
    JsonObject coord = jo.getJsonObject("coord");
    return String.format("lat: " + coord.getJsonNumber("lat") + ", lon: " + coord.getJsonNumber("lon"));

  }

  public static Pronostico Ej7(String localidad) {
    JsonObject jo = Ej1(localidad);
    final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    String fecha = Instant.ofEpochSecond(jo.getJsonNumber("dt").longValue()).atZone(ZoneId.of("GMT+1"))
        .format(formatter);
    JsonObject main = jo.getJsonObject("main");
    JsonObject clouds = jo.getJsonObject("clouds");
    JsonObject wind = jo.getJsonObject("wind");
    JsonArray weather = jo.getJsonArray("weather");
    Pronostico p = new Pronostico(jo.getString("name"), fecha, main.getJsonNumber("temp").doubleValue(),
        main.getInt("humidity"),
        clouds.getInt("all"), wind.getJsonNumber("speed").doubleValue(),
        weather.getJsonObject(0).getString("description"));

    return p;
  }

  public static Pronostico[] Ej8(String localidad, int n) {
    Pronostico[] pronosticos = new Pronostico[n];
    JsonObject jo = Ej1(localidad);
    JsonObject coord = jo.getJsonObject("coord");
    jo = Ej3(coord.getJsonNumber("lat").doubleValue(), coord.getJsonNumber("lon").doubleValue(), n);
    JsonArray list = jo.getJsonArray("list");
    for (int i = 0; i < pronosticos.length; i++) {
      JsonObject sitio = list.get(i).asJsonObject();
      final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
      String fecha = Instant.ofEpochSecond(sitio.getJsonNumber("dt").longValue()).atZone(ZoneId.of("GMT+1"))
          .format(formatter);
      JsonObject main = sitio.getJsonObject("main");
      JsonObject clouds = sitio.getJsonObject("clouds");
      JsonObject wind = sitio.getJsonObject("wind");
      JsonArray weather = sitio.getJsonArray("weather");
      Pronostico p = new Pronostico(sitio.getString("name"), fecha, main.getJsonNumber("temp").doubleValue(),
          main.getInt("humidity"),
          clouds.getInt("all"), wind.getJsonNumber("speed").doubleValue(),
          weather.getJsonObject(0).getString("description"));
      pronosticos[i] = p;
    }

    return pronosticos;
  }

  public static void Ej9() {
    String ruta = "https://opentdb.com/api.php?amount=20&category=18&difficulty=hard&type=multiple";
    JsonArray preguntas = leeJSON(ruta).asJsonObject().getJsonArray("results");
    JsonObject pregunta;
    for (int i = 0; i < preguntas.size(); i++) {
      pregunta = preguntas.getJsonObject(i);
      System.out.println(pregunta.getString("question"));
      System.out.printf("*\"%s\"\n", pregunta.getString("correct_answer"));
      JsonArray incorrectas = pregunta.getJsonArray("incorrect_answers");
      for (JsonValue mal : incorrectas) {
        System.out.println(mal.toString());
      }
    }
  }

  public static void Ej10(String codigoPais, String tipo) {
    String ruta = "https://app.ticketmaster.com/discovery/v2/events.json?classificationName=" + tipo + "&countryCode="
        + codigoPais + "&apikey=AMXR5Rf8zlr7oGucsebGKvDCLOQmGUGE";
    JsonArray eventos = leeJSON(ruta).asJsonObject().getJsonObject("_embedded").getJsonArray("events");
    for (JsonValue evento : eventos) {
      System.out.println(evento.asJsonObject().getString("name"));
    }
  }

  public static void Ej11Lugar(String codigoPais, String tipo) {
    String ruta = "https://app.ticketmaster.com/discovery/v2/events.json?classificationName=" + tipo + "&countryCode="
        + codigoPais + "&apikey=AMXR5Rf8zlr7oGucsebGKvDCLOQmGUGE";
    JsonArray eventos = leeJSON(ruta).asJsonObject().getJsonObject("_embedded").getJsonArray("events");
    JsonObject datosLugar;
    for (JsonValue evento : eventos) {
      System.out.println(evento.asJsonObject().getString("name"));
      datosLugar = evento.asJsonObject().getJsonObject("_embedded").getJsonArray("venues").getJsonObject(0);
      String ciudad = datosLugar.getJsonObject("city").getString("name");
      String lugar = datosLugar.getString("name");
      String direccion = datosLugar.getJsonObject("address").getString("line1");
      String cp = datosLugar.getString("postalCode");
      System.out.printf("%s, %s, %s, %s\n\n", ciudad, lugar, direccion, cp);
    }

  }

  public static void Ej11Info(String codigoPais, String tipo) {
    String ruta = "https://app.ticketmaster.com/discovery/v2/events.json?classificationName=" + tipo + "&countryCode="
        + codigoPais + "&apikey=AMXR5Rf8zlr7oGucsebGKvDCLOQmGUGE";
    JsonArray eventos = leeJSON(ruta).asJsonObject().getJsonObject("_embedded").getJsonArray("events");
    for (JsonValue evento : eventos) {
      System.out.println(evento.asJsonObject().getString("name"));
      String promotor=evento.asJsonObject().getJsonObject("promoter").getString("name");
      String genero=evento.asJsonObject().getJsonArray("classifications").getJsonObject(0).getJsonObject("genre").getString("name");
      JsonObject comienzo = evento.asJsonObject().getJsonObject("dates").getJsonObject("start");
      System.out.printf("Promotor: %s, Tipo: %s, Comienzo: %s %s\n\n",promotor,genero,comienzo.getString("localDate"),comienzo.getString("localTime"));
    }

  }

  public static void Ej12(String codigoPais, String tipo) {
    String ruta = "https://app.ticketmaster.com/discovery/v2/events.json?classificationName=" + tipo + "&countryCode="
        + codigoPais + "&apikey=AMXR5Rf8zlr7oGucsebGKvDCLOQmGUGE";
    JsonArray eventos = leeJSON(ruta).asJsonObject().getJsonObject("_embedded").getJsonArray("events");
    JsonObject datosLugar;
    for (JsonValue evento : eventos) {
      System.out.println(evento.asJsonObject().getString("name"));
      datosLugar = evento.asJsonObject().getJsonObject("_embedded").getJsonArray("venues").getJsonObject(0);
      String ciudad = datosLugar.getJsonObject("city").getString("name");
      System.out.println(Ej7(ciudad).toString());
    }
  }

  public static void main(String[] args) {

    // Ej10("ES","music");
    // Ej11Info("ES", "music");
    // Ej11Lugar("ES", "music");

    Ej12("ES", "music");

  }
}
