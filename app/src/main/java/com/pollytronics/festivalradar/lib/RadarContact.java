package com.pollytronics.festivalradar.lib;

/**
 * Created by pollywog on 9/22/14.
 */
public class RadarContact {

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

    public RadarContact(RadarContact contact, long id) {
        this(contact);
        this.ID = id;
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
        if(lastBlip==null || blip.after(lastBlip)){
            lastBlip = new RadarBlip(blip);
        }
        return this;
    }

    public boolean isSame(RadarContact contact){
        return (contact.ID == this.ID);
    }

    public long getID() {
        return ID;
    }
}
