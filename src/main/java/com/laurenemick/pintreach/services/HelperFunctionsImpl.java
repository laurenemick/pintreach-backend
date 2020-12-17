package com.laurenemick.pintreach.services;

import java.util.ArrayList;
import java.util.List;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;

import com.laurenemick.pintreach.exceptions.ResourceNotFoundException;
import com.laurenemick.pintreach.models.User;
import com.laurenemick.pintreach.models.ValidationError;
import com.laurenemick.pintreach.repositories.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service(value = "helperFunctions")
public class HelperFunctionsImpl implements HelperFunctions {
    private final UserRepository userRepository;

    public HelperFunctionsImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @SuppressWarnings("rawtypes")
    public List<ValidationError> getConstraintViolation(Throwable cause) {
        // Find any data violations that might be associated with the error and report them
        // data validations get wrapped in other exceptions as we work through the Spring
        // exception chain. Hence we have to search the entire Spring Exception Stack
        // to see if we have any violation constraints.
        while (
            (cause != null) && !(cause instanceof ConstraintViolationException)
        ) {
            cause = cause.getCause();
        }

        List<ValidationError> listVE = new ArrayList<>();

        // we know that cause either null or an instance of ConstraintViolationException
        if (cause != null) {
            ConstraintViolationException ex = (ConstraintViolationException) cause;
            for (ConstraintViolation cv : ex.getConstraintViolations()) {
                ValidationError newVe = new ValidationError();
                newVe.setCode(cv.getInvalidValue().toString());
                newVe.setMessage(cv.getMessage());
                listVE.add(newVe);
            }
        }
        return listVE;
    }

    @Override
    public boolean isAuthorizedToMakeChange(String username) {
        // Check to see if the user whose information being requested is the current user
        // Check to see if the requesting user is an admin
        // if either is true, return true
        // otherwise stop the process and throw an exception
        Authentication authentication = SecurityContextHolder
            .getContext()
            .getAuthentication();
        if (
            username.equalsIgnoreCase(authentication.getName().toLowerCase()) ||
                authentication
                    .getAuthorities()
                    .contains(new SimpleGrantedAuthority("ROLE_ADMIN"))
        ) {
            // this user can make this change
            return true;
        } else {
            // stop user is not authorized to make this change so stop the whole process and throw an exception
            throw new ResourceNotFoundException(
                authentication.getName() + " not authorized to make change"
            );
        }
    }

    @Override
    public User getCurrentUser() {
        {
            Authentication authentication = SecurityContextHolder
                .getContext()
                .getAuthentication();

            String name = authentication.getName();
            return userRepository.findByUsername(name);
        }
    }
}
