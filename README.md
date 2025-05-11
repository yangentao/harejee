## Hare Jee

Simple Code
```kotlin
@MultipartConfig
@WebFilter(urlPatterns = ["/*"], asyncSupported = true)
class MainFilter : HareFilter() {
    override val appName: String = "App"

    override fun onCreate() {
        HarePool.push(PostgresConnectionBuilder(user = DB.user, password = DB.pwd, dbname = DB.name, host = DB.host))
        SQLog.DEBUG_LOGGER = { logd(it) }
        SQLog.ERROR_LOGGER = { loge(it) }
        app.apply {
            migrate(UserAccount::class, AdminAccount::class)
            migrate(Messages::class, Relations::class, Topics::class)
            migrate(Upload::class)
            every(10.timeMinutes) {
                Topics.cleanExpired()
            }
            router {
                action("name") {
                    it.sendText(this@apply.name)
                }
                group(PubPage::class, ImagePage::class)
                group(AccountPage::class, RelationPage::class, ChatPage::class, TopicPage::class)
                before(TokenCheck::checkSessionAction)
                before(::checkAccountState)
            }
        }
        logi("Work: ", app.work.canonicalPath)
    }

    override fun onDestory() {
        // do clean 
    }
}

```


