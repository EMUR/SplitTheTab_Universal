package andy_eyad_ucsc.splitthetab.model;

import android.util.Log;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Class that manages communication with Firebase database.
 */

@SuppressWarnings("unchecked")
public final class DatabaseHandler {

    private static final String databaseReferenceString = "tabs_database";
    private static final String databaseReferenceUserString = "stripe_customers";
    private static DatabaseHandler instance;
    private String stripeUserID;
    private Map<String, Tab> tabsOwedTo = new HashMap<>();
    private Map<String, Tab> tabsOwing = new HashMap<>();
    private Map<String, Tab> tabsHistory = new HashMap<>();
    private Map<String, Object> recentlyUsedPayerEmails = new HashMap<>();

    private DatabaseHandler() {
        setupOwedToListener();
        setupOwingListener();
        setupRecentlyUsedPayerEmailsListener();
        setupStripeIDListener();
    }

    public static DatabaseHandler getInstance() {
        if (instance == null) {
            instance = new DatabaseHandler();
        }

        return instance;
    }

    public static void resetDatabaseHandler() {
        if (instance != null) {
            instance = new DatabaseHandler();
        }
    }

    public ArrayList<String> getRecentlyUsedPayerEmails() {
        if (recentlyUsedPayerEmails == null)
            recentlyUsedPayerEmails = new HashMap<>();

        ArrayList<String> recentPayersList = new ArrayList<>(recentlyUsedPayerEmails.keySet());

        for (int i = 0; i < recentPayersList.size(); i++) {
            recentPayersList.set(i, AppUser.convertEmailFromDatabaseToEmail(recentPayersList.get(i)));
        }

        return recentPayersList;
    }


