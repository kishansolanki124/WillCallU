package app.com.willcallu;

import android.content.Context;
import android.support.v7.app.AlertDialog;

public class PermissionDialog extends AlertDialog {

    public PermissionDialog(Context context,
                            OnClickListener OkOnClickListener, OnClickListener CancelOnClickListener, String message, String label) {
        super(context);

        Builder builder = new Builder(context, R.style.AlertDialogStyle);
        builder.setTitle(label);
        builder.setMessage(message);
        builder.setPositiveButton(android.R.string.ok, OkOnClickListener);
        builder.setNegativeButton(R.string.cancel, CancelOnClickListener);

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
}