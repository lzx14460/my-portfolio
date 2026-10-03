# 智慧校园体育设施预约与健康管理平台

一个面向高校场景的前后端分离平台，打通体育设施预约与个人健康数据，形成“运动—数据—反馈—优化运动”的良性闭环。

## 技术栈

**后端**
- Spring Boot 2.7.0
- MyBatis
- MySQL 8.0.45
- Druid 连接池
- Maven 3.8.1
- JDK 17

**前端**
- Vue.js 3.5.1
- Element-UI 2.3.2
- Vite

## 功能模块

### 用户端
- 注册、登录、修改密码、个人信息管理
- 浏览体育设施，按类型筛选，查看开放时间与收费标准
- 在线预约场馆，系统自动检测时间冲突
- 查看历史预约、取消预约
- 记录运动数据与健康数据，查看 BMI 趋势
- AI 生成个性化健康报告
- 查看系统公告

### 管理端
- 场馆增删改查、开放时间与收费标准设置
- 查看全部预约、手动取消订单
- 现场签到核验
- 用户管理、禁用/启用账号
- 审批场馆管理员 / 系统管理员申请
- 数据看板：场馆使用率、预约趋势、时段利用率
- 公告发布与管理

## 数据库

数据库名：`smart_campus_sports`，共 8 张表：

| 表名 | 说明 |
|------|------|
| user | 用户表 |
| sports_facility | 体育设施表 |
| reservation | 预约记录表 |
| payment_record | 支付记录表 |
| health_record | 健康数据记录表 |
| sport_record | 运动记录表 |
| announcement | 公告表 |
| system_config | 系统配置表 |

## 快速开始

### 环境要求
- JDK 17
- Maven 3.8+
- MySQL 8.0+
- Node.js 18+

### 1. 准备数据库


mysql -u root -p -e "CREATE DATABASE smart_campus_sports DEFAULT CHARACTER SET utf8mb4;"
mysql -u root -p smart_campus_sports < sql/smart_campus_sports.sql

2. 启动后端
修改 src/main/resources/application.yml 里的数据库账号密码：


spring:
  datasource:
    username: 你的数据库用户名
    password: 你的数据库密码
在项目根目录运行：


mvn spring-boot:run
后端地址：http://localhost:8080/api


### 配置 AI 健康报告（可选）

系统的 AI 健康报告功能依赖硅基流动（SiliconFlow）的 API Key。如需使用该功能：

1. 访问 [硅基流动官网](https://cloud.siliconflow.cn) 注册账号
2. 在控制台创建 API Key
3. 打开 `src/main/java/com/example/system/controller/AiController.java`
4. 找到这一行：

private static final String API_URL = "https://api.siliconflow.cn/v1/chat/completions";
private static final String API_KEY = "your-api-key-here";  // 替换成你自己的硅基流动 API Key
private static final String MODEL = "Qwen/Qwen2.5-7B-Instruct";


3. 启动前端

cd frontend
npm install
npm run dev
前端地址：http://localhost:5173

4. 默认账号
角色	用户名	密码
系统管理员	admin	123456
场馆管理员	manager	123456
学生	student	123456
如果数据库脚本中没有初始账号，请删除此表格。

接口概览
所有接口统一前缀 /api，主要分组：

模块	路径前缀
用户	/api/user
设施	/api/facility
预约	/api/reservation
健康数据	/api/health
运动记录	/api/sport-record
AI 报告	/api/ai
数据看板	/api/dashboard
项目亮点
预约冲突检测：同一设施同一时段不可重复预约

角色申请与审批：注册时可申请管理员，审批通过后自动提权

健康数据闭环：预约记录自动转化为运动数据

AI 健康报告：调用失败时自动降级为本地建议,在此强调，Ai模型以及其APIKEY需要自己贡献，