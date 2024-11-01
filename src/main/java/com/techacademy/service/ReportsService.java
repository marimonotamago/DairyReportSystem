package com.techacademy.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional; //今回初見

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.techacademy.constants.ErrorKinds;
import com.techacademy.entity.Employee;
import com.techacademy.entity.Reports;
import com.techacademy.repository.ReportsRepository;

@Service
public class ReportsService {

    private final ReportsRepository reportsRepository;

    @Autowired
    public ReportsService(ReportsRepository reportsRepository) {
        this.reportsRepository = reportsRepository;
    }

    // 日報一覧表示処理
    public List<Reports> findAll() {
        return reportsRepository.findAll();
    }

   //権限なし表示処理
    public List<Reports> filterRole(UserDetail userdetail) {
        List<Reports> allReports = reportsRepository.findAll();
        List<Reports> filterReports = new ArrayList<Reports>();

        if(userdetail.getEmployee().getRole().getValue().equals("一般")) {
            for(Reports rep : allReports) {
                if(rep.getEmployee().getName().equals(userdetail.getEmployee().getName())) {
                    filterReports.add(rep);
                }
            }
            return filterReports;
        }
        return allReports;
    }

    // 1件を検索
    public Reports findById(Integer id) {
        // findByIdで検索
        Optional<Reports> option = reportsRepository.findById(id);//idで探せばわかる　既にデータがあるものに対して使う
        // 取得できなかった場合はnullを返す
        Reports reports = option.orElse(null); //所見のoption　カリキュラムにはない
        return reports;
    }

    //日報新規登録処理
    @Transactional
    public ErrorKinds save(Reports reports, UserDetail userDetail) {

        if (reportsRepository.existsByEmployeeAndReportDate(userDetail.getEmployee(), reports.getReportDate())) {
            return ErrorKinds.DATECHECK_ERROR;
        }

        reports.setDeleteFlg(false);

        LocalDateTime now = LocalDateTime.now();
        reports.setCreatedAt(now);
        reports.setUpdatedAt(now);
        reports.setEmployee(userDetail.getEmployee());

        reportsRepository.save(reports);
        return ErrorKinds.SUCCESS;

   }

    // 日報削除
    @Transactional
    public ErrorKinds delete(Integer id) {

        Reports reports = findById(id);
        LocalDateTime now = LocalDateTime.now();
        reports.setUpdatedAt(now);
        reports.setDeleteFlg(true);

        return ErrorKinds.SUCCESS;
    }

    //日報更新
    @Transactional
    public ErrorKinds update(UserDetail userdetail, Reports report, Integer id) {

        List<Reports> reportList = reportsRepository.findByEmployee(userdetail.getEmployee());
        Reports beforeReport = reportsRepository.findById(id).get();

        if(reportList != null && beforeReport.getEmployee().getCode().equals(userdetail.getEmployee().getCode()) && !beforeReport.getReportDate().equals(report.getReportDate())) {
            for(Reports rep:reportList) {
                if(rep.getReportDate().equals(report.getReportDate())) {
                    return ErrorKinds.DATECHECK_ERROR;
                }
            }
        }

        Reports updateReport = findById(report.getId());
        report.setDeleteFlg(false);
        updateReport.setTitle(report.getTitle());
        updateReport.setReportDate(report.getReportDate());
        updateReport.setContent(report.getContent());
        LocalDateTime now = LocalDateTime.now();

        updateReport.setCreatedAt(reportsRepository.findById(report.getId()).get().getCreatedAt());
        updateReport.setUpdatedAt(now);

        reportsRepository.save(updateReport);
        return ErrorKinds.SUCCESS;
    }


}
