package it.unina.ingSw.cineMates20.view.activity;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.denzcoskun.imageslider.ImageSlider;
import com.denzcoskun.imageslider.constants.ScaleTypes;
import com.denzcoskun.imageslider.models.SlideModel;
import com.squareup.picasso.Picasso;
import com.abdulhakeem.seemoretextview.SeeMoreTextView;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import it.unina.ingSw.cineMates20.R;
import it.unina.ingSw.cineMates20.controller.ShowDetailsMovieController;

public class ShowDetailsMovieActivity extends AppCompatActivity {

    private ShowDetailsMovieController showDetailsMovieController;

    private ImageSlider backgroundImageSlider;
    private TextView titleMovieTextView,
                     yearTextView,
                     directorTextView,
                     categoryTextView,
                     lengthTextView,
                     ratingTextView,
                     etaMinimaTextView;
    private ImageView coverImageView;
    private SeeMoreTextView descrizioneTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        showDetailsMovieController = ShowDetailsMovieController.getShowDetailsMovieControllerInstance();
        showDetailsMovieController.setShowDetailsMovieActivity(this);

        initializeGraphicsComponents();
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

        //TODO: aggiungere altri parametri e settarli in setMovieDetails

        showDetailsMovieController.initializeShowDetailsMovieActivity();
    }

    public void setBackgroundImageSlider(@NotNull List<SlideModel> backgroundImage){
        if(!backgroundImage.isEmpty())
            backgroundImageSlider.setImageList(backgroundImage, ScaleTypes.CENTER_CROP);
        else {
            backgroundImage.add(new SlideModel(R.drawable.backdrop_image_not_found, ScaleTypes.CENTER_CROP));
            backgroundImageSlider.setImageList(backgroundImage, ScaleTypes.CENTER_CROP);
        }
    }

    public void setMovieDetails(String title, String year, String director,
                                String category, String length, String rating,
                                String age, String coverUrl, String description) {
        titleMovieTextView.setText(title);
        yearTextView.setText(year);
        directorTextView.setText(director);
        categoryTextView.setText(category);
        lengthTextView.setText(length);
        ratingTextView.setText(rating);
        etaMinimaTextView.setText(age);

        if(description != null && !description.equals("")) {
            //seemoreTv.setTextMaxLength(300) //Default è 250
            descrizioneTextView.setSeeMoreText("Mostra altro", "Mostra meno");
            descrizioneTextView.setSeeMoreTextColor(R.color.blue);
            descrizioneTextView.setElegantTextHeight(false);
            descrizioneTextView.setContent(description);
        }
        else
            descrizioneTextView.setContent(getResources().getString(R.string.unavailable));

        if(coverUrl != null)
            Picasso.get().load(getResources().getString(R.string.first_path_poster_image) + coverUrl).
                    resize(270, 360)
                    .noFade().into(coverImageView);
    }
}