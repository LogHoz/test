
/**
 * Tilt Lander: an accelerometer-controlled moon landing game for Android.
 * <br>Copyright (C) 2007 Google Inc.
 * 
 * <p>Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 * 
 * <p>Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */


package org.hermit.tiltlander;


import org.hermit.android.core.MainActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Window;
import android.widget.TextView;


/**
 * This is a simple LunarLander activity that houses a single GameView. It
 * demonstrates...
 * <ul>
 * <li>animating by calling invalidate() from draw()
 * <li>loading and drawing resources
 * <li>handling onPause() in an animation
 * </ul>
 */
public class LunarLander
	extends MainActivity
{
    
    // ******************************************************************** //
    // Activity Setup.
    // ******************************************************************** //

    /**
     * Called when the activity is starting.  This is where most
     * initialization should go: calling setContentView(int) to inflate
     * the activity's UI, etc.
     * 
     * You can call finish() from within this function, in which case
     * onDestroy() will be immediately called without any of the rest of
     * the activity lifecycle executing.
     * 
     * Derived classes must call through to the super class's implementation
     * of this method.  If they do not, an exception will be thrown.
     * 
     * @param   icicle          If the activity is being re-initialized
     *                          after previously being shut down then this
     *                          Bundle contains the data it most recently
     *                          supplied in onSaveInstanceState(Bundle).
     *                          Note: Otherwise it is null.
     */
    @Override
    protected void onCreate(Bundle icicle) {
        Log.i(TAG, "onCreate(): " +
                        (icicle == null ? "clean start" : "restart"));
    
        super.onCreate(icicle);

        // Get our power manager for wake locks.
        powerManager = (PowerManager) getSystemService(POWER_SERVICE);

        // Set up the standard dialogs.
        setAboutInfo(R.string.about_text);
        setHomeInfo(R.string.url_homepage);
        setLicenseInfo(R.string.url_license);

        // turn off the window's title bar
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        // tell system to use the layout defined in our XML file
        setContentView(R.layout.lunar_layout);

        // get handles to the GameView from XML.
        mLunarView = (GameView) findViewById(R.id.lunar);

        // give the LunarView a handle to the TextView used for messages
        mLunarView.setTextView((TextView) findViewById(R.id.text));
        
        // Restore our preferences.
        updatePreferences();

        if (icicle == null) {
            Log.w(this.getClass().getName(), "SIS is null");
        } else {
            // we are being restored: resume a previous game
            mLunarView.restoreState(icicle);
            Log.w(this.getClass().getName(), "SIS is nonnull");
        }
    }

    
    /**
     * Called after onCreate(Bundle) or onStop() when the current activity is
     * now being displayed to the user.  It will be followed by onResume()
     * if the activity comes to the foreground, or onStop() if it becomes
     * hidden.
     * 
     * Derived classes must call through to the super class's implementation
     * of this method.  If they do not, an exception will be thrown.
     */
    @Override
    protected void onStart() {
        Log.i(TAG, "onStart()");
        super.onStart();
        
        mLunarView.onStart();
    }
    

    /**
     * Called after onRestoreInstanceState(Bundle), onRestart(), or onPause(),
     * for your activity to start interacting with the user. 
     */
    @Override
    protected void onResume() {
        Log.i(TAG, "onResume()");
        
        super.onResume();
        
        // Take the wake lock if we want it.
        if (wakeLock != null && !wakeLock.isHeld())
            wakeLock.acquire();

        mLunarView.onResume();
    }

    
    /**
     * Notification that something is about to happen, to give the Activity a
     * chance to save state.
     * 
     * @param outState a Bundle into which this Activity should save its state
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        Log.i(TAG, "onSaveInstanceState()");
        
        super.onSaveInstanceState(outState);
        
        // just have the View's thread save its state into our Bundle
        mLunarView.saveState(outState);
    }


    /**
     * Invoked when the Activity loses user focus.
     */
    @Override
    protected void onPause() {
        Log.i(TAG, "onPause()");

        super.onPause();
        
        mLunarView.onPause();

        // Let go the wake lock if we have it.
        if (wakeLock != null && wakeLock.isHeld())
            wakeLock.release();
    }

    
    /**
     * Called when you are no longer visible to the user.  You will next
     * receive either onStart(), onDestroy(), or nothing, depending on
     * later user activity.
     * 
     * Note that this method may never be called, in low memory situations
     * where the system does not have enough memory to keep your activity's
     * process running after its onPause() method is called.
     * 
     * Derived classes must call through to the super class's implementation
     * of this method.  If they do not, an exception will be thrown.
     */
    @Override
    protected void onStop() {
        Log.i(TAG, "onStop()");
        super.onStop();
        
        mLunarView.onStop();
    }

    
    // ******************************************************************** //
    // Menu Management.
    // ******************************************************************** //
    
    /**
     * Invoked during init to give the Activity a chance to set up its Menu.
     * 
     * @param menu the Menu to which entries may be added
     * @return true
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // We must call through to the base implementation.
        super.onCreateOptionsMenu(menu);
        
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);

        return true;
    }

    
    /**
     * Invoked when the user selects an item from the Menu.
     * 
     * @param item the Menu entry which was selected
     * @return true if the Menu item was legit (and we consumed it), false
     *         otherwise
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_new:
                mLunarView.doStart();
                return true;
            case R.id.menu_prefs:
                // Launch the preferences activity as a subactivity, so we
                // know when it returns.
                Intent pIntent = new Intent();
                pIntent.setClass(this, Preferences.class);
                startActivityForResult(pIntent, new MainActivity.ActivityListener() {
                    @Override
                    public void onActivityFinished(int resultCode, Intent data) {
                        updatePreferences();
                    }
                });
                break;
            case R.id.menu_help:
                // Launch the help activity as a subactivity.
                mLunarView.pause();
                Intent hIntent = new Intent();
                hIntent.setClass(this, Help.class);
                startActivity(hIntent);
                break;
            case R.id.menu_about:
                showAbout();
                break;
       }

        return false;
    }


    // ******************************************************************** //
    // Preferences Handling.
    // ******************************************************************** //

    /**
     * Read our application preferences and configure ourself appropriately.
     */
    private void updatePreferences() {
        SharedPreferences prefs =
                        PreferenceManager.getDefaultSharedPreferences(this);

        // Get the desired orientation.
        GameView.Difficulty skillLevel = GameView.Difficulty.EASY;
        try {
            String sval = prefs.getString("skillLevel", skillLevel.toString());
            skillLevel = GameView.Difficulty.valueOf(sval);
        } catch (Exception e) {
            Log.e(TAG, "Pref: bad skillLevel");
        }
        Log.i(TAG, "Prefs: skillLevel " + skillLevel);
        mLunarView.setDifficulty(skillLevel);

        // Get the tilt controls direction.
        boolean tiltInvert = false;
        try {
            tiltInvert = prefs.getBoolean("tiltInvert", false);
        } catch (Exception e) {
            Log.e(TAG, "Pref: bad tiltInvert");
        }
        Log.i(TAG, "Prefs: tiltInvert " + tiltInvert);
        mLunarView.setTiltInverted(tiltInvert);

        // Get the desired orientation.
        int orientMode = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
        try {
            String omode = prefs.getString("orientationMode", null);
            orientMode = Integer.valueOf(omode);
        } catch (Exception e) {
            Log.e(TAG, "Pref: bad orientationMode");
        }
        Log.i(TAG, "Prefs: orientationMode " + orientMode);
        setRequestedOrientation(orientMode);

        boolean keepAwake = false;
        try {
            keepAwake = prefs.getBoolean("keepAwake", false);
        } catch (Exception e) {
            Log.e(TAG, "Pref: bad keepAwake");
        }
        if (keepAwake) {
            Log.i(TAG, "Prefs: keepAwake true: take the wake lock");
            if (wakeLock == null)
                wakeLock = powerManager.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "My Tag");
            if (!wakeLock.isHeld())
                wakeLock.acquire();
        } else {
            Log.i(TAG, "Prefs: keepAwake false: release the wake lock");
            if (wakeLock != null && wakeLock.isHeld())
                wakeLock.release();
            wakeLock = null;
        }

        boolean debugStats = false;
        try {
            debugStats = prefs.getBoolean("debugStats", false);
        } catch (Exception e) {
            Log.e(TAG, "Pref: bad debugStats");
        }
        Log.i(TAG, "Prefs: debugStats " + debugStats);
        mLunarView.setDebugPerf(debugStats);
    }
    

    // ******************************************************************** //
    // Private Constants.
    // ******************************************************************** //

    // Debugging tag.
    private static final String TAG = "netscramble";

    
    // ******************************************************************** //
    // Private Data.
    // ******************************************************************** //
    
    // Our power manager.
    private PowerManager powerManager = null;

    // A handle to the View in which the game is running.
    private GameView mLunarView;
    
    // Wake lock used to keep the screen alive.  Null if we aren't going
    // to take a lock; non-null indicates that the lock should be taken
    // while we're actually running.
    private PowerManager.WakeLock wakeLock = null;

}

