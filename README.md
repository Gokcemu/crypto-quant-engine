# Event-Driven Crypto Trading Engine

A modular, event-driven crypto trading engine built on top of the Binance API.
The project focuses on clean architecture, testability, and realistic trading workflows.

## Key Features
- Real-time market data via Binance WebSocket
- Order placement & verification via Binance REST API
- Strategy layer reacting to market events
- Test-data builders for clean integration tests
- Clear separation of market data, order execution, and strategy logic

## Architecture Overview
- Market Data Layer (WebSocket + REST)
- Order Execution Layer
- Strategy Layer
- Domain Models & DTOs
- Integration Tests (JUnit 5)

## Tech Stack
- Java 21
- Binance REST & WebSocket APIs
- Jackson
- JUnit 5
- Log4j2

## Notes
This project uses Binance Testnet for order execution.
API keys are not included in the repository.
