package org.example.model;

public class Meme {
    private int id;
    private String titulo;
    private String tags;
    private String caminho;
    private String tipo;
    private boolean status;

    public Meme(int id, String titulo, String tags, String caminho, String tipo, boolean status) {
        this.id = id;
        this.titulo = titulo;
        this.tags = tags;
        this.caminho = caminho;
        this.tipo = tipo;
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getTags() {
        return tags;
    }

    public String getCaminho() {
        return caminho;
    }

    public String getTipo() {
        return tipo;
    }

    public boolean getStatus() {
        return status;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public void setCaminho(String caminho) {
        this.caminho = caminho;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }
}
