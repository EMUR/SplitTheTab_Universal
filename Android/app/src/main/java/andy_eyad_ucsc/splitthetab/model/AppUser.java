package andy_eyad_ucsc.splitthetab.model;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * Contains user information, both from Firebase and from Stripe
 */

public class AppUser {
    private static final String TAG = "LOGIN";
    private static AppUser instance;
    private String name;
    private String email;
    private Uri photoUrl;
    private String userID;
    private String displayName;
    private boolean userIsLoggedIn = false;

    private AppUser() {
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            name = user.getDisplayName();
            email = user.getEmail();
            photoUrl = user.getPhotoUrl();
            userID = user.getUid();
            displayName = user.getDisplayName();
        }

        new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (user != null) {
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                    userIsLoggedIn = true;

                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                    userIsLoggedIn = false;
                }

                DatabaseHandler.resetDatabaseHandler();
            }
        };
    }

    public static AppUser getInstance() {
        if (instance == null) {
            instance = new AppUser();
        }

        return instance;
    }

    public static void updateUser() {
        instance = new AppUser();
    }

    public static String convertEmailFromDatabaseToEmail(String emailFromDatabase) {
        return emailFromDatabase.replace("~", ".");
    }

    public static String convertEmailToDatabaseFormat(String email) {
        return email.replace(".", "~");
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getEmailForDatabase() {
        return convertEmailToDatabaseFormat(email);
    }

    public Uri getPhotoUrl() {
        return photoUrl;
    }

    public String getUserID() {
        return userID;
    }

    public String getStripeID() {
        return DatabaseHandler.getInstance().getStripeUserID();
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean userIsLoggedIn() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        userIsLoggedIn = user != null;

        return userIsLoggedIn;
    }
}
