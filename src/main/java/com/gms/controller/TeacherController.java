package com.gms.controller;

import com.gms.entity.*;
import com.gms.mapper.*;
import com.gms.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/teacher")
public class TeacherController {

    @Autowired private TeacherService teacherService;
    @Autowired private TopicService topicService;
    @Autowired private TopicMapper topicMapper;
    @Autowired private SelectionService selectionService;
    @Autowired private SelectionMapper selectionMapper;
    @Autowired private StudentMapper studentMapper;
    @Autowired private SubmissionService submissionService;
    @Autowired private SubmissionMapper submissionMapper;
    @Autowired private ReviewService reviewService;
    @Autowired private ReviewMapper reviewMapper;
    @Autowired private StatisticsService statisticsService;

    private boolean isTeacher(HttpSession session) {
        Map<String, Object> user = (Map<String, Object>) session.getAttribute("loginUser");
        return user != null && "TEACHER".equals(user.get("role"));
    }

    private String getTeacherId(HttpSession session) {
        Map<String, Object> user = (Map<String, Object>) session.getAttribute("loginUser");
        return user != null ? (String) user.get("userId") : null;
    }

    // ==================== Dashboard ====================
    @GetMapping("/index")
    public String index(HttpSession session, Model model) {
        if (!isTeacher(session)) {
            return "redirect:/login";
        }
        String teacherId = getTeacherId(session);
        Teacher teacher = teacherService.getById(teacherId);
        model.addAttribute("teacher", teacher);

        List<Topic> myTopics = topicMapper.selectByTeacherId(teacherId);
        int studentCount = 0;
        int pendingReviews = 0;
        int completedCount = 0;

        if (myTopics != null && !myTopics.isEmpty()) {
            List<Integer> topicIds = myTopics.stream().map(Topic::getTopicId).collect(Collectors.toList());
            List<Selection> selections = selectionMapper.selectByTopicIds(topicIds);
            if (selections != null) {
                studentCount = selections.size();
                for (Selection sel : selections) {
                    if ("已完成".equals(sel.getStatus())) {
                        completedCount++;
                    }
                }
                if (!selections.isEmpty()) {
                    List<Integer> selectionIds = selections.stream().map(Selection::getSelectionId).collect(Collectors.toList());
                    List<Submission> submissions = submissionMapper.selectBySelectionIds(selectionIds);
                    if (submissions != null) {
                        for (Submission sub : submissions) {
                            List<Review> reviews = reviewMapper.selectBySubmissionId(sub.getSubmissionId());
                            if (reviews == null || reviews.isEmpty()) {
                                pendingReviews++;
                            }
                        }
                    }
                }
            }
        }
        model.addAttribute("studentCount", studentCount);
        model.addAttribute("pendingReviews", pendingReviews);
        model.addAttribute("completedCount", completedCount);

        return "teacher/index";
    }

    // ==================== Students ====================
    @GetMapping("/students")
    public String students(HttpSession session, Model model) {
        if (!isTeacher(session)) {
            return "redirect:/login";
        }
        String teacherId = getTeacherId(session);
        List<Topic> myTopics = topicMapper.selectByTeacherId(teacherId);
        if (myTopics == null || myTopics.isEmpty()) {
            model.addAttribute("studentList", new ArrayList<>());
            return "teacher/students";
        }

        List<Integer> topicIds = myTopics.stream().map(Topic::getTopicId).collect(Collectors.toList());
        List<Selection> selections = selectionMapper.selectByTopicIds(topicIds);

        // Build topicId -> title map
        Map<Integer, String> topicTitleMap = new HashMap<>();
        for (Topic t : myTopics) {
            topicTitleMap.put(t.getTopicId(), t.getTitle());
        }

        List<Map<String, Object>> studentList = new ArrayList<>();
        if (selections != null) {
            for (Selection sel : selections) {
                Student student = studentMapper.selectById(sel.getStuId());
                if (student != null) {
                    Map<String, Object> entry = new HashMap<>();
                    // Flat fields matching template expectations
                    entry.put("studentNo", student.getStuId());
                    entry.put("studentName", student.getStuName());
                    entry.put("selectionId", sel.getSelectionId());
                    entry.put("topicTitle", topicTitleMap.getOrDefault(sel.getTopicId(), ""));
                    entry.put("finalScore", sel.getFinalScore());
                    entry.put("currentStage", null);

                    List<Submission> submissions = submissionMapper.selectBySelectionId(sel.getSelectionId());
                    entry.put("submissions", submissions != null ? submissions : new ArrayList<>());
                    if (submissions != null && !submissions.isEmpty()) {
                        Submission latest = submissions.get(0);
                        entry.put("currentStage", latest.getStage());
                        Map<Integer, Review> reviewMap = new HashMap<>();
                        for (Submission sub : submissions) {
                            List<Review> revs = reviewMapper.selectBySubmissionId(sub.getSubmissionId());
                            if (revs != null && !revs.isEmpty()) {
                                reviewMap.put(sub.getSubmissionId(), revs.get(0));
                            }
                        }
                        entry.put("reviewMap", reviewMap);
                    }
                    studentList.add(entry);
                }
            }
        }
        model.addAttribute("studentList", studentList);

        return "teacher/students";
    }

