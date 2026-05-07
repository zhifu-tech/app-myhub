你这个需求本质是：
👉 **KMP（Kotlin Multiplatform）项目做「系统账号登录」**

* Android → 系统 Google 账号登录
* iOS → Apple ID 登录
* 统一获取：

    * 用户名
    * 头像
    * 唯一ID

这是一个 **典型 KMP + 平台原生 SDK 集成场景**，必须用 **expect/actual + 平台 SDK** 实现。

下面给你一个 **可以直接落地的完整工程级教程**（包含架构设计 + 配置 + 代码 + 注意事项）。

---

# 一、整体架构设计（推荐企业级方案）

KMP 登录必须这样设计：

```
commonMain
   LoginManager (expect)
androidMain
   GoogleLoginManager (actual)
iosMain
   AppleLoginManager (actual)
```

统一返回：

```kotlin
data class UserInfo(
    val id: String,
    val name: String?,
    val avatar: String?,
    val email: String?
)
```

👉 重要原则：

KMP 不做登录
KMP 只做登录结果封装

因为：

* Google 登录 → 必须 Android SDK
* Apple 登录 → 必须 iOS SDK

---

# 二、Android 系统账号登录（Google）

本质：
👉 使用 **Google Play 服务登录**

官方 SDK：

* Google Sign-In

---

## 1️⃣ Google Console 配置（必须）

打开：

👉 [https://console.cloud.google.com](https://console.cloud.google.com)

### 创建 OAuth Client

```
API & Services
→ Credentials
→ Create Credentials
→ OAuth client ID
→ Android
```

需要填写：

```
packageName
SHA1
```

获取 SHA1：

```
./gradlew signingReport
```

---

## 2️⃣ Android 依赖

```
implementation("com.google.android.gms:play-services-auth:20.7.0")
```

---

## 3️⃣ Android actual 实现

### commonMain

```kotlin
expect class PlatformLogin {
    suspend fun login(): UserInfo?
}
```

---

### androidMain

```kotlin
actual class PlatformLogin(
    private val activity: Activity
) {

    actual suspend fun login(): UserInfo? {
        val options = GoogleSignInOptions.Builder(
            GoogleSignInOptions.DEFAULT_SIGN_IN
        )
            .requestEmail()
            .build()

        val client = GoogleSignIn.getClient(activity, options)

        val account = client.silentSignIn().await()

        return account?.let {
            UserInfo(
                id = it.id ?: "",
                name = it.displayName,
                avatar = it.photoUrl?.toString(),
                email = it.email
            )
        }
    }
}
```

⚠️ 注意：

头像来自：

```
account.photoUrl
```

---

# 三、iOS 系统账号登录（Apple ID）

必须使用：

* Sign in with Apple

---

## 1️⃣ Apple Developer 配置

进入：

👉 [https://developer.apple.com](https://developer.apple.com)

步骤：

```
Certificates → Identifiers → App ID
→ 开启 Sign in with Apple
```

然后：

```
Keys → 创建 Sign in with Apple Key
```

得到：

```
KeyID
TeamID
BundleID
```

---

## 2️⃣ iOS actual 实现

KMP 必须调用 Swift/ObjC

### commonMain

同上

---

### iosMain

```kotlin
actual class PlatformLogin {

    actual suspend fun login(): UserInfo? {
        return suspendCancellableCoroutine { cont ->

            val provider = ASAuthorizationAppleIDProvider()

            val request = provider.createRequest()
            request.requestedScopes = listOf(
                ASAuthorizationScopeFullName,
                ASAuthorizationScopeEmail
            )

            val controller = ASAuthorizationController(
                authorizationRequests = listOf(request)
            )

            controller.delegate = AppleLoginDelegate { credential ->

                val user = credential as ASAuthorizationAppleIDCredential

                cont.resume(
                    UserInfo(
                        id = user.user,
                        name = user.fullName?.givenName,
                        avatar = null, // Apple 不提供头像
                        email = user.email
                    )
                )
            }

            controller.performRequests()
        }
    }
}
```

⚠️ Apple 特性：

| 信息     | 是否可获取  |
|--------|--------|
| name   | 只第一次返回 |
| email  | 可隐藏    |
| avatar | ❌ 不提供  |
| id     | ✅ 永久   |

👉 所以：

企业产品必须：

✔ 第一次登录存数据库

---

# 四、统一 UI 层调用

```
val login = PlatformLogin()

val user = login.login()
```

UI 不需要知道平台差异

这才是 **正确 KMP 架构**

---

# 五、头像策略（企业必做）

Apple 没头像 → 推荐策略：

```
if apple:
   使用默认头像
if google:
   使用 google 头像
```

推荐：

* 使用 CDN 默认头像

---

# 六、生产级必须加的一层

真实公司不会直接用 Google / Apple

而是：

```
客户端登录 → 获取 token
→ 发给后端
→ 后端生成自己系统账号
```

否则：

⚠️ 风险：

* 无法做封号
* 无法统一账号
* 无法风控
* 无法多端同步

---

# 七、推荐最终架构（真实企业）

```
Android → Google → token → backend → user
iOS → Apple → token → backend → user
```

KMP 只负责：

👉 UI + token 获取

---

# 八、你现在最容易踩的坑

### Android

* SHA1 不对 → 登录直接失败
* debug / release SHA1 不一致

### iOS

* Apple name 只给一次
* Apple email 可能隐藏
* Apple 登录必须真机

---

# 九、如果你想更高级（推荐）

可以做：

* Firebase Auth 统一登录
* 自建 OAuth Server
* 多登录方式融合

---

# 十、如果你愿意

我可以继续给你一套：

✅ **完整 KMP 登录模块架构图（大厂级）**
✅ **Compose Multiplatform 登录 UI 示例**
✅ **完整 Google + Apple 可运行 Demo**
✅ **带后端 token 校验方案**
✅ **统一用户模型设计**

你可以告诉我：

👉 你的 KMP 是：

* Compose Multiplatform？
* 传统 Android + iOS UI？
* 是否已有后端？
* 是否要支持微信 / Facebook？

我可以直接给你 **生产级方案**。
