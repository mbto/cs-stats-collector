package ru.csdm.stats.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import ru.csdm.stats.common.model.tables.pojos.ApiUser;
import ru.csdm.stats.dao.ApiUserDao;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Profile("default")
@Service
public class ApiUserService implements UserDetailsService {
    @Autowired
    private ApiUserDao apiUserDao;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        ApiUser apiUser = apiUserDao.findApiUserByUsername(username);

        if(Objects.isNull(apiUser))
            throw new UsernameNotFoundException("Username '" + username + "' not founded");

        List<String> roles = new ArrayList<>();

        if(apiUser.getManage())
            roles.add("manager");

        if(apiUser.getView())
            roles.add("viewer");

        return User.builder()
                .username(apiUser.getUsername())
                .password(apiUser.getPassword()) // https://www.browserling.com/tools/bcrypt
                .roles(roles.toArray(new String[0]))
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(!apiUser.getActive())
                .build();
    }
}