
# 🚀 SysWatch

> 轻量级运维监控与告警系统  
> 基于 Spring Boot 构建，支持实时日志推送与告警记录管理。

---

## 📌 项目简介

**SysWatch** 是一个面向运维场景的监控与告警系统，  
用于监控系统资源、规则变更与服务状态，并通过 WebSocket 实现实时日志推送。

系统支持告警日志持久化、分页查询、多环境配置及 Prometheus 集成，  
适用于中小型监控平台或个人运维平台实践。

---

## ✨ 核心特性

- 🔍 资源与规则变更监控  
- 📄 告警日志持久化存储  
- ⚡ WebSocket 实时日志推送  
- 📊 告警日志分页查询  
- 🧩 Prometheus 指标集成  
- 🛠 多环境配置（local / prod）  

---

## 🏗 技术栈
```
| 技术 | 版本 |
|------|------|
| Java | 21 |
| Spring Boot | 3.2.x |
| MyBatis-Plus | 最新稳定版 |
| WebSocket | Spring WebSocket |
| MySQL | 8.x |
| Jackson | JSON 序列化 |
| Lombok | 简化开发 |
```
---

## 📂 项目结构

```text
src/main/java/org/xiaomo/syswatch
├── config          # 配置类
├── controller      # 接口层
├── service         # 业务逻辑层
├── mapper          # 数据访问层
├── domain          # 实体类
├── handler         # WebSocket 处理器
```

---

## 🚀 快速启动

### 1️⃣ 克隆项目

```bash
git clone https://github.com/xiaomohub/syswatch.git
cd syswatch
```

---

### 2️⃣ 配置数据库

在 `application.yml` 或 `application-local.yml` 中配置：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/syswatch
    username: root
    password: 123456
```

---

### 3️⃣ Prometheus 配置（可选）

```yaml
prometheus:
  url: http://localhost:9090
```

---

### 4️⃣ WebSocket 配置

```yaml
websocket:
  allowed-origins:
    - "*"
```

---

### 5️⃣ 启动项目

```bash
mvn clean package
java -jar target/syswatch-*.jar
```

默认端口：`8080`

---

## 🔌 WebSocket 实时推送

**连接地址：**

```
ws://localhost:8080/ws/alert
```

系统在记录告警日志后，会自动向在线客户端广播日志信息。
推送内容已自动过滤大字段（contentBefore / contentAfter）。

---

## 📄 告警日志接口

**分页查询日志：**

```
GET /api/logs?pageNum=1&pageSize=10
```

返回数据默认不包含大字段内容，以提升查询性能。

---

## 🐳 Docker 部署（可选）

### 构建镜像

```bash
docker build -t syswatch .
```

### 运行容器

```bash
docker run -p 8080:8080 syswatch
```

---

## 📈 设计说明

* 日志写入数据库后同步通过 WebSocket 推送
* 分页查询裁剪大字段优化性能
* 支持多环境配置隔离
* 可扩展为分布式推送架构（结合 MQ）

---

## 🤝 贡献指南

1. Fork 本仓库
2. 创建功能分支 `feature/xxx`
3. 提交代码
4. 发起 Pull Request

欢迎 Issue 与建议。

---

## 📜 License

MIT License

```
```
