# Destino: Service Discovery and Job Scheduling

[![License](https://img.shields.io/badge/license-Apache%202-blue.svg)](https://www.apache.org/licenses/LICENSE-2.0.html)
[![GitHub release (latest by date)](https://img.shields.io/github/v/release/Egolessness/destino?style=flat-square)](https://github.com/Egolessness/destino)

English | [简体中文](./README-zh.md)

README：

- [Destino: Service Discovery and Job Scheduling](#Destino:-Service-Discovery-and-Job-Scheduling)
  - [Introduction](#introduction)
  - [How to install](#how-to-install)
  - [Philosophy](#Philosophy)
  - [Contact](#Contact)
  - [License](#License)

## Introduction

Destino is a distributed system for service discovery, service management and job scheduling.

**Functions**:

- service management: service registration, service discovery and health management
- job scheduling: scheduler setting, execution logs and balanced scheduling

**Features**:

- One-stop service, registration and scheduling center
- Communication flexibility, Restful API or gRPC
- Cluster self-arbitration, not relying on any external system
- Cluster self-discovery, forming clusters based on multicast
- Multi-mode registration, AP/CP mode service registration
- Bottleneck-free scheduling, negotiating plans and balancing execution

## How to install

**Requirements**

- Install requirement: *JDK 1.8+*
- Compile requirement: *JDK 1.8+* and Gradle *8.0+*

**Download the binary package**

Download the package from the [latest stable release](https://github.com/Egolessness/destino/releases).

**Start server**

> sh startup.sh -m cluster

## Philosophy

- Ultimate elegant design, minimalistic operation
- The goal is to replace all middleware and provide a complete one-stop microservice solution
- Currently, it is developed independently by an individual, and new members are welcome to join.

## Contact

Email to zsmjwk@outlook.com

## License

Destino is licensed under the Apache License 2.0. Destino relies on some third-party components, and their open source protocol is Apache License 2.0 OR MIT.
