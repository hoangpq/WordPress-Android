package org.wordpress.android.ui.sitecreation.creation

import android.arch.core.executor.testing.InstantTaskExecutorRule
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.wordpress.android.fluxc.Dispatcher
import org.wordpress.android.fluxc.action.SiteAction
import org.wordpress.android.fluxc.annotations.action.Action
import org.wordpress.android.fluxc.store.SiteStore
import org.wordpress.android.fluxc.store.SiteStore.NewSitePayload
import org.wordpress.android.fluxc.store.SiteStore.OnNewSiteCreated
import org.wordpress.android.fluxc.store.SiteStore.SiteVisibility
import org.wordpress.android.test

private val DUMMY_SITE_DATA: NewSiteCreationServiceData = NewSiteCreationServiceData(
        123,
        "m1p2",
        "title",
        "tagLine",
        "slug"
)
private const val LANGUAGE_ID = "lang_id"

@RunWith(MockitoJUnitRunner::class)
class CreateSiteUseCaseTest {
    @Rule
    @JvmField val rule = InstantTaskExecutorRule()

    @Mock lateinit var dispatcher: Dispatcher
    @Mock lateinit var store: SiteStore
    private lateinit var useCase: CreateSiteUseCase
    private lateinit var event: OnNewSiteCreated

    @Before
    fun setUp() {
        useCase = CreateSiteUseCase(dispatcher, store)
        event = OnNewSiteCreated()
        event.newSiteRemoteId = 123
    }

    @Test
    fun coroutineResumedWhenResultEventDispatched() = test {
        whenever(dispatcher.dispatch(any())).then { useCase.onNewSiteCreated(event) }
        val resultEvent = useCase.createSite(DUMMY_SITE_DATA, LANGUAGE_ID)

        assertThat(resultEvent).isEqualTo(event)
    }

    @Test
    fun verifySiteDataPropagated() = test {
        whenever(dispatcher.dispatch(any())).then { useCase.onNewSiteCreated(event) }
        useCase.createSite(DUMMY_SITE_DATA, LANGUAGE_ID)

        val captor = ArgumentCaptor.forClass(Action::class.java)
        verify(dispatcher).dispatch(captor.capture())

        assertThat(captor.value.type).isEqualTo(SiteAction.CREATE_NEW_SITE)
        assertThat(captor.value.payload).isInstanceOf(NewSitePayload::class.java)
        val payload = captor.value.payload as NewSitePayload
        assertThat(payload.siteName).isEqualTo(DUMMY_SITE_DATA.domain)
        assertThat(payload.siteTitle).isEqualTo(DUMMY_SITE_DATA.siteTitle)
        // TODO uncomment when the API is ready
//        assertThat(payload.segmentId).isEqualTo(DUMMY_SITE_DATA.segmentId)
//        assertThat(payload.verticalId).isEqualTo(DUMMY_SITE_DATA.verticalId)
//        assertThat(payload.tagLine).isEqualTo(DUMMY_SITE_DATA.siteTagLine)
    }

    @Test
    fun verifyDryRunIsFalse() = test {
        whenever(dispatcher.dispatch(any())).then { useCase.onNewSiteCreated(event) }
        useCase.createSite(DUMMY_SITE_DATA, LANGUAGE_ID)

        val captor = ArgumentCaptor.forClass(Action::class.java)
        verify(dispatcher).dispatch(captor.capture())

        val payload = captor.value.payload as NewSitePayload
        assertThat(payload.dryRun).isEqualTo(false)
    }

    @Test
    fun verifyCreatesPublicSite() = test {
        whenever(dispatcher.dispatch(any())).then { useCase.onNewSiteCreated(event) }
        useCase.createSite(DUMMY_SITE_DATA, LANGUAGE_ID)

        val captor = ArgumentCaptor.forClass(Action::class.java)
        verify(dispatcher).dispatch(captor.capture())

        val payload = captor.value.payload as NewSitePayload
        assertThat(payload.visibility).isEqualTo(SiteVisibility.PUBLIC)
    }

    @Test
    fun verifyPropagatesLanguageId() = test {
        whenever(dispatcher.dispatch(any())).then { useCase.onNewSiteCreated(event) }
        useCase.createSite(DUMMY_SITE_DATA, LANGUAGE_ID)

        val captor = ArgumentCaptor.forClass(Action::class.java)
        verify(dispatcher).dispatch(captor.capture())

        val payload = captor.value.payload as NewSitePayload
        assertThat(payload.language).isEqualTo(LANGUAGE_ID)
    }
}
