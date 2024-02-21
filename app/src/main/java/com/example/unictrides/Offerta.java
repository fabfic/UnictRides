package com.example.unictrides;

import java.io.Serializable;
import java.util.Objects;

public class Offerta implements Serializable {

    private String uid, nomeUtente, data, ora, key;
    private POI poiSorgente, poiDestinazione;
    private int posti;

    public Offerta(String key, String uid, String nomeUtente, String data, String ora, POI poiSorgente, POI poiDestinazione, int posti) {
        this.key = key;
        this.uid = uid;
        this.nomeUtente = nomeUtente;
        this.data = data;
        this.ora = ora;
        this.poiSorgente = poiSorgente;
        this.poiDestinazione = poiDestinazione;
        this.posti = posti;
    }

    public Offerta() {
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getOra() {
        return ora;
    }

    public void setOra(String ora) {
        this.ora = ora;
    }

    public String getNomeUtente() {
        return nomeUtente;
    }

    public void setNomeUtente(String nomeUtente) {
        this.nomeUtente = nomeUtente;
    }

    public POI getPoiSorgente() {
        return poiSorgente;
    }

    public void setPoiSorgente(POI poiSorgente) {
        this.poiSorgente = poiSorgente;
    }

    public POI getPoiDestinazione() {
        return poiDestinazione;
    }

    public void setPoiDestinazione(POI poiDestinazione) {
        this.poiDestinazione = poiDestinazione;
    }

    public int getPosti() {
        return posti;
    }

    public void setPosti(int posti) {
        this.posti = posti;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getUid(), getNomeUtente(), getData(), getOra(), getKey(), getPoiSorgente(), getPoiDestinazione(), getPosti());
    }
}
