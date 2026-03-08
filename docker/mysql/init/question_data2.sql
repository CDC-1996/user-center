-- Java并发、JVM、MySQL题目数据
-- 创建时间: 2026-03-08

USE user_center;

-- ========================
-- Java线程基础题目
-- ========================
INSERT INTO `question` (`course_id`, `title`, `content`, `answer`, `analysis`, `difficulty`, `tags`, `sort`) VALUES
(4, '线程和进程的区别是什么？',
'请详细说明线程和进程的区别，以及它们之间的关系。',
'## 进程 vs 线程

### 定义
- **进程**：操作系统分配资源的基本单位，拥有独立的内存空间
- **线程**：CPU调度的基本单位，共享进程的资源

### 主要区别

| 特性 | 进程 | 线程 |
|------|------|------|
| 资源分配 | 独立内存空间 | 共享进程资源 |
| 开销 | 大（创建、切换）| 小 |
| 通信 | IPC（管道、消息队列等）| 共享内存 |
| 稳定性 | 高（相互独立）| 低（一个线程崩溃影响整个进程）|
| 并发性 | 多进程并发 | 多线程并发 |

### 线程优势
1. **资源开销小**：线程共享进程资源
2. **切换效率高**：不需要切换内存空间
3. **通信方便**：直接通过共享变量通信

### 进程优势
1. **稳定性高**：进程相互独立
2. **安全性好**：内存隔离
3. **适合多核**：充分利用多核CPU

### Java中的体现
- Java程序运行在JVM进程中
- main()方法在主线程中执行
- 可以创建多个线程并发执行',
'## 核心要点

1. 进程是资源分配单位，线程是调度单位
2. 线程共享堆内存，拥有独立的栈和程序计数器
3. Java多线程适合I/O密集型任务',
1, '进程,线程,基础', 1),

(4, '创建线程有哪几种方式？',
'介绍Java中创建线程的几种方式及其优缺点。',
'## 创建线程的方式

### 1. 继承Thread类
```java
public class MyThread extends Thread {
    @Override
    public void run() {
        System.out.println("线程执行");
    }
}

// 使用
new MyThread().start();
```
**缺点**：Java单继承，无法继承其他类

### 2. 实现Runnable接口
```java
public class MyRunnable implements Runnable {
    @Override
    public void run() {
        System.out.println("线程执行");
    }
}

// 使用
new Thread(new MyRunnable()).start();
```
**优点**：可以继承其他类，更灵活

### 3. 实现Callable接口
```java
public class MyCallable implements Callable<String> {
    @Override
    public String call() throws Exception {
        return "结果";
    }
}

// 使用
FutureTask<String> task = new FutureTask<>(new MyCallable());
new Thread(task).start();
String result = task.get(); // 获取返回值
```
**优点**：可以返回结果、抛出异常

### 4. 线程池
```java
ExecutorService executor = Executors.newFixedThreadPool(10);
executor.submit(() -> {
    System.out.println("线程执行");
});
executor.shutdown();
```
**优点**：复用线程，控制并发数，管理方便

### 推荐方式
**生产环境使用线程池**，避免频繁创建销毁线程。',
'## 对比总结

| 方式 | 返回值 | 异常 | 继承限制 |
|------|--------|------|----------|
| Thread | 无 | 无 | 单继承 |
| Runnable | 无 | 无 | 无 |
| Callable | 有 | 有 | 无 |
| 线程池 | 有 | 有 | 无 |',
2, '线程,Thread,Runnable', 2),

