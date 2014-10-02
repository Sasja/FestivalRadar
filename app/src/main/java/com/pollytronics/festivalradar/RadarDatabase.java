package com.pollytronics.festivalradar;

import android.content.Context;
import android.os.SystemClock;

import com.pollytronics.festivalradar.lib.RadarBlip;
import com.pollytronics.festivalradar.lib.RadarContact;

import java.util.Collection;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

/**
 * Created by pollywog on 9/22/14.
 *
 * This is a Mock Class
 *
 * This class should provide methods to store and retrieve data on...
 * A) contacts and metadata
 * B) location data
 * it is used by and has an interface for the RadarService and for RadarActivities
 */
public class RadarDatabase implements RadarDatabase_Interface4RadarService, RadarDatabase_Interface4RadarActivity {

    private static final String TAG="RadarDatabase";
    private static RadarDatabase instance=null;

    private Set<RadarContact> allContacts = new HashSet<RadarContact>();
    private RadarContact selfContact;

    private RadarDatabase(Context context){
        /**
         * temporary stuff to generate some random blips for testing
         */
        final double LAT = 51.072478;
        final double LON = 3.709913;
        final double LATRANGE = 0.003;
        final double LONRANGE = 0.002;
        class VanbeverBlip extends RadarBlip{
            VanbeverBlip(){
                super();
                Random rnd = new Random();
                setLatitude(LAT+LATRANGE*(rnd.nextDouble()-0.5));
                setLongitude(LON+LONRANGE*(rnd.nextDouble()-0.5));
                setTime(SystemClock.currentThreadTimeMillis());
            }
        }
        // some random contacts for testing
        addContact((new RadarContact()).setName("Dieter").addBlip(new VanbeverBlip()));            // some dummy contacts
        addContact((new RadarContact()).setName("Seb").addBlip(new VanbeverBlip()));
        addContact((new RadarContact()).setName("Merel").addBlip(new VanbeverBlip()));
        addContact((new RadarContact()).setName("Alex").addBlip(new VanbeverBlip()));
        addContact((new RadarContact()).setName("Nancy de Pancy").addBlip(new VanbeverBlip()));
        addContact((new RadarContact()).setName("Sigfried Bracke").addBlip(new VanbeverBlip()));

        selfContact = (new RadarContact()).setName("self").addBlip(new VanbeverBlip());
    }

    public static RadarDatabase getInstance(Context context){
        if(instance==null){
            instance = new RadarDatabase(context);
        }
        return instance;
    }

    //-------------------------------

    /**
     * return an Iterable of contacts
     */
    @Override
    public Collection<RadarContact> getAllContacts() {
        Collection<RadarContact> clone = new HashSet<RadarContact>();
        for(RadarContact contact : allContacts){
            clone.add(new RadarContact(contact));
        }
        return clone;
    }

    @Override
    public void removeContact(RadarContact contact) {
        Collection<RadarContact> toRemove = new HashSet<RadarContact>();
        for(RadarContact c:allContacts){
            if(c.isSame(contact)) {
                toRemove.add(c);
            }
        }
        allContacts.removeAll(toRemove);
    }

    @Override
    public void updateContact(RadarContact contact) {
        removeContact(contact);
        allContacts.add(new RadarContact(contact));
    }

    @Override
    public void addContact(RadarContact contact) {
        long id;
        boolean ok;
        do {
            ok = true;
            id = (new Random()).nextLong();
            for(RadarContact c:allContacts){
                if(id==c.getID()) ok = false;
            }
        } while(!ok);
        allContacts.add(new RadarContact(contact).setID(id));
    }

    @Override
    public RadarContact getSelfContact() {
        return new RadarContact(selfContact);
    }

    @Override
    public void updateSelfContact(RadarContact newSelfContact) {
        selfContact = new RadarContact(newSelfContact);
    }


}
