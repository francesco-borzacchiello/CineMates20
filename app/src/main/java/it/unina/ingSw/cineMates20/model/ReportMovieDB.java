package it.unina.ingSw.cineMates20.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "FK_FilmSegnalato",
        "FK_UtenteSegnalatore",
        "MessaggioSegnalazione"
})
public class ReportMovieDB {

    @JsonProperty("FK_FilmSegnalato")
    private Long fKFilmSegnalato;
    @JsonProperty("FK_UtenteSegnalatore")
    private String fKUtenteSegnalatore;
    @JsonProperty("MessaggioSegnalazione")
    private String messaggioSegnalazione;

    public ReportMovieDB() {}

    public ReportMovieDB(Long fKFilmSegnalato, String fKUtenteSegnalatore, String messaggioSegnalazione) {
        super();
        this.fKFilmSegnalato = fKFilmSegnalato;
        this.fKUtenteSegnalatore = fKUtenteSegnalatore;
        this.messaggioSegnalazione = messaggioSegnalazione;
    }

    @JsonProperty("FK_FilmSegnalato")
    public Long getFKFilmSegnalato() {
        return fKFilmSegnalato;
    }

    @JsonProperty("FK_FilmSegnalato")
    public void setFKFilmSegnalato(Long fKFilmSegnalato) {
        this.fKFilmSegnalato = fKFilmSegnalato;
    }

    @JsonProperty("FK_UtenteSegnalatore")
    public String getFKUtenteSegnalatore() {
        return fKUtenteSegnalatore;
    }

    @JsonProperty("FK_UtenteSegnalatore")
    public void setFKUtenteSegnalatore(String fKUtenteSegnalatore) {
        this.fKUtenteSegnalatore = fKUtenteSegnalatore;
    }

    @JsonProperty("MessaggioSegnalazione")
    public String getMessaggioSegnalazione() {
        return messaggioSegnalazione;
    }

    @JsonProperty("MessaggioSegnalazione")
    public void setMessaggioSegnalazione(String messaggioSegnalazione) {
        this.messaggioSegnalazione = messaggioSegnalazione;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(fKFilmSegnalato).append(messaggioSegnalazione).append(fKUtenteSegnalatore).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof ReportMovieDB)) {
            return false;
        }
        ReportMovieDB rhs = ((ReportMovieDB) other);
        return new EqualsBuilder().append(fKFilmSegnalato, rhs.fKFilmSegnalato).append(messaggioSegnalazione, rhs.messaggioSegnalazione).append(fKUtenteSegnalatore, rhs.fKUtenteSegnalatore).isEquals();
    }

}