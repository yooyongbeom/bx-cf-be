# BX-CF-BE



## 서비스 실행순서

- discovery -> auth -> gateway

## 테스트 및 접속 url

- 유레카 : http://localhost:18761/
- h2 DB 콘솔 : http://localhost:18082/auth/h2-console
               JDBC URL : jdbc:h2:mem:auth-db
- login : http://localhost:18081/channel/backend/api/v1/auth/login
- 성공여부 테스트 : http://localhost:18081/te/home
- Refresh 요청 : http://localhost:18081/channel/backend/api/v1/auth/refresh-token

## 프로젝트 구조, 인증/인가 프로세스 설명은 다음 문서에 있음
- [ ] 웹앱 프레임워크_구축 v0.5 2025.09.19.pptx

## 로컬에서 postman 테스트용 collection
- [ ] BX-CF-BE-LCL.postman_collection

## 로그인 및 토큰발급 테스트 시나리오 
1. http://localhost:18081/channel/backend/api/v1/auth/login (로그인)
```
request body
{
      "usrId" : "아이디"
      ,"usrPwd: "패스워드(sha-256)"
}
```
```
response body (예)
-- success/code/msg(공통부), payload(데이터부)로 구성
-- payload 확인
{
     "success": true,            -- 성공/실패 true:false
      "code": "0",               -- 성공일 경우 0, 에러일경우 - (음수)
      "msg": "success",          -- 성공/실패 메시지
      "payload": {
         "usrId": "yongbeom.yoo",
         "usrNm": "유용범",
         "positDivName": "수석프로",
         "deptName": "Channel Unit",
         "usrPwd": null,
         "accessToken": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ5b25nYmVvbS55b28iLCJyb2xlcyI6WyJST0xFX1VTRVIiLCJST0xFX0FETUlOIl0sImlhdCI6MTc1ODg3MTQ5MSwiZXhwIjoxNzU4ODcxNTUxfQ.yoZpG2ZxgvulHqG6YuUArTtbmXmv8shNEnVGX1CIL4A",
         "accessTokenExpiresAt": "20250926162551",
         "refreshToken": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ5b25nYmVvbS55b28iLCJpYXQiOjE3NTg4NzE0OTEsImV4cCI6MTc1OTQ3NjI5MX0.yny3OPFFk6L1aChbBC5Zja7XaAylv9wVK3BAoWP1_jA",
         "refreshTokenExpiresAt": "20251003162451",
         "roles": [
            "ROLE_USER",
            "ROLE_ADMIN"
         ]
}
```

2. http://localhost:18081/te/home (로그인성공했는지 체크)
```
request header
{
   'Authorization': 'Bearer ' + 발급받은토큰,               -- 반드시 Bearer 패턴 쓸것
    ,'Content-Type': 'application/x-www-form-urlencoded'    -- 테스트 요청응답이 html로 되어있어 지정함
}
request body
{
      
}
```
```
response body (html)
<!DOCTYPE html>
<html>

<head>
	<meta charset="UTF-8">
	<title>Login Success</title>
</head>

<body>
	<h2>로그인 성공</h2>
</body>

</html>
```

3. http://localhost:18081/channel/backend/api/v1/auth/refresh-token (리프레쉬)
```
{
    "refreshToken": 리프레쉬 토큰
}
```
```
response body (예)
-- 성공할경우 1번과 같은 형식으로 옴
{
     "success": true,            -- 성공/실패 true:false
      "code": "0",               -- 성공일 경우 0, 에러일경우 - (음수)
      "msg": "success",          -- 성공/실패 메시지
      "payload": {
         "usrId": "yongbeom.yoo",
         "usrNm": "유용범",
         "positDivName": "수석프로",
         "deptName": "Channel Unit",
         "usrPwd": null,
         "accessToken": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ5b25nYmVvbS55b28iLCJyb2xlcyI6WyJST0xFX1VTRVIiLCJST0xFX0FETUlOIl0sImlhdCI6MTc1ODg3MTQ5MSwiZXhwIjoxNzU4ODcxNTUxfQ.yoZpG2ZxgvulHqG6YuUArTtbmXmv8shNEnVGX1CIL4A",
         "accessTokenExpiresAt": "20250926162551",
         "refreshToken": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ5b25nYmVvbS55b28iLCJpYXQiOjE3NTg4NzE0OTEsImV4cCI6MTc1OTQ3NjI5MX0.yny3OPFFk6L1aChbBC5Zja7XaAylv9wVK3BAoWP1_jA",
         "refreshTokenExpiresAt": "20251003162451",
         "roles": [
            "ROLE_USER",
            "ROLE_ADMIN"
         ]
}
```

## JAR 실행 예)
- java -jar -Dspring.profiles.active=local -Dfile.encoding=UTF-8 discovery-svc-0.0.1-SNAPSHOT.jar

## front에서의 인증/인가 요청 흐름
1. 클라이언트는 API 요청 시 항상 Access Token을 사용
2. 필터가 Access Token을 검증
3. 만약 Access Token이 만료되었다면, 서버는 "토큰이 만료되었다"는 특정 에러 코드를 클라이언트에게 반환
4. 클라이언트는 이 특정 에러 코드를 받으면, 가지고 있던 Refresh Token으로 토큰 재발급을 요청하는 API(예: POST /auth/refresh)를 호출
5. 서버는 Refresh Token을 검증하고, 유효하다면 새로운 Access Token(과 새로운 Refresh Token)을 발급하여 클라이언트에게 전달
6. 클라이언트는 새로 받은 토큰으로 기존에 실패했던 API 요청을 다시 시도