    // ==================== Review ====================
    @GetMapping("/review")
    public String reviewPage(@RequestParam Integer selectionId, HttpSession session, Model model) {
        if (!isTeacher(session)) {
            return "redirect:/login";
        }
        // Load submissions for this selection
        List<Submission> submissions = submissionMapper.selectBySelectionId(selectionId);
        model.addAttribute("submissions", submissions);
        model.addAttribute("selectionId", selectionId);

        // Get selection details
        Selection selection = selectionMapper.selectById(selectionId);
        model.addAttribute("selection", selection);
        if (selection != null) {
            Student student = studentMapper.selectById(selection.getStuId());
            model.addAttribute("student", student);
            Topic topic = topicMapper.selectById(selection.getTopicId());
            model.addAttribute("topic", topic);
        }
        return "teacher/review";
    }

    @PostMapping("/review")
    public String reviewSubmission(@RequestParam Integer submissionId,
                                   @RequestParam BigDecimal score,
                                   @RequestParam String comment,
                                   HttpSession session,
                                   RedirectAttributes redirectAttributes) {
        if (!isTeacher(session)) {
            return "redirect:/login";
        }
        String teacherId = getTeacherId(session);
        try {
            Map<String, Object> result = submissionService.reviewSubmission(submissionId, teacherId, score, comment);
            if ((Boolean) result.get("success")) {
                redirectAttributes.addFlashAttribute("success", "评审提交成功");
            } else {
                redirectAttributes.addFlashAttribute("error", result.get("message"));
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "评审失败: " + e.getMessage());
        }
        return "redirect:/teacher/students";
    }

    // ==================== Statistics ====================
    @GetMapping("/statistics")
    public String statistics(HttpSession session, Model model) {
        if (!isTeacher(session)) {
            return "redirect:/login";
        }
        String teacherId = getTeacherId(session);

        // 1. My student score ranking
        List<Map<String, Object>> myRankings = getMyStudentRankings(teacherId);
        model.addAttribute("scoreRanking", myRankings);

        // 2. My teacher summary stats
        Map<String, Object> myStats = getMyTeacherStats(teacherId);
        model.addAttribute("teacherStats", myStats);

        // 3. Department comparison stats
        Map<String, Object> deptStats = getDeptComparisonStats(teacherId);
        model.addAttribute("deptStats", deptStats);

        // 4. Teacher distribution across department
        List<Map<String, Object>> teacherDistribution = getTeacherDistribution(teacherId);
        model.addAttribute("teacherDistribution", teacherDistribution);

        return "teacher/statistics";
    }

    private List<Map<String, Object>> getMyStudentRankings(String teacherId) {
        List<Map<String, Object>> result = new ArrayList<>();
        List<Topic> myTopics = topicMapper.selectByTeacherId(teacherId);
        if (myTopics == null || myTopics.isEmpty()) return result;

        List<Integer> topicIds = myTopics.stream().map(Topic::getTopicId).collect(Collectors.toList());
        List<Selection> selections = selectionMapper.selectByTopicIds(topicIds);
        if (selections == null) return result;

        for (Selection sel : selections) {
            if (sel.getFinalScore() != null) {
                Map<String, Object> entry = new HashMap<>();
                Student stu = studentMapper.selectById(sel.getStuId());
                Topic t = topicMapper.selectById(sel.getTopicId());
                entry.put("stuName", stu != null ? stu.getStuName() : sel.getStuId());
                entry.put("topicTitle", t != null ? t.getTitle() : "");
                entry.put("score", sel.getFinalScore());
                // Determine current stage from submissions
                List<Submission> subs = submissionMapper.selectBySelectionId(sel.getSelectionId());
                String currentStage = (subs != null && !subs.isEmpty()) ? subs.get(0).getStage() : "未开始";
                entry.put("currentStage", currentStage);
                result.add(entry);
            }
        }
        result.sort((a, b) -> ((BigDecimal) b.get("score")).compareTo((BigDecimal) a.get("score")));
        return result;
    }

