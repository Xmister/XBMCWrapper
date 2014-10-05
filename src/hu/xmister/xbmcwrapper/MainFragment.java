package hu.xmister.xbmcwrapper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;

public class MainFragment extends Fragment implements OnClickListener {

	int fragVal;
	static MainFragment frag = null;
	TextView Montext;
	Button MonBt;

	static MainFragment init(int val) {
		if (frag == null) {
			frag = new MainFragment();
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
		View layoutView = inflater
				.inflate(R.layout.otherfrag, container, false);
		return layoutView;
	}

	@Override
	public void onViewStateRestored(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onViewStateRestored(savedInstanceState);
		Montext = (TextView) getActivity().findViewById(R.id.status);
		MonBt = (Button) getActivity().findViewById(R.id.button1);
		MonBt.setOnClickListener(this);

		File PlayerC = new File(
				Environment.getExternalStorageDirectory().getPath()
						+ "/Android/data/org.xbmc.xbmc/files/.xbmc/userdata/playercorefactory.xml");
		if (PlayerC.exists())
			Montext.setText("playercorefactory.xml already exist");
		else
			Montext.setText("playercorefactory.xml not found");

		File XbmcC = new File(Environment.getExternalStorageDirectory()
				.getPath() + "/Android/data/org.xbmc.xbmc/files/.xbmc/userdata");
		if (!XbmcC.isDirectory()) {
			Montext.setText("XBMC not found");
			MonBt.setClickable(false);
		} else
			MonBt.setClickable(true);
	}

	private void saveXML() {
		AssetManager assetManager = getActivity().getAssets();
		InputStream is;
		AssetFileDescriptor fd;
		try {
			fd = assetManager.openFd("playercorefactory");
			is = fd.createInputStream();
		} catch (IOException e1) {
			Montext.setText("Is error");
			return;
		}
		if (is == null) {
			Montext.setText("Is null");
			return;
		}

		OutputStream os;
		try {
			os = new FileOutputStream(
					Environment.getExternalStorageDirectory().getPath()
							+ "/Android/data/org.xbmc.xbmc/files/.xbmc/userdata/playercorefactory.xml");
			byte[] buffer = new byte[(int) fd.getLength()];
			is.read(buffer);
			is.close();
			String xml = new String(buffer);
			if (((CheckBox) getActivity().findViewById(R.id.ch_pvrEnable)).isChecked()) {
				xml=xml.replace("!PVR!","<rule protocols=\"pvr\" player=\"XBMCWrapper\" />");
			}
			else {
				xml=xml.replace("!PVR!","");
			}
			buffer=xml.getBytes();
				os.write(buffer, 0, buffer.length);
			os.close();
		} catch (Exception e) {
			Montext.setText("Error !!" + e.getMessage());
			return;
		}
	}

	@Override
	public void onClick(View v) {
		saveXML();
		Montext.setText("Install ok :-)");
	}

	public void save() {
		File PlayerC = new File(
				Environment.getExternalStorageDirectory().getPath()
						+ "/Android/data/org.xbmc.xbmc/files/.xbmc/userdata/playercorefactory.xml");
		if (PlayerC.exists())
			saveXML();
	}

	@Override
	public void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		save();
	}
}
