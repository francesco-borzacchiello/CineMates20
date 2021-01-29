package it.unina.ingSw.cineMates20.view.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.abdulhakeem.seemoretextview.SeeMoreTextView;
import com.denzcoskun.imageslider.ImageSlider;
import com.denzcoskun.imageslider.constants.ScaleTypes;
import com.denzcoskun.imageslider.models.SlideModel;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
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
    private ImageView coverImageView,
                      expandedCoverImageView,
                      expandedCoverBackgroundImageView;
    private SeeMoreTextView descrizioneTextView;
    private RecyclerView actorsRecyclerView;
    private FloatingActionsMenu floatingActionsMenu;
    private FloatingActionButton favouritesActionButton,
                                 toWatchActionButton;
    private Animator currentAnimator;
    private int shortAnimationDuration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        OnBackPressedCallback callback = new OnBackPressedCallback(true ) {
            @Override
            public void handleOnBackPressed() {
                if (floatingActionsMenu.isExpanded())
                    floatingActionsMenu.collapse();
                else if(expandedCoverImageView.getVisibility() == View.VISIBLE)
                    expandedCoverImageView.performClick();
                else {
                    finish();
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                }
            }
        };
        getOnBackPressedDispatcher().addCallback(this, callback);

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
        expandedCoverImageView = findViewById(R.id.expanded_cover);
        expandedCoverBackgroundImageView = findViewById(R.id.expanded_cover_background);
        descrizioneTextView = findViewById(R.id.descriptionMovieShowDetails);
        actorsRecyclerView = findViewById(R.id.movieCastRecyclerView);

        castTextView = findViewById(R.id.castMovieTextView);

        showDetailsMovieController.initializeShowDetailsMovieActivity();

        floatingActionsMenu = findViewById(R.id.movieDetailsFloatingActionsMenu);
        favouritesActionButton = findViewById(R.id.favouritesFloatingActionButton);
        toWatchActionButton = findViewById(R.id.toWatchFloatingActionButton);
        FloatingActionButton homepageActionButton = findViewById(R.id.homepageFloatingActionButton);

        if(showDetailsMovieController.parentIsMoviesListActivity()) {
            if(showDetailsMovieController.isHomePageAvailable()) {
                homepageActionButton.setOnClickListener(showDetailsMovieController.getHomePageOnClickListener());
                favouritesActionButton.setVisibility(View.GONE);
                toWatchActionButton.setVisibility(View.GONE);
            }
            else
                floatingActionsMenu.setVisibility(View.GONE);
        }
        else {
            favouritesActionButton = findViewById(R.id.favouritesFloatingActionButton);
            toWatchActionButton = findViewById(R.id.toWatchFloatingActionButton);

            if(showDetailsMovieController.isHomePageAvailable())
                homepageActionButton.setOnClickListener(showDetailsMovieController.getHomePageOnClickListener());
            else
                homepageActionButton.setVisibility(View.GONE);

            if (showDetailsMovieController.isSelectedMovieAlreadyInList(true)) {
                favouritesActionButton.setColorNormalResId(R.color.red);
                favouritesActionButton.setColorPressedResId(R.color.darkRed);
                favouritesActionButton.setOnClickListener(showDetailsMovieController.getRimuoviPreferitiOnClickListener());
                favouritesActionButton.setTitle("Rimuovi dai film \"Preferiti\"");
            } else
                favouritesActionButton.setOnClickListener(showDetailsMovieController.getAggiungiPreferitiOnClickListener());

            if (showDetailsMovieController.isSelectedMovieAlreadyInList(false)) {
                toWatchActionButton.setColorNormalResId(R.color.red);
                toWatchActionButton.setColorPressedResId(R.color.darkRed);
                toWatchActionButton.setOnClickListener(showDetailsMovieController.getRimuoviDaVedereOnClickListener());
                toWatchActionButton.setTitle("Rimuovi dai film \"Da vedere\"");
            } else
                toWatchActionButton.setOnClickListener(showDetailsMovieController.getAggiungiDaVedereOnClickListener());
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

    @Override
    public boolean dispatchTouchEvent(@NotNull MotionEvent event){
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (floatingActionsMenu.isExpanded()) {
                Rect outRect = new Rect();
                floatingActionsMenu.getGlobalVisibleRect(outRect);
                if(!outRect.contains((int)event.getRawX(), (int)event.getRawY())) {
                    floatingActionsMenu.collapse();
                    return false;
                }
            }
        }
        return super.dispatchTouchEvent(event);
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

        if(rating != null && rating.equals("0.0")) {
            ratingTextView.setText(getResources().getString(R.string.unavailable));
            ImageView ratingStar = findViewById(R.id.ratingStar);
            ratingStar.setVisibility(View.GONE);
        }
        else
            ratingTextView.setText(rating);

        if(description != null && !description.equals("")) {
            descrizioneTextView.setSeeMoreText("Mostra altro", "Mostra meno");
            descrizioneTextView.setSeeMoreTextColor(R.color.lightBlueVariant);
            descrizioneTextView.setElegantTextHeight(false);
            descrizioneTextView.setContent(description);
        }
        else
            descrizioneTextView.setContent(getResources().getString(R.string.unavailable));

        if(coverUrl != null && !coverUrl.equals("")) {
            Picasso.get().load(getResources().getString(R.string.first_path_image) + coverUrl).
                    noFade().into(coverImageView);

            Picasso.get().load(getResources().getString(R.string.first_path_image_original) + coverUrl).
                    noFade().into(expandedCoverImageView);

            coverImageView.setOnClickListener(view ->
                    zoomImageFromThumb(coverUrl));

            // Recupera e memorizza il tempo di animazione "short" del sistema.
            shortAnimationDuration = getResources().getInteger(android.R.integer.config_shortAnimTime);
        }
    }

    public void setMovieCastRecyclerView(RecyclerView.Adapter<RecyclerView.ViewHolder> adapter) {
        new Handler(Looper.getMainLooper()).postAtFrontOfQueue(()-> runOnUiThread(()-> {
            actorsRecyclerView.setAdapter(adapter);

            actorsRecyclerView.setItemViewCacheSize(10);

            actorsRecyclerView.setLayoutManager(new LinearLayoutManager
                    (this, LinearLayoutManager.HORIZONTAL, false));
        }));
    }

    public void hideCastTextView() {
        runOnUiThread(()-> castTextView.setVisibility(View.GONE));
    }

    public void changeAddFavouritesButtonToRemove() {
        runOnUiThread(()-> {
            favouritesActionButton.setColorNormalResId(R.color.red);
            favouritesActionButton.setColorPressedResId(R.color.darkRed);
            favouritesActionButton.setOnClickListener(showDetailsMovieController.getRimuoviPreferitiOnClickListener());
            favouritesActionButton.setTitle("Rimuovi dai film \"Preferiti\"");
        });
        Utilities.stampaToast(this, "Aggiunto ai film \"Preferiti\"");
    }

    public void changeAddToWatchButtonToRemove() {
        runOnUiThread(()-> {
            toWatchActionButton.setColorNormalResId(R.color.red);
            toWatchActionButton.setColorPressedResId(R.color.darkRed);
            toWatchActionButton.setOnClickListener(showDetailsMovieController.getRimuoviDaVedereOnClickListener());
            toWatchActionButton.setTitle("Rimuovi dai film \"Da vedere\"");
        });
        Utilities.stampaToast(this, "Aggiunto ai film \"Da vedere\"");
    }

    public void changeRemoveFavouritesButtonToAdd() {
        runOnUiThread(()-> {
            favouritesActionButton.setColorNormalResId(R.color.lightBlueButton);
            favouritesActionButton.setColorPressedResId(R.color.lightBlue);
            favouritesActionButton.setOnClickListener(showDetailsMovieController.getAggiungiPreferitiOnClickListener());
            favouritesActionButton.setTitle("Aggiungi ai film \"Preferiti\"");
        });
        Utilities.stampaToast(this, "Rimosso dai film \"Preferiti\"");
    }

    public void changeRemoveToWatchButtonToAdd() {
        runOnUiThread(()-> {
            toWatchActionButton.setColorNormalResId(R.color.lightBlueButton);
            toWatchActionButton.setColorPressedResId(R.color.lightBlue);
            toWatchActionButton.setOnClickListener(showDetailsMovieController.getAggiungiDaVedereOnClickListener());
            toWatchActionButton.setTitle("Aggiungi ai film \"Da vedere\"");
        });
        Utilities.stampaToast(this, "Rimosso dai film \"Da Vedere\"");
    }

    public void temporarilyDisableFavouritesButton() {
        runOnUiThread(()-> favouritesActionButton.setEnabled(false));
        Timer buttonTimer = new Timer();
        buttonTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(() -> favouritesActionButton.setEnabled(true));
            }
        }, 2000);
    }

    public void temporarilyDisableToWatchButton() {
        runOnUiThread(()-> toWatchActionButton.setEnabled(false));
        Timer buttonTimer = new Timer();
        buttonTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(() -> toWatchActionButton.setEnabled(true));
            }
        }, 2000);
    }

    public void collapseFloatingActionMenu() {
        if(floatingActionsMenu != null)
            runOnUiThread(()-> floatingActionsMenu.collapse());
    }

    //Metodo fornito dalla documentazione di Android per lo zoom di un'immagine
    private void zoomImageFromThumb(String imageUrl) {
        if (currentAnimator != null)
            currentAnimator.cancel();

        expandedCoverImageView.setAlpha(0f);
        expandedCoverImageView.animate().setDuration(300).alpha(1f).start();
        expandedCoverBackgroundImageView.setAlpha(0f);
        expandedCoverBackgroundImageView.animate().setDuration(300).alpha(1f).start();
        floatingActionsMenu.setVisibility(View.INVISIBLE);

        final Rect startBounds = new Rect();
        final Rect finalBounds = new Rect();
        final Point globalOffset = new Point();

        coverImageView.getGlobalVisibleRect(startBounds);
        findViewById(R.id.layoutRootMovieDetails)
                .getGlobalVisibleRect(finalBounds, globalOffset);
        startBounds.offset(-globalOffset.x, -globalOffset.y);
        finalBounds.offset(-globalOffset.x, -globalOffset.y);

        float startScale;
        if ((float) finalBounds.width() / finalBounds.height()
                > (float) startBounds.width() / startBounds.height()) {
            // Extend start bounds horizontally
            startScale = (float) startBounds.height() / finalBounds.height();
            float startWidth = startScale * finalBounds.width();
            float deltaWidth = (startWidth - startBounds.width()) / 2;
            startBounds.left -= deltaWidth;
            startBounds.right += deltaWidth;
        } else {
            // Extend start bounds vertically
            startScale = (float) startBounds.width() / finalBounds.width();
            float startHeight = startScale * finalBounds.height();
            float deltaHeight = (startHeight - startBounds.height()) / 2;
            startBounds.top -= deltaHeight;
            startBounds.bottom += deltaHeight;
        }

        coverImageView.setAlpha(0f);
        expandedCoverImageView.setVisibility(View.VISIBLE);
        expandedCoverBackgroundImageView.setVisibility(View.VISIBLE);

        expandedCoverImageView.setPivotX(0f);
        expandedCoverImageView.setPivotY(0f);

        AnimatorSet set = new AnimatorSet();
        set.play(ObjectAnimator.ofFloat(expandedCoverImageView, View.X,
                startBounds.left, finalBounds.left))
                .with(ObjectAnimator.ofFloat(expandedCoverImageView, View.Y,
                        startBounds.top, finalBounds.top))
                .with(ObjectAnimator.ofFloat(expandedCoverImageView, View.SCALE_X,
                        startScale, 1f))
                .with(ObjectAnimator.ofFloat(expandedCoverImageView,
                        View.SCALE_Y, startScale, 1f));
        set.setDuration(shortAnimationDuration);
        set.setInterpolator(new DecelerateInterpolator());
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) { currentAnimator = null; }

            @Override
            public void onAnimationCancel(Animator animation) { currentAnimator = null; }
        });
        set.start();
        currentAnimator = set;

        final float startScaleFinal = startScale;
        expandedCoverImageView.setOnClickListener(view -> {
            if (currentAnimator != null)
                currentAnimator.cancel();

            AnimatorSet set1 = new AnimatorSet();
            set1.play(ObjectAnimator.ofFloat(expandedCoverImageView, View.X, startBounds.left))
                    .with(ObjectAnimator
                            .ofFloat(expandedCoverImageView, View.Y,startBounds.top))
                    .with(ObjectAnimator
                            .ofFloat(expandedCoverImageView, View.SCALE_X, startScaleFinal))
                    .with(ObjectAnimator
                            .ofFloat(expandedCoverImageView, View.SCALE_Y, startScaleFinal));
            set1.setDuration(shortAnimationDuration);
            set1.setInterpolator(new DecelerateInterpolator());
            set1.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    coverImageView.setAlpha(1f);
                    expandedCoverImageView.setVisibility(View.GONE);
                    expandedCoverBackgroundImageView.setAlpha(1f);
                    expandedCoverBackgroundImageView.setVisibility(View.GONE);
                    floatingActionsMenu.setVisibility(View.VISIBLE);
                    currentAnimator = null;
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                    coverImageView.setAlpha(1f);
                    expandedCoverImageView.setVisibility(View.GONE);
                    expandedCoverBackgroundImageView.setAlpha(1f);
                    expandedCoverBackgroundImageView.setVisibility(View.GONE);
                    floatingActionsMenu.setVisibility(View.VISIBLE);
                    currentAnimator = null;
                }
            });
            set1.start();
            currentAnimator = set1;
        });
    }
}