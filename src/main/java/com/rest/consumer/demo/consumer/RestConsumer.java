package com.rest.consumer.demo.consumer;

import com.google.gson.Gson;
import com.rest.consumer.demo.model.User;
import com.rest.consumer.demo.model.Role;
import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class RestConsumer {

    private final RestTemplate restTemplate;
    private static HttpHeaders initialHeaders = null;
    private static long currentUserId;

    private final String GET_ALL_URL = "http://localhost:8080/users";
    private final String GET_USER_BY_ID_URL = "http://localhost:8080/user/get/";
    private final String ADD_USER_URL = "http://localhost:8080/users/add";
    private final String DELETE_USER_URL = "http://localhost:8080/user/delete/";
    private final String UPDATE_USER_URL = "http://localhost:8080/user/update/";
    private final String AUTH_USER = "http://localhost:8080/auth";

    @Autowired
    public RestConsumer(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public void dropCurrentData(){
        initialHeaders = null;
    }

    @SuppressWarnings("unchecked")
    public List<User> getAllUsers(){

        if (initialHeaders == null)
            return null;

        initialHeaders = getHeaders(currentUserId);

            ResponseEntity<List> users = getResponseEntity(GET_ALL_URL, initialHeaders, List.class);
            List<LinkedHashMap<String, Object>> usersMap = (List<LinkedHashMap<String, Object>>)users.getBody();

            if (usersMap != null) {
                List<User> userList = new ArrayList<>();

                for (LinkedHashMap<String, Object> user : usersMap){

                    User u = new Gson().fromJson(String.valueOf(user), User.class);

                    String mockRoles = u.getRoles().stream().map(Role::getRoleDescription).
                            collect(Collectors.joining(" "));

                    u.setMockRole(mockRoles);
                    userList.add(u);
                }
                return userList;
            }
            return null;
    }

    public User getCurrentUser(){
        return getUserById(currentUserId);
    }

    public User getUserById(long id){
        ResponseEntity<User> responseEntity;
        HttpHeaders httpHeaders = getHeaders(id);

        Set<Role> roles = getRolesById(currentUserId);

        if (id == currentUserId || roles.stream().anyMatch(role -> role.getRoleDescription().equals("ADMIN"))){
            responseEntity = getResponseEntity(GET_USER_BY_ID_URL.concat(String.valueOf(id)),httpHeaders, User.class);
            return responseEntity.getBody();
        }
        return null;
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

    @SuppressWarnings("unchecked")
    public String authenticateUser(String login, String password){
        if (initialHeaders == null)
        initialHeaders = getHeaders(login,password);

        User user = getUserByLogin(login, password);

        if (user == null)
            return "redirect:/login";

        currentUserId = user.getId();
        if (user.getRoles().stream().anyMatch(role -> role.getRoleDescription().equals("ADMIN")))
            return "redirect:/users";

        return "redirect:/user/get/".concat(String.valueOf(currentUserId));
    }

    public String getMockRoles(User user){
        return  user.getRoles().stream().map(Role::getRoleDescription).
                collect(Collectors.joining(" "));
    }

    private HttpHeaders getHeaders(String userLogin, String userPassword){
        String credentials = userLogin.concat(":").concat(userPassword);
        String base64credentials = new String(Base64.encodeBase64(credentials.getBytes()));
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Authorization", "Basic ".concat(base64credentials));
        httpHeaders.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        return httpHeaders;
    }

    private HttpHeaders getHeaders(long id){
        RestTemplate restTemplate = new RestTemplate();
        User user = restTemplate.getForObject(GET_USER_BY_ID_URL.concat(String.valueOf(id)), User.class);
        return this.getHeaders(user.getLogin(), user.getPassword());
    }

    @SuppressWarnings("unchecked")
    private <T> ResponseEntity<T> getResponseEntity(String URL, HttpHeaders headers, Class T){
        HttpEntity<String> httpEntity = new HttpEntity<>(headers);
        RestTemplate restTemplate = new RestTemplate();

        return restTemplate.exchange(URL, HttpMethod.GET, httpEntity, T);
    }

    private User getUserByLogin(String login, String password){
        HttpHeaders httpHeaders = getHeaders(login, password);
        httpHeaders.add("Login", login);

        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        ResponseEntity<User> responseEntity;

        try {
            responseEntity = getResponseEntity(AUTH_USER, httpHeaders, User.class);
        }catch (HttpClientErrorException e){
            responseEntity = new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        if (responseEntity.getStatusCode() == HttpStatus.OK)
            return responseEntity.getBody();

        return null;
    }

    private Set<Role> getRolesById(long id){
        HttpHeaders httpHeaders = getHeaders(id);
        ResponseEntity<User> responseEntity = getResponseEntity(GET_USER_BY_ID_URL.concat(String.valueOf(id)),httpHeaders, User.class);

        return Objects.requireNonNull(responseEntity.getBody()).getRoles();
    }

}
