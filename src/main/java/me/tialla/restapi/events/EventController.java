package me.tialla.restapi.events;

import me.tialla.restapi.common.ErrorsResource;
import org.modelmapper.ModelMapper;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.validation.Valid;
import java.net.URI;
import java.util.Arrays;
import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@Controller
@RequestMapping(value = "/api/events", produces = MediaTypes.HAL_JSON_VALUE)
public class EventController {

    private final EventRepository eventRepository;

    private final ModelMapper modelMapper;

    private final EventValidator eventValidator;

    public EventController(EventRepository eventRepository, ModelMapper modelMapper, EventValidator eventValidator) {
        this.eventRepository = eventRepository;
        this.modelMapper = modelMapper;
        this.eventValidator = eventValidator;
    }

    @PostMapping
    public ResponseEntity createEvent(@RequestBody @Valid EventDto eventDto, Errors errors){
        if(errors.hasErrors()){
            return badRequest(errors);
        }

        eventValidator.validate(eventDto, errors);
        if(errors.hasErrors()){
            return badRequest(errors);
        }

        Event event = modelMapper.map(eventDto, Event.class); //eventDto에 있는것을 Event.class타입의 인스턴스로 맵핑
        event.update();
        Event newEvent = this.eventRepository.save(event);

        WebMvcLinkBuilder selfLinkBuilder = linkTo(EventController.class);
        URI createdUri = selfLinkBuilder.toUri();

//        List<Link> links = Arrays.asList(
//                selfLinkBuilder.slash(newEvent.getId()).withSelfRel(),
//                selfLinkBuilder.withRel("query-events"),
//                selfLinkBuilder.slash(newEvent.getId()).withRel("update-event"),
//                Link.of("/docs/index.html#resources-events-create").withRel("profile")
//        );
//        EntityModel eventResource = EntityModel.of(newEvent, links);

        EntityModel eventResource = EventResource.modelof(newEvent);
        eventResource.add(selfLinkBuilder.withRel("query-events"));
        eventResource.add(selfLinkBuilder.slash(newEvent.getId()).withRel("update-event"));
        eventResource.add(Link.of("/docs/index.html#resources-events-create").withRel("profile"));
        return ResponseEntity.created(createdUri).body(eventResource);
    }


    private ResponseEntity badRequest(Errors errors) {
        return ResponseEntity.badRequest().body(ErrorsResource.modelOf(errors));
        //return ResponseEntity.badRequest().body(errors);
    }
}