-- ========================
-- Java锁机制题目
-- ========================
(5, 'synchronized 的原理是什么？',
'详细说明synchronized的实现原理和使用方式。',
'## synchronized原理

### 使用方式
```java
// 1. 同步代码块
synchronized (lock) {
    // 临界区
}

// 2. 同步实例方法
public synchronized void method() {}

// 3. 同步静态方法
public static synchronized void staticMethod() {}
```

### 底层原理

**字节码层面**：
- 同步代码块：monitorenter/monitorexit指令
- 同步方法：ACC_SYNCHRONIZED标志

**Monitor机制**：
```
ObjectMonitor {
    _owner;        // 持有锁的线程
    _WaitSet;      // 调用wait()的线程
    _EntryList;    // 等待获取锁的线程
}
```

### 锁升级过程（JDK 1.6+）

```
无锁 -> 偏向锁 -> 轻量级锁 -> 重量级锁
```

| 锁状态 | 场景 | 性能 |
|--------|------|------|
| 无锁 | 无竞争 | 最高 |
| 偏向锁 | 单线程重入 | 高 |
| 轻量级锁 | 交替执行 | 中 |
| 重量级锁 | 竞争激烈 | 低 |

### 锁升级过程
1. **偏向锁**：线程首次获取锁时，在对象头记录线程ID
2. **轻量级锁**：有竞争时，通过CAS自旋获取锁
3. **重量级锁**：自旋失败，阻塞等待

### 对象头结构
```
| Mark Word (32位/64位) | Class Pointer | 实例数据 |
```
Mark Word存储锁状态、线程ID、年龄等信息。',
'## 核心要点

1. synchronized是可重入锁
2. JDK 1.6后进行了大量优化
3. 锁只能升级，不能降级
4. 退出synchronized块自动释放锁',
3, 'synchronized,锁,Monitor', 3),

(5, 'synchronized 和 ReentrantLock 的区别？',
'对比这两种锁的异同点和使用场景。',
'## synchronized vs ReentrantLock

### 主要区别

| 特性 | synchronized | ReentrantLock |
|------|--------------|---------------|
| 实现方式 | JVM层面 | Java API |
| 锁获取 | 自动 | 手动lock() |
| 锁释放 | 自动 | 手动unlock() |
| 公平性 | 非公平 | 可选公平/非公平 |
| 条件变量 | 单一条件 | 多条件 |
| 可中断 | 不可 | lockInterruptibly() |
| 尝试获取 | 无 | tryLock() |
| 性能 | JDK1.6后相当 | 高并发略优 |

### ReentrantLock使用
```java
ReentrantLock lock = new ReentrantLock();
try {
    lock.lock();
    // 临界区
} finally {
    lock.unlock(); // 必须手动释放
}
```

### 公平锁示例
```java
// 公平锁：按等待顺序获取
ReentrantLock fairLock = new ReentrantLock(true);
```

### 条件变量
```java
ReentrantLock lock = new ReentrantLock();
Condition condition = lock.newCondition();

// 等待
condition.await();

// 唤醒
condition.signal();
```

### 选择建议
- **简单场景**：使用synchronized
- **需要高级功能**：使用ReentrantLock
- **公平性要求**：使用ReentrantLock',
'## 最佳实践

1. ReentrantLock必须在finally中释放
2. 避免死锁：不要嵌套锁
3. 优先使用synchronized，代码更简洁',
2, 'synchronized,ReentrantLock,锁', 4),

-- ========================
-- JVM题目
-- ========================
(8, 'JVM内存模型是怎样的？',
'介绍JVM运行时数据区的各个部分。',
'## JVM内存模型

### 运行时数据区
```
┌─────────────────────────────────────┐
│            线程共享区域              │
├─────────────────┬───────────────────┤
│   方法区        │      堆           │
│ (Method Area)   │     (Heap)        │
├─────────────────┴───────────────────┤
│            线程私有区域              │
├─────────────────┬───────────────────┤
│    虚拟机栈      │    本地方法栈     │
│ (VM Stack)      │(Native Stack)    │
├─────────────────┴───────────────────┤
│           程序计数器                 │
│     (Program Counter)               │
└─────────────────────────────────────┘
```

### 各区域说明

| 区域 | 作用 | 线程 | 异常 |
|------|------|------|------|
| 堆 | 存储对象实例 | 共享 | OOM |
| 方法区 | 存储类信息、常量、静态变量 | 共享 | OOM |
| 虚拟机栈 | 方法调用栈帧 | 私有 | StackOverflowError/OOM |
| 本地方法栈 | Native方法调用 | 私有 | StackOverflowError/OOM |
| 程序计数器 | 记录执行位置 | 私有 | 无 |

### 堆内存结构（JDK 1.8）
```
Heap
├── 新生代 (1/3)
│   ├── Eden (8/10)
│   ├── Survivor0 (1/10)
│   └── Survivor1 (1/10)
└── 老年代 (2/3)
```

### 方法区变化
- **JDK 1.7**：永久代（PermGen）
- **JDK 1.8**：元空间（Metaspace），使用本地内存',
'## 核心要点

1. 堆是GC主要区域
2. 栈深度有限，递归需注意
3. JDK 1.8后方法区改为元空间
4. 程序计数器是唯一不会OOM的区域',
2, 'JVM,内存模型,堆,栈', 1),

