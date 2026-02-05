# ADR-001: Arquitectura Mini Core Bancario

| Metadata       | Valor                          |
|----------------|--------------------------------|
| **Estado**     | Aceptado                       |
| **Fecha**      | 2025-02-04                     |
| **Autor**      | Wilmar Dario Garcia Valderrama |

---

## 1. Contexto

Se requiere diseñar e implementar una API RESTful que simule las funcionalidades esenciales de un núcleo bancario. El sistema debe manejar:

- Gestión de cuentas bancarias
- Transferencias entre cuentas con atomicidad transaccional
- Historial de transacciones
- Simulación de notificaciones de eventos

### 1.1 Restricciones

- **Tiempo**: Tiempo muy actodado de desarrollo
- **Equipo**: 1 desarrollador
- **Ambiente**: Desarrollo local con base de datos en memoria

### 1.2 Requerimientos No Funcionales

- Código production-ready
- Arquitectura limpia y mantenible
- Cobertura de tests para lógica crítica
- Documentación OpenAPI

---

## 2. Decisión

### 2.1 Patrón Arquitectónico: **Hexagonal Architecture (Ports & Adapters)**

```
┌─────────────────────────────────────────────────────────────────────────┐
│                           INFRASTRUCTURE                                 │
│                                                                         │
│   ┌─────────────────┐                           ┌─────────────────┐    │
│   │   REST API      │                           │   JPA/H2        │    │
│   │   (Driving      │                           │   (Driven       │    │
│   │    Adapter)     │                           │    Adapter)     │    │
│   └────────┬────────┘                           └────────┬────────┘    │
│            │                                             │              │
│            ▼                                             ▼              │
│   ┌─────────────────┐                           ┌─────────────────┐    │
│   │   Input Port    │                           │   Output Port   │    │
│   │   (Use Case     │                           │   (Repository   │    │
│   │    Interface)   │                           │    Interface)   │    │
│   └────────┬────────┘                           └────────┬────────┘    │
│            │                                             │              │
│            │         ┌───────────────────┐              │              │
│            └────────►│                   │◄─────────────┘              │
│                      │      DOMAIN       │                             │
│                      │                   │                             │
│                      │  - Aggregates     │                             │
│                      │  - Value Objects  │                             │
│                      │  - Domain Events  │                             │
│                      │  - Policies       │                             │
│                      │  - Services       │                             │
│                      └───────────────────┘                             │
└─────────────────────────────────────────────────────────────────────────┘
```

### 2.2 Justificación

| Criterio                | Hexagonal | Layered | MVC Simple |
|-------------------------|-----|------|------|
| Testabilidad dominio    | Alta | Media | Baja |
| Desacoplamiento         | Alto | Medio | Bajo |
| Estándar Fintech        | Sí  | Parcial| No   |
| Complejidad inicial     | Media | Baja | Baja |
| Mantenibilidad          | Alta | Media | Baja |

**Elegimos Hexagonal porque**:
1. En mi experiencia este es el estándar en la industria financiera (Bancolombia, Banco de Bogotá, BBVA, Nubank)
2. Permite testear el dominio sin frameworks
3. Facilita cambios de infraestructura sin afectar lógica de negocio

---

## 3. Modelo de Dominio

### 3.1 Diagrama de Entidades

