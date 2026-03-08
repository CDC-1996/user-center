#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
插入Java面试题目数据
"""
import pymysql

# 数据库连接配置
conn = pymysql.connect(
    host='localhost',
    port=3306,
    user='root',
    password='root123456',
    database='user_center',
    charset='utf8mb4'
)

# 题目数据
questions = [
    # Java集合框架 (course_id = 1)
    {
        'course_id': 1,
        'title': '说说 Java 中 HashMap 的原理？',
        'content': '请详细描述HashMap的底层实现原理，包括数据结构、put/get流程、扩容机制等。',
        'answer': '''## HashMap底层原理

### 1. 数据结构
- **JDK 1.8之前**：数组 + 链表
- **JDK 1.8之后**：数组 + 链表 + 红黑树

### 2. 核心字段
```java
Node<K,V>[] table;  // 哈希桶数组
int size;           // 元素个数
int threshold;      // 扩容阈值 = capacity * loadFactor
float loadFactor;   // 负载因子，默认0.75
```

### 3. put流程
1. 计算key的hash值
2. 定位数组索引：(n - 1) & hash
3. 如果该位置为空，直接插入
4. 如果有冲突，遍历链表/红黑树
5. 如果key已存在，覆盖value
6. 否则插入新节点
7. 插入后检查是否需要扩容

### 4. 扩容机制
- 当size > threshold时触发扩容
- 扩容为原来的2倍
- 重新计算每个元素的位置

### 5. 红黑树转换
- 链表长度 >= 8 且数组长度 >= 64时，链表转红黑树
- 红黑树节点数 <= 6时，红黑树转链表''',
        'analysis': '''## 核心要点

1. **为什么负载因子是0.75？**
   - 权衡空间和时间效率
   - 太大会增加冲突概率
   - 太小会浪费空间

2. **为什么JDK1.8要引入红黑树？**
   - 链表过长时查询效率O(n)
   - 红黑树查询效率O(log n)

3. **线程安全问题**
   - HashMap非线程安全
   - 多线程使用ConcurrentHashMap''',
        'difficulty': 2,
        'tags': 'HashMap,集合,数据结构'
    },
    {
        'course_id': 1,
        'title': 'Java 中 ConcurrentHashMap 1.7 和 1.8 有哪些区别？',
        'content': '对比ConcurrentHashMap在JDK 1.7和1.8中的实现差异。',
        'answer': '''## ConcurrentHashMap 1.7 vs 1.8

### JDK 1.7 实现
- 分段锁（Segment继承ReentrantLock）
- 默认16个Segment
- 锁粒度：Segment级别

### JDK 1.8 实现
- CAS + synchronized
- 锁粒度：Node级别（更细）
- 取消Segment，直接操作Node数组
- 引入红黑树优化查询

### 主要区别

| 特性 | JDK 1.7 | JDK 1.8 |
|------|---------|---------|
| 数据结构 | Segment + HashEntry | Node + 链表/红黑树 |
| 锁机制 | Segment分段锁 | CAS + synchronized |
| 锁粒度 | Segment级别 | Node级别 |
| 并发度 | 固定 | 动态 |''',
        'analysis': '''## 为什么1.8要大改？

1. **锁粒度更细**：从Segment到Node，并发度更高
2. **CAS+synchronized**：synchronized在JDK1.6后优化很多
3. **红黑树优化**：解决链表过长问题
4. **简化实现**：去掉复杂的Segment结构''',
        'difficulty': 3,
        'tags': 'ConcurrentHashMap,并发,集合'
    },
    {
        'course_id': 1,
        'title': 'Java 中有哪些集合类？请简单介绍',
        'content': '介绍Java集合框架的整体架构和常用集合类。',
        'answer': '''## Java集合框架

### List集合（有序、可重复）
- **ArrayList**：数组实现，查询快，增删慢
- **LinkedList**：双向链表，增删快，查询慢
- **Vector**：线程安全，已过时

### Set集合（无序、不可重复）
- **HashSet**：基于HashMap，无序，效率最高
- **LinkedHashSet**：保持插入顺序
- **TreeSet**：红黑树实现，可排序

### Map集合（键值对）
- **HashMap**：最常用，非线程安全
- **LinkedHashMap**：保持插入顺序
- **TreeMap**：可排序
- **ConcurrentHashMap**：线程安全''',
        'analysis': '''## 选择建议

1. **单线程环境**：ArrayList、HashMap
2. **频繁增删**：LinkedList
3. **需要排序**：TreeSet、TreeMap
4. **保持顺序**：LinkedHashSet、LinkedHashMap
5. **多线程环境**：ConcurrentHashMap''',
        'difficulty': 1,
        'tags': '集合,ArrayList,HashMap'
    },
    {
        'course_id': 1,
        'title': 'ArrayList 扩容机制是怎样的？',
        'content': '详细介绍ArrayList的扩容原理和过程。',
        'answer': '''## ArrayList扩容机制

### 扩容时机
当添加元素时，如果 size == elementData.length，触发扩容。

### 扩容规则
- 默认扩容50%：newCapacity = oldCapacity + (oldCapacity >> 1)
- 使用Arrays.copyOf()创建新数组

### 初始容量
- new ArrayList()：初始容量0，第一次add变为10
- new ArrayList(int)：指定初始容量

### 优化建议
已知元素数量时，指定初始容量，避免多次扩容：
```java
List<String> list = new ArrayList<>(1000);
```''',
        'analysis': '''## 关键点

1. **扩容代价大**：需要创建新数组并复制
2. **默认增长50%**：相对合理的增长率
3. **指定容量**：已知大小时预分配''',
        'difficulty': 2,
        'tags': 'ArrayList,集合,扩容'
    },
    # Java线程基础 (course_id = 4)
    {
        'course_id': 4,
        'title': '线程和进程的区别是什么？',
        'content': '请详细说明线程和进程的区别，以及它们之间的关系。',
        'answer': '''## 进程 vs 线程

### 定义
- **进程**：操作系统分配资源的基本单位，拥有独立的内存空间
- **线程**：CPU调度的基本单位，共享进程的资源

### 主要区别

| 特性 | 进程 | 线程 |
|------|------|------|
| 资源分配 | 独立内存空间 | 共享进程资源 |
| 开销 | 大 | 小 |
| 通信 | IPC | 共享内存 |
| 稳定性 | 高 | 低 |

### Java中的体现
- Java程序运行在JVM进程中
- main()方法在主线程中执行
- 可以创建多个线程并发执行''',
        'analysis': '''## 核心要点

1. 进程是资源分配单位，线程是调度单位
2. 线程共享堆内存，拥有独立的栈
3. Java多线程适合I/O密集型任务''',
        'difficulty': 1,
        'tags': '进程,线程,基础'
    },
    {
        'course_id': 4,
        'title': '创建线程有哪几种方式？',
        'content': '介绍Java中创建线程的几种方式及其优缺点。',
        'answer': '''## 创建线程的方式

### 1. 继承Thread类
```java
public class MyThread extends Thread {
    public void run() {
        System.out.println("线程执行");
    }
}
```
缺点：Java单继承，无法继承其他类

### 2. 实现Runnable接口
```java
public class MyRunnable implements Runnable {
    public void run() {
        System.out.println("线程执行");
    }
}
```
优点：可以继承其他类，更灵活

### 3. 实现Callable接口
优点：可以返回结果、抛出异常

### 4. 线程池
```java
ExecutorService executor = Executors.newFixedThreadPool(10);
executor.submit(() -> { /* 任务 */ });
```
优点：复用线程，控制并发数''',
        'analysis': '''## 推荐

生产环境使用线程池，避免频繁创建销毁线程。''',
        'difficulty': 2,
        'tags': '线程,Thread,Runnable'
    },
    # Java锁机制 (course_id = 5)
    {
        'course_id': 5,
        'title': 'synchronized 的原理是什么？',
        'content': '详细说明synchronized的实现原理和使用方式。',
        'answer': '''## synchronized原理

### 使用方式
1. 同步代码块：synchronized (lock) { }
2. 同步实例方法：public synchronized void method() {}
3. 同步静态方法：public static synchronized void method() {}

### 底层原理
- 同步代码块：monitorenter/monitorexit指令
- 同步方法：ACC_SYNCHRONIZED标志

### 锁升级过程（JDK 1.6+）
无锁 -> 偏向锁 -> 轻量级锁 -> 重量级锁

- **偏向锁**：单线程重入
- **轻量级锁**：交替执行，CAS自旋
- **重量级锁**：竞争激烈，阻塞等待''',
        'analysis': '''## 核心要点

1. synchronized是可重入锁
2. JDK 1.6后进行了大量优化
3. 锁只能升级，不能降级''',
        'difficulty': 3,
        'tags': 'synchronized,锁,Monitor'
    },
    {
        'course_id': 5,
        'title': 'synchronized 和 ReentrantLock 的区别？',
        'content': '对比这两种锁的异同点和使用场景。',
        'answer': '''## synchronized vs ReentrantLock

### 主要区别

| 特性 | synchronized | ReentrantLock |
|------|--------------|---------------|
| 实现方式 | JVM层面 | Java API |
| 锁获取/释放 | 自动 | 手动 |
| 公平性 | 非公平 | 可选 |
| 条件变量 | 单一条件 | 多条件 |
| 可中断 | 不可 | 可以 |

### 选择建议
- **简单场景**：使用synchronized
- **需要高级功能**：使用ReentrantLock
- **公平性要求**：使用ReentrantLock''',
        'analysis': '''## 最佳实践

1. ReentrantLock必须在finally中释放
2. 避免死锁：不要嵌套锁
3. 优先使用synchronized，代码更简洁''',
        'difficulty': 2,
        'tags': 'synchronized,ReentrantLock,锁'
    },
    # JVM内存模型 (course_id = 8)
    {
        'course_id': 8,
        'title': 'JVM内存模型是怎样的？',
        'content': '介绍JVM运行时数据区的各个部分。',
        'answer': '''## JVM内存模型

### 运行时数据区

| 区域 | 作用 | 线程 | 异常 |
|------|------|------|------|
| 堆 | 存储对象实例 | 共享 | OOM |
| 方法区 | 类信息、常量 | 共享 | OOM |
| 虚拟机栈 | 方法调用栈帧 | 私有 | StackOverflow |
| 本地方法栈 | Native方法 | 私有 | StackOverflow |
| 程序计数器 | 执行位置 | 私有 | 无 |

### 堆内存结构
- 新生代（1/3）：Eden + Survivor0 + Survivor1
- 老年代（2/3）

### 方法区变化
- JDK 1.7：永久代（PermGen）
- JDK 1.8：元空间（Metaspace）''',
        'analysis': '''## 核心要点

1. 堆是GC主要区域
2. 栈深度有限，递归需注意
3. JDK 1.8后方法区改为元空间''',
        'difficulty': 2,
        'tags': 'JVM,内存模型,堆,栈'
    },
    {
        'course_id': 8,
        'title': 'JVM垃圾回收算法有哪些？',
        'content': '介绍常见的垃圾回收算法及其原理。',
        'answer': '''## 垃圾回收算法

### 1. 标记-清除（Mark-Sweep）
- 标记存活对象，清除未标记对象
- 缺点：效率不高，产生内存碎片

### 2. 复制算法（Copying）
- 内存分两块，存活对象复制到另一块
- 优点：无碎片
- 应用：新生代

### 3. 标记-整理（Mark-Compact）
- 标记存活对象，向一端移动
- 优点：无碎片
- 应用：老年代

### 4. 分代收集
- 新生代：复制算法
- 老年代：标记-清除/标记-整理''',
        'analysis': '''## 核心要点

1. 没有最好的算法，只有最适合的
2. G1是现代主流选择
3. ZGC适合大内存、低延迟场景''',
        'difficulty': 2,
        'tags': 'JVM,GC,垃圾回收'
    },
    # MySQL索引优化 (course_id = 10)
    {
        'course_id': 10,
        'title': 'MySQL索引类型有哪些？',
        'content': '介绍MySQL支持的索引类型及其特点。',
        'answer': '''## MySQL索引类型

### 按数据结构分类
- **B+Tree索引**：默认类型，范围查询高效
- **Hash索引**：等值查询快，Memory引擎
- **全文索引**：文本搜索

### 按逻辑分类
- **主键索引**：唯一、非空
- **唯一索引**：值唯一，可为空
- **普通索引**：基本索引
- **组合索引**：多列索引
- **覆盖索引**：查询列都在索引中

### B+Tree特点
1. 非叶子节点只存索引
2. 叶子节点存储数据
3. 叶子节点用指针连接
4. 树高度低（通常3层）''',
        'analysis': '''## 核心要点

1. InnoDB默认B+Tree索引
2. 主键使用聚簇索引
3. 组合索引注意最左前缀''',
        'difficulty': 2,
        'tags': 'MySQL,索引,B+Tree'
    },
    {
        'course_id': 10,
        'title': 'MySQL索引的最左前缀匹配原则是什么？',
        'content': '解释最左前缀原则及其在查询优化中的应用。',
        'answer': '''## 最左前缀匹配原则

### 定义
组合索引按照定义顺序，从左到右依次匹配查询条件。

### 示例索引
CREATE INDEX idx_name ON user(name, age, city);

### 匹配情况
- WHERE name = ? ✅ 使用第一列
- WHERE name = ? AND age = ? ✅ 使用前两列
- WHERE age = ? ❌ 跳过第一列
- WHERE name = ? AND age > ? AND city = ? ⚠️ 范围查询后失效

### 范围查询影响
遇到范围查询（>、<、between），后面的索引列无法使用。

### 优化建议
1. 最常用的列放最左边
2. 等值查询在前，范围查询在后
3. 避免跳过中间列''',
        'analysis': '''## 核心要点

1. 索引按定义顺序匹配
2. 范围查询后索引失效
3. 索引下推优化了跳跃场景''',
        'difficulty': 3,
        'tags': 'MySQL,索引,最左前缀'
    },
    {
        'course_id': 10,
        'title': '什么是MySQL的覆盖索引？',
        'content': '解释覆盖索引的概念和优势。',
        'answer': '''## 覆盖索引

### 定义
查询的所有列都在索引中，不需要回表查询数据行。

### 示例
```sql
-- 索引
CREATE INDEX idx_name_age ON user(name, age);

-- 覆盖索引查询（EXPLAIN显示Using index）
SELECT name, age FROM user WHERE name = 'Tom';
```

### 优势
1. 减少IO：不需要回表
2. 提高速度：直接从索引获取数据
3. 减少锁竞争

### 使用场景
1. 统计查询
2. 只查询索引列
3. 联合索引优化''',
        'analysis': '''## 核心要点

1. 覆盖索引避免回表，提高性能
2. EXPLAIN中显示Using index
3. 尽量让查询使用覆盖索引''',
        'difficulty': 2,
        'tags': 'MySQL,索引,覆盖索引'
    }
]

try:
    cursor = conn.cursor()
    
    for q in questions:
        sql = '''
        INSERT INTO question (course_id, title, content, answer, analysis, difficulty, tags)
        VALUES (%s, %s, %s, %s, %s, %s, %s)
        '''
        cursor.execute(sql, (
            q['course_id'],
            q['title'],
            q['content'],
            q['answer'],
            q['analysis'],
            q['difficulty'],
            q['tags']
        ))
    
    conn.commit()
    print(f"成功插入 {len(questions)} 条题目数据")
    
    # 更新课程题目数量
    cursor.execute('''
        UPDATE course c SET question_count = (
            SELECT COUNT(*) FROM question q WHERE q.course_id = c.id
        )
    ''')
    conn.commit()
    print("课程题目数量已更新")
    
finally:
    conn.close()
