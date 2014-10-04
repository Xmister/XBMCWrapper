package hu.xmister.xbmcwrapper;

import android.support.v4.app.Fragment;
import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

public class ListFragment extends Fragment {
  
  private OnItemSelectedListener listener;
  
  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.overview,
        container, false);
    Button button = (Button) view.findViewById(R.id.button1);
    button.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        updateDetail();
      }
    });
    return view;
  }

  public interface OnItemSelectedListener {
      public void onRssItemSelected(String link);
    }
  
  @Override
    public void onAttach(Activity activity) {
      super.onAttach(activity);
      if (activity instanceof OnItemSelectedListener) {
        listener = (OnItemSelectedListener) activity;
      } else {
        throw new ClassCastException(activity.toString()
            + " must implemenet MyListFragment.OnItemSelectedListener");
      }
    }
  
  
  // May also be triggered from the Activity
  public void updateDetail() {
    // create fake data
    String newTime = String.valueOf(System.currentTimeMillis());
    // Send data to Activity
    listener.onRssItemSelected(newTime);
  }
} 