```
┌─────────────────────────────────────────────────────────────────┐
│                        BOUNDED CONTEXT                          │
│                      "Banking Operations"                       │
│                                                                 │
│  ┌─────────────────────────────┐    ┌─────────────────────────┐│
│  │      <<Aggregate Root>>     │    │       <<Entity>>        ││
│  │          Account            │    │      Transaction        ││
│  │  ─────────────────────────  │    │  ─────────────────────  ││
│  │  - id: AccountId [VO]       │    │  - id: TransactionId    ││
│  │  - accountNumber: AcctNum   │◄───│  - sourceAccount: AcctId││
│  │  - ownerName: String        │    │  - targetAccount: AcctId││
│  │  - balance: Money [VO]      │    │  - amount: Money [VO]   ││
│  │  - status: AccountStatus    │    │  - type: TxType [ENUM]  ││
│  │  - createdAt: LocalDateTime │    │  - description: String  ││
│  │  ─────────────────────────  │    │  - createdAt: DateTime  ││
│  │  + debit(Money): Result     │    │  ─────────────────────  ││
│  │  + credit(Money): void      │    │  + isDebit(): boolean   ││
│  │  + hasSufficientFunds(): bool│   │  + isCredit(): boolean  ││
│  └─────────────────────────────┘    └─────────────────────────┘│
│                                                                 │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │                    <<Value Objects>>                      │  │
│  │  ┌────────────┐  ┌────────────┐  ┌────────────────────┐  │  │
│  │  │ AccountId  │  │   Money    │  │   AccountNumber    │  │  │
│  │  │ ────────── │  │ ────────── │  │ ────────────────── │  │  │
│  │  │ -value:UUID│  │ -amount:BD │  │ -value: String     │  │  │
│  │  │            │  │ -currency  │  │ [10-20 digits]     │  │  │
│  │  └────────────┘  └────────────┘  └────────────────────┘  │  │
│  │  ┌────────────────┐  ┌─────────────────┐                 │  │
│  │  │ TransactionId  │  │ AccountStatus   │                 │  │
│  │  │ ────────────── │  │ ─────────────── │                 │  │
│  │  │ -value: UUID   │  │ ACTIVE/INACTIVE │                 │  │
│  │  └────────────────┘  └─────────────────┘                 │  │
│  └──────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
```

### 3.2 Value Objects - Especificación

| Value Object    | Validaciones                                      | Inmutable |
|-----------------|---------------------------------------------------|-----------|
| `AccountId`     | UUID válido, no nulo                              | Sí        |
| `AccountNumber` | 10-20 dígitos numéricos                           | Sí        |
| `Money`         | Amount >= 0, Currency = COP, Precision = 2        | Sí        |
| `TransactionId` | UUID válido, no nulo                              | Sí        |

### 3.3 Aggregate Rules

**Account (Aggregate Root)**:
- Solo `Account` puede modificar su `balance`
- El balance nunca puede ser negativo
- Todas las operaciones de débito/crédito pasan por métodos del agregado

---

## 4. Domain Events

```
┌─────────────────────────────────────────────────────────────────┐
│                       DOMAIN EVENTS                             │
│                                                                 │
│  ┌─────────────────────────┐  ┌─────────────────────────────┐  │
│  │   AccountCreatedEvent   │  │   TransferCompletedEvent    │  │
│  │   ─────────────────────│  │   ───────────────────────── │  │
│  │   - accountId           │  │   - transactionId           │  │
│  │   - accountNumber       │  │   - sourceAccountNumber     │  │
│  │   - ownerName           │  │   - targetAccountNumber     │  │
│  │   - initialBalance      │  │   - amount                  │  │
│  │   - occurredOn          │  │   - occurredOn              │  │
│  └─────────────────────────┘  └─────────────────────────────┘  │
│                                                                 │
│  ┌─────────────────────────┐                                   │
│  │   TransferFailedEvent   │  (Para logging/auditoría)         │
│  │   ─────────────────────│                                   │
│  │   - sourceAccountNumber │                                   │
│  │   - targetAccountNumber │                                   │
│  │   - amount              │                                   │
│  │   - reason              │                                   │
│  │   - occurredOn          │                                   │
│  └─────────────────────────┘                                   │
└─────────────────────────────────────────────────────────────────┘
```

---

## 5. Reglas de Negocio (Policies)

### 5.1 Transfer Policy

```
┌─────────────────────────────────────────────────────────────────┐
│                    TRANSFER VALIDATION CHAIN                    │
│                                                                 │
│  Request ──► [P1] ──► [P2] ──► [P3] ──► [P4] ──► [P5] ──► OK   │
│                │       │       │       │       │                │
│                ▼       ▼       ▼       ▼       ▼                │
│              FAIL    FAIL    FAIL    FAIL    FAIL              │
│                                                                 │
│  [P1] Amount > 0           "El monto debe ser mayor a cero"    │
│  [P2] Source ≠ Target      "No puede transferir a sí mismo"    │
│  [P3] Source exists        "Cuenta origen no encontrada"       │
│  [P4] Target exists        "Cuenta destino no encontrada"      │
│  [P5] Source.balance ≥ amt "Fondos insuficientes"              │
└─────────────────────────────────────────────────────────────────┘
```

