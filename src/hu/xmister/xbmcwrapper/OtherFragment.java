package hu.xmister.xbmcwrapper;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class OtherFragment extends Fragment {

	int fragVal;
	static OtherFragment frag = null;
	private SeekBar sb;

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
		((CheckBox) getActivity().findViewById(R.id.ch_xmlVideo)).setChecked(sharedPreferences
				.getBoolean("xmlvideo",
						((CheckBox) getActivity().findViewById(R.id.ch_xmlVideo)).isChecked()));
		sb=(SeekBar)getActivity().findViewById(R.id.resolution);
		sb.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				TextView tv= (TextView) getActivity().findViewById(R.id.otherTextView17);
				switch (progress) {
				case 0:
					tv.setText("ALL");
					break;
				case 1:
					tv.setText("540p");
					break;
				case 2:
					tv.setText("720p");
					break;
				case 3:
					tv.setText("1080p");
					break;
				}
				
			}
		});
		sb.setProgress(sharedPreferences.getInt("resolution", sb.getProgress()));
		
	}
	
	public void save() {
		if (getActivity() != null) {
			try {
			SharedPreferences sharedPreferences = getActivity()
					.getSharedPreferences("default", 0);
			Editor editor = sharedPreferences.edit();
			editor.putString("http",
					((EditText) getActivity().findViewById(R.id.http))
							.getText().toString());
			editor.putString("file",
					((EditText) getActivity().findViewById(R.id.file))
							.getText().toString());

			editor.putString("mddb",
					((EditText) getActivity().findViewById(R.id.mddb))
							.getText().toString());
			editor.putInt(
					"mdcut",
					Integer.valueOf(((EditText) getActivity().findViewById(
							R.id.mdcut)).getText().toString()));

			editor.putBoolean("rehttp",
					((CheckBox) getActivity().findViewById(R.id.ch_rehttp))
							.isChecked());
			editor.putBoolean("xmlvideo",
					((CheckBox) getActivity().findViewById(R.id.ch_xmlVideo))
							.isChecked());
			editor.putInt(
					"resolution",
					Integer.valueOf(((SeekBar) getActivity().findViewById(
							R.id.resolution)).getProgress()));
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
