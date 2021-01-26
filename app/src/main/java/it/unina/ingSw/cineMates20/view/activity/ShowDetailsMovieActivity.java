package it.unina.ingSw.cineMates20.view.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.abdulhakeem.seemoretextview.SeeMoreTextView;
import com.denzcoskun.imageslider.ImageSlider;
import com.denzcoskun.imageslider.constants.ScaleTypes;
import com.denzcoskun.imageslider.models.SlideModel;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import it.unina.ingSw.cineMates20.R;
import it.unina.ingSw.cineMates20.controller.ShowDetailsMovieController;
import it.unina.ingSw.cineMates20.view.util.Utilities;

public class ShowDetailsMovieActivity extends AppCompatActivity {

    private ShowDetailsMovieController showDetailsMovieController;

    private ImageSlider backgroundImageSlider;
    private TextView titleMovieTextView,
                     yearTextView,
                     directorTextView,
                     categoryTextView,
                     lengthTextView,
                     ratingTextView,
                     etaMinimaTextView,
                     castTextView;
    private Button addToFavouritesButton,
                   addToWatchButton;
    private ImageView coverImageView;
    private SeeMoreTextView descrizioneTextView;
    private RecyclerView actorsRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        OnBackPressedCallback callback = new OnBackPressedCallback(true ) {
            @Override
            public void handleOnBackPressed() {
                finish();
                overridePendingTransition(0,0);
            }
        };
        getOnBackPressedDispatcher().addCallback(this, callback);

        showDetailsMovieController = ShowDetailsMovieController.getShowDetailsMovieControllerInstance();
        showDetailsMovieController.setShowDetailsMovieActivity(this);

        initializeGraphicsComponents();

