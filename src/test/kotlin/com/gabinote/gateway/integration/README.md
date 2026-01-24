# Gateway Integration Tests

## 공통 사항

api 구분

1. prefixApi: prefix 가 적용된 api로 prefix 포함 테스트하고 싶을 때 사용
2. noPrefixApi: prefix 없는 api로 prefix 제외 테스트하고 싶을 때 사용

## 테스트 영역 구분

1. baseTest: 인증 옵션 등 기타 옵션 없이 공통 필터만 테스트
2. authTest: 인증 관련 필터 테스트