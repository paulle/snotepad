package info.aario.snotepad;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private SharedPreferences sharedPref;
    private SharedPreferences.Editor sharedPrefEditor;
    private FloatingActionButton fab;
    CoordinatorLayout coordinatorLayoutForSnackBar;
    ListFragment listFragment = new ListFragment();
    public Filer filer;
    public boolean editor_modified;

    public void setPath(String path) {
        sharedPrefEditor.putString("PATH", path);
        sharedPrefEditor.commit();
        listFragment.refresh();
    }

    public String getPath() {
        return sharedPref.getString("PATH", getExternalFilesDir(null).getAbsolutePath());
    }

    public void toast(String text) {
        Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
    }

    public void makeSnackBar(String text) {
        Snackbar.make(coordinatorLayoutForSnackBar, text, Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();
    }

    private void changeFragment(Fragment f, boolean allowBack) {
        FragmentManager fragmentManager = getSupportFragmentManager();

        // Prune the stack, so "back" always leads home.
        if (fragmentManager.getBackStackEntryCount() > 0) {
            onSupportNavigateUp();
        }

        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.fragment_container, f);
        if (allowBack) {
            transaction.addToBackStack(null);
            transaction.setTransition(FragmentTransaction.TRANSIT_NONE);
        }
        transaction.commit();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        coordinatorLayoutForSnackBar = (CoordinatorLayout) findViewById(R.id.co_ordinated_layout_main);
        sharedPref = getPreferences(Context.MODE_PRIVATE);
        sharedPrefEditor = sharedPref.edit();
        filer = new Filer(this);
        changeFragment(listFragment, false);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        fab = (FloatingActionButton) findViewById(R.id.fab);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    public void editFile(String filePath) {
        EditorFragment ef = new EditorFragment();
        changeFragment(ef, true);
        ef.open(filePath);
        editor_modified = false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_sort_by_name) {
            listFragment.sort(false);
            return true;
        }

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_sort_by_date) {
            listFragment.sort(true);
            return true;
        }

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            changeFragment(new SettingsFragment(), true);
            return true;
        }

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_refresh) {
            listFragment.refresh();
            return true;
        }

        if (id == R.id.action_about) {
            changeFragment(new AboutFragment(), true);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {

        int count = getSupportFragmentManager().getBackStackEntryCount();

        if (count == 0) {
            super.onBackPressed();
            //additional code
        } else if (editor_modified) {
            DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which) {
                        case DialogInterface.BUTTON_POSITIVE:
                            //Yes button clicked
                            fab.callOnClick();//Save document
                        case DialogInterface.BUTTON_NEGATIVE:
                            //No button clicked
                            getSupportFragmentManager().popBackStack();
                        case DialogInterface.BUTTON_NEUTRAL:
                            //Cancel button clicked
                            return;
                    }
                }
            };
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(getResources().getString(R.string.save_dialog_question))
                    .setPositiveButton(getResources().getString(R.string.yes), dialogClickListener)
                    .setNegativeButton(getResources().getString(R.string.no), dialogClickListener)
                    .setNeutralButton(getResources().getString(R.string.cancel), dialogClickListener)
                    .show();
        } else {
            getSupportFragmentManager().popBackStack();
        }
    }
}
