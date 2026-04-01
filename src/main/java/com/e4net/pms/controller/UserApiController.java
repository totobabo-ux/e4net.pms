package com.e4net.pms.controller;

import com.e4net.pms.dto.UserDto;
import com.e4net.pms.entity.User;
import com.e4net.pms.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserApiController {

    private final UserRepository userRepository;

    /** 참여자 검색 팝업용 JSON API */
    @GetMapping("/search")
    public List<UserDto> search(@RequestParam(defaultValue = "") String name,
                                @RequestParam(defaultValue = "") String company) {
        return userRepository.searchByNameAndCompany(name, company)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    private UserDto toDto(User u) {
        return new UserDto(u.getId(), u.getEmployeeNo(), u.getName(),
                u.getCompany(), u.getDepartment(), u.getPosition(),
                u.getPhone(), u.getEmail());
    }
}
