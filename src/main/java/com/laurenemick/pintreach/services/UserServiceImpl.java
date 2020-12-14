package com.laurenemick.pintreach.services;

import com.laurenemick.pintreach.exceptions.ResourceNotFoundException;
import com.laurenemick.pintreach.models.Role;
import com.laurenemick.pintreach.models.User;
import com.laurenemick.pintreach.models.UserRoles;
import com.laurenemick.pintreach.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;

@Service("userService")
public class UserServiceImpl implements UserService
{
    @Autowired
    UserRepository userRepository;

    @Autowired
    RoleService roleService;

    @Override
    public List<User> listAll() {
        List<User> myList= new ArrayList<>();
        userRepository.findAll().iterator().forEachRemaining(myList::add);
        return myList;
    }

    @Override
    public User findByUsername(String name) {
        return userRepository.findByUsername(name);
    }

    @Override
    public User findById(long id) {
        return userRepository.findById(id).orElseThrow(()->new ResourceNotFoundException("User " + id + " not found"));
    }

    @Transactional
    @Override
    public User save(User user)
    {
        User newUser = new User();

        if (user.getUserid() != 0)
        {
            userRepository.findById(user.getUserid())
                .orElseThrow(() -> new ResourceNotFoundException("User id " + user.getUserid() + " not found!"));
            newUser.setUserid(user.getUserid());
        }

        newUser.setUsername(user.getUsername()
            .toLowerCase());
        newUser.setPasswordNoEncrypt(user.getPassword());
        newUser.setPrimaryemail(user.getPrimaryemail()
            .toLowerCase());
        newUser.setImageurl(user.getImageurl());

        newUser.getRoles()
            .clear();
        for (UserRoles ur : user.getRoles())
        {
            Role addRole = roleService.findByName("USER");
            newUser.getRoles()
                .add(new UserRoles(newUser, addRole));
        }

        return userRepository.save(newUser);
    }

    @Transactional
    @Override
    public User update(User user, long id) {
        User updateUser = userRepository.findById(id)
            .orElseThrow(()->new ResourceNotFoundException("User " + id + " not found"));

        if(user.getPassword() != null && user.getPassword() != "")
        {
            updateUser.setPassword(user.getPassword());
        }
        if(user.getPrimaryemail() != null )
        {
            updateUser.setPrimaryemail(user.getPrimaryemail());
        }

        if(user.getImageurl() != null)
        {
            updateUser.setImageurl(user.getImageurl());
        }

        return userRepository.save(updateUser);

    }

    @Transactional
    @Override
    public void delete(long id) {
        userRepository.deleteById(id);
    }
}