        if(showDetailsMovieController.isParentMoviesListActivity())
            removeUnnecessaryViews();
    }

    private void initializeGraphicsComponents() {
        setContentView(R.layout.activity_show_details_movie);
        backgroundImageSlider = findViewById(R.id.backgroundSliderImageViewPaper);

        titleMovieTextView = findViewById(R.id.titleMovieShowDetails);
        yearTextView = findViewById(R.id.releaseDateMovieShowDetails);
        directorTextView = findViewById(R.id.directorMovieShowDetails);
        categoryTextView = findViewById(R.id.categoryMovieShowDetails);
        lengthTextView = findViewById(R.id.lengthMovieShowDetails);
        ratingTextView = findViewById(R.id.ratingValue);
        etaMinimaTextView = findViewById(R.id.ageMovieShowDetails);
        coverImageView = findViewById(R.id.details_cover_movie);
        descrizioneTextView = findViewById(R.id.descriptionMovieShowDetails);
        actorsRecyclerView = findViewById(R.id.movieCastRecyclerView);
        castTextView = findViewById(R.id.castMovieTextView);

        showDetailsMovieController.initializeShowDetailsMovieActivity();

        addToFavouritesButton = findViewById(R.id.addToFavouritesButton);
        addToWatchButton = findViewById(R.id.addToWatchButton);

        if(!showDetailsMovieController.isParentMoviesListActivity()) {
            if (showDetailsMovieController.isSelectedMovieAlreadyInList(true)) {
                addToFavouritesButton.setBackgroundTintList(AppCompatResources.getColorStateList(getApplicationContext(), R.color.red));
                addToFavouritesButton.setOnClickListener(showDetailsMovieController.getRimuoviPreferitiOnClickListener());
            } else
                addToFavouritesButton.setOnClickListener(showDetailsMovieController.getAggiungiPreferitiOnClickListener());

            if (showDetailsMovieController.isSelectedMovieAlreadyInList(false)) {
                addToWatchButton.setBackgroundTintList(AppCompatResources.getColorStateList(getApplicationContext(), R.color.red));
                addToWatchButton.setOnClickListener(showDetailsMovieController.getRimuoviDaVedereOnClickListener());
            } else
                addToWatchButton.setOnClickListener(showDetailsMovieController.getAggiungiDaVedereOnClickListener());
        }

        if(showDetailsMovieController.isTrailerAvailable()) {
            ImageView youtubeImageView = findViewById(R.id.youtubePlayImageView);
            youtubeImageView.setOnClickListener(showDetailsMovieController.getYoutubeImageViewOnClickListener());
        }
        else {
            LinearLayout youtubePlayLinearLayout = findViewById(R.id.youtubePlayLinearLayout);
            youtubePlayLinearLayout.setVisibility(View.GONE);

            float density = getResources().getDisplayMetrics().density;
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(Math.round(20*density), Math.round(20*density), 0, 0);
            castTextView.setLayoutParams(params);
        }
    }

    private void removeUnnecessaryViews() {
        TextView label = findViewById(R.id.showDetailsMovieButtonsLabel);
        label.setVisibility(View.GONE);
        LinearLayout buttonsLayout = findViewById(R.id.buttonsMovieDetailsLinearLayout);
        buttonsLayout.setVisibility(View.GONE);
        View bottomViewMovieDetails = findViewById(R.id.bottomViewMovieDetails);
        bottomViewMovieDetails.setVisibility(View.GONE);
    }

    public void setBackgroundImageSlider(@NotNull List<SlideModel> backgroundImage){
        if(backgroundImage.isEmpty())
            backgroundImage.add(new SlideModel(R.drawable.backdrop_image_not_found, ScaleTypes.CENTER_CROP));

        backgroundImageSlider.setImageList(backgroundImage, ScaleTypes.CENTER_CROP);
    }

    public void setMovieDetails(String title, String year, String director,
                                String category, String length, String rating,
                                String age, String coverUrl, String description) {
        titleMovieTextView.setText(title);
        yearTextView.setText(year);
        directorTextView.setText(director);
        categoryTextView.setText(category);
        lengthTextView.setText(length);
        etaMinimaTextView.setText(age);

        if(rating.equals("0.0")) {
            ratingTextView.setText(getResources().getString(R.string.unavailable));
            ImageView ratingStar = findViewById(R.id.ratingStar);
            ratingStar.setVisibility(View.GONE);
        }
        else
            ratingTextView.setText(rating);

        if(description != null && !description.equals("")) {
            //seemoreTv.setTextMaxLength(300) //Default è 250
            descrizioneTextView.setSeeMoreText("Mostra altro", "Mostra meno");
            descrizioneTextView.setSeeMoreTextColor(R.color.lightBlueVariant);
            descrizioneTextView.setElegantTextHeight(false);
            descrizioneTextView.setContent(description);
        }
        else
            descrizioneTextView.setContent(getResources().getString(R.string.unavailable));

        if(coverUrl != null)
            Picasso.get().load(getResources().getString(R.string.first_path_poster_image) + coverUrl).
                    noFade().into(coverImageView);
    }

    public void setMovieCastRecyclerView(RecyclerView.Adapter<RecyclerView.ViewHolder> adapter) {
        actorsRecyclerView.setAdapter(adapter);

        actorsRecyclerView.setItemViewCacheSize(10);

        actorsRecyclerView.setLayoutManager(new LinearLayoutManager
                (this, LinearLayoutManager.HORIZONTAL, false));
    }

    public void hideCastTextView() {
        castTextView.setVisibility(View.GONE);
    }

    public void changeAddFavouritesButtonToRemove() {
        runOnUiThread(()-> {
            addToFavouritesButton.setBackgroundTintList(AppCompatResources.getColorStateList(getApplicationContext(), R.color.red));
            addToFavouritesButton.setOnClickListener(showDetailsMovieController.getRimuoviPreferitiOnClickListener());
        });
        Utilities.stampaToast(this, "Film aggiunto alla lista dei preferiti");
    }

    public void changeAddToWatchButtonToRemove() {
        runOnUiThread(()-> {
            addToWatchButton.setBackgroundTintList(AppCompatResources.getColorStateList(getApplicationContext(), R.color.red));
            addToWatchButton.setOnClickListener(showDetailsMovieController.getRimuoviDaVedereOnClickListener());
        });
        Utilities.stampaToast(this, "Film aggiunto alla lista da vedere");
    }

    public void changeRemoveFavouritesButtonToAdd() {
        runOnUiThread(()-> {
            addToFavouritesButton.setBackgroundTintList(AppCompatResources.getColorStateList(getApplicationContext(), R.color.lightBlueVariant));
            addToFavouritesButton.setOnClickListener(showDetailsMovieController.getAggiungiPreferitiOnClickListener());
        });
        Utilities.stampaToast(this, "Film rimosso dalla lista dei preferiti");
    }

    public void changeRemoveToWatchButtonToAdd() {
        runOnUiThread(()-> {
            addToWatchButton.setBackgroundTintList(AppCompatResources.getColorStateList(getApplicationContext(), R.color.lightBlueVariant));
            addToWatchButton.setOnClickListener(showDetailsMovieController.getAggiungiDaVedereOnClickListener());
        });
        Utilities.stampaToast(this, "Film rimosso dalla lista da vedere");
    }

    public void temporarilyDisableFavouritesButton() {
        addToFavouritesButton.setEnabled(false);
        Timer buttonTimer = new Timer();
        buttonTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(() -> addToFavouritesButton.setEnabled(true));
            }
        }, 3000);
    }

    public void temporarilyDisableToWatchButton() {
        addToWatchButton.setEnabled(false);
        Timer buttonTimer = new Timer();
        buttonTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(() -> addToWatchButton.setEnabled(true));
            }
        }, 3000);
    }
}