package me.tialla.restapi.events;

import me.tialla.restapi.index.IndexController;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

public class EventResource extends EntityModel<Event> {

    public static EntityModel<Event> modelof(Event event){

        EntityModel<Event> eventEntityModel = EntityModel.of(event);
        eventEntityModel.add(linkTo(EventController.class).slash(event.getId()).withSelfRel());
        return eventEntityModel;

    }
}