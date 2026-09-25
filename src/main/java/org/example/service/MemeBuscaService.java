package org.example.service;

import org.example.model.Meme;
import org.example.model.MemeDAO;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MemeBuscaService {

    private final MemeDAO memeDAO = new MemeDAO();
    private final GiphyClient giphyClient = new GiphyClient();
    private final ImgflipClient imgflipClient = new ImgflipClient();

    public List<Meme> buscar(String q, String tipo) {
        List<Meme> bruto = new ArrayList<>();
        String tipoNorm = tipo == null ? "" : tipo.trim().toLowerCase();

        bruto.addAll(memeDAO.buscar(q, tipo));

        if (q == null || q.isBlank()) {
            return bruto;
        }

        boolean querGif = tipoNorm.isBlank() || "gif".equals(tipoNorm);
        boolean querImg = tipoNorm.isBlank() || "img".equals(tipoNorm);

        if (querGif) {
            bruto.addAll(giphyClient.buscarGifs(q));
        }
        if (querImg) {
            bruto.addAll(imgflipClient.buscar(q));
            bruto.addAll(giphyClient.buscarImagens(q));
        }

        return deduplicarPorCaminho(bruto);
    }

    private List<Meme> deduplicarPorCaminho(List<Meme> origem) {
        Map<String, Meme> unicos = new LinkedHashMap<>();
        for (Meme meme : origem) {
            if (meme.getCaminho() == null || meme.getCaminho().isBlank()) {
                continue;
            }
            unicos.putIfAbsent(meme.getCaminho(), meme);
        }
        return new ArrayList<>(unicos.values());
    }
}