    private Map<String, Object> getMyTeacherStats(String teacherId) {
        Map<String, Object> stats = new HashMap<>();
        List<Topic> myTopics = topicMapper.selectByTeacherId(teacherId);
        if (myTopics == null || myTopics.isEmpty()) {
            stats.put("studentCount", 0);
            stats.put("avgScore", BigDecimal.ZERO);
            stats.put("maxScore", BigDecimal.ZERO);
            stats.put("passRate", "0.0");
            return stats;
        }

        List<Integer> topicIds = myTopics.stream().map(Topic::getTopicId).collect(Collectors.toList());
        List<Selection> selections = selectionMapper.selectByTopicIds(topicIds);

        int studentCount = selections != null ? selections.size() : 0;
        stats.put("studentCount", studentCount);

        BigDecimal maxScore = BigDecimal.ZERO;
        BigDecimal sumScore = BigDecimal.ZERO;
        int scoredCount = 0;
        int passedCount = 0;

        if (selections != null) {
            for (Selection sel : selections) {
                if (sel.getFinalScore() != null) {
                    if (sel.getFinalScore().compareTo(maxScore) > 0) {
                        maxScore = sel.getFinalScore();
                    }
                    sumScore = sumScore.add(sel.getFinalScore());
                    scoredCount++;
                    if (sel.getFinalScore().compareTo(BigDecimal.valueOf(60)) >= 0) {
                        passedCount++;
                    }
                }
            }
        }

        stats.put("maxScore", maxScore);
        stats.put("avgScore", scoredCount > 0
                ? sumScore.divide(BigDecimal.valueOf(scoredCount), 1, RoundingMode.HALF_UP)
                : BigDecimal.ZERO);
        stats.put("passRate", scoredCount > 0
                ? BigDecimal.valueOf(passedCount * 100.0 / scoredCount).setScale(1, RoundingMode.HALF_UP).toString()
                : "0.0");
        return stats;
    }

