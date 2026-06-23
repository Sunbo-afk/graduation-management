package com.gms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gms.entity.Deadline;
import com.gms.entity.Review;
import com.gms.entity.Submission;
import com.gms.mapper.DeadlineMapper;
import com.gms.mapper.ReviewMapper;
import com.gms.mapper.SelectionMapper;
import com.gms.mapper.SubmissionMapper;
import com.gms.service.SubmissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class SubmissionServiceImpl extends ServiceImpl<SubmissionMapper, Submission> implements SubmissionService {

    @Autowired
    private DeadlineMapper deadlineMapper;

    @Autowired
    private ReviewMapper reviewMapper;

    @Autowired
    private SelectionMapper selectionMapper;

    @Override
    @Transactional
    public Map<String, Object> submitDocument(Integer selectionId, String stage, String filePath, String description) {
        Map<String, Object> result = new HashMap<>();

        // 1. Check deadline for this stage
        LocalDate today = LocalDate.now();
        LambdaQueryWrapper<Deadline> dlw = new LambdaQueryWrapper<>();
        dlw.eq(Deadline::getStage, stage);
        Deadline deadline = deadlineMapper.selectOne(dlw);
        if (deadline != null) {
            if (today.isBefore(deadline.getStartDate()) || today.isAfter(deadline.getEndDate())) {
                result.put("success", false);
                result.put("message", "当前不在" + stage + "提交时间内");
                return result;
            }
        }

        // 2. Determine latest version for this stage and selection
        LambdaQueryWrapper<Submission> sw = new LambdaQueryWrapper<>();
        sw.eq(Submission::getSelectionId, selectionId)
          .eq(Submission::getStage, stage)
          .orderByDesc(Submission::getVersion);
        Submission lastSub = baseMapper.selectOne(sw);
        int newVersion = (lastSub != null) ? lastSub.getVersion() + 1 : 1;

        // 3. Create submission record
        Submission submission = new Submission();
        submission.setSelectionId(selectionId);
        submission.setStage(stage);
        submission.setFilePath(filePath);
        submission.setDescription(description);
        submission.setSubmitTime(LocalDateTime.now());
        submission.setStatus("待审阅");
        submission.setVersion(newVersion);
        baseMapper.insert(submission);

        result.put("success", true);
        result.put("message", "提交成功");
        return result;
    }

    @Override
    @Transactional
    public Map<String, Object> reviewSubmission(Integer submissionId, String teacherId, BigDecimal score, String comment) {
        Map<String, Object> result = new HashMap<>();

        // 1. Check if this submission has already been reviewed
        LambdaQueryWrapper<Review> rw = new LambdaQueryWrapper<>();
        rw.eq(Review::getSubmissionId, submissionId);
        if (reviewMapper.selectCount(rw) > 0) {
            result.put("success", false);
            result.put("message", "该提交已经审阅过了");
            return result;
        }

        // 2. Create review record
        Review review = new Review();
        review.setSubmissionId(submissionId);
        review.setTeacherId(teacherId);
        review.setScore(score);
        review.setComment(comment);
        review.setReviewTime(LocalDateTime.now());
        reviewMapper.insert(review);

        // 3. Update submission status based on score
        Submission submission = baseMapper.selectById(submissionId);
        if (submission != null) {
            if (score.compareTo(BigDecimal.valueOf(60)) >= 0) {
                submission.setStatus("已通过");
            } else {
                submission.setStatus("需修改");
            }
            baseMapper.updateById(submission);
        }

        result.put("success", true);
        result.put("message", "审阅完成");
        return result;
    }
}
