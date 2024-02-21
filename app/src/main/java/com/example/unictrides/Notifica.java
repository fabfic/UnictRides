package com.example.unictrides;

public class Notifica {
    private int type;
    private Richiesta richiesta;
    private String key, uid;
    private Offerta offerta;
    private Passeggero passeggero;

    public Notifica() {
    }

    public Notifica(String key, int type, String uid) {
        this.key = key;
        this.type = type;
        this.uid = uid;
    }

    public Notifica(String key, int type, Offerta offerta) {
        this.type = type;
        this.key = key;
        this.offerta = offerta;
    }

    public Notifica(String key, int type, Offerta offerta, String uid) {
        this.type = type;
        this.key = key;
        this.offerta = offerta;
        this.uid = uid;
    }

    public Notifica(String key, int type, Richiesta richiesta) {
        this.key = key;
        this.type = type;
        this.richiesta = richiesta;
    }

    public Notifica(String key, int type, Passeggero passeggero) {
        this.key = key;
        this.type = type;
        this.passeggero = passeggero;
    }

    public int getType() {
        return type;
    }


    public Richiesta getRichiesta() {
        return richiesta;
    }

    public String getKey() {
        return key;
    }

    public Passeggero getPasseggero() {
        return passeggero;
    }

    public Offerta getOfferta() {
        return offerta;
    }

    public String getUid() {
        return uid;
    }
}
