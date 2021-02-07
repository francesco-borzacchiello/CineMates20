package it.unina.ingSw.cineMates20.model;

import android.app.Activity;
import android.os.Bundle;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONException;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.HashSet;
import java.util.Set;

import it.unina.ingSw.cineMates20.BuildConfig;

public class UserHttpRequests {

    private static UserHttpRequests instance;
    private static final String dbPath = BuildConfig.DB_PATH,
                                favourites = BuildConfig.FAVOURITES,
                                toWatch = BuildConfig.TO_WATCH;

    private UserHttpRequests() {}

    public static UserHttpRequests getInstance() {
        if(instance == null)
            instance = new UserHttpRequests();
        return instance;
    }

    public boolean createNewUser(@NotNull UserDB user) {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getMessageConverters().add(new StringHttpMessageConverter());
        String url = dbPath + "User/add";

        boolean[] ret = new boolean[1];
        Thread t = new Thread(()-> {
            try {
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);

                HttpEntity<UserDB> requestEntity = new HttpEntity<>(user, headers);
                ResponseEntity<UserDB> responseEntity = restTemplate.postForEntity(url, requestEntity, UserDB.class);

                if(responseEntity.getStatusCode() == HttpStatus.OK)
                    ret[0] = createMoviesLists(user.getEmail());
            } catch(HttpClientErrorException ignore) {}
        });
        t.start();

        try {
            t.join();
        }catch(InterruptedException ignore) {}

