package org.example.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.example.model.Meme;
import org.example.model.MemeDAO;

import java.io.IOException;

@WebServlet({"/admin", "/meme"})
public class AdminServlet extends HttpServlet {

    private final Validacao validacao = new Validacao();
    private final MemeDAO memeDAO = new MemeDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        if ("/meme".equals(req.getServletPath())) {
            resp.sendRedirect(req.getContextPath() + "/admin");
            return;
        }

        HttpSession session = req.getSession(false);

        if (session == null || session.getAttribute("usuario") == null) {
            resp.sendRedirect(req.getContextPath() + "/views/login.html?erro=sessao");
            return;
        }

        String login = (String) session.getAttribute("usuario");
        if (!"admin".equalsIgnoreCase(login)) {
            resp.sendRedirect(req.getContextPath() + "/home");
            return;
        }

        req.getRequestDispatcher("/views/admin.html").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        if (!"/meme".equals(req.getServletPath())) {
            resp.sendRedirect(req.getContextPath() + "/admin");
            return;
        }

        HttpSession session = req.getSession(false);

        if (session == null || session.getAttribute("usuario") == null) {
            resp.sendRedirect(req.getContextPath() + "/views/login.html?erro=sessao");
            return;
        }

        String login = (String) session.getAttribute("usuario");
        if (!"admin".equalsIgnoreCase(login)) {
            resp.sendRedirect(req.getContextPath() + "/home");
            return;
        }

        String titulo = req.getParameter("titulo");
        String tags = req.getParameter("tags");
        String caminho = req.getParameter("caminho");
        String tipo = req.getParameter("tipo");

        if (!validacao.validarMeme(titulo, caminho, tipo)) {
            resp.sendRedirect(req.getContextPath() + "/views/admin.html?erro=1");
            return;
        }

        Meme meme = new Meme(0, titulo, tags == null ? "" : tags, caminho, tipo.toLowerCase(), true);
        boolean cadastrado = memeDAO.cadastrar(meme);

        if (!cadastrado) {
            resp.sendRedirect(req.getContextPath() + "/views/admin.html?erro=1");
            return;
        }

        resp.sendRedirect(req.getContextPath() + "/admin");
    }
}
