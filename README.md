# amazing-talker-homework

可能在您的環境上會遇到的問題
-
### 1. Android Gradle plugin requires Java 11 to run. You are currently using Java 1.8.
-  Android Studio 新版安裝環境會自帶JDK11的版本，於 Preferences -> Build, Execution, Deployment -> Build Tools -> Gradle -> 選擇 Gradle JDK 版本為 Embeded JDK Version 11
- 可以嘗試升級至 Android Studio 最新版本後，會自帶 Embeded JDK 11
### 2. Root build.gradle 配置方式可能和您習慣的不一樣，專案內的配置是新版 Android Studio 預設產生

回家作業說明
-
### 壓縮檔內包含兩個專案分別是 `appWithCoroutineFlow` 和 `appWithKoinRxLivedata`，以現有主流與較新的技術線實做
- appWithCoroutineFlow : Retrofit + Coroutine + Flow 以 MVVM 架構實作，原本欲使用 Dagger 展示注入，但 Dagger 無法注入泛型Class，該專案Base元件以泛型封裝，因此實作Dagger效益並不明顯．因此實作下面另一個專案

- appWithKoinRxLivedata : Retrofit + ReactiveX + LiveData 以 MVVM 架構實作，原欲使用Hilt套件注入，但個人對於Hilt尚未直接實作過，學習上手時間可能會超過作業時限，因此以較熟悉與輕量的注入套件Koin實作

功能實現
- CalendarView 為獨立 View，解耦以利主程式彈性調用或刪除
- CalendarView 可由外部設定指定老師，並刷新可預約之時間
- CalendarView 當時區自動或手動至系統設定切換時，可預約時間自動刷新，並更新時區說明
- CalendarView 網路狀態改變時，若CalendarView 當前是資料載入失敗，則自動重試
- CalendarView 阻擋用戶至系統修改時間，用以存取過去的時間，因此當前時間由 Server 取得
- Schedule API 假定 Server 資料可能異常，因此在 RemoteDataSource 層級，會先檢查並阻擋，已避免 App 閃退或 UI 發生異常
- Schedule API 假定 Server 資料可能異常，若 Server Response 包含預約的時間於過去，則 UI 層將阻擋用戶選擇過去的時間
- 事件通知中心以 RxBus & FlowBus 實作
- 預設語系為en-US，並另外實現 zh-CN, zh-HK, zh-TW


其他發現
- 若未來可預約時間粒度（目前是30分鐘）可能異動，建議由 Server 端 API 格式中指定，App 自動適應，以減少未來 App 發布擴散的過渡時期
            