package com.example.unictrides;

import java.io.Serializable;

public class User implements Serializable {

    private String email, nome, cognome, facolta, telefono, profilePic;
    private boolean guidatore;
    private POI poiSorgete, poiDestinazione;
    private Float ratingSum;
    private int numRating;

    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public User(String email, String nome, String cognome, String facolta, String telefono, boolean guidatore, POI poiSorgete, POI poiDestinazione) {
        this.email = email;
        this.nome = nome;
        this.cognome = cognome;
        this.facolta = facolta;
        this.telefono = telefono;
        this.guidatore = guidatore;
        this.poiSorgete = poiSorgete;
        this.poiDestinazione = poiDestinazione;
        this.profilePic = null;
        numRating = 0;
        ratingSum = 0f;
    }

    public String getProfilePic() {
        return profilePic;
    }

    public void setProfilePic(String profilePic) {
        this.profilePic = profilePic;
    }

    public String getEmail() {
        return email;
    }

    public String getNome() {
        return nome;
    }

    public String getCognome() {
        return cognome;
    }

    public String getFacolta() {
        return facolta;
    }

    public void setFacolta(String facolta) {
        this.facolta = facolta;
    }

    public boolean isGuidatore() {
        return guidatore;
    }

    public void setGuidatore(boolean guidatore) {
        this.guidatore = guidatore;
    }

    public POI getPoiSorgete() {
        return poiSorgete;
    }

    public void setPoiSorgete(POI poiSorgete) {
        this.poiSorgete = poiSorgete;
    }

    public POI getPoiDestinazione() {
        return poiDestinazione;
    }

    public void setPoiDestinazione(POI poiDestinazione) {
        this.poiDestinazione = poiDestinazione;
    }

    public Float getRatingSum() {
        return ratingSum;
    }

    public void setRatingSum(Float ratingSum) {
        this.ratingSum = ratingSum;
    }

    public String getTelefono() {
        return telefono;
    }

    public int getNumRating() {
        return numRating;
    }

    public void setNumRating(int numRating) {
        this.numRating = numRating;
    }
}
