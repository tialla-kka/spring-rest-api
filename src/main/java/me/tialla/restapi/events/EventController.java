package me.tialla.restapi.events;

import me.tialla.restapi.accounts.Account;
import me.tialla.restapi.accounts.AccountAdapter;
import me.tialla.restapi.accounts.CurrentUser;
import me.tialla.restapi.common.ErrorsResource;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.PagedModel;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Controller;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

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
    public ResponseEntity createEvent(@RequestBody @Valid EventDto eventDto,
                                      Errors errors,
                                      @CurrentUser Account currentUser){
        if(errors.hasErrors()){
            return badRequest(errors);
        }

        eventValidator.validate(eventDto, errors);
        if(errors.hasErrors()){
            return badRequest(errors);
        }

        Event event = modelMapper.map(eventDto, Event.class); //eventDto에 있는것을 Event.class타입의 인스턴스로 맵핑
        event.update();
        event.setManager(currentUser);
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

    @GetMapping
    public ResponseEntity queryEvents(Pageable pageable,
                                      PagedResourcesAssembler<Event> assembler,
                                      @CurrentUser Account account){
        Page<Event> page = this.eventRepository.findAll(pageable);
        var pageResource = assembler.toModel(page, EventResource::modelof);

        pageResource.add(Link.of("/docs/index.html#resources-events-list").withRel("profile"));

        if(account != null){
            pageResource.add(linkTo(EventController.class).withRel("create-event"));
        }
        return ResponseEntity.ok(pageResource);
    }

    @GetMapping("/{id}")
    public ResponseEntity getEvents(@PathVariable Integer id,
                                    @CurrentUser Account currentUser){
        Optional<Event> optionalEvent = this.eventRepository.findById(id);
        if(optionalEvent.isEmpty()){
            return ResponseEntity.notFound().build();
        }

        Event event = optionalEvent.get();
        EntityModel<Event> eventResource = EventResource.modelof(event);
        eventResource.add(Link.of("/docs/index.html#resources-events-get").withRel("profile"));
        if(event.getManager().equals(currentUser)){
            eventResource.add(linkTo(EventController.class).slash(event.getId()).withRel("update-event"));
        }
        return ResponseEntity.ok(eventResource);
    }

    @PutMapping("/{id}")
    public ResponseEntity updateEvent(@PathVariable Integer id,
                                     @RequestBody @Valid EventDto eventDto,
                                     Errors errors,
                                      @CurrentUser Account currentUser){

        Optional<Event> optionalEvent = this.eventRepository.findById(id);
        if(optionalEvent.isEmpty()){
            return ResponseEntity.notFound().build();
        }

        if(errors.hasErrors()){
            return badRequest(errors);
        }

        this.eventValidator.validate(eventDto, errors);
        if(errors.hasErrors()){
            return badRequest(errors);
        }

        Event existingEvent = optionalEvent.get();
        if(!existingEvent.getManager().equals(currentUser)){
            return new ResponseEntity(HttpStatus.UNAUTHORIZED); //인가되지 않았다.
        }

        this.modelMapper.map(eventDto, existingEvent); //eventDto 에있는 것을 existingEvent 로 덥어쓰기^^
        Event savedEvent = this.eventRepository.save(existingEvent);

        EntityModel<Event> eventResource = EventResource.modelof(savedEvent);
        eventResource.add(Link.of("/docs/index.html#resources-events-update").withRel("profile"));

        return ResponseEntity.ok(eventResource);
    }

    private ResponseEntity badRequest(Errors errors) {
        return ResponseEntity.badRequest().body(ErrorsResource.modelOf(errors));
        //return ResponseEntity.badRequest().body(errors);
    }
}
