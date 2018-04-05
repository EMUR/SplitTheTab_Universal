package andy_eyad_ucsc.splitthetab.model;

/**
 * Interface to get tripeID
 */

public interface OnGetDataListener {
    public void onStart();

    public void onSuccess(String data);

    public void onFailed(String databaseError);
}
