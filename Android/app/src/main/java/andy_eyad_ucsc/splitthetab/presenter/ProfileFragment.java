package andy_eyad_ucsc.splitthetab.presenter;


import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

import andy_eyad_ucsc.splitthetab.R;
import andy_eyad_ucsc.splitthetab.model.AppUser;
import andy_eyad_ucsc.splitthetab.model.DatabaseHandler;
import andy_eyad_ucsc.splitthetab.model.Tab;
import andy_eyad_ucsc.splitthetab.view.SplitListAdapter;
import andy_eyad_ucsc.splitthetab.view.TransactionCellView;
import lib.kingja.switchbutton.SwitchMultiButton;

/**
 * A simple {@link Fragment} subclass.
 */
@SuppressWarnings("unchecked")
public class ProfileFragment extends Fragment {

    SplitListAdapter arrayAdapter;
    private ArrayList<TransactionCellView> cardContainer;
    private ListView transactionListView;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        ListView transactionListView = view.findViewById(R.id.transactionListView);
        TextView usernameTextView = view.findViewById(R.id.usernameTextView);
        TextView emailTextView = view.findViewById(R.id.emailTextView);
        cardContainer = new ArrayList<>();

        if (AppUser.getInstance().userIsLoggedIn()) {
            String[] parts = AppUser.getInstance().getEmail().split("@");
            usernameTextView.setText(String.format("%s%s", parts[0].substring(0, 1).toUpperCase(), parts[0].substring(1).toLowerCase()));
            emailTextView.setText(AppUser.getInstance().getEmail());
        }

        arrayAdapter = new SplitListAdapter(cardContainer, view.getContext(), true);
        transactionListView.setAdapter(arrayAdapter);
        SwitchMultiButton mSwitchMultiButton = (SwitchMultiButton) view.findViewById(R.id.switcher);
        mSwitchMultiButton.setText("Pending Outgoing","Pending Incoming","History").setOnSwitchListener(new SwitchMultiButton.OnSwitchListener() {
            @Override
            public void onSwitch(int position, String tabText) {
                if( position == 0)
                {
                    loadOwing(getContext());
                    arrayAdapter.notifyDataSetChanged();

                }else if( position == 1){
                    loadOwed(getContext());
                    arrayAdapter.notifyDataSetChanged();
                }else {
                    loadHistory(getContext());
                    arrayAdapter.notifyDataSetChanged();
                }
            }
        });

        loadOwing(this.getContext());
        return view;
    }

    public void loadOwed(Context context) {
        if (AppUser.getInstance().userIsLoggedIn()) {
            cardContainer.clear();

            DatabaseHandler handler = DatabaseHandler.getInstance();

            for (int i = 0; i < handler.getTabsOwedTo().size(); i++) {
                Tab thisTab = handler.getTabsOwedTo().get(i);

                ArrayList<String> owersList = thisTab.owersList();
                if (thisTab.getPayerID().equals(AppUser.getInstance().getEmailForDatabase())) {
                    for (String ower : owersList) {
                        HashMap<String, Object> owerStatus = (HashMap<String, Object>) thisTab.getAmountAndPaidForOwer(ower);
                        Log.d("DATABASE", "Outgoing status: " + owerStatus);
                        if (!((Boolean) owerStatus.get("paid"))) {
                            try {
                                cardContainer.add(new TransactionCellView(context, AppUser.convertEmailFromDatabaseToEmail(ower), (Long) owerStatus.get("amount")));

                            }catch (java.lang.ClassCastException e)
                            {
                                cardContainer.add(new TransactionCellView(context,  AppUser.convertEmailFromDatabaseToEmail(ower), (Double) owerStatus.get("amount")));
                            }
                        }
                    }
                }
            }
        }
    }

    public void loadOwing(Context context)
    {
        if (AppUser.getInstance().userIsLoggedIn()) {

            cardContainer.clear();

            DatabaseHandler handler = DatabaseHandler.getInstance();

            Log.d("Profile-OWING", String.valueOf(handler.getTabsOwing().size()));

            for (int i = 0; i < handler.getTabsOwing().size(); i++) {
                Tab thisTab = handler.getTabsOwing().get(i);

                if (!thisTab.getPayerID().equals(AppUser.getInstance().getEmailForDatabase())) {
                    ArrayList<String> owersList = thisTab.owersList();
                    for (String ower : owersList) {
                        HashMap<String, Object> owerStatus = (HashMap<String, Object>) thisTab.getAmountAndPaidForOwer(ower);
                        Log.d("DATABASE", "Incoming status: " + owerStatus);
                        if (!((Boolean) owerStatus.get("paid")) && AppUser.getInstance().getEmailForDatabase().equals(ower)) {
                            TransactionCellView tran;
                            try {
                                tran = new TransactionCellView(context, AppUser.convertEmailFromDatabaseToEmail(thisTab.getPayerID()), (Long) owerStatus.get("amount"));
                                tran.makeItACPayCell();
                                cardContainer.add(tran);

                            } catch (java.lang.ClassCastException e) {
                                tran = new TransactionCellView(context, AppUser.convertEmailFromDatabaseToEmail(thisTab.getPayerID()), (Double) owerStatus.get("amount"));
                                tran.makeItACPayCell();
                                cardContainer.add(tran);
                            }

                        }
                    }
                }
            }
            }
    }


    public void loadHistory(Context context)
    {
        if (AppUser.getInstance().userIsLoggedIn()) {

            cardContainer.clear();

            DatabaseHandler handler = DatabaseHandler.getInstance();

            for (int i = 0; i < handler.getTabsHistory().size(); i++) {
                Tab thisTab = handler.getTabsHistory().get(i);


                    ArrayList<String> owersList = thisTab.owersList();
                    Log.d("HISTORY",String.format("payerID:%s   -    user:%s",thisTab.getPayerID(),AppUser.getInstance().getEmailForDatabase()));
                if (thisTab.getPayerID().equals(AppUser.getInstance().getEmailForDatabase())){

                    for (String ower : owersList) {
                        HashMap<String, Object> owerStatus = (HashMap<String, Object>) thisTab.getAmountAndPaidForOwer(ower);
                        Log.d("HISTORY", "Ower :::: " + ower);

                        if (((Boolean) owerStatus.get("paid"))) {
                            try {
                                cardContainer.add(new TransactionCellView(context, AppUser.convertEmailFromDatabaseToEmail(ower), (Long) owerStatus.get("amount")));
                            }catch (java.lang.ClassCastException e)
                            {
                                cardContainer.add(new TransactionCellView(context,  AppUser.convertEmailFromDatabaseToEmail(ower), (Double) owerStatus.get("amount")));
                            }
                        }
                    }
                }else {
                    HashMap<String, Object> owerStatus = null;

                    for (String ower : owersList) {
                        if(ower.equals(AppUser.getInstance().getEmailForDatabase())) {
                            owerStatus = (HashMap<String, Object>) thisTab.getAmountAndPaidForOwer(ower);
                            break;
                        }
                    }
                    TransactionCellView trans;

                    if(owerStatus != null) {
                        try {
                             trans = new TransactionCellView(context,  AppUser.convertEmailFromDatabaseToEmail(thisTab.getPayerID()), (Double) owerStatus.get("amount"));

                        }catch (java.lang.ClassCastException e)
                        {
                             trans = new TransactionCellView(context, AppUser.convertEmailFromDatabaseToEmail(thisTab.getPayerID()), (Long) owerStatus.get("amount"));

                        }
                        
                        trans.makeItACPayCell();
                        cardContainer.add(trans);
                    }

                }
            }
        }

    }
}
