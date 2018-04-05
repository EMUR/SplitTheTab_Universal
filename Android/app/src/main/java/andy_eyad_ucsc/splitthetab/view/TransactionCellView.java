package andy_eyad_ucsc.splitthetab.view;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import andy_eyad_ucsc.splitthetab.R;

/**
 * Custom cell view for table
 */

public class TransactionCellView extends RelativeLayout {

    private TextView amountNameTextView;
    private TextView owerNameTextView;
    public boolean payout;
    public TransactionCellView(Context context) {
        super(context);
        inflate(getContext(), R.layout.trans_row_item, this);
    }

    public void makeItACPayCell(){
        payout = true;
    }

//    @Override
//    protected void dispatchDraw(Canvas canvas) {
//        super.dispatchDraw(canvas);
//        Log.d("==========>", String.valueOf(charge));
//
//        if(charge)
//        {
//            amountNameTextView.setTextColor(Color.BLUE);
//        }
//    }


    @Override
    protected void onVisibilityChanged(@NonNull View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        if(payout)
        {
            TextView amountTextV = changedView.findViewById(R.id.amountOwed);
            ImageView imgView = changedView.findViewById(R.id.moneyStatusImg);
            if(amountTextV != null)
            {
                amountTextV.setTextColor(getResources().getColor(R.color.colorAccent));
                imgView.setRotation(imgView.getRotation() + 180);

            }
        }

    }

    public TransactionCellView(final Context context, String ower, Double amount) {
        super(context);
        inflate(getContext(), R.layout.trans_row_item, this);

        TextView amountNameTextView = findViewById(R.id.amountOwed);
        TextView owerNameTextView = findViewById(R.id.nameOwers);

        String displayedAmount = "$" + amount;
        amountNameTextView.setText(displayedAmount);
        owerNameTextView.setText(ower);
    }


    public TransactionCellView(final Context context, String ower, Long amount) {
        super(context);
        inflate(getContext(), R.layout.trans_row_item, this);

        TextView amountNameTextView = findViewById(R.id.amountOwed);
        TextView owerNameTextView = findViewById(R.id.nameOwers);

        String displayedAmount = "$" + amount;
        amountNameTextView.setText(displayedAmount);
        owerNameTextView.setText(ower);
    }

}
