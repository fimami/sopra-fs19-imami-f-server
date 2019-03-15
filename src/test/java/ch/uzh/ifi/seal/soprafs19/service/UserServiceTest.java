package ch.uzh.ifi.seal.soprafs19.service;

import ch.uzh.ifi.seal.soprafs19.Application;
import ch.uzh.ifi.seal.soprafs19.constant.UserStatus;
import ch.uzh.ifi.seal.soprafs19.controller.DuplicateException;
import ch.uzh.ifi.seal.soprafs19.controller.NonexistentUserException;
import ch.uzh.ifi.seal.soprafs19.entity.User;
import ch.uzh.ifi.seal.soprafs19.repository.UserRepository;
import ch.uzh.ifi.seal.soprafs19.service.UserService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.test.web.servlet.MockMvc;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Test class for the UserResource REST resource.
 *
 * @see UserService
 */
@WebAppConfiguration
@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@SpringBootTest(classes= Application.class)
public class UserServiceTest {


    @Qualifier("userRepository")
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserService userService;

    @Test
    public void createUser() {
        userRepository.deleteAll(userRepository.findAll());
        Assert.assertNull(userRepository.findByUsername("testUsername"));

        User testUser = new User();
        testUser.setUsername("testUsername");
        testUser.setPassword("testPassword");
        User createdUser = userService.createUser(testUser);

        Assert.assertNotNull(createdUser.getToken());
        Assert.assertEquals(createdUser.getStatus(),UserStatus.OFFLINE);
        Assert.assertEquals(createdUser, userRepository.findByToken(createdUser.getToken()));

    }
    @Test (expected = DuplicateException.class)
    public void createUserSameUsername() {
        userRepository.deleteAll(userRepository.findAll());
        Assert.assertNull(userRepository.findByUsername("testUsername"));

        User testUser = new User();
        testUser.setUsername("testUsername");
        testUser.setPassword("testPassword");
        User createdUser = userService.createUser(testUser);
        User createdUser2 = userService.createUser(testUser);
    }
    @Test
    public void editUser() {
        userRepository.deleteAll(userRepository.findAll());
        User testUser = new User();
        testUser.setUsername("Name");
        testUser.setPassword("Password");
        User createdUser = userService.createUser(testUser);
        long id =createdUser.getId();
        userService.updateUser(createdUser);
        Assert.assertEquals(userRepository.findById(id).getBirthday(), "01.01.2000");
        Assert.assertEquals(userRepository.findById(id).getUsername(), "newName");

    }
    @Test(expected = DuplicateException.class)
    public void editUserSameUsername() {
        userRepository.deleteAll(userRepository.findAll());
        User testUser = new User();
        testUser.setUsername("Name");
        testUser.setPassword("Password");
        User createdUser = userService.createUser(testUser);
        User testUser2 = new User();
        testUser2.setUsername("Name");
        testUser2.setPassword("Password2");
        User createdUser2 = userService.createUser(testUser);
        userService.updateUser(createdUser);
    }
    @Test
    public void logInUser() {
        userRepository.deleteAll(userRepository.findAll());
        User testUser = new User();
        testUser.setUsername("newUsername");
        testUser.setPassword("newPassword");
        User createdUser = userService.createUser(testUser);
        Assert.assertEquals(userRepository.findById(createdUser.getId()).get().getStatus(), UserStatus.OFFLINE);
        userService.checkUser(testUser);
        Assert.assertEquals(userRepository.findById(createdUser.getId()).get().getStatus(), UserStatus.ONLINE);

    }
    @Test(expected = NonexistentUserException.class)
    public void logInUserWrongPassword() {
        userRepository.deleteAll(userRepository.findAll());
        User testUser = new User();
        testUser.setUsername("newUsername");
        testUser.setPassword("newPassword");
        User createdUser = userService.createUser(testUser);
        User testUser2 = new User();
        testUser2.setUsername("newName");
        testUser2.setPassword("newPassword2");
        userService.checkUser(testUser2);
    }
    @Test
    public void logOutUser() {
        userRepository.deleteAll(userRepository.findAll());
        User testUser = new User();
        testUser.setUsername("newName");
        testUser.setPassword("newPassword");
        User createdUser = userService.createUser(testUser);

        userService.checkUser(testUser);
        userService.logoutUser(createdUser);
        Assert.assertEquals(createdUser.getStatus(), UserStatus.OFFLINE);
    }

    @Test
    public void testInvalidToken() throws Exception {
        this.mockMvc.perform(get("/users").header("Access-Token","invalid-token")).andExpect(status().is4xxClientError());
    }

}
