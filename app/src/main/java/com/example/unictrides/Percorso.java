package com.example.unictrides;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.io.Serializable;
import java.util.List;

public class Percorso implements Serializable, Parcelable {
    public static final Creator<Percorso> CREATOR = new Creator<Percorso>() {
        @Override
        public Percorso createFromParcel(Parcel in) {
            return new Percorso(in);
        }

        @Override
        public Percorso[] newArray(int size) {
            return new Percorso[size];
        }
    };
    private List<Passeggero> passeggeri;
    private Offerta offerta;
    private String key;
    private boolean isRunning;

    public Percorso(String key, Offerta offerta, List<Passeggero> passeggeri) {
        this.passeggeri = passeggeri;
        this.offerta = offerta;
        this.key = key;
    }

    public Percorso() {
    }

    protected Percorso(Parcel in) {
        key = in.readString();
        isRunning = in.readByte() != 0;
    }

    public boolean isRunning() {
        return isRunning;
    }

    public void setRunning(boolean running) {
        isRunning = running;
    }

    public void addPasseggero(Passeggero passeggero) {
        passeggeri.add(passeggero);
    }

    public List<Passeggero> getPasseggeri() {
        return passeggeri;
    }

    public void setPasseggeri(List<Passeggero> passeggeri) {
        this.passeggeri = passeggeri;
    }

    public Offerta getOfferta() {
        return offerta;
    }

    public void setOfferta(Offerta offerta) {
        this.offerta = offerta;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(key);
        dest.writeByte((byte) (isRunning ? 1 : 0));
    }
}