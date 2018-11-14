package com.rest.consumer.demo.controller;

import com.rest.consumer.demo.consumer.RestConsumer;
import com.rest.consumer.demo.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import java.util.*;


@Controller
public class ConsumerController {

    private final RestConsumer restConsumer;

    @Autowired
    public ConsumerController(RestConsumer restConsumer) {
        this.restConsumer = restConsumer;
    }

    @GetMapping("/users")
    public String getAllUsers(ModelMap modelMap){
        List<User> userList = restConsumer.getAllUsers();

        if (userList == null)
            return "denied";

        modelMap.addAttribute("userList", userList);
            return "users";
    }

    @PostMapping(value = "/users")
    public String addUser(@RequestParam String newUserName,
                          @RequestParam String newUserLogin,
                          @RequestParam String newUserPassword,
                          @RequestParam String roleParam,
                          ModelMap modelMap){

        List<User> userList;
        if (restConsumer.addUser(newUserName,newUserLogin,newUserPassword, roleParam)){
            userList = restConsumer.getAllUsers();
            modelMap.addAttribute("userList", userList);
            return "users";
        }
         return this.getAllUsers(modelMap);
    }

    @GetMapping("/user/get/{id}")
    public String getUserById(@PathVariable ("id") Long id, ModelMap modelMap){
        User user = restConsumer.getUserById(id);

        if (user == null)
            return "denied";

        User currentUser = restConsumer.getCurrentUser();
        String currentUserMockRole = restConsumer.getMockRoles(currentUser);

        String mockRoles = restConsumer.getMockRoles(user);
        user.setMockRole(mockRoles);

        modelMap.addAttribute("user", user);
        modelMap.addAttribute("currentUserMockRoles", currentUserMockRole);
        return "user";
    }

    @GetMapping("/user/delete/{id}")
    public String deleteUserById(@PathVariable("id") Long id, ModelMap modelMap){

        User u = restConsumer.getCurrentUser();
        restConsumer.deleteUserById(id);

        if (u.getRoles().stream().anyMatch(role -> role.getRoleDescription().equals("ADMIN"))){
            modelMap.addAttribute("users", restConsumer.getAllUsers());
            return "redirect:/users";
        }
        return "redirect:/login";
    }

    @PostMapping("/user/update/{id}")
    public String updateUser(@PathVariable("id") Long id,
                             @RequestParam String newUserName,
                             @RequestParam String newUserLogin,
                             @RequestParam String newUserPassword){

        User user = restConsumer.getUserById(id);

        String userName = (newUserName.equals("")) ? user.getName() : newUserName;
        String userLogin = (newUserLogin.equals("")) ? user.getLogin() : newUserLogin;
        String userPassword = (newUserPassword.equals("")) ? user.getPassword() : newUserPassword;

        user.setName(userName);
        user.setLogin(userLogin);
        user.setPassword(userPassword);

        restConsumer.updateUser(user);
        user = restConsumer.getCurrentUser();

        if (user.getRoles().stream().anyMatch(role -> role.getRoleDescription().equals("ADMIN")))
            return "redirect:/users";

        return "redirect:/user/get/".concat(String.valueOf(user.getId()));
    }

    @GetMapping(name = "/login")
    public String loginPage(){
        restConsumer.dropCurrentData();
        return "login";
    }

    @SuppressWarnings("unchecked")
    @PostMapping(name = "/login")
    public String authenticateUser(@RequestParam String login, @RequestParam String password){
        return restConsumer.authenticateUser(login,password);
    }

    @RequestMapping(value = "/logout", method = RequestMethod.GET)
    public String logout(){
        return "redirect:/login?logout";
    }
}
