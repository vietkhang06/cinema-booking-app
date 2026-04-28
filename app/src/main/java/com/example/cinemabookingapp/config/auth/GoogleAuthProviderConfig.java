package com.example.cinemabookingapp.config.auth;

import androidx.credentials.GetCredentialRequest;

import com.example.cinemabookingapp.R;
import com.google.android.libraries.identity.googleid.GetGoogleIdOption;

public class GoogleAuthProviderConfig {
    public GetCredentialRequest getCredentialRequest(String defaultWebClientId){
        // Instantiate a Google sign-in request
        GetGoogleIdOption googleIdOption = new GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(defaultWebClientId)
                .build();

        // Create the Credential Manager request
        GetCredentialRequest request = new GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build();

        return request;
    }
}
