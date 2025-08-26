package com.scheduleNetwork_version2.scheduleNetwork_version2.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "activity")
public class Activity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column( nullable = false)
    private LocalDate startDate;

    @Column
    private LocalDate endDate;

    @Column( nullable = false)
    private LocalTime startTime;

    @Column( nullable = false)
    private LocalTime endTime;

    @Column(length = 500)
    private String description;

    @Column
    private String location;


    //
    @ManyToOne
    @JoinColumn(name = "week_day_id" , nullable = false)
    @JsonBackReference
    private WeekDay weekDay;

    @ManyToOne
    @JoinColumn(name = "schedule_id" , nullable = true)
    @JsonBackReference
    private Schedule schedule;

    @ManyToOne
    @JoinColumn(name = "status_id" , nullable = false)
    @JsonBackReference
    private Status status;

    @ManyToOne
    @JoinColumn(name = "activity_type_id" , nullable = false)
    @JsonBackReference
    private ActivityType activityType;

    @ManyToOne
    @JoinColumn(name = "group_table_id" )
    @JsonBackReference
    private Group group;

    @ManyToOne
    @JoinColumn(name = "access_level_id" , nullable = false)
    @JsonBackReference
    private AccessLevel accessLevel;
}
