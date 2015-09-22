package com.pollytronics.clique;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.pollytronics.clique.lib.CliqueActivity_MyViewPagerAct;
import com.pollytronics.clique.lib.base.Contact;
import com.pollytronics.clique.lib.database.CliqueDbException;
import com.pollytronics.clique.lib.database.cliqueSQLite.local.DbContact;
import com.pollytronics.clique.lib.tools.MyAssortedTools;

/**
 * TODO: (syncing) Study https://developer.android.com/training/sync-adapters/index.html and consider implementing such a thing
 * TODO: (code) receiving database update notifications every three seconds when in contacts activity!?! why is that?
 */
public class MVP_Activity_Contacts extends CliqueActivity_MyViewPagerAct {
    @SuppressWarnings("unused")
    private static final String TAG = "ViewPagerAct_Contacts";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        enableHeartBeat(3000);
    }

    /**
     * Will regularly call notifyDatabaseUpdate on all fragments in order to keep em up to date
     */
    @Override
    protected void heartBeatCallBack() {
        notifyDatabaseUpdate();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.contacts, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_sync_contacts) {
            if (MyAssortedTools.isNetworkAvailable(this)) {
                Log.i(TAG, "syncing contact data");
                CliqueSyncer.getInstance(this).poke();
            } else {
                Log.i(TAG, "cannot sync contact data: no network");
                Toast toast = Toast.makeText(getApplicationContext(), getString(R.string.toast_no_network), Toast.LENGTH_SHORT);
                toast.show();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    /**
     * make sure the getF methods use the same order!!!
     */
    protected void loadMyFragments() {
        addFragment(Fragment_Contacts_Ping.class, getString(R.string.contacts_tab_ping));               // 0
        addFragment(Fragment_Contacts_MyContacts.class, getString(R.string.contacts_tab_mycontacts));   // 1
        addFragment(Fragment_Contacts_Remote.class, getString(R.string.contacts_tab_remote));           // 2
    }

//    //TODO: (code) what is this for again?
//    public Fragment_Contacts_Ping getF_Ping() {             return (Fragment_Contacts_Ping)         getFragmentByNr(0); }
//    public Fragment_Contacts_MyContacts getF_MyContacts() { return (Fragment_Contacts_MyContacts)   getFragmentByNr(1); }
//    public Fragment_Contacts_Remote getF_Remote() {         return (Fragment_Contacts_Remote)       getFragmentByNr(2); }

    public void confirmAndDeleteContact(Contact contact) {
        DialogFragment mDialog = new TemporaryDeleteDialog();
        Bundle arguments = new Bundle();
        arguments.putLong("id", contact.getGlobalId());
        mDialog.setArguments(arguments);
        mDialog.show(getSupportFragmentManager(), "DeleteContactDialog");
    }

    // TODO: (code) this does not belong here
    public static class TemporaryDeleteDialog extends DialogFragment {
        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return new AlertDialog.Builder(getActivity())
                    .setMessage(getString(R.string.dialog_delete_this_contact))
                    .setNegativeButton(getString(R.string.dialog_no), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Log.i(TAG, "denied, not doing anything");
                        }
                    })
                    .setPositiveButton(getString(R.string.dialog_yes), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Log.i(TAG, "deleting selected contact locally");
                            try {
                                DbContact.remove(getArguments().getLong("id"));
                                ((MVP_Activity_Contacts) getActivity()).notifyDatabaseUpdate(); //TODO: (code) this might be dangerous
                            } catch (CliqueDbException e) {
                                e.printStackTrace();
                            }
                        }
                    })
                    .create();
        }
    }
}
