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
import com.techacademy.service.EmployeeService;
import com.techacademy.service.UserDetail;

@Controller // このクラスがHTTPリクエストを受けつけるクラス
@RequestMapping("employees")
public class EmployeeController {

    private final EmployeeService employeeService; //メンバー変数 EmployeeService型のEmployeeService

    @Autowired
    public EmployeeController(EmployeeService employeeService) { //勝手にインスタンス化してくれる
        this.employeeService = employeeService;
    }

    // 従業員一覧画面
    @GetMapping
    public String list(Model model) { // メソッドの引数にModelクラスのインスタンス。Modelクラスはテンプレートにデータを受け渡すために利用するクラス

        model.addAttribute("listSize", employeeService.findAll().size()); //size()は行数 addAttributeメソッドを使ってテンプレートに渡す値を設定。第一引数がデータの名前で、第二引数がデータの値
        model.addAttribute("employeeList", employeeService.findAll());

        return "employees/list";
    }

    // 従業員詳細画面
    @GetMapping(value = "/{code}/") // URL http://localhost:8080/（「/{code}/」のアドレス）に対する「GETメソッド」を受け取る関数
    public String detail(@PathVariable String code, Model model) {

        model.addAttribute("employee", employeeService.findByCode(code));
        return "employees/detail"; // Thymeleafを利用する場合、戻り値に合致するテンプレートファイルを検索して読み込み、レンダリングしてクライアントへ送信する仕組み
    }

    // 従業員新規登録画面
    @GetMapping(value = "/add")
    public String create(@ModelAttribute Employee employee) {

        return "employees/new";
    }

    // 従業員新規登録処理
    @PostMapping(value = "/add") // 「POST
                                    // メソッド」を受け取る関数.htmlの「送信」ボタンをクリックすると、フォームの内容がPOSTメソッドとしてサーバーに送信され、addメソッドが実行
    public String add(@Validated Employee employee, BindingResult res, Model model) {

        // パスワード空白チェック
        /*
         * エンティティ側の入力チェックでも実装は行えるが、更新の方でパスワードが空白でもチェックエラーを出さずに
         * 更新出来る仕様となっているため上記を考慮した場合に別でエラーメッセージを出す方法が簡単だと判断
         */
        if ("".equals(employee.getPassword())) {
            // パスワードが空白だった場合
            model.addAttribute(ErrorMessage.getErrorName(ErrorKinds.BLANK_ERROR),
                    ErrorMessage.getErrorValue(ErrorKinds.BLANK_ERROR));

            return create(employee); // エラーが発生した場合、従業員情報を入力するためのビューを表示するためにcreateメソッドを呼び出し

        }

        // 入力チェック
        if (res.hasErrors()) {
            return create(employee);
        }

        // 論理削除を行った従業員番号を指定すると例外となるためtry~catchで対応
        // (findByIdでは削除フラグがTRUEのデータが取得出来ないため)
        try {
            ErrorKinds result = employeeService.save(employee);

            if (ErrorMessage.contains(result)) {
                model.addAttribute(ErrorMessage.getErrorName(result), ErrorMessage.getErrorValue(result));
                return create(employee);
            }

        } catch (DataIntegrityViolationException e) {
            model.addAttribute(ErrorMessage.getErrorName(ErrorKinds.DUPLICATE_EXCEPTION_ERROR),
                    ErrorMessage.getErrorValue(ErrorKinds.DUPLICATE_EXCEPTION_ERROR));
            return create(employee);
        }

        return "redirect:/employees";
    }

    // 従業員更新画面
    @GetMapping(value = "/{code}/update")
    public String edit(@PathVariable("code") String code, Employee employee, Model model) { //Employee.javaの情報

        if (code != null) {
            model.addAttribute("employee", employeeService.findByCode(code)); // DBからもってきたい
            model.addAttribute("code", code);
        } else {
            model.addAttribute("employee", employee); // nullで飛ばされたときかどうかを判断 123行目のemployeeと同じ
            model.addAttribute("code", employee.getCode());
        }

        return "employees/update";
    }

    // 従業員更新処理
    @PostMapping(value = "{code}/update") // フォームから送信された更新データを処理.
    public String update(@Validated Employee employee, BindingResult res, Model model) {

        // 入力チェック
        if (res.hasErrors()) {
            return edit(null, employee, model);
        }


        ErrorKinds result = employeeService.update(employee); //Errokindsという型

        if (ErrorMessage.contains(result)) {
            model.addAttribute(ErrorMessage.getErrorName(result), ErrorMessage.getErrorValue(result));
            return edit(null, employee, model);
        }

        return "redirect:/employees"; // 従業員一覧画面へ遷移
    }

    // 従業員削除処理
    @PostMapping(value = "/{code}/delete")
    public String delete(@PathVariable String code, @AuthenticationPrincipal UserDetail userDetail, Model model) {

        ErrorKinds result = employeeService.delete(code, userDetail);

        if (ErrorMessage.contains(result)) {
            model.addAttribute(ErrorMessage.getErrorName(result), ErrorMessage.getErrorValue(result));
            model.addAttribute("employee", employeeService.findByCode(code));
            return detail(code, model);
        }

        return "redirect:/employees";
    }

}
