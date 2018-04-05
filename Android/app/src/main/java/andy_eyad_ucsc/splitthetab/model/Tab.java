package andy_eyad_ucsc.splitthetab.model;

import com.google.firebase.database.DataSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Defines the Tab class, which describes a transaction.
 */

@SuppressWarnings("unused")
public class Tab {

    private String tabID;
    private String payerID;
    private Double totalAmount;
    private Map<String, Object> owers;

    public Tab(String payerID, Double totalAmount, Map<String, Object> owers) {
        this.payerID = payerID;
        this.totalAmount = totalAmount;
        this.owers = owers;
    }

    @SuppressWarnings("unchecked")
    public Tab(DataSnapshot dataSnapshot) {
        Map<String, Object> databaseResult = (Map<String, Object>) dataSnapshot.getValue();

        this.tabID = dataSnapshot.getKey();

        for (Map.Entry<String, Object> element : databaseResult.entrySet()) {
            String currentKey = element.getKey();

            switch (currentKey) {
                case "payerID":
                    this.payerID = (String) element.getValue();
                    break;

                case "totalAmount":
                    this.totalAmount = ((Number) element.getValue()).doubleValue();
                    break;

                case "owers":
                    this.owers = (HashMap<String, Object>) element.getValue();
                    break;
            }
        }
    }

    public String tabID() {
        return this.tabID;
    }

    public String getPayerID() {
        return payerID;
    }

    public void setPayerID(String payerID) {
        this.payerID = payerID;
    }

    public Double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(Double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public Map<String, Object> getOwers() {
        return owers;
    }

    public void setOwers(Map<String, Object> owers) {
        this.owers = owers;
    }

    public ArrayList<String> owersList() {
        if (owers == null) {
            owers = new HashMap<>();
        }

        return new ArrayList<>(owers.keySet());
    }

    public void addOwer(String owerID, Double amount) {
        this.owers.put(owerID, amount);
    }

    public Object getAmountAndPaidForOwer(String owerID) {
        return this.owers.get(owerID);
    }

    @SuppressWarnings({"SuspiciousMethodCalls", "unchecked"})
    @Override
    public String toString() {
        StringBuilder result = new StringBuilder(payerID + " is owed " + totalAmount + ".");

        if (!owers.isEmpty()) {
            result.append(" Owers are ");

            for (Map.Entry<String, Object> ower : owers.entrySet()) {
                HashMap<Double, Boolean> owerStatus = (HashMap<Double, Boolean>) ower.getValue();

                if (owerStatus.get("paid")) {
                    result.append(AppUser.convertEmailFromDatabaseToEmail(ower.getKey())).append(", who paid $").append(owerStatus.get("amount")).append(", ");
                } else {
                    result.append(AppUser.convertEmailFromDatabaseToEmail(ower.getKey())).append(", who owes $").append(owerStatus.get("amount")).append(", ");
                }
            }
        }

        return result.toString();
    }
}
