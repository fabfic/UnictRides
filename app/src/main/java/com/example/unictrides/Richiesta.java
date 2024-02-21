package com.example.unictrides;

public class Richiesta {
    private String key, uidRichiedente, nomeRichiedente;
    private Offerta offerta;
    private POI poiSorgente;

    public Richiesta(String key, String uidRichiedente, String nomeRichiedente, Offerta offerta, POI poiSorgente) {
        this.key = key;
        this.uidRichiedente = uidRichiedente;
        this.nomeRichiedente = nomeRichiedente;
        this.offerta = offerta;
        this.poiSorgente = poiSorgente;
    }

    public Richiesta() {
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public POI getPoiSorgente() {
        return poiSorgente;
    }

    public void setPoiSorgente(POI poiSorgente) {
        this.poiSorgente = poiSorgente;
    }

    public Offerta getOfferta() {
        return offerta;
    }

    public void setOfferta(Offerta offerta) {
        this.offerta = offerta;
    }

    public String getUidRichiedente() {
        return uidRichiedente;
    }

    public void setUidRichiedente(String uidRichiedente) {
        this.uidRichiedente = uidRichiedente;
    }

    public String getNomeRichiedente() {
        return nomeRichiedente;
    }

    public void setNomeRichiedente(String nomeRichiedente) {
        this.nomeRichiedente = nomeRichiedente;
    }
}
