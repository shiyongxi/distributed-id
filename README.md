# distributed-id

```text

分布式ID服务，目前提供基于数据库的ID生成方案，采用步长+本地缓存，单节点QPS 1w+（虚拟机压测结果）。

distributed-id-server可作为独立服务部署，distributed-id-core，应用方自行访问数据库，避免跨系统调用，提高整体可用性。

```