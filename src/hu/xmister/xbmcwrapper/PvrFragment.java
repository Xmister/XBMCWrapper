package hu.xmister.xbmcwrapper;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;

public class PvrFragment extends Fragment {
	int fragVal;
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
		((CheckBox) getActivity().findViewById(R.id.ch_restream))
				.setChecked(sharedPreferences.getBoolean("restream",
						((CheckBox) getActivity()
								.findViewById(R.id.ch_restream)).isChecked()));
		((CheckBox) getActivity().findViewById(R.id.ch_pvrEnable))
		.setChecked(sharedPreferences.getBoolean("pvrEnable",
				((CheckBox) getActivity()
						.findViewById(R.id.ch_pvrEnable)).isChecked()));
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
			editor.putBoolean("restream",
					((CheckBox) getActivity().findViewById(R.id.ch_restream))
							.isChecked());
			editor.putBoolean("pvrEnable",
					((CheckBox) getActivity().findViewById(R.id.ch_pvrEnable))
							.isChecked());
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