    public void getStripeIDForUser(final String userEmail, final OnGetDataListener stripedIDListener) {
        stripedIDListener.onStart();

        final String databaseEmail = AppUser.convertEmailToDatabaseFormat(userEmail);

        Query query = FirebaseDatabase.getInstance()
                .getReference(databaseReferenceUserString).orderByKey().equalTo(databaseEmail);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                HashMap<String, Object> returnValue = (HashMap<String, Object>) dataSnapshot.getValue();
                HashMap<String, Object> returnIDDictionary = (HashMap<String, Object>) returnValue.get(AppUser.convertEmailToDatabaseFormat(userEmail));
                String stripeID = (String) returnIDDictionary.get("stripe_id");
                stripedIDListener.onSuccess(stripeID);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                stripedIDListener.onFailed("FAIL");
            }
        });
    }

    private void setupStripeIDListener() {
        Query query = FirebaseDatabase.getInstance()
                .getReference(databaseReferenceUserString).orderByChild("stripe_id");

        query.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChild) {
                String dataKey = dataSnapshot.getKey();

                if (dataKey.equals(AppUser.getInstance().getEmailForDatabase())) {
                    HashMap<String, String> result = (HashMap<String, String>) dataSnapshot.getValue();
                    stripeUserID = result.get("stripe_id");
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.i("DATABASE", "Cancelled");
            }
        });
    }

    private void setupRecentlyUsedPayerEmailsListener() {
        Query query = FirebaseDatabase.getInstance()
                .getReference(databaseReferenceUserString).orderByChild("recently_paid");

        query.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChild) {
                String dataKey = dataSnapshot.getKey();

                if (dataKey.equals(AppUser.getInstance().getEmailForDatabase()) && !recentlyUsedPayerEmails.containsKey(dataKey)) {
                    HashMap<String, Object> result = (HashMap<String, Object>) dataSnapshot.getValue();
                    recentlyUsedPayerEmails = (HashMap<String, Object>) result.get("recently_paid");
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    public void addTab(Tab tab) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference(databaseReferenceString);
        databaseReference.push().setValue(tab);
    }

    public String getStripeUserID() {
        return this.stripeUserID;
    }

    public void updateTab(String tabID, String userID) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference(databaseReferenceString);

        try {
            databaseReference.child(tabID).child("owers").child(AppUser.convertEmailToDatabaseFormat(userID)).child("paid").setValue(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void addStripeUserIDForUser(String stripeUserID) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference(databaseReferenceUserString);
        Map<String, Object> stripeIDEntry = new HashMap<>();
        stripeIDEntry.put("stripe_id", stripeUserID);
        databaseReference.child(AppUser.getInstance().getEmailForDatabase()).setValue(stripeIDEntry);
    }

    @SuppressWarnings("unused")
    public void addRecentlyPaidUser(final String userEmail, final OnGetDataListener stripedIDListener) {
        final String databaseEmail = AppUser.convertEmailToDatabaseFormat(userEmail);
        final DatabaseReference ref = FirebaseDatabase.getInstance().getReference(databaseReferenceUserString);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(databaseEmail)) {
                    if (recentlyUsedPayerEmails.get(databaseEmail) == null) {
                        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference(databaseReferenceUserString);
                        recentlyUsedPayerEmails.put(databaseEmail, true);
                        databaseReference.child(AppUser.getInstance().getEmailForDatabase()).child("recently_paid").setValue(recentlyUsedPayerEmails);
                    }
                    stripedIDListener.onSuccess(null);
                } else {
                    stripedIDListener.onFailed(null);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    public ArrayList<Tab> getTabsOwing() {
        return new ArrayList<>(tabsOwing.values());
    }

    @SuppressWarnings("unused")
    public ArrayList<Tab> getTabsOwedTo() {
        return new ArrayList<>(tabsOwedTo.values());
    }

    public ArrayList<Tab> getTabsHistory() {
        return new ArrayList<>(tabsHistory.values());
    }

    private void setupOwedToListener() {
        Query query = FirebaseDatabase.getInstance()
                .getReference(databaseReferenceString).orderByChild("payerID").equalTo(AppUser.getInstance().getEmailForDatabase());

        query.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChild) {
                modifyOwed(dataSnapshot);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                Log.i("DATABASE", "Child changed: owed");
                modifyOwed(dataSnapshot);
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                String dataKey = dataSnapshot.getKey();
                tabsOwedTo.remove(dataKey);
                Log.i("DATABASE", "Child removed");
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                Map<String, Object> value = (Map<String, Object>) dataSnapshot.getValue();
                Log.i("DATABASE", "Child moved: " + value.toString());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.i("DATABASE", "Cancelled");
            }
        });
    }

    private void modifyOwed(DataSnapshot dataSnapshot) {
        Log.d("HISTORY", "Modified owed called");

        String dataKey = dataSnapshot.getKey();

        Log.d("HISTORY", "Data snapshot: " + dataSnapshot);

        Map<String, Object> result = (Map<String, Object>) dataSnapshot.getValue();

        Tab thisTab = new Tab(dataSnapshot);

        HashMap<String, Object> owersDictionary = (HashMap<String, Object>) result.get("owers");
        ArrayList<String> owersList = thisTab.owersList();

        int paid = 0;

        for (String ower : owersList) {
            HashMap<String, Boolean> owerStatus = (HashMap<String, Boolean>) owersDictionary.get(ower);
            //Log.d("DATABASE", "Ower status: " + owerStatus);

            if (owerStatus.get("paid")) {
                paid++;
            }
        }

        Log.d("HISTORY", "Paid: " + paid + ", owers: " + owersList.size());

        // Everyone paid
        if (paid == owersList.size()) {
            tabsHistory.put(dataKey, thisTab);
            Log.i("DATABASE", "Added to HISTORY: " + thisTab.toString());

            if (tabsOwedTo.containsKey(dataKey)) {
                tabsOwedTo.remove(dataKey);
            }
        } else {
            tabsOwedTo.put(dataKey, thisTab);
            Log.i("DATABASE", "Added to OWED_TO: " + thisTab.toString());
        }

    }

    private void setupOwingListener() {
        Query query = FirebaseDatabase.getInstance()
                .getReference(databaseReferenceString).orderByChild("owers/" + AppUser.getInstance().getEmailForDatabase()).startAt(0);

        query.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChild) {
                modifyOwing(dataSnapshot);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                Log.i("DATABASE", "Child changed: owing");
                modifyOwing(dataSnapshot);
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                String dataKey = dataSnapshot.getKey();
                tabsOwing.remove(dataKey);
                Log.i("DATABASE", "Child removed");
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                Map<String, Object> value = (Map<String, Object>) dataSnapshot.getValue();
                Log.i("DATABASE", "Child moved: " + value.toString());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.i("DATABASE", "Cancelled");
            }
        });
    }

    private void modifyOwing(DataSnapshot dataSnapshot) {
        Log.d("HISTORY", "Modified owing called");

        String dataKey = dataSnapshot.getKey();

        Map<String, Object> result = (Map<String, Object>) dataSnapshot.getValue();

        Tab thisTab = new Tab(dataSnapshot);

        HashMap<String, Object> owersDictionary = (HashMap<String, Object>) result.get("owers");
        HashMap<String, Boolean> thisOwerStatus = (HashMap<String, Boolean>) owersDictionary.get(AppUser.getInstance().getEmailForDatabase());

        // User paid
        if (thisOwerStatus.get("paid")) {
            tabsHistory.put(dataKey, thisTab);
            Log.i("DATABASE", "Added to HISTORY: " + thisTab.toString());
            tabsOwing.remove(dataKey);
        } else {
            tabsOwing.put(dataKey, thisTab);
            Log.i("DATABASE", "Added to OWING: " + thisTab.toString());
        }
    }

    public void markUserPortionPaidInOwedTab(String tabID, String userID) {
        this.updateTab(tabID, userID);
    }
}
