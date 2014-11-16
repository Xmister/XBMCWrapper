package hu.xmister.xbmcwrapper;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class SmbFragment extends Fragment {

	DialogInterface.OnDismissListener dd = new DialogInterface.OnDismissListener() {

		@Override
		public void onDismiss(DialogInterface dialog) {

		}
	};
	private DialogInterface.OnCancelListener dc = new DialogInterface.OnCancelListener() {

		@Override
		public void onCancel(DialogInterface dialog) {
		}
	};
	private DialogInterface.OnClickListener di = new DialogInterface.OnClickListener() {

		@Override
		public void onClick(DialogInterface dialog, int which) {
			MetBt.setTag(Integer.valueOf(which));
			switch (which) {
			case 0:
				MetBt.setText("MiniDLNA");
				break;
			case 1:
				MetBt.setText("CIFS");
				break;
			case 2:
				MetBt.setText("HTTP");
				break;
			default:
				MetBt.setText("Ask");
				break;
			}

		}
	};
	int fragVal;
	Button MetBt;
	static SmbFragment frag = null;

	static SmbFragment init(int val) {
		if (frag == null) {
			frag = new SmbFragment();
			// Supply val input as an argument.
			Bundle args = new Bundle();
			args.putInt("val", val);
			frag.setArguments(args);
		}
		return frag;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		fragVal = getArguments() != null ? getArguments().getInt("val") : 1;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		View layoutView = inflater.inflate(R.layout.smbfrag, container, false);
		return layoutView;
	}

	@Override
	public void onViewStateRestored(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onViewStateRestored(savedInstanceState);
		SharedPreferences sharedPreferences = getActivity().getSharedPreferences("default", 0);
		MetBt = (Button) getActivity().findViewById(R.id.bt_method);
		MetBt.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				MethodDialog md = new MethodDialog(new String[] { "MiniDLNA",
						"CIFS", "HTTP", "Ask" }, di, dc, dd);
				md.show(getActivity().getSupportFragmentManager(), "method");
			}
		});
		((EditText) getActivity().findViewById(R.id.samba)).setText(sharedPreferences
				.getString("samba", ((EditText) getActivity().findViewById(R.id.samba))
						.getText().toString()));
		((EditText) getActivity().findViewById(R.id.rfrom)).setText(sharedPreferences
				.getString("rfrom", ((EditText) getActivity().findViewById(R.id.rfrom))
						.getText().toString()));
		((EditText) getActivity().findViewById(R.id.rto)).setText(sharedPreferences
				.getString("rto", ((EditText) getActivity().findViewById(R.id.rto)).getText()
						.toString()));
		((EditText) getActivity().findViewById(R.id.rfrom2)).setText(sharedPreferences
				.getString("rfrom2", ((EditText) getActivity().findViewById(R.id.rfrom2))
						.getText().toString()));
		((EditText) getActivity().findViewById(R.id.rto2)).setText(sharedPreferences
				.getString("rto2", ((EditText) getActivity().findViewById(R.id.rto2))
						.getText().toString()));
		((CheckBox) getActivity().findViewById(R.id.r1)).setChecked(sharedPreferences
				.getBoolean("r1",
						((CheckBox) getActivity().findViewById(R.id.r1)).isChecked()));
		((CheckBox) getActivity().findViewById(R.id.r2)).setChecked(sharedPreferences
				.getBoolean("r2",
						((CheckBox) getActivity().findViewById(R.id.r2)).isChecked()));
		((EditText) getActivity().findViewById(R.id.cifs)).setText(sharedPreferences
				.getString("cifs", ((EditText) getActivity().findViewById(R.id.cifs))
						.getText().toString()));
		((EditText) getActivity().findViewById(R.id.smbuser)).setText(sharedPreferences
				.getString("smbuser", ((EditText) getActivity().findViewById(R.id.smbuser))
						.getText().toString()));
		((EditText) getActivity().findViewById(R.id.smbpass)).setText(sharedPreferences
				.getString("smbpass", ((EditText) getActivity().findViewById(R.id.smbpass))
						.getText().toString()));
		di.onClick(null, sharedPreferences.getInt("method", 3));
	}
	
	public void save() {
		if (getActivity() != null) {
			try {
			SharedPreferences sharedPreferences = getActivity()
					.getSharedPreferences("default", 0);
			Editor editor = sharedPreferences.edit();
			editor.putString("samba",
					((EditText) getActivity().findViewById(R.id.samba))
							.getText().toString());
			editor.putString("rfrom",
					((EditText) getActivity().findViewById(R.id.rfrom))
							.getText().toString());
			editor.putString("rto",
					((EditText) getActivity().findViewById(R.id.rto)).getText()
							.toString());
			editor.putString("rfrom2",
					((EditText) getActivity().findViewById(R.id.rfrom2))
							.getText().toString());
			editor.putString("rto2",
					((EditText) getActivity().findViewById(R.id.rto2))
							.getText().toString());
			editor.putBoolean("r1",
					((CheckBox) getActivity().findViewById(R.id.r1))
							.isChecked());
			editor.putBoolean("r2",
					((CheckBox) getActivity().findViewById(R.id.r2))
							.isChecked());
			editor.putString("cifs",
					((EditText) getActivity().findViewById(R.id.cifs))
							.getText().toString());
			editor.putString("smbuser",
					((EditText) getActivity().findViewById(R.id.smbuser))
							.getText().toString());
			editor.putString("smbpass",
					((EditText) getActivity().findViewById(R.id.smbpass))
							.getText().toString());
			editor.putInt("method", (Integer) MetBt.getTag());
			editor.commit();
			} catch (Exception e) {}
		}
	}
	
	@Override
	public void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		((Go)getActivity()).save();
	}
}
