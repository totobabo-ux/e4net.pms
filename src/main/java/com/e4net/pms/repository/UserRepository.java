package com.e4net.pms.repository;

import com.e4net.pms.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByEmployeeNo(String employeeNo);              // 사번 중복 체크
    java.util.Optional<User> findByEmployeeNo(String employeeNo); // 로그인 조회

    @Query("SELECT u FROM User u WHERE " +
           "(:name IS NULL OR :name = '' OR u.name LIKE %:name%) AND " +
           "(:company IS NULL OR :company = '' OR u.company LIKE %:company%)")
    List<User> searchByNameAndCompany(@Param("name") String name,
                                      @Param("company") String company);

    // 권한별 사용자 목록 (권한 관리)
    List<User> findByRoleOrderByNameAsc(String role);
    List<User> findByRoleIsNullOrderByNameAsc();

    // 관리자 사용자 목록 검색 (페이징)
    @Query("SELECT u FROM User u WHERE " +
           "(:employeeNo IS NULL OR :employeeNo = '' OR u.employeeNo LIKE %:employeeNo%) AND " +
           "(:name IS NULL OR :name = '' OR u.name LIKE %:name%) AND " +
           "(:company IS NULL OR :company = '' OR u.company LIKE %:company%)")
    Page<User> searchUsers(@Param("employeeNo") String employeeNo,
                           @Param("name") String name,
                           @Param("company") String company,
                           Pageable pageable);
}
