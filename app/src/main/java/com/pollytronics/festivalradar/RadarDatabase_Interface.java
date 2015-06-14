package com.pollytronics.festivalradar;

import com.pollytronics.festivalradar.lib.RadarContact;

import java.util.Collection;

/**
 * Created by pollywog on 9/22/14.
 */
public interface RadarDatabase_Interface {

    Collection<RadarContact> getAllContacts();

    @SuppressWarnings("unused")
    Collection<Long> getAllContactIds();

    void removeContact(RadarContact contact);

    void removeContactById(long id);

    @SuppressWarnings("unused")
    void updateContact(RadarContact contact);

    void addContactWithId(RadarContact contact);

    RadarContact getSelfContact();

    @SuppressWarnings("unused")
    RadarContact getContact(Long id);

    void updateSelfContact(RadarContact newSelfContact);

}
