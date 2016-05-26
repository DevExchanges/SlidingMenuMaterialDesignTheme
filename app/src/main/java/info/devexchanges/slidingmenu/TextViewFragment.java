package info.devexchanges.slidingmenu;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class TextViewFragment extends Fragment {
    TextView textView;
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_textview, null);
    
        Bundle b = getArguments();
        String text = b.getString("KEY_STRING");
        
        textView = (TextView) view.findViewById(R.id.fragment_textview_textview);
        textView.setText(text);
        
        return view;
    }
}
