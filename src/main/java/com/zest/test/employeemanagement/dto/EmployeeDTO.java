package com.zest.test.employeemanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeDTO {

    private Long id;                    // null on create, populated on response

    private String name;
    private String email;

    private Integer departmentId;       // client sends ID
    private String departmentName;      // server sends name back in response

    private Integer positionId;         // client sends ID
    private String positionName;        // server sends title back in response

    private BigDecimal salary;
    private LocalDate dateOfJoining;
    private Boolean isActive;
}