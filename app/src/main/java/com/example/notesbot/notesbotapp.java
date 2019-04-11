package com.example.notesbot;

import android.app.Application;

import com.androidnetworking.AndroidNetworking;

/*Clase que inicializa el servicio AndroidNetworking*/

public class notesbotapp extends Application {

    @Override
    public void onCreate(){
        super.onCreate();
        AndroidNetworking.initialize(getApplicationContext());
    }

}
