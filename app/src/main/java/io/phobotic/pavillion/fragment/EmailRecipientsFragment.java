package io.phobotic.pavillion.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.apache.commons.validator.ValidatorException;

import java.util.List;

import io.phobotic.pavillion.R;
import io.phobotic.pavillion.email.EmailRecipient;
import io.phobotic.pavillion.prefs.Preferences;
import io.phobotic.pavillion.view.EmailAddressList;

/**
 * Created by Jonathan Nelson on 11/11/16.
 */

public class EmailRecipientsFragment extends DialogFragment {
    private EmailAddressList emailAddressList;
    private Button addRecipientButton;
    private EditText emailEditText;
    private View mRootView;

    public EmailRecipientsFragment() {

    }

    static EmailRecipientsFragment newInstance() {
        EmailRecipientsFragment f = new EmailRecipientsFragment();

        // Supply num input as an argument.
//        Bundle args = new Bundle();
//        args.putInt("num", num);
//        f.setArguments(args);

        return f;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //todo read bundle arguments
    }

//    @Nullable
//    @Override
//    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
//        mRootView = inflater.inflate(R.layout.fragment_email_recipients, container, false);
//        initView();
//
//        return mRootView;
//    }

    private void initView() {
        emailAddressList = (EmailAddressList) mRootView.findViewById(R.id.emailList);

        addRecipientButton = (Button) mRootView.findViewById(R.id.add);
        addRecipientButton.setEnabled(false);
        addRecipientButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                List<EmailRecipient> recipients = emailAddressList.getRecipients();
                String newEmail = emailEditText.getText().toString();
                try {
                    emailAddressList.addRecipient(newEmail);
                    emailEditText.setText("");
                } catch (IllegalArgumentException e) {
                    Toast.makeText(getActivity(), "Address is already in recipients list",
                            Toast.LENGTH_LONG).show();
                    emailEditText.setText("");
                } catch (ValidatorException e) {
                    Toast.makeText(getActivity(), "Email address is not valid", Toast.LENGTH_LONG)
                            .show();
                }
            }
        });

        emailEditText = (EditText) mRootView.findViewById(R.id.emailText);
        emailEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s == null || s.toString().length() == 0) {
                    addRecipientButton.setEnabled(false);
                } else {
                    addRecipientButton.setEnabled(true);
                }
            }
        });
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        mRootView = getActivity().getLayoutInflater().inflate(R.layout.fragment_email_recipients, null);
        initView();

        AlertDialog.Builder builder =  new AlertDialog.Builder(getActivity())
                .setView(mRootView)
                .setIcon(R.drawable.account_multiple)
                .setTitle("Email Recipients")
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                List<EmailRecipient> recipientList = emailAddressList.getRecipients();
                                Preferences prefs = Preferences.getInstance(getActivity());
                                prefs.setEmailRecipients(recipientList);
                            }
                        }
                )
                .setNegativeButton("cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                //todo discard email list changes
                                dialog.dismiss();
                            }
                        }
                );

        AlertDialog dialog =  builder.create();
        dialog.getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        return dialog;
    }

    @Override
    public void onResume() {
        super.onResume();
        emailEditText.requestFocus();
    }
}
