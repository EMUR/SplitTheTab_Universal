package andy_eyad_ucsc.splitthetab.view;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Observable;
import java.util.Observer;

import andy_eyad_ucsc.splitthetab.R;
import andy_eyad_ucsc.splitthetab.model.AppUser;
import andy_eyad_ucsc.splitthetab.model.DatabaseHandler;
import andy_eyad_ucsc.splitthetab.model.OnGetDataListener;
import es.dmoral.toasty.Toasty;


@SuppressLint({"ClickableViewAccessibility", "DefaultLocale"})
public class UserCardView extends RelativeLayout {

    private Data userData;
    private int index;
    private boolean selected;
    private GestureDetector gestureDetector;
    private Dialog d;

    public UserCardView(Context context) {
        super(context);
        inflate(getContext(), R.layout.row_item, this);
    }

    public UserCardView(final Context context, String name, double amount, int ind) {
        super(context);
        userData = new Data(amount, name);
        index = ind;
        selected = false;
        inflate(getContext(), R.layout.row_item, this);
        ImageButton r = findViewById(R.id.addImage);

        if (ind == 0) {
            r.setImageResource(R.drawable.user);

        } else {
            r.setImageResource(R.drawable.adduser);
            r.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    showRecent(context);
                }
            });
        }

        gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            public boolean onDoubleTap(MotionEvent e) {
                getCustomSetAlert(context);
                return true;
            }
        });

        initialiseViews();
    }

    public UserCardView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        inflate(getContext(), R.layout.row_item, this);
    }

    public UserCardView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        inflate(getContext(), R.layout.row_item, this);
    }

    public boolean isVerified() {
        return userData.verified;
    }

    @SuppressWarnings("deprecation")
    public void getCustomSetAlert(Context c) {
        final EditText editText = new EditText(c);
        final AlertDialog ad = new AlertDialog.Builder(c).create();

        editText.setTextColor(getResources().getColor(R.color.colorAccent));
        editText.setTextSize(24);
        editText.setSingleLine();
        editText.setRawInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        ad.setTitle("Enter custom amount:");
        ad.setView(editText);

        editText.setOnKeyListener(new OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                        (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    if (trySetCustom(editText.getText().toString())) {
                        ad.dismiss();

                    } else {
                        editText.setText("");
                    }

                    return true;
                }

                return false;
            }
        });

        ad.show();
    }

    public void setObserver(Observer a) {
        userData.addObserver(a);
    }

    public boolean trySetCustom(String am) {
        if (am.equals("")) {
            return false;
        }

        double custom = Double.parseDouble(am);

        if (custom < 0) {
            return false;

        }

        setCustomPay(custom);

        userData.notifyObservers();

        return true;
    }

    private void showRecent(final Context c) {

        if (AppUser.getInstance() == null) {
            Toasty.info(c, "You have to login to add splitters").show();
            return;
        }
        d = new Dialog(c);
        d.setContentView(R.layout.activity_add_split_person_main);

        Button cancelSplitViewButton = d.findViewById(R.id.cancelButton);
        Button findSplitViewButton = d.findViewById(R.id.findButton);
        ListView splittersList = d.findViewById(R.id.RecentSplitters);
        final ArrayAdapter<String> recentAdapter = new ArrayAdapter<>(c, R.layout.splitter_view_row, DatabaseHandler.getInstance().getRecentlyUsedPayerEmails());
        splittersList.setAdapter(recentAdapter);


        final EditText editTextEmailField = d.findViewById(R.id.editTextEmail);

        splittersList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (!selected) {
                    userData.payerName = recentAdapter.getItem(position);
                    payerVerified();
                    d.dismiss();
                    Toasty.success(d.getContext(), "Added " + userData.payerName, Toast.LENGTH_SHORT, true).show();
                }
            }
        });

        cancelSplitViewButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                d.dismiss();
            }
        });

        findSplitViewButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatabaseHandler.getInstance().addRecentlyPaidUser(editTextEmailField.getText().toString(), new OnGetDataListener() {

                    @Override
                    public void onStart() {

                    }

                    @Override
                    public void onSuccess(String data) {
                        final EditText editTextEmailField = d.findViewById(R.id.editTextEmail);

                        userData.payerName = editTextEmailField.getText().toString();
                        payerVerified();
                        d.dismiss();
                        Toasty.success(d.getContext(), "Added " + userData.payerName, Toast.LENGTH_SHORT, true).show();
                    }

                    @Override
                    public void onFailed(String databaseError) {
                        Toasty.error(d.getContext(), "Email is not in the database!", Toast.LENGTH_SHORT, true).show();

                    }
                });
            }
        });


        d.show();
    }

    public double getCustomPay() {
        return userData.customPay;
    }

    public void setCustomPay(double amount) {
        userData.customPay = amount;
    }

    public String getOwerName() {
        return userData.payerName;
    }

    public double getOwerPay() {
        if (getCustomPay() == 0.0)
            return userData.moneyAmount;
        else
            return getCustomPay();
    }

    private void initialiseViews() {
        TextView splitterNameField = findViewById(R.id.splitterName);
        TextView amountField = findViewById(R.id.splitterAmount);
        ImageButton increaseButton = findViewById(R.id.increaseButton);
        ImageButton decreaseButton = findViewById(R.id.decreaseButton);

        amountField.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                gestureDetector.onTouchEvent(event);
                return true;
            }
        });

        splitterNameField.setText(userData.payerName);
        amountField.setText(String.format("$%.2f", userData.moneyAmount));

        increaseButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                incrementPayment();
            }
        });

        decreaseButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                decrementPayment();
            }
        });
    }

    void select() {
        selected = true;
    }

    private void incrementPayment() {
        userData.customPay = userData.moneyAmount + 1;
        userData.notifyObservers(index);
    }

    private void decrementPayment() {
        userData.customPay = userData.moneyAmount - 1;
        userData.notifyObservers(index);
    }

    @SuppressLint("DefaultLocale")
    public void updatePrice(double amount) {
        userData.moneyAmount = amount;
        TextView a = findViewById(R.id.splitterAmount);
        a.setText(String.format("$%.2f", amount));

    }

    private void payerVerified() {
        userData.verified = true;
        TextView name = findViewById(R.id.splitterName);
        name.setText(userData.payerName);
        select();
        ImageButton profile = findViewById(R.id.addImage);
        profile.setClickable(false);
        profile.setImageResource(R.drawable.user);
    }

    private class Data extends Observable {
        double moneyAmount;
        String payerName;
        double customPay;
        boolean verified;

        Data(double m, String a) {
            moneyAmount = m;
            payerName = a;
            customPay = 0.0;
            verified = false;
        }

        @Override
        public boolean hasChanged() {
            return true;
        }
    }

}
