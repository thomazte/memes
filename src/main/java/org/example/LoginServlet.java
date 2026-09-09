package org.example;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    private static final String USUARIO_VALIDO = "admin";
    private static final String SENHA_VALIDA = "123456";

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

        if (USUARIO_VALIDO.equals(usuario) && SENHA_VALIDA.equals(senha)) {
            resp.sendRedirect(req.getContextPath() + "/html/home.html");
        } else {
            resp.sendRedirect(req.getContextPath() + "/html/login.html?erro=1");
        }
    }
}