package com.smarturban.dto;

import com.smarturban.entity.Department;

public class DepartmentResponse {

    private Long id;
    private String name;
    private String code;

    public DepartmentResponse() {}

    public static DepartmentResponse fromEntity(Department department) {
        if (department == null) return null;
        DepartmentResponse dto = new DepartmentResponse();
        dto.setId(department.getId());
        dto.setName(department.getName());
        dto.setCode(department.getCode());
        return dto;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
