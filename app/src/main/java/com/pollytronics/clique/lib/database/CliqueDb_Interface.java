package com.pollytronics.clique.lib.database;

import com.pollytronics.clique.lib.base.Blip;
import com.pollytronics.clique.lib.base.Contact;

import java.util.Collection;

/**
 * Using the singleton design pattern with a interface is a bit annoying as the static getInstance method
 * cannot be put in the interface being static (for now).
 *
 * Therefore retrieving an instance of the interface would go something like
 * (CliqueDb_Interface) CliqueDb_SQLite.getInstance();
 * Some base classes provide convenience for this through getCliqueDb()
 */
public interface CliqueDb_Interface {

    // Contacts --------------------

    Collection<Contact> getAllContacts();

    void removeContact(Contact contact);

    void removeContactById(long id);

    void addContact(Contact contact);

    void updateContact(Contact contact);

    Contact getSelfContact();

    Contact getContactById(Long id);

    void updateSelfContact(Contact newSelfContact);

    // Blips -----------------------

    Blip getLastBlip(Contact contact);

    void addSelfBlip(Blip blip);

    Blip getLastSelfBlip();

    void addBlip(Blip blip, Contact contact);
}
