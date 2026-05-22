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
@AllArgsConstructor
public class CalendarsController {

  private final PlanRepository planRepository;

  // 1週間のカレンダーと予定が表示されるページ
  @GetMapping("/")
  public String index(Model model) {
    model.addAttribute("planForm", new PlanForm());
    List<Map<String, Object>> weekDays = get_week();
    model.addAttribute("weekDays", weekDays);
    return "calendars/index";
  }

  // 予定の保存
  @PostMapping("/calendars")
  public String create(@ModelAttribute("planForm") @Validated PlanForm planForm, BindingResult result) {
    if (!result.hasErrors()) {
      PlanEntity newPlan = new PlanEntity();
      newPlan.setDate(planForm.getDate());
      newPlan.setPlan(planForm.getPlan());
      planRepository.insert(newPlan);
    }
    return "redirect:/calendars";
  }

  private List<Map<String, Object>> get_week() {
    List<Map<String, Object>> weekDays = new ArrayList<>();

    LocalDate todaysDate = LocalDate.now();
    List<PlanEntity> plans = planRepository.findByDateBetween(todaysDate, todaysDate.plusDays(6));

    String[] wdays = {"(日)", "(月)", "(火)", "(水)", "(木)", "(金)", "(土)"};

    for (int x = 0; x < 7; x++) {
      Map<String, Object> day_map = new HashMap<>();
      LocalDate currentDate = todaysDate.plusDays(x);

      List<String> todayPlans = new ArrayList<>();
      for (PlanEntity plan : plans) {
          if (plan.getDate().equals(currentDate)) {
              todayPlans.add(plan.getPlan());
          }
      }

      day_map.put("month", currentDate.getMonthValue());
      day_map.put("date", currentDate.getDayOfMonth());
      day_map.put("plans", todayPlans);

      weekDays.add(day_map);
    }

    return weekDays;
  }


}
