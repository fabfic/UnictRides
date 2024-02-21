package com.example.unictrides;

import java.io.Serializable;

public class Veicolo implements Serializable {

    private String targa, modello, colore;
    private String owner;

    public Veicolo() {
    }

    public Veicolo(String modello, String targa, String colore, String uid) {
        this.targa = targa;
        this.modello = modello;
        this.colore = colore;
        this.owner = uid;
    }

    public String getTarga() {
        return targa;
    }

    public void setTarga(String targa) {
        this.targa = targa;
    }

    public String getModello() {
        return modello;
    }

    public void setModello(String modello) {
        this.modello = modello;
    }

    public String getColore() {
        return colore;
    }

    public void setColore(String colore) {
        this.colore = colore;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }
}
