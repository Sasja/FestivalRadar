package com.pollytronics.clique.lib.database;

import com.pollytronics.clique.lib.base.Blip;
import com.pollytronics.clique.lib.base.Contact;

import java.util.List;

/**
 * Using the singleton design pattern with a interface is a bit annoying as the static getInstance method
 * cannot be put in the interface being static (for now).
 *
 * Therefore retrieving an instance of the interface would go something like
 * (CliqueDb_Interface) CliqueSQLite.getInstance();
 * Some base classes provide convenience for this through getCliqueDb()
 */
public interface CliqueDb_Interface {

    // Contacts --------------------

    List<Contact> getAllContacts() throws CliqueDbException;

    void removeContact(Contact contact) throws CliqueDbException;

    void removeContactById(long id) throws CliqueDbException;

    void addContact(Contact contact) throws CliqueDbException;

    Contact getSelfContact() throws CliqueDbException;

    Contact getContactById(Long id) throws CliqueDbException;

    void updateSelfContact(Contact newSelfContact) throws CliqueDbException;

    // Blips -----------------------

    Blip getLastBlip(Contact contact) throws CliqueDbException;

    void addSelfBlip(Blip blip) throws CliqueDbException;

    Blip getLastSelfBlip() throws CliqueDbException;

    void addBlip(Blip blip, Contact contact) throws CliqueDbException;
}
