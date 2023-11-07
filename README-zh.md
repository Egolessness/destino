# Destino：服务发现和任务调度平台

[![License](https://img.shields.io/badge/license-Apache%202-blue.svg)](https://www.apache.org/licenses/LICENSE-2.0.html)
[![GitHub release (latest by date)](https://img.shields.io/github/v/release/Egolessness/destino?style=flat-square)](https://github.com/Egolessness/destino)

[English](./README.md) | 简体中文

说明：

- [Destino：服务发现和任务调度](#Destino：服务发现和任务调度平台)
  - [介绍](#介绍)
  - [安装](#安装)
  - [理念](#理念)
  - [交流](#交流)
  - [许可](#许可)

## 介绍

Destino是一个高可用的服务发现、服务管理和任务调度平台，可支撑海量服务端点。

**功能**：

- 服务管理：服务注册、服务发现、健康管理
- 任务调度：调度设定、调度日志、均衡计划

**亮点**：

- 一站式服务，注册中心与调度中心
- 通信可选性，Restful API 或 gRPC
- 集群自仲裁，不依赖任何外部系统
- 集群自发现，基于组播自形成集群
- 注册多模式，AP/CP模式服务注册
- 调度无瓶颈，协商计划并均衡执行

## 安装

**环境准备**

- 安装环境要求 *JDK 1.8+*
- 编译环境要求 *JDK 1.8+*, *Gradle 8.0 +*

**使用源码构建JAR**

将该项目clone至本地后，使用Gradle进行构建，执行以下命令：
> gradlew -P prod -P cluster shadowJar

**直接下载安装**

下载最新稳定版本的[安装包](https://github.com/Egolessness/destino/releases)

**启动服务**

> sh startup.sh -m cluster

## 理念

- 极致优雅的设计，最简化的操作
- 目标是替代所有中间件，提供完整的微服务一站式解决方案
- 目前是个人独立开发，欢迎新的成员加入

## 交流

如果有问题可以发邮件至 zsmjwk@outlook.com
<br />欢迎个人或企业，支持参与完成这个庞大的工程

## 许可

遵循 Apache License 2.0 许可