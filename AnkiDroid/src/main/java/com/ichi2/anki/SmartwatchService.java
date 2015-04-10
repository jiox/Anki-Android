package com.ichi2.anki;

import android.app.IntentService;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.UserDictionary;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.Loader;
import android.util.Log;

import com.ichi2.anim.ActivityTransitionAnimation;
import com.ichi2.async.CollectionLoader;
import com.ichi2.libanki.Card;
import com.ichi2.libanki.Collection;
import com.ichi2.libanki.Sched;
import com.ichi2.libanki.Utils;

import org.json.JSONException;

import java.util.List;

/**
 * Created by jiox on 02.12.14.
 */
public class SmartwatchService extends IntentService {
    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public SmartwatchService() {
        super(SmartwatchService.class.getName());
    }
    public SmartwatchService(String name) {
        super(SmartwatchService.class.getName());
        Log.i("SmartwatchService", "started");
        onHandleIntent(null);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.i("Service", "onHandleIntent");
        CollectionLoader loader = new CollectionLoader(this);
        Collection col = loader.loadInBackground();

        Sched sched = col.getSched();
        long deck = 0;
        try {
            deck = col.getDecks().current().getLong("id");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        List<Long> cids = sched.getCol().getDb().queryColumn(Long.class, "select id from cards where did = " + deck, 0);

        sched.sortCards(Utils.toPrimitive(cids), 1, 1, true, false);
        Log.i("length", "" + cids.size());
        Log.i("first", "" + cids.get(0));
        Card card = col.getCard(cids.get(0));
        Log.i("question", card._getQA().get("q"));
        Log.i("answer", card.getPureAnswerForReading());

        boolean direction = true;
        //direction = intent.getData().toString() == "true" ? true : false;
        if (intent.getExtras() != null)
        {
            direction = intent.getExtras().getBoolean("direction");
        }
        Log.i("direction", ""+direction);

        NotificationManagerCompat notificationManager =
                NotificationManagerCompat.from(this);
        notificationManager.cancel(1);

        int startID = Math.max(getCurrentCardID(), 0);
        double randomSeed = getCurrentRandomSeed();
        if (startID >= words.length)
        {
            randomSeed = Math.random();
            setCurrentRandomSeed(randomSeed);
            startID = 0;
        }
        if (startID > 0) {
            if (direction) {
                startID = startID + cardCount;
            } else {
                startID = Math.max(startID - cardCount, 0);
            }
            setCurrentCardID(startID);
        }
        Log.i("randomSeed", ""+randomSeed);

        permuteWords(randomSeed);

        Log.i("startID","" + startID);

        Service activity = this;
        Intent nextIntent = new Intent(this, SmartwatchService.class);
        nextIntent.setData(Uri.parse("true"));
        nextIntent.putExtra("direction", true);
        android.app.PendingIntent nextPendingIntent =
                PendingIntent.getService(
                        this,
                        0,
                        nextIntent,
                        0
                );
        Intent prevIntent = new Intent(this, SmartwatchService.class);
        prevIntent.setData(Uri.parse("false"));
        prevIntent.putExtra("direction", false);
        android.app.PendingIntent prevPendingIntent =
                PendingIntent.getService(
                        this,
                        0,
                        prevIntent,
                        0
                );
        // Create the action
        NotificationCompat.Action nextWordsAction =
                new NotificationCompat.Action.Builder(R.drawable.anki,
                        "Next words", nextPendingIntent)
                        .build();
        NotificationCompat.Action prevWordsAction =
                new NotificationCompat.Action.Builder(R.drawable.anki,
                        "Previous words", prevPendingIntent)
                        .build();
        int notificationId = 001;

        // Create a WearableExtender to add functionality for wearables
        long [] pattern = {0, 500};
        // Create a big text style for the second page

        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(activity)
                        .setSmallIcon(R.drawable.anki)
                        .setContentTitle(words[startID][0])
                        .setContentText(words[startID][1]).setDeleteIntent(prevPendingIntent);

        NotificationCompat.WearableExtender extender = new NotificationCompat.WearableExtender()
                .setHintHideIcon(true)
                .setHintShowBackgroundOnly(true)
                .setBackground(BitmapFactory.decodeResource(getResources(), R.drawable.words))
                .setCustomSizePreset(NotificationCompat.WearableExtender.SIZE_SMALL)
                .addAction(nextWordsAction)
                .addAction(prevWordsAction);

        for (int i = startID + 1; i < startID + cardCount && i < words.length; i++)
        {
            NotificationCompat.BigTextStyle secondPageStyle = new NotificationCompat.BigTextStyle();
            secondPageStyle.setBigContentTitle(words[i][0])
                    .bigText(words[i][1]);
            // Create second page notification
            Notification secondPageNotification =
                    new NotificationCompat.Builder(activity)
                            .setStyle(secondPageStyle)
                            .build();
            extender.addPage(secondPageNotification);
        }

        Notification notification = notificationBuilder.extend(extender).build();

// Build the notification and issues it with notification manager.
        notificationManager.notify(notificationId, notification);


        if (startID == 0)
        {
            if (direction) {
                setCurrentCardID(startID + cardCount);
            }
        }
    }


    public double getCurrentRandomSeed()
    {
        SharedPreferences sharedPref = getSharedPreferences("flashcards", Context.MODE_PRIVATE);
        return (double)sharedPref.getLong("randomSeed", 0);
    }
    public void setCurrentRandomSeed(double randomSeed)
    {
        SharedPreferences sharedPref = getSharedPreferences("flashcards", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putLong("randomSeed", (long) randomSeed);
        editor.commit();
    }
    public int getCurrentCardID()
    {
        SharedPreferences sharedPref = getSharedPreferences("flashcards", Context.MODE_PRIVATE);
        return sharedPref.getInt("current_id", 0);
    }
    public void setCurrentCardID(int id)
    {
        SharedPreferences sharedPref = getSharedPreferences("flashcards", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt("current_id", id);
        editor.commit();
    }
}
