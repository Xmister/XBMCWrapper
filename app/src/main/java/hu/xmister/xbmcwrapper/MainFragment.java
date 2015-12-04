package hu.xmister.xbmcwrapper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.StringTokenizer;

import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.TextView;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.AssetManager;

/**
 * The Main Page of the GUI. Also handles the xml editing.
 */
public class MainFragment extends Fragment implements OnClickListener {

	int fragVal;
	static MainFragment frag = null;
	TextView Montext;
	Button MonBt;
	private final String XBMC_DIRS[]={"/Android/data/org.xbmc.xbmc/files/.xbmc", "/Android/data/org.xbmc.kodi/files/.xbmc", "/Android/data/org.xbmc.kodi/files/.kodi", "/Android/data/org.kodi.kodi/files/.kodi"};
	private int XBMC_DIR=0;

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

	/**
	 * Called by the system when the we are visible. Finds the Kodi config directory, and updates the main text.
	 * @param savedInstanceState
	 */
	@Override
	public void onViewStateRestored(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onViewStateRestored(savedInstanceState);
		Montext = (TextView) getActivity().findViewById(R.id.status);
		MonBt = (Button) getActivity().findViewById(R.id.button1);
		MonBt.setOnClickListener(this);
		
		File XbmcC = null;
		
		for (int i=0; i<XBMC_DIRS.length; i++) {
			XbmcC = new File(Environment.getExternalStorageDirectory()
					.getPath() + XBMC_DIRS[i]+"/userdata");
			if (XbmcC.isDirectory()) {
				XBMC_DIR=i;
				break;
			}
		}
		if (!XbmcC.isDirectory()) {
			Montext.setText("XBMC/Kodi not found! Please install Kodi from www.kodi.tv, if you want to use this app!");
			MonBt.setClickable(false);
		} else {
			MonBt.setClickable(true);
			File PlayerC = new File(
					Environment.getExternalStorageDirectory().getPath()
							+ XBMC_DIRS[XBMC_DIR]+"/userdata/playercorefactory.xml");
			if (PlayerC.exists())
				Montext.setText("XML Successfully installed! Now you can make changes in the app.\nPlease fill in your Samba(Windows share) username and password, and choose your favorite player!\nAfter the changes, you should exit Kodi, and restart it to take effect!.\nThe default settings will handle HD videos and let android choose the player.");
			else
				Montext.setText("Please click the button if you want this app to handle Kodi videos.");
		}
		SharedPreferences sharedPreferences = getActivity().getSharedPreferences("default", 0);
		((CheckBox) getActivity().findViewById(R.id.ch_update)).setChecked(sharedPreferences
				.getBoolean("ch_update",
						((CheckBox) getActivity().findViewById(R.id.ch_update)).isChecked()));
		if (sharedPreferences.getInt("theme",0) == 0) ((RadioButton) getActivity().findViewById(R.id.rb_white)).setChecked(true);
		else ((RadioButton) getActivity().findViewById(R.id.rb_black)).setChecked(true);

		switch (sharedPreferences.getInt("charset",0)) {
			case 0:
				((RadioButton) getActivity().findViewById(R.id.rb_utf8)).setChecked(true);
				break;
			case 1:
				((RadioButton) getActivity().findViewById(R.id.rb_utf16)).setChecked(true);
				break;
			case 2:
				((RadioButton) getActivity().findViewById(R.id.rb_utf16le)).setChecked(true);
				break;
			case 3:
				((RadioButton) getActivity().findViewById(R.id.rb_utf16be)).setChecked(true);
				break;
		}
	}

