package com.example.unictrides;

import java.util.ArrayList;
import java.util.List;

public class POIs {
    private static final List<POI> UNICT_POI = new ArrayList<>();
    private static final List<POI> CT_POI = new ArrayList<>();

    static {
        UNICT_POI.add(new POI("AOU Policlinico(Via Santa sofia 78)", 37.529589, 15.068775));
        UNICT_POI.add(new POI("P.O. Garibaldi - Nesima(Via Palermo, 636)", 37.511336, 15.041856));
        UNICT_POI.add(new POI("Torre Biologica(Via Santa Sofia, 87)", 37.52982470679159, 15.068195991178463));
        UNICT_POI.add(new POI("Cittadella Universitaria(Via santa sofia 64)", 37.52442167106908, 15.069877711914382));
        UNICT_POI.add(new POI("Agraria( Via santa sofia, 100)", 37.535278737199306, 15.067390727885954));
        UNICT_POI.add(new POI("Palazzo delle Scienze(Corso Italia, 55-57)", 37.51516252703831, 15.095574501379092));
        UNICT_POI.add(new POI("Villa Cerami(Via Gallo, 24)", 37.50663045338058, 15.08410413854425));
        UNICT_POI.add(new POI("Polo didattico G.Virlinzi(Via Rocca Romana 45)", 37.50863733956756, 15.078589085425381));
        UNICT_POI.add(new POI("Palazzo Pedagaggi(Via Vittorio Emanuele II, 49)", 37.50347400856735, 15.093682047560968));
        UNICT_POI.add(new POI("Monastero dei Benedettini(Piazza dante 32)", 37.50364762068237, 15.080737288526281));

        CT_POI.add(new POI("Nesima(stazione metropolitana)", 37.51631727704921, 15.050072916265124));
        CT_POI.add(new POI("Borgo-sanzio(stazione metropolitana)", 37.52167080130523, 15.084022033549923));
        CT_POI.add(new POI("Porta Garibaldi, Piazza Palestro", 37.499729039299375, 15.074166160522145));
        CT_POI.add(new POI("Piazza Cutelli", 37.503293456528546, 15.09316701927368));
        CT_POI.add(new POI("Palazzetto dello Sport - PalaCatania", 37.50399061072152, 15.05642612666687));
        CT_POI.add(new POI("San Giovanni Galermo,Piazza Beppe Montana", 37.53115059513084, 15.05982217561852));
        CT_POI.add(new POI("Ognina,Piazza Nettuno", 37.5238668045883, 15.116428778366075));
        CT_POI.add(new POI("Piazza Europa", 37.516698495227914, 15.105516110662352));
        CT_POI.add(new POI("Piazza Giovanni XXIII", 37.5073008403779, 15.099108725371044));
        CT_POI.add(new POI("Cibali, Piazza Bonadies", 37.51800643404458, 15.068725646209838));
        CT_POI.add(new POI("Milo(stazione metropolitana)", 37.52157287133531, 15.07564335194672));
        CT_POI.add(new POI("Piazza Santa Maria di Gesù", 37.512013827323955, 15.078898832524741));
        CT_POI.add(new POI("Corso Italia(stazione metropolitana)", 37.51615082806108, 15.096491835877305));
        CT_POI.add(new POI("Castello Ursino", 37.49941478260833, 15.084438298667635));
        CT_POI.add(new POI("Piazza Stesicoro", 37.507320806189774, 15.086282377332079));
        CT_POI.add(new POI("Piazza Eroi d'Ungheria", 37.507896168065784, 15.05030847315806));
    }

    public static List<POI> getUNICT_POI() {
        return UNICT_POI;
    }

    public static List<POI> getCT_POI() {
        return CT_POI;
    }
}
