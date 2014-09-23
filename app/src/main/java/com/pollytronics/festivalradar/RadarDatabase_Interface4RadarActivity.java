package com.pollytronics.festivalradar;

import com.pollytronics.festivalradar.lib.RadarContact;

import java.util.Collection;
import java.util.List;

/**
 * Created by pollywog on 9/22/14.
 */
public interface RadarDatabase_Interface4RadarActivity {

    public Collection<RadarContact> getAllContacts();

    public void removeContact(RadarContact contact);

    public void updateContact(RadarContact contact);

    public void addContact(RadarContact contact);

    public RadarContact getSelfContact();

    public void updateSelfContact(RadarContact newSelfContact);

}
