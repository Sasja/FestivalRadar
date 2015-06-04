package com.pollytronics.festivalradar;

import com.pollytronics.festivalradar.lib.RadarContact;

import java.util.Collection;

/**
 * Created by pollywog on 9/22/14.
 */
public interface RadarDatabase_Interface4RadarActivity {

    Collection<RadarContact> getAllContacts();

    void removeContact(RadarContact contact);

    void removeContactById(long id);

    @SuppressWarnings("unused")
    void updateContact(RadarContact contact);

    @SuppressWarnings("unused")
    void addContact(RadarContact contact);

    void addContactWithId(RadarContact contact);

    RadarContact getSelfContact();

    void updateSelfContact(RadarContact newSelfContact);

}
