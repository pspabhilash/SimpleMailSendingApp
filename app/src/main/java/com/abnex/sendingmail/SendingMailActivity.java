package com.abnex.sendingmail;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.graphics.PointF;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.BounceInterpolator;
import android.view.animation.RotateAnimation;
import android.view.animation.ScaleAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import com.jaouan.revealator.Revealator;
import com.jaouan.revealator.animations.AnimationListenerAdapter;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SendingMailActivity extends AppCompatActivity {

    @BindView(R.id.sendeditText) EditText editTextMessage;

    @BindView(R.id.emailid) EditText emailid;

    @BindView(R.id.send) View mFab;

    @BindView(R.id.plane) View mPlaneImageView;

    @BindView(R.id.plane_layout) View mPlaneLayout;

    @BindView(R.id.inputs_layout) View mInputsLayout;

    @BindView(R.id.sky_layout) View mSkyLayout;

    @BindView(R.id.sent_layout) View mSentLayout;

    @BindView(R.id.check) View mCheckImageView;

    boolean isConnected=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sending_mail);

        ButterKnife.bind(this);
    }

    private void sendEmail() {


        //Creating SendMail object
        SendMail sm = new SendMail(this, emailid.getText().toString().trim(), "From Simple Mail Sending App", editTextMessage.getText().toString().trim());

        //Executing sendmail to send email
        sm.execute();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @OnClick(R.id.send)
    void send() {
        hideSoftKeyboard();
        //check network connection
        if(!ConnectivityReceiver.isConnected(this)){
            Toast.makeText(this,"Please check internet connection",Toast.LENGTH_SHORT).show();
            return;
        }
        // validations
       if(!validateInput()){
           return;
       }

        // - Prepare views visibility.
        mCheckImageView.setVisibility(View.INVISIBLE);
        mSentLayout.setVisibility(View.INVISIBLE);

        // - Rotate fab.
        final RotateAnimation rotateAnimation = new RotateAnimation(0, 180, Animation.RELATIVE_TO_SELF, .5f, Animation.RELATIVE_TO_SELF, .5f);
        rotateAnimation.setInterpolator(new AccelerateDecelerateInterpolator());
        rotateAnimation.setDuration(280);
        mFab.startAnimation(rotateAnimation);

        // - Hide inputs layout.
        final Animator circularReveal = ViewAnimationUtils.createCircularReveal(mInputsLayout, (int) (mFab.getX() + mFab.getWidth() / 2), (int) (mFab.getY() + mFab.getHeight() / 2), mInputsLayout.getHeight(), 0);
        circularReveal.setDuration(250);
        circularReveal.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                // - Update views visibility.
                mInputsLayout.setVisibility(View.INVISIBLE);

                // - Fly away.
                flyAway();
            }
        });
        circularReveal.start();

        sendEmail();

        editTextMessage.setText("");
        emailid.setText("");


    }


    private void hideSoftKeyboard(){
        if(getCurrentFocus()!=null && getCurrentFocus() instanceof EditText){
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(editTextMessage.getWindowToken(), 0);
        }
    }

    private boolean validateInput() {
        String email=null;
        String message ="";
        // validating Email ID
        if(emailid.getText()!=null) {
            email = emailid.getText().toString().trim();
        }else{
            Toast.makeText(this,"Please enter Mail ID",Toast.LENGTH_SHORT).show();
        }

        if (email != null && !email.matches("[a-zA-Z0-9._-]+@[a-z]+.[a-z]+")) {

            emailid.setError(getString(R.string.error_invalid_password));

            Toast.makeText(this, "Please enter valid Mail ID", Toast.LENGTH_SHORT).show();
            emailid.setFocusable(true);
            return false;
        }
        if(editTextMessage.getText()!=null) message = editTextMessage.getText().toString().trim();

        if(message.equals("")) {
            Toast.makeText(this, "Please enter message",Toast.LENGTH_SHORT).show();
            editTextMessage.setFocusable(true);
            return false;
        }
        return true;
    }

    /**
     * Starts fly animation.
     */
    private void flyAway() {
        // - Combine rotation and translation animations.
        final RotateAnimation rotateAnimation = new RotateAnimation(0, 180);
        rotateAnimation.setDuration(1000);
        mPlaneImageView.startAnimation(rotateAnimation);
        Revealator.reveal(mSentLayout)
                .from(mPlaneLayout)
                .withTranslateDuration(1000)
                .withCurvedTranslation(new PointF(-1200, 0))
                .withRevealDuration(200)
                .withHideFromViewAtTranslateInterpolatedTime(.5f)
                .withEndAction(new Runnable() {
                    @Override
                    public void run() {

                        // - Display checked icon.
                        final ScaleAnimation scaleAnimation = new ScaleAnimation(0, 1, 0, 1, Animation.RELATIVE_TO_SELF, .5f, Animation.RELATIVE_TO_SELF, .5f);
                        scaleAnimation.setInterpolator(new BounceInterpolator());
                        scaleAnimation.setDuration(500);
                        scaleAnimation.setAnimationListener(new AnimationListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animation animation) {
                                mInputsLayout.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {

                                        // - Restore inputs layout.
                                        retoreInputsLayout();

                                    }
                                }, 1000);
                            }
                        });
                        mCheckImageView.startAnimation(scaleAnimation);
                        mCheckImageView.setVisibility(View.VISIBLE);

                    }
                }).start();
    }

    /**
     * Restores inputs layout.
     */
    private void retoreInputsLayout() {
        mInputsLayout.postDelayed(new Runnable() {
            @Override
            public void run() {

                final Animator circularReveal = ViewAnimationUtils.createCircularReveal(mInputsLayout, (int) (mFab.getX() + mFab.getWidth() / 2), (int) (mFab.getY() + mFab.getHeight() / 2), 0, mInputsLayout.getHeight());
                circularReveal.setDuration(250);
                circularReveal.start();

                mInputsLayout.setVisibility(View.VISIBLE);
            }
        }, 1000);
    }


}
