package org.acme.schooltimetabling.solver;


import org.acme.schooltimetabling.domain.Lesson;
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;
import org.optaplanner.core.api.score.stream.Constraint;
import org.optaplanner.core.api.score.stream.ConstraintFactory;
import org.optaplanner.core.api.score.stream.ConstraintProvider;
import org.optaplanner.core.api.score.stream.Joiners;

public class TimeTableConstraintProvider implements ConstraintProvider {

    @Override
    public Constraint[] defineConstraints(ConstraintFactory constraintFactory) {
        return new Constraint[] {
                // Hard constraints
                roomConflict(constraintFactory),
                teacherConflict(constraintFactory),
                studentGroupConflict(constraintFactory),
                courseContinuityConstraint(constraintFactory), // Add this new constraint
                // Soft constraints
                teacherRoomStability(constraintFactory),
                teacherTimeEfficiency(constraintFactory),
                studentGroupSubjectVariety(constraintFactory)
        };
    }

    Constraint roomConflict(ConstraintFactory constraintFactory) {
        // A room can accommodate at most one lesson at the same time.
        return constraintFactory
                // Select each pair of 2 different lessons ...
                .forEachUniquePair(Lesson.class,
                        // ... in the same timeslot ...
                        Joiners.equal(Lesson::getTimeslot),
                        // ... in the same room ...
                        Joiners.equal(Lesson::getRoom))
                // ... and penalize each pair with a hard weight.
                .penalize(HardSoftScore.ONE_HARD)
                .asConstraint("Room conflict");
    }

    Constraint teacherConflict(ConstraintFactory constraintFactory) {
        // A teacher can teach at most one lesson at the same time.
        return constraintFactory
                .forEachUniquePair(Lesson.class,
                        Joiners.equal(Lesson::getTimeslot),
                        Joiners.equal(Lesson::getTeacher))
                .penalize(HardSoftScore.ONE_HARD)
                .asConstraint("Teacher conflict");
    }

    Constraint studentGroupConflict(ConstraintFactory constraintFactory) {
        // A student can attend at most one lesson at the same time.
        return constraintFactory
                .forEachUniquePair(Lesson.class,
                        Joiners.equal(Lesson::getTimeslot),
                        Joiners.equal(Lesson::getStudentGroup))
                .penalize(HardSoftScore.ONE_HARD)
                .asConstraint("Student group conflict");
    }

    @SuppressWarnings("unchecked")
    Constraint courseContinuityConstraint(ConstraintFactory constraintFactory) {
        return constraintFactory
                .forEach(Lesson.class)
                .filter(lesson -> lesson.getCombinedSlotsPerDay() > 1) 
                .join(Lesson.class,
                        // Match lessons of same course
                        Joiners.equal(Lesson::getCourseId),
                        // Must be different lessons
                        Joiners.lessThan(Lesson::getId),
                        // Must be same day
                        Joiners.equal(lesson -> lesson.getTimeslot().getDayOfWeek()))
                .filter((lesson1, lesson2) -> {
                    if (lesson1.getTimeslot() == null || lesson2.getTimeslot() == null) {
                        return false;
                    }
                    
                    int slot1 = lesson1.getTimeslot().getSlot();
                    int slot2 = lesson2.getTimeslot().getSlot();
                    int combinedSlots = lesson1.getCombinedSlotsPerDay();
                    
                    // For lessons that should be combined:
                    // 1. They must be within the same day (already filtered by join)
                    // 2. They must be consecutive (difference between slots should be 1)
                    // 3. They must be part of the same block (distance from first slot should be < combinedSlots)
                    
                    // Check if slots are not consecutive
                    boolean areConsecutive = Math.abs(slot1 - slot2) == 1;
                    
                    // Check if slots are within the required group size
                    boolean withinGroupSize = Math.abs(slot1 - slot2) < combinedSlots;
                    
                    // Penalize if either condition is not met
                    return !areConsecutive || !withinGroupSize;
                })
                .penalize(HardSoftScore.ofHard(10)) // Increased penalty weight
                .asConstraint("Course continuity");
    }

    Constraint teacherRoomStability(ConstraintFactory constraintFactory) {
        // A teacher prefers to teach in a single room.
        return constraintFactory
                .forEachUniquePair(Lesson.class,
                        Joiners.equal(Lesson::getTeacher))
                .filter((lesson1, lesson2) -> lesson1.getRoom() != lesson2.getRoom())
                .penalize(HardSoftScore.ONE_SOFT)
                .asConstraint("Teacher room stability");
    }

    Constraint teacherTimeEfficiency(ConstraintFactory constraintFactory) {
        // A teacher prefers to teach sequential lessons and dislikes gaps between lessons.
        return constraintFactory
                .forEach(Lesson.class)
                .join(Lesson.class, Joiners.equal(Lesson::getTeacher),
                        Joiners.equal((lesson) -> lesson.getTimeslot().getDayOfWeek()))
                .filter((lesson1, lesson2) -> {
                    int slot1 = lesson1.getTimeslot().getSlot();
                    int slot2 = lesson2.getTimeslot().getSlot();
                    return Math.abs(slot1 - slot2) == 1; // Adjacent slots
                })
                .reward(HardSoftScore.ONE_SOFT)
                .asConstraint("Teacher time efficiency");
    }

    Constraint studentGroupSubjectVariety(ConstraintFactory constraintFactory) {
        // A student group dislikes sequential lessons on the same subject.
        return constraintFactory
                .forEach(Lesson.class)
                .join(Lesson.class,
                        Joiners.equal(Lesson::getSubject),
                        Joiners.equal(Lesson::getStudentGroup),
                        Joiners.equal((lesson) -> lesson.getTimeslot().getDayOfWeek()))
                .filter((lesson1, lesson2) -> {
                    int slot1 = lesson1.getTimeslot().getSlot();
                    int slot2 = lesson2.getTimeslot().getSlot();
                    return Math.abs(slot1 - slot2) == 1; // Adjacent slots
                })
                .penalize(HardSoftScore.ONE_SOFT)
                .asConstraint("Student group subject variety");
    }

}
