package com.pollytronics.clique.lib.database;

import com.pollytronics.clique.lib.base.Blip;
import com.pollytronics.clique.lib.base.Contact;

import java.util.List;

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

    List<Contact> getAllContacts() throws CliqueDb_SQLite.CliqueDbException;

    void removeContact(Contact contact) throws CliqueDb_SQLite.CliqueDbException;

    void removeContactById(long id) throws CliqueDb_SQLite.CliqueDbException;

    void addContact(Contact contact) throws CliqueDb_SQLite.CliqueDbException;

    Contact getSelfContact() throws CliqueDb_SQLite.CliqueDbException;

    Contact getContactById(Long id) throws CliqueDb_SQLite.CliqueDbException;

    void updateSelfContact(Contact newSelfContact) throws CliqueDb_SQLite.CliqueDbException;

    // Blips -----------------------

    Blip getLastBlip(Contact contact) throws CliqueDb_SQLite.CliqueDbException;

    void addSelfBlip(Blip blip) throws CliqueDb_SQLite.CliqueDbException;

    Blip getLastSelfBlip() throws CliqueDb_SQLite.CliqueDbException;

    void addBlip(Blip blip, Contact contact) throws CliqueDb_SQLite.CliqueDbException;
}
