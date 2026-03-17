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

```java
Flow flow = makeComplexFlow();
Assert.isTrue(flow.validate(), "任务流初始化失败");

TestContext context = new TestContext();
FlowResult execute = flow.execute(context, Flow.TaskDirection.ROOT_FIRST);
Assert.isTrue(execute.isSuccess(), "任务执行失败");



private Flow makeComplexFlow() {
    Flow flow = new Flow("complex-flow");
    TestTask a = new TestTask("a");
    TestTask b = new TestTask("b");
    TestTask c = new TestTask("c");
    TestTask d = new TestTask("d");
    TestTask e = new TestTask("e");
    TestTask f = new TestTask("f");
    TestTask g = new TestTask("g");
    TestTask h = new TestTask("h");

    flow.addTask(a, b, c);
    flow.addTask(b, e, f, g);
    flow.addTask(c, d);
    flow.addTask(h);
    return flow;
}
```