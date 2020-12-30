package it.unina.ingSw.cineMates20.controller;

import it.unina.ingSw.cineMates20.view.login.activity.LoginActivity;

public class ControllerLogin {

    LoginActivity loginActivity;

    public ControllerLogin() {
        this.loginActivity = new LoginActivity();
    }

    public void Start() {
        //TODO: Capire come va gestito il Bundle, dal costruttore dell'activity sembra voglia indicare un contenitore dello stato dell'app
        //loginActivity.onCreate(new Bundle());
    }
}
