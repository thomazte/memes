package org.example;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.sendRedirect(req.getContextPath() + "/html/login.html");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String usuario = req.getParameter("usuario");
        String senha = req.getParameter("senha");

        AuthService.UsuarioAutenticado autenticado = AuthService.autenticar(usuario, senha);

        if (autenticado == null) {
            resp.sendRedirect(req.getContextPath() + "/html/login.html?erro=1");
            return;
        }

        HttpSession session = req.getSession(true);
        session.setAttribute("usuario", autenticado.nome());
        session.setAttribute("papel", autenticado.papel().name());

        if (autenticado.papel() == AuthService.Papel.ADMIN) {
            resp.sendRedirect(req.getContextPath() + "/admin");
        } else {
            resp.sendRedirect(req.getContextPath() + "/home");
        }
    }
}
