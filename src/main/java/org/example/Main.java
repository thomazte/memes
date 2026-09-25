package org.example;

import org.example.model.Usuario;
import org.example.model.UsuarioDAO;

import java.io.Console;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        UsuarioDAO dao = new UsuarioDAO();
        cadastrar(dao, "teste");
        cadastrar(dao, "admin");
    }

    private static void cadastrar(UsuarioDAO dao, String login) {
        String senha = lerSenha("Senha para '" + login + "': ");

        if (senha == null || senha.isBlank()) {
            System.out.println("Senha nao informada. Pulando " + login);
            return;
        }

        boolean ok = dao.cadastrar(new Usuario(0, login, senha, true));
        System.out.println(ok
                ? "OK: " + login + " cadastrado no banco (senha com BCrypt)."
                : "ERRO: falha ao cadastrar " + login);
    }

    private static String lerSenha(String prompt) {
        Console console = System.console();
        if (console != null) {
            char[] chars = console.readPassword(prompt);
            return chars == null ? null : new String(chars);
        }
        System.out.print(prompt);
        return new Scanner(System.in).nextLine();
    }
}
