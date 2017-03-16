package io.phobotic.pavillion.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;

import org.apache.commons.validator.ValidatorException;
import org.apache.commons.validator.routines.EmailValidator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import io.phobotic.pavillion.R;
import io.phobotic.pavillion.email.EmailRecipient;
import io.phobotic.pavillion.prefs.Preferences;

/**
 * Created by Jonathan Nelson on 11/11/16.
 */

public class EmailAddressList extends RelativeLayout {
    private RelativeLayout mLayout;
    private ListView list;
    private List<EmailRecipient> emailAdditions = new ArrayList<>();
    private List<EmailRecipient> emailList;
    private EmailArrayAdapter adapter;
    private View error;

    public EmailAddressList(Context context) {
        this(context, null);
    }

    public EmailAddressList(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();

    }

    private void init() {
        mLayout = (RelativeLayout) inflate(super.getContext(), R.layout.view_email_address_list, this);
        if (!isInEditMode()) {
            initViews();
            fetchSavedRecipients();
        }
    }

    private void initViews() {
        error = mLayout.findViewById(R.id.error);
        list = (ListView) mLayout.findViewById(R.id.list);
        list.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
//        list.setStackFromBottom(true);
    }

    private void fetchSavedRecipients() {
        emailList = getSavedRecipientList();
        Collections.sort(emailList);

        for (EmailRecipient recipient: emailList) {
            recipient.setSaved(true);
        }

        updateListAdapter();
    }

    public void updateListAdapter() {
        List<EmailRecipient> combinedList = getCombinedRecipients();
        if (combinedList.isEmpty()) {
            showError();
        } else {
            showList();
        }

        //make sure the list has an adapter set even if the recipients list is empty
        //this avoids having to set an adapter when the first recipient is added later
        adapter = new EmailArrayAdapter(super.getContext(), combinedList);
        list.setAdapter(adapter);
    }

    private void showError() {
        list.setVisibility(View.GONE);
        error.setVisibility(View.VISIBLE);
    }


    private void showList() {
        list.setVisibility(View.VISIBLE);
        error.setVisibility(View.GONE);
    }

    private List<EmailRecipient> getCombinedRecipients() {
        List<EmailRecipient> combinedList = new ArrayList<>();
        combinedList.addAll(emailList);
        combinedList.addAll(emailAdditions);

        return combinedList;
    }

    public void addRecipient(String email) throws ValidatorException {
        if (email == null) throw new IllegalArgumentException("Can not add null email address to recipients list");
        List<EmailRecipient> combinedRecipients = getCombinedRecipients();

        EmailRecipient newRecipient = new EmailRecipient(email);
        newRecipient.setReceiveDailyReport(true);
        EmailValidator validator = EmailValidator.getInstance(false);

        if (combinedRecipients.contains(newRecipient)) {
            throw new IllegalArgumentException("Recipients list already contains email: " + email);
        } else if (!validator.isValid(email)) {
            throw new ValidatorException("Email address is not valid");
        } else {
            emailAdditions.add(newRecipient);
            adapter.notifyDataSetChanged();
        }

        updateListAdapter();
    }

    public void removeRecipient(String email) {
        //this email could be in either list
        Iterator<EmailRecipient> it = emailList.iterator();
        while (it.hasNext()) {
            EmailRecipient recipient = it.next();
            if (recipient.getEmail().equals(email)) {
                it.remove();
            }
        }

        it = emailAdditions.iterator();
        while (it.hasNext()) {
            EmailRecipient recipient = it.next();
            if (recipient.getEmail().equals(email)) {
                it.remove();
            }
        }

        Collections.sort(emailList);
        updateListAdapter();
    }

    public List<EmailRecipient> getRecipients() {
        List<EmailRecipient> combinedList = getCombinedRecipients();

        return combinedList;
    }

    private List<EmailRecipient> getSavedRecipientList() {
        Preferences prefs = Preferences.getInstance(super.getContext());
        List<EmailRecipient> recipientList = prefs.getEmailRecipients();
        Collections.sort(recipientList);

        return recipientList;
    }

    private class EmailArrayAdapter extends ArrayAdapter<EmailRecipient> {

        public EmailArrayAdapter(Context context, List<EmailRecipient> objects) {
            super(context, 0, objects);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            EmailRecipient recipient = getItem(position);

            // Check if an existing view is being reused, otherwise inflate the view
            if (convertView == null) {
                convertView = new EmailAddress(getContext(), null);
            }

            ((EmailAddress)convertView).setRecipient(recipient);
            final EmailArrayAdapter that = this;
            ((EmailAddress)convertView).setListener(new EmailListRemoveItemListener() {
                @Override
                public void onRemoveItem(EmailRecipient recipient) {
                    removeRecipient(recipient.getEmail());
                }
            });
            return convertView;
        }
    }

    public interface EmailListRemoveItemListener {
        public void onRemoveItem(EmailRecipient recipient);
    }


}
