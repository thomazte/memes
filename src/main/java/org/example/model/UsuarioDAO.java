package org.example.model;

import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UsuarioDAO {

    public boolean cadastrar(Usuario usuario) {
        String sql = """
                INSERT INTO usuarios (login, senha, status) VALUES (?, ?, ?)
                """;
        String senhaHash = BCrypt.hashpw(usuario.getSenha(), BCrypt.gensalt(12));

        try (Connection conexao = Conexao.conectar();
             PreparedStatement stmt = conexao.prepareStatement(sql)) {
            stmt.setString(1, usuario.getLogin());
            stmt.setString(2, senhaHash);
            stmt.setBoolean(3, usuario.getStatus());
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public Usuario buscarPorLogin(String login) {
        String sql = """
                SELECT id, login, senha, status
                FROM usuarios
                WHERE login = ?
                """;

        try (Connection conexao = Conexao.conectar();
             PreparedStatement stmt = conexao.prepareStatement(sql)) {
            stmt.setString(1, login);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new Usuario(
                        rs.getInt("id"),
                        rs.getString("login"),
                        rs.getString("senha"),
                        rs.getBoolean("status")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean excluir(int id) {
        String sql = """
                DELETE FROM usuarios
                WHERE id = ?
                """;

        try (Connection conexao = Conexao.conectar();
             PreparedStatement stmt = conexao.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean autenticar(String login, String senha) {
        Usuario usuario = buscarPorLogin(login);
        if (usuario == null || !usuario.getStatus()) {
            return false;
        }
        return verificarSenha(senha, usuario.getSenha());
    }

    private boolean verificarSenha(String senhaDigitada, String senhaHash) {
        if (senhaDigitada == null || senhaHash == null || senhaHash.isBlank()) {
            return false;
        }
        return BCrypt.checkpw(senhaDigitada, senhaHash);
    }
}
