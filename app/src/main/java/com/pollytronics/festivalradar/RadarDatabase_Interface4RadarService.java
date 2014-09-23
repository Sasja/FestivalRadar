package com.pollytronics.festivalradar;

import com.pollytronics.festivalradar.lib.RadarContact;

import java.util.Collection;

/**
 * Created by pollywog on 9/22/14.
 */
public interface RadarDatabase_Interface4RadarService {

    public Collection<RadarContact> getAllContacts();

    public void updateContact(RadarContact contact);

    public RadarContact getSelfContact();

    public void updateSelfContact(RadarContact newSelfContact);
}
