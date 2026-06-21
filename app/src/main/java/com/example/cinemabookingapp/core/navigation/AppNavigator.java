package com.example.cinemabookingapp.core.navigation;

import android.app.Activity;
import android.content.Intent;

import androidx.annotation.Nullable;

import com.example.cinemabookingapp.ui.features.admin.dashboard.AdminDashboardActivity;
import com.example.cinemabookingapp.ui.features.auth.ForgotPasswordActivity;
import com.example.cinemabookingapp.ui.features.auth.LoginActivity;
import com.example.cinemabookingapp.ui.features.auth.RegisterActivity;
import com.example.cinemabookingapp.ui.features.home.HomeActivity;
import com.example.cinemabookingapp.ui.features.notification.NotificationActivity;
import com.example.cinemabookingapp.ui.features.transaction.TicketDetailActivity;
import com.example.cinemabookingapp.ui.features.transaction.TransactionHistoryActivity;

import java.util.Map;

public final class AppNavigator {

    private AppNavigator() {
    }

    private static void open(Activity activity, Class<?> target) {
        Intent intent = new Intent(activity, target);
        activity.startActivity(intent);
    }

    private static void openAndClear(Activity activity, Class<?> target) {
        Intent intent = new Intent(activity, target);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        activity.startActivity(intent);
        activity.finish();
    }

    public static void navigateWithData(Activity activity, Class<?> target, @Nullable Map<String, String> navigateBundle) {
        Intent intent = new Intent(activity, target);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        if(navigateBundle != null){
            navigateBundle.forEach((name, value) -> {
                intent.putExtra(name, value);
            });
        }
        activity.startActivity(intent);
        activity.finish();
    }

    public static void goToLogin(Activity activity) {
        openAndClear(activity, LoginActivity.class);
    }

    public static void goToRegister(Activity activity) {
        open(activity, RegisterActivity.class);
    }

    public static void goToForgotPassword(Activity activity) {
        open(activity, ForgotPasswordActivity.class);
    }

    public static void goToCustomerHome(Activity activity) {
        openAndClear(activity, HomeActivity.class);
    }

    public static void goToTransactionHistory(Activity activity) {
        open(activity, TransactionHistoryActivity.class);
    }

    public static void goToNotification(Activity activity) {
        open(activity, NotificationActivity.class);
    }

    public static void goToTicketDetail(Activity activity, String bookingId) {
        Intent intent = new Intent(activity, TicketDetailActivity.class);
        intent.putExtra(TicketDetailActivity.EXTRA_BOOKING_ID, bookingId);
        activity.startActivity(intent);
    }

    public static void goToAdminDashboard(Activity activity) {
        openAndClear(activity, AdminDashboardActivity.class);
    }

    public static void goToHomeByRole(Activity activity, String role) {
        String safeRole = role == null ? "customer" : role.trim().toLowerCase();

        if ("admin".equals(safeRole)) {
            goToAdminDashboard(activity);
        } else {
            goToCustomerHome(activity);
        }
    }

    public static void goToLoginForBooking(Activity activity) {
        Intent intent = new Intent(activity, LoginActivity.class);
        intent.putExtra(LoginActivity.EXTRA_FROM_BOOKING, true);
        activity.startActivity(intent);
    }
}