package it.unina.ingSw.cineMates20.controller;

import android.content.Intent;

import it.unina.ingSw.cineMates20.EntryPoint;
import it.unina.ingSw.cineMates20.view.activity.LoginActivity;

public class ControllerLogin {

    LoginActivity loginActivity;

    public ControllerLogin() {
        this.loginActivity = new LoginActivity();
    }

    public void startControllerLogin(EntryPoint e) {
        Intent intent;
        intent = new Intent(e, LoginActivity.class);
        e.startActivity(intent);
    }
}
