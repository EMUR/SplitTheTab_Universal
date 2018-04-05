package andy_eyad_ucsc.splitthetab.view;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import java.util.ArrayList;

import andy_eyad_ucsc.splitthetab.R;

/**
 * Adapter for splitters list
 */

@SuppressWarnings("unchecked")
public class SplitListAdapter extends ArrayAdapter {

    private ArrayList<UserCardView> dataSetForUser;
    private ArrayList<TransactionCellView> dataSetForTrans;
    private boolean isSecure;

    public SplitListAdapter(ArrayList<UserCardView> data, Context context) {
        super(context, R.layout.row_item, data);
        this.dataSetForUser = data;
        isSecure = false;
    }

    public SplitListAdapter(ArrayList<TransactionCellView> data, Context context, boolean secure) {
        super(context, R.layout.trans_row_item, data);
        this.dataSetForTrans = data;
        isSecure = true;
    }


    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (isSecure) {
            return dataSetForTrans.get(position);

        } else {
            return dataSetForUser.get(position);
        }
    }
}

