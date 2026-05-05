package com.example.cinemabookingapp.ui.customer.profile;

import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.ImageView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;

import com.bumptech.glide.Glide;
import com.example.cinemabookingapp.R;
import com.example.cinemabookingapp.core.base.AuthActivity;
import com.example.cinemabookingapp.di.ServiceProvider;
import com.example.cinemabookingapp.domain.model.User;
import com.example.cinemabookingapp.service.ProfileService;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.UUID;

public class EditProfileActivity extends AuthActivity {

    TextInputEditText emailTV, uidTV, phoneInputTV, usernameInputTV;
    MaterialButton saveChangesBtn, cancelBtn, backBtn;
    MaterialCardView selectPhotoBtn;
    ImageView profileAvatar;
    Uri s_profileUri;


    ProfileService profileService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.customer_activity_edit_profile);

        profileService = ServiceProvider.getInstance().getProfileService();

        initViews();
        loadUserData();

        bindActions();
        handlePhotoPickerResult();
    }

    private void initViews() {
        emailTV = findViewById(R.id.edit_profile_email);
        uidTV = findViewById(R.id.edit_profile_uid);
        phoneInputTV = findViewById(R.id.edit_profile_phone);
        usernameInputTV = findViewById(R.id.edit_profile_username);

        saveChangesBtn = findViewById(R.id.edit_profile_save_change);
        cancelBtn = findViewById(R.id.edit_profile_cancel);
        backBtn = findViewById(R.id.edit_profile_back_btn);

        profileAvatar = findViewById(R.id.edit_profile_avatar);
        selectPhotoBtn = findViewById(R.id.edit_profile_photo_picker);

    }

    private void bindActions() {
        saveChangesBtn.setEnabled(false);
        saveChangesBtn.setOnClickListener(v -> {
            saveChanges();
        });

        selectPhotoBtn.setOnClickListener(v -> {
            pickProfileAvatar();
        });

        cancelBtn.setOnClickListener(v -> {
            finish();
        });

        backBtn.setOnClickListener(v -> {
            finish();
        });

        TextWatcher setBtnState = new TextWatcher() {
            @Override
            public void afterTextChanged(Editable editable) {
                saveChangesBtn.setEnabled(true);
            }
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }
        };

        phoneInputTV.addTextChangedListener(setBtnState);
        usernameInputTV.addTextChangedListener(setBtnState);
    }

    ActivityResultLauncher<PickVisualMediaRequest> pickMedia;
    void handlePhotoPickerResult(){
        pickMedia = registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
            // Callback is invoked after the user selects a media item or closes the picker.
            if (uri != null) {
                s_profileUri = uri;
                Glide.with(this)
                    .load(uri)
                    .into(profileAvatar);
                saveChangesBtn.setEnabled(true);
                Log.d("PhotoPicker", "Selected URI: " + uri);
                // Use the URI to display the image, e.g., imageView.setImageURI(uri);
            } else {
                Log.d("PhotoPicker", "No media selected");
            }
        });
    }

    void loadUserData(){
        User userProfile = profileService.getUserProfile();

        phoneInputTV.setText(userProfile.phone);
        uidTV.setText(userProfile.uid);
        emailTV.setText(userProfile.email);
        usernameInputTV.setText(userProfile.name);

        Glide.with(this)
                .load((userProfile.avatarUrl == null || userProfile.avatarUrl.isBlank()) ?
                        R.drawable.user_solid_full :
                        userProfile.avatarUrl)
                .into(profileAvatar);
    }

    private void saveChanges(){
        User user = profileService.getUserProfile();

        String avatarUrl = ServiceProvider.getInstance().getUploadService().uploadImage(s_profileUri);
        user.avatarUrl = avatarUrl == null || avatarUrl.isBlank() ? user.avatarUrl : avatarUrl;
        user.phone = phoneInputTV.getText().toString().isBlank() ? user.phone : phoneInputTV.getText().toString();
        user.name = usernameInputTV.getText().toString().isBlank() ? user.name : usernameInputTV.getText().toString();

        profileService.updateUserProfile(user);
        saveChangesBtn.setEnabled(false);
        showToast("Updated user profile.");
    }

    void pickProfileAvatar(){
        // Launch the photo picker to let the user choose only images.
        pickMedia.launch(new PickVisualMediaRequest.Builder()
            .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
            .build());

    }



}
