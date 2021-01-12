package it.unina.ingSw.cineMates20.controller;

import android.app.Activity;
import android.content.Intent;

import com.denzcoskun.imageslider.constants.ScaleTypes;
import com.denzcoskun.imageslider.models.SlideModel;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.regex.PatternSyntaxException;

import info.movito.themoviedbapi.TmdbMovies;
import info.movito.themoviedbapi.model.Artwork;
import info.movito.themoviedbapi.model.ArtworkType;
import info.movito.themoviedbapi.model.Credits;
import info.movito.themoviedbapi.model.Genre;
import info.movito.themoviedbapi.model.MovieDb;
import info.movito.themoviedbapi.model.ReleaseDate;
import info.movito.themoviedbapi.model.ReleaseInfo;
import info.movito.themoviedbapi.model.people.PersonCrew;
import it.unina.ingSw.cineMates20.R;
import it.unina.ingSw.cineMates20.view.activity.ShowDetailsMovieActivity;

public class ShowDetailsMovieController {

    private static ShowDetailsMovieController instance;
    private static TmdbMovies tmdbMovies;
    private ShowDetailsMovieActivity showDetailsMovieActivity;
    private MovieDb actualMovie;

    private ShowDetailsMovieController() {}

    public static ShowDetailsMovieController getShowDetailsMovieControllerInstance() {
        if(instance == null) {
            instance = new ShowDetailsMovieController();
            tmdbMovies = new TmdbMovies(SearchMovieController.getTmdbApiInstance());
        }
        return instance;
    }

    public void setShowDetailsMovieActivity(ShowDetailsMovieActivity activity) {
        showDetailsMovieActivity = activity;
    }

    public void initializeShowDetailsMovieActivity() {
        String title;
        if(actualMovie.getTitle() != null)
            title = actualMovie.getTitle();
        else
            title = actualMovie.getOriginalTitle();
        String prefissoDataDiUscita  = showDetailsMovieActivity.getResources().getString(R.string.data_di_uscita),
               prefissoRegista = showDetailsMovieActivity.getResources().getString(R.string.regista),
               prefissoGenere = showDetailsMovieActivity.getResources().getString(R.string.genere),
               prefissoDurata = showDetailsMovieActivity.getResources().getString(R.string.durata),
               prefissoEtaMinima = showDetailsMovieActivity.getResources().getString(R.string.etaConsigliata);

        showDetailsMovieActivity.setBackgroundImageSlider(createListForBackgroundImage());
        showDetailsMovieActivity.setMovieDetails(title, prefissoDataDiUscita +
                getEuropeanMovieReleaseDate(actualMovie),
                prefissoRegista + findDirectorByMovieId(actualMovie.getId()),
                prefissoGenere + getMovieGenresById(actualMovie.getId()),
                prefissoDurata + getMovieRuntimeById(actualMovie.getId()),
                actualMovie.getVoteAverage() + "", prefissoEtaMinima +
                getAverageRecommendedAgeById(actualMovie.getId()), actualMovie.getPosterPath(),
                actualMovie.getOverview());
    }

    private String getEuropeanMovieReleaseDate(@NotNull MovieDb movie) {
        String usFormatDate = movie.getReleaseDate();

        if(usFormatDate != null && !usFormatDate.equals("")) {
            String[] arr = usFormatDate.split("-");
            return arr[2] + "-" + arr[1] + "-" + arr[0];
        }
        return showDetailsMovieActivity.getResources().getString(R.string.unavailable);
    }

    @NotNull
    private String getMovieRuntimeById(int id) {
        int runtime = tmdbMovies.getMovie(id,"it").getRuntime();
        if(runtime == 0)
            return showDetailsMovieActivity.getResources().getString(R.string.unavailable);
        return runtime + "m";
    }

    @NotNull
    private String getMovieGenresById(int id) {
        List<Genre> genres = tmdbMovies.getMovie(id, "it").getGenres();
        if(genres == null || genres.size() == 0)
            return showDetailsMovieActivity.getResources().getString(R.string.unavailable);

        try {
            String trimmedGenres = genres.toString().replaceAll("\\[\\d+]|\\[|]", "");
            return trimmedGenres.replaceAll(" ,", ",");
        }catch(PatternSyntaxException e) {
            return showDetailsMovieActivity.getResources().getString(R.string.unavailable);
        }
    }

    private String findDirectorByMovieId(int id) {
        String director = showDetailsMovieActivity.getResources().getString(R.string.unavailable);
        TmdbMovies tmdbMovies = new TmdbMovies(SearchMovieController.getTmdbApiInstance());
        Credits credits = tmdbMovies.getCredits(id);
        if(credits != null) {
            for(PersonCrew person : credits.getCrew()) {
                if(person.getJob().equals("Director"))
                    director = person.getName();
            }
        }
        return director;
    }

    private String getAverageRecommendedAgeById(int id) {
        List<ReleaseInfo> releaseInfo = tmdbMovies.getReleaseInfo(id, "it");
        String certification = showDetailsMovieActivity.getResources().getString(R.string.unavailable);
        int size = 0, sum = 0;

        for(ReleaseInfo info: releaseInfo) {
            List<ReleaseDate> releaseDate = info.getReleaseDates();
            for(ReleaseDate rd: releaseDate) {
                Scanner in = new Scanner(rd.getCertification()).useDelimiter("[^0-9]+");
                try{
                    sum += in.nextInt();
                    size++;
                }catch(NoSuchElementException ignore){}
            }
        }

        if(size > 0)
            certification = "" +  sum/size;

        return certification;
    }

    @NotNull
    private List<SlideModel> createListForBackgroundImage() {
        List<SlideModel> imageBackground = new ArrayList<>();
        int picturesCount = 0;

        TmdbMovies tmdbMovies = new TmdbMovies(SearchMovieController.getTmdbApiInstance());
        actualMovie.setImages(tmdbMovies.getImages(actualMovie.getId(), null));

        String firstPath = showDetailsMovieActivity.getResources().getString(R.string.first_path_backdrop_image);
        for(Artwork currentImageBackground :actualMovie.getImages(ArtworkType.BACKDROP)){
            try {
                imageBackground.add(new SlideModel(firstPath + currentImageBackground.getFilePath(), ScaleTypes.CENTER_CROP));
                picturesCount++;
            }catch(NullPointerException ignore){}

            if(picturesCount == 12)
                break;
        }

        return imageBackground;
    }

    public void start(Activity activityParent, MovieDb movie){
        actualMovie = movie;
        Intent intent = new Intent(activityParent, ShowDetailsMovieActivity.class);
        activityParent.startActivity(intent);
    }
}
