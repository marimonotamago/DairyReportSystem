package com.techacademy.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.techacademy.service.ReportsService;

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
        model.addAttribute("reportslist", reportsService.findAll());

        return "reports/reports";
    }


    @GetMapping(value="/add")
    public String create() {
        //他のことはせず、引数も書かない
        return "reports/add";
    }
}
