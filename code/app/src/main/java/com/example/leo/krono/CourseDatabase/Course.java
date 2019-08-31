package com.example.leo.krono.CourseDatabase;

/**
 * Created by guptaji on 15/10/17.
 */
// class that models a course (used in the search for courses activity)
public class Course {
    private String code;
    private String name;
    private String venue;
    private String slots;
    public Course(String name,String code,String venue, String slots){
        super();
        this.name=name;
        this.code=code;
        this.venue=venue;
        this.slots=slots;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getVenue() {
        return venue;
    }

    String getSlots() {
        return slots;
    }
}
