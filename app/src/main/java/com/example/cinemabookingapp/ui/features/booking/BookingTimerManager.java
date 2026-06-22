package com.example.cinemabookingapp.ui.features.booking;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import java.util.ArrayList;
import java.util.List;

public class BookingTimerManager {

    private static final String TAG = "BookingTimerManager";
    private static final String PREFS_NAME = "booking_timer_prefs";
    private static final String KEY_END_TIME = "end_time_millis";
    private static final String KEY_IS_RUNNING = "is_running";

    private static BookingTimerManager instance;

    private long endTimeMillis = 0;
    private boolean isRunning = false;
    private CountDownTimer countDownTimer;
    private final List<TimerListener> listeners = new ArrayList<>();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public interface TimerListener {
        void onTick(long millisUntilFinished);
        void onFinish();
    }

    private BookingTimerManager() {}

    public static synchronized BookingTimerManager getInstance() {
        if (instance == null) {
            instance = new BookingTimerManager();
        }
        return instance;
    }

    public synchronized void startTimer(Context context, long durationMillis) {
        Log.d(TAG, "startTimer called with duration: " + durationMillis);
        // Load existing timer if already running to prevent double starting
        if (isTimerActive(context)) {
            Log.d(TAG, "startTimer: timer already active, restoring");
            restoreTimer(context);
            return;
        }

        endTimeMillis = System.currentTimeMillis() + durationMillis;
        isRunning = true;

        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit()
                .putLong(KEY_END_TIME, endTimeMillis)
                .putBoolean(KEY_IS_RUNNING, true)
                .apply();

        startInternalTimer();
    }

    public synchronized void startTimerWithEndTime(Context context, long targetEndTimeMillis) {
        Log.d(TAG, "startTimerWithEndTime called with target: " + targetEndTimeMillis + " (remaining: " + (targetEndTimeMillis - System.currentTimeMillis()) + "ms)");
        if (targetEndTimeMillis <= System.currentTimeMillis()) {
            Log.d(TAG, "startTimerWithEndTime: target time is in the past, stopping timer");
            stopTimer(context);
            notifyFinished();
            return;
        }

        endTimeMillis = targetEndTimeMillis;
        isRunning = true;

        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit()
                .putLong(KEY_END_TIME, endTimeMillis)
                .putBoolean(KEY_IS_RUNNING, true)
                .apply();

        startInternalTimer();
    }

    public synchronized void restoreTimer(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        endTimeMillis = prefs.getLong(KEY_END_TIME, 0);
        isRunning = prefs.getBoolean(KEY_IS_RUNNING, false);
        Log.d(TAG, "restoreTimer: isRunning=" + isRunning + ", endTimeMillis=" + endTimeMillis + " (remaining: " + (endTimeMillis - System.currentTimeMillis()) + "ms)");

        if (isRunning && endTimeMillis > System.currentTimeMillis()) {
            startInternalTimer();
        } else {
            Log.d(TAG, "restoreTimer: timer expired or not running, stopping");
            stopTimer(context);
        }
    }

    public synchronized void stopTimer(Context context) {
        Log.d(TAG, "stopTimer called");
        mainHandler.post(() -> {
            synchronized (BookingTimerManager.this) {
                if (countDownTimer != null) {
                    Log.d(TAG, "stopTimer: cancelling countDownTimer");
                    countDownTimer.cancel();
                    countDownTimer = null;
                }
            }
        });
        isRunning = false;
        endTimeMillis = 0;

        if (context != null) {
            SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            prefs.edit()
                    .remove(KEY_END_TIME)
                    .remove(KEY_IS_RUNNING)
                    .apply();
        }
    }

    public synchronized boolean isTimerActive(Context context) {
        if (isRunning && endTimeMillis > System.currentTimeMillis()) {
            return true;
        }
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        long savedEndTime = prefs.getLong(KEY_END_TIME, 0);
        boolean savedIsRunning = prefs.getBoolean(KEY_IS_RUNNING, false);
        boolean active = savedIsRunning && savedEndTime > System.currentTimeMillis();
        Log.d(TAG, "isTimerActive: inMemory=" + (isRunning && endTimeMillis > System.currentTimeMillis()) + ", sharedPrefs=" + active);
        return active;
    }

    public synchronized long getRemainingTimeMillis() {
        if (!isRunning) return 0;
        long remaining = endTimeMillis - System.currentTimeMillis();
        return Math.max(0, remaining);
    }

    public synchronized void registerListener(TimerListener listener) {
        Log.d(TAG, "registerListener called. Listeners size before: " + listeners.size());
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
        // Send initial tick immediately if running
        if (isRunning) {
            long remaining = getRemainingTimeMillis();
            Log.d(TAG, "registerListener: sending initial tick of " + remaining + "ms to new listener");
            listener.onTick(remaining);
        }
    }

    public synchronized void unregisterListener(TimerListener listener) {
        Log.d(TAG, "unregisterListener called. Listeners size before: " + listeners.size());
        listeners.remove(listener);
    }

    private synchronized void startInternalTimer() {
        Log.d(TAG, "startInternalTimer called");
        mainHandler.post(() -> {
            synchronized (BookingTimerManager.this) {
                if (countDownTimer != null) {
                    Log.d(TAG, "startInternalTimer: cancelling existing countDownTimer");
                    countDownTimer.cancel();
                }

                long remaining = getRemainingTimeMillis();
                Log.d(TAG, "startInternalTimer (post): starting CountDownTimer with remaining=" + remaining + "ms");
                if (remaining <= 0) {
                    Log.d(TAG, "startInternalTimer (post): remaining is <= 0, notifying finish");
                    notifyFinished();
                    return;
                }

                countDownTimer = new CountDownTimer(remaining, 1000) {
                    @Override
                    public void onTick(long millisUntilFinished) {
                        notifyTick(millisUntilFinished);
                    }

                    @Override
                    public void onFinish() {
                        Log.d(TAG, "CountDownTimer finished");
                        notifyFinished();
                    }
                };
                countDownTimer.start();
                Log.d(TAG, "startInternalTimer (post): CountDownTimer started successfully");
            }
        });
    }

    private synchronized void notifyTick(long millisUntilFinished) {
        mainHandler.post(() -> {
            synchronized (BookingTimerManager.this) {
                // Log.d(TAG, "notifyTick: " + millisUntilFinished + "ms to " + listeners.size() + " listeners");
                for (TimerListener listener : new ArrayList<>(listeners)) {
                    listener.onTick(millisUntilFinished);
                }
            }
        });
    }

    private synchronized void notifyFinished() {
        Log.d(TAG, "notifyFinished called");
        isRunning = false;
        endTimeMillis = 0;
        mainHandler.post(() -> {
            synchronized (BookingTimerManager.this) {
                for (TimerListener listener : new ArrayList<>(listeners)) {
                    listener.onFinish();
                }
            }
        });
    }
}