	/**
	 * Edits the XML file to reflect the settings.
	 */
	private void saveXML() {
		if (getActivity() != null) {
			AssetManager assetManager = getActivity().getAssets();
			SharedPreferences sharedPreferences = getActivity()
					.getSharedPreferences("default", 0);
			InputStream is;
			long length = 0;
			try {
				//Built-in playercorefactory.xml
				is = assetManager.open("playercorefactory", AssetManager.ACCESS_BUFFER);
				length=is.available();
			} catch (IOException e1) {
				Montext.setText("IO error");
				return;
			}

			OutputStream os;
			try {
				String fileName=Environment.getExternalStorageDirectory().getPath()
						+ XBMC_DIRS[XBMC_DIR]+"/userdata/playercorefactory.xml";
				File f = new File(fileName);
				//If exists, make a backup
				if (f.exists()) {
					File fOld = new File(fileName+".old."+System.currentTimeMillis()/1000);
					f.renameTo(fOld);
				}
				os = new FileOutputStream(fileName);
				byte[] buffer = new byte[(int) length];
				is.read(buffer);
				is.close();
				String xml = new String(buffer);
				String temp = new String("<rule protocols=\"smb\" name=\"XBMCWrapper\" >\n");
				if (sharedPreferences.getInt("resolution", 1) != 0) {
					for (int i = sharedPreferences.getInt("resolution", 1); i <= 3; i++) {
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
						for (int i = sharedPreferences.getInt("resolution", 1); i <= 3; i++) {
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
					StringTokenizer st = new StringTokenizer(sharedPreferences.getString("excludepvr","1,2"),",");
					while (st.hasMoreTokens()) {
						String t=st.nextToken();
						temp += "\t<rule filename=\".*"+String.valueOf(Integer.parseInt(t)-1)+".pvr\" player=\"dvdplayer\"/>\n";
					}
					temp +="</rule>\n";
					xml = xml
							.replace("!PVR!",
									temp);
				} else {
					xml = xml.replace("!PVR!", "");
				}
				if (sharedPreferences.getBoolean("dav",true)) {
					temp = new String("<rule protocols=\"dav\" player=\"XBMCWrapper\" >\n");
					if (sharedPreferences.getInt("resolution", 1) != 0) {
						for (int i = sharedPreferences.getInt("resolution", 1); i <= 3; i++) {
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
							.replace("!DAV!",
									temp);
				} else {
					xml = xml.replace("!DAV!", "");
				}
				if (sharedPreferences.getBoolean("xmlvideo",true)) {
					temp = new String("");
					if (sharedPreferences.getInt("resolution", 1) != 0) {
						for (int i = sharedPreferences.getInt("resolution", 1); i <= 3; i++) {
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
						for (int i = sharedPreferences.getInt("resolution", 1); i <= 3; i++) {
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

	/**
	 * Install button's function. Saves the XML and updates the main text.
	 * @param v
	 */
	@Override
	public void onClick(View v) {
		saveXML();
		Montext.setText("XML Successfully installed! Now you can make changes in the app.\nPlease fill in your Samba(Windows share) username and password, and choose your favorite player!\nAfter the changes, you should exit Kodi, and restart it to take effect!.\nThe default settings will handle HD videos and will let android choose your player every time.");
	}

	/**
	 * Saves the settings when called.
	 */
	public void save() {
		if (getActivity() != null) {
			try {
				SharedPreferences sharedPreferences = getActivity()
						.getSharedPreferences("default", 0);
				Editor editor = sharedPreferences.edit();
				editor.putBoolean("ch_update",
						((CheckBox) getActivity().findViewById(R.id.ch_update))
								.isChecked());
				editor.putInt("theme",
						(((RadioButton)getActivity().findViewById(R.id.rb_white)).isChecked() ? 0 : 1));
				if (((RadioButton)getActivity().findViewById(R.id.rb_utf8)).isChecked()) editor.putInt("charset",0);
				else if (((RadioButton)getActivity().findViewById(R.id.rb_utf16)).isChecked()) editor.putInt("charset",1);
				else if (((RadioButton)getActivity().findViewById(R.id.rb_utf16le)).isChecked()) editor.putInt("charset",2);
				else if (((RadioButton)getActivity().findViewById(R.id.rb_utf16be)).isChecked()) editor.putInt("charset",3);
				editor.commit();
				if (sharedPreferences
						.getBoolean("ch_update",
								((CheckBox) getActivity().findViewById(R.id.ch_update)).isChecked())) {
					
										File PlayerC = new File(
												Environment.getExternalStorageDirectory().getPath()
														+ XBMC_DIRS[XBMC_DIR]+"/userdata");
										if (PlayerC.isDirectory())
											saveXML();
				}
			} catch (Exception e) {}
		}
	}

	@Override
	public void onPause() {
		// TODO Auto-generated method stub
		((MainGUI)getActivity()).save();
		super.onPause();
	}
}
