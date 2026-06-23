package com.gms.controller;

import com.gms.entity.*;
import com.gms.mapper.SubmissionMapper;
import com.gms.mapper.ReviewMapper;
import com.gms.mapper.TopicMapper;
import com.gms.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/student")
public class StudentController {

    @Autowired private StudentService studentService;
    @Autowired private TopicService topicService;
    @Autowired private TeacherService teacherService;
    @Autowired private TopicMapper topicMapper;
    @Autowired private SelectionService selectionService;
    @Autowired private SubmissionService submissionService;
    @Autowired private ReviewService reviewService;
    @Autowired private SubmissionMapper submissionMapper;
    @Autowired private ReviewMapper reviewMapper;
    @Autowired private DeadlineService deadlineService;

    private boolean isStudent(HttpSession session) {
        Map<String, Object> user = (Map<String, Object>) session.getAttribute("loginUser");
        return user != null && "STUDENT".equals(user.get("role"));
    }

    private String getStudentId(HttpSession session) {
        Map<String, Object> user = (Map<String, Object>) session.getAttribute("loginUser");
        return user != null ? (String) user.get("userId") : null;
    }

    // ==================== Dashboard ====================
    @GetMapping("/index")
    public String index(HttpSession session, Model model) {
        if (!isStudent(session)) {
            return "redirect:/login";
        }
        String stuId = getStudentId(session);
        Student student = studentService.getById(stuId);
        model.addAttribute("student", student);

        Selection selection = selectionService.getDetailByStuId(stuId);
        if (selection != null) {
            model.addAttribute("selection", selection);
            Topic topic = topicService.getById(selection.getTopicId());
            model.addAttribute("selectedTopic", topic);
        }

        List<Deadline> deadlines = deadlineService.list();
        model.addAttribute("deadlines", deadlines);
        return "student/index";
    }

    // ==================== Topics ====================
    @GetMapping("/topics")
    public String topics(@RequestParam(required = false) String keyword,
                         @RequestParam(required = false) String direction,
                         HttpSession session, Model model) {
        if (!isStudent(session)) {
            return "redirect:/login";
        }
        List<Topic> topics = topicMapper.selectAvailableTopics(keyword, direction, null);
        model.addAttribute("topics", topics);
        model.addAttribute("keyword", keyword);
        model.addAttribute("direction", direction);

        List<String> directions = topicMapper.selectDistinctDirections();
        model.addAttribute("directions", directions);

        String stuId = getStudentId(session);
        Selection existingSelection = selectionService.getDetailByStuId(stuId);
        model.addAttribute("hasSelection", existingSelection != null);

        return "student/topics";
    }

    // ==================== Select Topic ====================
    @PostMapping("/select")
    public String selectTopic(@RequestParam Integer topicId, HttpSession session,
                              RedirectAttributes redirectAttributes) {
        if (!isStudent(session)) {
            return "redirect:/login";
        }
        String stuId = getStudentId(session);
        try {
            Map<String, Object> result = selectionService.selectTopic(stuId, topicId);
            if ((Boolean) result.get("success")) {
                redirectAttributes.addFlashAttribute("success", result.get("message"));
            } else {
                redirectAttributes.addFlashAttribute("error", result.get("message"));
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "选题失败: " + e.getMessage());
        }
        return "redirect:/student/topics";
    }

    // ==================== My Selection ====================
    @GetMapping("/my-selection")
    public String mySelection(HttpSession session, Model model) {
        if (!isStudent(session)) {
            return "redirect:/login";
        }
        String stuId = getStudentId(session);
        Selection selection = selectionService.getDetailByStuId(stuId);
        if (selection == null) {
            model.addAttribute("hasSelection", false);
            return "student/my-selection";
        }
        model.addAttribute("hasSelection", true);
        model.addAttribute("selection", selection);

        Topic topic = topicService.getById(selection.getTopicId());
        model.addAttribute("topic", topic);

        List<Submission> submissions = submissionMapper.selectBySelectionId(selection.getSelectionId());
        model.addAttribute("submissions", submissions);

        if (submissions != null && !submissions.isEmpty()) {
            List<Integer> subIds = submissions.stream().map(Submission::getSubmissionId).collect(Collectors.toList());
            List<Review> reviews = reviewMapper.selectBySubmissionIds(subIds);
            model.addAttribute("reviews", reviews != null ? reviews : new ArrayList<>());
        }

        return "student/my-selection";
    }

