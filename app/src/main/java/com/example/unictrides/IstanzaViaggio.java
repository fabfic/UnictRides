package com.example.unictrides;

import java.io.Serializable;

public class IstanzaViaggio implements Serializable {
    private Percorso percorso;
    private String uid;
    private Float rating;
    private boolean firstRating;
    private boolean guidatore;
    private String key;
    private long timestamp;

    public IstanzaViaggio() {
    }

    public IstanzaViaggio(String key, Percorso percorso, String uid, boolean guidatore, long timestamp) {
        this.key = key;
        this.percorso = percorso;
        this.uid = uid;
        this.guidatore = guidatore;
        this.timestamp = timestamp;
    }

    public Percorso getPercorso() {
        return percorso;
    }

    public void setPercorso(Percorso percorso) {
        this.percorso = percorso;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public Float getRating() {
        return rating;
    }

    public void setRating(Float rating) {

        this.rating = rating;
        firstRating = false;
    }

    public boolean isFirstRating() {
        return firstRating;
    }

    public void setFirstRating(boolean firstRating) {
        this.firstRating = firstRating;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public boolean isGuidatore() {
        return guidatore;
    }

}
