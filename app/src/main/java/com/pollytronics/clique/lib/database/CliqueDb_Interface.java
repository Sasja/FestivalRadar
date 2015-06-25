package com.pollytronics.clique.lib.database;

import com.pollytronics.clique.lib.base.Blip;
import com.pollytronics.clique.lib.base.Contact;

import java.util.Collection;

/**
 * Created by pollywog on 9/22/14.
 */
public interface CliqueDb_Interface {

    Collection<Contact> getAllContacts();

    void removeContact(Contact contact);

    void removeContactById(long id);

    void updateContact(Contact contact);

    void addContact(Contact contact);

    Contact getSelfContact();

    Contact getContactById(Long id);

    void updateSelfContact(Contact newSelfContact);

    Blip getLastBlip(Contact contact);

    void addSelfBlip(Blip blip);

    Blip getLastSelfBlip();

    void addBlip(Blip blip, Contact contact);

    Contact insertRandomSelfContact();
}
