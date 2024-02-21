package com.example.unictrides;

import java.io.Serializable;
import java.util.Objects;

public class POI implements Serializable {

    private String nome;
    private double latitude;
    private double longitude;

    public POI(String nome, double latitude, double longitude) {
        this.nome = nome;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public POI() {
    }

    public POI(String nome) {
        this.nome = nome;
    }

    @Override
    public String toString() {
        return nome;
    }

    public String getNome() {
        return nome;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        POI poi = (POI) o;
        return Double.compare(poi.getLatitude(), getLatitude()) == 0 && Double.compare(poi.getLongitude(), getLongitude()) == 0 && getNome().equals(poi.getNome());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getNome(), getLatitude(), getLongitude());
    }
}
