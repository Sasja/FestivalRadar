package com.pollytronics.festivalradar;

import com.pollytronics.festivalradar.lib.RadarContact;

import java.util.Collection;

/**
 * Created by pollywog on 9/22/14.
 */
public interface RadarDatabase_Interface4RadarService {

    Collection<RadarContact> getAllContacts();

    Collection<Long> getAllContactIds();

    void updateContact(RadarContact contact);

    RadarContact getSelfContact();

    void updateSelfContact(RadarContact newSelfContact);

    RadarContact getContact(Long id);
}
