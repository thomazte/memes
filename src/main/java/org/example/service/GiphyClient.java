package org.example.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.example.model.Meme;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class GiphyClient {

    private static final String BASE =
            "https://api.giphy.com/v1/gifs/search?api_key=%s&q=%s&limit=48&rating=pg-13&lang=pt";

    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(8))
            .build();

    public List<Meme> buscarGifs(String termo) {
        return buscar(termo, "gif");
    }

    public List<Meme> buscarImagens(String termo) {
        return buscar(termo, "img");
    }

    private List<Meme> buscar(String termo, String tipo) {
        List<Meme> resultado = new ArrayList<>();
        String apiKey = ApiKeys.giphy();

        if (apiKey == null || apiKey.isBlank() || termo == null || termo.isBlank()) {
            return resultado;
        }

        resultado.addAll(consultar(apiKey, termo.trim(), tipo));
        if (resultado.isEmpty()) {
            resultado.addAll(consultar(apiKey, termo.trim() + " meme", tipo));
        }
        return resultado;
    }

    private List<Meme> consultar(String apiKey, String termoBusca, String tipo) {
        List<Meme> resultado = new ArrayList<>();
        String query = URLEncoder.encode(termoBusca, StandardCharsets.UTF_8);
        String url = String.format(BASE, apiKey, query);

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(12))
                    .GET()
                    .build();

            HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                System.err.println("[Giphy] HTTP " + response.statusCode());
                return resultado;
            }

            JsonObject root = JsonParser.parseString(response.body()).getAsJsonObject();
            JsonArray data = root.getAsJsonArray("data");
            if (data == null) {
                return resultado;
            }

            for (JsonElement element : data) {
                JsonObject item = element.getAsJsonObject();
                String titulo = texto(item, "title");
                if (titulo == null || titulo.isBlank()) {
                    titulo = termoBusca;
                }

                JsonObject images = item.has("images") ? item.getAsJsonObject("images") : null;
                if (images == null) {
                    continue;
                }

                String caminho;
                if ("img".equals(tipo)) {
                    caminho = urlImagem(images, "fixed_width_still", "downsized_still", "original_still");
                } else {
                    caminho = urlImagem(images, "fixed_width", "downsized", "original");
                }
                if (caminho == null || caminho.isBlank()) {
                    continue;
                }

                resultado.add(new Meme(0, titulo, "giphy", caminho, tipo, true));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return resultado;
    }

    private String urlImagem(JsonObject images, String... preferencias) {
        for (String nome : preferencias) {
            if (images.has(nome) && images.get(nome).isJsonObject()) {
                String url = texto(images.getAsJsonObject(nome), "url");
                if (url != null && !url.isBlank()) {
                    return url;
                }
            }
        }
        return null;
    }

    private String texto(JsonObject obj, String campo) {
        if (obj == null || !obj.has(campo) || obj.get(campo).isJsonNull()) {
            return null;
        }
        return obj.get(campo).getAsString();
    }
}
