package andy_eyad_ucsc.splitthetab.presenter;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Observable;
import java.util.Observer;

import andy_eyad_ucsc.splitthetab.R;
import andy_eyad_ucsc.splitthetab.model.AppUser;
import andy_eyad_ucsc.splitthetab.model.DatabaseHandler;
import andy_eyad_ucsc.splitthetab.model.Tab;
import andy_eyad_ucsc.splitthetab.view.SplitListAdapter;
import andy_eyad_ucsc.splitthetab.view.UserCardView;
import es.dmoral.toasty.Toasty;

public class SplitFragment extends Fragment implements Observer {

    private ListView payersView;
    private ArrayList<UserCardView> cardContainer;
    private SplitListAdapter adapter;
    private EditText amountInput;
    private int customPays;

    public SplitFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_main_split, container, false);

        cardContainer = new ArrayList<>();
        payersView = view.findViewById(R.id.listView);
        customPays = 0;
        cardContainer.add(newCardView("You", 0.0));
        adapter = new SplitListAdapter(cardContainer, getContext());
        payersView.setAdapter(adapter);
        setupButtons(view);

        return view;
    }

    private UserCardView newCardView(String payerName, double amount) {
        UserCardView temp = new UserCardView(payersView.getContext(), payerName, amount, cardContainer.size());
        temp.setObserver(this);
        return temp;
    }

    void setupButtons(final View view) {
        final ImageButton add = view.findViewById(R.id.addButton);
        final ImageButton remove = view.findViewById(R.id.removeButton);
        final Button sendPaymentButton = view.findViewById(R.id.sendPayment);
        amountInput = view.findViewById(R.id.amountEdit);

        adjustClickable(view);

        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createCardView();
                adjustClickable(view);
            }
        });

        remove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeCardView();
                adjustClickable(view);
            }
        });

        sendPaymentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (int i = 1; i < cardContainer.size(); i++) {
                    if (!cardContainer.get(i).isVerified()) {
                        Toasty.error(getContext(), "You need to add a user for " + cardContainer.get(i).getOwerName() + " first", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }

                HashMap<String, Object> owers = new HashMap<>();

                for (int i = 1; i < cardContainer.size(); i++) {
                    HashMap<String, Object> amountAndPaid = new HashMap<>();

                    if (cardContainer.get(i).getCustomPay() == 0)
                        amountAndPaid.put("amount", cardContainer.get(i).getOwerPay());
                    else
                        amountAndPaid.put("amount", cardContainer.get(i).getCustomPay());

                    amountAndPaid.put("paid", false);

                    owers.put(AppUser.convertEmailToDatabaseFormat(cardContainer.get(i).getOwerName()), amountAndPaid);
                }

                sendPaymentRequest(owers);
            }
        });

        amountInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateValues();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        amountInput.setSingleLine();
        amountInput.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {

                if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                        (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    amountInput.clearFocus();
                    hideKeyboard();
                }

                return false;
            }
        });
    }

    void sendPaymentRequest(HashMap<String, Object> owers) {
        DatabaseHandler.getInstance().addTab(new Tab(AppUser.getInstance().getEmailForDatabase(), getTotalAmount(), owers));

        AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create();
        alertDialog.setTitle("Sent");
        alertDialog.setMessage("Payment request sent");
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        resetInterface();
                        dialog.dismiss();
                    }
                });

        alertDialog.show();
    }

    void resetInterface() {
        this.amountInput.setText("");

        int size = this.cardContainer.size();

        for (int i = size; i > 0; i--) {
            this.removeCardView();
        }

        this.adapter.notifyDataSetChanged();
    }

    void adjustClickable(View view) {
        amountInput.clearFocus();
        hideKeyboard();

        final ImageButton remove = view.findViewById(R.id.removeButton);

        if (cardContainer.size() <= 1)
            remove.setAlpha(0.4f);
        else
            remove.setAlpha(1f);
    }

    @SuppressLint("DefaultLocale")
    void createCardView() {
        cardContainer.add(newCardView(String.format("Splitter %d", cardContainer.size()), 0.0));
        updateValues();
    }

    private void hideKeyboard() {
        InputMethodManager inputManager = (InputMethodManager) getContext()
                .getSystemService(Context.INPUT_METHOD_SERVICE);

        if (inputManager != null) {
            inputManager.hideSoftInputFromWindow(amountInput.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    private double getTotalAmount() {
        double total;

        if (amountInput.getText().toString().equals(""))
            total = 0.0;
        else
            total = Double.parseDouble(amountInput.getText().toString());

        return total;

    }

    void updateValues() {
        customPays = 0;
        double total = getTotalAmount();

        for (int i = 0; i < cardContainer.size(); i++) {
            if (cardContainer.get(i).getCustomPay() != 0) {
                total -= cardContainer.get(i).getCustomPay();
                customPays++;
            }
        }

        for (int i = 0; i < cardContainer.size(); i++) {
            if (cardContainer.get(i).getCustomPay() != 0) {
                cardContainer.get(i).updatePrice(cardContainer.get(i).getCustomPay());

            } else {
                cardContainer.get(i).updatePrice(total / (cardContainer.size() - customPays));
            }
        }

        adapter.notifyDataSetChanged();
    }

    public void update(Observable obj, Object arg) {
        updateValues();
    }

    void removeCardView() {
        if (cardContainer.size() == 2) {
            cardContainer.get(0).setCustomPay(0);
            cardContainer.remove(cardContainer.size() - 1);
            updateValues();

        } else if (cardContainer.size() > 2) {
            if (cardContainer.get(cardContainer.size() - 1).getCustomPay() != 0)
                customPays--;

            cardContainer.remove(cardContainer.size() - 1);
            updateValues();
        }
    }
}
