package com.example.game1;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.util.Random;

public class EnemySpaceship {
    Context context;
    Bitmap enemySpaceship;
    int ex, ey;
    int enemyVelocity;
    Random random;

    public EnemySpaceship(Context context){
        this.context =context;
        enemySpaceship = BitmapFactory.decodeResource(context.getResources(), R.drawable.ship);
        random = new Random();
        resetEnemySpaceship();

    }

    public Bitmap getEnemySpaceship(){
        return enemySpaceship;
    }
    int getEnemySpaceshipWidth(){
        return enemySpaceship.getWidth();
    }
    int getEnemySpaceshipHeight(){
        return enemySpaceship.getHeight();
    }
    private void resetEnemySpaceship() {
        ex = 1000 + random.nextInt(400);
        ey = 0;
        enemyVelocity = 50 + random.nextInt(10);
    }

}