(8, 'JVM垃圾回收算法有哪些？',
'介绍常见的垃圾回收算法及其原理。',
'## 垃圾回收算法

### 1. 标记-清除（Mark-Sweep）
```
标记阶段：标记所有存活对象
清除阶段：清除未标记对象
```
**缺点**：
- 效率不高（两次遍历）
- 产生内存碎片

### 2. 复制算法（Copying）
```
将内存分为两块，每次使用一块
存活对象复制到另一块，清空当前块
```
**优点**：无碎片
**缺点**：内存利用率低
**应用**：新生代（Eden:Survivor = 8:1）

### 3. 标记-整理（Mark-Compact）
```
标记存活对象
将存活对象向一端移动
清理边界外内存
```
**优点**：无碎片
**缺点**：移动成本高
**应用**：老年代

### 4. 分代收集
- **新生代**：复制算法（对象存活率低）
- **老年代**：标记-清除/标记-整理

### 常见垃圾收集器

| 收集器 | 类型 | 算法 | 特点 |
|--------|------|------|------|
| Serial | 新生代 | 复制 | 单线程，简单高效 |
| ParNew | 新生代 | 复制 | Serial多线程版 |
| Parallel Scavenge | 新生代 | 复制 | 吞吐量优先 |
| CMS | 老年代 | 标记-清除 | 低延迟 |
| G1 | 全堆 | 分区+复制 | 可预测停顿 |
| ZGC | 全堆 | 并发整理 | 低延迟（<10ms）|',
'## 核心要点

1. 没有最好的算法，只有最适合的
2. 新生代用复制，老年代用标记-整理
3. G1是现代主流选择
4. ZGC适合大内存、低延迟场景',
2, 'JVM,GC,垃圾回收', 2),

-- ========================
-- MySQL题目
-- ========================
(15, 'MySQL索引类型有哪些？',
'介绍MySQL支持的索引类型及其特点。',
'## MySQL索引类型

### 按数据结构分类

| 类型 | 特点 | 适用场景 |
|------|------|----------|
| B+Tree索引 | 默认类型，范围查询 | 大多数场景 |
| Hash索引 | 等值查询快，不支持范围 | Memory引擎 |
| 全文索引 | 文本搜索 | 文本检索 |
| 空间索引 | 地理位置数据 | GIS应用 |

### 按逻辑分类

| 类型 | 说明 |
|------|------|
| 主键索引 | 唯一、非空 |
| 唯一索引 | 值唯一，可为空 |
| 普通索引 | 基本索引 |
| 组合索引 | 多列索引 |
| 覆盖索引 | 查询列都在索引中 |

### B+Tree特点
```
1. 非叶子节点不存数据，只存索引
2. 叶子节点存储所有数据
3. 叶子节点用指针连接（范围查询高效）
4. 树高度低（通常3层）
```

### 索引创建
```sql
-- 普通索引
CREATE INDEX idx_name ON table(col);

-- 唯一索引
CREATE UNIQUE INDEX uk_name ON table(col);

-- 组合索引
CREATE INDEX idx_name ON table(col1, col2);

-- 全文索引
CREATE FULLTEXT INDEX ft_name ON table(col);
```

### 索引使用建议
1. 频繁查询的列建索引
2. 区分度高的列建索引
3. 组合索引遵循最左前缀
4. 避免过多索引（影响写入）',
'## 核心要点

1. InnoDB默认B+Tree索引
2. 主键使用聚簇索引
3. 辅助索引存储主键值
4. 组合索引注意最左前缀',
2, 'MySQL,索引,B+Tree', 1),

