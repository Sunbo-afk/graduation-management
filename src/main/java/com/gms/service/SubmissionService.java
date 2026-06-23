package com.gms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.gms.entity.Submission;

import java.math.BigDecimal;
import java.util.Map;

public interface SubmissionService extends IService<Submission> {

    /**
     * Submit a document for a specific stage
     * @param selectionId the selection record ID
     * @param stage the submission stage (e.g. "开题报告", "中期检查", "论文终稿")
     * @param filePath the file path of the uploaded document
     * @param description description of the submission
     * @return result map with "success" boolean and "message" string
     */
    Map<String, Object> submitDocument(Integer selectionId, String stage, String filePath, String description);

    /**
     * Teacher reviews a student submission
     * @param submissionId the submission ID
     * @param teacherId the reviewing teacher's ID
     * @param score the review score
     * @param comment the review comment
     * @return result map with "success" boolean and "message" string
     */
    Map<String, Object> reviewSubmission(Integer submissionId, String teacherId, BigDecimal score, String comment);
}
