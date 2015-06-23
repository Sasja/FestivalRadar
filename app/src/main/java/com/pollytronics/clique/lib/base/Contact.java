package com.pollytronics.clique.lib.base;

/**
 * Class to contain all data on a Contact, including location history
 * there will also be a self instance to contain data on yourself
 * Each instance also holds an ID, this ID should be managed by the CliqueDb_SQLite class
 * methods that change the object should return themselves so calls can be chained.
 * Created by pollywog on 9/22/14.
 */
public class Contact implements Comparable<Contact>{

    private String name;
    private Blip lastBlip;
    private long ID;

    public Contact() {
        this.name = "no name";
        this.lastBlip = new Blip();
        this.ID = 0;
    }

    public Contact(Contact contact) {
        this.name = contact.name;
        this.ID = contact.ID;
        this.lastBlip = new Blip(contact.lastBlip);
    }

    public String getName() {
        return name;
    }

    public Contact setName(String name) {
        this.name = name;
        return this;
    }

    public Blip getLastBlip() { return lastBlip; }

    public Contact addBlip(Blip blip) {
        lastBlip = new Blip(blip);
        return this;
    }

    public boolean isSame(Contact contact){
        return (contact.ID == this.ID);
    }

    public long getID() {
        return ID;
    }

    public Contact setID(long ID) {
        this.ID = ID;
        return this;
    }

    @Override
    public int compareTo(Contact another) {
        int result = getName().toUpperCase().compareTo(another.getName().toUpperCase());
        if (result == 0) {
            result = ((Long) getID()).compareTo(another.getID());
        }
        return result;
    }

    @Override
    public String toString() {
        return name;
    }
}
