package com.senior.cyber.sftps.web.validator;

import com.senior.cyber.sftps.dao.entity.User;
import com.senior.cyber.sftps.web.repository.UserRepository;
import com.senior.cyber.frmk.common.base.WicketFactory;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidator;
import org.apache.wicket.validation.ValidationError;
import org.springframework.context.ApplicationContext;

import java.util.Optional;

public class UserEmailAddressValidator implements IValidator<String> {

    @Override
    public void validate(IValidatable<String> validatable) {
        String emailAddress = validatable.getValue();
        if (emailAddress != null && !"".equals(emailAddress)) {
            ApplicationContext context = WicketFactory.getApplicationContext();
            UserRepository userRepository = context.getBean(UserRepository.class);
            Optional<User> optionalUser = userRepository.findByEmailAddress(emailAddress);
            optionalUser.ifPresent(user -> validatable.error(new ValidationError(emailAddress + " is not available")));
        }
    }

}
