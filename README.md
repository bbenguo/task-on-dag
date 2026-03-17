# task-on-dag - 基于有向无环图（DAG）封装任务框架

# 简介
基于 有向无环图（Directed Acyclic GraphDAG，简称DAG）实现的任务框架，它可以将多个有依赖的作业在线程池中并行执行，从而大幅提升系统计算效率。

# 使用方式
```xml
<dependency>
  <groupId>io.github.bbenguo</groupId>
  <artifactId>task-on-dag-core</artifactId>
  <version>1.0.0</version>
</dependency>
```