        return ret[0];
    }

    private boolean createMoviesLists(String email) {
        boolean [] ret = new boolean[1];

        Thread t = new Thread (() -> {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            ListaFilmDB listaFilmPreferiti = new ListaFilmDB(favourites, email);
            ListaFilmDB listaFilmDaVedere = new ListaFilmDB(toWatch, email);

            HttpEntity<ListaFilmDB> requestListaPreferitiEntity = new HttpEntity<>(listaFilmPreferiti, headers);
            HttpEntity<ListaFilmDB> requestListaDaVedereEntity = new HttpEntity<>(listaFilmDaVedere, headers);

            RestTemplate restTemplate = new RestTemplate();
            restTemplate.getMessageConverters().add(new StringHttpMessageConverter());
            String url = dbPath + "ListaFilm/add";

            ResponseEntity<ListaFilmDB> responsePreferiti = restTemplate.postForEntity(url, requestListaPreferitiEntity, ListaFilmDB.class);
            ResponseEntity<ListaFilmDB> responseDaVedere =  restTemplate.postForEntity(url, requestListaDaVedereEntity, ListaFilmDB.class);

            if(responsePreferiti.getStatusCode() == HttpStatus.OK &&
                   responseDaVedere.getStatusCode() == HttpStatus.OK)
                ret[0] = true;
        });
        t.start();

        try {
            t.join();
        }catch(InterruptedException ignore){}

        return ret[0];
    }

    @NotNull
    public Set<UserDB> getUsersByQuery(String query) {
        Set<UserDB> users = new HashSet<>();

        Thread t = new Thread(()-> {
            try {
                RestTemplate restTemplate = new RestTemplate();
                restTemplate.getMessageConverters().add(new StringHttpMessageConverter());

                String url = dbPath + "User/getUsersByQuery/{query}";

                ResponseEntity<Set<UserDB>> responseUsers = restTemplate.exchange(url, HttpMethod.GET,
                        null, new ParameterizedTypeReference<Set<UserDB>>() {}, query);

                if(!responseUsers.getBody().isEmpty())
                    users.addAll(responseUsers.getBody());

            }catch(HttpClientErrorException ignore){}
        });
        t.start();

        try {
            t.join();
        }catch(InterruptedException ignore){}

        return users;
    }

    public boolean isUserAlreadyRegistered(String email) {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getMessageConverters().add(new StringHttpMessageConverter());
        String url = dbPath + "User/getById/{email}";

        if(email == null) return false;

        try {
            final UserDB[] userDB = new UserDB[1];
            Thread t = new Thread(()-> userDB[0] = restTemplate.getForObject(url, UserDB.class, email));
            t.start();

            try{
                t.join();
            }catch(InterruptedException ignore){}

            if(userDB[0] != null)
                return true;
        }catch(HttpClientErrorException ignore){}

        return false;
    }

    @Nullable
    public UserDB getSocialLoggedUser(@NotNull Activity activity) {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getMessageConverters().add(new StringHttpMessageConverter());
        String url = dbPath + "User/getById/{email}";

        String[] email = new String[1];
        email[0] = tryToGetFacebookEmail();
        if(email[0] == null || email[0].equals(""))
            email[0] = tryToGetGoogleEmail(activity);

        if(email[0] == null) return null;

        final UserDB retUser = new UserDB();
        try {
            Thread t = new Thread(()-> {
                //Usa l'email social per identificare l'utente nel Database interno
                try {
                    UserDB userDB = restTemplate.getForObject(url, UserDB.class, email[0]);
                    retUser.setNome(userDB.getNome());
                    retUser.setCognome(userDB.getCognome());
                    retUser.setUsername(userDB.getUsername());
                    retUser.setEmail(email[0]);
                }catch(Exception ignore){}
            });
            t.start();

            try {
                t.join();
            }catch(InterruptedException ignore){}
        }catch(RestClientException ignore){}

        if(retUser.getEmail() == null)
            return null;

        return retUser;
    }

    public String getSocialUserEmail(Activity activity) {
        String email;
        email = tryToGetFacebookEmail();
        if(email == null || email.equals(""))
            email = tryToGetGoogleEmail(activity);
        return email;
    }

    @Nullable
    private String tryToGetFacebookEmail() {
        final String[] facebookEmail = new String[1];
        facebookEmail[0] = null;
        AccessToken fbAccessToken = AccessToken.getCurrentAccessToken();

        if(fbAccessToken == null) return null;

        GraphRequest request = GraphRequest.newMeRequest(
                AccessToken.getCurrentAccessToken(),
                (object, response) -> {
                    try {
                        if (object.has("email"))
                            facebookEmail[0] = object.getString("email");
                        else facebookEmail[0] = null;
                    } catch (JSONException ignore) {}
                });
        Bundle parameters = new Bundle();
        parameters.putString("fields", "email");
        request.setParameters(parameters);

        Thread t = new Thread(request::executeAndWait);
        t.start();

        try {
            t.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return facebookEmail[0];
    }

    @Nullable
    private String tryToGetGoogleEmail(Activity activity) {
        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(activity);
        if (acct != null)
            return acct.getEmail();

        return null;
    }

    public boolean addFriend(String emailUser, String emailFriend) {
        boolean[] ret = new boolean[1];
        Thread t = new Thread(()-> {
            try {
                RestTemplate restTemplate = new RestTemplate();
                restTemplate.getMessageConverters().add(new StringHttpMessageConverter());

                String url = dbPath + "User/addFriend/{Email_Utente}/{Email_Amico}";

                ResponseEntity<String> responseEntity = restTemplate.postForEntity(url, null, String.class, emailUser, emailFriend);

                if(responseEntity.getStatusCode() == HttpStatus.OK)
                    ret[0] = true;
            } catch(HttpClientErrorException ignore) {}
        });

        t.start();

        try {
            t.join();
        } catch (InterruptedException ignore) {}

        return ret[0];
    }

    public boolean removeFriend(String userEmail, String friendEmail) {
        boolean[] ret = new boolean[1];
        Thread t = new Thread(()-> {
            try {
                RestTemplate restTemplate = new RestTemplate();
                restTemplate.getMessageConverters().add(new StringHttpMessageConverter());

                String url = dbPath + "User/deleteFriend/{Email_Utente}/{Email_Amico}";

                ResponseEntity<String> responseEntity = restTemplate.postForEntity(url, null, String.class, userEmail, friendEmail);

                if(responseEntity.getStatusCode() == HttpStatus.OK)
                    ret[0] = true;
            } catch(HttpClientErrorException ignore) {}
        });

        t.start();

        try {
            t.join();
        } catch (InterruptedException ignore) {}

        return ret[0];
    }

    public boolean confirmFriendRequest(String emailUser, String emailFriend) {
        boolean[] ret = new boolean[1];
        Thread t = new Thread(()-> {
            try {
                RestTemplate restTemplate = new RestTemplate();
                restTemplate.getMessageConverters().add(new StringHttpMessageConverter());

                String url = dbPath + "User/confirmFriendRequest/{Email_Utente}/{Email_Amico}";

                ResponseEntity<String> responseEntity = restTemplate.postForEntity(url, null, String.class,  emailFriend, emailUser);

                if(responseEntity.getStatusCode() == HttpStatus.OK)
                    ret[0] = true;
            } catch(HttpClientErrorException ignore) {}
        });

        t.start();

        try {
            t.join();
        } catch (InterruptedException ignore) {}

        return ret[0];
    }

    public boolean isUserFriendshipPending(String userEmail, String friendEmail) {
        boolean[] ret = new boolean[1];

        Thread t = new Thread(()-> {
            try {
                RestTemplate restTemplate = new RestTemplate();
                restTemplate.getMessageConverters().add(new StringHttpMessageConverter());

                String url = dbPath + "User/isFriendRequestPending/{Email_Utente}/{Email_Amico}";

                ResponseEntity<Boolean> response = restTemplate.exchange(url, HttpMethod.GET,
                        null, new ParameterizedTypeReference<Boolean>() {}, userEmail, friendEmail);

                if(response.getBody() != null)
                    ret[0] = response.getBody();

            }catch(HttpClientErrorException ignore){}
        });
        t.start();

        try {
            t.join();
        }catch(InterruptedException ignore){}

        return ret[0];
    }

    @NotNull
    public Set<UserDB> getAllFriends(String userEmail) {
        Set<UserDB> users = new HashSet<>();

        Thread t = new Thread(()-> {
            try {
                RestTemplate restTemplate = new RestTemplate();
                restTemplate.getMessageConverters().add(new StringHttpMessageConverter());

                String url = dbPath + "User/getAllFriends/{email}";

                ResponseEntity<Set<UserDB>> responseUsers = restTemplate.exchange(url, HttpMethod.GET,
                        null, new ParameterizedTypeReference<Set<UserDB>>() {}, userEmail);

                if(!responseUsers.getBody().isEmpty())
                    users.addAll(responseUsers.getBody());

            }catch(HttpClientErrorException ignore){}
        });
        t.start();

        try {
            t.join();
        }catch(InterruptedException ignore){}

        return users;
    }

    @NotNull
    public Set<UserDB> getAllPendingFriendRequests(String userEmail) {
        Set<UserDB> users = new HashSet<>();

        Thread t = new Thread(()-> {
            try {
                RestTemplate restTemplate = new RestTemplate();
                restTemplate.getMessageConverters().add(new StringHttpMessageConverter());

                String url = dbPath + "User/getAllPendingFriendRequests/{email}";

                ResponseEntity<Set<UserDB>> responseUsers = restTemplate.exchange(url, HttpMethod.GET,
                        null, new ParameterizedTypeReference<Set<UserDB>>() {}, userEmail);

                if(!responseUsers.getBody().isEmpty())
                    users.addAll(responseUsers.getBody());

            }catch(HttpClientErrorException ignore){}
        });
        t.start();

        try {
            t.join();
        }catch(InterruptedException ignore){}

        return users;
    }
}
