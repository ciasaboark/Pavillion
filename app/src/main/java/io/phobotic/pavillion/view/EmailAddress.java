package io.phobotic.pavillion.view;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import io.phobotic.pavillion.R;
import io.phobotic.pavillion.email.EmailRecipient;

/**
 * Created by Jonathan Nelson on 11/11/16.
 */

public class EmailAddress extends CardView {
    private CardView mLayout;
    private View rootView;
    private ImageButton minusButton;
    private TextView emailTextView;
    private Button dailyButton;
    private Button weeklyButton;
    private Button monthlyButton;
    private EmailRecipient recipient;
    private EmailAddressList.EmailListRemoveItemListener listener;
    private int position;

    public EmailAddress(Context context) {
        this(context, null);
    }

    public EmailAddress(Context context, AttributeSet attrs) {
        super(context, attrs);
        initViews();
    }

    private void initViews() {
        mLayout = (CardView) inflate(super.getContext(), R.layout.view_email_address, this);
        if (!isInEditMode()) {
            rootView = mLayout.findViewById(R.id.root);
            minusButton = (ImageButton) mLayout.findViewById(R.id.minus);
            minusButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        listener.onRemoveItem(recipient);
                    }
                }
            });
            emailTextView = (TextView) mLayout.findViewById(R.id.email);
        }
    }

    public EmailRecipient getRecipient() {
        return recipient;
    }

    public void setRecipient(EmailRecipient recipient) {
        this.recipient = recipient;
        emailTextView.setText(recipient.getEmail());

        if (recipient.isSaved()) {
            rootView.setBackgroundColor(getResources().getColor(R.color.saved_email_background));
        } else {
            rootView.setBackgroundColor(getResources().getColor(R.color.new_email_background));
        }

        initButtons();
    }

    private void initButtons() {
        dailyButton = (Button) mLayout.findViewById(R.id.daily_button);
        setButtonColor(dailyButton, recipient.isReceiveDailyReport());
        dailyButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                recipient.setReceiveDailyReport(!recipient.isReceiveDailyReport());
                setButtonColor(dailyButton, recipient.isReceiveDailyReport());
            }
        });

        weeklyButton = (Button) mLayout.findViewById(R.id.weekly_button);
        setButtonColor(weeklyButton, recipient.isReceiveWeeklyReport());
        weeklyButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                recipient.setReceiveWeeklyReport(!recipient.isReceiveWeeklyReport());
                setButtonColor(weeklyButton, recipient.isReceiveWeeklyReport());
            }
        });

        monthlyButton = (Button) mLayout.findViewById(R.id.monthly_button);
        setButtonColor(monthlyButton, recipient.isReceiveMonthlyReport());
        monthlyButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                recipient.setReceiveMonthlyReport(!recipient.isReceiveMonthlyReport());
                setButtonColor(monthlyButton, recipient.isReceiveMonthlyReport());
            }
        });
    }

    private void setButtonColor(Button button, boolean enabled) {
        if (enabled) {
            button.setTextColor(getResources().getColor(R.color.colorAccent));
        } else {
            button.setTextColor(getResources().getColor(R.color.primary_text_disabled_light));
        }
    }

    public EmailAddressList.EmailListRemoveItemListener getListener() {
        return listener;
    }

    public EmailAddress setListener(EmailAddressList.EmailListRemoveItemListener listener) {
        this.listener = listener;
        return this;
    }
}
