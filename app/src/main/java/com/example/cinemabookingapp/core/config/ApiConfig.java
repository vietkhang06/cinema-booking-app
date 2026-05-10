package com.example.cinemabookingapp.core.config;

public class ApiConfig {
    // 10.0.2.2 is the alias for loopback interface (localhost) in Android Emulator
    public static final String BASE_URL = "http://10.0.2.2:8080/api/v1/";
    
    // Timeout values in seconds
    public static final int CONNECT_TIMEOUT = 15;
    public static final int READ_TIMEOUT = 30;
    public static final int WRITE_TIMEOUT = 15;
    
    // API Versions / Headers
    public static final String API_VERSION = "v1.0.27";
}
