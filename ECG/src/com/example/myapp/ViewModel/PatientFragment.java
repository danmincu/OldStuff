package com.example.myapp.ViewModel;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import com.example.myapp.DataModel.ISettingsProvider;
import com.example.myapp.DropBox.IDropboxSession;
import com.example.myapp.EcgApplication;
import com.example.myapp.IUpdater;
import com.example.myapp.R;

import javax.inject.Inject;


public class PatientFragment extends Fragment implements IUpdater {

    @Inject
    public ISettingsProvider patientProvider;
    @Inject
    public IDropboxSession dropboxSession;

    Button authButton;
    EditText editPatientName;
    boolean mLoggedIn;
    private final View.OnClickListener handleAuthEvent = new View.OnClickListener() {
        @Override
        public void onClick(final View view) {
            if (mLoggedIn) {

                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case DialogInterface.BUTTON_POSITIVE:
                                //Yes button clicked
                                PatientFragment.this.dropboxSession.logOut();
                                break;

                            case DialogInterface.BUTTON_NEGATIVE:
                                //No button clicked
                                break;
                        }
                    }
                };
                AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                builder.setMessage("Are you sure?").setPositiveButton("Yes", dialogClickListener)
                        .setNegativeButton("No", dialogClickListener).show();

            } else {
                PatientFragment.this.dropboxSession.authenticateMyApp();
            }
        }
    };

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        // If activity recreated (such as from screen rotate), restore
        // the previous article selection set by onSaveInstanceState().
        // This is primarily necessary when in the two-pane layout.
        if (savedInstanceState != null) {
            //    this.currentPosition = savedInstanceState.getInt(NoteFragment.ARG_POSITION);
        }

        View view = inflater.inflate(R.layout.patient_tab, container, false);

        this.authButton = (Button) view.findViewById(R.id.auth_button);
        this.editPatientName = (EditText) view.findViewById(R.id.editPatientName);
        this.authButton.setOnClickListener(handleAuthEvent);

        //patient name
        this.editPatientName.setText(this.patientProvider.getPatientName());
        this.editPatientName.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                PatientFragment.this.patientProvider.setPatientName(PatientFragment.this.editPatientName.getText().toString());
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        });

        //patient status
        RadioGroup patientStatusRadioGroup = (RadioGroup) view.findViewById(R.id.radioGroupStatus);
        RadioButton restingRadioButton = (RadioButton) view.findViewById(R.id.radio0);
        RadioButton walkingRadioButton = (RadioButton) view.findViewById(R.id.radio1);
        RadioButton runningRadioButton = (RadioButton) view.findViewById(R.id.radio2);

        if (this.patientProvider.getPatientStatus().equalsIgnoreCase("resting"))
            restingRadioButton.setChecked(true);
        else if (this.patientProvider.getPatientStatus().equalsIgnoreCase("walking")) {
            walkingRadioButton.setChecked(true);
        } else {
            runningRadioButton.setChecked(true);
        }

        patientStatusRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {

                if (checkedId == R.id.radio0) {
                    PatientFragment.this.patientProvider.setPatientStatus("resting");
                } else if (checkedId == R.id.radio1) {
                    PatientFragment.this.patientProvider.setPatientStatus("walking");
                } else {
                    PatientFragment.this.patientProvider.setPatientStatus("running");
                }
            }

        });

        this.dropboxSession.addUpdater(this);
        this.setLoggedIn(this.dropboxSession.isTheAppLoggedInDropbox());
        return view;
    }

    @Override
    public void onAttach(final Activity activity) {
        super.onAttach(activity);
        EcgApplication app = (EcgApplication) activity.getApplication();
        app.getObjectGraph().inject(this);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onStart() {
        super.onStart();

        // During startup, check if there are arguments passed to the fragment.
        // onStart is a good place to do this because the layout has already
        // been applied to the fragment at this point so we can safely call the
        // method below that sets the text.
        final Bundle args = this.getArguments();

    }


    public void setLoggedIn(boolean loggedIn) {

        mLoggedIn = loggedIn;
        if (loggedIn) {
            if (authButton != null)
                authButton.setText("Unlink from Dropbox");
        } else {
            if (authButton != null)
                authButton.setText("Link with Dropbox");
        }

    }


    @Override
    public void Update() {
        this.setLoggedIn(this.dropboxSession.isTheAppLoggedInDropbox());
    }
}
