package andy_eyad_ucsc.splitthetab.model;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import andy_eyad_ucsc.splitthetab.R;
import at.markushi.ui.CircleButton;

/**
 * Handles payments
 */

public class PaymentHandler {

    private static void postToConnectChargeClient(final Long amount, final Tab tabID, final Context context) {
        RequestQueue queue = Volley.newRequestQueue(context);

        StringRequest sr = new StringRequest(Request.Method.POST, "https://api.stripe.com/v1/charges", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                DatabaseHandler.getInstance().getStripeIDForUser(tabID.getPayerID(), new OnGetDataListener() {
                    @Override
                    public void onStart() {

                    }

                    @Override
                    public void onSuccess(String data) {
                        DatabaseHandler.getInstance().markUserPortionPaidInOwedTab(tabID.tabID(), AppUser.getInstance().getEmail());
                        postToConnectSendToClient(data, amount, context);
                    }

                    @Override
                    public void onFailed(String databaseError) {

                    }
                });

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("~~~~~~~~~>", error.toString());
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("amount", "" + amount * 100);
                params.put("currency", "usd");
                params.put("source", "tok_visa");

                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("Content-Type", "application/x-www-form-urlencoded");
                String auth = "Basic " + Base64.encodeToString("sk_test_hHnZqkTRUgfhd3sRbMpZ4UX7:".getBytes(), Base64.DEFAULT);

                params.put("Authorization", auth);
                params.put("Stripe-Account", AppUser.getInstance().getStripeID());

                return params;
            }
        };
        queue.add(sr);
    }

    private static void postToConnectSendToClient(final String stripeID, final Long amount, Context context) {
        RequestQueue queue = Volley.newRequestQueue(context);

        StringRequest sr = new StringRequest(Request.Method.POST, "https://api.stripe.com/v1/charges", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("----->", response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("~~~~~~~~~>", error.toString());
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("amount", "" + amount * 100);
                params.put("currency", "usd");
                params.put("source", "tok_visa");
                params.put("destination[account]", stripeID);

                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("Content-Type", "application/x-www-form-urlencoded");
                String auth = "Basic " + Base64.encodeToString("sk_test_hHnZqkTRUgfhd3sRbMpZ4UX7:".getBytes(), Base64.DEFAULT);
                params.put("Authorization", auth);
                return params;
            }
        };
        queue.add(sr);
    }

    @SuppressWarnings("unchecked")
    @SuppressLint("DefaultLocale")
    static public void showOwingDialog(Activity thisActivity, final Context context) {
        final ArrayList<Tab> owing = DatabaseHandler.getInstance().getTabsOwing();

        Log.d("DATABASE", "Owing from dialog: " + owing);

        int requests = owing.size();

        for (int i = 0; i < requests; i++) {

            final Dialog paymentDialog = new Dialog(thisActivity);
            paymentDialog.setContentView(R.layout.activity_payment_alert);
            CircleButton dismiss = paymentDialog.findViewById(R.id.dismissButton);
            CircleButton payButton = paymentDialog.findViewById(R.id.payButton);

            dismiss.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    paymentDialog.dismiss();
                }
            });

            TextView fromTextView = paymentDialog.findViewById(R.id.fromText);
            TextView amountTextView = paymentDialog.findViewById(R.id.amountText);

            fromTextView.setText(String.format("From: %s", AppUser.convertEmailFromDatabaseToEmail(owing.get(i).getPayerID())));
            HashMap<String, Object> thisOwerStatus = (HashMap<String, Object>) owing.get(i).getAmountAndPaidForOwer(AppUser.getInstance().getEmailForDatabase());

            if (thisOwerStatus.get("amount") instanceof Double) {
                final Double amountOwedDouble = ((Double) thisOwerStatus.get("amount"));
                final Long amountOwed = amountOwedDouble.longValue();

                amountTextView.setText(String.format("$%.2f", amountOwedDouble));

                final int finalI = i;
                payButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        paymentDialog.dismiss();
                        PaymentHandler.postToConnectChargeClient(amountOwed, owing.get(finalI), context);
                    }
                });

            } else if (thisOwerStatus.get("amount") instanceof Long) {
                final Long amountOwed = (Long) thisOwerStatus.get("amount");

                amountTextView.setText(String.format("$%d", amountOwed));

                final int finalI1 = i;
                payButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        paymentDialog.dismiss();
                        PaymentHandler.postToConnectChargeClient(amountOwed, owing.get(finalI1), context);
                    }
                });
            }

            paymentDialog.show();
        }
    }
}
