package hu.xmister.xbmcwrapper;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

@SuppressLint("ValidFragment")
public class MethodDialog extends DialogFragment {
	private DialogInterface.OnClickListener olistener;
	private DialogInterface.OnCancelListener clistener;
	private DialogInterface.OnDismissListener dlistener;
	private String[] options;
	
	public MethodDialog(String[] options,DialogInterface.OnClickListener ol, DialogInterface.OnCancelListener cl, DialogInterface.OnDismissListener cd) {
		olistener=ol;	
		clistener=cl;
		dlistener=cd;
		this.options=options;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
	    builder.setTitle("Choose method")
	           .setItems(options, olistener).setOnCancelListener(clistener);
	    return builder.create();
	}
	
	@Override
	public void onDismiss(DialogInterface dialog) {
		dlistener.onDismiss(dialog);
		super.onDismiss(dialog);
	}

}
