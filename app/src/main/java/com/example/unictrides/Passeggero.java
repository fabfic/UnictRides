package com.example.unictrides;

public class Passeggero {
    private String uid;
    private String nome;
    private POI posizione;

    public Passeggero(String uid, String nome, POI posizione) {
        this.uid = uid;
        this.nome = nome;
        this.posizione = posizione;
    }

    public Passeggero() {
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public POI getPosizione() {
        return posizione;
    }

    public void setPosizione(POI posizione) {
        this.posizione = posizione;
    }
}
