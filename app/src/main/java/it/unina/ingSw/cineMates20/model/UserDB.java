
package it.unina.ingSw.cineMates20.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "username",
        "nome",
        "cognome",
        "email",
        "tipoUtente"
})
public class UserDB {

    @JsonProperty("username")
    private String username;
    @JsonProperty("nome")
    private String nome;
    @JsonProperty("cognome")
    private String cognome;
    @JsonProperty("email")
    private String email;
    @JsonProperty("tipoUtente")
    private String tipoUtente;

    public UserDB() {}

    public UserDB(String username, String nome, String cognome, String email, String tipoUtente) {
        super();
        this.username = username;
        this.nome = nome;
        this.cognome = cognome;
        this.email = email;
        this.tipoUtente = tipoUtente;
    }

    @JsonProperty("username")
    public String getUsername() {
        return username;
    }

    @JsonProperty("username")
    public void setUsername(String username) {
        this.username = username;
    }

    @JsonProperty("nome")
    public String getNome() {
        return nome;
    }

    @JsonProperty("nome")
    public void setNome(String nome) {
        this.nome = nome;
    }

    @JsonProperty("cognome")
    public String getCognome() {
        return cognome;
    }

    @JsonProperty("cognome")
    public void setCognome(String cognome) {
        this.cognome = cognome;
    }

    @JsonProperty("email")
    public String getEmail() {
        return email;
    }

    @JsonProperty("email")
    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(nome).append(tipoUtente).append(cognome).append(email).append(username).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof UserDB)) {
            return false;
        }
        UserDB otherUser = ((UserDB) other);
        return new EqualsBuilder().append(nome, otherUser.nome).append(tipoUtente, otherUser.tipoUtente).
                append(cognome, otherUser.cognome).append(email, otherUser.email).append(username, otherUser.username).isEquals();
    }

}