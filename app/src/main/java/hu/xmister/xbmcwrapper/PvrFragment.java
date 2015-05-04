package hu.xmister.xbmcwrapper;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import java.util.List;

public class PvrFragment extends Fragment {
	int fragVal;
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
			PVRBt.setTag(Integer.valueOf(which));
			switch (which) {
			case 1:
				PVRBt.setText("TvHeadend");
				break;
			case 2:
				PVRBt.setText("DVBViewer");
				break;
			case 0:
			default:
				PVRBt.setText("Disabled");
				break;
			}

		}
	};
	Button PVRBt;

	Button PlayBT;
	String[] items=null;
	private DialogInterface.OnClickListener sdi = new DialogInterface.OnClickListener() {

		@Override
		public void onClick(DialogInterface dialog, int which) {
			if ( items != null ) {
				((EditText) getActivity().findViewById(R.id.pvr)).setText(items[which]);
			}
		}
	};
	
	static PvrFragment frag;

	static PvrFragment init(int val) {
		if (frag == null) {
			frag = new PvrFragment();
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
		View layoutView = inflater.inflate(R.layout.pvrfrag, container, false);
		return layoutView;
	}

	@Override
	public void onViewStateRestored(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onViewStateRestored(savedInstanceState);
		SharedPreferences sharedPreferences = getActivity()
				.getSharedPreferences("default", 0);
		PlayBT = (Button) getActivity().findViewById(R.id.bt_pvrplayer);
		PlayBT.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				final Intent mainIntent = new Intent(Intent.ACTION_VIEW);
				mainIntent.setType("video/*");
				//mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
				final List<ResolveInfo> pkgAppsList = getActivity().getPackageManager().queryIntentActivities(mainIntent, 0);
				items = new String[pkgAppsList.size()];
				int i = 0;
				for (ResolveInfo ri : pkgAppsList) {
					items[i++] = ri.resolvePackageName;
				}
				ChoiceDialog md = new ChoiceDialog("Choose a Player", items, sdi, dc, dd);
				md.show(getActivity().getSupportFragmentManager(), "pvrplayer");
			}
		});
		PVRBt = (Button) getActivity().findViewById(R.id.bt_backend);
		PVRBt.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				ChoiceDialog md = new ChoiceDialog("Choose a backend",new String[] { "Disabled",
						"TvHeadend", "DVBViewer" }, di, dc, dd);
				md.show(getActivity().getSupportFragmentManager(), "backend");
			}
		});
		((EditText) getActivity().findViewById(R.id.pvr))
				.setText(sharedPreferences.getString("pvr",
						((EditText) getActivity().findViewById(R.id.pvr))
								.getText().toString()));
		((EditText) getActivity().findViewById(R.id.pvrmap))
				.setText(sharedPreferences.getString("pvrmap",
						((EditText) getActivity().findViewById(R.id.pvrmap))
								.getText().toString()));
		((EditText) getActivity().findViewById(R.id.tvh))
				.setText(sharedPreferences.getString("tvh",
						((EditText) getActivity().findViewById(R.id.tvh))
								.getText().toString()));
		((EditText) getActivity().findViewById(R.id.excludePvr))
		.setText(sharedPreferences.getString("excludepvr",
				((EditText) getActivity().findViewById(R.id.excludePvr))
						.getText().toString()));
		((CheckBox) getActivity().findViewById(R.id.ch_restream))
				.setChecked(sharedPreferences.getBoolean("restream",
						((CheckBox) getActivity()
								.findViewById(R.id.ch_restream)).isChecked()));
		((CheckBox) getActivity().findViewById(R.id.ch_pvrEnable))
		.setChecked(sharedPreferences.getBoolean("pvrEnable",
				((CheckBox) getActivity()
						.findViewById(R.id.ch_pvrEnable)).isChecked()));
						
		di.onClick(null, sharedPreferences.getInt("backend", 1));
	}

	public void save() {
		if (getActivity() != null) {
			try {
			SharedPreferences sharedPreferences = getActivity()
					.getSharedPreferences("default", 0);
			Editor editor = sharedPreferences.edit();
			editor.putString("pvr", ((EditText) getActivity()
					.findViewById(R.id.pvr)).getText().toString());
			editor.putString("pvrmap",
					((EditText) getActivity().findViewById(R.id.pvrmap)).getText()
							.toString());
			editor.putString("tvh", ((EditText) getActivity()
					.findViewById(R.id.tvh)).getText().toString());
			editor.putString("excludepvr", ((EditText) getActivity()
					.findViewById(R.id.excludePvr)).getText().toString());
			editor.putBoolean("restream",
					((CheckBox) getActivity().findViewById(R.id.ch_restream))
							.isChecked());
			editor.putBoolean("pvrEnable",
					((CheckBox) getActivity().findViewById(R.id.ch_pvrEnable))
							.isChecked());
			editor.putInt("backend", (Integer) PVRBt.getTag());
			editor.commit();
			} catch (Exception e) {}
		}
	}

	@Override
	public void onPause() {
		// TODO Auto-generated method stub
		((Go)getActivity()).save();
		super.onPause();
	}
}
