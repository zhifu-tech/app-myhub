package tech.zhifu.app.myhub.analytics

/**
 * Mock Provider 用于测试
 */
class MockProvider(
    override val name: String = "Mock",
    override val supportedPlatforms: Set<Platform> = setOf(Platform.JVM),
    override val supportedRegions: Set<Region> = setOf(Region.DOMESTIC, Region.OVERSEAS)
) : BaseAnalyticsProvider() {

    val loggedEvents = mutableListOf<AnalyticsEvent>()
    val userProperties = mutableMapOf<String, AnalyticsValue?>()
    var currentUserId: String? = null
    var currentScreenName: String? = null
    var currentScreenClass: String? = null
    var resetCalled = false

    override suspend fun initialize(config: ProviderConfig) {
        markAsReady()
    }

    override fun doLogEvent(event: AnalyticsEvent) {
        loggedEvents.add(event)
    }

    override fun setUserProperty(key: String, value: AnalyticsValue?) {
        userProperties[key] = value
    }

    override fun setUserId(userId: String?) {
        this.currentUserId = userId
    }

    override fun setScreen(screenName: String, screenClass: String?) {
        this.currentScreenName = screenName
        this.currentScreenClass = screenClass
    }

    override fun reset() {
        resetCalled = true
        loggedEvents.clear()
        userProperties.clear()
        currentUserId = null
        currentScreenName = null
        currentScreenClass = null
    }
}
