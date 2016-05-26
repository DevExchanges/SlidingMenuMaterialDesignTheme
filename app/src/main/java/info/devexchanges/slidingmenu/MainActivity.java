package info.devexchanges.slidingmenu;

import android.annotation.SuppressLint;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    // The SlidingLayout which will hold both the sliding menu and our main content
    // Main content will holds our Fragment respectively
    SlidingLayout slidingLayout;

    // ListView menu
    private ListView listMenu;
    private String[] listMenuItems;

    private Toolbar toolbar;
    private TextView title; //page title
    private ImageView btMenu; // Menu button
    private Fragment currentFragment;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inflate the mainLayout
        setContentView(R.layout.activity_main);
        slidingLayout = (SlidingLayout) findViewById(R.id.sliding_layout);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        title = (TextView) findViewById(R.id.title);
        setSupportActionBar(toolbar);

        // Init menu
        listMenuItems = getResources().getStringArray(R.array.menu_items);
        listMenu = (ListView) findViewById(R.id.activity_main_menu_listview);
        listMenu.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, listMenuItems));
        listMenu.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                onMenuItemClick(parent, view, position, id);
            }

        });

        // handling menu button event
        btMenu = (ImageView) findViewById(R.id.menu_icon);
        btMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Show/hide the menu
                toggleMenu(v);
            }
        });

        // Replace fragment main when activity start
        FragmentManager fm = MainActivity.this.getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();

        MainFragment fragment = new MainFragment();
        ft.add(R.id.activity_main_content_fragment, fragment);
        ft.commit();

        currentFragment = fragment;
        title.setText("Sliding Menu like Facebook");
    }

    public void toggleMenu(View v) {
        slidingLayout.toggleMenu();
    }

    // Perform action when a menu item is clicked
    private void onMenuItemClick(AdapterView<?> parent, View view, int position, long id) {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        Fragment fragment;

        if (position == 0) {
            fragment = new MainFragment();
            title.setText("Main Screen");
        } else if (position == 1) {
            fragment = new ListViewFragment();
            title.setText("ListView Fragment");
        } else if (position == 2) {
            fragment = new TextViewFragment();
            Bundle args = new Bundle();
            args.putString("KEY_STRING", "This is a TextView in the Fragment");
            fragment.setArguments(args);
            title.setText("TextView Fragment");
        } else {
            fragment = new DummyFragment();
            title.setText("Blank Fragment");
        }

        if(!fragment.getClass().equals(currentFragment.getClass())) {
            // Replace current fragment by this new one
            ft.replace(R.id.activity_main_content_fragment, fragment);
            ft.commit();

            currentFragment = fragment;
        }

        // Hide menu anyway
        slidingLayout.toggleMenu();

    }

    @Override
    public void onBackPressed() {
        if (slidingLayout.isMenuShown()) {
            slidingLayout.toggleMenu();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        getSupportActionBar().setTitle("");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }
}
