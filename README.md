```markdown
# SysWatch

SysWatch 是一个基于 Spring Boot 构建的轻量级运维监控与告警系统，用于对系统资源、服务状态及规则变更进行监控与记录，并通过 WebSocket 实现实时日志推送。

---

## ✨ 项目特性

- 🔍 资源与规则变更监控
- 📄 告警日志持久化存储
- ⚡ WebSocket 实时推送告警日志
- 📊 告警日志分页查询
- 🧩 支持 Prometheus 集成
- 🛠 支持多环境配置（local / prod）

---

## 🏗 技术栈

- Java 21
- Spring Boot 3.2.x
- MyBatis-Plus
- WebSocket
- MySQL
- Jackson
- Lombok

---

## 📂 项目结构

```

src/main/java/org/xiaomo/syswatch
├── config          # 配置类
├── controller      # 接口控制层
├── service         # 业务逻辑层
├── mapper          # 数据访问层
├── domain          # 实体类
├── handler         # WebSocket 处理器

````

---

## 🚀 快速启动

### 1️⃣ 克隆项目

```bash
git clone https://github.com/xiaomohub/syswatch.git
cd syswatch
````

### 2️⃣ 配置数据库

在 `application.yml` 或 `application-local.yml` 中配置：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/syswatch
    username: root
    password: 123456
```

### 3️⃣ Prometheus 配置（可选）

```yaml
prometheus:
  url: http://localhost:9090
```

### 4️⃣ WebSocket 配置

```yaml
websocket:
  allowed-origins:
    - "*"
```

### 5️⃣ 运行项目

```bash
mvn clean package
java -jar target/syswatch-*.jar
```

默认端口：`8080`

---

## 🔌 WebSocket 使用

连接地址：

```
ws://localhost:8080/ws/alert
```

服务端在记录日志后会自动向所有在线客户端广播告警信息（已过滤大文本字段）。

---

## 📄 告警日志接口

获取分页日志：

```
GET /api/logs?pageNum=1&pageSize=10
```

返回数据不包含大字段内容（contentBefore / contentAfter），提升查询性能。

---

## 🐳 Docker 部署（可选）

构建镜像：

```bash
docker build -t syswatch .
```

运行容器：

```bash
docker run -p 8080:8080 syswatch
```

---

## 📈 设计说明

* 日志写入数据库后同步通过 WebSocket 推送
* 查询接口对大字段做裁剪优化性能
* 支持多环境配置隔离
* 支持后续扩展为分布式推送架构（结合 MQ）

---

## 🤝 贡献

欢迎提交 Issue 或 Pull Request 改进项目。

---

## 📜 License

MIT License

```
```
