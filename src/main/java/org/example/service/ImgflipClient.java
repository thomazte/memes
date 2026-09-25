package org.example.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.example.model.Meme;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ImgflipClient {

    private static final String URL = "https://api.imgflip.com/get_memes";

    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(8))
            .build();

    public List<Meme> buscar(String termo) {
        List<Meme> resultado = new ArrayList<>();

        if (termo == null || termo.isBlank()) {
            return resultado;
        }

        String[] palavras = termo.trim().toLowerCase(Locale.ROOT).split("\\s+");

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(URL))
                    .timeout(Duration.ofSeconds(10))
                    .GET()
                    .build();

            HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                return resultado;
            }

            JsonObject root = JsonParser.parseString(response.body()).getAsJsonObject();
            if (!root.has("success") || !root.get("success").getAsBoolean()) {
                return resultado;
            }

            JsonArray memes = root.getAsJsonObject("data").getAsJsonArray("memes");
            if (memes == null) {
                return resultado;
            }

            for (JsonElement element : memes) {
                JsonObject item = element.getAsJsonObject();
                String nome = texto(item, "name");
                String caminho = texto(item, "url");

                if (nome == null || caminho == null) {
                    continue;
                }

                String nomeLower = nome.toLowerCase(Locale.ROOT);
                boolean bateu = false;
                for (String palavra : palavras) {
                    if (palavra.length() >= 2 && nomeLower.contains(palavra)) {
                        bateu = true;
                        break;
                    }
                }
                if (!bateu) {
                    continue;
                }

                resultado.add(new Meme(0, nome, "imgflip", caminho, "img", true));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return resultado;
    }

    private String texto(JsonObject obj, String campo) {
        if (obj == null || !obj.has(campo) || obj.get(campo).isJsonNull()) {
            return null;
        }
        return obj.get(campo).getAsString();
    }
}
