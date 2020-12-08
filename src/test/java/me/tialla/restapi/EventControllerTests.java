package me.tialla.restapi;

import com.fasterxml.jackson.databind.ObjectMapper;
import me.tialla.restapi.common.TestDescription;
import me.tialla.restapi.events.Event;
import me.tialla.restapi.events.EventDto;
import me.tialla.restapi.events.EventStatus;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class EventControllerTests {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    @TestDescription("정상적으로 이벤트를 생성하는 테스트")
    public void createEvent() throws Exception{

        EventDto event = EventDto.builder()
                .name("Spring")
                .description("Rest API Development with Spring")
                .beginEnrollmentDateTime(LocalDateTime.of(2020,12,7,15,6))
                .closeEnrollmentDateTime(LocalDateTime.of(2020,12,8,11,6))
                .beginEventDateTime(LocalDateTime.of(2020,12,7,15,6))
                .endEventDateTime(LocalDateTime.of(2020,12,8,16,6))
                .basePrice(100)
                .maxPrice(200)
                .limitOfEnrollment(100)
                .location("감남역 D2 스타일 팩토리")
                .build();

        mockMvc.perform(post("/api/events/")
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaTypes.HAL_JSON)
                    .content(objectMapper.writeValueAsString(event)))
                .andDo(print())
                .andExpect(status().isCreated()) //201
                .andExpect(jsonPath("id").exists())
                .andExpect(header().exists(HttpHeaders.LOCATION))
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaTypes.HAL_JSON_VALUE))
                .andExpect(jsonPath("free").value(false))
                .andExpect(jsonPath("offline").value(true))
                .andExpect(jsonPath("eventStatus").value(EventStatus.DRAFT.name()))
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.query-events").exists())
                .andExpect(jsonPath("_links.update-event").exists())



        ;

    }

    @Test
    @TestDescription("입력 받을 수 없는 값을 사용한 경우에 에러가 발생하는 테스트")
    public void createEvent_Bad_Request() throws Exception{

        Event event = Event.builder()
                .id(100)

                .name("Spring")
                .description("Rest API Development with Spring")
                .beginEnrollmentDateTime(LocalDateTime.of(2020,12,7,15,6))
                .closeEnrollmentDateTime(LocalDateTime.of(2020,12,8,11,6))
                .beginEventDateTime(LocalDateTime.of(2020,12,7,15,1))
                .endEventDateTime(LocalDateTime.of(2020,12,7,15,1))
                .basePrice(100)
                .maxPrice(200)
                .limitOfEnrollment(100)
                .location("감남역 D2 스타일 팩토리")

                .free(true)
                .offline(false)
                .build();

        mockMvc.perform(post("/api/events/")
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaTypes.HAL_JSON)
                    .content(objectMapper.writeValueAsString(event)))
                .andDo(print())
                .andExpect(status().isBadRequest()) //400

        ;

    }

    @Test
    @TestDescription("입력값이 비어있는 경우에 에러가 발생하는 테스트")
    public void createEvent_Bad_Request_Empty_Input() throws Exception {
        EventDto eventDto = EventDto.builder().build();

        this.mockMvc.perform(post("/api/events/")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(this.objectMapper.writeValueAsString(eventDto)))
                .andExpect(status().isBadRequest())

        ;
    }

    @Test
    @TestDescription("입력 값이 잘못된 경우에 에러가 발생하는 테스트")
    public void createEvent_Bad_Request_Wrong_Input() throws Exception {
        EventDto eventDto = EventDto.builder()
                .name("Spring")
                .description("Rest API Development with Spring")
                .beginEnrollmentDateTime(LocalDateTime.of(2020,12,10,15,6))
                .closeEnrollmentDateTime(LocalDateTime.of(2020,12,8,11,6))
                .beginEventDateTime(LocalDateTime.of(2020,12,7,15,1))
                .endEventDateTime(LocalDateTime.of(2020,12,7,16,1))
                .basePrice(10000)
                .maxPrice(200)
                .limitOfEnrollment(100)
                .location("감남역 D2 스타일 팩토리")
                .build();

        this.mockMvc.perform(post("/api/events/")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(this.objectMapper.writeValueAsString(eventDto)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$[0].objectName").exists())
                .andExpect(jsonPath("$[0].defaultMessage").exists())
                .andExpect(jsonPath("$[0].code").exists())

        ;
    }

}
