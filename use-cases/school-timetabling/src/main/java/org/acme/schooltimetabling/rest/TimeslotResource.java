package org.acme.schooltimetabling.rest;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

import org.acme.schooltimetabling.domain.Timeslot;
import org.acme.schooltimetabling.persistence.TimeslotRepository;

@Path("/timeslots")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TimeslotResource {
    
    private final TimeslotRepository timeslotRepository;

    public TimeslotResource(TimeslotRepository timeslotRepository) {
        this.timeslotRepository = timeslotRepository;
    }

    @POST
    public Timeslot add(Timeslot timeslot) {
        timeslotRepository.persist(timeslot);
        return timeslot;
    }

    @DELETE
    @Path("{id}")
    public void delete(@PathParam("id") Long id) {
        timeslotRepository.deleteById(id);
    }
}
