package it.unina.ingSw.cineMates20.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "FK_UtenteSegnalato",
        "FK_UtenteSegnalatore",
        "MessaggioSegnalazione",
        "EsitoSegnalazione"
})
public class ReportUserDB {

    @JsonProperty("FK_UtenteSegnalato")
    private String fKUtenteSegnalato;
    @JsonProperty("FK_UtenteSegnalatore")
    private String fKUtenteSegnalatore;
    @JsonProperty("MessaggioSegnalazione")
    private String messaggioSegnalazione;
    @JsonProperty("EsitoSegnalazione")
    private String esitoSegnalazione;

    public ReportUserDB() { }

    public ReportUserDB(String fKUtenteSegnalato, String fKUtenteSegnalatore,
                        String messaggioSegnalazione, String esitoSegnalazione) {
        super();
        this.fKUtenteSegnalato = fKUtenteSegnalato;
        this.fKUtenteSegnalatore = fKUtenteSegnalatore;
        this.messaggioSegnalazione = messaggioSegnalazione;
        this.esitoSegnalazione = esitoSegnalazione;
    }

    @JsonProperty("FK_UtenteSegnalato")
    public String getFKUtenteSegnalato() {
        return fKUtenteSegnalato;
    }

    @JsonProperty("FK_UtenteSegnalato")
    public void setFKUtenteSegnalato(String fKUtenteSegnalato) {
        this.fKUtenteSegnalato = fKUtenteSegnalato;
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

    @JsonProperty("EsitoSegnalazione")
    public String getEsitoSegnalazione() {
        return esitoSegnalazione;
    }

    @JsonProperty("EsitoSegnalazione")
    public void setEsitoSegnalazione(String esitoSegnalazione) {
        this.esitoSegnalazione = esitoSegnalazione;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(esitoSegnalazione).
                append(fKUtenteSegnalato).
                append(messaggioSegnalazione).
                append(fKUtenteSegnalatore).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof ReportUserDB)) {
            return false;
        }
        ReportUserDB rhs = ((ReportUserDB) other);
        return new EqualsBuilder().append(esitoSegnalazione, rhs.esitoSegnalazione)
                .append(fKUtenteSegnalato, rhs.fKUtenteSegnalato)
                .append(messaggioSegnalazione, rhs.messaggioSegnalazione)
                .append(fKUtenteSegnalatore, rhs.fKUtenteSegnalatore).isEquals();
    }

}