package org.acme.schooltimetabling.rest;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

import org.acme.schooltimetabling.domain.Timeslot;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;

@QuarkusTest
public class TimeslotResourceTest {

    @Test
    public void getAll() {
        List<Timeslot> timeslotList = given()
                .when().get("/timeslots")
                .then()
                .statusCode(200)
                .extract().body().jsonPath().getList(".", Timeslot.class);
        assertFalse(timeslotList.isEmpty());
        Timeslot firstTimeslot = timeslotList.get(0);
        assertEquals(1, firstTimeslot.getDayOfWeek()); // Monday
        assertEquals(0, firstTimeslot.getSlot()); // First slot
    }

    @Test
    void addAndRemove() {
        Timeslot timeslot = given()
                .when()
                .contentType(ContentType.JSON)
                .body(new Timeslot(null, 0, 4)) // Sunday, fifth slot
                .post("/timeslots")
                .then()
                .statusCode(201)
                .extract().as(Timeslot.class);

        given()
                .when()
                .delete("/timeslots/{id}", timeslot.getId())
                .then()
                .statusCode(204);
    }

}
