package org.example.controller;

import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.example.model.Meme;
import org.example.service.MemeBuscaService;

import java.io.IOException;
import java.util.List;

@WebServlet({"/home", "/buscar"})
public class HomeServlet extends HttpServlet {

    private final MemeBuscaService memeBuscaService = new MemeBuscaService();
    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        HttpSession session = req.getSession(false);

        if (session == null || session.getAttribute("usuario") == null) {
            if ("/buscar".equals(req.getServletPath())) {
                resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                resp.setContentType("application/json;charset=UTF-8");
                resp.getWriter().write("{\"erro\":\"sessao\"}");
                return;
            }
            resp.sendRedirect(req.getContextPath() + "/views/login.html?erro=sessao");
            return;
        }

        String login = (String) session.getAttribute("usuario");
        if ("admin".equalsIgnoreCase(login) && "/home".equals(req.getServletPath())) {
            resp.sendRedirect(req.getContextPath() + "/admin");
            return;
        }

        if ("/buscar".equals(req.getServletPath())) {
            responderBusca(req, resp);
            return;
        }

        req.getRequestDispatcher("/views/home.html").forward(req, resp);
    }

    private void responderBusca(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        String q = req.getParameter("q");
        String tipo = req.getParameter("tipo");
        List<Meme> memes = memeBuscaService.buscar(q, tipo);

        resp.setContentType("application/json;charset=UTF-8");
        resp.getWriter().write(gson.toJson(memes));
    }
}
