# -*- coding: utf-8 -*-
"""
PowerDesigner PDM 风格 ER 图生成器
- 深蓝色表头 + 白色表名
- 主键区/普通字段区分隔
- 外键→主键引用线（正交折线）
- Crow's Foot 基数标注
"""
import sys, io
sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8')

# ============================================================
# 全局样式
# ============================================================
HEADER_BG    = "#2B579A"   # PowerDesigner 经典深蓝
HEADER_FG    = "#FFFFFF"
TABLE_BG     = "#FFFFFF"
TABLE_BORDER = "#1A3A5C"
PK_SEP_COLOR = "#2B579A"
TEXT_COLOR   = "#1A1A1A"
REF_COLOR    = "#6B6B6B"
CARD_COLOR   = "#C0392B"
LABEL_COLOR  = "#2471A3"
PK_BG        = "#F0F4FA"
FK_BG        = "#FFF8E1"

FONT        = "Microsoft YaHei, SimHei, sans-serif"
MONO_FONT   = "Consolas, Courier New, monospace"
BOX_W       = 240
ROW_H       = 20
HEADER_H    = 32
FONT_SIZE   = 12
SMALL_FONT  = 10

# ============================================================
# 实体数据
# ============================================================
ENTITIES = {}

def ent(name, x, y, label, attrs):
    """注册实体: name=英文名, label=中文显示名, attrs=[(列名,类型,{PK/FK/U/N})]"""
    ENTITIES[name] = {"label": label, "attrs": attrs, "pos": (x, y)}

# ---- 核心业务实体 (左侧列) ----
ent("Department", 30, 55, "系 (Department)", [
    ("dept_id",    "INT",            "PK"),
    ("dept_name",  "VARCHAR(50)",    "U"),
    ("created_at", "DATETIME",       ""),
])

ent("Major", 30, 205, "专业 (Major)", [
    ("major_id",   "INT",            "PK"),
    ("major_name", "VARCHAR(50)",    ""),
    ("dept_id",    "INT",            "FK"),
    ("created_at", "DATETIME",       ""),
])

ent("Class", 30, 370, "班级 (Class)", [
    ("class_id",   "INT",            "PK"),
    ("class_name", "VARCHAR(50)",    ""),
    ("major_id",   "INT",            "FK"),
    ("grade",      "VARCHAR(10)",    ""),
    ("created_at", "DATETIME",       ""),
])

ent("Student", 30, 550, "学生 (Student)", [
    ("stu_id",     "VARCHAR(20)",    "PK"),
    ("stu_name",   "VARCHAR(30)",    ""),
    ("password",   "VARCHAR(255)",   ""),
    ("class_id",   "INT",            "FK"),
    ("phone",      "VARCHAR(20)",    ""),
    ("email",      "VARCHAR(100)",   ""),
    ("created_at", "DATETIME",       ""),
])

ent("Selection", 350, 500, "选题 (Selection)", [
    ("selection_id","INT",           "PK"),
    ("stu_id",      "VARCHAR(20)",   "FK/U"),
    ("topic_id",    "INT",           "FK"),
    ("select_time", "DATETIME",      ""),
    ("status",      "VARCHAR(20)",   ""),
    ("final_score", "DECIMAL(5,2)",  ""),
    ("completed_at","DATETIME",      ""),
])

ent("Submission", 350, 680, "文档提交 (Submission)", [
    ("submission_id","INT",          "PK"),
    ("selection_id", "INT",          "FK"),
    ("stage",        "VARCHAR(20)",  ""),
    ("file_path",    "VARCHAR(500)", ""),
    ("description",  "TEXT",         ""),
    ("submit_time",  "DATETIME",     ""),
    ("status",       "VARCHAR(20)",  ""),
    ("version",      "INT",          ""),
])

# ---- 教师/题目 (中间列) ----
ent("Teacher", 650, 55, "教师 (Teacher)", [
    ("teacher_id",      "VARCHAR(20)",   "PK"),
    ("teacher_name",    "VARCHAR(30)",   ""),
    ("password",        "VARCHAR(255)",  ""),
    ("dept_id",         "INT",           "FK"),
    ("title",           "VARCHAR(30)",   ""),
    ("research_direction","VARCHAR(200)",""),
    ("phone",           "VARCHAR(20)",   ""),
    ("email",           "VARCHAR(100)",  ""),
    ("max_students",    "INT",           ""),
    ("created_at",      "DATETIME",      ""),
])

