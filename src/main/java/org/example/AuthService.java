package org.example;

public final class AuthService {

    private static final String USUARIO_VALIDO = "admin";
    private static final String SENHA_VALIDA = "admin123";

    private AuthService() {
    }

    public static boolean autenticar(String usuario, String senha) {
        return USUARIO_VALIDO.equals(usuario) && SENHA_VALIDA.equals(senha);
    }
}
