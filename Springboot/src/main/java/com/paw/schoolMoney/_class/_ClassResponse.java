package com.paw.schoolMoney._class;

import com.paw.schoolMoney.child.Child;
import com.paw.schoolMoney.child.ChildResponse;
import com.paw.schoolMoney.user.User;
import com.paw.schoolMoney.user.UserDTO;
import com.paw.schoolMoney.user.UserResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class _ClassResponse {
    private int id;
    private String className;
    private UserResponse treasurer;
    private List<ChildResponse> children;
    private List<UserResponse> parents;
}