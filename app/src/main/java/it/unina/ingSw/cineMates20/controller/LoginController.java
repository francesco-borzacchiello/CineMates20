package it.unina.ingSw.cineMates20.controller;

import android.content.Intent;

import it.unina.ingSw.cineMates20.EntryPoint;
import it.unina.ingSw.cineMates20.view.activity.LoginActivity;

public class LoginController {

    LoginActivity loginActivity;

    public LoginController() {
        this.loginActivity = new LoginActivity();
    }

    public void start(EntryPoint e) {
        Intent intent;
        intent = new Intent(e, LoginActivity.class);
        e.startActivity(intent);
    }
}