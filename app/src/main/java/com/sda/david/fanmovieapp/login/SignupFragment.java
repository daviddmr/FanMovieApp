package com.sda.david.fanmovieapp.login;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.sda.david.fanmovieapp.R;
import com.sda.david.fanmovieapp.api.ServiceGenerator;
import com.sda.david.fanmovieapp.api.interfaces.UserService;
import com.sda.david.fanmovieapp.model.User;
import com.sda.david.fanmovieapp.util.ShowMessageUtil;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by david on 01/05/2017.
 */

public class SignupFragment extends Fragment {

    public static final String TAG = "SignupFrag";

    private EditText etName;
    private EditText etLogin;
    private EditText etPassword;
    private EditText etConfirmPassword;
    private SwitchCompat switchAdm;
    private Button btSignup;
    private ProgressDialog dialog;

    private OnSignUpFragmentListener mListener;

    public static SignupFragment newInstance() {
        return new SignupFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_sign_up, container, false);
        initComponents(rootView);

        return rootView;
    }

    private void initComponents(View rootView) {

        etName = (EditText) rootView.findViewById(R.id.et_name);
        etLogin = (EditText) rootView.findViewById(R.id.et_login);
        etPassword = (EditText) rootView.findViewById(R.id.et_password);
        etConfirmPassword = (EditText) rootView.findViewById(R.id.et_password_confirm);
        btSignup = (Button) rootView.findViewById(R.id.bt_signup);
        btSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signUp();
            }
        });
        switchAdm = (SwitchCompat) rootView.findViewById(R.id.switch_adm);

        dialog = new ProgressDialog(getContext());
        dialog.setIndeterminate(true);
        dialog.setCanceledOnTouchOutside(false);

    }

    private void signUp() {

        if (verifyFields()) {
            requestSignup();
        }

    }

    private void requestSignup() {
        dialog.setMessage(getString(R.string.loading_signup));
        dialog.show();
        User user = new User(etName.getText().toString(), etLogin.getText().toString(), etPassword.getText().toString(), switchAdm.isChecked());
        Call<User> call = ServiceGenerator.createService(UserService.class).signupUser(user);
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                dialog.dismiss();
                if (response.isSuccessful()) {
                    signUpSuccess(response.body(), etLogin);
                } else {
                    if(response.code() == 500)
                        ShowMessageUtil.longSnackBar(etLogin, getString(R.string.username_already_registered));
                    else
                        ServiceGenerator.verifyErrorResponse(response.code(), etLogin, getContext(), true, getActivity());
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                dialog.dismiss();
                ServiceGenerator.verifyFailedConnection(t, etLogin, getContext());
            }
        });
    }

    public void signUpSuccess(User user, View view) {
        if (mListener != null) {
            mListener.signUpSuccess(user, view);
        }
    }

    private boolean verifyFields() {
        boolean validFields = true;

        if (etName.getText().toString().matches("")) {
            etName.setError(getString(R.string.empty_name));
            validFields = false;
        }

        if (etLogin.getText().toString().matches("")) {
            etLogin.setError(getString(R.string.empty_login));
            validFields = false;
        }

        if (etPassword.getText().toString().matches("")) {
            etPassword.setError(getString(R.string.empty_password));
            validFields = false;
        }

        if (etConfirmPassword.getText().toString().matches("")) {
            etConfirmPassword.setError(getString(R.string.empty_confirm_password));
            validFields = false;
        }

        if (!etPassword.getText().toString().matches("") && !etConfirmPassword.getText().toString().matches("")
                && !etPassword.getText().toString().matches(etConfirmPassword.getText().toString())) {
            etPassword.setError(getString(R.string.password_not_compatible));
            etConfirmPassword.setError(getString(R.string.password_not_compatible));
            validFields = false;
        }

        return validFields;

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnSignUpFragmentListener) {
            mListener = (OnSignUpFragmentListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement OnSignUpFragmentListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnSignUpFragmentListener {
        void signUpSuccess(User user, View view);
    }

}
