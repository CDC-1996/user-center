-- Java面试题数据初始化脚本
-- 创建时间: 2026-03-08

USE user_center;

-- ========================
-- Java集合框架题目
-- ========================
INSERT INTO `question` (`course_id`, `title`, `content`, `answer`, `analysis`, `difficulty`, `tags`, `sort`) VALUES
(1, '说说 Java 中 HashMap 的原理？',
'请详细描述HashMap的底层实现原理，包括数据结构、put/get流程、扩容机制等。',
'## HashMap底层原理

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
1. 计算key的hash值：`(h = key.hashCode()) ^ (h >>> 16)`
2. 定位数组索引：`(n - 1) & hash`
3. 如果该位置为空，直接插入
4. 如果有冲突，遍历链表/红黑树
5. 如果key已存在，覆盖value
6. 否则插入新节点
7. 插入后检查是否需要扩容

### 4. 扩容机制
- 当size > threshold时触发扩容
- 扩容为原来的2倍
- 重新计算每个元素的位置（高位/低位分流）

### 5. 红黑树转换
- 链表长度 >= 8 且数组长度 >= 64时，链表转红黑树
- 红黑树节点数 <= 6时，红黑树转链表',
'## 核心要点

1. **为什么负载因子是0.75？**
   - 权衡空间和时间效率
   - 太大会增加冲突概率
   - 太小会浪费空间

2. **为什么JDK1.8要引入红黑树？**
   - 链表过长时查询效率O(n)
   - 红黑树查询效率O(log n)

3. **线程安全问题**
   - HashMap非线程安全
   - 多线程使用ConcurrentHashMap',
2, 'HashMap,集合,数据结构', 1),

(1, 'Java 中 ConcurrentHashMap 1.7 和 1.8 有哪些区别？',
'对比ConcurrentHashMap在JDK 1.7和1.8中的实现差异。',
'## ConcurrentHashMap 1.7 vs 1.8

### JDK 1.7 实现

**数据结构**：
```
Segment[] -> HashEntry[]
```

**核心机制**：
- 分段锁（Segment继承ReentrantLock）
- 默认16个Segment
- 每个Segment管理一个HashEntry数组
- 锁粒度：Segment级别

### JDK 1.8 实现

**数据结构**：
```
Node[] + 链表/红黑树
```

**核心机制**：
- CAS + synchronized
- 锁粒度：Node级别（更细）
- 取消Segment，直接操作Node数组

### 主要区别对比

| 特性 | JDK 1.7 | JDK 1.8 |
|------|---------|---------|
| 数据结构 | Segment + HashEntry | Node + 链表/红黑树 |
| 锁机制 | Segment分段锁 | CAS + synchronized |
| 锁粒度 | Segment级别 | Node级别 |
| 并发度 | 固定（Segment数量） | 动态（Node数量） |
| 查询效率 | O(n)链表 | O(log n)红黑树 |

### 1.8 put流程
1. 计算hash，定位数组位置
2. 如果为空，CAS插入
3. 如果在扩容，帮助扩容
4. 否则synchronized锁定头节点
5. 遍历链表/红黑树插入/更新
6. 检查是否需要树化',
'## 为什么1.8要大改？

1. **锁粒度更细**：从Segment到Node，并发度更高
2. **CAS+synchronized**：synchronized在JDK1.6后优化很多
3. **红黑树优化**：解决链表过长问题
4. **简化实现**：去掉复杂的Segment结构',
3, 'ConcurrentHashMap,并发,集合', 2),

