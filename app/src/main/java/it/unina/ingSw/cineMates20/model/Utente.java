package it.unina.ingSw.cineMates20.model;

/**
 * Data class that captures user information for logged in users retrieved from LoginRepository
 */
public class Utente {

    private String userId;
    private String displayName;

    public Utente(String userId, String displayName) {
        this.userId = userId;
        this.displayName = displayName;
    }

    public String getUserId() {
        return userId;
    }

    public String getDisplayName() {
        return displayName;
    }
}