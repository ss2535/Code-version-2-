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
@Table(name = "time")
public class Time {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column( nullable = false)
    private LocalDate startDate;

//    @Column( nullable = false)
    private LocalDate endDate;

    @Column( nullable = false)
    private LocalTime startTime;

    @Column( nullable = false)
    private LocalTime endTime;


    @ManyToOne
    @JoinColumn(name = "user_table_id", nullable = false)
    @JsonBackReference
    private User user;

    @ManyToOne
    @JoinColumn(name = "group_table_id", nullable = false)
    @JsonBackReference
    private Group group;

    @ManyToOne
    @JoinColumn(name = "week_day_id", nullable = false)
    @JsonBackReference
    private WeekDay weekDay;

}
