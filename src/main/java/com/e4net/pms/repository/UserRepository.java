package com.e4net.pms.repository;

import com.e4net.pms.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {

    @Query("SELECT u FROM User u WHERE " +
           "(:name IS NULL OR :name = '' OR u.name LIKE %:name%) AND " +
           "(:company IS NULL OR :company = '' OR u.company LIKE %:company%)")
    List<User> searchByNameAndCompany(@Param("name") String name,
                                      @Param("company") String company);
}