(15, 'MySQL索引的最左前缀匹配原则是什么？',
'解释最左前缀原则及其在查询优化中的应用。',
'## 最左前缀匹配原则

### 定义
组合索引按照定义顺序，从左到右依次匹配查询条件。

### 示例索引
```sql
CREATE INDEX idx_name ON user(name, age, city);
```

### 匹配情况

| SQL条件 | 是否使用索引 | 说明 |
|---------|-------------|------|
| WHERE name = ? | ✅ | 使用索引第一列 |
| WHERE name = ? AND age = ? | ✅ | 使用索引前两列 |
| WHERE name = ? AND age = ? AND city = ? | ✅ | 使用全部索引 |
| WHERE age = ? | ❌ | 跳过第一列 |
| WHERE name = ? AND city = ? | ⚠️ | 只用第一列，city用不上 |
| WHERE name = ? AND age > ? AND city = ? | ⚠️ | 范围查询后失效 |

### 范围查询影响
遇到范围查询（>、<、between、like前缀匹配），后面的索引列无法使用。

```sql
-- name用到索引，age用到索引，city用不到
WHERE name = 'Tom' AND age > 18 AND city = 'Beijing'
```

### 优化建议
1. **列顺序很重要**：最常用的列放最左边
2. **等值查询在前**：范围查询放后面
3. **避免跳跃**：不要跳过中间列
4. **考虑业务场景**：根据查询模式设计索引

### 索引下推（ICP）
MySQL 5.6后支持索引下推：
```sql
-- name用索引，city在索引层过滤（不回表）
WHERE name = 'Tom' AND city = 'Beijing'
```',
'## 核心要点

1. 索引按定义顺序匹配
2. 范围查询后索引失效
3. 索引下推优化了跳跃场景
4. 设计时考虑查询模式',
3, 'MySQL,索引,最左前缀', 2),

(15, '什么是MySQL的覆盖索引？',
'解释覆盖索引的概念和优势。',
'## 覆盖索引

### 定义
查询的所有列都在索引中，不需要回表查询数据行。

### 示例
```sql
-- 索引
CREATE INDEX idx_name_age ON user(name, age);

-- 覆盖索引查询
SELECT name, age FROM user WHERE name = 'Tom';
-- EXPLAIN结果：Using index
```

### 非覆盖索引
```sql
-- 需要回表（查询了id不在索引中）
SELECT id, name, age FROM user WHERE name = 'Tom';
```

### 执行流程对比

**非覆盖索引**：
```
1. 辅助索引查找name = 'Tom'
2. 获取主键id
3. 回表查询聚簇索引获取完整数据
```

**覆盖索引**：
```
1. 辅助索引直接返回name, age
2. 无需回表
```

### 优势
1. **减少IO**：不需要回表
2. **提高速度**：直接从索引获取数据
3. **减少锁竞争**：索引数据量小

### 使用场景
1. 统计查询：SELECT COUNT(*) FROM table WHERE ...
2. 只查询索引列：SELECT name FROM user WHERE ...
3. 联合索引优化

### 查看是否使用覆盖索引
```sql
EXPLAIN SELECT name, age FROM user WHERE name = 'Tom';
-- Extra: Using index
```',
'## 核心要点

1. 覆盖索引避免回表，提高性能
2. EXPLAIN中显示Using index
3. 尽量让查询使用覆盖索引
4. 合理设计组合索引',
2, 'MySQL,索引,覆盖索引', 3);

-- 更新课程题目数量
UPDATE `course` SET `question_count` = (SELECT COUNT(*) FROM `question` WHERE `question`.`course_id` = `course`.`id`);