ent("Topic", 650, 390, "题目 (Topic)", [
    ("topic_id",    "INT",            "PK"),
    ("title",       "VARCHAR(200)",   ""),
    ("description", "TEXT",           ""),
    ("teacher_id",  "VARCHAR(20)",    "FK"),
    ("direction",   "VARCHAR(100)",   ""),
    ("status",      "TINYINT",        ""),
    ("max_select",  "INT",            ""),
    ("created_at",  "DATETIME",       ""),
])

ent("Review", 650, 680, "审阅 (Review)", [
    ("review_id",     "INT",           "PK"),
    ("submission_id", "INT",           "FK/U"),
    ("teacher_id",    "VARCHAR(20)",   "FK"),
    ("score",         "DECIMAL(5,2)",  ""),
    ("comment",       "TEXT",          ""),
    ("review_time",   "DATETIME",      ""),
])

# ---- 辅助表 (右侧独立列) ----
ent("Deadline", 960, 55, "流程期限\n(Deadline)", [
    ("deadline_id","INT",            "PK"),
    ("stage",      "VARCHAR(20)",    ""),
    ("start_date", "DATE",           ""),
    ("end_date",   "DATE",           ""),
    ("description","VARCHAR(500)",   ""),
    ("semester",   "VARCHAR(20)",    ""),
])

ent("AuditLog", 960, 270, "审计日志\n(AuditLog)", [
    ("log_id",        "BIGINT",       "PK"),
    ("table_name",    "VARCHAR(50)",  ""),
    ("operation",     "VARCHAR(10)",  ""),
    ("record_id",     "VARCHAR(50)",  ""),
    ("old_value",     "JSON",         ""),
    ("new_value",     "JSON",         ""),
    ("operated_by",   "VARCHAR(30)",  ""),
    ("operation_time","DATETIME",     "PK"),
    ("ip_address",    "VARCHAR(50)",  ""),
])

ent("SysUser", 960, 510, "管理员\n(SysUser)", [
    ("user_id",   "INT",            "PK"),
    ("username",  "VARCHAR(30)",    "U"),
    ("password",  "VARCHAR(255)",   ""),
    ("real_name", "VARCHAR(30)",    ""),
    ("role",      "VARCHAR(20)",    ""),
    ("created_at","DATETIME",       ""),
])

# ============================================================
# 引用关系: (from_ent, from_attr, to_ent, to_attr, rel_name, card)
# card: "1:1" or "1:N"
# ============================================================
REFERENCES = [
    # 左列垂直链
    ("Major",      "dept_id",    "Department", "dept_id",       "所属", "N:1"),
    ("Class",      "major_id",   "Major",      "major_id",      "下设", "N:1"),
    ("Student",    "class_id",   "Class",      "class_id",      "属于", "N:1"),
    # 系→教师
    ("Teacher",    "dept_id",    "Department", "dept_id",       "聘用", "N:1"),
    # 教师→题目 (FK side is Topic)
    ("Topic",      "teacher_id", "Teacher",    "teacher_id",    "发布", "N:1"),
    # 学生→选题
    ("Selection",  "stu_id",     "Student",    "stu_id",        "选题", "1:1"),
    # 题目→选题
    ("Selection",  "topic_id",   "Topic",      "topic_id",      "被选", "N:1"),
    # 选题→提交
    ("Submission", "selection_id","Selection", "selection_id",  "提交", "1:N"),
    # 提交→审阅
    ("Review",     "submission_id","Submission","submission_id","审阅", "1:1"),
    # 教师→审阅
    ("Review",     "teacher_id", "Teacher",    "teacher_id",    "评审", "N:1"),
]

# ============================================================
# 布局引擎
# ============================================================
class TableBox:
    def __init__(self, name, ent):
        self.name = name
        self.x, self.y = ent["pos"]
        self.label = ent["label"]
        self.attrs = ent["attrs"]
        self.n_pk = sum(1 for _, _, t in self.attrs if "PK" in t)
        self.n_all = len(self.attrs)
        # 计算高度: header + pk 区 + 分隔线 + 其余字段
        self.h = HEADER_H + self.n_all * ROW_H + 6
        self.w = BOX_W
        # 列 Y 起点
        self.col_y0 = self.y + HEADER_H + 2

    @property
    def cx(self): return self.x + self.w / 2

    @property
    def cy(self): return self.y + self.h / 2

    @property
    def right(self): return self.x + self.w

    @property
    def bottom(self): return self.y + self.h

    def col_center_y(self, idx):
        """第 idx 个属性的 Y 中心"""
        return self.col_y0 + idx * ROW_H + ROW_H / 2

    def find_attr_y(self, attr_name):
        for i, (a, _, _) in enumerate(self.attrs):
            if a == attr_name:
                return self.col_center_y(i)
        return self.cy

    def find_attr_left(self, attr_name):
        return self.x

    def find_attr_right(self, attr_name):
        return self.x + self.w


