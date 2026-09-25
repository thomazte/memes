package org.example.controller;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Locale;
import java.util.Set;

@WebServlet("/download")
public class DownloadServlet extends HttpServlet {

    private static final Set<String> HOSTS_PERMITIDOS = Set.of(
            "media.giphy.com",
            "media0.giphy.com",
            "media1.giphy.com",
            "media2.giphy.com",
            "media3.giphy.com",
            "media4.giphy.com",
            "i.giphy.com",
            "i.imgflip.com"
    );

    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(8))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("usuario") == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        String url = req.getParameter("url");
        String titulo = req.getParameter("titulo");
        String tipo = req.getParameter("tipo");

        if (url == null || url.isBlank()) {
            resp.sendRedirect(req.getContextPath() + "/home");
            return;
        }

        String nomeArquivo = montarNomeArquivo(titulo, tipo, url);

        try {
            if (url.startsWith("http://") || url.startsWith("https://")) {
                baixarRemoto(url, nomeArquivo, resp);
            } else {
                baixarLocal(url, nomeArquivo, resp);
            }
        } catch (Exception e) {
            e.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_BAD_GATEWAY);
        }
    }

    private void baixarRemoto(String url, String nomeArquivo, HttpServletResponse resp)
            throws IOException, InterruptedException {
        URI uri = URI.create(url);
        String host = uri.getHost() == null ? "" : uri.getHost().toLowerCase(Locale.ROOT);
        if (!HOSTS_PERMITIDOS.contains(host)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .timeout(Duration.ofSeconds(20))
                .GET()
                .build();

        HttpResponse<InputStream> response = http.send(request, HttpResponse.BodyHandlers.ofInputStream());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            resp.sendError(HttpServletResponse.SC_BAD_GATEWAY);
            return;
        }

        String contentType = response.headers()
                .firstValue("Content-Type")
                .orElse(contentTypePorNome(nomeArquivo));

        resp.setContentType(contentType);
        resp.setHeader("Content-Disposition", "attachment; filename=\"" + nomeArquivo + "\"");

        try (InputStream in = response.body(); OutputStream out = resp.getOutputStream()) {
            in.transferTo(out);
        }
    }

    private void baixarLocal(String caminho, String nomeArquivo, HttpServletResponse resp)
            throws IOException {
        String path = caminho.replace("..", "").replace('\\', '/');
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        if (path.startsWith("/css/") || path.startsWith("/views/")) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        try (InputStream in = getServletContext().getResourceAsStream(path)) {
            if (in == null) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            resp.setContentType(contentTypePorNome(nomeArquivo));
            resp.setHeader("Content-Disposition", "attachment; filename=\"" + nomeArquivo + "\"");
            try (OutputStream out = resp.getOutputStream()) {
                in.transferTo(out);
            }
        }
    }

    private String montarNomeArquivo(String titulo, String tipo, String url) {
        String base = (titulo == null || titulo.isBlank()) ? "meme" : titulo;
        base = base.replaceAll("[^a-zA-Z0-9-_\\. ]", "")
                .trim()
                .replace(' ', '_')
                .toLowerCase(Locale.ROOT);
        if (base.isBlank()) {
            base = "meme";
        }
        if (base.length() > 40) {
            base = base.substring(0, 40);
        }

        String ext = extensao(tipo, url);
        if (!base.endsWith(ext)) {
            base = base + ext;
        }
        return base;
    }

    private String extensao(String tipo, String url) {
        String lower = (url == null ? "" : url).toLowerCase(Locale.ROOT);
        if ("gif".equalsIgnoreCase(tipo) || lower.contains(".gif")) {
            return ".gif";
        }
        if (lower.contains(".png")) {
            return ".png";
        }
        if (lower.contains(".webp")) {
            return ".webp";
        }
        return ".jpg";
    }

    private String contentTypePorNome(String nome) {
        String lower = nome.toLowerCase(Locale.ROOT);
        if (lower.endsWith(".gif")) {
            return "image/gif";
        }
        if (lower.endsWith(".png")) {
            return "image/png";
        }
        if (lower.endsWith(".webp")) {
            return "image/webp";
        }
        return "image/jpeg";
    }
}