### 5.2 Tabla de Reglas de Negocio

| ID   | Regla                          | Código Error | HTTP  | Mensaje                                      |
|------|--------------------------------|--------------|-------|----------------------------------------------|
| BR01 | Monto > 0                      | `INVALID_AMOUNT` | 400 | "El monto debe ser mayor a cero"             |
| BR02 | Cuenta origen ≠ destino        | `SELF_TRANSFER` | 400 | "No se permite transferir a la misma cuenta" |
| BR03 | Cuenta origen existe           | `SOURCE_NOT_FOUND` | 404 | "Cuenta origen no encontrada"                |
| BR04 | Cuenta destino existe          | `TARGET_NOT_FOUND` | 404 | "Cuenta destino no encontrada"               |
| BR05 | Saldo suficiente               | `INSUFFICIENT_FUNDS` | 422 | "Fondos insuficientes"                       |
| BR06 | Saldo resultante ≥ 0           | `OVERDRAFT_NOT_ALLOWED` | 422 | "Sobregiro no permitido"                     |

---

## 6. Flujo de Transferencia

### 6.1 Diagrama de Secuencia

```
┌──────┐    ┌──────────┐    ┌───────────┐    ┌────────┐    ┌──────┐    ┌────────┐
│Client│    │Controller│    │TransferSvc│    │ Domain │    │ Repo │    │EventPub│
└──┬───┘    └────┬─────┘    └─────┬─────┘    └───┬────┘    └──┬───┘    └───┬────┘
   │             │                │              │            │            │
   │ POST /transfer               │              │            │            │
   │ ──────────► │                │              │            │            │
   │             │                │              │            │            │
   │             │ execute(cmd)   │              │            │            │
   │             │ ─────────────► │              │            │            │
   │             │                │              │            │            │
   │             │                │ ┌──────────────────────────────────┐   │
   │             │                │ │      @Transactional              │   │
   │             │                │ │                                  │   │
   │             │                │ │ findById(source)                 │   │
   │             │                │ │ ────────────────────────────────►│   │
   │             │                │ │              sourceAccount       │   │
   │             │                │ │ ◄────────────────────────────────│   │
   │             │                │ │                                  │   │
   │             │                │ │ findById(target)                 │   │
   │             │                │ │ ────────────────────────────────►│   │
   │             │                │ │              targetAccount       │   │
   │             │                │ │ ◄────────────────────────────────│   │
   │             │                │ │                                  │   │
   │             │                │ │ validatePolicies()               │   │
   │             │                │ │ ───────────────►│                │   │
   │             │                │ │      Result<OK> │                │   │
   │             │                │ │ ◄───────────────│                │   │
   │             │                │ │                                  │   │
   │             │                │ │ source.debit()  │                │   │
   │             │                │ │ ───────────────►│                │   │
   │             │                │ │                 │                │   │
   │             │                │ │ target.credit() │                │   │
   │             │                │ │ ───────────────►│                │   │
   │             │                │ │                                  │   │
   │             │                │ │ save(source, target, tx)         │   │
   │             │                │ │ ────────────────────────────────►│   │
   │             │                │ │                                  │   │
   │             │                │ └──────────────────────────────────┘   │
   │             │                │                                        │
   │             │                │  publishEvent(TransferCompleted) ─────►│
   │             │                │            [ASYNC - No afecta TX]      │
   │             │                │                                        │
   │             │   Result<OK>   │                                        │
   │             │ ◄───────────── │                                        │
   │             │                │                                        │
   │  200 OK     │                │                                        │
   │ ◄────────── │                │                                        │
```

### 6.2 Manejo de Notificación (Resiliente)

