# Event-Driven Crypto Trading Engine

A modular, event-driven trading engine designed for strategy execution across
multiple market data and order execution protocols.

The project focuses on clean architecture, testability, and realistic
algorithmic trading workflows.

## Key Features
- Event-driven market data ingestion
- Pluggable order execution layer
- Strategy layer reacting to real-time market events
- Test-data builders for clean and expressive integration tests
- Clear separation of market data, execution, and strategy logic

## Architecture Overview
- Market Data Layer (protocol-agnostic)
- Order Execution Layer
- Strategy Layer
- Domain Models & DTOs
- Integration Tests (JUnit 5)

## Tech Stack
- Java 21
- Jackson
- JUnit 5
- Log4j2

## Notes
Current implementations use exchange APIs for market data and order execution.
The architecture is designed to support multiple protocols (e.g. REST, WebSocket, FIX).
API keys are not included in the repository.
