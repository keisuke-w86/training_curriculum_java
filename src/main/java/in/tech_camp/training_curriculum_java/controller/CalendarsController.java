package in.tech_camp.training_curriculum_java.controller;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import in.tech_camp.training_curriculum_java.entity.PlanEntity;
import in.tech_camp.training_curriculum_java.form.PlanForm;
import in.tech_camp.training_curriculum_java.repository.PlanRepository;
import lombok.AllArgsConstructor;

@Controller
@AllArgsConstructor // 全てのインスタンス変数（planRepository）を初期化するコンストラクタを自動生成（Lombok機能）
public class CalendarsController {

  // データベース操作を担うRepositoryクラスを定義（コンストラクタ注入により自動で利用可能になる）
  private final PlanRepository planRepository;

  /**
   * 【処理1】ルートURL（/）にアクセスした時の処理（初期表示）
   */
  @GetMapping("/")
  public String index(Model model) {
    // 画面のフォームと連動させるための、中身が空っぽの初期用PlanFormオブジェクトを登録
    model.addAttribute("planForm", new PlanForm());
    
    // 自作メソッド「get_week()」を呼び出し、今日から1週間分のカレンダー・予定データを取得
    List<Map<String, Object>> weekDays = get_week();
    
    // 取得した1週間分のデータを、"weekDays"という名前でHTML（画面）に引き渡す
    model.addAttribute("weekDays", weekDays);
    
    // 「src/main/resources/templates/calendars/index.html」を表示する
    return "calendars/index";
  }

  /**
   * 【処理2】フォームから予定が送信（POST）されてきた時の保存処理
   */
  @PostMapping("/calendars")
  public String create(@ModelAttribute("planForm") @Validated PlanForm planForm, BindingResult result) {
    
    // バリデーションエラー（入力漏れなど）が発生していないかチェック
    if (!result.hasErrors()) { 
      // エラーがない場合、データベース保存用のエンティティクラス（PlanEntity）のインスタンスを作成
      PlanEntity newPlan = new PlanEntity();
      
      // 画面のフォーム（planForm）から送られてきた「日付」と「予定内容」をエンティティに詰め替える
      newPlan.setDate(planForm.getDate());
      newPlan.setPlan(planForm.getPlan());
      
      // SQLを実行して、データベースに新しい予定を挿入（保存）する
      planRepository.insert(newPlan);
    }
    
    // 処理が終わったら、ブラウザに対して「/calendars へGETリクエストを送り直して（リダイレクト）」と命令する
    return "redirect:/calendars";
  }

  /**
   * 【処理3】予定追加後などにリダイレクト（GET）されてくるページ（一覧の再表示）
   */
  @GetMapping("/calendars")
  public String show(Model model) {
    // 処理1（indexメソッド）と全く同じ動き：最新のデータを揃えてカレンダー画面を再表示する
    model.addAttribute("planForm", new PlanForm());
    List<Map<String, Object>> weekDays = get_week();
    model.addAttribute("weekDays", weekDays);
    return "calendars/index";
  }

  /**
   * 【共通処理】今日から7日分のカレンダー情報と、それぞれの日の予定を組み立てるメソッド
   */
  private List<Map<String, Object>> get_week() {
    // 最終的に画面に渡す「7日分のデータ」を格納するためのリストを用意
    List<Map<String, Object>> weekDays = new ArrayList<>();

    // 現在の当日の日付（今日）を取得
    LocalDate todaysDate = LocalDate.now();
    
    // データベースから「今日から6日後まで（計7日間）」に含まれる予定データを一括で全て取得
    List<PlanEntity> plans = planRepository.findByDateBetween(todaysDate, todaysDate.plusDays(6));

    // （※現状のロジックでは未使用ですが、曜日用の文字列配列定義）
    String[] wdays = { "(月)", "(火)", "(水)", "(木)", "(金)", "(土)", "(日)" };

    // 今日(0日後)から、6日後までの計7回ループを回して、1日ずつのデータを作成する
    for (int x = 0; x < 7; x++) {
      // 1日分のデータを「キーと値」のペアで管理するためのマップ（箱）を作成
      Map<String, Object> day_map = new HashMap<String, Object>();
      
      // 今日の日付に「x日」を足して、ループ対象の具体的な日付を計算
      LocalDate currentDate = todaysDate.plusDays(x);

      // ループ対象の日に該当する予定（文字列）だけを詰め込むためのリストを用意
      List<String> todayPlans = new ArrayList<>();
      
      // データベースから取得しておいた全ての予定（plans）を1つずつチェック
      for (PlanEntity plan : plans) {
          // もし予定の日付が、今ループで処理している日付（currentDate）と一致した場合
          if (plan.getDate().equals(currentDate)) {
              // その予定のテキスト（文字列）を取り出して、リストに追加
              todayPlans.add(plan.getPlan());
          }
      }

      

      // マップ（day_map）に、「何月」「何日」「その日の予定リスト」をそれぞれ名前をつけて保存
      day_map.put("month", currentDate.getMonthValue()); // 月の数字 (例: 5)
      day_map.put("date", currentDate.getDayOfMonth());   // 日の数字 (例: 22)
      day_map.put("plans", todayPlans);                  // その日の予定リスト
      day_map.put("wday", wdays[currentDate.getDayOfWeek().getValue()-1]); // 曜日

      // 完成した1日分のマップを、全体のリスト（7日分）に追加
      weekDays.add(day_map);
    }

    // 組み立てが終わった7日分のデータリストを呼び出し元（indexやshow）に返す
    return weekDays;
  }
}