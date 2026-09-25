package org.example.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.example.model.Usuario;
import org.example.model.UsuarioDAO;

import java.io.IOException;

@WebServlet({"/login", "/excluir"})
public class LoginServlet extends HttpServlet {

    private final Validacao validacao = new Validacao();
    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        if ("/excluir".equals(req.getServletPath())) {
            excluirUsuario(req, resp);
            return;
        }
        resp.sendRedirect(req.getContextPath() + "/views/login.html");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        if ("/excluir".equals(req.getServletPath())) {
            excluirUsuario(req, resp);
            return;
        }

        String login = req.getParameter("usuario");
        String senha = req.getParameter("senha");

        if (!validacao.validarLogin(login, senha)) {
            resp.sendRedirect(req.getContextPath() + "/views/login.html?erro=1");
            return;
        }

        Usuario autenticado = usuarioDAO.buscarPorLogin(login);
        String perfil = validacao.getPerfil(autenticado.getLogin());

        HttpSession session = req.getSession(true);
        session.setAttribute("usuarioId", autenticado.getId());
        session.setAttribute("usuario", autenticado.getLogin());
        session.setAttribute("perfil", perfil);

        if ("admin".equals(perfil)) {
            resp.sendRedirect(req.getContextPath() + "/admin");
        } else {
            resp.sendRedirect(req.getContextPath() + "/home");
        }
    }

    private void excluirUsuario(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        HttpSession session = req.getSession(false);

        if (session == null || session.getAttribute("usuarioId") == null) {
            resp.sendRedirect(req.getContextPath() + "/views/login.html?erro=sessao");
            return;
        }

        int usuarioId = (Integer) session.getAttribute("usuarioId");
        boolean excluido = usuarioDAO.excluir(usuarioId);

        session.invalidate();

        if (!excluido) {
            resp.sendRedirect(req.getContextPath() + "/views/login.html?erro=1");
            return;
        }

        resp.sendRedirect(req.getContextPath() + "/views/exclusao.html");
    }
}
