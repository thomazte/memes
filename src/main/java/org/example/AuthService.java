package org.example;

import java.util.Map;

public final class AuthService {

    public enum Papel {
        USER,
        ADMIN
    }

    public record UsuarioAutenticado(String nome, Papel papel) {
    }

    private static final Map<String, String> SENHAS = Map.of(
            "user", "123456",
            "admin", "123456"
    );

    private static final Map<String, Papel> PAPEIS = Map.of(
            "user", Papel.USER,
            "admin", Papel.ADMIN
    );

    private AuthService() {
    }

    public static UsuarioAutenticado autenticar(String usuario, String senha) {
        if (usuario == null || senha == null) {
            return null;
        }

        String senhaEsperada = SENHAS.get(usuario);
        if (senhaEsperada == null || !senhaEsperada.equals(senha)) {
            return null;
        }

        return new UsuarioAutenticado(usuario, PAPEIS.get(usuario));
    }
}
