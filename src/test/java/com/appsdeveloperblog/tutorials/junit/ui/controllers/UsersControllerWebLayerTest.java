package com.appsdeveloperblog.tutorials.junit.ui.controllers;

import com.appsdeveloperblog.tutorials.junit.service.UsersService;
import com.appsdeveloperblog.tutorials.junit.service.UsersServiceImpl;
import com.appsdeveloperblog.tutorials.junit.shared.UserDto;
import com.appsdeveloperblog.tutorials.junit.ui.request.UserDetailsRequestModel;
import com.appsdeveloperblog.tutorials.junit.ui.response.UserRest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;


@WebMvcTest(controllers = UsersController.class,
        excludeAutoConfiguration = {SecurityAutoConfiguration.class})
@MockBean({UsersServiceImpl.class})///////////// !!!!!!!!!!!!!
public class UsersControllerWebLayerTest {
    @Autowired
    private MockMvc mockMvc;

//    @MockBean
    @Autowired
    UsersService usersService;

    private UserDetailsRequestModel userDetailsRequestModel;
    @BeforeEach
    void setup(){
        userDetailsRequestModel = new UserDetailsRequestModel();
        userDetailsRequestModel.setFirstName("Mike");
        userDetailsRequestModel.setLastName("Stoba");
        userDetailsRequestModel.setEmail("chigov@gmail.com");
        userDetailsRequestModel.setPassword("13850000");
        userDetailsRequestModel.setRepeatPassword("13850000");
    }

    @Test
    @DisplayName("User can be created")
    void testCreateUser_WhenValidUserDetailsProvided_returnCreatedUserDetails() throws Exception {
        //arrange
        //        since userService is mockBean -> createUser(userDto) must be initialized
        //        UserDto userDto = new UserDto();
        //        userDto.setFirstName("Mike");
        //        userDto.setLastName("Stoba");
        //        userDto.setEmail("chigov@gmail.com");
        //        userDto.setUserId(UUID.randomUUID().toString());
        // the same
        UserDto userDto = new ModelMapper().map(userDetailsRequestModel,UserDto.class);
        userDto.setUserId(UUID.randomUUID().toString());
        //now we can mock createUser method and return userDto
        when(usersService.createUser(any(UserDto.class))).thenReturn(userDto);

        //request
        RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(userDetailsRequestModel));

        //act //this line execute requestBuilder
        MvcResult mvcResult = mockMvc.perform(requestBuilder).andReturn();

        String responseBodyAsString = mvcResult.getResponse().getContentAsString();
        //converting response to java object
        UserRest createdUser = new ObjectMapper().readValue(responseBodyAsString, UserRest.class);

        //assert
        Assertions.assertEquals(userDetailsRequestModel.getFirstName(),createdUser.getFirstName(),
                "The returned user first name is most likely incorrect");
        Assertions.assertEquals(userDetailsRequestModel.getLastName(),createdUser.getLastName(),
                "The returned user last name is most likely incorrect");
        Assertions.assertEquals(userDetailsRequestModel.getEmail(), createdUser.getEmail(),
                "The returned user email is most likely incorrect");
        Assertions.assertFalse(createdUser.getUserId().isEmpty(),"UserId should not be empty");

    }

    @Test
    @DisplayName("First name is not present")
    void testCreateUser_FirstNameIsNotProvided_return400Status() throws Exception {
        //arrange
        userDetailsRequestModel.setFirstName("");

        RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(userDetailsRequestModel));

        //act
        MvcResult mvcResult = mockMvc.perform(requestBuilder).andReturn();

        //assert
        Assertions.assertEquals(HttpStatus.BAD_REQUEST.value(),mvcResult.getResponse().getStatus(),"Incorrect HTTP Status code returned");
    }
    @Test
    @DisplayName("First name cannot be shorter then 2 characters")
    void testCreateUser_whenFirstNameIsOnlyOneCharacter_return400Status() throws Exception {
        //arrange
//        UserDetailsRequestModel userDetailsRequestModel = new UserDetailsRequestModel();
        userDetailsRequestModel.setFirstName("g");

        RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(userDetailsRequestModel));

        //act
        MvcResult mvcResult = mockMvc.perform(requestBuilder).andReturn();
        //assert
        Assertions.assertEquals(HttpStatus.BAD_REQUEST.value(),mvcResult.getResponse().getStatus(),
                "Http status code is not set to 400");
    }
}
