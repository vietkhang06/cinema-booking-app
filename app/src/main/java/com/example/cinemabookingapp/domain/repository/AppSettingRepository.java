package com.example.cinemabookingapp.domain.repository;

import com.example.cinemabookingapp.domain.common.ResultCallback;
import com.example.cinemabookingapp.domain.model.AppSetting;

import java.util.List;

public interface AppSettingRepository {
    void upsertSetting(AppSetting setting, ResultCallback<AppSetting> callback);
    void getSettingByKey(String key, ResultCallback<AppSetting> callback);
    void getAllSettings(ResultCallback<List<AppSetting>> callback);
}