(1, 'Java 中有哪些集合类？请简单介绍',
'介绍Java集合框架的整体架构和常用集合类。',
'## Java集合框架

### 整体架构
```
Collection
├── List（有序、可重复）
│   ├── ArrayList
│   ├── LinkedList
│   └── Vector
├── Set（无序、不可重复）
│   ├── HashSet
│   ├── LinkedHashSet
│   └── TreeSet
└── Queue（队列）
    ├── LinkedList
    ├── PriorityQueue
    └── ArrayDeque

Map（键值对）
├── HashMap
├── LinkedHashMap
├── TreeMap
├── ConcurrentHashMap
└── Hashtable
```

### List集合

| 集合 | 底层结构 | 线程安全 | 特点 |
|------|----------|----------|------|
| ArrayList | 数组 | 否 | 查询快，增删慢 |
| LinkedList | 双向链表 | 否 | 增删快，查询慢 |
| Vector | 数组 | 是 | 已过时，用CopyOnWriteArrayList |

### Set集合

| 集合 | 底层结构 | 特点 |
|------|----------|------|
| HashSet | HashMap | 无序，效率最高 |
| LinkedHashSet | LinkedHashMap | 保持插入顺序 |
| TreeSet | 红黑树 | 自然排序 |

### Map集合

| 集合 | 底层结构 | 线程安全 | 特点 |
|------|----------|----------|------|
| HashMap | 数组+链表+红黑树 | 否 | 最常用 |
| LinkedHashMap | HashMap+双向链表 | 否 | 有序 |
| TreeMap | 红黑树 | 否 | 可排序 |
| ConcurrentHashMap | 数组+链表+红黑树 | 是 | 并发安全 |
| Hashtable | 数组+链表 | 是 | 已过时 |',
'## 选择建议

1. **单线程环境**：ArrayList、HashMap
2. **频繁增删**：LinkedList
3. **需要排序**：TreeSet、TreeMap
4. **保持顺序**：LinkedHashSet、LinkedHashMap
5. **多线程环境**：ConcurrentHashMap、CopyOnWriteArrayList',
1, '集合,ArrayList,HashMap', 3),

(1, 'ArrayList 扩容机制是怎样的？',
'详细介绍ArrayList的扩容原理和过程。',
'## ArrayList扩容机制

### 核心属性
```java
transient Object[] elementData; // 存储元素的数组
private int size;               // 实际元素个数
```

### 扩容时机
当添加元素时，如果 size == elementData.length，触发扩容。

### 扩容流程

**1. 确定新容量**
```java
// 默认扩容50%
int newCapacity = oldCapacity + (oldCapacity >> 1);

// 如果新容量小于最小需求，使用最小需求
if (newCapacity - minCapacity < 0)
    newCapacity = minCapacity;
```

**2. 创建新数组**
```java
elementData = Arrays.copyOf(elementData, newCapacity);
```

### 初始容量

| 构造方法 | 初始容量 |
|----------|----------|
| new ArrayList() | 0（第一次add变为10）|
| new ArrayList(int) | 指定值 |
| new ArrayList(Collection) | 集合大小 |

### 扩容示例
```
初始：容量10，size=0
添加11个元素：
  1. 前10个元素正常添加
  2. 第11个元素触发扩容
  3. 新容量 = 10 + 5 = 15
  4. 复制原数组到新数组
```

### 优化建议
```java
// 已知元素数量时，指定初始容量
List<String> list = new ArrayList<>(1000);

// 避免多次扩容带来的性能损耗
```',
'## 关键点

1. **扩容代价大**：需要创建新数组并复制
2. **默认增长50%**：oldCapacity + oldCapacity >> 1
3. **指定容量**：已知大小时预分配，避免多次扩容',
2, 'ArrayList,集合,扩容', 4),

(1, 'HashMap 和 Hashtable 的区别？',
'对比HashMap和Hashtable的差异。',
'## HashMap vs Hashtable

### 主要区别

| 特性 | HashMap | Hashtable |
|------|---------|-----------|
| 线程安全 | 否 | 是 |
| null键/值 | 允许 | 不允许 |
| 迭代器 | fail-fast | enumerator |
| 初始容量 | 16 | 11 |
| 扩容方式 | 2倍 | 2倍+1 |
| 哈希算法 | 异或高低位 | 直接取模 |
| 效率 | 高 | 低 |
| 出现版本 | JDK 1.2 | JDK 1.0 |

### 线程安全
```java
// Hashtable：方法加synchronized
public synchronized V put(K key, V value);

// HashMap：无同步
public V put(K key, V value);
```

### null处理
```java
// HashMap：允许null
map.put(null, null); // OK

// Hashtable：抛出NullPointerException
table.put(null, null); // NPE
```

### 为什么Hashtable被废弃？
1. 全表锁，效率低
2. 设计老旧，不符合现代需求
3. 替代方案：ConcurrentHashMap',
'## 使用建议

1. **单线程**：使用HashMap
2. **多线程**：使用ConcurrentHashMap
3. **不推荐**：Hashtable（已过时）',
1, 'HashMap,Hashtable,集合', 5);

-- 更新课程题目数量
UPDATE `course` SET `question_count` = (SELECT COUNT(*) FROM `question` WHERE `question`.`course_id` = `course`.`id`);