# 构建全部 TableBox
tables = {k: TableBox(k, v) for k, v in ENTITIES.items()}

# ============================================================
# SVG 渲染
# ============================================================
def render():
    W, H = 1280, 960
    svg = []
    def tag(s): svg.append(s)

    tag(f'<?xml version="1.0" encoding="UTF-8"?>')
    tag(f'<svg xmlns="http://www.w3.org/2000/svg" width="{W}" height="{H}" viewBox="0 0 {W} {H}">')

    # -- defs: 箭头标记 --
    tag('<defs>')
    # Crow's Foot: "many" side = 分叉线
    tag('<marker id="crow-many" viewBox="0 0 12 12" refX="10" refY="6" markerWidth="8" markerHeight="8" orient="auto">')
    tag('  <path d="M0,0 L8,6 L0,12 M4,2 L12,6 L4,10" fill="none" stroke="#6B6B6B" stroke-width="1.2"/>')
    tag('</marker>')
    # "one" side = 短竖线
    tag('<marker id="crow-one" viewBox="0 0 10 14" refX="2" refY="7" markerWidth="5" markerHeight="7" orient="auto">')
    tag('  <line x1="2" y1="0" x2="2" y2="14" stroke="#6B6B6B" stroke-width="2"/>')
    tag('</marker>')
    tag('</defs>')

    # -- 背景 --
    tag(f'<rect width="{W}" height="{H}" fill="#F5F6FA"/>')

    # -- 标题 --
    tag(f'<text x="{W/2}" y="28" text-anchor="middle" font-size="20" font-weight="bold"'
        f' fill="#1A237E" font-family="{FONT}">毕业设计管理系统 — 数据库 ER 图 (PowerDesigner PDM 风格)</text>')
    tag(f'<text x="{W/2}" y="46" text-anchor="middle" font-size="11" fill="#888"'
        f' font-family="{FONT}">题目37 · 12 张表 · 10 条引用关系</text>')

    # ============================================================
    # 先画引用线（在实体下方）
    # ============================================================
    for ref in REFERENCES:
        f_ent, f_attr, t_ent, t_attr, label, card = ref
        ft = tables[f_ent]  # from (FK 所在的表)
        tt = tables[t_ent]  # to   (PK 所在的表)

        # FK 列在 from 表右侧 → 连线起点
        fy = ft.find_attr_y(f_attr)
        fx = ft.right

        # PK 列在 to 表左侧 → 连线终点
        ty = tt.find_attr_y(t_attr)
        tx = tt.x

        # 生成正交折线路径
        gap = abs(fx - tx) * 0.2
        mid_x = fx + gap if fx < tx else fx - gap

        if abs(fy - ty) < 20:
            # 几乎水平 → 简单直连
            d = f"M {fx} {fy} L {tx} {ty}"
        else:
            d = (f"M {fx} {fy} "
                 f"L {fx + 12} {fy} "
                 f"L {fx + 12} {ty} "
                 f"L {tx} {ty}")

        # 根据 cardinality 决定端点样式
        tag(f'<path d="{d}" fill="none" stroke="{REF_COLOR}" stroke-width="1.5"/>')

        # 基数标注 — 在折线转角处
        lx = fx + 20 if fx < tx else fx - 20
        ly = (fy + ty) / 2
        tag(f'<text x="{lx}" y="{ly - 6}" text-anchor="middle" font-size="10"'
            f' fill="{CARD_COLOR}" font-weight="bold" font-family="{FONT}">{card}</text>')
        tag(f'<text x="{lx}" y="{ly + 10}" text-anchor="middle" font-size="10"'
            f' fill="{LABEL_COLOR}" font-family="{FONT}">{label}</text>')

    # ============================================================
    # 画实体表
    # ============================================================
    for name, tb in tables.items():
        x, y = tb.x, tb.y
        w, h = tb.w, tb.h

        # 外框圆角矩形
        tag(f'<rect x="{x}" y="{y}" width="{w}" height="{h}" rx="5" ry="5" '
            f'fill="{TABLE_BG}" stroke="{TABLE_BORDER}" stroke-width="1.5"/>')

        # 表头
        head_lines = tb.label.split("\n")
        head_h_total = HEADER_H if len(head_lines) == 1 else HEADER_H + 14
        tag(f'<rect x="{x}" y="{y}" width="{w}" height="{head_h_total}" rx="5" ry="5" '
            f'fill="{HEADER_BG}" stroke="{TABLE_BORDER}" stroke-width="1.5"/>')
        tag(f'<rect x="{x}" y="{y + head_h_total - 5}" width="{w}" height="6" fill="{HEADER_BG}"/>')

        # 表头文字
        for li, line in enumerate(head_lines):
            ty2 = y + HEADER_H/2 + (li - (len(head_lines)-1)/2) * 14
            tag(f'<text x="{x + w/2}" y="{ty2 + 4}" text-anchor="middle" font-size="{FONT_SIZE}" '
                f'font-weight="bold" fill="{HEADER_FG}" font-family="{FONT}">{line}</text>')

        # 主键分隔线
        sep_y = y + head_h_total
        tag(f'<line x1="{x}" y1="{sep_y}" x2="{x + w}" y2="{sep_y}" '
            f'stroke="{HEADER_BG}" stroke-width="2"/>')

        # 属性列
        pk_count = 0
        for i, (aname, atype, tag_str) in enumerate(tb.attrs):
            row_y = tb.col_y0 + i * ROW_H

            # 行背景
            is_pk = "PK" in tag_str
            is_fk = "FK" in tag_str or "U" in tag_str
            row_bg = "none"
            if is_pk: row_bg = PK_BG
            elif is_fk: row_bg = FK_BG

            if row_bg != "none":
                tag(f'<rect x="{x + 2}" y="{row_y - 1}" width="{w - 4}" '
                    f'height="{ROW_H}" fill="{row_bg}" rx="1"/>')

            # 列名 + 类型
            type_str = f"{aname}  <tspan fill='#888' font-size='{SMALL_FONT}'>{atype}</tspan>"
            tx_x = x + 8
            if is_pk:
                tag(f'<text x="{tx_x}" y="{row_y + ROW_H - 6}" font-size="{SMALL_FONT}" '
                    f'fill="{TEXT_COLOR}" font-family="{MONO_FONT}" font-weight="bold">'
                    f'{aname}  <tspan fill="#888" font-weight="normal">{atype}</tspan>  '
                    f'<tspan fill="#2B579A">[PK]</tspan></text>')
            elif is_fk:
                tag(f'<text x="{tx_x}" y="{row_y + ROW_H - 6}" font-size="{SMALL_FONT}" '
                    f'fill="{TEXT_COLOR}" font-family="{MONO_FONT}">'
                    f'{aname}  <tspan fill="#888">{atype}</tspan>  '
                    f'<tspan fill="#E67E22">[FK]</tspan></text>')
            else:
                tag(f'<text x="{tx_x}" y="{row_y + ROW_H - 6}" font-size="{SMALL_FONT}" '
                    f'fill="{TEXT_COLOR}" font-family="{MONO_FONT}">'
                    f'{aname}  <tspan fill="#888">{atype}</tspan></text>')

    # ============================================================
    # 图例
    # ============================================================
    lx, ly = 30, 870
    tag(f'<rect x="{lx}" y="{ly}" width="700" height="78" fill="white" stroke="#CCC" rx="5"/>')
    tag(f'<text x="{lx + 20}" y="{ly + 22}" font-size="13" font-weight="bold" fill="#333" '
        f'font-family="{FONT}">图例 Legend</text>')

    items = [
        (lx+20,  ly+36, PK_BG, "#2B579A", "[PK] 主键 Primary Key"),
        (lx+180, ly+36, FK_BG, "#E67E22", "[FK] 外键 Foreign Key"),
        (lx+340, ly+36, "none", "none",   "── 引用线 Reference Line"),
        (lx+20,  ly+60, "none", "none",   "N:1  多对一  ·  1:1  一对一  ·  1:N  一对多"),
    ]
    for ix, iy, bg, fc, txt in items:
        if bg != "none":
            tag(f'<rect x="{ix}" y="{iy}" width="14" height="14" fill="{bg}" rx="1" '
                f'stroke="#CCC"/>')
        tag(f'<text x="{ix + 20}" y="{iy + 10}" font-size="{SMALL_FONT}" fill="#333" '
            f'font-family="{FONT}">{txt}</text>')

    tag('</svg>')
    return "\n".join(svg)


if __name__ == "__main__":
    svg = render()
    out_path = r"e:\ClaudeCode\tasks\graduation-management\ER图.svg"
    with open(out_path, "w", encoding="utf-8") as f:
        f.write(svg)
    print(f"[OK] 已生成: {out_path}")
    print(f"     大小: {len(svg):,} bytes")
    print(f"     风格: PowerDesigner PDM (深蓝表头 + 引用线 + 基数标注)")
