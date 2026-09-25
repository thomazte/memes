package org.example.controller;

import org.example.model.UsuarioDAO;

public class Validacao {

    private final UsuarioDAO usuarioDAO;

    public Validacao() {
        usuarioDAO = new UsuarioDAO();
    }

    public boolean validarLogin(String usuario, String senha) {
        if (usuario == null || usuario.isBlank()) {
            return false;
        }
        if (senha == null || senha.isBlank()) {
            return false;
        }
        return usuarioDAO.autenticar(usuario, senha);
    }

    public boolean validarCadastro(String usuario, String senha) {
        if (usuario == null || usuario.isBlank()) {
            return false;
        }
        if (senha == null || senha.isBlank()) {
            return false;
        }
        return usuarioDAO.buscarPorLogin(usuario) == null;
    }

    public boolean validarMeme(String titulo, String caminho, String tipo) {
        if (titulo == null || titulo.isBlank()) {
            return false;
        }
        if (caminho == null || caminho.isBlank()) {
            return false;
        }
        if (tipo == null || tipo.isBlank()) {
            return false;
        }
        return "img".equalsIgnoreCase(tipo) || "gif".equalsIgnoreCase(tipo);
    }

    public String getPerfil(String usuario) {
        if (usuario == null || usuario.isBlank()) {
            return null;
        }
        if ("admin".equalsIgnoreCase(usuario)) {
            return "admin";
        }
        return "usuario";
    }
}
