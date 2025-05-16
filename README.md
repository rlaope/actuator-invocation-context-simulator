Spring Boot Actuator에서 `InvocationContext.resolveArgument()` 메서드는 사용자 정의 엔드포인트를 구현할 때 간접적으로 활용될 수 있습니다. 이 메서드는 엔드포인트 실행 시 필요한 인자를 동적으로 해결하는 데 사용됩니다.


###  `InvocationContext.resolveArgument()`의 역할

`InvocationContext`는 Actuator 엔드포인트 실행 시 컨텍스트 정보를 제공하는 클래스입니다. `resolveArgument(Class<T> argumentType)` 메서드는 등록된 `OperationArgumentResolver`를 통해 특정 타입의 인자를 찾아 반환합니다. 만약 해당 타입의 인자를 찾을 수 없다면 `null`을 반환합니다.


###  Kotlin에서의 사용 예시 및 문제점

Kotlin에서 `resolveArgument()`를 사용할 때, 반환값이 `null`일 수 있음에도 불구하고 컴파일러는 이를 인식하지 못합니다. 이는 `@Nullable` 어노테이션이 누락되어 있기 때문입니다.

예를 들어, 다음과 같은 Kotlin 코드를 고려해보겠습니다:

```kotlin
val context: InvocationContext = ...
val userId = context.resolveArgument(UserId::class.java)
val id = userId.value  // 컴파일러는 null 가능성을 인식하지 못함
```



위 코드에서 `resolveArgument()`가 `null`을 반환할 경우, `userId.value`에서 `NullPointerException`이 발생할 수 있습니다. 그러나 컴파일러는 이를 경고하지 않으며, 개발자는 런타임 오류를 경험할 수 있습니다.


### 해결 방안: `@Nullable` 어노테이션 추가

`resolveArgument()` 메서드에 `@Nullable` 어노테이션을 추가하면, Kotlin 컴파일러는 반환값이 `null`일 수 있음을 인식하게 됩니다. 이를 통해 개발자는 안전하게 null 처리를 할 수 있습니다.

예시:

```kotlin
val context: InvocationContext = ...
val userId = context.resolveArgument(UserId::class.java)
val id = userId?.value ?: defaultId  // 안전한 null 처리
```



이렇게 하면 `userId`가 `null`일 경우에도 `defaultId`를 사용하여 안전하게 처리할 수 있습니다.


### 결론

* `InvocationContext.resolveArgument()`는 `null`을 반환할 수 있으므로, `@Nullable` 어노테이션을 추가하는 것이 바람직합니다.
* Kotlin에서의 null-safety를 보장하고, 런타임 오류를 예방할 수 있습니다.
* Spring 프로젝트의 일관성과 Kotlin 사용자 경험 향상을 위해 해당 어노테이션 추가를 고려해야 합니다.


## Example

### 1. **사용자 정의 Actuator 엔드포인트 구현 시**

**상황:** 내부 운영 툴에서 시스템 캐시 상태를 실시간으로 확인하거나 재설정할 수 있는 기능을 만들고자 할 때.

### 예시:

```java
@Endpoint(id = "cache-control")
public class CacheEndpoint {

    @ReadOperation
    public String cacheStatus(InvocationContext context) {
        UserSession session = context.resolveArgument(UserSession.class);  // ✅ 여기서 사용
        if (session == null || !session.isAdmin()) {
            return "Access Denied";
        }
        return "Cache valid";
    }
}
```

🔎 **실제 사용된 이유**: 요청자가 `UserSession` 타입으로 전달된 정보를 가졌는지 판단해서, 보안 체크에 활용.


### 2. **외부 서비스 통합용 엔드포인트 제공**

**상황:** 외부 관리자 시스템이 우리 서버의 상태를 주기적으로 확인하거나 명령을 내릴 수 있도록 엔드포인트를 제공.

### 예시:

```java
@WriteOperation
public String restartService(InvocationContext context) {
    String serviceName = context.resolveArgument(String.class); // 예: body param or query param
    if (serviceName == null) {
        return "Missing service name";
    }
    // 실행 로직...
    return "Restarted " + serviceName;
}
```

🔎 **실제 사용된 이유**: `resolveArgument()`를 통해 HTTP 요청으로 전달된 값을 자동 추출하여 처리.


### 3. **운영 지표용 커스텀 메트릭 엔드포인트 구현**

**상황:** 서비스 내 특정 유저 활동, 로그 상태, 실패 건수 등 특정 운영 지표를 수집해서 운영팀에게 제공하고 싶을 때.

### 예시:

```java
@ReadOperation
public Map<String, Any> customHealth(InvocationContext context) {
    FeatureFlag flag = context.resolveArgument(FeatureFlag.class);  // ✅ 특정 feature 상태 필터링
    return monitoringService.getFilteredMetrics(flag);
}
```

🔎 **실제 사용된 이유**: 요청 파라미터에 따라 엔드포인트 출력 내용을 동적으로 제어하기 위해.


### 4. **Kotlin 기반의 DSL 보안/운영 설정 구현 시 내부 재사용**

**상황:** Kotlin 기반 DSL에서 엔드포인트 확장을 구성하고, `resolveArgument`를 활용하여 DSL 내부 컨텍스트를 다룰 때

```kotlin
fun EndpointDsl.customFilter() {
    operation { ctx ->
        val traceId = ctx.resolveArgument(TraceId::class.java)  // DSL 내 인자 주입
        ...
    }
}
```


### 정리

| 사용 시점               | 설명                                               |
| ------------------- | ------------------------------------------------ |
| 운영팀/관리자용 커스텀 API 구현 | 사용자 요청에 따라 런타임 시 동적으로 파라미터 resolve               |
| 보안 정책 적용            | `UserSession`, `Token`, `FeatureFlag` 등 동적 객체 확인 |
| 외부 연동 제어 인터페이스      | actuator-based webhook, 자동화 스크립트에서 사용            |
| 모니터링/지표 제공 엔드포인트    | 조건부 필터링, 요청자 기반 응답 변화                            |


