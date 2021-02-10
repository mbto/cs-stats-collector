package ru.csdm.stats.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import ru.csdm.stats.common.model.collector.tables.pojos.Manager;
import ru.csdm.stats.dao.ManagerDao;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Profile("default")
@Service
public class ManagerService implements UserDetailsService {
    @Autowired
    private ManagerDao managerDao;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Manager manager = managerDao.findManagerUsername(username);

        if(Objects.isNull(manager))
            throw new UsernameNotFoundException("Username '" + username + "' not founded");

        List<String> roles = new ArrayList<>();

        if(manager.getManageHosting())
            roles.add("manager");

        roles.add("client");

        return User.builder()
                .username(manager.getUsername())
                .password(manager.getPassword()) // https://www.browserling.com/tools/bcrypt
                .roles(roles.toArray(new String[0]))
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(!manager.getActive())
                .build();
    }
}