```
┌─────────────────────────────────────────────────────────────────┐
│                  NOTIFICATION RESILIENCE                        │
│                                                                 │
│   ┌─────────────────────────────────────────────────────────┐  │
│   │                   @Transactional                         │  │
│   │  1. Debit source account                                 │  │
│   │  2. Credit target account                                │  │
│   │  3. Save transaction                                     │  │
│   │  4. COMMIT ─────────────────────────────────────────┐    │  │
│   └─────────────────────────────────────────────────────│────┘  │
│                                                         │       │
│   ┌─────────────────────────────────────────────────────▼────┐  │
│   │            @TransactionalEventListener                   │  │
│   │            (phase = AFTER_COMMIT)                        │  │
│   │                                                          │  │
│   │   try {                                                  │  │
│   │       notificationPort.notify(event);  // Simulated LOG  │  │
│   │   } catch (Exception e) {                                │  │
│   │       log.error("Notification failed", e);  // NO ROLLBACK│ │
│   │   }                                                      │  │
│   └──────────────────────────────────────────────────────────┘  │
│                                                                 │
│   Si notificación falla → Transacción YA fue committed          │
│   Error se registra en log → No afecta respuesta al cliente     │
└─────────────────────────────────────────────────────────────────┘
```

---

## 7. Contratos API (OpenAPI)

### 7.1 Endpoints

| Método | Endpoint                          | Descripción                    | Request Body         | Response          |
|--------|-----------------------------------|--------------------------------|----------------------|-------------------|
| POST   | `/api/v1/accounts`                | Crear cuenta                   | CreateAccountRequest | AccountResponse   |
| GET    | `/api/v1/accounts/{accountNumber}`| Consultar cuenta               | -                    | AccountResponse   |
| POST   | `/api/v1/transactions/transfer`   | Transferir fondos              | TransferRequest      | TransactionResponse|
| GET    | `/api/v1/accounts/{accountNumber}/transactions` | Historial | -                    | List<TransactionResponse> |

### 7.2 Códigos de Respuesta

| Código | Significado           | Cuándo usar                                    |
|--------|-----------------------|------------------------------------------------|
| 200    | OK                    | GET exitoso                                    |
| 201    | Created               | POST que crea recurso exitosamente             |
| 400    | Bad Request           | Validación fallida, monto inválido, self-transfer |
| 404    | Not Found             | Cuenta no existe                               |
| 422    | Unprocessable Entity  | Regla de negocio violada (fondos insuficientes)|
| 500    | Internal Server Error | Error no controlado                            |

### 7.3 Estructura de Error

```json
{
  "timestamp": "2025-02-04T10:30:00Z",
  "status": 422,
  "error": "Unprocessable Entity",
  "code": "INSUFFICIENT_FUNDS",
  "message": "Fondos insuficientes. Saldo disponible: 500.00 COP, monto solicitado: 1000.00 COP",
  "path": "/api/v1/transactions/transfer"
}
```

---

## 8. Estrategia de Testing

### 8.1 Pirámide de Tests

```
                    ┌───────────┐
                    │   E2E     │  ← Opcional (Postman/Newman)
                    │   Tests   │
                   ┌┴───────────┴┐
                   │ Integration │  ← Controllers + Repos
                   │    Tests    │
                  ┌┴─────────────┴┐
                  │    Unit       │  ← Domain + Application
                  │    Tests      │  ← FOCO PRINCIPAL
                 ┌┴───────────────┴┐
```

### 8.2 Tests Críticos

1. **Money Value Object**: Operaciones aritméticas, validaciones
2. **Account.debit()**: Fondos suficientes/insuficientes
3. **TransferPolicy**: Todas las reglas de negocio
4. **TransferApplicationService**: Flujo completo con mocks

---

## 9. Consideraciones de Seguridad

| Aspecto                | Implementación                                    |
|------------------------|---------------------------------------------------|
| Validación de entrada  | Bean Validation en DTOs + validación en dominio   |
| SQL Injection          | Prevenido por JPA/Hibernate (prepared statements) |
| Sensitive data logging | No loguear datos sensibles de cuentas             |
| Error messages         | No exponer detalles internos en errores 500       |

---

## 10. Consecuencias

### Positivas
- Dominio completamente desacoplado de frameworks
- Tests unitarios rápidos y sin dependencias
- Fácil de extender con nuevos adapters
- Código expresivo y autodocumentado

### Negativas
- Más archivos que una arquitectura simple
- Curva de aprendizaje para desarrolladores junior
- Mapeos adicionales entre capas

### Riesgos Mitigados
- El patrón Result evita excepciones no controladas
- Los Value Objects previenen estados inválidos
- La atomicidad transaccional garantiza consistencia

---
