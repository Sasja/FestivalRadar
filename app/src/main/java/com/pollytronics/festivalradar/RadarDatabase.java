package com.pollytronics.festivalradar;

import com.pollytronics.festivalradar.lib.RadarBlip;
import com.pollytronics.festivalradar.lib.RadarContact;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * Created by pollywog on 9/22/14.
 * This class should provide methods to store and retrieve data on...
 * A) contacts and metadata
 * B) location data
 * it is used by and has an interface for the RadarService and for RadarActivities
 */
public class RadarDatabase implements RadarDatabase_Interface4RadarService, RadarDatabase_Interface4RadarActivity {

    private static RadarDatabase instance=null;

    private Set<RadarContact> allContacts = new HashSet<RadarContact>();
    private RadarContact selfContact;

    private RadarDatabase(){
        Random rand = new Random();
        addContact((new RadarContact()).setName("Jos").addBlip(new RadarBlip(rand.nextFloat()*200-100,rand.nextFloat()*200-100)));            // some dummy contacts
        addContact((new RadarContact()).setName("Korneel").addBlip(new RadarBlip(rand.nextFloat()*200-100,rand.nextFloat()*200-100)));
        addContact((new RadarContact()).setName("Merel").addBlip(new RadarBlip(rand.nextFloat()*200-100,rand.nextFloat()*200-100)));
        addContact((new RadarContact()).setName("Davy de Pavy").addBlip(new RadarBlip(rand.nextFloat()*200-100,rand.nextFloat()*200-100)));
        addContact((new RadarContact()).setName("Nancy de Pancy").addBlip(new RadarBlip(rand.nextFloat()*200-100,rand.nextFloat()*200-100)));
        addContact((new RadarContact()).setName("Sigfried Bracke").addBlip(new RadarBlip(rand.nextFloat()*200-100,rand.nextFloat()*200-100)));

        selfContact = (new RadarContact()).setName("self");
    }

    public static RadarDatabase getInstance(){
        if(instance==null){
            instance = new RadarDatabase();
        }
        return instance;
    }

    //-------------------------------

    /**
     * return an Iterable of contacts
     * @return
     */
    @Override
    public Collection<RadarContact> getAllContacts() {
        Collection<RadarContact> clone = new HashSet<RadarContact>();
        for(RadarContact contact : allContacts){
            clone.add(new RadarContact(contact));
        }
        return (Collection<RadarContact>) clone;
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
        addContact(contact);
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
        allContacts.add(new RadarContact(contact, id));
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
