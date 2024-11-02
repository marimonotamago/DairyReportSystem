package com.techacademy.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.techacademy.constants.ErrorKinds;
import com.techacademy.constants.ErrorMessage;
import com.techacademy.entity.Employee;
import com.techacademy.entity.Reports;
import com.techacademy.entity.Reports;
import com.techacademy.entity.Reports;
import com.techacademy.service.ReportsService;
import com.techacademy.service.UserDetail;

@Controller
@RequestMapping("reports")
public class ReportsController {

    private final ReportsService reportsService;

    @Autowired
    public ReportsController(ReportsService reportsService) { //勝手にインスタンス化してくれる
        this.reportsService = reportsService;
    }

    // 日報一覧画面
    @GetMapping
    public String list(Model model) { // メソッドの引数にModelクラスのインスタンス。Modelクラスはテンプレートにデータを受け渡すために利用するクラス

        model.addAttribute("listSize", reportsService.findAll().size()); //size()は行数 addAttributeメソッドを使ってテンプレートに渡す値を設定。第一引数がデータの名前で、第二引数がデータの値
        model.addAttribute("reportslist", reportsService.findAll());//serviceにいっている

        return "reports/list";//reportsフォルダのreportsファイルを指定している
    }

    // 日報詳細画面
    @GetMapping(value = "/{id}/")
    public String detail(@PathVariable Integer id, Model model) {

        model.addAttribute("report", reportsService.findById(id));
        return "reports/detail";
    }


    //新規登録画面
    @GetMapping(value="/add")
    public String create(@ModelAttribute Reports reports, Model model, @AuthenticationPrincipal UserDetail userDetail) {
        // ユーザー名をモデルに追加
        model.addAttribute("employeeName", userDetail.getEmployee().getName());

        return "reports/new";
    }

    //日報新規登録処理
    @PostMapping(value = "/add") // 「POSTメソッド」を受け取る関数.htmlの「送信」ボタンをクリックすると、フォームの内容がPOSTメソッドとしてサーバーに送信され、addメソッドが実行
    public String add(@Validated Reports reports, BindingResult res, Model model, @AuthenticationPrincipal UserDetail userDetail) { //userdetailはユーザー情報を参照
        //入力チェック
        if (res.hasErrors()) {//エラーありは新規登録画面に日報情報を入力するためのビューを表示するためにcreateメソッドを呼び出し戻る
            return create(reports, model, userDetail);
        }
        reports.setEmployee(userDetail.getEmployee());

        // 論理削除を行った従業員番号を指定すると例外となるためtry~catchで対応
        // (findByIdでは削除フラグがTRUEのデータが取得出来ないため)
        try {
            ErrorKinds result = reportsService.save(reports, userDetail);

            if (ErrorMessage.contains(result)) {
                model.addAttribute(ErrorMessage.getErrorName(result), ErrorMessage.getErrorValue(result));
                return create(reports,model,userDetail);
            }

        } catch (DataIntegrityViolationException e) {
            model.addAttribute(ErrorMessage.getErrorName(ErrorKinds.DUPLICATE_EXCEPTION_ERROR),
                    ErrorMessage.getErrorValue(ErrorKinds.DUPLICATE_EXCEPTION_ERROR));
            return create(reports,model,userDetail);
        }
            //登録したら日報一覧画面に戻る
        return "redirect:/reports";
    }

    // 日報削除処理
    @PostMapping(value = "/{id}/delete")
    public String delete(@PathVariable Integer id, @AuthenticationPrincipal UserDetail userDetail, Model model) {

        ErrorKinds result = reportsService.delete(id);

        if (ErrorMessage.contains(result)) {
            model.addAttribute(ErrorMessage.getErrorName(result), ErrorMessage.getErrorValue(result));
            model.addAttribute("reports", reportsService.findById(id));
            return detail(id, model);
        }

        return "redirect:/reports";
    }

    //日報更新画面
    @GetMapping(value = "/{id}/update")
    public String getReport(@PathVariable("id") Integer id, @AuthenticationPrincipal UserDetail userdetail, Model model, @ModelAttribute Reports reports) {

        model.addAttribute("employeeName", userdetail.getEmployee().getName());

        if (id == null) {
            model.addAttribute("reports",reports);
            return "reports/update";

        } else {
            model.addAttribute("reports", reportsService.findById(id));
            return "reports/update";
        }
    }

    // 日報更新処理
    @PostMapping(value = "/{id}/update")
    public String update(@PathVariable("id") Integer id, @Validated Reports reports,BindingResult res,
            @AuthenticationPrincipal UserDetail userdetail,  Model model) {
        if (res.hasErrors()) {
            // エラーあり
            return getReport(null, userdetail, model, reports);
        }

        try {
            ErrorKinds result = reportsService.update(userdetail, reports, id);

            if (ErrorMessage.contains(result)) {
                model.addAttribute(ErrorMessage.getErrorName(result), ErrorMessage.getErrorValue(result));
                return getReport(null, userdetail, model, reports);
            }

        } catch (DataIntegrityViolationException e) {
            model.addAttribute(ErrorMessage.getErrorName(ErrorKinds.DUPLICATE_EXCEPTION_ERROR),
                    ErrorMessage.getErrorValue(ErrorKinds.DUPLICATE_EXCEPTION_ERROR));
            return getReport(null, userdetail, model, reports);
        }

        return "redirect:/reports";
    }


}