package cmu.cconfs;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.github.florent37.materialviewpager.MaterialViewPager;
import com.github.florent37.materialviewpager.header.HeaderDesign;
import com.parse.ParseException;
import com.yalantis.contextmenu.lib.ContextMenuDialogFragment;
import com.yalantis.contextmenu.lib.MenuObject;
import com.yalantis.contextmenu.lib.MenuParams;
import com.yalantis.contextmenu.lib.interfaces.OnMenuItemClickListener;

import java.util.ArrayList;
import java.util.List;

import cmu.cconfs.fragment.RecyclerSelectedExpandableFragment;
import cmu.cconfs.parseUtils.helper.LoadingUtils;
import cmu.cconfs.utils.PreferencesManager;
import cmu.cconfs.utils.data.AbstractExpandableDataProvider;
import cmu.cconfs.utils.data.DataProvider;
import cmu.cconfs.utils.data.UnityDataProvider;

public class ScheduleActivity extends AppCompatActivity implements OnMenuItemClickListener {
    private final static String TAG = ScheduleActivity.class.getSimpleName();

    private MaterialViewPager mViewPager;
    private DrawerLayout mDrawer;
    private ActionBarDrawerToggle mDrawerToggle;
    private Toolbar toolbar;

    private FragmentManager fragmentManager;
    private DialogFragment mMenuDialogFragment;

    // navigate toobar
    PreferencesManager mPreferencesManager;
    private Toolbar mmToolbar;

