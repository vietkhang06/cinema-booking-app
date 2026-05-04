package com.example.cinemabookingapp.core.navigation;

import android.app.Activity;
import android.content.Intent;

import androidx.annotation.Nullable;

import com.example.cinemabookingapp.ui.admin.AdminDashboardActivity;
import com.example.cinemabookingapp.ui.auth.ForgotPasswordActivity;
import com.example.cinemabookingapp.ui.auth.LoginActivity;
import com.example.cinemabookingapp.ui.auth.RegisterActivity;
import com.example.cinemabookingapp.ui.customer.HomeActivity;
import com.example.cinemabookingapp.ui.staff.StaffDashboardActivity;

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

    public static void goToStaffDashboard(Activity activity) {
        openAndClear(activity, StaffDashboardActivity.class);
    }

    public static void goToAdminDashboard(Activity activity) {
        openAndClear(activity, AdminDashboardActivity.class);
    }

    public static void goToHomeByRole(Activity activity, String role) {
        String safeRole = role == null ? "customer" : role.trim().toLowerCase();

        if ("admin".equals(safeRole)) {
            goToAdminDashboard(activity);
        } else if ("staff".equals(safeRole)) {
            goToStaffDashboard(activity);
        } else {
            goToCustomerHome(activity);
        }
    }
}