    // ==================== Submit Document ====================
    @PostMapping("/submit")
    public String submitDocument(@RequestParam Integer selectionId,
                                 @RequestParam String stage,
                                 @RequestParam String filePath,
                                 @RequestParam String description,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {
        if (!isStudent(session)) {
            return "redirect:/login";
        }
        try {
            Map<String, Object> result = submissionService.submitDocument(selectionId, stage, filePath, description);
            if ((Boolean) result.get("success")) {
                redirectAttributes.addFlashAttribute("success", "文档提交成功");
            } else {
                redirectAttributes.addFlashAttribute("error", result.get("message"));
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "提交失败: " + e.getMessage());
        }
        return "redirect:/student/my-selection";
    }

    // ==================== Reviews ====================
    @GetMapping("/reviews")
    public String reviews(HttpSession session, Model model) {
        if (!isStudent(session)) {
            return "redirect:/login";
        }
        String stuId = getStudentId(session);
        Selection selection = selectionService.getDetailByStuId(stuId);
        if (selection == null) {
            return "student/reviews";
        }

        // Load topic info for the selection and add to model
        Topic topic = topicService.getById(selection.getTopicId());
        model.addAttribute("topic", topic);
        model.addAttribute("selection", selection);

        List<Submission> submissions = submissionMapper.selectBySelectionId(selection.getSelectionId());
        if (submissions == null || submissions.isEmpty()) {
            return "student/reviews";
        }

        // Build submissionReviews list (submission + review combined)
        List<Map<String, Object>> submissionReviews = new ArrayList<>();
        List<Map<String, Object>> stageScores = new ArrayList<>();
        BigDecimal overallScore = null;
        int totalWeight = 0;
        BigDecimal weightedSum = BigDecimal.ZERO;

        // Stage weights
        Map<String, Integer> stageWeights = new HashMap<>();
        stageWeights.put("开题报告", 15);
        stageWeights.put("中期检查", 15);
        stageWeights.put("初稿", 30);
        stageWeights.put("终稿", 40);

        for (Submission sub : submissions) {
            Map<String, Object> sr = new HashMap<>();
            sr.put("stage", sub.getStage());
            sr.put("version", sub.getVersion());
            sr.put("submitTime", sub.getSubmitTime());
            sr.put("submissionStatus", sub.getStatus());

            List<Review> revs = reviewMapper.selectBySubmissionId(sub.getSubmissionId());
            if (revs != null && !revs.isEmpty()) {
                Review rev = revs.get(0);
                sr.put("reviewStatus", "已完成");
                sr.put("score", rev.getScore());
                sr.put("comment", rev.getComment());
                sr.put("reviewTime", rev.getReviewTime());

                // Track stage scores
                Map<String, Object> ss = new HashMap<>();
                ss.put("stage", sub.getStage());
                ss.put("maxScore", BigDecimal.valueOf(100));
                ss.put("score", rev.getScore());
                ss.put("comment", rev.getComment());
                ss.put("reviewTime", rev.getReviewTime());
                stageScores.add(ss);

                // Calculate weighted overall score
                Integer weight = stageWeights.getOrDefault(sub.getStage(), 25);
                weightedSum = weightedSum.add(rev.getScore().multiply(BigDecimal.valueOf(weight)));
                totalWeight += weight;
            } else {
                sr.put("reviewStatus", null);
                sr.put("score", null);
                sr.put("comment", null);
                sr.put("reviewTime", null);
            }
            submissionReviews.add(sr);
        }

        if (totalWeight > 0) {
            overallScore = weightedSum.divide(BigDecimal.valueOf(totalWeight), 1, RoundingMode.HALF_UP);
        }

        model.addAttribute("submissionReviews", submissionReviews);
        model.addAttribute("stageScores", stageScores);
        model.addAttribute("overallScore", overallScore);
        return "student/reviews";
    }
}
