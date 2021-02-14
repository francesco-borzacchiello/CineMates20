package it.unina.ingSw.cineMates20.model;

import org.jetbrains.annotations.NotNull;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.LinkedList;
import java.util.List;

import it.unina.ingSw.cineMates20.BuildConfig;

public class ReportHttpRequests {
    private static ReportHttpRequests instance;
    private static final String dbPath = BuildConfig.DB_PATH;

    private ReportHttpRequests() {}

    public static ReportHttpRequests getInstance() {
        if(instance == null)
            instance = new ReportHttpRequests();
        return instance;
    }

    public boolean reportUser(@NotNull String emailUtenteSegnalatore,
                              @NotNull String emailUtenteSegnalato, @NotNull String msg) {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getMessageConverters().add(new StringHttpMessageConverter());
        String url = dbPath + "Report/User/add";

        boolean[] ret = new boolean[1];
        Thread t = new Thread(()-> {
            try {
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);

                ReportUserDB reportUserDB = new ReportUserDB(emailUtenteSegnalato, emailUtenteSegnalatore, msg, null);

                HttpEntity<ReportUserDB> requestEntity = new HttpEntity<>(reportUserDB, headers);
                ResponseEntity<ReportUserDB> responseEntity = restTemplate.postForEntity(url, requestEntity, ReportUserDB.class);

                if(responseEntity.getStatusCode() == HttpStatus.OK)
                    ret[0] = true;
            } catch(HttpClientErrorException ignore) {}
        });
        t.start();

        try {
            t.join();
        }catch(InterruptedException ignore) {}

        return ret[0];
    }

    public boolean reportMovie(long idFilmSegnalato,
                               @NotNull String emailUtenteSegnalatore,
                               @NotNull String msg) {

        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getMessageConverters().add(new StringHttpMessageConverter());
        String url = dbPath + "Report/Film/add";

        boolean[] ret = new boolean[1];
        Thread t = new Thread(()-> {
            try {
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);

                ReportMovieDB reportMovieDB = new ReportMovieDB(idFilmSegnalato, emailUtenteSegnalatore, msg, null);

                HttpEntity<ReportMovieDB> requestEntity = new HttpEntity<>(reportMovieDB, headers);
                ResponseEntity<ReportMovieDB> responseEntity = restTemplate.postForEntity(url, requestEntity, ReportMovieDB.class);

                if(responseEntity.getStatusCode() == HttpStatus.OK)
                    ret[0] = true;
            } catch(HttpClientErrorException ignore) {}
        });
        t.start();

        try {
            t.join();
        }catch(InterruptedException ignore) {}

        return ret[0];
    }

    public List<ReportMovieDB> getAllMoviesReports(String userEmail) {
        List<ReportMovieDB> reportedMovies = new LinkedList<>();

        Thread t = new Thread(()-> {
            try {
                RestTemplate restTemplate = new RestTemplate();
                restTemplate.getMessageConverters().add(new StringHttpMessageConverter());

                String url = dbPath + "Report/getAllMoviesReports/{email}";

                ResponseEntity<List<ReportMovieDB>> responseReports = restTemplate.exchange(url, HttpMethod.GET,
                        null, new ParameterizedTypeReference<List<ReportMovieDB>>() {}, userEmail);

                if(!responseReports.getBody().isEmpty())
                    reportedMovies.addAll(responseReports.getBody());

            }catch(HttpClientErrorException ignore){}
        });
        t.start();

        try {
            t.join();
        }catch(InterruptedException ignore){}

        return reportedMovies;
    }

    public List<ReportUserDB> getAllUsersReports(String userEmail) {
        List<ReportUserDB> reportedUsers = new LinkedList<>();

        Thread t = new Thread(()-> {
            try {
                RestTemplate restTemplate = new RestTemplate();
                restTemplate.getMessageConverters().add(new StringHttpMessageConverter());

                String url = dbPath + "Report/getAllUsersReports/{email}";

                ResponseEntity<List<ReportUserDB>> responseReports = restTemplate.exchange(url, HttpMethod.GET,
                        null, new ParameterizedTypeReference<List<ReportUserDB>>() {}, userEmail);

                if(!responseReports.getBody().isEmpty())
                    reportedUsers.addAll(responseReports.getBody());

            }catch(HttpClientErrorException ignore){}
        });
        t.start();

        try {
            t.join();
        }catch(InterruptedException ignore){}

        return reportedUsers;
    }

    public boolean updateUserDeleteMovieNotification(ReportMovieDB movie) {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getMessageConverters().add(new StringHttpMessageConverter());
        String url = dbPath + "Report/userDeleteMovieNotification";

        boolean[] ret = new boolean[1];
        Thread t = new Thread(()-> {
            try {
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);

                HttpEntity<ReportMovieDB> requestEntity = new HttpEntity<>(movie, headers);
                ResponseEntity<ReportMovieDB> responseEntity = restTemplate.postForEntity(url, requestEntity, ReportMovieDB.class);

                if(responseEntity.getStatusCode() == HttpStatus.OK)
                    ret[0] = true;
            } catch(HttpClientErrorException e) {
                e.printStackTrace();
            }
        });
        t.start();

        try {
            t.join();
        }catch(InterruptedException ignore) {}

        return ret[0];
    }

    public boolean updateUserDeleteUserNotification(ReportUserDB user) {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getMessageConverters().add(new StringHttpMessageConverter());
        String url = dbPath + "Report/userDeleteUserNotification";

        boolean[] ret = new boolean[1];
        Thread t = new Thread(()-> {
            try {
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);

                HttpEntity<ReportUserDB> requestEntity = new HttpEntity<>(user, headers);
                ResponseEntity<ReportUserDB> responseEntity = restTemplate.postForEntity(url, requestEntity, ReportUserDB.class);

                if(responseEntity.getStatusCode() == HttpStatus.OK)
                    ret[0] = true;
            } catch(HttpClientErrorException e) {
                e.printStackTrace();
            }
        });
        t.start();

        try {
            t.join();
        }catch(InterruptedException ignore) {}

        return ret[0];
    }
}