    private final static int REQUEST_SIGN_IN = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agenda);
        fragmentManager = getSupportFragmentManager();
        mPreferencesManager = new PreferencesManager(this);

        initMenuFragment();

        setTitle("Schedule");

        mViewPager = (MaterialViewPager) findViewById(R.id.materialViewPager);
        toolbar = mViewPager.getToolbar();
        mDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        if (toolbar != null) {
            setSupportActionBar(toolbar);

            final ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.setDisplayHomeAsUpEnabled(true);
                actionBar.setDisplayShowHomeEnabled(true);
                actionBar.setDisplayShowTitleEnabled(true);
                actionBar.setDisplayUseLogoEnabled(false);
                actionBar.setHomeButtonEnabled(true);
            }
        }

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawer, 0, 0);
        mDrawer.setDrawerListener(mDrawerToggle);
        mViewPager.getViewPager().setAdapter(new FragmentStatePagerAdapter(fragmentManager) {
            @Override
            public Fragment getItem(int position) {
                RecyclerSelectedExpandableFragment instance;

                Bundle args = new Bundle();
                args.putInt("dateIndex", position);
                instance = RecyclerSelectedExpandableFragment.newInstance(position);
                instance.setArguments(args);

                return instance;

            }

            @Override
            public int getCount() {
                return 6;
            }

            @Override
            public CharSequence getPageTitle(int position) {
                switch (position % 6) {
                    case 0:
                        return "June 27th";
                    case 1:
                        return "June 28th";
                    case 2:
                        return "June 29th";
                    case 3:
                        return "June 30th";
                    case 4:
                        return "July 1st";
                    case 5:
                        return "July 2nd";
                }
                return "";
            }
        });

        mViewPager.setMaterialViewPagerListener(new MaterialViewPager.Listener() {
            @Override
            public HeaderDesign getHeaderDesign(int page) {
                switch (page) {
                    case 0:
                        return HeaderDesign.fromColorResAndUrl(
                                R.color.green,
                                "https://fs01.androidpit.info/a/63/0e/android-l-wallpapers-630ea6-h900.jpg");
                    case 1:
                        return HeaderDesign.fromColorResAndUrl(
                                R.color.blue,
                                "http://cdn1.tnwcdn.com/wp-content/blogs.dir/1/files/2014/06/wallpaper_51.jpg");
                    case 2:
                        return HeaderDesign.fromColorResAndUrl(
                                R.color.cyan,
                                "http://www.droid-life.com/wp-content/uploads/2014/10/lollipop-wallpapers10.jpg");
                    case 3:
                        return HeaderDesign.fromColorResAndUrl(
                                R.color.red,
                                "http://www.tothemobile.com/wp-content/uploads/2014/07/original.jpg");
                    default:
                        return HeaderDesign.fromColorResAndUrl(
                                R.color.blue,
                                "http://cdn1.tnwcdn.com/wp-content/blogs.dir/1/files/2014/06/wallpaper_51.jpg");
//                        return HeaderDesign.fromColorAndDrawable(
//                                Color.BLUE,
//                                getResources().getDrawable(R.drawable.golden_gate_small));
                }

                //execute others actions if needed (ex : modify your header logo)

                //  return null;
            }
        });

        mViewPager.getViewPager().setOffscreenPageLimit(mViewPager.getViewPager().getAdapter().getCount());
        mViewPager.getPagerTitleStrip().setViewPager(mViewPager.getViewPager());

        mViewPager.getViewPager().setCurrentItem(0);

        // TODO: delete after test
        printSelectedSessions();

        // set up the nav drawer
        mmToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mmToolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_menu_white_24dp);
        getSupportActionBar().setTitle("My Schedule");

        NavigationView mmNavigationView = (NavigationView) findViewById(R.id.nvView);
        mDrawerToggle = setupDrawerToggle();
        mDrawer.addDrawerListener(mDrawerToggle);

        setupNavigationView(mmNavigationView);
    }
    private ActionBarDrawerToggle setupDrawerToggle() {
        return new ActionBarDrawerToggle(this, mDrawer, mmToolbar, R.string.drawer_open,  R.string.drawer_close);
    }

    private void setupNavigationView(NavigationView navView) {
        navView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                selectDrawerItem(menuItem);
                return true;
            }
        });
    }
    private void selectDrawerItem(MenuItem menuItem) {
        Intent i = new Intent();
        switch (menuItem.getItemId()) {
            case R.id.nav_dash_board:
                Toast.makeText(getApplicationContext(), "dash board clicked", Toast.LENGTH_SHORT).show();
                i.setClass(this, HomeActivity.class);
                startActivity(i);
                break;
            case R.id.nav_my_schedule:
                Toast.makeText(getApplicationContext(), "my schedule clicked", Toast.LENGTH_SHORT).show();
                i.setClass(getApplicationContext(), ScheduleActivity.class);
                startActivity(i);
                break;
            case R.id.nav_my_profile:
                Toast.makeText(getApplicationContext(), "my profile clicked", Toast.LENGTH_SHORT).show();
                i = getLoginStatusIntent(ProfileActivity.class, LoginActivity.class);
                startActivityForResult(i, REQUEST_SIGN_IN);
                break;
            case R.id.nav_my_to_do_list:
                Toast.makeText(getApplicationContext(), "my todo list clicked", Toast.LENGTH_SHORT).show();
                i.setClass(this, TodoListActivity.class);
                startActivity(i);
                break;
            case R.id.nav_sync_data:
                syncScheduleData(this);
                break;
            case R.id.nav_log_in:
                i = getLoginStatusIntent(UserActivity.class, LoginActivity.class);
                startActivityForResult(i, REQUEST_SIGN_IN);
                break;
        }

        menuItem.setCheckable(true);
        setTitle(menuItem.getTitle());
        mDrawer.closeDrawers();
    }

    private Intent getLoginStatusIntent(Class loginTarget, Class notLoginTarget) {
        boolean loggedIn = mPreferencesManager.getBooleanPreference("LoggedIn",false);
        Toast .makeText(this, loggedIn + "", Toast.LENGTH_SHORT).show();
        Intent i = new Intent();
        if(!loggedIn) {
            i.setClass(getApplicationContext(), notLoginTarget);
        } else {
            i.setClass(getApplicationContext(), loginTarget);
        }
        return i;
    }

    private void syncScheduleData(final Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.MyDialogTheme);
        builder.setTitle("Sync Data");
        builder.setMessage("You sure want to reload the backend data?");
        builder.setPositiveButton(getString(android.R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                final ProgressDialog pd = new ProgressDialog(context);
                String st = "Syncing data...";
                pd.setMessage(st);
                pd.setCanceledOnTouchOutside(false);
                pd.show();
                new AsyncTask<Void, Void, Void>() {
                    @Override
                    protected Void doInBackground(Void... voids) {
                        try {
                            LoadingUtils.loadFromParse();
                            LoadingUtils.populateDataProvider();
                            LoadingUtils.populateRoomProvider();
                        } catch (ParseException e) {
                            Log.e(TAG, e.getMessage());
                        }
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Void aVoid) {
                        pd.dismiss();
                    }
                }.execute();
            }
        });
        builder.setNegativeButton(getString(android.R.string.cancel), null);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void initMenuFragment() {
        MenuParams menuParams = new MenuParams();
        menuParams.setActionBarSize((int) getResources().getDimension(R.dimen.tool_bar_height));
        menuParams.setMenuObjects(getMenuObjects());
        menuParams.setClosableOutside(false);
        menuParams.setFitsSystemWindow(true);
        menuParams.setClipToPadding(false);
        mMenuDialogFragment = ContextMenuDialogFragment.newInstance(menuParams);
//        mMenuDialogFragment.getDialog().getWindow().setGravity(Gravity.TOP| Gravity.LEFT);
    }

    private List<MenuObject> getMenuObjects() {
        // You can use any [resource, bitmap, drawable, color] as image:
        // item.setResource(...)
        // item.setBitmap(...)
        // item.setDrawable(...)
        // item.setColor(...)
        // You can set image ScaleType:
        // item.setScaleType(ScaleType.FIT_XY)
        // You can use any [resource, drawable, color] as background:
        // item.setBgResource(...)
        // item.setBgDrawable(...)
        // item.setBgColor(...)
        // You can use any [color] as text color:
        // item.setTextColor(...)
        // You can set any [color] as divider color:
        // item.setDividerColor(...)

        List<MenuObject> menuObjects = new ArrayList<>();

        MenuObject close = new MenuObject();
        close.setResource(R.drawable.icn_close);

        MenuObject send = new MenuObject("Send message");
        send.setResource(R.drawable.icn_1);

        MenuObject like = new MenuObject("Like profile");
        Bitmap b = BitmapFactory.decodeResource(getResources(), R.drawable.icn_2);
        like.setBitmap(b);

        MenuObject addFr = new MenuObject("Add to friends");
        BitmapDrawable bd = new BitmapDrawable(getResources(),
                BitmapFactory.decodeResource(getResources(), R.drawable.icn_3));
        addFr.setDrawable(bd);

        MenuObject addFav = new MenuObject("Add to favorites");
        addFav.setResource(R.drawable.icn_4);

        MenuObject block = new MenuObject("Block user");
        block.setResource(R.drawable.icn_5);

        menuObjects.add(close);
        menuObjects.add(send);
        menuObjects.add(like);
        menuObjects.add(addFr);
        menuObjects.add(addFav);
        menuObjects.add(block);

        return menuObjects;
    }


    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_agenda, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.context_menu:
                if (fragmentManager.findFragmentByTag(ContextMenuDialogFragment.TAG) == null) {
//                    mMenuDialogFragment.getDialog().getWindow().setGravity(Gravity.TOP| Gravity.LEFT);
                    mMenuDialogFragment.show(fragmentManager, ContextMenuDialogFragment.TAG);
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onMenuItemClick(View view, int i) {
        Toast.makeText(this, "Clicked on position: " + i, Toast.LENGTH_SHORT).show();
    }

    private void printSelectedSessions() {
        for (int i = 0; i < DataProvider.days; i++) {
            UnityDataProvider provider = CConfsApplication.getInstance().getUnityDataProvider(i);
            for (int j = 0; j < provider.getSelectedGroupCount(); j++) {
                AbstractExpandableDataProvider.BaseData groupData = provider.getSelectedGroupItem(j);
                for (int k = 0; k < provider.getSelectedChildCount(j); k++) {
                    StringBuffer sb = new StringBuffer(DataProvider.DATES[i].toString() + "\n");
                    sb.append("group: " + groupData.getText() + "\n");
                    UnityDataProvider.ConcreteChildData childData = provider.getSelectedChildItem(j, k);
                    sb.append("child: (" + childData.getFirstText() + ", " + childData.getSecondText() + ")\n");
                    Log.d(TAG, "Get a selected item:\n" + sb.toString());
                }
            }
        }

    }

}
