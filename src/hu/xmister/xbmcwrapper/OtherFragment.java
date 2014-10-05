package hu.xmister.xbmcwrapper;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class OtherFragment extends Fragment {

	int fragVal;
	static OtherFragment frag = null;

	static OtherFragment init(int val) {
		if (frag == null) {
			frag = new OtherFragment();
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
		View layoutView = inflater.inflate(R.layout.otherfrag, container, false);
		return layoutView;
	}

	@Override
	public void onViewStateRestored(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onViewStateRestored(savedInstanceState);
		SharedPreferences sharedPreferences = getActivity().getSharedPreferences("default", 0);
		((EditText) getActivity().findViewById(R.id.http)).setText(sharedPreferences
				.getString("http", ((EditText) getActivity().findViewById(R.id.http))
						.getText().toString()));		
		((EditText) getActivity().findViewById(R.id.file)).setText(sharedPreferences
				.getString("file", ((EditText) getActivity().findViewById(R.id.file))
						.getText().toString()));
		((EditText) getActivity().findViewById(R.id.mddb)).setText(sharedPreferences
				.getString("mddb", ((EditText) getActivity().findViewById(R.id.mddb))
						.getText().toString()));
		((EditText) getActivity().findViewById(R.id.mdcut)).setText(String
				.valueOf(sharedPreferences.getInt("mdcut", Integer
						.valueOf(((EditText) getActivity().findViewById(R.id.mdcut))
								.getText().toString()))));
		((CheckBox) getActivity().findViewById(R.id.ch_rehttp)).setChecked(sharedPreferences
				.getBoolean("rehttp",
						((CheckBox) getActivity().findViewById(R.id.ch_rehttp)).isChecked()));
	}
	
	public void save() {
		SharedPreferences sharedPreferences = getActivity().getSharedPreferences("default", 0);
		Editor editor = sharedPreferences.edit();
		 editor.putString("http", ((EditText)
		 getActivity().findViewById(R.id.http)).getText()
		 .toString());
		 editor.putString("file", ((EditText)
		 getActivity().findViewById(R.id.file)).getText()
		 .toString());
		
		 editor.putString("mddb", ((EditText)
		 getActivity().findViewById(R.id.mddb)).getText()
		 .toString());
		 editor.putInt("mdcut", Integer
		 .valueOf(((EditText) getActivity().findViewById(R.id.mdcut)).getText()
		 .toString()));
		
		 editor.putBoolean("rehttp",
		 ((CheckBox) getActivity().findViewById(R.id.ch_rehttp)).isChecked());
		editor.commit();
	}
	
	@Override
	public void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		save();
	}
}
