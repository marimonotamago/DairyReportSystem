package com.techacademy.repository;

import java.time.LocalDate;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.techacademy.entity.Employee;
import com.techacademy.entity.Reports;

public interface ReportsRepository extends JpaRepository<Reports, Integer> {
    List<Reports> findByEmployee(Employee employee);
    boolean existsByEmployeeAndReportDate(Employee employee, LocalDate reportDate);

}
