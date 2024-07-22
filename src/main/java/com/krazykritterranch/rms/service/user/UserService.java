package com.krazykritterranch.rms.service.user;

import com.krazykritterranch.rms.model.user.Role;
import com.krazykritterranch.rms.model.user.User;
import com.krazykritterranch.rms.repositories.user.RoleRepository;
import com.krazykritterranch.rms.repositories.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    public List<User> getAllCustomers(){
        return userRepository.findAll();
    }

    public User findById(Long id){
        return userRepository.findById(id).get();
    }

    public User findByLogin(String userName){
        //TODO: REMOVE THIS BEFORE DOING ANYTHING REALLY STUPID
        if (userName.equals("SUPERADMIN")){
            User adminUser = new User();
            adminUser.setActive(true);
            adminUser.setFirstName("SUPER");
            adminUser.setLastName("USER");
            adminUser.setUserName("SUPERUSER");
            Role superRole = new Role();
            superRole.setRoleName("SUPERADMIN");
            adminUser.getRoles().add(superRole);
            adminUser.setPassWord(passwordEncoder.encode("ABC123"));
            return adminUser;
        }
        return userRepository.findByUserName(userName);
    }


    public  List<User> searchByPhone(String partialPhone){
        return userRepository.findByPhonesFullNumberContaining(partialPhone);
    }

    public User findByPhone(String phoneNumber){
        return userRepository.findByPhonesFullNumber(phoneNumber);
    }

    public User saveCustomer(User user){
        return userRepository.save(user);
    }

}