    private Map<String, Object> getDeptComparisonStats(String teacherId) {
        Map<String, Object> stats = new HashMap<>();
        Teacher me = teacherService.getById(teacherId);
        if (me == null || me.getDeptId() == null) {
            return stats;
        }

        // My stats
        Map<String, Object> myStats = getMyTeacherStats(teacherId);
        stats.put("myStudentCount", myStats.get("studentCount"));
        stats.put("myAvgScore", myStats.get("avgScore"));
        stats.put("myPassRate", myStats.get("passRate"));

        // Department-wide stats
        List<Teacher> deptTeachers = teacherService.list(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Teacher>()
                .eq(Teacher::getDeptId, me.getDeptId())
        );

        int totalDeptStudents = 0;
        BigDecimal deptSumScore = BigDecimal.ZERO;
        int deptScoredCount = 0;
        int deptPassedCount = 0;

        for (Teacher t : deptTeachers) {
            List<Topic> topics = topicMapper.selectByTeacherId(t.getTeacherId());
            if (topics != null && !topics.isEmpty()) {
                List<Integer> tids = topics.stream().map(Topic::getTopicId).collect(Collectors.toList());
                List<Selection> sels = selectionMapper.selectByTopicIds(tids);
                if (sels != null) {
                    totalDeptStudents += sels.size();
                    for (Selection sel : sels) {
                        if (sel.getFinalScore() != null) {
                            deptSumScore = deptSumScore.add(sel.getFinalScore());
                            deptScoredCount++;
                            if (sel.getFinalScore().compareTo(BigDecimal.valueOf(60)) >= 0) {
                                deptPassedCount++;
                            }
                        }
                    }
                }
            }
        }

        int deptTeacherCount = deptTeachers.size();
        BigDecimal avgStudentCount = deptTeacherCount > 0
                ? BigDecimal.valueOf((double) totalDeptStudents / deptTeacherCount).setScale(1, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        stats.put("avgStudentCount", avgStudentCount);
        stats.put("myStudentPercent", avgStudentCount.compareTo(BigDecimal.ZERO) > 0
                ? BigDecimal.valueOf(((Number) myStats.get("studentCount")).doubleValue() * 100.0 / avgStudentCount.doubleValue()).intValue()
                : 0);
        stats.put("deptAvgScore", deptScoredCount > 0
                ? deptSumScore.divide(BigDecimal.valueOf(deptScoredCount), 1, RoundingMode.HALF_UP)
                : BigDecimal.ZERO);
        stats.put("myScorePercent", deptScoredCount > 0 && ((BigDecimal) myStats.get("avgScore")).compareTo(BigDecimal.ZERO) > 0
                ? BigDecimal.valueOf(((BigDecimal) myStats.get("avgScore")).doubleValue() * 100.0 / deptSumScore.divide(BigDecimal.valueOf(deptScoredCount), 1, RoundingMode.HALF_UP).doubleValue()).intValue()
                : 0);
        stats.put("deptPassRate", deptScoredCount > 0
                ? BigDecimal.valueOf(deptPassedCount * 100.0 / deptScoredCount).setScale(1, RoundingMode.HALF_UP).toString()
                : "0.0");
        return stats;
    }

    private List<Map<String, Object>> getTeacherDistribution(String teacherId) {
        List<Map<String, Object>> result = new ArrayList<>();
        Teacher me = teacherService.getById(teacherId);
        if (me == null || me.getDeptId() == null) return result;

        List<Teacher> deptTeachers = teacherService.list(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Teacher>()
                .eq(Teacher::getDeptId, me.getDeptId())
        );

        int totalDeptStudents = 0;
        List<Map<String, Object>> tempResults = new ArrayList<>();

        for (Teacher t : deptTeachers) {
            Map<String, Object> entry = new HashMap<>();
            entry.put("teacherName", t.getTeacherName());
            entry.put("researchDirection", t.getResearchDirection());
            entry.put("isMe", t.getTeacherId().equals(teacherId));

            List<Topic> topics = topicMapper.selectByTeacherId(t.getTeacherId());
            int studentCount = 0;
            BigDecimal sumScore = BigDecimal.ZERO;
            int scoredCount = 0;
            int passedCount = 0;

            if (topics != null && !topics.isEmpty()) {
                List<Integer> tids = topics.stream().map(Topic::getTopicId).collect(Collectors.toList());
                List<Selection> sels = selectionMapper.selectByTopicIds(tids);
                if (sels != null) {
                    studentCount = sels.size();
                    for (Selection sel : sels) {
                        if (sel.getFinalScore() != null) {
                            sumScore = sumScore.add(sel.getFinalScore());
                            scoredCount++;
                            if (sel.getFinalScore().compareTo(BigDecimal.valueOf(60)) >= 0) {
                                passedCount++;
                            }
                        }
                    }
                }
            }

            entry.put("studentCount", studentCount);
            totalDeptStudents += studentCount;
            entry.put("avgScore", scoredCount > 0
                    ? sumScore.divide(BigDecimal.valueOf(scoredCount), 1, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO);
            entry.put("passRate", scoredCount > 0
                    ? BigDecimal.valueOf(passedCount * 100.0 / scoredCount).setScale(1, RoundingMode.HALF_UP).toString()
                    : "0.0");
            tempResults.add(entry);
        }

        // Calculate distribution percent
        for (Map<String, Object> entry : tempResults) {
            entry.put("distributionPercent", totalDeptStudents > 0
                    ? Math.round(((Number) entry.get("studentCount")).doubleValue() * 100.0 / totalDeptStudents)
                    : 0);
            result.add(entry);
        }

        return result;
    }

    // ==================== Topics (own) ====================
    @GetMapping("/topics")
    public String topics(HttpSession session, Model model) {
        if (!isTeacher(session)) {
            return "redirect:/login";
        }
        String teacherId = getTeacherId(session);
        List<Topic> topics = topicMapper.selectByTeacherId(teacherId);
        model.addAttribute("topics", topics != null ? topics : new ArrayList<>());
        return "teacher/my-topics";
    }

    @PostMapping("/topic/save")
    public String saveTopic(@ModelAttribute Topic topic, HttpSession session,
                            RedirectAttributes redirectAttributes) {
        if (!isTeacher(session)) {
            return "redirect:/login";
        }
        String teacherId = getTeacherId(session);
        topic.setTeacherId(teacherId);
        try {
            if (topic.getTopicId() != null) {
                Topic existing = topicService.getById(topic.getTopicId());
                if (existing == null || !teacherId.equals(existing.getTeacherId())) {
                    redirectAttributes.addFlashAttribute("error", "无权修改该课题");
                    return "redirect:/teacher/topics";
                }
                topicService.updateById(topic);
                redirectAttributes.addFlashAttribute("success", "课题更新成功");
            } else {
                topicService.save(topic);
                redirectAttributes.addFlashAttribute("success", "课题添加成功");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "操作失败: " + e.getMessage());
        }
        return "redirect:/teacher/topics";
    }

    @GetMapping("/topic/delete/{id}")
    public String deleteTopic(@PathVariable Integer id, HttpSession session,
                              RedirectAttributes redirectAttributes) {
        if (!isTeacher(session)) {
            return "redirect:/login";
        }
        String teacherId = getTeacherId(session);
        try {
            Topic existing = topicService.getById(id);
            if (existing == null || !teacherId.equals(existing.getTeacherId())) {
                redirectAttributes.addFlashAttribute("error", "无权删除该课题");
                return "redirect:/teacher/topics";
            }
            topicService.removeById(id);
            redirectAttributes.addFlashAttribute("success", "课题删除成功");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "删除失败: " + e.getMessage());
        }
        return "redirect:/teacher/topics";
    }
}
