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
import android.widget.TextView;
import android.content.SharedPreferences;
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
				.inflate(R.layout.mainfrag, container, false);
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
		if (getActivity() != null) {
			AssetManager assetManager = getActivity().getAssets();
			SharedPreferences sharedPreferences = getActivity()
					.getSharedPreferences("default", 0);
			InputStream is;
			long length = 0;
			try {
				is = assetManager.open("playercorefactory", AssetManager.ACCESS_BUFFER);
				length=is.available();
			} catch (IOException e1) {
				Montext.setText("IO error");
				return;
			}

			OutputStream os;
			try {
				os = new FileOutputStream(
						Environment.getExternalStorageDirectory().getPath()
								+ "/Android/data/org.xbmc.xbmc/files/.xbmc/userdata/playercorefactory.xml");
				byte[] buffer = new byte[(int) length];
				is.read(buffer);
				is.close();
				String xml = new String(buffer);
				String temp = new String("<rule protocols=\"smb\" name=\"XBMCWrapper\" >\n");
				if (sharedPreferences.getInt("resolution", 1) != 0) {
					for (int i = sharedPreferences.getInt("resolution", 1); i <= 2; i++) {
						switch (i) {
						case 1:
							temp += "\t<rule video=\"true\" videoresolution=\"540\" player=\"XBMCWrapper\"/>\n";
							temp += "\t<rule filename=\".*540.*\" player=\"XBMCWrapper\"/>\n";
							break;
						case 2:
							temp += "\t<rule video=\"true\" videoresolution=\"720\" player=\"XBMCWrapper\"/>\n";
							temp += "\t<rule filename=\".*720.*\" player=\"XBMCWrapper\"/>\n";
							break;
						case 3:
							temp += "\t<rule video=\"true\" videoresolution=\"1080\" player=\"XBMCWrapper\"/>\n";
							temp += "\t<rule filename=\".*1080.*\" player=\"XBMCWrapper\"/>\n";
							break;
						}
					}
				}
				temp+="</rule>\n";
				xml = xml
						.replace("!SMB!",
								temp);
				if (sharedPreferences.getBoolean("pvrEnable",true)) {
					temp = new String("<rule protocols=\"pvr\" player=\"XBMCWrapper\" >\n");
					if (sharedPreferences.getInt("resolution", 1) != 0) {
						for (int i = sharedPreferences.getInt("resolution", 1); i <= 2; i++) {
							switch (i) {
							case 1:
								temp += "\t<rule video=\"true\" videoresolution=\"540\" player=\"XBMCWrapper\"/>\n";
								temp += "\t<rule filename=\".*540.*\" player=\"XBMCWrapper\"/>\n";
								break;
							case 2:
								temp += "\t<rule video=\"true\" videoresolution=\"720\" player=\"XBMCWrapper\"/>\n";
								temp += "\t<rule filename=\".*720.*\" player=\"XBMCWrapper\"/>\n";
								break;
							case 3:
								temp += "\t<rule video=\"true\" videoresolution=\"1080\" player=\"XBMCWrapper\"/>\n";
								temp += "\t<rule filename=\".*1080.*\" player=\"XBMCWrapper\"/>\n";
								break;
							}
						}
					}
					temp +="</rule>\n";
					xml = xml
							.replace("!PVR!",
									temp);
				} else {
					xml = xml.replace("!PVR!", "");
				}
				if (sharedPreferences.getBoolean("xmlvideo",true)) {
					temp = new String("");
					if (sharedPreferences.getInt("resolution", 1) != 0) {
						for (int i = sharedPreferences.getInt("resolution", 1); i <= 2; i++) {
							switch (i) {
							case 1:
								temp += "\t<rule video=\"true\" videoresolution=\"540\" player=\"XBMCWrapper\">\n\t<rule filename=\".*540.*\" player=\"XBMCWrapper\"/>\n\t</rule>";
								break;
							case 2:
								temp += "\t<rule video=\"true\" videoresolution=\"720\" player=\"XBMCWrapper\">\n\t<rule filename=\".*720.*\" player=\"XBMCWrapper\"/>\n\t</rule>";
								break;
							case 3:
								temp += "\t<rule video=\"true\" videoresolution=\"1080\" player=\"XBMCWrapper\">\n\t<rule filename=\".*1080.*\" player=\"XBMCWrapper\"/>\n\t</rule>";
								break;
							}
						}
					} else temp="<rule video=\"true\" player=\"XBMCWrapper\"/>\n";
					temp +="\n";
					temp +="<rule dvdimage=\"true\" player=\"XBMCWrapper\" >\n";
					if (sharedPreferences.getInt("resolution", 1) != 0) {
						for (int i = sharedPreferences.getInt("resolution", 1); i <= 2; i++) {
							switch (i) {
							case 1:
								temp += "\t<rule video=\"true\" videoresolution=\"540\" player=\"XBMCWrapper\"/>\n";
								temp += "\t<rule filename=\".*540.*\" player=\"XBMCWrapper\"/>\n";
								break;
							case 2:
								temp += "\t<rule video=\"true\" videoresolution=\"720\" player=\"XBMCWrapper\"/>\n";
								temp += "\t<rule filename=\".*540.*\" player=\"XBMCWrapper\"/>\n";
								break;
							case 3:
								temp += "\t<rule video=\"true\" videoresolution=\"1080\" player=\"XBMCWrapper\"/>\n";
								temp += "\t<rule filename=\".*540.*\" player=\"XBMCWrapper\"/>\n";
								break;
							}
						}
					}
					temp +="</rule>\n<rule protocols=\"rtmp\" player=\"XBMCWrapper\"/>\n<rule protocols=\"rtsp\" player=\"XBMCWrapper\" />\n<rule protocols=\"sop\" player=\"XBMCWrapper\" />";
					xml = xml
							.replace("!VIDEO!",
									temp);
				} else {
					xml = xml.replace("!VIDEO!", "");
				}
				buffer = xml.getBytes();
				os.write(buffer, 0, buffer.length);
				os.close();
			} catch (Exception e) {
				Montext.setText("Error !!" + e.getMessage());
				return;
			}
		}
	}

	@Override
	public void onClick(View v) {
		saveXML();
		Montext.setText("Install ok");
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
		((Go)getActivity()).save();
	}
}
