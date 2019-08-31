package com.example.leo.krono.mService;

/**
 * Created by guptaji on 10/10/17.
 */


import java.util.GregorianCalendar;
//class that models an event for use by the service
class CalendarEvent {

    private String nom;
    private GregorianCalendar startTime;
    private GregorianCalendar endTime;

    private CalendarEvent(String nom, GregorianCalendar startTime, GregorianCalendar endTime) {
        this.nom = nom;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    CalendarEvent(String nom, long startTime, long endTime) {
        this(nom, new GregorianCalendar(), new GregorianCalendar());
        this.startTime.setTimeInMillis(startTime);
        this.endTime.setTimeInMillis(endTime);
    }

    GregorianCalendar getStartTime() {
        return startTime;
    }

    GregorianCalendar getEndTime() {
        return endTime;
    }

    String getNom() {
        return nom;
    }
}
