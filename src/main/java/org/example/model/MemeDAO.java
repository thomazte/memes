package org.example.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MemeDAO {

    public boolean cadastrar(Meme meme) {
        String sql = """
                INSERT INTO memes (titulo, tags, caminho, tipo, status)
                VALUES (?, ?, ?, ?, ?)
                """;

        try (Connection conexao = Conexao.conectar();
             PreparedStatement stmt = conexao.prepareStatement(sql)) {
            stmt.setString(1, meme.getTitulo());
            stmt.setString(2, meme.getTags());
            stmt.setString(3, meme.getCaminho());
            stmt.setString(4, meme.getTipo());
            stmt.setBoolean(5, meme.getStatus());
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Meme> buscar(String q, String tipo) {
        String sql = """
                SELECT id, titulo, tags, caminho, tipo, status
                FROM memes
                WHERE status = true
                  AND (? IS NULL OR ? = '' OR titulo ILIKE ? OR tags ILIKE ?)
                  AND (? IS NULL OR ? = '' OR tipo = ?)
                ORDER BY id DESC
                """;
        List<Meme> memes = new ArrayList<>();
        String termo = (q == null || q.isBlank()) ? null : "%" + q.trim() + "%";
        String tipoFiltro = (tipo == null || tipo.isBlank()) ? null : tipo.trim();

        try (Connection conexao = Conexao.conectar();
             PreparedStatement stmt = conexao.prepareStatement(sql)) {
            stmt.setString(1, q);
            stmt.setString(2, q);
            stmt.setString(3, termo);
            stmt.setString(4, termo);
            stmt.setString(5, tipoFiltro);
            stmt.setString(6, tipoFiltro);
            stmt.setString(7, tipoFiltro);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                memes.add(mapear(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return memes;
    }

    private Meme mapear(ResultSet rs) throws SQLException {
        return new Meme(
                rs.getInt("id"),
                rs.getString("titulo"),
                rs.getString("tags"),
                rs.getString("caminho"),
                rs.getString("tipo"),
                rs.getBoolean("status")
        );
    }
}
