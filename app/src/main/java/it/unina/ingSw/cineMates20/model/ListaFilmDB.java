package it.unina.ingSw.cineMates20.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "id",
        "nome",
        "Email_Possessore"
})
public class ListaFilmDB {

    @JsonProperty("id")
    private Long id;
    @JsonProperty("nome")
    private String nome;
    @JsonProperty("Email_Possessore")
    private String emailPossessore;

    public ListaFilmDB() {}

    public ListaFilmDB(Long id, String nome, String emailPossessore) {
        super();
        this.id = id;
        this.nome = nome;
        this.emailPossessore = emailPossessore;
    }

    public ListaFilmDB(String nome, String emailPossessore) {
        super();
        this.id = null;
        this.nome = nome;
        this.emailPossessore = emailPossessore;
    }

    @JsonProperty("id")
    public Long getId() {
        return id;
    }

    @JsonProperty("id")
    public void setId(Long id) {
        this.id = id;
    }

    @JsonProperty("nome")
    public String getNome() {
        return nome;
    }

    @JsonProperty("nome")
    public void setNome(String nome) {
        this.nome = nome;
    }

    @JsonProperty("Email_Possessore")
    public String getEmailPossessore() {
        return emailPossessore;
    }

    @JsonProperty("Email_Possessore")
    public void setEmailPossessore(String emailPossessore) {
        this.emailPossessore = emailPossessore;
    }

}