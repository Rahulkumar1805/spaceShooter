package com.example.game1;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.os.Handler;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.Random;

public class game1 extends View {
    Context context;
    Bitmap background, lifeImage;
    Handler handler;
    long UPDATE_MILLIS= 30;
    static  int screenWidth, screenHeight;
    int points =0;
    int highScore=0;
    int life = 3;
    Paint scorePaint;
    int TEXT_SIZE = 80;
    boolean paused = false;
    OurSpaceship ourSpaceship;
    EnemySpaceship enemySpaceship;
    Random random;
    ArrayList<Shot> enemyShots, ourShots;
    Boolean enemyExplosion = false;
    Explosion explosion;
    ArrayList<Explosion> explosions;
    boolean enemyShotAction = false;
    SharedPreferences preferences;
    int getHighScore;
    final Runnable runnable = new Runnable() {
        @Override
        public void run() {
            invalidate();
        }
    };



    public  game1(Context context){
        super(context);
        this.context = context;
        random = new Random();
        enemyShots = new ArrayList<>();
        ourShots = new ArrayList<>();
        explosions = new ArrayList<>();
        Display display = ((Activity) getContext()).getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        screenWidth = size.x;
        screenHeight = size.y;
        ourSpaceship = new OurSpaceship(context);
        enemySpaceship = new EnemySpaceship(context);
        background = BitmapFactory.decodeResource(context.getResources(),R.drawable.space);
        lifeImage = BitmapFactory.decodeResource(context.getResources(), R.drawable.life);
        handler = new Handler();
        scorePaint = new Paint();
        scorePaint.setColor(Color.WHITE);
        scorePaint.setTextSize(TEXT_SIZE);
        scorePaint.setTextAlign(Paint.Align.LEFT);

        preferences = context.getSharedPreferences("highScore",Context.MODE_PRIVATE);
        getHighScore = preferences.getInt("flag",0);
        highScore= getHighScore;

    }
    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawBitmap(background, 0,0,null);
        canvas.drawText("Pts: " +points, 0, TEXT_SIZE, scorePaint);
        for(int i= life ;i>=1;i--){
            canvas.drawBitmap(lifeImage, screenWidth- lifeImage.getWidth()*i,0,null);
        }
        if (life == 0) {
            paused = true;
            handler = null;
            if (points > highScore) {
                highScore = points;
                // Save the new high score to SharedPreferences
                SharedPreferences.Editor editor = preferences.edit();
                editor.putInt("flag", highScore);
                editor.apply();
            }

            @SuppressLint("DrawAllocation") Intent intent = new Intent(context, GameOver.class);
            intent.putExtra("Points",points);
            intent.putExtra("HighScore",highScore);
            context.startActivity(intent);
              ((Activity) context).finish();

        }


        enemySpaceship.ex+=enemySpaceship.enemyVelocity;
        if(enemySpaceship.ex+ enemySpaceship.getEnemySpaceshipWidth()>= screenWidth){
            enemySpaceship.enemyVelocity*=-1;
        }
        if(enemySpaceship.ex<=0){
            enemySpaceship.enemyVelocity*=-1;
        }
        if((!enemyShotAction)&& (enemySpaceship.ex>=200+random.nextInt(400))){
            Shot enemyShot = new Shot(context, enemySpaceship.ex + enemySpaceship.getEnemySpaceshipWidth()/2,enemySpaceship.ey);
            enemyShots.add(enemyShot);
            enemyShotAction= true;
        }
        if(!enemyExplosion){
            canvas.drawBitmap(enemySpaceship.getEnemySpaceship(), enemySpaceship.ex, enemySpaceship.ey, null);
        }
        if(ourSpaceship.isAlive){
            if(ourSpaceship.ox>screenWidth- ourSpaceship.getOurSpaceshipWidth()){
                ourSpaceship.ox= screenWidth-ourSpaceship.getOurSpaceshipWidth();
            } else if(ourSpaceship.ox<0){
                ourSpaceship.ox=0;
            }
            canvas.drawBitmap(ourSpaceship.getOurSpaceship(),ourSpaceship.ox, ourSpaceship.oy,null);
        }
        for (int i =0;i<enemyShots.size();i++){
            enemyShots.get(i).shy+=50;
            canvas.drawBitmap(enemyShots.get(i).getShot(),enemyShots.get(i).shx, enemyShots.get(i).shy,null);
            if((enemyShots.get(i).shx>=ourSpaceship.ox)
            && (enemyShots.get(i).shx<=ourSpaceship.ox+ ourSpaceship.getOurSpaceshipWidth())
            && (enemyShots.get(i).shy>= ourSpaceship.oy)
            && (enemyShots.get(i).shy<=screenWidth)){
                life--;
                enemyShots.remove(i);
                explosion = new Explosion(context,ourSpaceship.ox, ourSpaceship.oy);
                explosions.add(explosion);
            } else if (enemyShots.get(i).shy>= screenHeight) {
                enemyShots.remove(i);
            }
            if(enemyShots.size()==0){
                enemyShotAction= false;
            }
        }

        for(int i = 0;i<ourShots.size();i++){
            ourShots.get(i).shy-=30;
            canvas.drawBitmap(ourShots.get(i).getShot(), ourShots.get(i).shx, ourShots.get(i).shy, null);
            if((ourShots.get(i).shx>=enemySpaceship.ex)
            && (ourShots.get(i).shx<=enemySpaceship.ex+ enemySpaceship.getEnemySpaceshipWidth())
            && (ourShots.get(i).shy<=enemySpaceship.getEnemySpaceshipHeight())
            && (ourShots.get(i).shy>=enemySpaceship.ey)){
                points++;
                ourShots.remove(i);
                explosion= new Explosion(context , enemySpaceship.ex, enemySpaceship.ey);
                explosions.add(explosion);
            }
            else if(ourShots.get(i).shy<=0){
                ourShots.remove(i);
            }
        }

        for(int i= 0;i<explosions.size();i++){
            canvas.drawBitmap(explosions.get(i).getExplosion(explosions.get(i).explosionFrame), explosions.get(i).ex, explosions.get(i).ey, null);
            explosions.get(i).explosionFrame++;
            if(explosions.get(i).explosionFrame>11){
                explosions.remove(i);
            }
        }
        if(!paused)
            handler.postDelayed(runnable, UPDATE_MILLIS);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int touchX= (int) event.getX();
        if(event.getAction()== MotionEvent.ACTION_UP){
            if(ourShots.size()<3){
                Shot ourShot = new Shot (context, ourSpaceship.ox+ourSpaceship.getOurSpaceshipWidth()/2, ourSpaceship.oy);
                ourShots.add(ourShot);
            }
        }
        if(event.getAction()== MotionEvent.ACTION_DOWN){
            ourSpaceship.ox=touchX;
        }
        if(event.getAction()==MotionEvent.ACTION_MOVE){
            ourSpaceship.ox= touchX;
        }
        return true;
    }
}
