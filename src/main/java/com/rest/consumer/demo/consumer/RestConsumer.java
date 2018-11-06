package com.rest.consumer.demo.consumer;

import com.rest.consumer.demo.model.User;
import com.rest.consumer.demo.model.Role;
import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.annotation.Role;
import org.springframework.http.*;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Service
public class RestConsumer {

    private final RestTemplate restTemplate;

    private final String GET_ALL_URL = "http://localhost:8080/users";
    private final String GET_USER_BY_ID_URL = "http://localhost:8080/users/get/";
    private final String ADD_USER_URL = "http://localhost:8080/users/add/";
    private final String DELETE_USER_URL = "http://localhost:8080/users/delete/";
    private final String UPDATE_USER_URL = "http://localhost:8080/users/update/";

    @Autowired
    public RestConsumer(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public List<User> getAllUsers(){
        List<User> users = Arrays.stream(Objects.requireNonNull(restTemplate.getForObject(GET_ALL_URL, User[].class))).
                collect(Collectors.toList());

        for (User user : users ){
           Set<Role> roles = user.getRoles();
           Set<String> s = roles.stream().
                   map(Role::getRoleDescription).
                   collect(Collectors.toSet());
            String string = s.stream().map(s1 -> s1.concat(" ")).collect(Collectors.joining(" "));
            user.setMockRole(string);
        }

        return users;
    }

    public User getUserById(long id){
        return restTemplate.getForObject(GET_USER_BY_ID_URL.concat(String.valueOf(id)), User.class);
    }

    public boolean addUser(String newUserName, String newUserLogin, String newUserPassword, String roleParam) {
        if (newUserName.equals("") || newUserLogin.equals("") || newUserPassword.equals(""))
            return false;

        User user = new User(newUserName, newUserLogin, newUserPassword);

        if (this.getAllUsers().contains(user))
            return false;

        Set<Role> roles = new HashSet<>();

        if (roleParam.equals("ADMIN"))
            roles.add(new Role(1, "ADMIN"));

        roles.add(new Role(2, "USER"));

        user.setRoles(roles);

        try {
            restTemplate.postForObject(ADD_USER_URL, user, User.class);
        } catch (Exception ignored) {}
        return true;
    }

    public void deleteUserById(long id){
        restTemplate.delete(DELETE_USER_URL.concat(String.valueOf(id)));
    }

    public void updateUser(User user){
        String url = UPDATE_USER_URL.concat(String.valueOf(user.getId()));
        restTemplate.put(url, user);
    }

    public Optional<User> getUserByParams(String login){ // Secured that login is unique
        List<User> users = this.getAllUsers();

        Optional<User> optionalUser = users.stream().
                filter(user -> user.getLogin().equals(login)).findAny();

        return optionalUser;
    }


}
