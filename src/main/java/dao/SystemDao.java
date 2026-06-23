package dao;

import db.DB;
import model.User;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SystemDao {
    public User login(String username, String password) {
        String sql = "select u.user_id, u.username, u.related_id, r.role_name " +
                "from user_account u join role r on u.role_id = r.role_id " +
                "where u.username = ? and u.password_hash = ? and u.status = 'normal'";
        try (Connection conn = DB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, password);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    User user = new User();
                    user.setUserId(rs.getInt("user_id"));
                    user.setUsername(rs.getString("username"));
                    user.setRoleName(rs.getString("role_name"));
                    int relatedId = rs.getInt("related_id");
                    if (rs.wasNull()) {
                        user.setRelatedId(null);
                    } else {
                        user.setRelatedId(relatedId);
                    }
                    return user;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Map<String, String>> listStudents() {
        String sql = "select s.student_id, s.student_no, s.student_name, s.gender, c.class_name, m.major_name, d.department_name " +
                "from student s join class_info c on s.class_id = c.class_id " +
                "join major m on c.major_id = m.major_id " +
                "join department d on m.department_id = d.department_id " +
                "order by s.student_id";
        return query(sql);
    }

    public List<Map<String, String>> listTeachers() {
        String sql = "select t.teacher_id, t.teacher_no, t.teacher_name, d.department_name, t.research_direction " +
                "from teacher t join department d on t.department_id = d.department_id order by t.teacher_id";
        return query(sql);
    }

    public List<Map<String, String>> listTopics() {
        String sql = "select gt.topic_id, gt.topic_title, t.teacher_name, gt.required_skill, gt.max_students, gt.selected_count, gt.status " +
                "from graduation_topic gt join teacher t on gt.teacher_id = t.teacher_id order by gt.topic_id";
        return query(sql);
    }

    public List<Map<String, String>> listStages() {
        return query("select stage_id, stage_name, stage_order from stage order by stage_order");
    }

    public List<Map<String, String>> listDeadlines() {
        String sql = "select dl.deadline_id, d.department_name, st.stage_name, dl.start_time, dl.end_time " +
                "from deadline dl join department d on dl.department_id = d.department_id " +
                "join stage st on dl.stage_id = st.stage_id order by dl.deadline_id";
        return query(sql);
    }

    public List<Map<String, String>> listNotices() {
        String sql = "select n.notice_id, n.title, n.content, u.username, n.publish_time " +
                "from notice n join user_account u on n.publisher_id = u.user_id order by n.notice_id desc";
        return query(sql);
    }

    public List<Map<String, String>> listSubmissions() {
        String sql = "select sub.submission_id, s.student_name, st.stage_name, sub.title, sub.file_path, sub.submit_time, sub.status " +
                "from submission sub join student s on sub.student_id = s.student_id " +
                "join stage st on sub.stage_id = st.stage_id order by sub.submission_id desc";
        return query(sql);
    }

    public List<Map<String, String>> listReviews() {
        String sql = "select r.review_id, s.student_name, st.stage_name, t.teacher_name, r.score, r.comment, r.review_time " +
                "from review r join submission sub on r.submission_id = sub.submission_id " +
                "join student s on sub.student_id = s.student_id " +
                "join stage st on sub.stage_id = st.stage_id " +
                "join teacher t on r.teacher_id = t.teacher_id order by r.review_id desc";
        return query(sql);
    }

    public List<Map<String, String>> listProgress() {
        return query("select * from v_student_progress order by student_id, stage_name");
    }

    public List<Map<String, String>> listProgressByStudent(int studentId) {
        String sql = "select * from v_student_progress where student_id = " + studentId + " order by stage_name";
        return query(sql);
    }

    public List<Map<String, String>> listTeacherGuidance() {
        return query("select * from v_teacher_guidance order by teacher_id");
    }

    public List<Map<String, String>> listScoreRank() {
        return query("select * from v_score_rank order by department_name, department_rank");
    }

    public List<Map<String, String>> listLogs() {
        String sql = "select log_id, operation_type, target_table, target_id, operation_time, detail " +
                "from operation_log order by log_id desc limit 50";
        return query(sql);
    }

    public boolean addStudent(String classId, String studentNo, String studentName, String gender, String phone, String email) {
        boolean ok = false;
        try (Connection conn = DB.getConnection()) {
            conn.setAutoCommit(false);

            int studentId = 0;
            String sql = "insert into student(class_id, student_no, student_name, gender, phone, email) values(?, ?, ?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, classId);
                ps.setString(2, studentNo);
                ps.setString(3, studentName);
                ps.setString(4, gender);
                ps.setString(5, phone);
                ps.setString(6, email);
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        studentId = rs.getInt(1);
                    }
                }
            }

            int roleId = getRoleId(conn, "student");
            String accountSql = "insert into user_account(username, password_hash, role_id, related_id) values(?, '123456', ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(accountSql)) {
                ps.setString(1, studentName);
                ps.setInt(2, roleId);
                ps.setInt(3, studentId);
                ps.executeUpdate();
            }

            conn.commit();
            ok = true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ok;
    }

    public boolean addTeacher(String departmentId, String teacherNo, String teacherName, String direction, String phone, String email) {
        boolean ok = false;
        try (Connection conn = DB.getConnection()) {
            conn.setAutoCommit(false);

            int teacherId = 0;
            String sql = "insert into teacher(department_id, teacher_no, teacher_name, research_direction, phone, email) values(?, ?, ?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, departmentId);
                ps.setString(2, teacherNo);
                ps.setString(3, teacherName);
                ps.setString(4, direction);
                ps.setString(5, phone);
                ps.setString(6, email);
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        teacherId = rs.getInt(1);
                    }
                }
            }

            int roleId = getRoleId(conn, "teacher");
            String accountSql = "insert into user_account(username, password_hash, role_id, related_id) values(?, '123456', ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(accountSql)) {
                ps.setString(1, teacherName);
                ps.setInt(2, roleId);
                ps.setInt(3, teacherId);
                ps.executeUpdate();
            }

            conn.commit();
            ok = true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ok;
    }

    public boolean deleteTeacher(String teacherId) {
        boolean ok = false;
        try (Connection conn = DB.getConnection()) {
            conn.setAutoCommit(false);

            String deleteAccountSql = "delete from user_account where role_id = ? and related_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(deleteAccountSql)) {
                ps.setInt(1, getRoleId(conn, "teacher"));
                ps.setString(2, teacherId);
                ps.executeUpdate();
            }

            String deleteTeacherSql = "delete from teacher where teacher_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(deleteTeacherSql)) {
                ps.setString(1, teacherId);
                ok = ps.executeUpdate() > 0;
            }

            conn.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ok;
    }

    public boolean changePassword(int userId, String oldPassword, String newPassword) {
        String sql = "update user_account set password_hash = ? where user_id = ? and password_hash = ?";
        return update(sql, newPassword, String.valueOf(userId), oldPassword);
    }

    private int getRoleId(Connection conn, String roleName) throws SQLException {
        String sql = "select role_id from role where role_name = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, roleName);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("role_id");
                }
            }
        }
        throw new SQLException("role not found: " + roleName);
    }

    public boolean addTopic(String teacherId, String title, String desc, String skill, String maxStudents) {
        String sql = "insert into graduation_topic(teacher_id, topic_title, topic_desc, required_skill, max_students) values(?, ?, ?, ?, ?)";
        return update(sql, teacherId, title, desc, skill, maxStudents);
    }

    public boolean addNotice(int publisherId, String title, String content) {
        String sql = "insert into notice(publisher_id, title, content, publish_time) values(?, ?, ?, now())";
        return update(sql, String.valueOf(publisherId), title, content);
    }

    public boolean setDeadline(String departmentId, String stageId, String startTime, String endTime) {
        String sql = "insert into deadline(department_id, stage_id, start_time, end_time) values(?, ?, ?, ?) " +
                "on duplicate key update start_time = values(start_time), end_time = values(end_time)";
        return update(sql, departmentId, stageId, startTime, endTime);
    }

    public boolean addSubmission(String studentId, String stageId, String title, String content, String filePath) {
        String sql = "insert into submission(student_id, stage_id, title, content, file_path, submit_time, status) " +
                "values(?, ?, ?, ?, ?, now(), 'submitted')";
        return update(sql, studentId, stageId, title, content, filePath);
    }

    public boolean hasSelectedTopic(String studentId) {
        String sql = "select count(*) as total from topic_selection where student_id = ?";
        try (Connection conn = DB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int total = rs.getInt("total");
                    if (total > 0) {
                        return true;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean addReview(String submissionId, String teacherId, String comment, String score) {
        String sql = "insert into review(submission_id, teacher_id, comment, score, review_time) values(?, ?, ?, ?, now())";
        return update(sql, submissionId, teacherId, comment, score);
    }

    public String selectTopic(String studentId, String topicId) {
        String result = "select topic failed";
        try (Connection conn = DB.getConnection();
             CallableStatement cs = conn.prepareCall("{call sp_select_topic(?, ?, ?)}")) {
            cs.setInt(1, Integer.parseInt(studentId));
            cs.setInt(2, Integer.parseInt(topicId));
            cs.registerOutParameter(3, Types.VARCHAR);
            cs.execute();
            result = cs.getString(3);
        } catch (SQLException e) {
            result = e.getMessage();
            e.printStackTrace();
        }
        return result;
    }

    public String calcScore(String studentId) {
        String result = "calculate failed";
        try (Connection conn = DB.getConnection();
             CallableStatement cs = conn.prepareCall("{call sp_calc_final_score(?, ?)}")) {
            cs.setInt(1, Integer.parseInt(studentId));
            cs.registerOutParameter(2, Types.DECIMAL);
            cs.execute();
            result = "total score: " + cs.getBigDecimal(2);
        } catch (SQLException e) {
            result = e.getMessage();
            e.printStackTrace();
        }
        return result;
    }

    private boolean update(String sql, String... params) {
        try (Connection conn = DB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                ps.setString(i + 1, params[i]);
            }
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private List<Map<String, String>> query(String sql) {
        List<Map<String, String>> list = new ArrayList<>();
        try (Connection conn = DB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            ResultSetMetaData metaData = rs.getMetaData();
            int count = metaData.getColumnCount();
            while (rs.next()) {
                Map<String, String> row = new LinkedHashMap<>();
                for (int i = 1; i <= count; i++) {
                    String name = metaData.getColumnLabel(i);
                    String value = rs.getString(i);
                    if (value == null) {
                        value = "";
                    }
                    row.put(name, value);
                }
                list.add(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}
