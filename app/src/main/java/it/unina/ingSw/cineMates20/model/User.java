package it.unina.ingSw.cineMates20.model;

/**
 * Classe che memorizza le informazioni di base dell'utente loggato
 */
public class User {

    private String name;
    private String surname;
    private String email;

    public User(String name, String surname, String email) {
        this.name = name;
        this.surname = surname;
        this.email = email;
    }

    public String getUserName() {
        return name;
    }

    public String getUserSurname() {
        return surname;
    }

    public String getUserEmail() {
        return email;
    }
}