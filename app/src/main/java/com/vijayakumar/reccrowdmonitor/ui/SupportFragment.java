package com.vijayakumar.reccrowdmonitor.ui;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.vijayakumar.reccrowdmonitor.R;
import com.vijayakumar.reccrowdmonitor.util.ThemeHelper;

public class SupportFragment extends Fragment {

    private MaterialButton btnContactEmail;
    private MaterialSwitch switchTheme;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_support, container, false);

        btnContactEmail = view.findViewById(R.id.btn_contact_email);
        switchTheme = view.findViewById(R.id.switch_theme);

        btnContactEmail.setOnClickListener(v -> openEmailClient());

        if (getContext() != null) {
            boolean isDark = ThemeHelper.isDarkMode(getContext());
            switchTheme.setChecked(isDark);

            switchTheme.setOnCheckedChangeListener((buttonView, isChecked) -> {
                ThemeHelper.setDarkMode(requireContext(), isChecked);
            });
        }

        return view;
    }

    private void openEmailClient() {
        String email = getString(R.string.email_address);
        String subject = getString(R.string.email_subject);

        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:" + email));
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{email});
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);

        try {
            startActivity(Intent.createChooser(intent, getString(R.string.email_chooser_title)));
        } catch (ActivityNotFoundException e) {
            Toast.makeText(getContext(), R.string.no_email_app, Toast.LENGTH_LONG).show();
        }
    }
}
