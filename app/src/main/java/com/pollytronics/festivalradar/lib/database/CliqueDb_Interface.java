package com.pollytronics.festivalradar.lib.database;

import com.pollytronics.festivalradar.lib.base.Contact;

import java.util.Collection;

/**
 * Created by pollywog on 9/22/14.
 */
public interface CliqueDb_Interface {

    Collection<Contact> getAllContacts();

    @SuppressWarnings("unused")
    Collection<Long> getAllContactIds();

    void removeContact(Contact contact);

    void removeContactById(long id);

    @SuppressWarnings("unused")
    void updateContact(Contact contact);

    void addContact(Contact contact);

    Contact getSelfContact();

    @SuppressWarnings("unused")
    Contact getContact(Long id);

    void updateSelfContact(Contact newSelfContact);

}
