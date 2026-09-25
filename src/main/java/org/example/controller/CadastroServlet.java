package org.example.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.model.Usuario;
import org.example.model.UsuarioDAO;

import java.io.IOException;

@WebServlet("/cadastro")
public class CadastroServlet extends HttpServlet {

    private final Validacao validacao = new Validacao();
    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.sendRedirect(req.getContextPath() + "/views/cadastro.html");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String login = req.getParameter("usuario");
        String senha = req.getParameter("senha");

        if (!validacao.validarCadastro(login, senha)) {
            resp.sendRedirect(req.getContextPath() + "/views/cadastro.html?erro=1");
            return;
        }

        Usuario novo = new Usuario(0, login, senha, true);
        boolean cadastrado = usuarioDAO.cadastrar(novo);

        if (!cadastrado) {
            resp.sendRedirect(req.getContextPath() + "/views/cadastro.html?erro=1");
            return;
        }

        resp.sendRedirect(req.getContextPath() + "/views/login.html?ok=1");
    }
}
