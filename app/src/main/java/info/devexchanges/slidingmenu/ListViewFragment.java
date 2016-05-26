package info.devexchanges.slidingmenu;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class ListViewFragment extends Fragment {
    private ListView listView;
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_listview, container, false);
    
        String[] listViewItems = new String[] {"Android", "iOS", "Windows Phone", "Blackberry", "Ubuntu Phone", "Sailfish", "Symbian"};
        listView = (ListView) view.findViewById(R.id.fragment_listview);
        listView.setAdapter(new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, listViewItems));
        
        return view;
    }
}
