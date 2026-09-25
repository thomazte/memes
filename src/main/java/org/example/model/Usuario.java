package org.example.model;

public class Usuario {
    private int id;
    private String login;
    private String senha;
    private boolean status;

    public Usuario(int id, String login, String senha, boolean status) {
        this.id = id;
        this.login = login;
        this.senha = senha;
        this.status = status;
    }

    public int getId() {
        return id;
    }
    public String getLogin() {
        return login;
    }
    public String getSenha() {
        return senha;
    }
    public boolean getStatus() {
        return status;
    }

    public void setId(int id) {
        this.id = id;
    }
    public void setLogin(String login) {
        this.login = login;
    }
    public void setSenha(String senha) {
        this.senha = senha;
    }
    public void setStatus(boolean status) {
        this.status = status;
    }
}
