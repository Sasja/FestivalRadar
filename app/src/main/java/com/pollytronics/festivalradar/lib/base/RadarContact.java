package com.pollytronics.festivalradar.lib.base;

/**
 * Class to contain all data on a Contact, including location history
 * there will also be a self instance to contain data on yourself
 * Each instance also holds an ID, this ID should be managed by the RadarDatabase_SQLite class
 * methods that change the object should return themselves so calls can be chained.
 * Created by pollywog on 9/22/14.
 */
public class RadarContact implements Comparable<RadarContact>{

    private String name;
    private RadarBlip lastBlip;
    private long ID;

    public RadarContact() {
        this.name = "no name";
        this.lastBlip = new RadarBlip();
        this.ID = 0;
    }

    public RadarContact(RadarContact contact) {
        this.name = contact.name;
        this.ID = contact.ID;
        this.lastBlip = new RadarBlip(contact.lastBlip);
    }

    public String getName() {
        return name;
    }

    public RadarContact setName(String name) {
        this.name = name;
        return this;
    }

    public RadarBlip getLastBlip() { return lastBlip; }

    public RadarContact addBlip(RadarBlip blip) {
        lastBlip = new RadarBlip(blip);
        return this;
    }

    public boolean isSame(RadarContact contact){
        return (contact.ID == this.ID);
    }

    public long getID() {
        return ID;
    }

    public RadarContact setID(long ID) {
        this.ID = ID;
        return this;
    }

    @Override
    public int compareTo(RadarContact another) {
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
