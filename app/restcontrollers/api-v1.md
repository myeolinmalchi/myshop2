# User

## Account

---
### `api/v1/user`

#### POST

일반 사용자 회원가입 및 유효성 검사

##### Request Header:
```json
{
  "Content-Type": "application/json",
  "Data-Type": "json"
}
```
##### Request Body:

```json
{
  "userId": "Your Account ID",
  "userPw": "Your Account Password",
  "name": "Your Name",
  "email": "Your email",
  "phonenumber": "Your Phone Number"
}
```

##### Response:

###### 201 Created
회원가입이 정상적으로 수행됨.


###### 422 Unprocessable Entity
회원 정보가 유효하지 않음.

###### 400 Bad Request
올바르지 않은 요청.

---

### `/api/v1/user/login`

#### POST
일반 사용자 로그인

##### Request:

```json
{
  "Content-Type": "application/json",
  "Data-Type": "json"
}
```
```json
{
  "userId": "Your Account ID",
  "userPw": "Your Account Password"
}
```

##### Response:

###### 200 Ok
로그인이 정상적으로 수행됨.

```json
{
  ...  
  "Authorization": "Your Json Web Token Here",
  ... 
}
```
```json
{
  //Body is Empty
}
```

###### 401 Unauthorized
아이디는 존재하나, 비밀번호가 일치하지 않음.


###### 404 Not Found
아이디가 존재하지 않음.

###### 400 Bad Request
올바르지 않은 요청.

---

### `api/v1/user/kakao`
카카오 회원가입

#### POST




## Carts


## Orders


# Product 

## Categories

## Search

# Seller

## Account

## Product

## Order
