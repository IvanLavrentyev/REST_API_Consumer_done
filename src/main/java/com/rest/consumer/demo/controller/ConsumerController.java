package com.rest.consumer.demo.controller;

import com.rest.consumer.demo.consumer.RestConsumer;
import com.rest.consumer.demo.model.Role;
import com.rest.consumer.demo.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


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
        modelMap.addAttribute("userList", userList);
        return "users";
    }

    @PostMapping(value = "/users")
    public String addUser(@RequestParam String newUserName,
                          @RequestParam String newUserLogin,
                          @RequestParam String newUserPassword,
                          @RequestParam String roleParam,
                          ModelMap modelMap){

        List<User> userList = null;

        if (restConsumer.addUser(newUserName,newUserLogin,newUserPassword, roleParam)){
            userList = restConsumer.getAllUsers();
            modelMap.addAttribute("userList", userList);
            return "users";
        }
         return this.getAllUsers(modelMap);
    }

    @GetMapping("/users/get/{id}")
    public String getUserById(@PathVariable ("id") Long id, ModelMap modelMap){
        User user = restConsumer.getUserById(id);

        String mockRoles = user.getRoles().
                stream().map(Role::getRoleDescription).
                collect(Collectors.joining(" "));

        user.setMockRole(mockRoles);

        modelMap.addAttribute("user", user);
        return "user";
    }

    @GetMapping("/users/delete/{id}")
    public String deleteUserById(@PathVariable("id") Long id, ModelMap modelMap){
        restConsumer.deleteUserById(id);
        modelMap.addAttribute("users", restConsumer.getAllUsers());
        return "redirect:/users";
    }

    @PostMapping("/users/update/{id}")
    public String updateUser(@PathVariable("id") Long id,
                             @RequestParam String newUserName,
                             @RequestParam String newUserLogin,
                             @RequestParam String newUserPassword){

        User user = restConsumer.getUserById(id);

        String userName = (newUserName.isEmpty()) ? user.getName() : newUserName;
        String userLogin = (newUserLogin.isEmpty()) ? user.getLogin() : newUserLogin;
        String userPassword = (newUserName.isEmpty()) ? user.getPassword() : newUserPassword;

        user.setName(userName);
        user.setLogin(userLogin);
        user.setPassword(userPassword);

        restConsumer.updateUser(user);
        return "redirect:/users";
    }

    @GetMapping(name = "/login")
    public String loginPage(){
        return "login";
    }

    @PostMapping(name = "/login")
    public String authenticateUser(@RequestParam String login, @RequestParam String password){

        Optional<User> optionalUser = restConsumer.getUserByParams(login);

        if (!optionalUser.isPresent())
            return "authorization";

        User u = optionalUser.get();

        if (u.getPassword().equals(password))
            return (u.getRoles().stream().
                    map(role -> role.getRoleDescription().equals("ADMIN")).
                    findAny().isPresent()) ? "redirect:/users" : "redirect:/users/get/".concat(String.valueOf(u.getId()));
        return "redirect:/login";
    }
}
